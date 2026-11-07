package com.boldblot.backgammonplyground.ui.shell

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.boldblot.backgammonplyground.PlyGroundApp
import com.boldblot.backgammonplyground.ViewPrefs
import com.boldblot.backgammonplyground.data.PositionsRepository
import com.boldblot.backgammonplyground.ui.home.SettingsGroup
import com.boldblot.backgammonplyground.ui.positions.QuizSpec
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** What the My positions window shows on the right; the tree on its left picks it. */
sealed interface PositionsPane {
    /** All positions (null), No folder, or a folder. */
    data class Folder(val id: Long?) : PositionsPane
    data object Errors : PositionsPane
    data object Marked : PositionsPane
    data object Learned : PositionsPane
}

/** The Quiz setup dialog's starting point. */
data class QuizSetup(val folders: List<Long> = emptyList(), val filter: Set<String> = emptySet())

/**
 * Which windows and dialogs are open, and the requests that reach the main board from anywhere: the menu bar, the toolbar and
 * the separate windows all go through this.
 */
class ShellState(val app: PlyGroundApp) {
    /** The main window, once it exists. */
    var mainWindow by mutableStateOf<java.awt.Window?>(null)

    private fun raiseMain() {
        mainWindow?.let { it.toFront(); it.requestFocus() }
    }

    var libraryOpen by mutableStateOf(false)
    var positionsOpen by mutableStateOf(false)
    var statisticsOpen by mutableStateOf(false)
    var aboutOpen by mutableStateOf(false)
    var speedTestOpen by mutableStateOf(false)
    /** The Welcome dialog: null is closed; true when it comes up on a new install. */
    var welcome by mutableStateOf<Boolean?>(null)
    var preferencesOpen by mutableStateOf(false)
    var preferencesGroup by mutableStateOf(SettingsGroup.TRAINING)
    var quizSetup by mutableStateOf<QuizSetup?>(null)

    /** What the My positions window shows. */
    var positionsPane by mutableStateOf<PositionsPane>(PositionsPane.Folder(null))
    /** File → Export positions asked for its dialog. */
    var exportPositions by mutableStateOf(false)
    /** File → Export matches asked for it: the Match Library says how. */
    var exportMatches by mutableStateOf(false)

    private val view = ViewPrefs.load()
    var panelVisible by mutableStateOf(view.panel)
        private set
    var toolbarVisible by mutableStateOf(view.toolbar)
        private set

    fun togglePanel() { panelVisible = !panelVisible; saveView() }
    fun toggleToolbar() { toolbarVisible = !toolbarVisible; saveView() }
    private fun saveView() = ViewPrefs.save(ViewPrefs.Saved(panelVisible, toolbarVisible))

    /** Shows [route] on the main board and brings the main window forward. */
    fun open(route: String) {
        raiseMain()
        app.navRequests.tryEmit(route)
    }

    /** The Start view: the board with the New game options. */
    fun newGame() {
        panelVisible = true
        open(START_ROUTE)
    }

    fun openPreferences(group: SettingsGroup? = null) {
        group?.let { preferencesGroup = it }
        preferencesOpen = true
    }

    fun openPositions(pane: PositionsPane? = null) {
        pane?.let { positionsPane = it }
        positionsOpen = true
    }

    /** The saved quick quiz, without its deleted folders. */
    fun quickQuiz() {
        app.appScope.launch {
            val q = QuizSpec.quick(app.settings.settings.first())
            val known = app.positions.categories().map { it.id }.toSet() + PositionsRepository.UNCATEGORIZED
            open("positions/quiz/${q.copy(folders = q.folders.filter { it in known }).encode()}")
        }
    }
}

const val START_ROUTE = "start"

/** The Back button of a view opened over [previous], the route it goes back to. */
fun backLabel(previous: String?): String = when {
    previous == "game" -> "Back to game"
    previous == START_ROUTE -> "Back to start"
    previous?.startsWith("review/") == true -> "Back to review"
    previous?.startsWith("positions/quiz/") == true -> "Back to quiz"
    else -> "Back"
}

val LocalShell = staticCompositionLocalOf<ShellState> { error("No shell") }
