package com.boldblot.backgammonplyground.ui.shell

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import com.boldblot.backgammonplyground.EscapeStack
import com.boldblot.backgammonplyground.LocalEscape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boldblot.backgammonplyground.BackHandler
import com.boldblot.backgammonplyground.WindowPrefs
import com.boldblot.backgammonplyground.generated.resources.Res
import com.boldblot.backgammonplyground.generated.resources.plyground_logo
import com.boldblot.backgammonplyground.ui.game.GameViewModel
import com.boldblot.backgammonplyground.ui.home.AboutScreen
import com.boldblot.backgammonplyground.ui.home.HistoryScreen
import com.boldblot.backgammonplyground.ui.home.PreferencesContent
import com.boldblot.backgammonplyground.ui.home.SpeedTestScreen
import com.boldblot.backgammonplyground.ui.home.WelcomeScreen
import com.boldblot.backgammonplyground.ui.home.speedTestLine
import com.boldblot.backgammonplyground.ui.positions.PositionsBrowser
import com.boldblot.backgammonplyground.ui.positions.QuizMenuScreen
import com.boldblot.backgammonplyground.ui.stats.StatsScreen
import com.boldblot.backgammonplyground.ui.stats.StatsViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

private fun EscapeStack.handleKey(e: KeyEvent): Boolean = e.type == KeyEventType.KeyDown && e.key == Key.Escape && handle()

/** A window that stays open beside the board and remembers its size and place under [name]. */
@Composable
private fun SeparateWindow(name: String, title: String, width: Int, height: Int, onClose: () -> Unit, content: @Composable FrameWindowScope.() -> Unit) {
    val saved = remember { WindowPrefs.load(name, 520, 400) }
    val state = rememberWindowState(
        size = DpSize((saved?.width ?: width).dp, (saved?.height ?: height).dp),
        position = if (saved?.x != null && saved.y != null) WindowPosition(saved.x.dp, saved.y!!.dp) else WindowPosition.PlatformDefault,
    )
    // Also when the app quits with this window open.
    DisposableEffect(Unit) {
        onDispose {
            val p = state.position
            WindowPrefs.save(
                WindowPrefs.Saved(
                    state.size.width.value.toInt(), state.size.height.value.toInt(),
                    p.takeIf { it.isSpecified }?.x?.value?.toInt(), p.takeIf { it.isSpecified }?.y?.value?.toInt(), false,
                ),
                name,
            )
        }
    }
    val escape = remember { EscapeStack() }
    Window(onCloseRequest = onClose, state = state, title = title, icon = painterResource(Res.drawable.plyground_logo), onKeyEvent = { e ->
        // Ctrl+W closes the window. Esc only clears a selection here: an open menu also takes Esc, and the window can't tell.
        if (e.type == KeyEventType.KeyDown && e.isCtrlPressed && e.key == Key.W) { onClose(); true } else escape.handleKey(e)
    }) {
        window.minimumSize = java.awt.Dimension(520, 400)
        CompositionLocalProvider(LocalEscape provides escape) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { content() }
        }
    }
}

/**
 * A dialog in a window of its own. It sits beside the main window in the composition but is, for the system, the child of the window
 * it was opened from, so it comes up in front of that one and holds it until answered; the other windows stay usable.
 */
@Composable
private fun AppDialog(title: String, width: Int, height: Int, onClose: () -> Unit, content: @Composable () -> Unit) {
    val main = LocalShell.current.mainWindow ?: return
    val owner = remember { java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow ?: main }
    val escape = remember { EscapeStack() }
    val close = rememberUpdatedState(onClose)
    DialogWindow(
        onKeyEvent = { escape.handleKey(it) },
        create = {
            ComposeDialog(owner, java.awt.Dialog.ModalityType.DOCUMENT_MODAL).apply {
                this.title = title
                setSize(width, height)
                setLocationRelativeTo(owner)
                isResizable = true
                defaultCloseOperation = javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE
                addWindowListener(object : java.awt.event.WindowAdapter() {
                    override fun windowClosing(e: java.awt.event.WindowEvent) = close.value()
                })
            }
        },
        dispose = { it.dispose() },
    ) {
        CompositionLocalProvider(LocalEscape provides escape) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                BackHandler(onBack = onClose)
                content()
            }
        }
    }
}

/** Every window and dialog the shell can have open, beside the main window in the composition. */
@Composable
fun ShellWindows(shell: ShellState, gameVm: GameViewModel) {
    val app = shell.app
    if (shell.libraryOpen) {
        SeparateWindow("library", "Match Library", 940, 640, onClose = { shell.libraryOpen = false }) {
            val progress by gameVm.analysisProgress.collectAsState()
            HistoryScreen(app, shell, onImported = gameVm::queueAnalysis, analysisProgress = progress)
        }
    }
    if (shell.positionsOpen) {
        SeparateWindow("positions", "My positions", 1060, 720, onClose = { shell.positionsOpen = false }) {
            PositionsBrowser(app, shell)
        }
    }
    if (shell.statisticsOpen) {
        SeparateWindow("statistics", "Statistics", 780, 760, onClose = { shell.statisticsOpen = false }) {
            val vm: StatsViewModel = viewModel { StatsViewModel(app) }
            StatsScreen(vm)
        }
    }
    if (shell.preferencesOpen) {
        AppDialog("Preferences", 880, 660, onClose = { shell.preferencesOpen = false }) {
            PreferencesContent(app, shell)
        }
    }
    shell.quizSetup?.let { setup ->
        AppDialog("Quiz", 620, 680, onClose = { shell.quizSetup = null }) {
            QuizMenuScreen(app, setup.folders, preFilter = setup.filter) { spec ->
                shell.quizSetup = null
                shell.open("positions/quiz/${spec.encode()}")
            }
        }
    }
    if (shell.aboutOpen) {
        AppDialog("About", 620, 680, onClose = { shell.aboutOpen = false }) {
            val settings by app.settings.settings.collectAsState(initial = null)
            AboutScreen(
                speedTest = settings?.let { speedTestLine(it) },
                onSpeedTest = { shell.speedTestOpen = true },
                onWelcome = { shell.welcome = false },
            )
        }
    }
    if (shell.speedTestOpen) {
        AppDialog("Engine speed test", 680, 620, onClose = { shell.speedTestOpen = false }) {
            SpeedTestScreen(app)
        }
    }
    shell.welcome?.let { newInstall ->
        val seen = {
            shell.welcome = null
            app.appScope.launch { app.settings.update { it.copy(welcomeSeen = com.boldblot.backgammonplyground.BuildConfig.VERSION_NAME) } }
            Unit
        }
        AppDialog("Welcome", 720, 800, onClose = seen) {
            WelcomeScreen(
                app, newInstall,
                onSpeedTest = { shell.welcome = null; shell.speedTestOpen = true },
                onDone = { shell.welcome = null },
            )
        }
    }
}
