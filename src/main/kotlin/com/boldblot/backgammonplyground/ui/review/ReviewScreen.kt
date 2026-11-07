package com.boldblot.backgammonplyground.ui.review

import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.AssistChip
import com.boldblot.backgammonplyground.ui.components.ChipGapH
import com.boldblot.backgammonplyground.ui.components.ChipGapV
import com.boldblot.backgammonplyground.ui.components.PanelTitle
import com.boldblot.backgammonplyground.ui.components.desktopVerticalScroll
import com.boldblot.backgammonplyground.ui.shell.BoardAndPanel
import com.boldblot.backgammonplyground.ui.shell.Cmd
import com.boldblot.backgammonplyground.ui.shell.RegisterCommand
import com.boldblot.backgammonplyground.ui.shell.RegisterStatus

import com.boldblot.backgammonplyground.ui.components.ScrollingLazyColumn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import com.boldblot.backgammonplyground.KeyHandler
import com.boldblot.backgammonplyground.LocalApp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boldblot.backgammonplyground.PlyGroundApp
import com.boldblot.backgammonplyground.core.StudyPosition
import com.boldblot.backgammonplyground.ui.positions.SaveToPositionsDialog
import com.boldblot.backgammonplyground.core.Board
import com.boldblot.backgammonplyground.core.CpuLevel
import com.boldblot.backgammonplyground.core.CubeState
import com.boldblot.backgammonplyground.core.DecisionKind
import com.boldblot.backgammonplyground.core.DecisionRecord
import com.boldblot.backgammonplyground.core.Depth
import com.boldblot.backgammonplyground.core.Reanalysis
import com.boldblot.backgammonplyground.core.GameState
import com.boldblot.backgammonplyground.core.MoveNotation
import com.boldblot.backgammonplyground.core.PerformanceTotals
import com.boldblot.backgammonplyground.core.Player
import com.boldblot.backgammonplyground.core.Severity
import com.boldblot.backgammonplyground.core.Summaries
import com.boldblot.backgammonplyground.core.XgRules
import com.boldblot.backgammonplyground.data.GameEntity
import com.boldblot.backgammonplyground.data.MatchEntity
import com.boldblot.backgammonplyground.data.Settings
import com.boldblot.backgammonplyground.ui.board.ArrowColors
import com.boldblot.backgammonplyground.ui.board.BoardView
import com.boldblot.backgammonplyground.ui.board.DiceView
import com.boldblot.backgammonplyground.ui.board.MoveArrow
import com.boldblot.backgammonplyground.ui.board.arrowsFor
import com.boldblot.backgammonplyground.ui.board.boardColorsFor
import com.boldblot.backgammonplyground.ui.components.CandidateList
import com.boldblot.backgammonplyground.ui.components.CubeTable
import com.boldblot.backgammonplyground.ui.components.Labelled
import com.boldblot.backgammonplyground.ui.components.PlayedBestBar
import com.boldblot.backgammonplyground.ui.components.SeverityBadge
import com.boldblot.backgammonplyground.ui.components.CheckerIcon
import com.boldblot.backgammonplyground.ui.components.ErrorPill
import com.boldblot.backgammonplyground.ui.components.errText
import com.boldblot.backgammonplyground.ui.components.decisionCube
import com.boldblot.backgammonplyground.ui.components.describeAction
import com.boldblot.backgammonplyground.ui.components.bestCubeLabel
import com.boldblot.backgammonplyground.ui.components.fmt
import com.boldblot.backgammonplyground.ui.components.severityColor
import kotlinx.coroutines.Job
import com.boldblot.backgammonplyground.core.CubeEval
import com.boldblot.backgammonplyground.core.CubeContext
import kotlinx.coroutines.flow.first
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.filled.Folder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ReviewFilter(val label: String) { ALL("All"), MINE("Mine"), MISTAKES("My mistakes") }

/**
 * The cube decision before a checker play (the Cube chip): the recorded one ([record]), or one analyzed when
 * asked ([eval] alone); [available] false when the player couldn't double.
 */
data class TurnCube(val record: DecisionRecord?, val eval: CubeEval?, val available: Boolean, val busy: Boolean = false)

data class ReviewState(
    val loading: Boolean = true,
    val match: MatchEntity? = null,
    val games: List<Pair<GameEntity, GameState>> = emptyList(),
    val gameIndex: Int = 0,
    /** Null: Marked alone ([marked] is on). */
    val filter: ReviewFilter? = ReviewFilter.ALL,
    /** The match being finished has marked positions, so the Marked chip shows. */
    val hasMarks: Boolean = false,
    /**
     * The Marked chip. With All, every decision stays in the list and "Next" also stops at the marked
     * ones; with any other filter (or alone) the list holds only the marked ones and "Next" stops at those.
     */
    val marked: Boolean = false,
    /** Index into the filtered decision list. */
    val position: Int = 0,
    /** Candidate whose arrow is shown; null = played and best together. */
    val candidate: Int? = null,
    /** Arrows, the bare position, or the position after the move (chosen in the list, or played). */
    val view: ReviewView = ReviewView.ARROWS,
    /** The selected decision's analysis is closed (tapped again), so the whole list shows. */
    val collapsed: Boolean = false,
    val settings: Settings = Settings(),
    /** Keys (XGID, doubled) of My positions, for the folder marker on decisions filed there. */
    val library: Set<Pair<String, Boolean>> = emptySet(),
    /** Top X per match, before the match ends: the errors going to My positions at its end (game index, decision index). */
    val goingIn: Set<Pair<Int, Int>> = emptySet(),
    /** The Cube chip on a checker play: that turn's cube decision is shown instead ([turnCube] once known). */
    val cubeShown: Boolean = false,
    val turnCube: TurnCube? = null,
    /** Analyzing again: decisions done and in all; null = not running. */
    val progress: Pair<Int, Int>? = null,
    val message: String? = null,
) {
    val factor: Float get() = XgRules.errorFactor(match?.length ?: 0)
    val game: GameState? get() = games.getOrNull(gameIndex)?.second

    val decisions: List<IndexedValue<DecisionRecord>>
        get() {
            // Obvious, error-free "no double" decisions (not counted by XG) are left out of the list.
            val marks = game?.marked.orEmpty()
            val all = game?.decisions?.withIndex()?.filterNot { (i, d) ->
                i !in marks && d.kind == DecisionKind.NO_DOUBLE && !d.counted && (d.error ?: 0f) < 0.0005f
            } ?: return emptyList()
            if (onlyMarked) return all.filter { it.index in marks }
            return when (filter) {
                ReviewFilter.ALL, null -> all
                ReviewFilter.MINE -> all.filter { it.value.player == Player.HUMAN }
                ReviewFilter.MISTAKES -> all.filter {
                    it.value.player == Player.HUMAN && severityOf(it.value).atLeast(Severity.ERROR)
                }
            }
        }

    val current: DecisionRecord? get() = decisions.getOrNull(position)?.value

    /** Only the marked decisions are listed. */
    val onlyMarked: Boolean get() = hasMarks && marked && filter != ReviewFilter.ALL

    /** Whether the decision shown is marked (only while the match's marks count). */
    val currentMarked: Boolean get() = hasMarks && decisions.getOrNull(position)?.index?.let { it in game?.marked.orEmpty() } == true

    /** A decision tried again is graded by its first try. */
    fun severityOf(d: DecisionRecord): Severity = XgRules.severity((d.scoredError ?: 0f) * factor)

    private fun isMistake(d: DecisionRecord) = d.player == Player.HUMAN && severityOf(d).atLeast(Severity.ERROR)

    /** Where "Next" stops in [g]: at mistakes, also at marked decisions with Marked on, and only at those when only they are listed. */
    private fun isStop(g: GameState?, d: IndexedValue<DecisionRecord>): Boolean {
        val isMarked = hasMarks && marked && d.index in g?.marked.orEmpty()
        return isMarked || (!onlyMarked && isMistake(d.value))
    }

    /** The next stop of "Next" (game index, list position): later in this game, or in a later game of the match. */
    val nextMistake: Pair<Int, Int>?
        get() {
            val list = decisions
            (position + 1 until list.size).firstOrNull { isStop(game, list[it]) }?.let { return gameIndex to it }
            for (gi in gameIndex + 1 until games.size) {
                val other = copy(gameIndex = gi, position = 0).decisions
                other.indices.firstOrNull { isStop(games[gi].second, other[it]) }?.let { return gi to it }
            }
            return null
        }

    /** The previous stop (game index, list position): earlier in this game, or in an earlier game of the match. */
    val previousMistake: Pair<Int, Int>?
        get() {
            val list = decisions
            (position - 1 downTo 0).firstOrNull { isStop(game, list[it]) }?.let { return gameIndex to it }
            for (gi in gameIndex - 1 downTo 0) {
                val other = copy(gameIndex = gi, position = 0).decisions
                other.indices.lastOrNull { isStop(games[gi].second, other[it]) }?.let { return gi to it }
            }
            return null
        }

    /**
     * True for a game played after the Crawford game. A .mat file cannot say that the
     * Crawford game is over, so on its own such a game is read as the Crawford game.
     */
    val isPostCrawford: Boolean
        get() {
            val m = match ?: return false
            val (e, g) = games.getOrNull(gameIndex) ?: return false
            return m.length > 0 && m.crawford && !g.crawford &&
                (e.scoreBeforeHuman == m.length - 1 || e.scoreBeforeCpu == m.length - 1)
        }
}

class ReviewViewModel(
    val app: PlyGroundApp,
    private val matchId: Long,
    /** Game to open (its number); null = the last one. */
    private val startGame: Int? = null,
    /** Open on "My mistakes". */
    private val mistakesOnly: Boolean = false,
    /** Opened from the summary of a game or match just played: its marked positions still count. */
    private val marksCount: Boolean = false,
    /** "Review marked": open on the marked positions alone. */
    private val markedOnly: Boolean = false,
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewState(filter = if (mistakesOnly) ReviewFilter.MISTAKES else ReviewFilter.ALL))
    val state: StateFlow<ReviewState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val match = app.repository.match(matchId)
            val games = app.repository.games(matchId)
            val index = games.indexOfFirst { it.first.number == startGame }.takeIf { it >= 0 } ?: (games.size - 1).coerceAtLeast(0)
            // Review opens with Marked on (Review my mistakes keeps to the mistakes; the chip can add them).
            val hasMarks = marksCount && games.any { it.second.marked.isNotEmpty() }
            _state.update {
                it.copy(
                    loading = false, match = match, games = games, gameIndex = index, hasMarks = hasMarks,
                    marked = hasMarks && !mistakesOnly, filter = if (hasMarks && markedOnly) null else it.filter,
                )
            }
            loadLibrary()
        }
        viewModelScope.launch { app.settings.settings.collect { s -> _state.update { it.copy(settings = s) } } }
    }

    /** The folder markers: what is in My positions, and with Top X what will go at the end of the match. */
    private suspend fun loadLibrary() {
        val keys = com.boldblot.backgammonplyground.ui.positions.libraryKeys(app)
        val st = _state.value
        val s = app.settings.settings.first()
        val m = st.match
        var going = emptySet<Pair<Int, Int>>()
        if (m != null && s.errorsMode == com.boldblot.backgammonplyground.data.ErrorsMode.TOP_X && m.status == com.boldblot.backgammonplyground.data.MatchStatus.IN_PROGRESS) {
            val factor = XgRules.errorFactor(m.length)
            val errors = st.games.flatMapIndexed { gi, (_, g) ->
                g.decisions.mapIndexedNotNull { di, d ->
                    val w = (d.scoredError ?: return@mapIndexedNotNull null) * factor
                    if (d.player == Player.HUMAN && !d.hinted && severityAtLeastError(w)) ((gi.toLong() shl 32) or di.toLong()) to w else null
                }
            }
            going = com.boldblot.backgammonplyground.core.NewErrors.topX(errors, s.topX, s.errorMin, s.topXFiledOf(m.id))
                .map { (it shr 32).toInt() to (it and 0xffffffffL).toInt() }.toSet()
        }
        _state.update { it.copy(library = keys, goingIn = going) }
    }

    private fun severityAtLeastError(w: Float) = XgRules.severity(w).atLeast(Severity.ERROR)

    fun selectGame(i: Int) = _state.update { it.copy(gameIndex = i, position = 0, candidate = null, collapsed = false, cubeShown = false, turnCube = null) }
    fun setFilter(f: ReviewFilter) = _state.update {
        // Marked would hide what Mine or My mistakes are asked to show.
        it.copy(filter = f, marked = it.marked && f == ReviewFilter.ALL, position = 0, candidate = null, collapsed = false, cubeShown = false, turnCube = null)
    }

    /** The Marked chip. Turned off when it was alone, every decision shows again. */
    fun toggleMarked() = _state.update {
        it.copy(
            marked = !it.marked, filter = it.filter ?: ReviewFilter.ALL,
            position = 0, candidate = null, collapsed = false, cubeShown = false, turnCube = null,
        )
    }
    fun go(pos: Int) = _state.update {
        it.copy(position = pos.coerceIn(0, (it.decisions.size - 1).coerceAtLeast(0)), candidate = null, collapsed = false, cubeShown = false, turnCube = null)
    }

    /** The Cube chip: the selected checker play's cube decision (recorded, or analyzed now), or back to the play. */
    fun toggleCube() {
        val st = _state.value
        if (st.cubeShown) { _state.update { it.copy(cubeShown = false) }; return }
        val m = st.match ?: return
        val g = st.game ?: return
        val (index, d) = st.decisions.getOrNull(st.position) ?: return
        if (d.kind != DecisionKind.CHECKER) return
        // The doubling decision of the same turn comes just before it (an answer to a double may sit between).
        var k = index - 1
        var rec: DecisionRecord? = null
        while (k >= 0 && g.decisions[k].kind != DecisionKind.CHECKER) {
            val c = g.decisions[k]
            if (c.player == d.player && (c.kind == DecisionKind.DOUBLE || c.kind == DecisionKind.NO_DOUBLE)) { rec = c; break }
            k--
        }
        if (rec?.cubeEval != null) { _state.update { it.copy(cubeShown = true, turnCube = TurnCube(rec, rec.cubeEval, true)) }; return }
        val ctx = CubeContext(m.length, d.score, d.crawford, m.jacoby, d.cube, d.player)
        _state.update { it.copy(cubeShown = true, turnCube = TurnCube(rec, null, true, busy = true)) }
        viewModelScope.launch {
            val s = _state.value.settings
            val e = runCatching { app.engine.run { cubeDecision(d.board, ctx, Depth(s.cubePlies).code) } }.getOrNull()
            _state.update { st2 ->
                if (st2.decisions.getOrNull(st2.position)?.value !== d) st2
                else st2.copy(turnCube = TurnCube(rec, e, e?.cubeAvailable ?: false))
            }
        }
    }

    /** The next decision, or the first of the next game after the last one. */
    fun next() {
        val st = _state.value
        if (st.position < st.decisions.size - 1) go(st.position + 1)
        else if (st.gameIndex < st.games.size - 1) selectGame(st.gameIndex + 1)
    }

    /** The previous decision, or the last of the previous game before the first one. */
    fun previous() {
        val st = _state.value
        if (st.position > 0) go(st.position - 1)
        else if (st.gameIndex > 0) _state.update {
            val prev = it.copy(gameIndex = it.gameIndex - 1)
            prev.copy(position = (prev.decisions.size - 1).coerceAtLeast(0), candidate = null, collapsed = false, cubeShown = false, turnCube = null)
        }
    }

    /** A tap on a decision in the list: opens it, or closes its analysis when it is open already. */
    fun tapDecision(pos: Int) {
        if (pos == _state.value.position) _state.update { it.copy(collapsed = !it.collapsed) } else go(pos)
    }
    fun selectCandidate(i: Int?) = _state.update { it.copy(candidate = i) }
    fun setView(v: ReviewView) = _state.update { it.copy(view = v) }

    /**
     * The game being reviewed (or the whole match) in the .mat format read by eXtreme
     * Gammon and GNU Backgammon: file name and content.
     */
    fun exportMat(wholeMatch: Boolean): Pair<String, String>? {
        val s = _state.value
        val match = s.match ?: return null
        if (s.games.isEmpty()) return null
        return MatFiles.matchFile(
            match, s.games, only = if (wholeMatch) null else s.games.getOrNull(s.gameIndex) ?: return null,
            playerName = s.settings.playerName, humanFirst = s.settings.meFirstInFiles,
        )
    }

    private var reanalysis: Job? = null

    /** Analyzes the game shown (or the whole match) again; the results replace the stored analysis. */
    fun reanalyze(r: ReanalyzeRequest) {
        val s = _state.value
        val match = s.match ?: return
        if (reanalysis?.isActive == true) return
        val targets = if (r.wholeMatch) s.games.indices.toList() else listOf(s.gameIndex)
        val re = Reanalysis(app.engine.of(r.engine), r.checker.code, Depth(r.cubePlies).code)
        val total = targets.sumOf { i -> s.games[i].second.decisions.count { re.wanted(it, r.cpu) } }
        reanalysis = viewModelScope.launch {
            var done = 0
            var games = 0
            _state.update { it.copy(progress = 0 to total, message = null) }
            try {
                for (i in targets) {
                    val (e, g) = _state.value.games[i]
                    val decisions = g.decisions.toMutableList()
                    for ((k, d) in g.decisions.withIndex()) {
                        if (!re.wanted(d, r.cpu)) continue
                        decisions[k] = app.engine.run { re.decision(d, match.length, match.jacoby) }
                        done++
                        _state.update { it.copy(progress = done to total) }
                    }
                    val g2 = g.copy(decisions = decisions)
                    val e2 = app.repository.saveReanalyzedGame(e, g2, match.length)
                    games++
                    _state.update { st -> st.copy(games = st.games.toMutableList().also { it[i] = e2 to g2 }) }
                }
                val depth = "${r.engine.label} ${r.checker.label()}, cube ${r.cubePlies}-ply"
                _state.update { it.copy(progress = null, message = "Analyzed again: $depth.") }
            } catch (c: kotlinx.coroutines.CancellationException) {
                _state.update { it.copy(progress = null, message = "Stopped. $games of ${targets.size} games were analyzed again.") }
                throw c
            } catch (t: Throwable) {
                _state.update { it.copy(progress = null, message = "Analysis failed: ${t.message}") }
            }
        }
    }

    /**
     * Evaluates the decision on show again ([ReanalyzeRequest.engine] and depths), or with the Cube chip on, that turn's cube decision.
     * The result replaces the stored analysis; a cube decision that was never recorded (a no double left out of the list) has none to
     * replace, so it is only shown.
     */
    fun evaluate(r: ReanalyzeRequest) {
        val s = _state.value
        val match = s.match ?: return
        val (index, d) = s.decisions.getOrNull(s.position) ?: return
        if (reanalysis?.isActive == true) return
        val turnCube = s.turnCube.takeIf { s.cubeShown }
        val gameIndex = s.gameIndex
        val engine = app.engine.of(r.engine)
        val cubeDepth = Depth(r.cubePlies).code
        val done = "Evaluated: ${r.engine.label} ${if (turnCube != null || d.kind != DecisionKind.CHECKER) "cube ${r.cubePlies}-ply" else r.checker.label()}."
        reanalysis = viewModelScope.launch {
            _state.update { it.copy(progress = 0 to 1, message = null) }
            try {
                if (turnCube != null && turnCube.record == null) {
                    val ctx = CubeContext(match.length, d.score, d.crawford, match.jacoby, d.cube, d.player)
                    val e = app.engine.run { engine.cubeDecision(d.board, ctx, cubeDepth) }
                    _state.update { it.copy(progress = null, message = done, turnCube = TurnCube(null, e, e.cubeAvailable)) }
                } else {
                    val (shownEntity, shown) = _state.value.games[gameIndex]
                    val target = turnCube?.record?.let { shown.decisions.indexOf(it) } ?: index
                    val re = Reanalysis(engine, r.checker.code, cubeDepth)
                    val fresh = app.engine.run { re.decision(shown.decisions[target], match.length, match.jacoby) }.copy(pending = false)
                    // As saved now: the background analysis may have filled in other decisions of this game meanwhile.
                    val (entity, g) = app.repository.savedGame(match.id, shownEntity.number) ?: (shownEntity to shown)
                    val g2 = g.copy(decisions = g.decisions.toMutableList().also { it[target] = fresh })
                    val e2 = app.repository.saveReanalyzedGame(entity, g2, match.length)
                    _state.update { st ->
                        st.copy(
                            games = st.games.toMutableList().also { it[gameIndex] = e2 to g2 }, candidate = null, progress = null, message = done,
                            turnCube = if (turnCube != null) TurnCube(fresh, fresh.cubeEval, fresh.cubeEval?.cubeAvailable ?: false) else st.turnCube,
                        )
                    }
                }
            } catch (c: kotlinx.coroutines.CancellationException) {
                _state.update { it.copy(progress = null, message = "Stopped.") }
                throw c
            } catch (t: Throwable) {
                _state.update { it.copy(progress = null, message = "Evaluation failed: ${t.message}") }
            }
        }
    }

    fun stopReanalysis() { reanalysis?.cancel() }

    fun dismissMessage() = _state.update { it.copy(message = null) }

    /** Next decision of the human with an error or blunder, in a later game of the match after this game's last. */
    fun nextMistake() {
        val (game, pos) = _state.value.nextMistake ?: return
        _state.update { it.copy(gameIndex = game, position = pos, candidate = null, collapsed = false) }
    }

    /** The decision of the human with an error or blunder (or a marked one) before this one, in an earlier game after this game's first. */
    fun previousMistake() {
        val (game, pos) = _state.value.previousMistake ?: return
        _state.update { it.copy(gameIndex = game, position = pos, candidate = null, collapsed = false) }
    }
}

@Composable
fun ReviewScreen(vm: ReviewViewModel, onBack: () -> Unit) {
    val st by vm.state.collectAsState()
    // Left and Right step through the decisions (the arrows beside the counter).
    val ready = !st.loading && st.games.isNotEmpty()
    KeyHandler(enabled = ready) { e ->
        when (e.key) {
            Key.DirectionLeft -> if (st.position > 0 || st.gameIndex > 0) { vm.previous(); true } else false
            Key.DirectionRight -> if (st.position < st.decisions.size - 1 || st.gameIndex < st.games.size - 1) { vm.next(); true } else false
            else -> false
        }
    }
    Box(Modifier.fillMaxSize()) {
        when {
            st.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            st.games.isEmpty() -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No finished games in this match yet.")
                TextButton(onClick = onBack) { Text("Back") }
            }
            else -> ReviewContent(st, vm, onBack)
        }
    }
}

private fun title(m: MatchEntity?): String = when {
    m == null -> "Review"
    m.length == 0 -> "Money session"
    else -> "${m.length}-point match"
}

@Composable
private fun ReviewContent(st: ReviewState, vm: ReviewViewModel, onBack: () -> Unit) {
    val capture = rememberBoardCapture()
    ReviewCommands(st, vm, capture, onBack)
    BoardAndPanel(
        panelRight = st.settings.railRight,
        board = { m -> ReviewBoard(st, m.then(capture.modifier)) },
        under = { BottomBar(st, vm) },
    ) {
        PanelTitle(title(st.match))
        st.message?.let { msg ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(msg, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = vm::dismissMessage) { Text("OK") }
            }
        }
        Filters(st, vm)
        DecisionList(st, vm, Modifier.weight(1f))
        HorizontalDivider()
        // The analysis of the decision on the board, under the list, so both can be seen at once.
        Column(Modifier.weight(1f).desktopVerticalScroll().padding(top = 6.dp)) {
            st.current?.let { DecisionAnalysis(st, vm, it, Modifier.padding(end = 10.dp)) }
        }
    }
}

/** What the menus and the toolbar can do in a review. */
@Composable
private fun ReviewCommands(st: ReviewState, vm: ReviewViewModel, capture: BoardCapture, onBack: () -> Unit) {
    var ask by remember { mutableStateOf(false) }
    var summary by remember { mutableStateOf(false) }
    val n = st.decisions.size
    val game = st.games.getOrNull(st.gameIndex)?.first?.number
    RegisterCommand(Cmd.ANALYZE_AGAIN, enabled = st.progress == null) { ask = true }
    RegisterCommand(Cmd.STOP_ANALYSIS, enabled = st.progress != null) { vm.stopReanalysis() }
    RegisterCommand(Cmd.PREVIOUS, enabled = st.position > 0 || st.gameIndex > 0) { vm.previous() }
    RegisterCommand(Cmd.NEXT, enabled = st.position < n - 1 || st.gameIndex < st.games.size - 1) { vm.next() }
    RegisterCommand(Cmd.SUMMARY) { summary = true }
    fun saveMat(whole: Boolean) {
        val (name, text) = vm.exportMat(whole) ?: return
        com.boldblot.backgammonplyground.FileDialogs.save("Save .mat file", name)?.let { MatFiles.save(it, text) }
    }
    RegisterCommand(Cmd.SAVE_MATCH) { saveMat(true) }
    // A game after the Crawford game goes out with the whole match, so that XG and gnubg know that.
    RegisterCommand(Cmd.SAVE_GAME, enabled = !st.isPostCrawford, label = "Save game $game as .mat…") { saveMat(false) }
    val d = st.current
    val m = st.match
    if (d != null && m != null) {
        ShareCommands(d, m.length, m.jacoby, PositionShare.title(d, m.length, com.boldblot.backgammonplyground.ui.components.LocalScoreAway.current), capture)
    }
    st.progress?.let { (done, total) ->
        RegisterStatus("Analyzing… $done / $total decisions", if (total == 0) 0f else done / total.toFloat(), vm::stopReanalysis)
    }
    if (ask) {
        ReanalyzeDialog(
            st.settings, gameNumber = game,
            onDismiss = { ask = false },
            cpuDefault = st.settings.cpuAnalysis || st.games.any { (_, g) -> Reanalysis.unanalyzedCpu(g) },
            onStart = { ask = false; vm.reanalyze(it) },
        )
    }
    if (m != null && summary) SavedSummaryDialog(
        vm.app, m.id, gameNumber = game, startWithMatch = false,
        onDismiss = { summary = false },
        onDeleted = { summary = false; onBack() },
    )
}

/** The game picker (a menu, so a long match takes no extra row) and the list's filters. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Filters(st: ReviewState, vm: ReviewViewModel) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(ChipGapH), verticalArrangement = Arrangement.spacedBy(ChipGapV)) {
        if (st.games.size > 1) GamePicker(st, vm)
        if (st.hasMarks) FilterChip(
            selected = st.marked, onClick = vm::toggleMarked, label = { Text("Marked") },
            leadingIcon = { Icon(com.boldblot.backgammonplyground.ui.components.BlotIcon, null, Modifier.size(18.dp)) },
        )
        ReviewFilter.entries.forEach { f ->
            FilterChip(selected = st.filter == f, onClick = { vm.setFilter(f) }, label = { Text(f.label) })
        }
    }
}

@Composable
private fun GamePicker(st: ReviewState, vm: ReviewViewModel) {
    var open by remember { mutableStateOf(false) }
    fun label(e: GameEntity) = "Game ${e.number} (${e.scoreBeforeHuman}-${e.scoreBeforeCpu})"
    Box {
        FilterChip(
            selected = true, onClick = { open = true },
            label = { Text(st.games.getOrNull(st.gameIndex)?.first?.let(::label) ?: "Game") },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Choose game") },
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            st.games.forEachIndexed { i, (e, _) ->
                DropdownMenuItem(
                    text = { Text(label(e), fontWeight = if (i == st.gameIndex) FontWeight.SemiBold else FontWeight.Normal) },
                    onClick = { open = false; vm.selectGame(i) },
                )
            }
        }
    }
}

/** Arrows for the current decision: the chosen candidate, or played (severity colour) + best (green). */
private fun arrowsOf(st: ReviewState): List<MoveArrow> = st.current?.let { decisionArrows(it, st.candidate, st.factor) } ?: emptyList()

/** Arrows for [d]: candidate [selected], or (null) the played move in its severity colour plus the best in green. */
internal fun decisionArrows(d: DecisionRecord, selected: Int?, factor: Float): List<MoveArrow> {
    val a = d.checker ?: return d.move?.let { arrowsFor(d.player, it, ArrowColors.neutral) } ?: emptyList()
    if (a.candidates.isEmpty()) return emptyList()
    if (selected != null) {
        val c = a.candidates.getOrNull(selected) ?: return emptyList()
        val sev = XgRules.severity((a.bestEquity - c.equity) * factor)
        return arrowsFor(d.player, c.move, ArrowColors.forSeverity(sev))
    }
    // Tried again: the first try is the play shown (what PR counts).
    val best = a.candidates.first()
    val index = d.reviewIndex
    val played = d.reviewMove ?: index?.let { a.candidates.getOrNull(it)?.move }
    val bestArrows = arrowsFor(d.player, best.move, ArrowColors.best)
    if (played == null || index == 0) return bestArrows
    return bestArrows + arrowsFor(d.player, played, ArrowColors.forSeverity(XgRules.severity((d.scoredError ?: 0f) * factor)))
}

@Composable
private fun ReviewBoard(st: ReviewState, modifier: Modifier) {
    val d = st.current
    val (board, after) = d?.let { reviewBoard(it, st.view, st.candidate) } ?: ((st.game?.board ?: Board.INITIAL) to false)
    val dice = d?.dice?.takeIf { !after }?.let { DiceView(d.player, it.highFirst) }
    BoardView(
        board = board, modifier = modifier,
        colors = boardColorsFor(st.settings),
        mirrored = st.settings.mirrorBoard,
        dice = listOfNotNull(dice),
        cube = d?.let { decisionCube(it).first } ?: CubeState(),
        cubeOffered = d?.let { decisionCube(it).second } ?: false,
        crawford = d?.crawford ?: st.game?.crawford ?: false,
        crawfordSign = st.settings.crawfordSign,
        dmp = d != null && com.boldblot.backgammonplyground.core.Scoring.isDmp(st.match?.length ?: 0, d.score, d.crawford),
        pips = listOf(board.pipCount(Player.HUMAN), board.pipCount(Player.CPU)),
        arrows = if (st.view == ReviewView.ARROWS) arrowsOf(st) else emptyList(),
    )
}

/** Save to My positions, for the decision shown. */
@Composable
private fun SaveCommand(st: ReviewState, vm: ReviewViewModel) {
    val d = st.current ?: return
    var saving by remember { mutableStateOf(false) }
    val study = remember(d, st.match) { st.match?.let { m -> StudyPosition.of(d, m.length, m.jacoby) } }
    if (saving && study != null) {
        val labels = com.boldblot.backgammonplyground.core.Labels.of(d, st.match?.length ?: 0, opponent = d.player != Player.HUMAN).mapTo(HashSet()) { it.name }
        // A marked position keeps the mark as a label.
        if (st.currentMarked) labels += com.boldblot.backgammonplyground.core.AutoLabel.MARKED.name
        SaveToPositionsDialog(vm.app, study, "Game review", labels, com.boldblot.backgammonplyground.core.MyAnswer.played(d)) { saving = false }
    }
    RegisterCommand(Cmd.SAVE_POSITION, enabled = study != null) { saving = true }
}

/**
 * Under the board: the played and best moves, the decision stepper and the view chips. The played/best bar keeps its room
 * on cube decisions, so the board keeps its size.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BottomBar(st: ReviewState, vm: ReviewViewModel) {
    SaveCommand(st, vm)
    val d = st.current
    val a = d?.checker
    if (d != null && a != null && a.candidates.isNotEmpty()) {
        // Tried again: the first try (what PR counts) is the play shown.
        val played = d.reviewMove
        val best = a.candidates.first()
        PlayedBestBar(
            playedText = played?.let { MoveNotation.format(d.board, d.player, it) } ?: "?",
            playedError = d.scoredError, playedSeverity = st.severityOf(d),
            bestText = MoveNotation.format(d.board, d.player, best.move), bestEquity = best.equity,
            modifier = Modifier.padding(top = 4.dp),
            onPlayed = { d.reviewIndex?.let { vm.selectCandidate(it) } },
            onBest = { vm.selectCandidate(0) },
        )
    } else if (d != null) {
        // Cube decisions and decisions not analyzed keep the bar too, so the arrows below don't jump.
        PlayedBestBar(
            playedText = describeAction(d.reviewKind, d.board, d.player, d.reviewMove),
            playedError = d.scoredError, playedSeverity = st.severityOf(d),
            bestText = d.cubeEval?.let { bestCubeLabel(it, d.kind, d.beavers) } ?: "[n/a]", bestEquity = null,
            modifier = Modifier.padding(top = 4.dp),
        )
    } else {
        KeepRoom(false) { EmptyPlayedBestBar() }
    }
    val n = st.decisions.size
    FlowRow(
        Modifier.fillMaxWidth(), itemVerticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ChipGapH, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(ChipGapV),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Previous and Next: the mistakes (and marked positions); the arrows step through every decision.
            TextButton(onClick = vm::previousMistake, enabled = st.previousMistake != null) { Text("Previous") }
            // At either end of a game the arrows go on into the previous or next game.
            IconButton(onClick = vm::previous, enabled = st.position > 0 || st.gameIndex > 0) { Icon(Icons.AutoMirrored.Filled.NavigateBefore, "Previous") }
            Text(if (n == 0) "0 / 0" else "${st.position + 1} / $n", style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = vm::next, enabled = st.position < n - 1 || st.gameIndex < st.games.size - 1) { Icon(Icons.AutoMirrored.Filled.NavigateNext, "Next") }
            TextButton(onClick = vm::nextMistake, enabled = st.nextMistake != null) { Text("Next") }
        }
        if (d != null) ReviewViewChips(st.view, d.move != null, vm::setView, null)
    }
}

/**
 * All decisions of the game in one scrolling list (Galaxy style). The selected decision opens in place
 * with its analysis, so list and analysis share the whole height (no tabs, no second small window).
 */
@Composable
private fun DecisionList(st: ReviewState, vm: ReviewViewModel, modifier: Modifier) {
    val colors = boardColorsFor(st.settings)
    val listState = rememberLazyListState()
    // keep one decision of context above the selected one
    LaunchedEffect(st.position, st.gameIndex, st.filter) {
        if (st.decisions.isNotEmpty()) listState.animateScrollToItem((st.position - 1).coerceAtLeast(0))
    }
    LazyColumn(
        modifier.fillMaxWidth().padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp)),
        state = listState,
    ) {
        if (st.decisions.isEmpty()) {
            item {
                Text(
                    when {
                        st.onlyMarked -> "No marked positions in this game."
                        st.filter == ReviewFilter.MISTAKES -> "No errors or blunders in this game. Well played!"
                        else -> "No decisions."
                    },
                    Modifier.padding(10.dp),
                )
            }
        }
        itemsIndexed(st.decisions) { pos, (_, d) ->
            val sev = st.severityOf(d)
            val selected = pos == st.position
            Column(Modifier.background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color.Transparent)) {
                Row(
                    Modifier.fillMaxWidth()
                        .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                        .clickable { vm.tapDecision(pos) }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CheckerIcon(if (d.player == Player.HUMAN) colors.human else colors.cpu, rim = if (d.player == Player.HUMAN) colors.humanRim else colors.cpuRim)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        d.dice?.toString() ?: "",
                        Modifier.width(26.dp), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        describeAction(d.reviewKind, d.board, d.player, d.reviewMove),
                        Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                    if (st.hasMarks && st.decisions[pos].index in st.game?.marked.orEmpty()) {
                        Icon(com.boldblot.backgammonplyground.ui.components.BlotIcon, "Marked", Modifier.padding(horizontal = 4.dp).size(16.dp))
                    }
                    val scored = d.scoredError
                    // In My positions (filled folder), or going there at the end of the match with Top X (outlined).
                    val inLibrary = d.player == Player.HUMAN && st.match != null && sev.atLeast(Severity.ERROR) &&
                        com.boldblot.backgammonplyground.core.StudyPosition.of(d, st.match.length, st.match.jacoby)?.let { (it.xgid to it.doubled) in st.library } == true
                    val going = !inLibrary && (st.gameIndex to st.decisions[pos].index) in st.goingIn
                    if (inLibrary || going) {
                        Icon(
                            if (inLibrary) Icons.Default.Folder else Icons.Outlined.Folder,
                            if (inLibrary) "In My positions" else "Goes to My positions at the end of the match",
                            Modifier.size(16.dp).padding(end = 2.dp), tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    if (scored != null && sev != Severity.NONE && !selected) ErrorPill(errText(-scored), sev, style = MaterialTheme.typography.bodySmall, mono = true)
                    if (scored != null && selected) SeverityBadge(sev, scored * st.factor, factor = st.factor)
                }
            }
        }
        item { GameSummary(st, Modifier.padding(6.dp)) }
    }
}

/** The selected decision's analysis, shown in the side panel under [DecisionList]. */
@Composable
private fun DecisionAnalysis(st: ReviewState, vm: ReviewViewModel, d: DecisionRecord, modifier: Modifier) {
    val colors = boardColorsFor(st.settings)
    Column(modifier) {
        val notes = buildList {
            if (d.hinted) add("hint used, not counted")
            if (d.auto) add("played by autofinish, not counted")
            val quick = d.player == Player.CPU && (d.checker?.plies ?: d.cubeEval?.plies) == com.boldblot.backgammonplyground.core.MatchController.LIGHT_PLIES
            if (quick && !d.counted) add("quick 1-ply analysis, not in the computer's PR (its analysis is off)")
            else if (d.error != null && !d.counted && !d.hinted) add("obvious decision, not counted in PR")
            if (d.kind == DecisionKind.CHECKER && d.checker?.legalMoves == 1) add("forced move")
            d.firstTry?.let { t ->
                val last = describeAction(d.kind, d.board, d.player, d.move)
                val tries = if (t.retries == 1) "tried again" else "tried ${t.retries} more times"
                add("first try shown and counted in PR; $tries, played at last: $last (final)")
            }
        }
        if (notes.isNotEmpty()) {
            Text(notes.joinToString(" · "), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(4.dp))
        // A checker play's Cube chip shows the cube decision of the same turn (no doubles are often left out of the list).
        FlowRow(horizontalArrangement = Arrangement.spacedBy(ChipGapH), verticalArrangement = Arrangement.spacedBy(ChipGapV)) {
            if (d.kind == DecisionKind.CHECKER) {
                FilterChip(selected = !st.cubeShown, onClick = { if (st.cubeShown) vm.toggleCube() }, label = { Text("Checker") })
                FilterChip(selected = st.cubeShown, onClick = { if (!st.cubeShown) vm.toggleCube() }, label = { Text("Cube") })
            }
            var evaluating by remember { mutableStateOf(false) }
            AssistChip(
                onClick = { evaluating = true }, enabled = !d.auto && st.progress == null, label = { Text("Evaluate") },
                leadingIcon = { Icon(Icons.Default.Calculate, null, Modifier.size(18.dp)) },
            )
            if (evaluating) {
                val cube = st.cubeShown || d.kind != DecisionKind.CHECKER
                ReanalyzeDialog(
                    st.settings, gameNumber = null, onDismiss = { evaluating = false },
                    title = if (cube) "Evaluate this cube decision" else "Evaluate this move", confirm = "Evaluate",
                    note = if (st.cubeShown && st.turnCube?.record == null) "This cube decision wasn't recorded, so the result is only shown."
                    else "The new evaluation replaces the old one, in the review and in your statistics.",
                    onStart = { evaluating = false; vm.evaluate(it) },
                )
            }
        }
        val tc = st.turnCube
        if (st.cubeShown && tc != null) {
            val played = tc.record?.let { describeAction(it.kind, it.board, it.player, null) }
            when {
                tc.busy -> Text("Analyzing the cube decision…", style = MaterialTheme.typography.bodyMedium)
                tc.eval == null -> Text("The cube decision could not be analyzed.")
                !tc.eval.cubeAvailable -> Text("No cube decision: the cube wasn't available to ${if (d.player == Player.HUMAN) "you" else "the computer"}.")
                else -> {
                    val sev = tc.record?.scoredError?.let { st.severityOf(tc.record) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Before this roll: " + (played ?: "rolled (not recorded)"),
                            style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f),
                        )
                        val e = tc.record?.scoredError
                        if (e != null && sev != null && sev != Severity.NONE) ErrorPill(errText(-e), sev, style = MaterialTheme.typography.bodySmall, mono = true)
                    }
                    CubeTable(tc.eval, tc.record?.kind, st.factor, d.beavers || tc.record?.beavers == true, colors = colors, rollerHuman = d.player == Player.HUMAN)
                }
            }
            return@Column
        }
        when {
            d.checker != null && d.checker.candidates.isNotEmpty() -> CandidateList(
                board = d.board, player = d.player, analysis = d.checker, factor = st.factor,
                highlightIndex = d.reviewIndex, maxRows = 8, selectedIndex = st.candidate, detailed = true,
                triedIndices = d.triedIndices, finalIndex = d.checker.playedIndex.takeIf { d.firstTry != null },
                moverColor = if (d.player == Player.HUMAN) colors.human else colors.cpu,
                moverRim = if (d.player == Player.HUMAN) colors.humanRim else colors.cpuRim,
                opponentRim = if (d.player == Player.HUMAN) colors.cpuRim else colors.humanRim,
                opponentColor = if (d.player == Player.HUMAN) colors.cpu else colors.human,
                onSelect = { i, _ -> vm.selectCandidate(if (st.candidate == i) null else i) },
                playedLabel = if (d.player == Player.HUMAN) "you" else "computer",
            )
            d.cubeEval != null -> CubeTable(d.cubeEval, d.kind, st.factor, d.beavers, colors = colors, rollerHuman = (d.player == Player.HUMAN) != d.kind.isResponse)
            d.kind == DecisionKind.CHECKER && d.checker != null -> Text("No legal move.")
            d.auto -> Text("Not analyzed (played by autofinish).")
            d.pending -> Text("Not analyzed yet: waiting for the background analysis. Analyze again does it now.")
            d.player == Player.CPU -> Text("Not analyzed. Analyze again, with \"The computer's decisions too\" on, analyzes it.")
            else -> Text("Not analyzed. Analyze again analyzes it.")
        }
    }
}

@Composable
private fun GameSummary(st: ReviewState, modifier: Modifier = Modifier) {
    val g = st.game ?: return
    if (g.result == null) return
    val s = Summaries.of(g, st.match?.length ?: 0)
    Column(
        modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(10.dp),
    ) {
        Text("Game summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        val r = g.result
        val how = when {
            r.byPass -> "double passed"
            r.resigned -> "${r.type.name.lowercase().replace("single", "single game")} resigned"
            else -> r.type.name.lowercase().replace("single", "single game")
        }
        Labelled("Result", "${if (r.winner == Player.HUMAN) "You" else "Computer"} +${r.points} ($how)")
        PerfRows("You", s.performance[0])
        PerfRows("Computer", s.performance[1])
    }
}

@Composable
private fun PerfRows(who: String, p: PerformanceTotals) {
    Labelled(
        "$who PR (checker / cube)",
        "${p.pr?.let { fmt(it) } ?: "–"}  (${p.checkerPr?.let { fmt(it) } ?: "–"} / ${p.cubePr?.let { fmt(it) } ?: "–"})",
    )
    Labelled(
        "$who errors / blunders",
        "${p.checkerErrors + p.doubleErrors + p.takeErrors} / ${p.checkerBlunders + p.doubleBlunders + p.takeBlunders}",
    )
}
