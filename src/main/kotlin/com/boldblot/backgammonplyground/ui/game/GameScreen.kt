package com.boldblot.backgammonplyground.ui.game

import com.boldblot.backgammonplyground.ui.components.ChipGapH
import com.boldblot.backgammonplyground.ui.components.ChipGapV
import com.boldblot.backgammonplyground.ui.review.ShareCommands
import com.boldblot.backgammonplyground.ui.shell.BoardAndPanel
import com.boldblot.backgammonplyground.ui.shell.Cmd
import com.boldblot.backgammonplyground.ui.shell.RegisterCommand
import com.boldblot.backgammonplyground.ui.components.desktopVerticalScroll
import com.boldblot.backgammonplyground.showMessage
import androidx.compose.material.icons.filled.Share
import com.boldblot.backgammonplyground.core.PositionIds
import com.boldblot.backgammonplyground.core.StudyPosition
import com.boldblot.backgammonplyground.ui.review.PositionShare
import com.boldblot.backgammonplyground.ui.review.rememberBoardCapture
import com.boldblot.backgammonplyground.ui.review.BoardCapture
import androidx.compose.foundation.Canvas
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import com.boldblot.backgammonplyground.KeyHandler
import androidx.compose.foundation.border
import androidx.compose.ui.geometry.Offset
import androidx.compose.material3.FilledIconButton
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.boldblot.backgammonplyground.core.Board
import com.boldblot.backgammonplyground.core.CubeState
import com.boldblot.backgammonplyground.core.DecisionKind
import com.boldblot.backgammonplyground.core.Feedback
import com.boldblot.backgammonplyground.core.GameState
import com.boldblot.backgammonplyground.core.GameSummary
import com.boldblot.backgammonplyground.core.MatchState
import com.boldblot.backgammonplyground.core.MoveNotation
import com.boldblot.backgammonplyground.core.PerformanceTotals
import com.boldblot.backgammonplyground.core.Phase
import com.boldblot.backgammonplyground.core.Scoring
import com.boldblot.backgammonplyground.core.Player
import com.boldblot.backgammonplyground.core.Severity
import com.boldblot.backgammonplyground.core.WinType
import com.boldblot.backgammonplyground.core.XgRules
import com.boldblot.backgammonplyground.data.GameEntity
import com.boldblot.backgammonplyground.data.MeterMode
import com.boldblot.backgammonplyground.data.TrainingMode
import com.boldblot.backgammonplyground.ui.board.ArrowColors
import com.boldblot.backgammonplyground.ui.board.BoardGeometry
import com.boldblot.backgammonplyground.ui.board.BoardView
import com.boldblot.backgammonplyground.ui.board.DiceView
import com.boldblot.backgammonplyground.ui.board.arrowsFor
import com.boldblot.backgammonplyground.ui.board.boardColorsFor
import com.boldblot.backgammonplyground.ui.components.CandidateList
import com.boldblot.backgammonplyground.ui.components.CubeTable
import com.boldblot.backgammonplyground.ui.components.PlayedBestBar
import com.boldblot.backgammonplyground.ui.components.SeverityBadge
import com.boldblot.backgammonplyground.ui.components.describeAction
import com.boldblot.backgammonplyground.ui.components.eq
import com.boldblot.backgammonplyground.ui.review.DeleteMatchDialog
import com.boldblot.backgammonplyground.ui.components.errText
import com.boldblot.backgammonplyground.ui.components.fmt
import com.boldblot.backgammonplyground.ui.components.pct
import com.boldblot.backgammonplyground.ui.components.scrollIndicator
import com.boldblot.backgammonplyground.ui.review.GameStepper
import com.boldblot.backgammonplyground.ui.review.SavedGameRows
import com.boldblot.backgammonplyground.ui.review.SavedSummaryDialog
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    vm: GameViewModel,
    onBack: () -> Unit,
    onReview: (matchId: Long) -> Unit,
    /** Opens the review on [gameNumber]'s errors and blunders. */
    onReviewMistakes: (matchId: Long, gameNumber: Int) -> Unit = { id, _ -> onReview(id) },
    onReviewMarked: (matchId: Long, gameNumber: Int) -> Unit = { id, _ -> onReview(id) },
) {
    val st by vm.state.collectAsState()
    val m = st.match
    var confirmAbandon by remember { mutableStateOf(false) }
    var chooseResign by remember { mutableStateOf(false) }
    // A money session just ended with its mistakes held back ("after the match" training): its summary.
    var endedSession by remember { mutableStateOf<Long?>(null) }
    // Bumped to open the end-of-game summary again.
    var summaryKey by remember { mutableIntStateOf(0) }
    fun endSession(id: Long, finishedGames: Boolean, end: () -> Unit) {
        end()
        if (finishedGames && st.settings.trainingMode == TrainingMode.AFTER_GAME) endedSession = id else onBack()
    }

    Box(Modifier.fillMaxSize()) {
        when {
            st.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            m == null -> NoMatch(onBack)
            else -> GameContent(st, m, vm, onAbandon = { confirmAbandon = true }, onResign = { chooseResign = true }, onReview, onReviewMistakes, onReviewMarked)
        }
        if (st.autoFinishing) AutoFinishOverlay(vm::stopAutoFinishAndPlay)
        st.diceRequest?.let { r -> ManualDicePicker(r, vm::answerDice, Modifier.align(Alignment.Center)) }
        // Under the end-of-game summary: if its dialog window is ever lost, this brings it back.
        if (st.gameOver != null) {
            Button(onClick = { summaryKey++ }, Modifier.align(Alignment.Center)) { Text("Show game summary") }
        }
    }

    if (chooseResign && m != null) {
        ResignDialog(m, onDismiss = { chooseResign = false }, onResign = { chooseResign = false; vm.resign(it) })
    }

    val offer = m?.game?.resignOffer
    if (m != null && offer != null && m.game.turn == Player.CPU && !st.busy && !st.autoFinishing) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Computer resigns") },
            text = { Text("The computer offers to resign ${GameViewModel.resignText(offer, m)}.") },
            confirmButton = { TextButton(onClick = { vm.answerResign(true) }) { Text("Accept") } },
            dismissButton = { TextButton(onClick = { vm.answerResign(false) }) { Text("Reject") } },
        )
    }

    if (confirmAbandon && m != null) {
        AlertDialog(
            onDismissRequest = { confirmAbandon = false },
            title = { Text(if (m.rules.isMoney) "End session?" else "Abandon match?") },
            text = {
                Text(
                    if (m.rules.isMoney) "Finished games are kept in your statistics."
                    else "Finished games stay in your statistics. The match will not count as won or lost and will not change your rating.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmAbandon = false
                    if (m.rules.isMoney) endSession(m.id, m.gameNumber > 1, vm::abandonMatch) else { vm.abandonMatch(); onBack() }
                }) { Text("Yes") }
            },
            dismissButton = { TextButton(onClick = { confirmAbandon = false }) { Text("Cancel") } },
        )
    }

    st.gameOver?.let { info ->
        key(summaryKey) { GameOverDialog(info, st.inStats, st.settings.gameSummary, st.analysisLeft, st.analysisEta, vm, onReview, onReviewMistakes, onReviewMarked, onBack) {
            endSession(info.matchId, true, vm::endSession)
        } }
    }
    endedSession?.let { id ->
        SavedSummaryDialog(
            vm.app, id, gameNumber = null, startWithMatch = true, onDismiss = { endedSession = null; onBack() },
            onReviewMistakes = { game -> endedSession = null; onReviewMistakes(id, game) },
            onDeleted = { endedSession = null; onBack() },
        )
    }
}

/**
 * Manual dice: two columns of dice 1-6 (one die each for the opening roll, which cannot be a tie)
 * and ✓ to roll them. The player's own roll can be cancelled (to double instead).
 */
@Composable
private fun ManualDicePicker(r: DiceRequest, onDone: (Pair<Int, Int>?) -> Unit, modifier: Modifier) {
    var first by remember(r) { mutableStateOf<Int?>(null) }
    var second by remember(r) { mutableStateOf<Int?>(null) }
    val ok = first != null && second != null && !(r.opening && first == second)
    Surface(modifier, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, shadowElevation = 8.dp) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                when {
                    r.opening -> "Opening roll"
                    r.player == Player.HUMAN -> "Your roll"
                    else -> "Computer's roll"
                },
                style = MaterialTheme.typography.titleSmall,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                listOf(first to { v: Int -> first = v }, second to { v: Int -> second = v }).forEachIndexed { i, (picked, pick) ->
                    Column(Modifier.padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (r.opening) Text(if (i == 0) "You" else "Computer", style = MaterialTheme.typography.labelSmall)
                        (1..6).forEach { v -> PickDie(v, picked == v) { pick(v) } }
                    }
                }
                Column(Modifier.padding(start = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledIconButton(onClick = { onDone(first!! to second!!) }, enabled = ok, modifier = Modifier.size(52.dp)) {
                        Icon(Icons.Default.Check, "Roll these dice")
                    }
                    if (!r.opening && r.player == Player.HUMAN) {
                        IconButton(onClick = { onDone(null) }) { Icon(Icons.Default.Close, "Cancel") }
                    }
                }
            }
            if (r.opening && first != null && first == second) {
                Text("A tie is rolled again", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/** One die face [value] to tap in [ManualDicePicker]. */
@Composable
private fun PickDie(value: Int, selected: Boolean, onClick: () -> Unit) {
    val face = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val pip = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val pips = when (value) {
        1 -> listOf(1 to 1)
        2 -> listOf(0 to 0, 2 to 2)
        3 -> listOf(0 to 0, 1 to 1, 2 to 2)
        4 -> listOf(0 to 0, 2 to 0, 0 to 2, 2 to 2)
        5 -> listOf(0 to 0, 2 to 0, 1 to 1, 0 to 2, 2 to 2)
        else -> listOf(0 to 0, 0 to 1, 0 to 2, 2 to 0, 2 to 1, 2 to 2)
    }
    Canvas(
        Modifier.padding(2.dp).size(38.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(7.dp))
            .background(face, RoundedCornerShape(7.dp))
            .clickable(onClickLabel = "$value", onClick = onClick),
    ) {
        val step = size.width / 4f
        pips.forEach { (x, y) -> drawCircle(pip, radius = size.width / 11f, center = Offset(step * (x + 1), step * (y + 1))) }
    }
}

/** Lets the human choose how much to resign: a single game, a gammon or a backgammon. */
@Composable
private fun ResignDialog(m: MatchState, onDismiss: () -> Unit, onResign: (WinType) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Resign") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "The computer accepts unless it has at least a 1% chance of winning more.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                WinType.entries.forEach { type ->
                    OutlinedButton(onClick = { onResign(type) }, modifier = Modifier.fillMaxWidth()) {
                        Text(GameViewModel.resignText(type, m).replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun title(m: MatchState): String =
    if (m.rules.isMoney) "Money game" else "${m.rules.length}-point match"

private fun subtitle(m: MatchState, inStats: Boolean): String =
    "vs ${m.opponentLabel} · game ${m.gameNumber}" + (if (m.game.crawford) " · Crawford" else "") +
        if (inStats) "" else " · not in statistics"

@Composable
private fun NoMatch(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No match in progress.", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onBack) { Text("Back to menu") }
    }
}

/**
 * Share during a game: the XGID, the board, and the board with the top 5 (or the cube actions) of
 * an open hint. Without one, the top 5 says to open Hint (no analysis is shown during play otherwise).
 */
/** Edit → Copy XGID and the File menu's board pictures, for the position on the board. */
@Composable
private fun GameShareCommands(st: GameUiState, m: MatchState, capture: BoardCapture) {
    val p = StudyPosition.current(m)
    val g = m.game
    val factor = XgRules.errorFactor(m.rules.length)
    val lines = when {
        st.hint != null -> PositionShare.analysisLines(
            g.board, Player.HUMAN, st.hint, null, response = false, beavers = false, playedIndex = null, playedKind = null, factor = factor,
        )
        st.cubeHint != null -> PositionShare.analysisLines(
            g.board, Player.HUMAN, null, st.cubeHint, response = g.phase == Phase.DOUBLED, beavers = m.rules.beaversAllowed,
            playedIndex = null, playedKind = null, factor = factor,
        )
        else -> emptyList()
    }
    if (p != null) ShareCommands(
        PositionIds.toXgid(p.setup.copy(doubled = p.doubled)), PositionShare.title(p, com.boldblot.backgammonplyground.ui.components.LocalScoreAway.current), lines, capture,
        analysisLabel = (st.hint?.label ?: st.cubeHint?.label)?.ifBlank { null },
        noAnalysis = { showMessage("Not available during a game. Open Hint, then save the board with the top 5.") },
    )
}

/** While autofinish plays: a click anywhere stops it, and the status bar says so. */
@Composable
private fun AutoFinishOverlay(onStop: () -> Unit) {
    com.boldblot.backgammonplyground.ui.shell.RegisterStatus("Autofinish: click the board to stop", onStop = onStop)
    Box(
        Modifier.fillMaxSize().clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null, onClick = onStop,
        ),
    )
}

/** True when an analysis panel (practice prompt or hint) should be visible. */
private fun hasPanel(st: GameUiState) = st.feedback != null || st.hint != null || st.cubeHint != null

/** True when the practice prompt holds play; a hint does not (the player moves and finishes the turn with it open). */
private fun blocksPlay(st: GameUiState) = st.feedback != null

@Composable
private fun GameContent(
    st: GameUiState, m: MatchState, vm: GameViewModel, onAbandon: () -> Unit, onResign: () -> Unit,
    onReview: (Long) -> Unit, onReviewMistakes: (Long, Int) -> Unit, onReviewMarked: (Long, Int) -> Unit,
) {
    val capture = rememberBoardCapture()
    // Double asked for (Preferences → Play: confirm), from the board button or the Double chip.
    val askState = remember(m.game.phase, m.game.decisions.size) { mutableStateOf<CubeAsk?>(null) }
    // Esc cancels a pending double.
    com.boldblot.backgammonplyground.BackHandler(enabled = askState.value != null) { askState.value = null }
    var summary by remember { mutableStateOf(false) }
    if (summary) SavedSummaryDialog(vm.app, m.id, gameNumber = null, startWithMatch = false, onDismiss = { summary = false })
    val g = m.game
    val humanTurn = g.turn == Player.HUMAN
    val humanActs = st.viewing == null && !st.busy && st.feedback == null && !st.autoFinishing && (humanTurn || g.phase == Phase.DOUBLED)
    val moving = humanActs && g.phase == Phase.MOVING && humanTurn && !st.noLegalMove
    val preRoll = humanActs && g.phase == Phase.PRE_ROLL && humanTurn
    fun double() { if (st.settings.confirmDouble) askState.value = CubeAsk.DOUBLE else vm.double() }
    // Keys: Enter rolls or finishes the move, D doubles, U undoes a checker, the arrows look back at earlier positions.
    KeyHandler(enabled = st.gameOver == null && askState.value == null && !blocksPlay(st)) { e ->
        when (e.key) {
            Key.Enter, Key.NumPadEnter -> when {
                preRoll -> { vm.roll(); true }
                moving && st.moveComplete -> { vm.confirmMove(); true }
                else -> false
            }
            Key.D -> if (preRoll && g.mayDouble(m.rules, m.score)) { double(); true } else false
            Key.U -> if (moving && st.steps.isNotEmpty()) { vm.undo(); true } else false
            Key.DirectionLeft -> if (st.settings.pastPositions && st.canView) { vm.viewBack(); true } else false
            Key.DirectionRight -> if (st.viewing != null) { vm.viewForward(); true } else false
            else -> false
        }
    }
    GameCommands(st, m, vm, preRoll, moving, ::double, onAbandon, onResign, onSummary = { summary = true }, onReview, onReviewMistakes, onReviewMarked)
    GameShareCommands(st, m, capture)
    BoardAndPanel(
        panelRight = st.settings.railRight,
        board = { mod -> BoardArea(st, m, vm, mod, capture, askState, onBoardMoveButtons = st.settings.onBoardMoveButtons) },
    ) {
        // The computer at the top and you at the bottom, each on its side of the board.
        PlayerCard(st, m, Player.CPU)
        Spacer(Modifier.height(6.dp))
        Text(title(m), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Text(subtitle(m, st.inStats), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.weight(1f).desktopVerticalScroll().padding(end = 10.dp)) {
            // A hint leaves play going: the status, Undo / Done and the chips stay above it.
            if (!blocksPlay(st)) {
                StatusText(st)
                Meter(st, vm, twoLines = true)
                Actions(st, m, vm, vertical = true, onResign = onResign, askState = askState)
            }
            if (hasPanel(st)) Panels(st, m, vm)
        }
        Spacer(Modifier.height(6.dp))
        PlayerCard(st, m, Player.HUMAN)
    }
}

/** What the Game and Analyze menus and the toolbar can do in a game. */
@Composable
private fun GameCommands(
    st: GameUiState, m: MatchState, vm: GameViewModel, preRoll: Boolean, moving: Boolean, double: () -> Unit, onAbandon: () -> Unit, onResign: () -> Unit,
    onSummary: () -> Unit, onReview: (Long) -> Unit, onReviewMistakes: (Long, Int) -> Unit, onReviewMarked: (Long, Int) -> Unit,
) {
    val g = m.game
    val humanTurn = g.turn == Player.HUMAN
    val viewing = st.viewing
    val humanActs = viewing == null && !st.busy && st.feedback == null && !st.autoFinishing && (humanTurn || g.phase == Phase.DOUBLED)
    val canHint = humanActs && (
        (g.phase == Phase.MOVING && humanTurn && !st.noLegalMove) ||
            (g.phase == Phase.PRE_ROLL && humanTurn && g.mayDouble(m.rules, m.score)) ||
            (g.phase == Phase.DOUBLED && g.responder == Player.HUMAN) ||
            (g.phase == Phase.BEAVERED && humanTurn)
        )
    val myDecision = viewing == null && st.gameOver == null && !st.busy && when (g.phase) {
        Phase.PRE_ROLL, Phase.MOVING, Phase.BEAVERED -> g.turn == Player.HUMAN
        Phase.DOUBLED -> g.responder == Player.HUMAN
        else -> false
    }
    val inPlay = st.gameOver == null && g.phase != Phase.OVER && g.phase != Phase.OPENING
    val queued = st.queuedResign != null
    val over = st.gameOver
    RegisterCommand(Cmd.ROLL_DONE, enabled = preRoll || (moving && st.moveComplete), label = if (moving) "Done" else "Roll") {
        if (preRoll) vm.roll() else vm.confirmMove()
    }
    RegisterCommand(Cmd.DOUBLE, enabled = preRoll && g.mayDouble(m.rules, m.score)) { double() }
    RegisterCommand(Cmd.UNDO, enabled = moving && st.steps.isNotEmpty()) { vm.undo() }
    RegisterCommand(Cmd.HINT, enabled = canHint) { vm.hint() }
    RegisterCommand(Cmd.MARK, enabled = viewing != null || myDecision, checked = if (viewing != null) viewing in g.marked else st.marking) { vm.toggleMark() }
    RegisterCommand(Cmd.MAX_BEAROFF, enabled = inPlay || st.maxBearOff, checked = st.maxBearOff) { vm.toggleMaxBearOff() }
    RegisterCommand(
        Cmd.KEEP_ROLLING, enabled = st.keepRolling || (inPlay && Scoring.mayDouble(m.rules, g.cube, Player.HUMAN, m.score, g.crawford)), checked = st.keepRolling,
    ) { vm.toggleKeepRolling() }
    if (viewing != null) RegisterCommand(Cmd.BACK_TO_GAME) { vm.backToGame() }
    RegisterCommand(Cmd.AUTOFINISH, enabled = st.canAutoFinish) { vm.startAutoFinish() }
    RegisterCommand(
        Cmd.RESIGN, enabled = queued || st.canResign || st.canQueueResign,
        label = if (queued) "Resigning at my turn" else if (st.canResign || !st.canQueueResign) "Resign" else "Resign at my turn",
        checked = queued,
    ) { if (queued) vm.cancelQueuedResign() else onResign() }
    RegisterCommand(Cmd.NEXT_GAME, enabled = over != null && !over.matchOver) { vm.nextGame() }
    RegisterCommand(Cmd.LAST_SUMMARY, enabled = m.gameNumber > 1) { onSummary() }
    RegisterCommand(Cmd.TOGGLE_STATS, label = if (st.inStats) "Exclude from statistics" else "Count in statistics again") { vm.toggleInStats() }
    RegisterCommand(Cmd.ABANDON, label = if (m.rules.isMoney) "End session" else "Abandon match") { onAbandon() }
    RegisterCommand(Cmd.ANALYZE_NOW, enabled = st.analysisLeft > 0) { vm.analyzeMatchNow(m.id) }
    RegisterCommand(Cmd.STOP_ANALYSIS, enabled = st.analysisLeft > 0) { vm.stopAnalysis(m.id) }
    val played = m.gameNumber > 1 || over != null
    RegisterCommand(Cmd.REVIEW_MATCH, enabled = played) { onReview(m.id) }
    RegisterCommand(Cmd.REVIEW_MISTAKES, enabled = played) { onReviewMistakes(m.id, over?.reviewGame ?: (m.gameNumber - 1).coerceAtLeast(1)) }
    RegisterCommand(Cmd.REVIEW_MARKED, enabled = over != null && over.marked > 0) { over?.let { onReviewMarked(m.id, it.markedGame) } }
}

@Composable
private fun StatusText(st: GameUiState) {
    // (Looking at an earlier position adds no line here: the chips and arrows below must not move.)
    val text = st.busyText ?: st.message ?: return
    Text(
        text, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun PlayerCard(st: GameUiState, m: MatchState, player: Int) {
    val colors = boardColorsFor(st.settings)
    val toAct = toAct(m, player)
    val name = if (player == Player.HUMAN) "You" else "Computer"
    val bg = if (toAct) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
    Column(
        Modifier.fillMaxWidth().background(bg, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(14.dp).background(if (player == Player.HUMAN) colors.human else colors.cpu, RoundedCornerShape(7.dp)))
            Spacer(Modifier.width(6.dp))
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = if (toAct) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
            Text(scoreText(m, player, st.settings.scoreAway), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

private fun toAct(m: MatchState, player: Int): Boolean = when (m.game.phase) {
    Phase.DOUBLED -> m.game.responder == player
    Phase.BEAVERED -> m.game.turn == player
    Phase.OVER, Phase.OPENING -> false
    else -> m.game.turn == player
}

private fun scoreText(m: MatchState, player: Int, away: Boolean = false): String = if (m.rules.isMoney) {
    val net = m.score[player] - m.score[Player.other(player)]
    "${if (net > 0) "+" else ""}$net"
} else if (away) "${m.rules.length - m.score[player]}-away" else "${m.score[player]}/${m.rules.length}"

@Composable
private fun BoardArea(
    st: GameUiState, m: MatchState, vm: GameViewModel, modifier: Modifier, capture: BoardCapture, askState: MutableState<CubeAsk?>,
    /** Landscape: Undo on the board in the half without dice, and Done next to the dice. */
    onBoardMoveButtons: Boolean = false,
) {
    val g = m.game
    // Looking at an earlier decision (the arrows): its board, dice and cube, nothing to play.
    val viewed = st.viewing?.let { g.decisions.getOrNull(it) }
    val showBoard = viewed?.board ?: st.preview ?: st.board
    val previewing = st.preview != null || viewed != null
    val dice = buildList {
        if (viewed != null) {
            viewed.dice?.let { add(DiceView(viewed.player, it.highFirst)) }
            return@buildList
        }
        if (previewing) return@buildList
        // The computer's last roll stays visible (greyed) until the human rolls or doubles, or the
        // computer doubles.
        if (g.offeredCube == null) st.cpuLastDice?.let { add(DiceView(Player.CPU, it.highFirst, greyed = true)) }
        if (g.phase == Phase.MOVING && g.dice != null) {
            if (g.turn == Player.HUMAN) {
                add(DiceView(Player.HUMAN, st.diceOrder, st.diceDim, error = st.diceError))
            } else {
                add(DiceView(Player.CPU, g.dice.highFirst))
            }
        }
    }
    val interactive = !previewing && g.phase == Phase.MOVING && g.turn == Player.HUMAN && !st.busy
    Column(modifier) {
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            BoardView(
                board = showBoard,
                modifier = Modifier.fillMaxSize().then(capture.modifier),
                colors = boardColorsFor(st.settings),
                mirrored = st.settings.mirrorBoard,
                dice = dice,
                cube = if (viewed != null) viewed.cube else offeredCube(g),
                cubeOffered = viewed == null && g.offeredCube != null,
                cubeAside = viewed == null && passShown(st, m),
                crawford = viewed?.crawford ?: g.crawford,
                crawfordSign = st.settings.crawfordSign,
                dmp = com.boldblot.backgammonplyground.core.Scoring.isDmp(m.rules.length, viewed?.score ?: m.score, viewed?.crawford ?: g.crawford),
                movable = if (interactive) st.movable else emptySet(),
                lastCpuSteps = if (previewing) emptyList() else st.lastCpuSteps,
                lastCpuHits = st.lastCpuHits,
                pips = if (st.settings.showPips) listOf(showBoard.pipCount(Player.HUMAN), showBoard.pipCount(Player.CPU)) else null,
                // A hint play's arrows go on the live board (the dice stay, and the player can move).
                arrows = if (previewing || st.hint != null) st.previewArrows else emptyList(),
                onTap = if (interactive) vm::tap else null,
                onDrag = if (interactive) vm::drag else null,
                onDiceTap = if (interactive) vm::tapDice else null,
            )
            // On-board buttons in the human's half of the board.
            val w = maxWidth
            val margin = w * 0.018f
            val tray = w * 0.075f
            val bar = w * 0.065f
            val pointW = (w - tray - margin * 2 - bar) / 12
            val mirrored = st.settings.mirrorBoard
            val halfLeft = if (mirrored) margin + tray else margin + pointW * 6 + bar
            val buttonH = buttonHeight(w, maxHeight)
            // Double or Pass waiting for the confirmation bar's answer (Preferences → Play).
            var asking by askState
            if (asking == null) {
                Box(
                    Modifier.offset(x = halfLeft).width(pointW * 6).fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (viewed == null) OnBoardButtons(st, m, vm, pointW, buttonH, onDouble = {
                        if (st.settings.confirmDouble) asking = CubeAsk.DOUBLE else vm.double()
                    })
                }
            }
            // Looking at an earlier position: Back to game goes in the half without the dice (the dice are in the player's half).
            if (viewed != null) {
                val otherLeft = if (mirrored) margin + tray + pointW * 6 + bar else margin
                Box(Modifier.offset(x = otherLeft).width(pointW * 6).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = vm::backToGame, modifier = Modifier.widthIn(min = pointW * 3).height(buttonH),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    ) { Text("Back to game", style = buttonLabel(pointW), maxLines = 1, softWrap = false) }
                }
            }
            if (onBoardMoveButtons) MoveButtonsOnBoard(st, vm, w, maxHeight, mirrored, pointW, buttonH, interactive)
            // Pass to a double goes in the computer's half, next to the offered cube.
            if (viewed == null && passShown(st, m) && asking == null) {
                val geo = BoardGeometry(w.value, maxHeight.value, mirrored)
                Box(Modifier.offset(x = geo.cubeAside().second.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    FilledTonalButton(
                        onClick = { if (st.settings.confirmPass) asking = CubeAsk.PASS else vm.respond(false) },
                        modifier = Modifier.width(geo.passW.dp).height(buttonH),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                    ) { Text("Pass", style = buttonLabel(pointW), maxLines = 1) }
                }
            }
            // Signs in the middle of the board, centred on the bar: a cube mistake, or a turn without
            // a choice. They go just below the centre line, or to the nearest free spot so that they
            // cover neither the Roll / Double buttons nor checkers, dice or the cube.
            val buttonsShown = viewed != null || !st.busy && st.preview == null && !blocksPlay(st) && !st.autoFinishing &&
                ((g.phase == Phase.PRE_ROLL && g.turn == Player.HUMAN) || (g.phase == Phase.DOUBLED && g.responder == Player.HUMAN) ||
                    (g.phase == Phase.BEAVERED && g.turn == Player.HUMAN))
            val boardW = maxWidth
            val boardH = maxHeight
            Box(Modifier.width(barCenterXOf(boardW, mirrored) * 2).fillMaxHeight(), contentAlignment = Alignment.Center) {
                Column(
                    Modifier
                        .wrapContentWidth(unbounded = true)
                        .layout { measurable, constraints ->
                            val p = measurable.measure(constraints)
                            val geo = BoardGeometry(boardW.toPx(), boardH.toPx(), mirrored)
                            val top = signTop(
                                geo, showBoard, dice, offeredCube(g), g.offeredCube != null, buttonsShown,
                                p.width.toFloat(), p.height.toFloat(),
                                buttonH.toPx() / 2, 4.dp.toPx(),
                            )
                            layout(p.width, p.height) { p.place(0, (top - (geo.height - p.height) / 2).roundToInt()) }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CubeMistakeSign(st, m)
                    BoardNoticeSign(st)
                }
            }
            // Above everything on the board, signs included.
            asking?.let { ask ->
                CubeConfirmBar(
                    ask, g, buttonH,
                    onCancel = { asking = null },
                    onConfirm = { stopAsking ->
                        asking = null
                        if (stopAsking) vm.stopConfirming(double = ask == CubeAsk.DOUBLE)
                        if (ask == CubeAsk.DOUBLE) vm.double() else vm.respond(false)
                    },
                )
            }
        }
        if (st.busy) LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp)) else Spacer(Modifier.height(2.dp))
    }
}

/**
 * Landscape: Undo in the computer's half (no dice there while the human moves), centred, once a checker
 * has moved; Done next to the human's dice, on the side away from the bar, once the move is complete.
 */
@Composable
private fun MoveButtonsOnBoard(
    st: GameUiState, vm: GameViewModel, w: Dp, h: Dp, mirrored: Boolean, pointW: Dp, buttonH: Dp, interactive: Boolean,
) {
    if (!interactive || st.noLegalMove || st.feedback != null || st.autoFinishing) return
    val geo = BoardGeometry(w.value, h.value, mirrored)
    val label = buttonLabel(pointW)
    val pad = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
    if (st.steps.isNotEmpty()) {
        val cpuHalfLeft = if (mirrored) geo.columnLeft(6) else geo.columnLeft(0)
        Box(Modifier.offset(x = cpuHalfLeft.dp).width((geo.pointW * 6).dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
            FilledTonalButton(onClick = vm::undo, modifier = Modifier.width(pointW * 2 - 4.dp).height(buttonH), contentPadding = pad) {
                Icon(Icons.AutoMirrored.Filled.Undo, null, Modifier.size(16.dp))
                Spacer(Modifier.width(2.dp))
                Text("Undo", style = label, maxLines = 1)
            }
        }
    }
    if (st.moveComplete) {
        val dice = geo.diceRects(Player.HUMAN)
        val gap = geo.dieSize * 0.3f
        val chipW = geo.pointW * 1.6f
        val x = if (!mirrored) dice[1].right + gap else dice[0].left - gap - chipW
        Box(Modifier.offset(x = x.dp, y = dice[0].top.dp).width(chipW.dp).height(geo.dieSize.dp)) {
            Button(onClick = vm::confirmMove, modifier = Modifier.fillMaxSize(), contentPadding = pad, shape = RoundedCornerShape(8.dp)) {
                Text("Done", style = label, maxLines = 1)
            }
        }
    }
}

@Composable
private fun OnBoardButtons(st: GameUiState, m: MatchState, vm: GameViewModel, pointW: Dp, height: Dp, onDouble: () -> Unit) {
    val g = m.game
    if (st.busy || st.preview != null || blocksPlay(st) || st.autoFinishing) return
    // A pair of buttons stays within the four middle points, clear of the stacks on the outer
    // ones (the 6- and 19-points at the start); on a narrow board with a smaller label.
    val pairMod = Modifier.width(pointW * 2 - 4.dp).height(height)
    // Small side padding unless the buttons have room to spare (a big landscape board).
    val pad = androidx.compose.foundation.layout.PaddingValues(horizontal = if (pointW < 48.dp) 2.dp else 14.dp, vertical = 0.dp)
    val label = buttonLabel(pointW)
    when {
        g.phase == Phase.PRE_ROLL && g.turn == Player.HUMAN -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (g.mayDouble(m.rules, m.score)) {
                FilledTonalButton(onClick = onDouble, modifier = pairMod, contentPadding = pad) { Text("Double", style = label, maxLines = 1) }
            }
            Button(onClick = vm::roll, modifier = pairMod, contentPadding = pad) { Text("Roll", style = label, maxLines = 1) }
        }
        // Pass is in the computer's half (see BoardArea).
        g.phase == Phase.DOUBLED && g.responder == Player.HUMAN -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.respond(true) }, modifier = pairMod, contentPadding = pad) {
                Text("Take ${g.cube.value * 2}", style = label, maxLines = 1)
            }
            if (m.rules.beaversAllowed) {
                FilledTonalButton(onClick = { vm.respond(true, beaver = true) }, modifier = pairMod, contentPadding = pad) {
                    Text("Beaver ${g.cube.value * 4}", style = label, maxLines = 1)
                }
            }
        }
        // The cube on the board shows the value at stake.
        g.phase == Phase.BEAVERED && g.turn == Player.HUMAN -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.answerBeaver(false) }, modifier = pairMod, contentPadding = pad) { Text("Play on", style = label, maxLines = 1) }
            FilledTonalButton(onClick = { vm.answerBeaver(true) }, modifier = pairMod, contentPadding = pad) {
                Text("Raccoon", style = label, maxLines = 1)
            }
        }
        else -> Unit
    }
}

/** A cube action waiting for the confirmation bar (Preferences → Play: confirm Double / Pass). */
private enum class CubeAsk { DOUBLE, PASS }

/**
 * The confirmation for Double or Pass: a bar across the middle of the board with the question,
 * Cancel and the action side by side in the centre, and "Don't ask again" (saved when the action
 * is confirmed). It stays until answered; a tap on the board outside it cancels. Taps in the
 * first moment after it opens are ignored, so a double tap on Double or Pass does not confirm.
 */
@Composable
private fun CubeConfirmBar(ask: CubeAsk, g: GameState, buttonH: Dp, onCancel: () -> Unit, onConfirm: (stopAsking: Boolean) -> Unit) {
    var stopAsking by remember { mutableStateOf(false) }
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(400)
        ready = true
    }
    val question = when (ask) {
        CubeAsk.DOUBLE -> "Double to ${g.cube.value * 2}?"
        CubeAsk.PASS -> "Pass and lose ${g.cube.value} point${if (g.cube.value == 1) "" else "s"}?"
    }
    val action = if (ask == CubeAsk.DOUBLE) "Double" else "Pass"
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.18f))
            .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) {
                if (ready) onCancel()
            },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            // Taps on the bar itself do nothing.
            Modifier.fillMaxWidth().clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) {},
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f), shadowElevation = 6.dp,
        ) {
            BoxWithConstraints(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                val buttons: @Composable () -> Unit = {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { if (ready) onCancel() }, modifier = Modifier.height(buttonH.coerceAtLeast(36.dp))) { Text("Cancel", maxLines = 1) }
                        Button(onClick = { if (ready) onConfirm(stopAsking) }, modifier = Modifier.height(buttonH.coerceAtLeast(36.dp))) { Text(action, maxLines = 1) }
                    }
                }
                val questionText: @Composable () -> Unit = {
                    Text(question, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
                val dontAsk: @Composable () -> Unit = {
                    Row(
                        Modifier.toggleable(stopAsking, role = Role.Checkbox) { stopAsking = it }.padding(end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = stopAsking, onCheckedChange = null, modifier = Modifier.size(32.dp).padding(6.dp))
                        Text("Don't ask again", style = MaterialTheme.typography.labelMedium, maxLines = 1, softWrap = false)
                    }
                }
                if (maxWidth >= 560.dp) {
                    // Question at the start, the buttons in the centre, "Don't ask again" at the end.
                    Box(Modifier.fillMaxWidth()) {
                        Box(Modifier.align(Alignment.CenterStart)) { questionText() }
                        Box(Modifier.align(Alignment.Center)) { buttons() }
                        Box(Modifier.align(Alignment.CenterEnd)) { dontAsk() }
                    }
                } else {
                    // A narrow (portrait) board: the question and "Don't ask again" above the buttons.
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f)) { questionText() }
                            dontAsk()
                        }
                        buttons()
                    }
                }
            }
        }
    }
}

/**
 * Height of the on-board buttons on a board [w] × [h]: 40 dp, or less (down to 24 dp) to fit
 * between two five-checker stacks, as on a portrait board.
 */
private fun buttonHeight(w: Dp, h: Dp): Dp {
    val geo = BoardGeometry(w.value, h.value)
    val gap = geo.surfaceBottom - geo.surfaceTop - 20 * geo.radius
    return (gap - 4f).dp.coerceIn(24.dp, 40.dp)
}

/** The on-board buttons' label: smaller, without letter spacing, on a narrow (portrait) board. */
@Composable
private fun buttonLabel(pointW: Dp): TextStyle =
    if (pointW < 36.dp) MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp) else MaterialTheme.typography.labelLarge

/** The computer doubles the human: Pass shows in the computer's half, the offered cube beside it. */
private fun passShown(st: GameUiState, m: MatchState): Boolean =
    !st.busy && st.preview == null && !blocksPlay(st) && !st.autoFinishing && m.game.phase == Phase.DOUBLED && m.game.responder == Player.HUMAN



/** The cube to draw: while a double (or beaver) is offered, the value before the offer (the board shows it doubled). */
private fun offeredCube(g: GameState): CubeState = g.offeredCube?.let { CubeState(it / 2, g.cube.owner) } ?: g.cube

/** x of the bar's centre for a board [w] wide (as in [BoardGeometry]; [mirrored]: tray on the left). */
private fun barCenterXOf(w: Dp, mirrored: Boolean): Dp {
    val margin = w * 0.018f
    val bar = w * 0.065f
    val tray = w * 0.075f
    val pointW = (w - tray - margin * 2 - bar) / 12
    return (if (mirrored) tray else 0.dp) + margin + pointW * 6 + bar / 2
}

/**
 * Top of the board signs ([w] × [h] px, centred on the bar): just below the centre line if
 * that is free, otherwise the nearest free spot (below preferred) that covers neither the
 * on-board buttons ([buttons], [buttonHalf] high on each side of the centre) nor checkers on
 * the points under the sign, bar checkers, dice or the cube. If nothing is free: the spot
 * that covers least, the buttons counting most.
 */
private fun signTop(
    g: BoardGeometry, board: Board, dice: List<DiceView>, cube: CubeState, cubeOffered: Boolean,
    buttons: Boolean, w: Float, h: Float, buttonHalf: Float, gap: Float,
): Float {
    val x0 = g.barCenterX - w / 2
    val x1 = g.barCenterX + w / 2
    // (top, bottom, weight): what the sign should not cover, by importance
    val blocked = ArrayList<Triple<Float, Float, Float>>()
    fun block(left: Float, right: Float, top: Float, bottom: Float, weight: Float = 1f) {
        if (right > x0 && left < x1) blocked += Triple(top, bottom, weight)
    }
    val mid = g.height / 2
    val r = g.radius
    for (i in 0 until 24) {
        val n = board.count(Player.HUMAN, i) + board.count(Player.CPU, 23 - i)
        if (n == 0) continue
        val left = g.columnLeft(g.column(i))
        val extent = minOf(n, 5) * 2 * r
        if (g.isTop(i)) block(left, left + g.pointW, g.surfaceTop, g.surfaceTop + extent)
        else block(left, left + g.pointW, g.surfaceBottom - extent, g.surfaceBottom)
    }
    val humanBar = minOf(board.bar(Player.HUMAN), 4)
    val cpuBar = minOf(board.bar(Player.CPU), 4)
    if (humanBar > 0) block(g.barCenterX - r, g.barCenterX + r, mid - 1.2f * r - humanBar * 2 * r, mid - 1.2f * r)
    if (cpuBar > 0) block(g.barCenterX - r, g.barCenterX + r, mid + 1.2f * r, mid + 1.2f * r + cpuBar * 2 * r)
    for (d in dice) for (rect in g.diceRects(d.owner)) block(rect.left, rect.right, rect.top, rect.bottom)
    val s = g.barW * 0.86f
    val cubeY = when {
        cubeOffered -> mid - s / 2
        cube.owner == Player.HUMAN -> g.height - g.vMargin - s
        cube.owner == Player.CPU -> g.vMargin
        else -> mid - s / 2
    }
    val cubeX = if (cubeOffered) g.columnLeft(3) - s / 2 else g.barCenterX - s / 2
    block(cubeX, cubeX + s, cubeY, cubeY + s)
    if (buttons) block(g.columnLeft(6), g.playRight, mid - buttonHalf, mid + buttonHalf, weight = 4f)

    // The free spot nearest to just below the centre (below preferred); if there is none, the
    // spot that covers least (the buttons count most).
    val preferred = mid + gap
    fun cost(y: Float): Float {
        var covered = 0f
        for ((t, b, w) in blocked) covered += w * (minOf(y + h, b + gap) - maxOf(y, t - gap)).coerceAtLeast(0f)
        val distance = kotlin.math.abs(y - preferred) * if (y < preferred) 1.3f else 1f
        return covered * 1000f + distance
    }
    var best = preferred
    var bestCost = Float.MAX_VALUE
    var y = g.surfaceTop
    while (y + h <= g.surfaceBottom) {
        val c = cost(y)
        if (c < bestCost) { bestCost = c; best = y }
        y += 2f
    }
    return best
}

/**
 * A button-shaped sign on the board while the practice prompt is about one of the human's cube
 * decisions (yellow for an error, red for a blunder), as the prompt below the board is easy to
 * miss. Play goes on as usual; a tap shows the cube analysis.
 */
@Composable
private fun CubeMistakeSign(st: GameUiState, m: MatchState) {
    val fb = st.feedback ?: return
    val d = fb.decision
    val eval = d.cubeEval ?: return
    if (d.checker != null || st.preview != null || st.autoFinishing) return
    // "Try again": the sign names the mistake but keeps the answer back.
    val hidden = st.retry?.revealed == false
    var open by remember(fb) { mutableStateOf(false) }
    val (container, content) = when (fb.severity) {
        Severity.BLUNDER -> ArrowColors.blunder to Color.White
        Severity.ERROR -> CUBE_ERROR_YELLOW to Color.Black
        else -> ArrowColors.inaccuracy to Color.Black
    }
    Button(
        onClick = { if (!hidden) open = true },
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        shape = RoundedCornerShape(SIGN_CORNER),
        contentPadding = SIGN_PADDING,
        modifier = Modifier.defaultMinSize(minHeight = 1.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
    ) {
        SignText("Cube ${fb.severity.label.lowercase()}", if (hidden) null else errText(-fb.weightedError / XgRules.errorFactor(m.rules.length)))
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text("You: ${describeAction(d.kind, d.board, Player.HUMAN, d.move)}") },
            text = {
                Column(Modifier.desktopVerticalScroll()) {
                    SeverityBadge(fb.severity, fb.weightedError, factor = XgRules.errorFactor(m.rules.length))
                    Spacer(Modifier.height(8.dp))
                    CubeTable(eval, d.kind, XgRules.errorFactor(m.rules.length), d.beavers, colors = boardColorsFor(st.settings), rollerHuman = !d.kind.isResponse)
                }
            },
            confirmButton = { TextButton(onClick = { open = false }) { Text("Close") } },
        )
    }
}

/** Yellow of the cube-error sign (the orange arrow colour reads as red at a glance). */
private val CUBE_ERROR_YELLOW = Color(0xFFFFC928)
private val NO_MOVE_GREY = Color(0xFFE6E9EC)
private val FORCED_BLUE = Color(0xFF9CCBFF)
private val SIGN_CORNER = 10.dp
private val SIGN_PADDING = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)

/** Two lines of a board sign: a bold title and a detail line (equity lost, the move, the roll). */
@Composable
private fun SignText(title: String, detail: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
        if (detail != null) {
            Text(detail, style = MaterialTheme.typography.labelMedium, maxLines = 1, softWrap = false)
        }
    }
}

/**
 * "You can't move" / "Computer can't move" / "Forced move" in the middle of the board for a
 * moment, styled like the cube sign. Not clickable: taps go through to the board.
 */
@Composable
private fun BoardNoticeSign(st: GameUiState) {
    val n = st.notice ?: return
    if (st.preview != null || st.autoFinishing) return
    val container = if (n.kind == BoardNotice.Kind.FORCED) FORCED_BLUE else NO_MOVE_GREY
    Box(
        Modifier
            .shadow(3.dp, RoundedCornerShape(SIGN_CORNER))
            .background(container, RoundedCornerShape(SIGN_CORNER))
            .padding(SIGN_PADDING),
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.Black) { SignText(n.title, n.detail) }
    }
}

@Composable
private fun Panels(st: GameUiState, m: MatchState, vm: GameViewModel) {
    val g = m.game
    // Practice-mode prompt has priority over everything else.
    st.feedback?.let { FeedbackCard(st, m, vm); return }

    st.hint?.let { h ->
        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Column(Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Hint (not counted in statistics)", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    TextButton(onClick = vm::closeHint) { Text("Close") }
                }
                CandidateList(
                    board = g.board, player = Player.HUMAN, analysis = h, factor = XgRules.errorFactor(m.rules.length),
                    highlightIndex = null, selectedIndex = st.previewCandidate, detailed = true,
                    moverColor = boardColorsFor(st.settings).human, opponentColor = boardColorsFor(st.settings).cpu,
                    moverRim = boardColorsFor(st.settings).humanRim, opponentRim = boardColorsFor(st.settings).cpuRim,
                    onSelect = { i, c -> vm.showHintPlay(i, arrowsFor(Player.HUMAN, c.move, ArrowColors.best)) },
                )
                if (st.previewArrows.isNotEmpty()) TextButton(onClick = { vm.preview(null) }) { Text("Clear arrows") }
            }
        }
    }
    st.cubeHint?.let { c ->
        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Column(Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Cube hint (not counted)", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    TextButton(onClick = vm::closeHint) { Text("Close") }
                }
                val kind = when (g.phase) {
                    Phase.DOUBLED -> DecisionKind.TAKE
                    Phase.BEAVERED -> DecisionKind.RACCOON
                    else -> null
                }
                CubeTable(c, kind, XgRules.errorFactor(m.rules.length), m.rules.beaversAllowed, colors = boardColorsFor(st.settings), rollerHuman = kind != DecisionKind.TAKE)
            }
        }
    }
}

/** Off-board controls: undo / done / hint (and cube actions as a fallback). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Actions(st: GameUiState, m: MatchState, vm: GameViewModel, vertical: Boolean, onResign: () -> Unit, askState: MutableState<CubeAsk?>) {
    val g = m.game
    val humanTurn = g.turn == Player.HUMAN
    if (st.autoFinishing) return
    // The computer's turn (but not its double, which the human answers here) has no move buttons.
    val viewing = st.viewing
    val humanActs = viewing == null && !st.busy && st.feedback == null && (humanTurn || g.phase == Phase.DOUBLED)
    val moving = humanActs && g.phase == Phase.MOVING && humanTurn && !st.noLegalMove
    val canHint = humanActs && (
        (g.phase == Phase.MOVING && humanTurn && !st.noLegalMove) ||
            (g.phase == Phase.PRE_ROLL && humanTurn && g.mayDouble(m.rules, m.score)) ||
            (g.phase == Phase.DOUBLED && g.responder == Player.HUMAN) ||
            (g.phase == Phase.BEAVERED && humanTurn)
        )
    // In landscape, Undo and Done are chips here unless Preferences → Board puts them on the board.
    val sideButtons = !vertical || !st.settings.onBoardMoveButtons
    // Undo and Done: chips beside the board (landscape) or under it (portrait), always there during
    // play and greyed out when they can't be used, so nothing jumps about.
    // The mark sits with them, for any decision of the player's: to roll or double, to take or pass, to move.
    val myDecision = viewing == null && st.gameOver == null && !st.busy && when (g.phase) {
        Phase.PRE_ROLL, Phase.MOVING, Phase.BEAVERED -> g.turn == Player.HUMAN
        Phase.DOUBLED -> g.responder == Player.HUMAN
        else -> false
    }
    val markChip: @Composable () -> Unit = {
        // Looking at an earlier position, the blot marks that one.
        com.boldblot.backgammonplyground.ui.components.MarkChip(
            on = if (viewing != null) viewing in g.marked else st.marking, enabled = viewing != null || myDecision, onClick = vm::toggleMark,
        )
    }
    // Arrows to the positions of earlier turns: in the same place whatever else changes (Preferences → Display can turn them off).
    val viewList = viewableDecisions(m)
    val arrows: @Composable () -> Unit = {
        if (st.settings.pastPositions) {
            com.boldblot.backgammonplyground.ui.components.ViewArrows(
                canBack = st.canView && viewList.any { viewing == null || it < viewing }, canForward = viewing != null,
                onBack = vm::viewBack, onForward = vm::viewForward, size = if (vertical) 30.dp else 36.dp,
            )
        }
    }
    val preRoll = humanActs && g.phase == Phase.PRE_ROLL && humanTurn
    val moveChips: @Composable () -> Unit = {
        // Portrait: the arrows keep the left end of the row whatever the chips beside them are; the chips are centred in the rest.
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (!vertical) arrows()
            // A FlowRow: with the desktop's wider text the mark chip wraps to a second line instead of being cut off.
            FlowRow(
                Modifier.weight(1f), itemVerticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ChipGapH, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(ChipGapV),
            ) {
            // Before the roll the Roll / Double buttons are on the board, and the chips become Roll / Double too.
            if (viewing != null) {
                com.boldblot.backgammonplyground.ui.components.BackToGameChip(onClick = vm::backToGame)
            } else if (preRoll) {
                com.boldblot.backgammonplyground.ui.components.RollChips(
                    canRoll = true, canDouble = g.mayDouble(m.rules, m.score), onRoll = vm::roll,
                    onDouble = { if (st.settings.confirmDouble) askState.value = CubeAsk.DOUBLE else vm.double() },
                )
            } else {
                com.boldblot.backgammonplyground.ui.components.MoveChips(
                    canUndo = moving && st.steps.isNotEmpty(), canDone = moving && st.moveComplete,
                    onUndo = vm::undo, onDone = vm::confirmMove,
                )
            }
            markChip()
            }
        }
    }
    if (vertical) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(ChipGapV),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (sideButtons || preRoll || viewing != null) moveChips() else markChip()
            // The side column: Resign takes Keep rolling's place and the arrows sit by it, so nothing needs scrolling.
            GameChips(st, m, vm, canHint, onResign, sideArrows = arrows)
        }
    } else {
        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) { moveChips() }
        GameChips(st, m, vm, canHint, onResign)
    }
    if (moving && st.steps.isEmpty() && !vertical) {
        Text(
            "Click a checker to move it by the left die (or drag it). Click the dice to swap them, and to finish your move.",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Hint, Keep rolling, Max Bearoff and Resign: always in the same place during a game, greyed out
 * when they can't be used. Resign while the computer plays offers the resignation at the human's next
 * turn (the chip then shows it is queued; a tap cancels).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GameChips(
    st: GameUiState, m: MatchState, vm: GameViewModel, canHint: Boolean, onResign: () -> Unit,
    /** The side column (landscape): Hint, Resign and these arrows share a row, then Max Bearoff and Keep rolling. */
    sideArrows: (@Composable () -> Unit)? = null,
) {
    val g = m.game
    val inPlay = st.gameOver == null && g.phase != Phase.OVER && g.phase != Phase.OPENING
    // Keep rolling makes sense while the human may double this game (or to switch it off).
    val keepRollingOn = st.keepRolling || (inPlay && Scoring.mayDouble(m.rules, g.cube, Player.HUMAN, m.score, g.crawford))
    val queued = st.queuedResign != null
    val hint: @Composable () -> Unit = {
        FilterChip(
            selected = false, onClick = vm::hint, enabled = canHint,
            leadingIcon = { Icon(Icons.Default.Lightbulb, null, Modifier.size(18.dp)) },
            label = { Text("Hint", maxLines = 1) },
        )
    }
    val keepRolling: @Composable () -> Unit = {
        FilterChip(
            selected = st.keepRolling, onClick = vm::toggleKeepRolling, enabled = keepRollingOn,
            label = { Text("Keep rolling", maxLines = 1) },
        )
    }
    // Autofinish is in the Game menu.
    val maxBearOff: @Composable () -> Unit = {
        FilterChip(
            selected = st.maxBearOff, onClick = vm::toggleMaxBearOff, enabled = inPlay || st.maxBearOff,
            // The landscape side column is narrow: drop the icon there so both chips fit one row.
            leadingIcon = if (sideArrows == null) ({ Icon(Icons.Default.FastForward, null, Modifier.size(18.dp)) }) else null,
            label = { Text("Max Bearoff", maxLines = 1) },
        )
    }
    val resign: @Composable () -> Unit = {
        FilterChip(
            selected = queued, onClick = { if (queued) vm.cancelQueuedResign() else onResign() },
            enabled = queued || st.canResign || st.canQueueResign,
            leadingIcon = { Icon(Icons.Default.Flag, null, Modifier.size(18.dp)) },
            label = { Text(if (queued) "Resigning at my turn" else "Resign", maxLines = 1) },
        )
    }
    if (sideArrows != null) {
        FlowRow(
            Modifier.fillMaxWidth(), maxItemsInEachRow = 3, itemVerticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChipGapH, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(ChipGapV),
        ) { hint(); resign(); sideArrows() }
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ChipGapH, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(ChipGapV),
        ) { maxBearOff(); keepRolling() }
        return
    }
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ChipGapH, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(ChipGapV),
    ) { hint(); keepRolling(); maxBearOff(); resign() }
}

/**
 * Winning chances as XG shows them: one bar, from the left your backgammons, gammons and single
 * wins, then the computer's single wins, gammons and backgammons to the right edge. The numbers
 * show with [MeterMode.BAR_AND_NUMBERS], or in [MeterMode.BAR] after a tap until your next decision.
 */
@Composable
private fun Meter(st: GameUiState, vm: GameViewModel, twoLines: Boolean) {
    val mode = st.settings.meter
    val p = st.meter ?: return
    if (mode == MeterMode.OFF) return
    val colors = boardColorsFor(st.settings)
    val win = p[0]
    val lose = 1 - p[0]
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 3.dp)
            .then(if (mode == MeterMode.BAR) Modifier.clickable(onClick = vm::toggleMeterNumbers) else Modifier),
    ) {
        com.boldblot.backgammonplyground.ui.components.ChancesBar(p, colors.human, colors.cpu)
        if (mode == MeterMode.BAR_AND_NUMBERS || st.meterNumbers) {
            val you = "You ${pct(win)} (G ${pct(p[1])}, B ${pct(p[2])})"
            val cpu = "Computer ${pct(lose)} (G ${pct(p[3])}, B ${pct(p[4])})"
            val style = MaterialTheme.typography.labelSmall
            if (twoLines) {
                Text(you, style = style, modifier = Modifier.padding(top = 2.dp))
                Text(cpu, style = style)
            } else {
                Row(Modifier.fillMaxWidth().padding(top = 2.dp)) {
                    Text(you, style = style, modifier = Modifier.weight(1f))
                    Text(cpu, style = style, textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
private fun FeedbackCard(st: GameUiState, m: MatchState, vm: GameViewModel) {
    val fb = st.feedback ?: return
    if (st.retry?.revealed == false) {
        TryAgainCard(fb, vm)
        return
    }
    val d = fb.decision
    val factor = XgRules.errorFactor(m.rules.length)
    Card(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(12.dp)) {
            // A long badge ("Blunder -0.068 (×1.5 in PR)") wraps; Continue keeps its width.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { SeverityBadge(fb.severity, fb.weightedError, factor = factor) }
                Spacer(Modifier.width(8.dp))
                Button(onClick = vm::dismissFeedback) { Text("Continue", maxLines = 1, softWrap = false) }
            }
            Text(
                "You played: ${describeAction(d.kind, d.board, Player.HUMAN, d.move)}",
                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(Modifier.height(6.dp))
            when {
                d.checker != null -> {
                    val a = d.checker
                    val colors = boardColorsFor(st.settings)
                    val played = a.candidates.getOrNull(a.playedIndex)
                    val best = a.candidates.firstOrNull()
                    fun showBoth() = vm.preview(
                        d.board,
                        (best?.let { arrowsFor(Player.HUMAN, it.move, ArrowColors.best) } ?: emptyList()) +
                            (played?.let { arrowsFor(Player.HUMAN, it.move, ArrowColors.forSeverity(fb.severity)) } ?: emptyList()),
                        -1,
                    )
                    if (played != null && best != null) {
                        PlayedBestBar(
                            playedText = MoveNotation.format(d.board, Player.HUMAN, played.move),
                            playedError = fb.weightedError / factor, playedSeverity = fb.severity,
                            bestText = MoveNotation.format(d.board, Player.HUMAN, best.move), bestEquity = best.equity,
                            onPlayed = { vm.preview(d.board, arrowsFor(Player.HUMAN, played.move, ArrowColors.forSeverity(fb.severity)), a.playedIndex) },
                            onBest = { vm.preview(d.board, arrowsFor(Player.HUMAN, best.move, ArrowColors.best), 0) },
                            stacked = true,
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                    CandidateList(
                        board = d.board, player = Player.HUMAN, analysis = a, factor = factor,
                        highlightIndex = a.playedIndex, maxRows = 5, selectedIndex = st.previewCandidate,
                        detailed = true, moverColor = colors.human, opponentColor = colors.cpu, moverRim = colors.humanRim, opponentRim = colors.cpuRim,
                        onSelect = { i, c ->
                            val sev = XgRules.severity((a.bestEquity - c.equity) * factor)
                            vm.preview(d.board, arrowsFor(Player.HUMAN, c.move, ArrowColors.forSeverity(sev)), i)
                        },
                    )
                    OutlinedButton(onClick = { showBoth() }) { Text("Played vs best") }
                }
                d.cubeEval != null -> {
                    CubeTable(d.cubeEval, d.kind, factor, d.beavers, colors = boardColorsFor(st.settings), rollerHuman = !d.kind.isResponse)
                }
                else -> Unit
            }
        }
    }
}

/** "Try again" training: names the mistake (not its size or the answer) and offers another try. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TryAgainCard(fb: Feedback, vm: GameViewModel) {
    val d = fb.decision
    val what = when (fb.severity) {
        Severity.BLUNDER -> "a blunder"
        Severity.ERROR -> "an error"
        else -> "not the best play"
    }
    val (container, content) = when (fb.severity) {
        Severity.BLUNDER -> ArrowColors.blunder to Color.White
        Severity.ERROR -> CUBE_ERROR_YELLOW to Color.Black
        else -> ArrowColors.inaccuracy to Color.Black
    }
    Card(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                fb.severity.label,
                style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = content,
                modifier = Modifier.background(container, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 3.dp),
            )
            Text(
                "${describeAction(d.kind, d.board, Player.HUMAN, d.move)} was $what. Try the decision again: your first try is what counts.",
                style = MaterialTheme.typography.bodyMedium,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), itemVerticalAlignment = Alignment.CenterVertically) {
                Button(onClick = vm::tryAgain) { Text("Try again") }
                OutlinedButton(onClick = vm::showAnswer) { Text("Show answer") }
                TextButton(onClick = vm::dismissFeedback) { Text("Continue") }
            }
        }
    }
}

/**
 * End-of-game summary: result, score and both players' PR (with XG's rating), errors and
 * blunders, luck (with gnubg's luck rating), for this game or for the whole match / session
 * so far (a switch at the top; the match is shown first once it is over). The body scrolls,
 * so nothing is cut off in landscape or with a large font; the buttons always stay visible.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun GameOverDialog(
    info: GameOverInfo, inStats: Boolean, gameSummary: Boolean, analysisLeft: Int, analysisEta: Long?, vm: GameViewModel, onReview: (Long) -> Unit,
    onReviewMistakes: (matchId: Long, gameNumber: Int) -> Unit, onReviewMarked: (matchId: Long, gameNumber: Int) -> Unit,
    onBack: () -> Unit, onEndSession: () -> Unit,
) {
    val won = info.result.winner == Player.HUMAN
    val kind = when (info.result.type) {
        WinType.SINGLE -> if (info.result.byPass) "by a pass" else "a single game"
        WinType.GAMMON -> "a gammon"
        WinType.BACKGAMMON -> "a backgammon"
    }
    val title = when {
        info.matchOver -> if (info.matchWinner == Player.HUMAN) "You win the match!" else "Computer wins the match"
        won -> "You win $kind"
        else -> "Computer wins $kind"
    }
    val report = info.match?.takeIf { it.games > 1 }
    val matchName = if (info.isMoney) "Session" else "Match"
    var showMatch by remember(info) { mutableStateOf(info.matchOver && report != null) }
    // The match's saved games, for stepping through their summaries; this game is shown from [info].
    var saved by remember(info) { mutableStateOf<List<Pair<GameEntity, GameSummary>>>(emptyList()) }
    LaunchedEffect(info) { if (report != null) saved = vm.app.repository.gameSummaries(info.matchId) }
    var shown by remember(info) { mutableStateOf(info.gameNumber) }
    // At the end of a match with the summary after each game turned off: a box to turn it back on.
    val offerSummary = remember(info) { info.matchOver && !gameSummary }
    Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.padding(12.dp).widthIn(max = 640.dp).fillMaxWidth(),
        ) {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 4.dp)) {
                val scroll = rememberScrollState()
                Column(
                    Modifier.weight(1f, fill = false)
                        .desktopVerticalScroll(scroll)
                        .padding(end = 6.dp),
                ) {
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        (if (info.matchOver) "Last game: ${if (won) "you" else "computer"}" else if (won) "You" else "Computer") +
                            " +${info.result.points} point${if (info.result.points > 1) "s" else ""}" +
                            (if (info.result.resigned) "  (${if (won) "computer" else "you"} resigned)" else "") +
                            " · " + (if (info.isMoney) "session" else "score") + ": you ${info.score[0]}, computer ${info.score[1]}",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    if (report != null) {
                        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            SegmentedButton(
                                selected = !showMatch, onClick = { showMatch = false },
                                shape = SegmentedButtonDefaults.itemShape(0, 2),
                            ) { Text("Game $shown", maxLines = 1) }
                            SegmentedButton(
                                selected = showMatch, onClick = { showMatch = true },
                                shape = SegmentedButtonDefaults.itemShape(1, 2),
                            ) { Text("$matchName (${report.games} games)", maxLines = 1) }
                        }
                    }
                    val index = saved.indexOfFirst { it.first.number == shown }
                    if (report != null && !showMatch && saved.size > 1 && index >= 0) {
                        GameStepper(shown, index, saved.size) { shown = saved[it].first.number }
                    }
                    if (analysisLeft > 0) {
                        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Analyzing: $analysisLeft decision${if (analysisLeft > 1) "s" else ""} left" +
                                        (analysisEta?.let { " · about ${durationText(it)}" } ?: ""),
                                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    if (info.matchOver) "PR and mistakes fill in as it goes." else "You can play on; PR and mistakes fill in as it goes.",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            TextButton(onClick = { vm.stopAnalysis(info.matchId) }) { Text("Stop") }
                        }
                    }
                    if (info.notAnalyzed > 0 && analysisLeft == 0) {
                        Text(
                            "${info.notAnalyzed} decision${if (info.notAnalyzed > 1) "s" else ""} not analyzed: PR and mistakes count only " +
                                "the analyzed ones. Analyze again in the review finishes them.",
                            style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    if (info.offerAnalysis && analysisLeft == 0) {
                        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Played without analysis (Preferences → Training).",
                                style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f),
                            )
                            OutlinedButton(onClick = { vm.analyzeMatchNow(info.matchId) }) { Text("Analyze now") }
                        }
                    }
                    // The automatic New errors methods: what of this match went to My positions.
                    val filed by produceState<String?>(null, info, analysisLeft) {
                        value = com.boldblot.backgammonplyground.ui.positions.filedText(vm.app, info.matchId, info.matchOver)
                    }
                    filed?.let { Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp)) }
                    if (info.afterMatch && analysisLeft == 0 && !info.offerAnalysis) {
                        Text(
                            when (info.mistakes) {
                                0 -> "No errors or blunders this match. Well played!"
                                1 -> "You made 1 error or blunder this match."
                                else -> "You made ${info.mistakes} errors and blunders this match."
                            },
                            style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                    if (report != null && showMatch) {
                        SummaryRow(
                            if (info.matchLength > 0) "${info.matchLength}-point match" else "Money session",
                            "You", "Computer", header = true,
                        )
                        SummaryRow("Games won", report.gamesWon[0].toString(), report.gamesWon[1].toString(), strong = true)
                        PerformanceRows(report.human, report.cpu)
                    } else if (shown != info.gameNumber && index >= 0) {
                        SavedGameRows(saved[index].first, saved[index].second)
                    } else {
                        SummaryRow(if (report != null) "Game ${info.gameNumber}" else "This game", "You", "Computer", header = true)
                        PerformanceRows(info.gamePerformance, info.cpuPerformance)
                    }
                    val before = info.match?.eloBefore
                    val after = info.match?.eloAfter
                    if (info.matchOver && before != null && after != null) {
                        val change = after - before
                        Text(
                            "Your FIBS rating: ${fmt(before, 1)} → ${fmt(after, 1)} (${if (change >= 0) "+" else "−"}${fmt(kotlin.math.abs(change), 1)})",
                            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    if (!inStats) {
                        Text(
                            "This ${if (info.isMoney) "session" else "match"} is not counted in your statistics.",
                            style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    Text(
                        "Luck: each player's rolls compared with the average roll in the same position, so both " +
                            "players can be lucky; the net luck shows who was luckier.",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
                    )
                    if (!info.matchOver) {
                        Row(
                            Modifier.fillMaxWidth().toggleable(!gameSummary, role = Role.Checkbox) { vm.setGameSummary(!it) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = !gameSummary, onCheckedChange = null)
                            Text(
                                "Don't show this after each game (Preferences → After each game)",
                                style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    } else if (offerSummary) {
                        Row(
                            Modifier.fillMaxWidth().toggleable(gameSummary, role = Role.Checkbox) { vm.setGameSummary(it) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = gameSummary, onCheckedChange = null)
                            Text(
                                "Show the summary after each game",
                                style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
                var deleting by remember { mutableStateOf(false) }
                if (deleting) DeleteMatchDialog(
                    vm.app, info.matchId, onDismiss = { deleting = false },
                    onConfirm = { errors -> deleting = false; vm.deleteMatch(info.matchId, errors) { onBack() } },
                ) {}
                androidx.compose.foundation.layout.FlowRow(Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.End) {
                    // The match's summary can delete it (history, statistics, Match Library; its errors too).
                    if (info.matchOver) TextButton(onClick = { deleting = true }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                    if (info.afterMatch && (info.mistakes > 0 || analysisLeft > 0)) {
                        TextButton(onClick = { onReviewMistakes(info.matchId, info.reviewGame) }, enabled = analysisLeft == 0) { Text("Review my mistakes") }
                    } else {
                        TextButton(onClick = { onReview(info.matchId) }) { Text("Review") }
                    }
                    if (info.marked > 0) TextButton(onClick = { onReviewMarked(info.matchId, info.markedGame) }) { Text("Review marked") }
                    if (info.isMoney && !info.matchOver) {
                        TextButton(onClick = onEndSession) { Text("End session") }
                    }
                    if (info.matchOver) {
                        TextButton(onClick = { vm.endSession(); onBack() }) { Text("Done") }
                    } else {
                        TextButton(onClick = vm::nextGame) { Text("Next game") }
                    }
                }
            }
        }
    }
}

/** PR, rating, luck and error counts of both players. */
@Composable
internal fun PerformanceRows(you: PerformanceTotals, cpu: PerformanceTotals) {
    SummaryRow("PR", you.pr?.let { fmt(it) } ?: "–", cpu.pr?.let { fmt(it) } ?: "–", strong = true)
    SummaryRow("Rating", you.pr?.let { XgRules.ratingName(it) } ?: "–", cpu.pr?.let { XgRules.ratingName(it) } ?: "–")
    SummaryRow("Luck", eq(you.luck), eq(cpu.luck), strong = true)
    SummaryRow("Luck rating", XgRules.luckRating(you.luck, you.rolls) ?: "–", XgRules.luckRating(cpu.luck, cpu.rolls) ?: "–")
    // the difference, shown on the side of the luckier player
    val net = you.luck - cpu.luck
    val even = kotlin.math.abs(net) < 0.0005
    SummaryRow(
        "Net luck (luckier side)",
        if (even) "even" else if (net > 0) eq(net) else "",
        if (even) "even" else if (net < 0) eq(-net) else "",
        strong = true,
    )
    SummaryRow("Checker errors / blunders", "${you.checkerErrors} / ${you.checkerBlunders}", "${cpu.checkerErrors} / ${cpu.checkerBlunders}")
    SummaryRow(
        "Cube errors / blunders",
        "${you.doubleErrors + you.takeErrors} / ${you.doubleBlunders + you.takeBlunders}",
        "${cpu.doubleErrors + cpu.takeErrors} / ${cpu.doubleBlunders + cpu.takeBlunders}",
    )
}

/** A row of the game summary: label, the human's value, the computer's value. */
@Composable
internal fun SummaryRow(label: String, you: String, cpu: String, header: Boolean = false, strong: Boolean = false) {
    val labelStyle = if (header) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium
    val valueStyle = if (header) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium
    val weight = if (header || strong) FontWeight.SemiBold else FontWeight.Normal
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label, Modifier.weight(1.6f), style = labelStyle,
            color = if (header) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(you, Modifier.weight(1f).padding(start = 8.dp), style = valueStyle, fontWeight = weight, textAlign = TextAlign.End)
        Text(cpu, Modifier.weight(1f).padding(start = 8.dp), style = valueStyle, fontWeight = weight, textAlign = TextAlign.End)
    }
}

/** "40 s", "3 min": a rough duration for the analysis still to come. */
private fun durationText(ms: Long): String {
    val s = (ms + 999) / 1000
    return if (s < 90) "$s s" else "${(s + 30) / 60} min"
}
