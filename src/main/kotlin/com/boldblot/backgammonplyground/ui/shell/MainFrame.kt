package com.boldblot.backgammonplyground.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuScope
import com.boldblot.backgammonplyground.AppLog
import com.boldblot.backgammonplyground.DesktopMessages
import com.boldblot.backgammonplyground.FileDialogs
import com.boldblot.backgammonplyground.data.Settings
import com.boldblot.backgammonplyground.showMessage
import com.boldblot.backgammonplyground.ui.components.ToolButton
import com.boldblot.backgammonplyground.ui.game.GameViewModel
import com.boldblot.backgammonplyground.ui.home.MANUAL_URL
import com.boldblot.backgammonplyground.ui.positions.FolderAnalysis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** The main window's frame: toolbar on top, status bar below, and the board's view between them. */
@Composable
fun MainFrame(shell: ShellState, gameVm: GameViewModel, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        if (shell.toolbarVisible) {
            Toolbar(shell)
            HorizontalDivider()
        }
        Box(Modifier.weight(1f).fillMaxWidth()) { content() }
        HorizontalDivider()
        StatusBar(shell, gameVm)
    }
}

private fun importMatch(shell: ShellState) {
    FileDialogs.open("Import a .mat file")?.let { shell.app.openFile(it) }
}

@Composable
private fun Toolbar(shell: ShellState) {
    val cmds = LocalCommands.current
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            // The row is long: as the window narrows the view's own buttons lose their names first, then the others.
            val compact = maxWidth < 1200.dp
            val compactView = maxWidth < 1550.dp
            Row(Modifier.fillMaxWidth().height(38.dp).padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                ToolButton(Icons.Default.Add, "New game", iconOnly = compact, onClick = shell::newGame)
                ToolButton(Icons.Default.FileOpen, "Import match", iconOnly = compact) { importMatch(shell) }
                ToolButton(Icons.Default.LibraryBooks, "Match Library", iconOnly = compact) { shell.libraryOpen = true }
                ToolButton(Icons.Default.Folder, "My positions", iconOnly = compact) { shell.openPositions() }
                ToolButton(Icons.Default.Bolt, "Quick quiz", iconOnly = compact) { shell.quickQuiz() }
                ToolButton(Icons.Default.BarChart, "Statistics", iconOnly = compact) { shell.statisticsOpen = true }
                ToolButton(Icons.Default.Edit, "Analyze Position", iconOnly = compact) { shell.open("position") }
                Spacer(Modifier.width(14.dp))
                ViewButton(cmds, Cmd.CLOSE_VIEW, Icons.AutoMirrored.Filled.ArrowBack, "Back", compactView)
                ViewButton(cmds, Cmd.HINT, Icons.Default.Lightbulb, "Hint", compactView)
                ViewButton(cmds, Cmd.MARK, com.boldblot.backgammonplyground.ui.components.BlotIcon, "Mark", compactView)
                ViewButton(cmds, Cmd.RESIGN, Icons.Default.Flag, "Resign", compactView)
                ViewButton(cmds, Cmd.BACK_TO_GAME, Icons.Default.History, "Back to game", compactView)
                ViewButton(cmds, Cmd.PREVIOUS, Icons.AutoMirrored.Filled.NavigateBefore, "Previous", compactView)
                ViewButton(cmds, Cmd.NEXT, Icons.AutoMirrored.Filled.NavigateNext, "Next", compactView)
                ViewButton(cmds, Cmd.UNDO, Icons.AutoMirrored.Filled.Undo, "Undo", compactView)
                ViewButton(cmds, Cmd.SAVE_POSITION, Icons.Default.BookmarkAdd, "Save to My positions", compactView)
                Spacer(Modifier.weight(1f))
                ToolButton(Icons.Default.Settings, "Preferences", iconOnly = compact) { shell.openPreferences() }
            }
        }
    }
}

/** A toolbar button only the views that offer [cmd] show. */
@Composable
private fun ViewButton(cmds: CommandBoard, cmd: Cmd, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, compact: Boolean) {
    val c = cmds[cmd] ?: return
    ToolButton(icon, c.label ?: label, enabled = c.enabled, selected = c.checked == true, iconOnly = compact) { c.run() }
}

@Composable
private fun StatusBar(shell: ShellState, gameVm: GameViewModel) {
    var message by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        DesktopMessages.flow.collectLatest {
            message = it
            delay(8000)
            message = null
        }
    }
    val game by gameVm.state.collectAsState()
    val folder by FolderAnalysis.progress.collectAsState()
    val status = LocalStatus.current
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(
            Modifier.fillMaxWidth().height(28.dp).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                message ?: "", style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            status.items.forEach { StatusEntry(it.text, it.progress, it.onStop) }
            folder?.let { r ->
                StatusEntry("Analyzing “${r.folder}” again: ${r.done} of ${r.total}", r.done / r.total.coerceAtLeast(1).toFloat()) { FolderAnalysis.stop() }
            }
            if (game.analysisLeft > 0) {
                val id = game.match?.id ?: game.gameOver?.matchId
                StatusEntry(
                    "Background analysis: ${game.analysisLeft} left", null,
                    id?.let { matchId -> { gameVm.stopAnalysis(matchId) } },
                )
            }
        }
    }
}

@Composable
private fun StatusEntry(text: String, progress: Float?, onStop: (() -> Unit)?) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        progress?.let { p -> LinearProgressIndicator(progress = { p }, modifier = Modifier.width(90.dp)) }
        onStop?.let { stop ->
            TextButton(
                onClick = stop, modifier = Modifier.height(24.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp),
            ) { Text("Stop", style = MaterialTheme.typography.labelMedium) }
        }
    }
}

/** The menu bar. Everything is here; what the view on the board can't do now is greyed out. */
@Composable
fun FrameWindowScope.AppMenuBar(shell: ShellState, gameVm: GameViewModel, onFullScreen: () -> Unit, onQuit: () -> Unit) {
    val app = shell.app
    val cmds = LocalCommands.current
    val s by app.settings.settings.collectAsState(initial = Settings())
    val scope = rememberCoroutineScope()
    fun update(f: (Settings) -> Settings) { scope.launch { app.settings.update(f) } }
    val uri = LocalUriHandler.current
    val game by gameVm.state.collectAsState()
    val hasMatch = game.match != null && game.match?.isOver == false
    MenuBar {
        Menu("File", mnemonic = 'F') {
            Item("New game…", shortcut = KeyShortcut(Key.N, ctrl = true), onClick = shell::newGame)
            Item("Continue match", enabled = hasMatch) { shell.open("game") }
            Separator()
            Item("Import match…", shortcut = KeyShortcut(Key.O, ctrl = true)) { importMatch(shell) }
            Item("Import positions…", shortcut = KeyShortcut(Key.O, ctrl = true, shift = true)) {
                FileDialogs.open("Import positions")?.let { app.openFile(it) }
            }
            Separator()
            CmdItem(cmds, Cmd.SAVE_MATCH, "Save match as .mat…", KeyShortcut(Key.S, ctrl = true))
            CmdItem(cmds, Cmd.SAVE_GAME, "Save game as .mat…")
            Item("Export matches…") { shell.exportMatches = true; shell.libraryOpen = true }
            Item("Export positions…") { shell.exportPositions = true; shell.positionsOpen = true }
            Separator()
            CmdItem(cmds, Cmd.SAVE_BOARD, "Save board picture…")
            CmdItem(cmds, Cmd.SAVE_TOP5, "Save board with top 5…")
            Separator()
            Item("Quit", shortcut = KeyShortcut(Key.Q, ctrl = true), onClick = onQuit)
        }
        Menu("Edit", mnemonic = 'E') {
            CmdItem(cmds, Cmd.UNDO, "Undo move", KeyShortcut(Key.Z, ctrl = true))
            Separator()
            CmdItem(cmds, Cmd.COPY_XGID, "Copy XGID", KeyShortcut(Key.C, ctrl = true, shift = true))
            CmdItem(cmds, Cmd.COPY_GNU_ID, "Copy GNU ID")
            // A text box that has the focus takes Ctrl+V itself; everywhere else it opens the position in Analyze Position.
            Item("Paste XGID", shortcut = KeyShortcut(Key.V, ctrl = true)) {
                val text = runCatching {
                    java.awt.Toolkit.getDefaultToolkit().systemClipboard.getData(java.awt.datatransfer.DataFlavor.stringFlavor) as String
                }.getOrNull()
                if (text.isNullOrBlank()) showMessage("The clipboard has no text.") else app.pasteXgid.tryEmit(text.trim())
            }
            CmdItem(cmds, Cmd.IMPORT_XGID, "Import XGID…")
            Separator()
            Item("Preferences…", shortcut = KeyShortcut(Key.Comma, ctrl = true)) { shell.openPreferences() }
        }
        Menu("View", mnemonic = 'V') {
            CheckboxItem("Side panel", shell.panelVisible, shortcut = KeyShortcut(Key.F9)) { shell.togglePanel() }
            CheckboxItem("Toolbar", shell.toolbarVisible) { shell.toggleToolbar() }
            CheckboxItem("Side panel on the right", s.railRight) { v -> update { it.copy(railRight = v) } }
            Separator()
            CheckboxItem("Show pip counts", s.showPips) { v -> update { it.copy(showPips = v) } }
            CheckboxItem("Match score as away", s.scoreAway) { v -> update { it.copy(scoreAway = v) } }
            CheckboxItem("Crawford and DMP sign", s.crawfordSign) { v -> update { it.copy(crawfordSign = v) } }
            CheckboxItem("Arrows to past positions", s.pastPositions) { v -> update { it.copy(pastPositions = v) } }
            Separator()
            Item("Full screen", shortcut = KeyShortcut(Key.F11), onClick = onFullScreen)
        }
        Menu("Game", mnemonic = 'G') {
            CmdItem(cmds, Cmd.CLOSE_VIEW, "Back")
            Separator()
            CmdItem(cmds, Cmd.ROLL_DONE, "Roll", KeyShortcut(Key.Enter))
            CmdItem(cmds, Cmd.DOUBLE, "Double", KeyShortcut(Key.D))
            CmdItem(cmds, Cmd.HINT, "Hint", KeyShortcut(Key.H))
            CmdCheck(cmds, Cmd.MARK, "Mark this position", KeyShortcut(Key.M))
            Separator()
            CmdCheck(cmds, Cmd.MAX_BEAROFF, "Max Bearoff")
            CmdCheck(cmds, Cmd.KEEP_ROLLING, "Keep rolling")
            CmdItem(cmds, Cmd.BACK_TO_GAME, "Back to game")
            CmdItem(cmds, Cmd.AUTOFINISH, "Autofinish game")
            Separator()
            CmdItem(cmds, Cmd.RESIGN, "Resign…")
            CmdItem(cmds, Cmd.NEXT_GAME, "Next game")
            CmdItem(cmds, Cmd.LAST_SUMMARY, "Last game's summary")
            CmdItem(cmds, Cmd.TOGGLE_STATS, "Exclude from statistics")
            CmdItem(cmds, Cmd.ABANDON, "Abandon match")
        }
        Menu("Analyze", mnemonic = 'A') {
            Item("Analyze Position", shortcut = KeyShortcut(Key.E, ctrl = true)) { shell.open("position") }
            CmdItem(cmds, Cmd.ANALYZE_NOW, "Analyze now")
            CmdItem(cmds, Cmd.ANALYZE_AGAIN, "Analyze again…")
            CmdItem(cmds, Cmd.STOP_ANALYSIS, "Stop analysis")
            Separator()
            CmdItem(cmds, Cmd.REVIEW_MATCH, "Review match")
            CmdItem(cmds, Cmd.REVIEW_MISTAKES, "Review my mistakes")
            CmdItem(cmds, Cmd.REVIEW_MARKED, "Review marked")
            CmdItem(cmds, Cmd.PREVIOUS, "Previous decision", KeyShortcut(Key.DirectionLeft))
            CmdItem(cmds, Cmd.NEXT, "Next decision", KeyShortcut(Key.DirectionRight))
            CmdItem(cmds, Cmd.SUMMARY, "Summary")
            Separator()
            CmdItem(cmds, Cmd.SAVE_POSITION, "Save to My positions…")
            Item("Engine speed test…") { shell.speedTestOpen = true }
        }
        Menu("Study", mnemonic = 'S') {
            Item("Match Library", shortcut = KeyShortcut(Key.One, ctrl = true)) { shell.libraryOpen = true }
            Item("My positions", shortcut = KeyShortcut(Key.Two, ctrl = true)) { shell.openPositions() }
            Item("Statistics", shortcut = KeyShortcut(Key.Three, ctrl = true)) { shell.statisticsOpen = true }
            Separator()
            Item("New errors") { shell.openPositions(PositionsPane.Errors) }
            Item("New Marked") { shell.openPositions(PositionsPane.Marked) }
            Item("Learned") { shell.openPositions(PositionsPane.Learned) }
            Separator()
            Item("Quiz…") { shell.quizSetup = QuizSetup() }
            Item("Quick quiz") { shell.quickQuiz() }
        }
        Menu("Help", mnemonic = 'H') {
            Item("Manual (boldblot.com)", shortcut = KeyShortcut(Key.F1)) { uri.openUri(MANUAL_URL) }
            Item("Welcome screen") { shell.welcome = false }
            Item("Send error report") { AppLog.sendReport().let { showMessage("Log saved to ${it.path}") } }
            Separator()
            Item("About") { shell.aboutOpen = true }
        }
    }
}

/** A menu item for [cmd]: greyed out unless the view on the board offers it now. */
@Composable
private fun MenuScope.CmdItem(cmds: CommandBoard, cmd: Cmd, text: String, shortcut: KeyShortcut? = null) {
    val c = cmds[cmd]
    Item(c?.label ?: text, enabled = c?.enabled == true, shortcut = shortcut) { c?.run() }
}

/** The same for a command that is on or off. */
@Composable
private fun MenuScope.CmdCheck(cmds: CommandBoard, cmd: Cmd, text: String, shortcut: KeyShortcut? = null) {
    val c = cmds[cmd]
    CheckboxItem(c?.label ?: text, c?.checked == true, enabled = c?.enabled == true, shortcut = shortcut) { c?.run() }
}
