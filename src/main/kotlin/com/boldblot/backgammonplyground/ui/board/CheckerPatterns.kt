package com.boldblot.backgammonplyground.ui.board

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.toArgb
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ClipMode
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.MaskFilter
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PaintStrokeCap
import org.jetbrains.skia.PathBuilder
import org.jetbrains.skia.Rect
import com.boldblot.backgammonplyground.data.CheckerStyle
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * The patterned checker styles, flat in colour with a pattern of their own: [CheckerStyle.CATALIN]
 * (soft forking veins in a deeper shade, as in catalin checkers) and [CheckerStyle.SWIRL] (smoky folds,
 * as in swirled resin). Each is drawn once into a bitmap and cached by colour, size and pattern number;
 * [PATTERNS] patterns per colour go round the board, so neighbouring checkers differ.
 */
object CheckerPatterns {
    const val PATTERNS = 12

    private data class Key(val style: CheckerStyle, val fill: Int, val rim: Int, val size: Int, val pattern: Int)

    private val cache = object : LinkedHashMap<Key, ImageBitmap>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Key, ImageBitmap>?) = size > 160
    }

    /** The pattern number for a checker [seed] (its slot on the board). */
    fun patternOf(seed: Int): Int = ((seed * -1640531535) ushr 20) % PATTERNS

    /** A checker of diameter [size] px in [style] (CATALIN or SWIRL), pattern [pattern]. */
    fun image(style: CheckerStyle, fill: Color, rim: Color, size: Int, pattern: Int): ImageBitmap {
        val key = Key(style, fill.toArgb(), rim.toArgb(), size.coerceAtLeast(8), pattern)
        synchronized(cache) { cache[key]?.let { return it } }
        val bmp = when (style) {
            CheckerStyle.SWIRL -> swirl(key.fill, key.rim, key.size, 101 + pattern * 37)
            else -> catalin(key.fill, key.rim, key.size, 101 + pattern * 37)
        }.asComposeImageBitmap()
        synchronized(cache) { cache[key] = bmp }
        return bmp
    }

    // ---- colour helpers ------------------------------------------------------------------------

    private fun red(c: Int) = (c shr 16) and 0xFF
    private fun green(c: Int) = (c shr 8) and 0xFF
    private fun blue(c: Int) = c and 0xFF
    private fun rgb(r: Int, g: Int, b: Int) = (0xFF shl 24) or (r shl 16) or (g shl 8) or b

    private fun lum(c: Int): Float = (red(c) * 0.3f + green(c) * 0.59f + blue(c) * 0.11f) / 255f

    /** Hue (0..360), saturation and lightness (0..1), as Android's ColorUtils.colorToHSL. */
    private fun hsl(c: Int): FloatArray {
        val r = red(c) / 255f
        val g = green(c) / 255f
        val b = blue(c) / 255f
        val mx = max(r, max(g, b))
        val mn = min(r, min(g, b))
        val d = mx - mn
        val l = (mx + mn) / 2f
        var h: Float
        val s: Float
        if (mx == mn) {
            h = 0f; s = 0f
        } else {
            h = when (mx) {
                r -> ((g - b) / d) % 6f
                g -> (b - r) / d + 2f
                else -> (r - g) / d + 4f
            }
            s = d / (1f - kotlin.math.abs(2f * l - 1f))
        }
        h = (h * 60f) % 360f
        if (h < 0) h += 360f
        return floatArrayOf(h, s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))
    }

    private fun hslToColor(hsl: FloatArray): Int {
        val h = hsl[0]
        val s = hsl[1]
        val l = hsl[2]
        val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
        val m = l - 0.5f * c
        val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
        val (r, g, b) = when ((h / 60f).toInt()) {
            0 -> Triple(c, x, 0f)
            1 -> Triple(x, c, 0f)
            2 -> Triple(0f, c, x)
            3 -> Triple(0f, x, c)
            4 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        fun ch(v: Float) = ((v + m) * 255f + 0.5f).toInt().coerceIn(0, 255)
        return rgb(ch(r), ch(g), ch(b))
    }

    private fun mix(a: Int, b: Int, t: Float): Int {
        val u = t.coerceIn(0f, 1f)
        fun ch(x: Int, y: Int) = (x * (1 - u) + y * u).toInt().coerceIn(0, 255)
        return rgb(ch(red(a), red(b)), ch(green(a), green(b)), ch(blue(a), blue(b)))
    }

    /** Light checkers: a deeper shade (tan on white and ivory); dark ones: a lighter shade. */
    private fun veinColour(fill: Int, depth: Float): Int {
        val (h0, s0, l) = hsl(fill).toList()
        var h = h0
        var s = s0
        val l2: Float
        var s2: Float
        if (lum(fill) > 0.35f) {
            if (s < 0.6f && l > 0.8f) {
                h = 32f; s = 0.22f; l2 = max(0f, l - depth); s2 = min(1f, s + 0.15f)
            } else {
                l2 = l * (1 - depth * 1.1f); s2 = s
            }
        } else {
            l2 = min(1f, l + depth * 0.9f); s2 = s * 0.8f
        }
        return hslToColor(floatArrayOf(h, s2, l2))
    }

    /** The same random numbers as the design previews (a 32-bit LCG). */
    private class Rnd(seed: Int) {
        private var x = seed * 1103515245 + 12345
        operator fun invoke(): Float {
            x = x * 1103515245 + 12345
            return ((x ushr 8) and 0xFFFF) / 65535f
        }
    }

    private fun circle(size: Int) = PathBuilder().addCircle(size / 2f, size / 2f, size / 2f - 1f).detach()

    private fun rimStroke(canvas: Canvas, size: Int, rim: Int) {
        val r = size / 2f - 1f
        canvas.drawCircle(
            size / 2f, size / 2f, r * 0.985f,
            Paint().apply {
                isAntiAlias = true; mode = PaintMode.STROKE; strokeWidth = max(1f, r * 0.035f)
                color = (190 shl 24) or (rim and 0xFFFFFF)
            },
        )
    }

    // ---- catalin ---------------------------------------------------------------------------------

    private fun path(rnd: Rnd, cx: Float, cy: Float, r: Float, n: Int, curl: Float, start: FloatArray? = null, heading: Float = 0f): List<FloatArray> {
        var a = heading
        var p = start ?: run {
            val e = rnd() * 6.283f
            a = e + PI.toFloat() + (rnd() - 0.5f) * 1.4f
            floatArrayOf(cx + cos(e) * r * 1.05f, cy + sin(e) * r * 1.05f)
        }
        var turn = 0f
        val pts = mutableListOf(p)
        repeat(n) {
            turn = (turn + (rnd() - 0.5f) * curl * 0.5f).coerceIn(-curl * 0.35f, curl * 0.35f)
            a += turn
            p = floatArrayOf(p[0] + cos(a) * r * 0.085f, p[1] + sin(a) * r * 0.085f)
            pts += p
        }
        return pts
    }

    private fun newBitmap(w: Int, h: Int) = Bitmap().apply { allocPixels(ImageInfo(w, h, ColorType.BGRA_8888, ColorAlphaType.PREMUL)) }

    private fun catalin(fill: Int, rim: Int, size: Int, seed: Int): Bitmap {
        val bmp = newBitmap(size, size)
        val canvas = Canvas(bmp)
        val c = size / 2f
        val r = size / 2f - 1f
        val rnd = Rnd(seed)
        canvas.drawCircle(c, c, r, Paint().apply { isAntiAlias = true; color = fill })
        canvas.save()
        canvas.clipPath(circle(size), ClipMode.INTERSECT, true)
        // The veins go opaque into a layer that is laid on at [alpha], so where they cross they don't darken.
        canvas.saveLayer(Rect.makeWH(size.toFloat(), size.toFloat()), Paint().apply { color = (204 shl 24) })
        val paint = Paint().apply {
            isAntiAlias = true
            color = veinColour(fill, 0.28f); mode = PaintMode.STROKE; strokeCap = PaintStrokeCap.ROUND
            // Android's blur radius to a Gaussian sigma
            maskFilter = MaskFilter.makeBlur(FilterBlurMode.NORMAL, 0.57735f * max(0.5f, r * 0.02f) + 0.5f)
        }
        val paths = mutableListOf<Pair<List<FloatArray>, Float>>()
        repeat(2) {
            val pts = path(rnd, c, c, r, 26, 0.6f)
            paths += pts to 1f
            repeat(2) {
                val j = 2 + (rnd() * (pts.size - 6)).toInt()
                val dir = atan2(pts[j + 1][1] - pts[j][1], pts[j + 1][0] - pts[j][0])
                val ang = dir + (0.5f + rnd() * 0.5f) * if (rnd() < 0.5f) 1f else -1f
                paths += path(rnd, c, c, r, 6 + (rnd() * 8).toInt(), 0.6f, pts[j], ang) to 0.55f
            }
        }
        for ((pts, scale) in paths) {
            val w0 = r * 0.05f * (0.6f + 0.8f * rnd()) * scale
            for (k in 0 until pts.size - 1) {
                // the vein swells and thins along its length
                paint.strokeWidth = max(1f, w0 * (0.55f + 0.45f * sin(k * 0.22f + scale * 3)))
                canvas.drawLine(pts[k][0], pts[k][1], pts[k + 1][0], pts[k + 1][1], paint)
            }
        }
        canvas.restore()
        canvas.restore()
        rimStroke(canvas, size, rim)
        return bmp
    }

    // ---- swirl -----------------------------------------------------------------------------------

    private fun hash(ix: Int, iy: Int, seed: Int): Float {
        var h = (ix.toLong() * 374761393L + iy.toLong() * 668265263L + seed.toLong() * 2147483647L) and 0xffffffffL
        h = ((h xor (h ushr 13)) * 1274126177L) and 0xffffffffL
        return ((h xor (h ushr 16)) and 0xffff) / 65535f
    }

    private fun noise(x: Float, y: Float, seed: Int): Float {
        val ix = floor(x).toInt()
        val iy = floor(y).toInt()
        val fx = x - ix
        val fy = y - iy
        val ux = fx * fx * (3 - 2 * fx)
        val uy = fy * fy * (3 - 2 * fy)
        val a = hash(ix, iy, seed)
        val b = hash(ix + 1, iy, seed)
        val c = hash(ix, iy + 1, seed)
        val d = hash(ix + 1, iy + 1, seed)
        return a + (b - a) * ux + (c - a) * uy + (a - b - c + d) * ux * uy
    }

    private fun fbm(x: Float, y: Float, seed: Int, octaves: Int): Float {
        var v = 0f
        var amp = 0.5f
        var f = 1f
        for (o in 0 until octaves) {
            v += amp * noise(x * f, y * f, seed + o * 101)
            f *= 2f; amp *= 0.5f
        }
        return v / (1 - (1 shl octaves).let { 1f / it })
    }

    private fun smooth(e0: Float, e1: Float, x: Float): Float {
        val t = ((x - e0) / (e1 - e0)).coerceIn(0f, 1f)
        return t * t * (3 - 2 * t)
    }

    private fun swirl(fill: Int, rim: Int, size: Int, seed: Int): Bitmap {
        val (h, s, l) = hsl(fill).toList()
        val pale = l > 0.85f && s < 0.75f
        val tan = rgb(150, 120, 90)
        val deep = if (pale) mix(fill, tan, 0.2f) else hslToColor(floatArrayOf(h, min(1f, s * 1.05f), l * 0.72f))
        val light = if (pale) mix(fill, rgb(170, 140, 105), 0.45f) else mix(fill, rgb(250, 246, 236), 0.7f)
        val n = size
        val c = (n - 1) / 2f
        val r = n / 2f - 2f
        val scale = 0.75f
        val warp = 2.6f
        val oct = 3
        val px = ByteArray(n * n * 4)
        for (y in 0 until n) for (x in 0 until n) {
            val sx = (x - c) / r * scale + seed * 7.31f
            val sy = (y - c) / r * scale - seed * 3.17f
            val qx = fbm(sx, sy, seed, oct)
            val qy = fbm(sx + 5.2f, sy + 1.3f, seed + 11, oct)
            val rx = fbm(sx + warp * qx + 1.7f, sy + warp * qy + 9.2f, seed + 23, oct)
            val ry = fbm(sx + warp * qx + 8.3f, sy + warp * qy + 2.8f, seed + 37, oct)
            val f = fbm(sx + warp * rx, sy + warp * ry, seed + 51, oct + 1)
            var col = mix(deep, fill, smooth(0.32f, 0.6f, f))
            // wisps: thin bands where the second warped field crosses a level, as smoke folds
            val band = exp(-((rx - 0.5f) / 0.045f).let { it * it }) * smooth(0.4f, 0.65f, f)
            val w = (band + smooth(0.62f, 0.8f, f) * 0.6f).coerceIn(0f, 1f) * if (pale) 0.55f else 0.8f
            col = if (pale) {
                mix(mix(fill, light, w), deep, smooth(0.45f, 0.2f, f) * 0.35f)
            } else {
                mix(col, light, w)
            }
            val o = (y * n + x) * 4
            px[o] = blue(col).toByte(); px[o + 1] = green(col).toByte(); px[o + 2] = red(col).toByte(); px[o + 3] = 0xFF.toByte()
        }
        val pattern = Image.makeRaster(ImageInfo(n, n, ColorType.BGRA_8888, ColorAlphaType.PREMUL), px, n * 4)
        val bmp = newBitmap(n, n)
        val canvas = Canvas(bmp)
        canvas.save()
        canvas.clipPath(circle(n), ClipMode.INTERSECT, true)
        canvas.drawImage(pattern, 0f, 0f)
        canvas.restore()
        pattern.close()
        rimStroke(canvas, n, rim)
        return bmp
    }
}
