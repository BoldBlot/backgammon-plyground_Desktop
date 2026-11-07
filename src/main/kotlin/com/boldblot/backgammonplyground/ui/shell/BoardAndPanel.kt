package com.boldblot.backgammonplyground.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boldblot.backgammonplyground.ui.components.PanelSurface

/** Width over height of the board. */
const val BOARD_ASPECT = 1.38f

private val MIN_PANEL = 340.dp
private val MAX_PANEL = 440.dp
/** The room [BoardAndPanel] leaves under the board when it has something there. */
private val UNDER_BOARD = 96.dp

/**
 * The main window's one layout: the board, the largest that fits, with the side panel beside it. Every view (start, play,
 * review, position, quiz) uses it, so the board stays where it is when the view changes. [board] gets the modifier that
 * gives it its size. View → Side panel hides the panel and the board takes the room.
 */
@Composable
fun BoardAndPanel(
    panelRight: Boolean,
    board: @Composable (Modifier) -> Unit,
    under: (@Composable ColumnScope.() -> Unit)? = null,
    panel: @Composable ColumnScope.() -> Unit,
) {
    val showPanel = LocalShell.current.panelVisible
    val back = LocalCommands.current[Cmd.CLOSE_VIEW]
    val content: @Composable ColumnScope.() -> Unit = {
        if (back != null) {
            FilledTonalButton(onClick = { back.run() }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(back.label ?: "Back")
            }
        }
        panel()
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val boardHeight: Dp = maxHeight - 12.dp - if (under != null) UNDER_BOARD else 0.dp
        val width = (maxWidth - boardHeight * BOARD_ASPECT - 13.dp).coerceIn(MIN_PANEL, MAX_PANEL)
        Row(Modifier.fillMaxSize()) {
            if (showPanel && !panelRight) {
                PanelSurface(Modifier.width(width), content)
                VerticalDivider()
            }
            Column(Modifier.weight(1f).fillMaxHeight().padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    board(Modifier.aspectRatio(BOARD_ASPECT))
                }
                under?.invoke(this)
            }
            if (showPanel && panelRight) {
                VerticalDivider()
                PanelSurface(Modifier.width(width), content)
            }
        }
    }
}
