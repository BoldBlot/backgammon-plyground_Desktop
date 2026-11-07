package com.boldblot.backgammonplyground

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberUpdatedState

/** True when the window is wider than tall: the landscape layouts. Set from the window's size in Main.kt. */
val LocalLandscape = compositionLocalOf { true }

/**
 * Esc in a separate window or dialog: the handlers of that window, innermost last. The main window uses Compose's own back
 * handling, which doesn't reach the other windows.
 */
class EscapeStack {
    private val stack = ArrayList<() -> Unit>()
    fun add(h: () -> Unit) { stack.add(h) }
    fun remove(h: () -> Unit) { stack.remove(h) }
    /** Runs the innermost handler; true if there was one. */
    fun handle(): Boolean = stack.lastOrNull()?.let { it(); true } ?: false
}

val LocalEscape = compositionLocalOf<EscapeStack?> { null }

/**
 * Esc / back. The innermost enabled handler runs first, and in the main window the navigation's pop of the view runs last, so
 * Esc cancels a prompt before it leaves the view.
 */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    val escape = LocalEscape.current
    if (escape == null) {
        androidx.compose.ui.backhandler.BackHandler(enabled = enabled, onBack = onBack)
    } else {
        val current = rememberUpdatedState(onBack)
        DisposableEffect(escape, enabled) {
            val h: () -> Unit = { current.value() }
            if (enabled) escape.add(h)
            onDispose { escape.remove(h) }
        }
    }
}

/** Keyboard shortcuts of the screen on show: the innermost enabled handler gets a key first (see [KeyHandler]). */
object KeyActions {
    private val stack = ArrayList<(androidx.compose.ui.input.key.KeyEvent) -> Boolean>()

    fun add(h: (androidx.compose.ui.input.key.KeyEvent) -> Boolean) { stack.add(h) }
    fun remove(h: (androidx.compose.ui.input.key.KeyEvent) -> Boolean) { stack.remove(h) }

    /** Offers a key press to the handlers, innermost first; true if one used it. */
    fun handle(e: androidx.compose.ui.input.key.KeyEvent): Boolean = stack.asReversed().any { it(e) }
}

/**
 * Registers [onKey] for key presses while this is in the composition and [enabled]. It returns true for the keys it uses.
 * Dialogs are their own windows, so while one is open the main window gets no keys. Only for screens without text fields.
 */
@Composable
fun KeyHandler(enabled: Boolean = true, onKey: (androidx.compose.ui.input.key.KeyEvent) -> Boolean) {
    val current = rememberUpdatedState(onKey)
    DisposableEffect(enabled) {
        val h: (androidx.compose.ui.input.key.KeyEvent) -> Boolean = { current.value(it) }
        if (enabled) KeyActions.add(h)
        onDispose { KeyActions.remove(h) }
    }
}
