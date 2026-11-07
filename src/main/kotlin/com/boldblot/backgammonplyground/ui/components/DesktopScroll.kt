package com.boldblot.backgammonplyground.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * [verticalScroll] for the desktop: the mouse wheel scrolls as before, and a bar at the right edge (shown while the
 * content is taller than the view) can be dragged, or clicked to jump to that place. Replaces `verticalScroll(...)`.
 */
@Composable
fun Modifier.desktopVerticalScroll(state: ScrollState = rememberScrollState()): Modifier {
    val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    return this
        .drawWithContent {
            drawContent()
            val max = state.maxValue
            if (max <= 0 || max == Int.MAX_VALUE) return@drawWithContent
            val view = size.height
            val thumb = (view * view / (view + max)).coerceAtLeast(32.dp.toPx())
            val top = state.value.toFloat() / max * (view - thumb)
            val w = 6.dp.toPx()
            drawRoundRect(color, Offset(size.width - w - 2.dp.toPx(), top), Size(w, thumb), CornerRadius(w / 2))
        }
        .pointerInput(state) {
            val grab = 16.dp.toPx()
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                val max = state.maxValue
                if (max <= 0 || max == Int.MAX_VALUE || down.position.x < size.width - grab) return@awaitEachGesture
                val view = size.height.toFloat()
                val thumb = (view * view / (view + max)).coerceAtLeast(32.dp.toPx())
                val room = view - thumb
                if (room <= 0f) return@awaitEachGesture
                down.consume()
                val top = state.value.toFloat() / max * room
                // A press beside the thumb jumps so that the thumb is under the pointer; then it follows the pointer.
                if (down.position.y < top || down.position.y > top + thumb) {
                    state.dispatchRawDelta((down.position.y - thumb / 2) / room * max - state.value)
                }
                var lastY = down.position.y
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val c = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!c.pressed) { c.consume(); break }
                    state.dispatchRawDelta((c.position.y - lastY) / room * max)
                    lastY = c.position.y
                    c.consume()
                }
            }
        }
        .verticalScroll(state)
}
