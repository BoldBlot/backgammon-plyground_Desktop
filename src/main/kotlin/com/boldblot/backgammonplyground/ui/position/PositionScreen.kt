package com.boldblot.backgammonplyground.ui.position

import com.boldblot.backgammonplyground.ui.components.ChipGapH
import com.boldblot.backgammonplyground.ui.components.ChipGapV
import androidx.compose.material3.OutlinedButton
import com.boldblot.backgammonplyground.showMessage
import com.boldblot.backgammonplyground.ui.shell.BoardAndPanel
import com.boldblot.backgammonplyground.ui.shell.Cmd
import com.boldblot.backgammonplyground.ui.shell.RegisterCommand

import com.boldblot.backgammonplyground.ui.components.desktopVerticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.platform.LocalClipboardManager
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.boldblot.backgammonplyground.core.StudyPosition
import com.boldblot.backgammonplyground.ui.positions.SaveToPositionsDialog
import com.boldblot.backgammonplyground.core.BAR
import com.boldblot.backgammonplyground.core.DecisionKind
import com.boldblot.backgammonplyground.core.Player
import com.boldblot.backgammonplyground.core.XgRules
import com.boldblot.backgammonplyground.ui.board.ArrowColors
import com.boldblot.backgammonplyground.ui.board.BoardColors
import com.boldblot.backgammonplyground.ui.board.BoardView
import com.boldblot.backgammonplyground.ui.board.DiceView
import com.boldblot.backgammonplyground.ui.board.arrowsFor
import com.boldblot.backgammonplyground.ui.board.boardColorsFor
import com.boldblot.backgammonplyground.ui.components.AnalysisLevelPicker
import com.boldblot.backgammonplyground.ui.components.CandidateList
import com.boldblot.backgammonplyground.ui.components.CubeTable

/**
 * Set up any position and analyze it: tap the board to place checkers, choose who is on
 * roll, the dice (none = cube decision), the score and the cube, or import one as an XGID or a
 * GNU Backgammon ID.
 */
@Composable
fun PositionScreen(vm: PositionViewModel) {
    val st by vm.state.collectAsState()
    val capture = com.boldblot.backgammonplyground.ui.review.rememberBoardCapture()
    LaunchedEffect(st.message) {
        val m = st.message ?: return@LaunchedEffect
        showMessage(m)
        vm.dismissMessage()
    }
    val colors = boardColorsFor(st.settings)
    // Results arriving bring their tab forward.
    var tab by remember { mutableStateOf(0) }
    LaunchedEffect(st.checker, st.cubeEval) { if (st.checker != null || st.cubeEval != null) tab = 1 }
    PositionCommands(vm, st, capture)
    BoardAndPanel(
        panelRight = st.settings.railRight,
        board = { m -> EditorBoard(st, vm, colors, m.then(capture.modifier)) },
        under = { BrushRow(st, vm, colors) },
    ) {
        com.boldblot.backgammonplyground.ui.components.PanelTitle("Analyze Position")
        Spacer(Modifier.height(4.dp))
        com.boldblot.backgammonplyground.ui.components.PanelTabs(listOf("Setup", "Analysis"), tab) { tab = it }
        Column(Modifier.weight(1f).desktopVerticalScroll().padding(end = 10.dp)) {
            if (tab == 0) Controls(st, vm) else if (st.checker == null && st.cubeEval == null) {
                Text(
                    if (st.analyzing) "Analyzing…" else "Nothing analyzed yet. Set the position up, then press ${if (st.hasDice) "Best moves" else "Cube decision"}.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else Results(st, vm, colors)
        }
    }
}

/** What the menus and the toolbar can do with the position being set up. */
@Composable
private fun PositionCommands(vm: PositionViewModel, st: PositionState, capture: com.boldblot.backgammonplyground.ui.review.BoardCapture) {
    var importing by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf<StudyPosition?>(null) }
    RegisterCommand(Cmd.UNDO, enabled = st.canUndo) { vm.undo() }
    RegisterCommand(Cmd.COPY_GNU_ID) { com.boldblot.backgammonplyground.ui.review.PositionShare.text(vm.exportId(xgid = false)) }
    RegisterCommand(Cmd.IMPORT_XGID) { importing = true }
    RegisterCommand(Cmd.SAVE_POSITION, enabled = st.problem == null) { saving = vm.study() }
    // The XGID, the board, or the board with the top 5 (analyzed first when there is no analysis yet).
    val unit = com.boldblot.backgammonplyground.ui.components.LocalErrorUnit.current
    val scope = rememberCoroutineScope()
    val away = com.boldblot.backgammonplyground.ui.components.LocalScoreAway.current
    fun shareTitle() = vm.study()?.let { com.boldblot.backgammonplyground.ui.review.PositionShare.title(it, away) } ?: "Analyze Position"
    fun shareLines(s: PositionState) = com.boldblot.backgammonplyground.ui.review.PositionShare.analysisLines(
        s.board, s.onRoll, s.checker, s.cubeEval, response = false, beavers = false,
        playedIndex = null, playedKind = null, factor = XgRules.errorFactor(s.matchLength),
    )
    fun shareLabel(s: PositionState) = (s.checker?.label ?: s.cubeEval?.label)?.ifBlank { null }
    com.boldblot.backgammonplyground.ui.review.ShareCommands(
        xgid = vm.exportId(xgid = true), title = shareTitle(), lines = shareLines(st), capture = capture,
        analysisLabel = shareLabel(st),
        noAnalysis = {
            val job = vm.analyze()
            if (job != null) scope.launch {
                job.join()
                val s = vm.state.value
                val lines = shareLines(s)
                if (lines.isNotEmpty()) com.boldblot.backgammonplyground.ui.review.PositionShare.analysisImage(
                    capture, shareTitle(), lines, vm.exportId(xgid = true), unit, analysisLabel = shareLabel(s),
                )
            }
        },
    )
    saving?.let { SaveToPositionsDialog(vm.app, it, "Analyze Position") { saving = null } }
    if (importing) PositionIdImportDialog(onDismiss = { importing = false }, read = vm::import)
}

/**
 * Import a position as XG or GNU Backgammon write it: an XGID, or a Position ID with its Match ID.
 * [read] takes the text and returns an error message, or null when it was read (the dialog closes).
 */
@Composable
fun PositionIdImportDialog(onDismiss: () -> Unit, read: (String) -> String?) {
    val clipboard = LocalClipboardManager.current
    var text by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    fun import() {
        error = read(text)
        if (error == null) onDismiss()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import a position") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Paste an XGID from eXtreme Gammon, or a GNU Backgammon Position ID with its Match ID.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = text, onValueChange = { text = it; error = null },
                    label = { Text("XGID or GNU ID") }, minLines = 2, maxLines = 4,
                    isError = error != null,
                    supportingText = { Text(error ?: "XGID=-b----E-C---eE---c-e----B-:0:0:1:00:0:0:0:0:10  ·  4HPwATDgc/ABMA:cAkAAAAAAAAA") },
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(onClick = { clipboard.getText()?.text?.let { text = it; error = null } }) {
                    Icon(Icons.Default.ContentPaste, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Paste")
                }
            }
        },
        confirmButton = { TextButton(onClick = ::import, enabled = text.isNotBlank()) { Text("Import") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun EditorBoard(st: PositionState, vm: PositionViewModel, colors: BoardColors, modifier: Modifier) {
    val a = st.checker
    val arrows = if (a != null && a.candidates.isNotEmpty()) {
        val i = st.selected.coerceIn(0, a.candidates.lastIndex)
        val c = a.candidates[i]
        val sev = XgRules.severity((a.bestEquity - c.equity) * XgRules.errorFactor(st.matchLength))
        arrowsFor(st.onRoll, c.move, if (i == 0) ArrowColors.best else ArrowColors.forSeverity(sev))
    } else emptyList()
    BoardView(
        board = st.board, modifier = modifier, colors = colors, mirrored = st.settings.mirrorBoard,
        dice = if (st.hasDice) listOf(DiceView(st.onRoll, listOf(st.dice[0]!!, st.dice[1]!!).sortedDescending())) else emptyList(),
        cube = if (st.isCrawford) com.boldblot.backgammonplyground.core.CubeState() else st.cube,
        pips = listOf(st.board.pipCount(Player.HUMAN), st.board.pipCount(Player.CPU)),
        arrows = arrows,
        onEditTap = { target, slot, barPlayer -> if (target == BAR) vm.tapBar(barPlayer, slot) else vm.tapPoint(target, slot) },
        onEditSwipe = { target, delta, barPlayer -> if (target == BAR) vm.swipeBar(barPlayer, delta) else vm.swipePoint(target, delta) },
    )
}

@Composable
private fun BrushRow(st: PositionState, vm: PositionViewModel, colors: BoardColors) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Click places ", style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow(Modifier.weight(1f)) {
            listOf(Player.HUMAN to "Yours", Player.CPU to "Opponent's").forEachIndexed { i, (p, label) ->
                SegmentedButton(
                    selected = st.brush == p, onClick = { vm.setBrush(p) },
                    shape = SegmentedButtonDefaults.itemShape(i, 2),
                    icon = { Swatch(if (p == Player.HUMAN) colors.human else colors.cpu) },
                ) { Text(label, maxLines = 1) }
            }
        }
    }
    val off = listOf(st.board.off(Player.HUMAN), st.board.off(Player.CPU))
    Text(
        "Click a checker slot to set the count, click the top checker to remove it. Drag along a point towards the " +
            "middle to add checkers, back to remove them. Not on the board = borne off " +
            "(you ${off[0]}, opponent ${off[1]}).",
        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun Swatch(c: Color) {
    Box(Modifier.size(14.dp).background(c, RoundedCornerShape(7.dp)).border(1.dp, Color(0x55000000), RoundedCornerShape(7.dp)))
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun Controls(st: PositionState, vm: PositionViewModel) {
    val clipboard = LocalClipboardManager.current
    Column(Modifier.padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(ChipGapH), verticalArrangement = Arrangement.spacedBy(ChipGapV)) {
            OutlinedButton(onClick = {
                val text = clipboard.getText()?.text
                if (text.isNullOrBlank()) com.boldblot.backgammonplyground.showMessage("The clipboard has no text.")
                else vm.import(text.trim())?.let { com.boldblot.backgammonplyground.showMessage(it) }
            }) { Text("Paste XGID", maxLines = 1) }
            OutlinedButton(onClick = vm::startPosition) { Text("Starting position", maxLines = 1) }
            OutlinedButton(onClick = vm::clear) { Text("Clear board", maxLines = 1) }
            OutlinedButton(onClick = vm::swapSides) { Text("Swap sides", maxLines = 1) }
            OutlinedButton(onClick = vm::undo, enabled = st.canUndo) { Text("Undo", maxLines = 1) }
        }
        Labelled("On roll") {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf(Player.HUMAN to "You", Player.CPU to "Opponent").forEachIndexed { i, (p, label) ->
                    SegmentedButton(selected = st.onRoll == p, onClick = { vm.setOnRoll(p) }, shape = SegmentedButtonDefaults.itemShape(i, 2)) { Text(label) }
                }
            }
        }
        Labelled("Dice") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DiePicker(st.dice[0]) { vm.setDie(0, it) }
                Spacer(Modifier.width(8.dp))
                DiePicker(st.dice[1]) { vm.setDie(1, it) }
                Spacer(Modifier.width(10.dp))
                if (st.dice.any { it != null }) {
                    TextButton(onClick = vm::clearDice) { Text("No dice") }
                } else {
                    Text("None: cube decision", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Labelled("Game") {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MatchPicker(st.matchLength, vm::setMatchLength)
                    Spacer(Modifier.width(8.dp))
                    if (st.matchLength == 0) {
                        Switch(st.jacoby, vm::setJacoby, modifier = Modifier.padding(end = 10.dp))
                        Text("Jacoby")
                    }
                }
                if (st.matchLength > 0) {
                    FlowRow(verticalArrangement = Arrangement.Center, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ScoreStepper("You", st.score[0], st.matchLength) { vm.setScore(Player.HUMAN, it) }
                        ScoreStepper("Opponent", st.score[1], st.matchLength) { vm.setScore(Player.CPU, it) }
                    }
                    if (st.crawfordPossible) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(st.crawford, vm::setCrawford, modifier = Modifier.padding(end = 10.dp))
                            Text("Crawford game")
                        }
                    }
                }
            }
        }
        if (!st.isCrawford) {
            Labelled("Cube") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CubePicker(st.cube.value) { v -> vm.setCube(v, st.cube.owner) }
                    Spacer(Modifier.width(8.dp))
                    if (st.cube.value > 1) {
                        SingleChoiceSegmentedButtonRow(Modifier.weight(1f)) {
                            listOf(Player.HUMAN to "Yours", Player.CPU to "Opponent's").forEachIndexed { i, (p, label) ->
                                SegmentedButton(selected = st.cube.owner == p, onClick = { vm.setCube(st.cube.value, p) },
                                    shape = SegmentedButtonDefaults.itemShape(i, 2)) { Text(label) }
                            }
                        }
                    } else {
                        Text("centred", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Labelled("Analysis") {
            AnalysisLevelPicker(
                engine = st.engine, onEngine = vm::setEngine,
                checker = st.checkerDepth, cubePlies = st.cubePlies, onChange = vm::setLevel,
            )
        }
        Button(onClick = vm::analyze, enabled = !st.analyzing, modifier = Modifier.fillMaxWidth()) {
            if (st.analyzing) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(8.dp))
                Text("Analyzing…")
            } else {
                Text(if (st.hasDice) "Best moves" else "Cube decision")
            }
        }
    }
}

@Composable
private fun Results(st: PositionState, vm: PositionViewModel, colors: BoardColors) {
    val factor = XgRules.errorFactor(st.matchLength)
    val who = if (st.onRoll == Player.HUMAN) "You are" else "Your opponent is"
    st.checker?.takeIf { it.candidates.isNotEmpty() }?.let { a ->
        HorizontalDivider()
        Text("$who on roll · ${listOf(st.dice[0]!!, st.dice[1]!!).sortedDescending().joinToString("")}", style = MaterialTheme.typography.titleSmall)
        CandidateList(
            board = st.board, player = st.onRoll, analysis = a, factor = factor, highlightIndex = null,
            maxRows = 10, selectedIndex = st.selected, detailed = true,
            moverColor = if (st.onRoll == Player.HUMAN) colors.human else colors.cpu,
            opponentColor = if (st.onRoll == Player.HUMAN) colors.cpu else colors.human,
            moverRim = if (st.onRoll == Player.HUMAN) colors.humanRim else colors.cpuRim,
            opponentRim = if (st.onRoll == Player.HUMAN) colors.cpuRim else colors.humanRim,
            onSelect = { i, _ -> vm.select(i) },
        )
    }
    st.cubeEval?.let { e ->
        HorizontalDivider()
        if (st.onRoll == Player.CPU) {
            Text("If your opponent doubles: take or pass?", style = MaterialTheme.typography.titleSmall)
            CubeTable(e, DecisionKind.TAKE, factor, colors = boardColorsFor(st.settings), rollerHuman = false)
            Text("Your opponent's cube decision", style = MaterialTheme.typography.titleSmall)
        } else {
            Text("Your cube decision", style = MaterialTheme.typography.titleSmall)
        }
        CubeTable(e, null, factor, colors = boardColorsFor(st.settings), rollerHuman = st.onRoll == Player.HUMAN)
    }
}

@Composable
private fun Labelled(label: String, content: @Composable () -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        content()
    }
}

@Composable
private fun DiePicker(value: Int?, onPick: (Int?) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Box(
            Modifier.size(44.dp)
                .background(if (value == null) MaterialTheme.colorScheme.surfaceContainerHigh else Color(0xFFF7F3EA), RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .clickable { open = true },
            contentAlignment = Alignment.Center,
        ) {
            Text(value?.toString() ?: "–", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                color = if (value == null) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF222222))
        }
        DropdownMenu(open, onDismissRequest = { open = false }) {
            for (v in 1..6) DropdownMenuItem(text = { Text("$v") }, onClick = { open = false; onPick(v) })
            DropdownMenuItem(text = { Text("No die") }, onClick = { open = false; onPick(null) })
        }
    }
}

@Composable
private fun MatchPicker(length: Int, onPick: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }) { Text(if (length == 0) "Money game" else "$length-point match") }
        DropdownMenu(open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text("Money game") }, onClick = { open = false; onPick(0) })
            for (n in 1..25) DropdownMenuItem(text = { Text("$n-point match") }, onClick = { open = false; onPick(n) })
        }
    }
}

@Composable
private fun CubePicker(value: Int, onPick: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }) { Text(if (value == 1) "Cube 1" else "Cube $value") }
        DropdownMenu(open, onDismissRequest = { open = false }) {
            for (v in listOf(1, 2, 4, 8, 16, 32, 64)) DropdownMenuItem(text = { Text("$v") }, onClick = { open = false; onPick(v) })
        }
    }
}

@Composable
private fun ScoreStepper(label: String, value: Int, length: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        IconButton(onClick = { onChange(value - 1) }, enabled = value > 0) { Icon(Icons.Default.Remove, "Less") }
        Text("$value", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        IconButton(onClick = { onChange(value + 1) }, enabled = value < length - 1) { Icon(Icons.Default.Add, "More") }
        Text("(${length - value} away)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
