package com.boldblot.backgammonplyground

import com.boldblot.backgammonplyground.core.EngineKind
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.boldblot.backgammonplyground.ui.game.GameScreen
import com.boldblot.backgammonplyground.ui.game.GameViewModel
import com.boldblot.backgammonplyground.ui.position.PositionScreen
import com.boldblot.backgammonplyground.ui.position.PositionViewModel
import com.boldblot.backgammonplyground.ui.positions.PositionDetailScreen
import com.boldblot.backgammonplyground.ui.positions.QuizScreen
import com.boldblot.backgammonplyground.ui.positions.QuizSpec
import com.boldblot.backgammonplyground.ui.positions.QuizViewModel
import com.boldblot.backgammonplyground.ui.review.BlunderScreen
import com.boldblot.backgammonplyground.ui.review.BlunderViewModel
import com.boldblot.backgammonplyground.ui.review.ReviewScreen
import com.boldblot.backgammonplyground.ui.review.ReviewViewModel
import com.boldblot.backgammonplyground.ui.shell.AppMenuBar
import com.boldblot.backgammonplyground.ui.shell.Cmd
import com.boldblot.backgammonplyground.ui.shell.RegisterCommand
import com.boldblot.backgammonplyground.ui.shell.backLabel
import com.boldblot.backgammonplyground.ui.shell.CommandBoard
import com.boldblot.backgammonplyground.ui.shell.LocalCommands
import com.boldblot.backgammonplyground.ui.shell.LocalShell
import com.boldblot.backgammonplyground.ui.shell.LocalStatus
import com.boldblot.backgammonplyground.ui.shell.MainFrame
import com.boldblot.backgammonplyground.ui.shell.START_ROUTE
import com.boldblot.backgammonplyground.ui.shell.ShellState
import com.boldblot.backgammonplyground.ui.shell.ShellWindows
import com.boldblot.backgammonplyground.ui.shell.StartScreen
import com.boldblot.backgammonplyground.ui.shell.StatusBoard
import com.boldblot.backgammonplyground.ui.theme.GammonTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.unit.isSpecified
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.jetbrains.compose.resources.painterResource
import com.boldblot.backgammonplyground.generated.resources.Res
import com.boldblot.backgammonplyground.generated.resources.plyground_logo

fun main(args: Array<String>) {
    // A second copy hands its files to the first and quits: two would fight over the database.
    val pending = java.util.concurrent.ConcurrentLinkedQueue<String>()
    val raise = java.util.concurrent.atomic.AtomicInteger(0)
    if (!SingleInstance.acquire { paths -> pending.addAll(paths.filter { it.isNotBlank() }); raise.incrementAndGet() }) {
        if (SingleInstance.send(args.toList())) return
    }
    // Whether this is the first run, before anything creates the data folder's files.
    val newInstall = !DataDirs.file("settings.preferences_pb").exists()
    // The menu bar is Swing's: the same smallest text size as the rest.
    for (key in listOf("MenuBar.font", "Menu.font", "MenuItem.font", "CheckBoxMenuItem.font", "MenuItem.acceleratorFont", "CheckBoxMenuItem.acceleratorFont", "Menu.acceleratorFont")) {
        javax.swing.UIManager.getFont(key)?.let { javax.swing.UIManager.put(key, it.deriveFont(14f)) }
    }
    // An unavailable menu item is plainly grey, also in a menu where every item is.
    for (key in listOf("Menu.disabledForeground", "MenuItem.disabledForeground", "CheckBoxMenuItem.disabledForeground")) {
        javax.swing.UIManager.put(key, javax.swing.plaf.ColorUIResource(0xB0, 0xB0, 0xB0))
    }
    val app = PlyGroundApp()
    pending.addAll(args)
    val crash = MutableStateFlow<Throwable?>(null)
    AppLog.onCrash = { e -> crash.compareAndSet(null, e) }
    application {
        val saved = androidx.compose.runtime.remember { WindowPrefs.load() }
        val state = rememberWindowState(
            size = DpSize((saved?.width ?: 1280).dp, (saved?.height ?: 820).dp),
            position = if (saved?.x != null && saved.y != null) WindowPosition(saved.x.dp, saved.y!!.dp) else WindowPosition.PlatformDefault,
        )
        val owner = androidx.compose.runtime.remember { object : ViewModelStoreOwner { override val viewModelStore = ViewModelStore() } }
        // The size the window settles at after opening differs a few pixels from the saved one (the menu bar and the
        // frame take some), so saving it back every time would shrink the window each run: keep the saved size
        // unless the user resized the window since it settled.
        var settled: DpSize? = null
        fun quit() {
            val p = state.position
            val keepSaved = saved != null && settled != null && state.size == settled
            if (state.placement != WindowPlacement.Fullscreen) {
                WindowPrefs.save(WindowPrefs.Saved(
                    if (keepSaved) saved!!.width else state.size.width.value.toInt(), if (keepSaved) saved!!.height else state.size.height.value.toInt(),
                    p.takeIf { it.isSpecified }?.x?.value?.toInt(), p.takeIf { it.isSpecified }?.y?.value?.toInt(),
                    state.placement == WindowPlacement.Maximized,
                ))
            }
            owner.viewModelStore.clear()
            app.shutdown()
            exitApplication()
        }
        fun toggleFullScreen() {
            state.placement = if (state.placement == WindowPlacement.Fullscreen) WindowPlacement.Floating else WindowPlacement.Fullscreen
        }
        val shell = androidx.compose.runtime.remember { ShellState(app) }
        val commands = androidx.compose.runtime.remember { CommandBoard() }
        val status = androidx.compose.runtime.remember { StatusBoard() }
        // Errors in Eq or mEq everywhere (Preferences → Display).
        val errorUnit by androidx.compose.runtime.remember { app.settings.settings.map { it.errorUnit } }.collectAsState(initial = com.boldblot.backgammonplyground.data.ErrorUnit.EQ)
        val checkerStyle by androidx.compose.runtime.remember { app.settings.settings.map { it.checkerStyle } }.collectAsState(initial = com.boldblot.backgammonplyground.data.CheckerStyle.RIMMED)
        val scoreAway by androidx.compose.runtime.remember { app.settings.settings.map { it.scoreAway } }.collectAsState(initial = false)
        val crashed by crash.collectAsState()
        GammonTheme {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides owner, LocalApp provides app,
                LocalShell provides shell, LocalCommands provides commands, LocalStatus provides status,
                com.boldblot.backgammonplyground.ui.components.LocalScoreAway provides scoreAway,
                com.boldblot.backgammonplyground.ui.components.LocalErrorUnit provides errorUnit,
                com.boldblot.backgammonplyground.ui.components.LocalCheckerStyle provides checkerStyle,
                // A mouse needs no touch-sized targets.
                androidx.compose.material3.LocalMinimumInteractiveComponentSize provides 24.dp,
            ) {
                val gameVm: GameViewModel = viewModel { GameViewModel(app) }
                Window(
                    onCloseRequest = ::quit, state = state, title = "Backgammon PlyGround ${BuildConfig.VERSION_NAME}",
                    icon = painterResource(Res.drawable.plyground_logo),
                    onPreviewKeyEvent = { e ->
                        e.type == KeyEventType.KeyDown && !e.isCtrlPressed && !e.isAltPressed && !e.isMetaPressed && KeyActions.handle(e)
                    },
                ) {
                    window.minimumSize = java.awt.Dimension(WindowPrefs.MIN_WIDTH, WindowPrefs.MIN_HEIGHT)
                    androidx.compose.runtime.SideEffect { shell.mainWindow = window }
                    LaunchedEffect(Unit) { kotlinx.coroutines.delay(2000); settled = state.size }
                    if (saved?.maximized == true) androidx.compose.runtime.LaunchedEffect(Unit) { state.placement = WindowPlacement.Maximized }
                    // Files from the command line, from another copy, or dropped on the window.
                    androidx.compose.runtime.DisposableEffect(Unit) {
                        val target = java.awt.dnd.DropTarget(window, java.awt.dnd.DnDConstants.ACTION_COPY, object : java.awt.dnd.DropTargetAdapter() {
                            override fun drop(e: java.awt.dnd.DropTargetDropEvent) {
                                e.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY)
                                @Suppress("UNCHECKED_CAST")
                                val files = runCatching { e.transferable.getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor) as List<java.io.File> }.getOrNull()
                                files?.forEach { pending.add(it.path) }
                                e.dropComplete(files != null)
                                raise.incrementAndGet()
                            }
                        })
                        onDispose { target.removeNotify() }
                    }
                    val wakeups by androidx.compose.runtime.remember { kotlinx.coroutines.flow.flow { while (true) { emit(raise.get()); kotlinx.coroutines.delay(300) } } }.collectAsState(initial = 0)
                    LaunchedEffect(wakeups) {
                        while (true) { val path = pending.poll() ?: break; app.openFile(java.io.File(path)) }
                        if (wakeups > 0) { window.toFront(); window.requestFocus() }
                    }
                    AppMenuBar(shell, gameVm, onFullScreen = ::toggleFullScreen, onQuit = ::quit)
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        MainFrame(shell, gameVm) { AppNav(app, shell, gameVm, newInstall) }
                        crashed?.let { e ->
                            androidx.compose.material3.AlertDialog(
                                onDismissRequest = { crash.value = null },
                                title = { Text("Something went wrong") },
                                text = { Text("${e.javaClass.simpleName}: ${e.message ?: "no message"}\n\nIt was written to the log. The app may still work; if it doesn't, restart it. Send the log to help fix it.") },
                                confirmButton = { androidx.compose.material3.TextButton(onClick = { crash.value = null; AppLog.sendReport().let { showMessage("Log saved to ${it.path}") } }) { Text("Send error report") } },
                                dismissButton = { androidx.compose.material3.TextButton(onClick = { crash.value = null }) { Text("Dismiss") } },
                            )
                        }
                    }
                }
                ShellWindows(shell, gameVm)
            }
        }
    }
}

/** What shows on the main board. Every route here is one view of the same window: board and side panel. */
@androidx.compose.runtime.Composable
fun AppNav(app: PlyGroundApp, shell: ShellState, gameVm: GameViewModel, newInstall: Boolean) {
    val positionVm: PositionViewModel = viewModel { PositionViewModel(app) }
    val nav = rememberNavController()
    // Requests from the menu bar, the toolbar and the separate windows.
    LaunchedEffect(Unit) {
        app.navRequests.collect { route ->
            when (route) {
                START_ROUTE -> nav.popBackStack(START_ROUTE, inclusive = false)
                "game" -> nav.navigate("game") { popUpTo(START_ROUTE); launchSingleTop = true }
                else -> nav.navigate(route) { launchSingleTop = true }
            }
        }
    }
    LaunchedEffect(Unit) {
        app.pasteXgid.collect { text ->
            val problem = positionVm.import(text)
            if (problem != null) showMessage(problem)
            else if (nav.currentDestination?.route != "position") nav.navigate("position")
        }
    }
    val gameState by gameVm.state.collectAsState()
    val hasMatch = gameState.match != null && gameState.match?.isOver == false
    // A positions file to import opens My positions, a match file the Match Library: each shows the import dialog.
    val pendingImport by app.pendingImport.collectAsState()
    LaunchedEffect(pendingImport != null) { if (pendingImport != null) shell.positionsOpen = true }
    val pendingMatch by app.pendingMatch.collectAsState()
    LaunchedEffect(pendingMatch != null) { if (pendingMatch != null) shell.libraryOpen = true }

    // The welcome dialog: only when it has never been shown (a new install, or the app's data cleared).
    LaunchedEffect(Unit) {
        if (app.settings.settings.first().welcomeSeen.isEmpty()) shell.welcome = newInstall
    }

    val none = androidx.compose.animation.EnterTransition.None
    val noExit = androidx.compose.animation.ExitTransition.None
    NavHost(
        nav, startDestination = START_ROUTE,
        enterTransition = { none }, exitTransition = { noExit }, popEnterTransition = { none }, popExitTransition = { noExit },
    ) {
        composable(START_ROUTE) {
            StartScreen(app, gameVm, hasMatch, onContinue = { nav.navigate("game") { popUpTo(START_ROUTE); launchSingleTop = true } }) { rules, level, mistakes, engine ->
                gameVm.newMatch(rules, level, mistakes, engine)
                nav.navigate("game") { popUpTo(START_ROUTE); launchSingleTop = true }
            }
        }
        composable("game") {
            GameScreen(
                gameVm, onBack = { nav.popBackStack(START_ROUTE, inclusive = false) },
                onReview = { id -> nav.navigate("review/$id?marks=true") },
                onReviewMistakes = { id, game -> nav.navigate("review/$id?game=$game&mistakes=true&marks=true") },
                onReviewMarked = { id, game -> nav.navigate("review/$id?game=$game&marks=true&markedOnly=true") },
            )
        }
        composable(
            "review/{id}?game={game}&mistakes={mistakes}&marks={marks}&markedOnly={markedOnly}",
            arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("game") { type = NavType.IntType; defaultValue = 0 },
                navArgument("mistakes") { type = NavType.BoolType; defaultValue = false },
                navArgument("marks") { type = NavType.BoolType; defaultValue = false },
                navArgument("markedOnly") { type = NavType.BoolType; defaultValue = false },
            ),
        ) { entry ->
            val id = entry.arguments?.read { getLong("id") } ?: 0L
            val game = entry.arguments?.read { getInt("game") }?.takeIf { it > 0 }
            val mistakes = entry.arguments?.read { getBoolean("mistakes") } ?: false
            val marks = entry.arguments?.read { getBoolean("marks") } ?: false
            val markedOnly = entry.arguments?.read { getBoolean("markedOnly") } ?: false
            val vm: ReviewViewModel = viewModel(key = "review-$id-$game-$mistakes-$marks-$markedOnly") { ReviewViewModel(app, id, game, mistakes, marks, markedOnly) }
            ReviewScreen(vm, onBack = { nav.popBackStack() })
        }
        composable(
            "blunder/{id}?marked={marked}",
            arguments = listOf(navArgument("id") { type = NavType.LongType }, navArgument("marked") { type = NavType.BoolType; defaultValue = false }),
        ) { entry ->
            val id = entry.arguments?.read { getLong("id") } ?: 0L
            val marked = entry.arguments?.read { getBoolean("marked") } ?: false
            val vm: BlunderViewModel = viewModel(key = "blunder-$id-$marked") { BlunderViewModel(app, id, marked) }
            BlunderScreen(vm, onBack = { nav.popBackStack() })
        }
        composable("position") {
            PositionScreen(positionVm)
        }
        composable("positions/item/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            PositionDetailScreen(app, entry.arguments?.read { getLong("id") } ?: 0L, onBack = { nav.popBackStack() })
        }
        composable("positions/quiz/{spec}") { entry ->
            val key = entry.arguments?.read { getString("spec") }.orEmpty()
            val vm: QuizViewModel = viewModel(key = "quiz-$key") { QuizViewModel(app, QuizSpec.decode(key)) }
            QuizScreen(vm, onBack = { nav.popBackStack() }, onOpen = { p -> nav.navigate("positions/item/$p") })
        }
    }
    // Esc in a game stays in the game: it cancels what is pending there, and the match isn't left by accident.
    val entry by nav.currentBackStackEntryAsState()
    com.boldblot.backgammonplyground.BackHandler(enabled = entry?.destination?.route == "game") {}
    // Every view opened over another one has a button back to it, in the side panel and on the toolbar.
    val route = entry?.destination?.route
    if (route != null && route != START_ROUTE && route != "game") {
        RegisterCommand(Cmd.CLOSE_VIEW, label = backLabel(nav.previousBackStackEntry?.destination?.route)) { nav.popBackStack() }
    }
}
