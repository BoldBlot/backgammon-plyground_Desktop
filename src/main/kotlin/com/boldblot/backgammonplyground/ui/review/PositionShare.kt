package com.boldblot.backgammonplyground.ui.review

import com.boldblot.backgammonplyground.ui.components.ChipGapH
import com.boldblot.backgammonplyground.ui.components.ChipGapV
import com.boldblot.backgammonplyground.ui.shell.Cmd
import com.boldblot.backgammonplyground.ui.shell.RegisterCommand

import com.boldblot.backgammonplyground.showMessage
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import org.jetbrains.skia.Typeface
import com.boldblot.backgammonplyground.FileDialogs
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.boldblot.backgammonplyground.core.Board
import com.boldblot.backgammonplyground.core.CheckerAnalysis
import com.boldblot.backgammonplyground.core.CubeEval
import com.boldblot.backgammonplyground.core.Player
import com.boldblot.backgammonplyground.core.PositionAnalysis
import com.boldblot.backgammonplyground.data.ErrorUnit
import com.boldblot.backgammonplyground.ui.components.LocalErrorUnit
import com.boldblot.backgammonplyground.ui.components.err
import com.boldblot.backgammonplyground.core.DecisionKind
import com.boldblot.backgammonplyground.core.DecisionMath
import com.boldblot.backgammonplyground.core.DecisionRecord
import com.boldblot.backgammonplyground.core.MoveNotation
import com.boldblot.backgammonplyground.core.PositionIds
import com.boldblot.backgammonplyground.core.Severity
import com.boldblot.backgammonplyground.core.StudyPosition
import com.boldblot.backgammonplyground.core.XgRules
import com.boldblot.backgammonplyground.ui.components.PlayedBestBar
import com.boldblot.backgammonplyground.ui.components.eq
import com.boldblot.backgammonplyground.ui.theme.Quality
import kotlinx.coroutines.launch

/** What a review board shows: the decision with arrows, the bare position, or the position after a move. */
enum class ReviewView(val label: String) { ARROWS("Arrows"), BOARD("Board"), AFTER("After") }

/**
 * The chips that pick [ReviewView] (After greyed out for a decision without a move), then a Save chip
 * that saves the position to My positions ([onSave]; none when it cannot be saved).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewViewChips(view: ReviewView, hasMove: Boolean, onPick: (ReviewView) -> Unit, onSave: (() -> Unit)?, modifier: Modifier = Modifier) {
    val shown = if (!hasMove && view == ReviewView.AFTER) ReviewView.BOARD else view
    FlowRow(
        modifier, horizontalArrangement = Arrangement.spacedBy(ChipGapH, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(ChipGapV),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        ReviewView.entries.forEach { v ->
            FilterChip(
                selected = shown == v, onClick = { onPick(v) }, label = { Text(v.label) },
                enabled = hasMove || v != ReviewView.AFTER,
            )
        }
        if (onSave != null) {
            AssistChip(
                onClick = onSave, label = { Text("Save") },
                leadingIcon = { Icon(Icons.Default.BookmarkAdd, null, Modifier.size(AssistChipDefaults.IconSize)) },
            )
        }
    }
}

/**
 * Keeps the room of [content] when it is not [shown] (in landscape the board takes what is left, so
 * it would change size between decisions with and without a played/best bar).
 */
@Composable
fun KeepRoom(shown: Boolean, content: @Composable () -> Unit) {
    if (shown) content() else Box(Modifier.alpha(0f).clearAndSetSemantics {}) { content() }
}

/** The played/best bar's room, for [KeepRoom] on decisions without one. */
@Composable
fun EmptyPlayedBestBar() = PlayedBestBar("", null, Severity.NONE, "", null, Modifier.padding(top = 4.dp))

/**
 * The board of a review showing [view] of decision [d]: the position after the move chosen in the
 * list ([candidate]), or after the move played when none is chosen; otherwise the position itself.
 * Returns the board and whether it is the position after a move.
 */
fun reviewBoard(d: DecisionRecord, view: ReviewView, candidate: Int?): Pair<Board, Boolean> {
    if (view != ReviewView.AFTER) return d.board to false
    val move = candidate?.let { d.checker?.candidates?.getOrNull(it)?.move } ?: d.reviewMove ?: return d.board to false
    return d.board.applyMove(d.player, move) to true
}

/** Records what a board draws, so that it can be shared as a picture. */
class BoardCapture(val layer: GraphicsLayer) {
    /** For the board's modifier: draws it through [layer]. */
    val modifier: Modifier
        get() = Modifier.drawWithContent {
            layer.record { this@drawWithContent.drawContent() }
            drawLayer(layer)
        }

    suspend fun image(): ImageBitmap = layer.toImageBitmap()
}

@Composable
fun rememberBoardCapture(): BoardCapture = BoardCapture(rememberGraphicsLayer())

/**
 * The File and Edit menus' entries for decision [d]: its XGID, the board as shown ([capture]), and the board
 * with the top five plays (or the cube table). [title] heads the picture ("5-point match · score 2-1").
 */
@Composable
fun ShareCommands(d: DecisionRecord, matchLength: Int, jacoby: Boolean, title: String, capture: BoardCapture) {
    val study = StudyPosition.of(d, matchLength, jacoby)
    ShareCommands(
        study?.let { PositionIds.toXgid(it.setup.copy(doubled = it.doubled)) }, title,
        PositionShare.analysisLines(d, XgRules.errorFactor(matchLength)), capture,
        analysisLabel = (d.checker?.label ?: d.cubeEval?.label)?.ifBlank { null },
    )
}

/**
 * The same for a saved position [p]: its XGID, the board as shown, and the board with the top five plays or the cube
 * actions of [analysis]; [answerIndex] / [answerCube] mark the player's answer, tagged "you".
 */
@Composable
fun ShareCommands(
    p: StudyPosition, analysis: PositionAnalysis?, answerIndex: Int?, answerCube: DecisionKind?, capture: BoardCapture,
    noAnalysis: (() -> Unit)? = null,
) {
    val lines = analysis?.let {
        PositionShare.analysisLines(
            p.board, Player.HUMAN, it.checker, it.cube, response = p.doubled, beavers = p.beavers,
            playedIndex = answerIndex, playedKind = answerCube, factor = XgRules.errorFactor(p.setup.matchLength),
        )
    }.orEmpty()
    val label = analysis?.let { a -> a.label.ifBlank { a.checker?.label ?: a.cube?.label ?: "" } }?.ifBlank { null }
    ShareCommands(
        PositionIds.toXgid(p.setup.copy(doubled = p.doubled)), PositionShare.title(p, com.boldblot.backgammonplyground.ui.components.LocalScoreAway.current),
        lines, capture, playedTag = "you", analysisLabel = label, noAnalysis = noAnalysis,
    )
}

/** Offers [xgid] as text (if any), the board picture, and the board with [lines] (if any). */
@Composable
fun ShareCommands(
    xgid: String?, title: String, lines: List<PositionShare.Line>, capture: BoardCapture, playedTag: String = "played",
    /** "PureTD 3-ply, pruned": the engine and depth of [lines], printed under them. */
    analysisLabel: String? = null,
    /** Without [lines]: the top-5 entry still works and calls this (analyze first, or say why not). */
    noAnalysis: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val unit = LocalErrorUnit.current
    // The players' checkers beside their scores in the picture's heading, in the board's colours.
    val app = com.boldblot.backgammonplyground.LocalApp.current
    val settings = app?.settings?.settings?.collectAsState(initial = null)?.value
    val checkers = settings?.let { com.boldblot.backgammonplyground.ui.board.boardColorsFor(it) }?.let {
        PositionShare.ScoreCheckers(it.human.toArgb(), it.humanRim.toArgb(), it.cpu.toArgb(), it.cpuRim.toArgb())
    }
    RegisterCommand(Cmd.COPY_XGID, enabled = xgid != null) { xgid?.let { PositionShare.text(it) } }
    RegisterCommand(Cmd.SAVE_BOARD) {
        scope.launch { PositionShare.image(PositionShare.withAnalysis(capture.image().asSkiaBitmap(), title, emptyList(), null, unit, checkers = checkers), "board") }
    }
    RegisterCommand(Cmd.SAVE_TOP5, enabled = lines.isNotEmpty() || noAnalysis != null) {
        if (lines.isEmpty()) noAnalysis?.invoke()
        else scope.launch { PositionShare.analysisImage(capture, title, lines, xgid, unit, playedTag, analysisLabel, checkers) }
    }
}

/** Sharing positions as text or pictures (Messenger, WhatsApp, email…). */
object PositionShare {
    /** The players' checker colours (ARGB), drawn beside their scores in the heading. */
    data class ScoreCheckers(val human: Int, val humanRim: Int, val cpu: Int, val cpuRim: Int)

    /** One row of the analysis under a shared board: a play or cube action, its equity and its error. */
    data class Line(val text: String, val equity: Float, val diff: Float, val severity: Severity, val played: Boolean)

    /** Puts [text] on the clipboard. */
    fun text(text: String) {
        java.awt.Toolkit.getDefaultToolkit().systemClipboard.setContents(java.awt.datatransfer.StringSelection(text), null)
        showMessage("Copied to the clipboard.")
    }

    /** Asks where to save [image] as a PNG called PlyGround-[name].png. */
    fun image(image: Image, name: String) {
        val file = FileDialogs.save("Save picture", "PlyGround-$name.png") ?: return
        runCatching {
            val data = image.encodeToData(EncodedImageFormat.PNG, 100) ?: error("no data")
            file.writeBytes(data.bytes)
        }.onFailure { showMessage("The picture could not be made."); return }
        showMessage("Saved ${file.name}.")
    }

    /** Saves the board as [capture] shows it with [lines] (the top 5 or the cube actions) under it. */
    suspend fun analysisImage(
        capture: BoardCapture, title: String, lines: List<Line>, xgid: String?, unit: ErrorUnit,
        playedTag: String = "played", analysisLabel: String? = null, checkers: ScoreCheckers? = null,
    ) = image(withAnalysis(capture.image().asSkiaBitmap(), title, lines, xgid, unit, playedTag, analysisLabel, checkers), "analysis")

    /** The analysis of decision [d] for a shared picture (see the other [analysisLines]). */
    fun analysisLines(d: DecisionRecord, factor: Float): List<Line> {
        if (d.kind.isRaccoonDecision) return emptyList()
        return analysisLines(
            d.board, d.player, d.checker, d.cubeEval, response = d.kind.isResponse, beavers = d.beavers || d.kind == DecisionKind.BEAVER,
            playedIndex = d.reviewIndex, playedKind = d.reviewKind, factor = factor,
        )
    }

    /**
     * The analysis for a shared picture: the five best plays of [checker] (and the one played, [playedIndex],
     * if it is not among them), or else the cube actions of [cube] ([response]: take / pass, otherwise the
     * doubler's). [playedKind] marks the cube action taken. Empty without an analysis.
     */
    fun analysisLines(
        board: Board, player: Int, checker: CheckerAnalysis?, cube: CubeEval?, response: Boolean, beavers: Boolean,
        playedIndex: Int?, playedKind: DecisionKind?, factor: Float,
    ): List<Line> {
        fun line(text: String, equity: Float, best: Float, played: Boolean) =
            Line(text, equity, equity - best, XgRules.severity((best - equity) * factor), played)
        val a = checker
        if (a != null && a.candidates.isNotEmpty()) {
            val shown = (a.candidates.indices.take(5) + listOfNotNull(playedIndex).filter { it >= 5 && it < a.candidates.size }).distinct()
            return shown.map { i ->
                val c = a.candidates[i]
                line("${i + 1}. ${MoveNotation.format(board, player, c.move)}", c.equity, a.bestEquity, i == playedIndex)
            }
        }
        val e = cube ?: return emptyList()
        if (response) {
            val (take, pass, beaver) = DecisionMath.responseEquities(e, beavers)
            val best = maxOf(take, pass, beaver ?: take)
            return listOfNotNull(
                line("Take", take, best, playedKind == DecisionKind.TAKE),
                line("Pass", pass, best, playedKind == DecisionKind.PASS),
                beaver?.let { line("Beaver", it, best, playedKind == DecisionKind.BEAVER) },
            )
        }
        val best = maxOf(e.noDouble, DecisionMath.afterDouble(e, beavers))
        val doubled = playedKind == DecisionKind.DOUBLE
        return listOf(
            line("No double", e.noDouble, best, playedKind == DecisionKind.NO_DOUBLE),
            line("Double, take", e.doubleTake, best, doubled && e.doubleTake <= e.doublePass),
            line("Double, pass", e.doublePass, best, doubled && e.doubleTake > e.doublePass),
        )
    }

    private fun rgb(r: Int, g: Int, b: Int) = (0xFF shl 24) or (r shl 16) or (g shl 8) or b

    private fun paint(argb: Int, mode: PaintMode = PaintMode.FILL, width: Float = 1f) =
        Paint().apply { color = argb; isAntiAlias = true; this.mode = mode; strokeWidth = width }

    private fun Canvas.string(t: String, x: Float, y: Float, font: Font, p: Paint, alignRight: Boolean = false) =
        drawString(t, if (alignRight) x - font.measureTextWidth(t) else x, y, font, p)

    /** [board] under [title], with the analysis [lines] and the [xgid] below it (if any), on a white card. */
    fun withAnalysis(
        board: Bitmap, title: String, lines: List<Line>, xgid: String?, unit: ErrorUnit = ErrorUnit.EQ, playedTag: String = "played",
        analysisLabel: String? = null, checkers: ScoreCheckers? = null,
    ): Image {
        val w = board.width
        val pad = w / 30f
        val text = w / 30f
        val small = text * 0.7f
        val row = text * 1.45f
        val top = pad + row
        val label = analysisLabel?.takeIf { lines.isNotEmpty() }
        val below = (if (lines.isNotEmpty()) pad + row * lines.size else 0f) + (if (label != null) small * 1.6f else 0f) +
            (if (xgid != null) small * 2 else 0f) + pad / 2
        val h = (top + board.height + below).toInt()
        val surface = Surface.makeRasterN32Premul(w, h)
        val c = surface.canvas
        c.clear(rgb(0xFF, 0xFF, 0xFF))
        val dark = rgb(0x22, 0x22, 0x22)
        val grey = rgb(0x66, 0x66, 0x66)
        val sansFace = FontMgr.default.matchFamilyStyle(null, FontStyle.NORMAL) ?: Typeface.makeEmpty()
        val boldFace = FontMgr.default.matchFamilyStyle(null, FontStyle.BOLD) ?: sansFace
        val monoFace = FontMgr.default.matchFamilyStyle("monospace", FontStyle.NORMAL) ?: sansFace
        val sans = Font(sansFace, text)
        val bold = Font(boldFace, text)
        val smallSans = Font(sansFace, small)
        val smallMono = Font(monoFace, small)
        val mono = Font(monoFace, text)
        drawTitle(c, title, pad, pad + text, bold, paint(dark), checkers)
        c.drawImage(Image.makeFromBitmap(board), 0f, top)
        var y = top + board.height + pad + text
        for (l in lines) {
            if (l.played) {
                c.drawRect(Rect(pad / 2, y - text * 1.05f, w - pad / 2, y + text * 0.4f), paint(rgb(0xEC, 0xEF, 0xF6)))
            }
            c.string(l.text + if (l.played) "   ($playedTag)" else "", pad, y, if (l.played) bold else sans, paint(dark))
            c.string(eq(l.equity), w - pad - text * 5.5f, y, mono, paint(dark), alignRight = true)
            if (l.diff < -0.0005f) {
                c.string(err(l.diff, unit), w - pad, y, mono, paint(Quality.color(l.severity, false).toArgb()), alignRight = true)
            }
            y += row
        }
        if (label != null) {
            y += small * 0.1f
            c.string("Analysis: $label", pad, y, smallSans, paint(grey))
            y += small * 1.6f
        }
        if (xgid != null) {
            y += small * 0.5f
            c.string(xgid, pad, y, smallMono, paint(grey))
        }
        return surface.makeImageSnapshot()
    }

    /**
     * [title] at ([x], [y]); with [checkers], a checker of each player's colour before their score
     * ("5-point match · ● 2–1 ●", your score by your checker), as on the screens.
     */
    private fun drawTitle(c: Canvas, title: String, x: Float, y: Float, font: Font, ink: Paint, checkers: ScoreCheckers?) {
        val m = SCORE.find(title)
        if (checkers == null || m == null) {
            c.drawString(title, x, y, font, ink)
            return
        }
        val size = font.size
        val r = size * 0.36f
        var at = x
        fun text(t: String) { c.drawString(t, at, y, font, ink); at += font.measureTextWidth(t) }
        fun checker(colour: Int, rimColour: Int) {
            val cx = at + r
            val cy = y - size * 0.35f
            c.drawCircle(cx, cy, r, paint(colour))
            c.drawCircle(cx, cy, r, paint(rimColour, PaintMode.STROKE, size * 0.07f))
            at += 2 * r + size * 0.2f
        }
        text(title.substring(0, m.range.first))
        checker(checkers.human, checkers.humanRim)
        text(if (m.groupValues[1].isNotEmpty()) "${m.groupValues[1]}\u2013${m.groupValues[2]}" else "${m.groupValues[3]}-away, ${m.groupValues[4]}-away")
        at += size * 0.2f
        checker(checkers.cpu, checkers.cpuRim)
        text(title.substring(m.range.last + 1).trimStart().let { if (it.isEmpty()) it else " $it" })
    }

    /** "5-point match · 2-1", or with [away] "3-away, 4-away" (Preferences → Display). */
    private fun scoreHeading(matchLength: Int, score: List<Int>, away: Boolean): String = when {
        matchLength == 0 -> "Money game"
        away -> "${matchLength - score[0]}-away, ${matchLength - score[1]}-away"
        else -> "$matchLength-point match · ${score[0]}-${score[1]}"
    }

    /** The score in a heading: "2-1" after "N-point match · ". */
    private val SCORE = Regex("""(?<=match · )(\d+)-(\d+)|(\d+)-away, (\d+)-away""")

    /** The heading of a shared picture of saved position [p], as for a decision. */
    fun title(p: StudyPosition, away: Boolean = false): String = buildString {
        val s = p.setup
        append(scoreHeading(s.matchLength, s.score, away))
        if (s.crawford) append(" Crawford")
        if (s.cube.value > 1) append(" · Cube ${s.cube.value}")
        p.dice?.let { append(" · Roll $it") }
        if (p.doubled) append(" · Doubled")
    }

    /** "5-point match · 2-1 · Cube 2 · Roll 64" (or "Money game"), the heading of a shared picture. */
    fun title(d: DecisionRecord, matchLength: Int, away: Boolean = false): String = buildString {
        append(scoreHeading(matchLength, d.score, away))
        if (d.crawford) append(" Crawford")
        if (d.cube.value > 1) append(" · Cube ${d.cube.value}")
        d.dice?.let { append(" · Roll $it") }
    }
}
