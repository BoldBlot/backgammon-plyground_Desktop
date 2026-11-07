package com.boldblot.backgammonplyground.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Between chips in a row, and between the rows of chips that wrap. */
val ChipGapH = 10.dp
val ChipGapV = 8.dp

/** The bar on top of a window or pane: a title and its actions, in a desktop's height (no back arrow). */
@Composable
fun DesktopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            navigationIcon()
            ProvideTextStyle(MaterialTheme.typography.titleSmall) {
                Row(Modifier.weight(1f).padding(horizontal = 4.dp)) { title() }
            }
            actions()
        }
        HorizontalDivider()
    }
}

/** A small button of a toolbar: an icon with its name, or the icon alone when [iconOnly], which shows the name on hover. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ToolButton(icon: ImageVector, label: String, enabled: Boolean = true, selected: Boolean = false, iconOnly: Boolean = false, onClick: () -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    var m = Modifier.height(32.dp)
    if (selected) m = m.background(MaterialTheme.colorScheme.secondaryContainer, shape)
    val button: @Composable () -> Unit = {
        Row(
            m.then(Modifier.clickable(enabled = enabled, onClick = onClick)).alpha(if (enabled) 1f else 0.38f).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, label, Modifier.size(18.dp))
            if (!iconOnly) {
                Spacer(Modifier.width(5.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1, softWrap = false)
            }
        }
    }
    if (iconOnly) {
        androidx.compose.foundation.TooltipArea(
            tooltip = {
                Surface(shape = shape, color = MaterialTheme.colorScheme.inverseSurface) {
                    Text(label, color = MaterialTheme.colorScheme.inverseOnSurface, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            },
            delayMillis = 300,
        ) { button() }
    } else button()
}

/** Tabs at the top of the side panel. */
@Composable
fun PanelTabs(names: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            names.forEachIndexed { i, name ->
                val on = i == selected
                Column(Modifier.weight(1f).clickable { onSelect(i) }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                        color = if (on) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 6.dp),
                    )
                    Spacer(
                        Modifier.fillMaxWidth().height(2.dp)
                            .background(if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}

/** A heading inside the side panel. */
@Composable
fun PanelTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/**
 * The mouse on a row of a list or table: a press selects at once, a double click opens, the right button asks for its
 * menu. [onPress] gets whether Ctrl and Shift are held.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.rowPointer(onPress: (ctrl: Boolean, shift: Boolean) -> Unit, onOpen: () -> Unit = {}, onMenu: () -> Unit = {}): Modifier =
    onPointerEvent(PointerEventType.Press) { e ->
        when (e.button) {
            PointerButton.Secondary -> onMenu()
            PointerButton.Primary -> {
                if ((e.awtEventOrNull?.clickCount ?: 1) >= 2) onOpen()
                else onPress(e.keyboardModifiers.isCtrlPressed, e.keyboardModifiers.isShiftPressed)
            }
            else -> Unit
        }
    }

/** The selection after a press on a row, as desktop lists do it; [anchor] is where a Shift range starts. */
data class RowSelection<T>(val selected: Set<T> = emptySet(), val anchor: T? = null) {
    /** [clicked] pressed with these keys, the rows being in [order]. */
    fun press(clicked: T, order: List<T>, ctrl: Boolean, shift: Boolean): RowSelection<T> {
        val from = anchor?.let(order::indexOf) ?: -1
        val to = order.indexOf(clicked)
        return when {
            shift && from >= 0 && to >= 0 -> {
                val range = order.subList(minOf(from, to), maxOf(from, to) + 1).toSet()
                RowSelection(if (ctrl) selected + range else range, anchor)
            }
            ctrl -> RowSelection(if (clicked in selected) selected - clicked else selected + clicked, clicked)
            else -> RowSelection(setOf(clicked), clicked)
        }
    }

    /** Selected rows that no longer exist are dropped. */
    fun keepOnly(rows: Collection<T>): RowSelection<T> =
        RowSelection(selected.intersect(rows.toSet()), anchor?.takeIf { it in rows })
}

/** The surface of the side panel. */
@Composable
fun PanelSurface(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxHeight(), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.fillMaxHeight().fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.Top, content = content)
    }
}
