package com.boldblot.backgammonplyground.ui.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/** What the view on the main board offers to the menu bar and the toolbar. A command nobody registers is greyed out. */
enum class Cmd {
    // Game
    CLOSE_VIEW, ROLL_DONE, DOUBLE, HINT, MARK, MAX_BEAROFF, KEEP_ROLLING, BACK_TO_GAME, AUTOFINISH, RESIGN, NEXT_GAME, LAST_SUMMARY, TOGGLE_STATS, ABANDON,
    // Analyze
    ANALYZE_NOW, ANALYZE_AGAIN, STOP_ANALYSIS, REVIEW_MATCH, REVIEW_MISTAKES, REVIEW_MARKED, PREVIOUS, NEXT, SUMMARY, SAVE_POSITION,
    // File
    SAVE_MATCH, SAVE_GAME, SAVE_BOARD, SAVE_TOP5,
    // Edit
    UNDO, COPY_XGID, COPY_GNU_ID, IMPORT_XGID,
}

/** One view's offer of a [Cmd]: what it does, whether it can run now, and its label if that changes ("Roll" or "Done"). */
class Command(private val action: State<() -> Unit>) {
    var enabled by mutableStateOf(true)
    var label by mutableStateOf<String?>(null)
    var checked by mutableStateOf<Boolean?>(null)
    fun run() = action.value()
}

/** The commands on offer now. A view leaving the composition takes its own away; the newest offer of a [Cmd] wins. */
class CommandBoard {
    private val entries = mutableStateListOf<Pair<Cmd, Command>>()

    fun add(cmd: Cmd, c: Command) { entries.add(cmd to c) }
    fun remove(cmd: Cmd, c: Command) { entries.removeAll { it.first == cmd && it.second === c } }
    operator fun get(cmd: Cmd): Command? = entries.lastOrNull { it.first == cmd }?.second
    fun enabled(cmd: Cmd): Boolean = get(cmd)?.enabled == true
    fun run(cmd: Cmd) { get(cmd)?.takeIf { it.enabled }?.run() }
}

val LocalCommands = staticCompositionLocalOf { CommandBoard() }

/** Offers [cmd] while this is in the composition. */
@Composable
fun RegisterCommand(cmd: Cmd, enabled: Boolean = true, label: String? = null, checked: Boolean? = null, action: () -> Unit) {
    val board = LocalCommands.current
    val current = rememberUpdatedState(action)
    val entry = remember(cmd) { Command(current) }
    SideEffect {
        entry.enabled = enabled
        entry.label = label
        entry.checked = checked
    }
    DisposableEffect(board, cmd) {
        board.add(cmd, entry)
        onDispose { board.remove(cmd, entry) }
    }
}

/** Something long-running shown in the status bar, with its progress (0 to 1) and a Stop button if it has them. */
class StatusItem {
    var text by mutableStateOf("")
    var progress by mutableStateOf<Float?>(null)
    var onStop by mutableStateOf<(() -> Unit)?>(null)
}

class StatusBoard {
    val items = mutableStateListOf<StatusItem>()
}

val LocalStatus = staticCompositionLocalOf { StatusBoard() }

/** Shows [text] in the status bar while this is in the composition and [text] isn't null. */
@Composable
fun RegisterStatus(text: String?, progress: Float? = null, onStop: (() -> Unit)? = null) {
    val board = LocalStatus.current
    val item = remember { StatusItem() }
    SideEffect {
        item.text = text.orEmpty()
        item.progress = progress
        item.onStop = onStop
    }
    DisposableEffect(board, text != null) {
        if (text != null) board.items.add(item)
        onDispose { board.items.remove(item) }
    }
}
