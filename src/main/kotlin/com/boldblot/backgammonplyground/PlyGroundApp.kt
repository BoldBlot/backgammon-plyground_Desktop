package com.boldblot.backgammonplyground

import com.boldblot.backgammonplyground.core.NewErrors
import com.boldblot.backgammonplyground.ui.positions.libraryAverageError
import com.boldblot.backgammonplyground.data.AppDatabase
import com.boldblot.backgammonplyground.data.BackgroundPriority
import com.boldblot.backgammonplyground.data.ErrorsMode
import com.boldblot.backgammonplyground.data.ErrorsSort
import com.boldblot.backgammonplyground.data.PositionsRepository
import com.boldblot.backgammonplyground.data.Repository
import com.boldblot.backgammonplyground.data.SettingsStore
import com.boldblot.backgammonplyground.engine.EngineService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** The running app, for composables that need it ([PlyGroundApp] is passed down explicitly elsewhere). */
val LocalApp = androidx.compose.runtime.staticCompositionLocalOf<PlyGroundApp?> { null }

class PlyGroundApp {

    val appScope = CoroutineScope(SupervisorJob())

    val engine: EngineService by lazy { EngineService() }
    val database: AppDatabase by lazy { AppDatabase.create(DataDirs.file("plyground.db").path) }
    val repository: Repository by lazy { Repository(database) }
    val positions: PositionsRepository by lazy { PositionsRepository(database) }
    val settings: SettingsStore by lazy { SettingsStore(DataDirs.file("settings.preferences_pb")) }
    /** A positions file opened with PlyGround (or picked in My positions), waiting for its import dialog. */
    val pendingImport = MutableStateFlow<String?>(null)
    /** A .mat match file opened with PlyGround, waiting for the Match Library's import dialog. */
    val pendingMatch = MutableStateFlow<String?>(null)
    /** Routes the menu bar, the toolbar and the separate windows show on the main board (collected in AppNav). */
    val navRequests = MutableSharedFlow<String>(extraBufferCapacity = 4)
    /** XGID text from the Edit menu's Paste, for the Analyze Position screen. */
    val pasteXgid = MutableSharedFlow<String>(extraBufferCapacity = 1)

    /** A file opened from the system, the menu or a drop: a match goes to the Match Library, positions to My positions. */
    fun openFile(file: java.io.File): Boolean {
        val text = runCatching { file.readText() }.getOrNull() ?: run { showMessage("The file could not be read."); return false }
        if (com.boldblot.backgammonplyground.ui.home.looksLikeMatch(text)) pendingMatch.value = text else pendingImport.value = text
        return true
    }

    init {
        AppLog.init()
        // Warm up the engine (data extraction + neural net loading) in the background.
        appScope.launch { engine.start() }
        // 0.7.4, for the testers' phones; remove before the production release (see Positions.migrateFolders074).
        appScope.launch {
            if (!settings.settings.first().folders074) {
                val collecting = positions.migrateFolders074()
                settings.update { it.copy(folders074 = true, errorsMode = if (collecting) ErrorsMode.AUTOMATIC else it.errorsMode) }
            }
        }
        // A new error: autofilters take the ones they match; the automatic methods file it at once when it
        // passes the fixed or adaptive threshold ([Settings.errorsSort]), Top X at the end of the match. A filed
        // error skips New errors; with the automatic methods New errors keeps the others, a rolling list.
        repository.onNewBlunder = { b ->
            val s = settings.settings.first()
            var filed = false
            for (c in positions.categories()) {
                val filter = c.filter?.takeIf { c.autoAdd }?.let { com.boldblot.backgammonplyground.core.Labels.decode(it) } ?: continue
                if (com.boldblot.backgammonplyground.core.Labels.matchesAll(b.labels, s.visible(filter))) {
                    positions.addBlunders(listOf(b), c.id, jacoby)
                    filed = true
                }
            }
            val threshold = when (s.errorsMode) {
                ErrorsMode.AUTOMATIC -> s.errorMin
                ErrorsMode.ADAPTIVE -> NewErrors.adaptiveThreshold(s.errorMin, s.adaptiveRatio, libraryAverageError(this))
                else -> null
            }
            if (!b.entity.opponent && threshold != null && b.entity.error >= threshold) {
                positions.fileErrors(listOf(b), s.errorsSort == ErrorsSort.SPLIT, jacoby)
                filed = true
            }
            if (filed) repository.removeBlunder(b.entity.id)
            // An error analyzed after its match ended (the background analysis) can still be among its top X.
            if (!filed && s.errorsMode == ErrorsMode.TOP_X && repository.matchOver(b.entity.matchId)) fileTopX(b.entity.matchId)
            if (s.errorsMode != ErrorsMode.MANUAL) trimNewErrors(s.excludedKeep)
        }
        repository.libraryMax = { settings.settings.first().libraryMax }
        repository.onMatchFinished = { id -> if (settings.settings.first().errorsMode == ErrorsMode.TOP_X) fileTopX(id) }
        // Analysis uses the engine chosen in Settings.
        appScope.launch {
            settings.settings.collect {
                engine.analysisKind = it.analysisEngine
                // Advanced engine settings (0 = the default: all cores for the game, a quarter for the analysis).
                engine.gameThreads = it.gameThreads.takeIf { n -> n > 0 } ?: engine.cores
                engine.backgroundThreads = it.backgroundThreads.takeIf { n -> n > 0 } ?: EngineService.defaultBackgroundThreads(engine.cores)
                engine.backgroundLow = it.backgroundPriority == BackgroundPriority.LOW
                engine.net = it.puretdNet
                engine.cubefulCheckers = it.cubefulCheckers
            }
        }
    }

    private val jacoby: suspend (Long) -> Boolean = { id -> repository.match(id)?.jacoby ?: true }

    /** Top X per match: files the biggest errors of match [id] still in New errors, up to X with the ones filed before. */
    private suspend fun fileTopX(id: Long) {
        val s = settings.settings.first()
        val waiting = repository.blundersOf(id)
        val already = s.topXFiledOf(id)
        val chosen = NewErrors.topX(waiting.map { it.entity.id to it.entity.error }, s.topX, s.errorMin, already).toSet()
        if (chosen.isEmpty()) return
        positions.fileErrors(waiting.filter { it.entity.id in chosen }, s.errorsSort == ErrorsSort.SPLIT, jacoby)
        repository.removeBlunders(chosen)
        settings.update { it.copy(topXFiled = it.withTopXFiled(id, already + chosen.size)) }
    }

    /** The automatic methods' New errors: only the [keep] newest errors left out stay. */
    private suspend fun trimNewErrors(keep: Int) {
        val ids = repository.blunders(com.boldblot.backgammonplyground.data.BlunderSort.NEWEST).map { it.entity.id }
        NewErrors.overflow(ids, keep).takeIf { it.isNotEmpty() }?.let { repository.removeBlunders(it) }
    }

    /** Called when the window closes: stops new work and lets the database finish what it is writing. */
    fun shutdown() {
        AppLog.i("Closing")
        appScope.cancel()
        runCatching { database.close() }
        SingleInstance.release()
    }
}
