package com.ticka.app.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.SweepGradient
import com.ticka.app.data.Star
import kotlin.math.cos
import kotlin.math.sin

/**
 * 星图海报渲染：用 android.graphics 独立绘制高清星图图片（与 app 内图腾同款几何）。
 * phase 取自每颗星的获得时刻，导出结果自然多样。
 */
object SkyPoster {

    private const val BG = 0xFF070714.toInt()
    private const val WHITE = 0xFFFFFFFF.toInt()
    private const val TEXT_GLOW = 0xFFEEF0FF.toInt()
    private const val CYAN = 0xFF22D3EE.toInt()
    private const val VIOLET = 0xFF8B5CF6.toInt()
    private const val PINK = 0xFFEC4899.toInt()
    private const val AMBER = 0xFFFBBF24.toInt()
    private const val INDIGO = 0xFF6366F1.toInt()
    private const val GREEN = 0xFF34D399.toInt()
    private const val ORANGE = 0xFFFB923C.toInt()
    private const val ICE = 0xFFA5F3FC.toInt()
    private const val ICE_DEEP = 0xFF67E8F9.toInt()
    private const val GOLD = 0xFFFDE68A.toInt()
    private const val GOLD_DEEP = 0xFFF59E0B.toInt()
    private const val CREAM = 0xFFFFFBEB.toInt()
    private const val BLUE = 0xFF3B82F6.toInt()
    private const val ROSE = 0xFFFDF2F8.toInt()
    private const val PALE_GOLD = 0xFFFEF9C3.toInt()
    private val RAINBOW = intArrayOf(0xFFF87171.toInt(), 0xFFFBBF24.toInt(), 0xFF34D399.toInt(), 0xFF22D3EE.toInt(), 0xFF818CF8.toInt(), 0xFFE879F9.toInt())

    fun render(stars: List<Star>, w: Int = 1080, h: Int = 1350): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.shader = LinearGradient(0f, 0f, 0f, h.toFloat(), 0xFF05050F.toInt(), 0xFF0A0A24.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        val rnd = java.util.Random(42)
        for (i in 0 until 60) {
            paint.color = Color.argb(25 + (i % 3) * 14, 255, 255, 255)
            canvas.drawCircle(rnd.nextFloat() * w, rnd.nextFloat() * h, if (i % 6 == 0) 2.2f else 1.2f, paint)
        }

        val unit = w / 380f
        stars.forEachIndexed { i, s ->
            val sizeDp = starSkySizes[s.kind % starSkySizes.size].value
            val phase = (s.obtainedAt % 1000L) / 1000f
            drawStar(canvas, paint, s.kind, s.x * w, s.y * h, sizeDp * unit, phase, i)
        }

        paint.shader = null
        paint.color = Color.argb(130, 238, 240, 255)
        paint.textSize = 32f
        paint.isFakeBoldText = true
        canvas.drawText("Ticka 星空 · ${stars.size} 颗星", 44f, h - 52f, paint)
        return bmp
    }

    private fun tint(c: Int, a: Float): Int =
        Color.argb((a * 255).toInt().coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))

    private fun glow(canvas: Canvas, paint: Paint, x: Float, y: Float, r: Float, c: Int, a: Float) {
        paint.shader = RadialGradient(x, y, r, intArrayOf(tint(c, a), 0x00000000), floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        canvas.drawCircle(x, y, r, paint)
        paint.shader = null
    }

    private fun star4(cx: Float, cy: Float, r: Float, ir: Float = 0.30f): Path {
        val p = Path()
        for (i in 0 until 8) {
            val ang = (-Math.PI / 2 + i * Math.PI / 4)
            val rad = if (i % 2 == 0) r else (r * ir).toFloat()
            val x = cx + (rad * cos(ang)).toFloat()
            val y = cy + (rad * sin(ang)).toFloat()
            if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        p.close()
        return p
    }

    private fun hex(cx: Float, cy: Float, r: Float, startDeg: Float = -90f): Path {
        val p = Path()
        for (i in 0 until 6) {
            val a = Math.toRadians((startDeg + i * 60).toDouble())
            val x = cx + (r * cos(a)).toFloat()
            val y = cy + (r * sin(a)).toFloat()
            if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        p.close()
        return p
    }

    private fun drawStar(canvas: Canvas, paint: Paint, kind: Int, cx: Float, cy: Float, box: Float, phase: Float, seed: Int) {
        val R = box * 0.36f
        val S = box
        val twoPi = (2 * Math.PI).toFloat()
        val tw = 0.7f + 0.3f * sin(twoPi * phase)
        when (kind) {
            0 -> { // 尘星
                glow(canvas, paint, cx, cy, R, TEXT_GLOW, 0.45f)
                paint.color = 0xFFF5F6FF.toInt(); canvas.drawCircle(cx, cy, R * 0.30f, paint)
            }
            1 -> { // 辉星
                glow(canvas, paint, cx, cy, R * 1.15f, CYAN, 0.35f + 0.15f * sin(twoPi * phase))
                paint.color = CYAN; canvas.drawPath(star4(cx, cy, R), paint)
            }
            2 -> { // 焰星
                glow(canvas, paint, cx, cy, R * 1.2f, AMBER, 0.40f + 0.10f * sin(twoPi * phase))
                paint.shader = LinearGradient(cx - R, cy - R, cx + R, cy + R, AMBER, ORANGE, Shader.TileMode.CLAMP)
                canvas.drawPath(star4(cx, cy, R), paint); paint.shader = null
                paint.color = GOLD; canvas.drawCircle(cx, cy, R * 0.10f, paint)
            }
            3 -> { // 双子星
                val dx = R * 0.52f
                val ay = cy + R * 0.25f; val by = cy - R * 0.25f
                paint.color = tint(WHITE, 0.40f); paint.strokeWidth = S * 0.012f
                canvas.drawLine(cx - dx, ay, cx + dx, by, paint)
                glow(canvas, paint, cx - dx, ay, R * 0.8f, VIOLET, 0.5f)
                glow(canvas, paint, cx + dx, by, R * 0.8f, PINK, 0.5f)
                paint.color = VIOLET; canvas.drawPath(star4(cx - dx, ay, R * 0.62f), paint)
                paint.color = PINK; canvas.drawPath(star4(cx + dx, by, R * 0.62f), paint)
            }
            4 -> { // 超新星
                glow(canvas, paint, cx, cy, R, INDIGO, 0.5f)
                paint.color = tint(WHITE, 0.70f); paint.strokeWidth = S * 0.012f
                for (i in 0 until 8) {
                    val ang = i * Math.PI / 4
                    canvas.drawLine(
                        cx + (R * 0.55 * sin(ang)).toFloat(), cy - (R * 0.55 * cos(ang)).toFloat(),
                        cx + (R * 0.95 * sin(ang)).toFloat(), cy - (R * 0.95 * cos(ang)).toFloat(), paint
                    )
                }
                paint.shader = SweepGradient(cx, cy, intArrayOf(INDIGO, CYAN, INDIGO), null)
                paint.style = Paint.Style.STROKE; paint.strokeWidth = S * 0.02f
                canvas.drawCircle(cx, cy, R * 0.5f, paint)
                paint.style = Paint.Style.FILL; paint.shader = null
                paint.color = WHITE; canvas.drawCircle(cx, cy, R * 0.15f, paint)
            }
            5 -> { // 彗星
                canvas.save(); canvas.rotate(38f, cx, cy)
                val tail = R * 1.9f
                paint.shader = LinearGradient(cx - tail, cy, cx, cy, 0x00000000, tint(CYAN, 0.75f), Shader.TileMode.CLAMP)
                val p = Path()
                p.moveTo(cx + R * 0.1f, cy - R * 0.34f); p.lineTo(cx - tail, cy); p.lineTo(cx + R * 0.1f, cy + R * 0.34f); p.close()
                canvas.drawPath(p, paint); paint.shader = null
                glow(canvas, paint, cx, cy, R * 0.9f, CYAN, 0.55f)
                paint.color = tint(VIOLET, 0.6f); canvas.drawCircle(cx, cy, R * 0.34f, paint)
                paint.color = WHITE; canvas.drawCircle(cx, cy, R * 0.22f, paint)
                canvas.restore()
            }
            6 -> { // 极光星
                val cols = intArrayOf(GREEN, CYAN, VIOLET)
                for (k in 0..2) {
                    val sway = sin(twoPi * phase + k * 1.4f) * R * 0.16f
                    val col = cols[k]
                    paint.shader = LinearGradient(
                        0f, cy - R * 1.15f, 0f, cy + R * 1.15f,
                        0x00000000, tint(col, 0.75f), Shader.TileMode.CLAMP
                    )
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = R * 0.26f - k * R * 0.04f
                    paint.strokeCap = Paint.Cap.ROUND
                    val p = Path()
                    p.moveTo(cx - R * 0.5f + sway, cy - R * 1.15f)
                    p.cubicTo(
                        cx + R * 0.55f + sway, cy - R * 0.4f,
                        cx - R * 0.6f + sway, cy + R * 0.3f,
                        cx + R * 0.45f + sway, cy + R * 1.15f
                    )
                    canvas.drawPath(p, paint)
                }
                paint.style = Paint.Style.FILL; paint.strokeCap = Paint.Cap.BUTT; paint.shader = null
                glow(canvas, paint, cx, cy - R * 0.9f, R * 0.5f, WHITE, 0.8f)
            }
            7 -> { // 星环星
                canvas.save(); canvas.rotate(-22f, cx, cy)
                paint.style = Paint.Style.STROKE; paint.strokeWidth = R * 0.13f
                paint.shader = LinearGradient(cx - R * 1.05f, cy, cx + R * 1.05f, cy, CYAN, AMBER, Shader.TileMode.CLAMP)
                canvas.drawOval(cx - R * 1.05f, cy - R * 0.34f, cx + R * 1.05f, cy + R * 0.34f, paint)
                paint.shader = null; paint.color = tint(WHITE, 0.25f); paint.strokeWidth = R * 0.04f
                canvas.drawOval(cx - R * 1.28f, cy - R * 0.44f, cx + R * 1.28f, cy + R * 0.44f, paint)
                paint.style = Paint.Style.FILL
                canvas.restore()
                glow(canvas, paint, cx, cy, R * 1.1f, AMBER, 0.5f)
                paint.shader = RadialGradient(cx - R * 0.1f, cy - R * 0.1f, R * 0.36f, CREAM, GOLD_DEEP, Shader.TileMode.CLAMP)
                canvas.drawCircle(cx, cy, R * 0.36f, paint); paint.shader = null
            }
            8 -> { // 钻石星
                glow(canvas, paint, cx, cy, R * 0.8f, ICE, 0.35f * tw)
                paint.alpha = (tw * 255).toInt()
                paint.shader = LinearGradient(0f, cy - R * 1.1f, 0f, cy + R * 1.1f, WHITE, ICE, Shader.TileMode.CLAMP)
                canvas.drawPath(star4(cx, cy, R * 1.1f, 0.09f), paint)
                paint.shader = null
                canvas.save(); canvas.rotate(45f, cx, cy)
                paint.color = tint(WHITE, 0.85f * tw)
                canvas.drawPath(star4(cx, cy, R * 0.5f, 0.14f), paint)
                canvas.restore(); paint.alpha = 255
            }
            9 -> { // 脉冲星
                val pulse = 0.6f + 0.4f * sin(twoPi * phase)
                canvas.save(); canvas.rotate(45f, cx, cy)
                paint.strokeCap = Paint.Cap.ROUND
                paint.strokeWidth = R * 0.12f * pulse + 1f
                paint.shader = LinearGradient(
                    0f, cy - R * 1.3f, 0f, cy + R * 1.3f,
                    intArrayOf(0x00000000, tint(ICE_DEEP, 0.85f * pulse), 0x00000000), null,
                    Shader.TileMode.CLAMP
                )
                canvas.drawLine(cx, cy - R * 1.3f, cx, cy + R * 1.3f, paint)
                canvas.restore(); paint.shader = null; paint.strokeCap = Paint.Cap.BUTT
                glow(canvas, paint, cx, cy, R * 0.9f, CYAN, 0.2f + 0.5f * pulse)
                paint.color = WHITE; canvas.drawCircle(cx, cy, R * 0.20f, paint)
            }
            10 -> { // 星云
                val blobs = arrayOf(
                    arrayOf(PINK, -0.35f, -0.2f, 0.55f, 0.9f),
                    arrayOf(VIOLET, 0.32f, 0.1f, 0.62f, 0.7f),
                    arrayOf(BLUE, -0.1f, 0.38f, 0.5f, 1.1f),
                    arrayOf(ICE_DEEP, 0.15f, -0.42f, 0.4f, 0.8f)
                )
                blobs.forEachIndexed { i, b ->
                    val col = b[0] as Int
                    val ox = b[1] as Float; val oy = b[2] as Float
                    val rr = b[3] as Float; val sp = b[4] as Float
                    val x = cx + ox * R + sin(twoPi * phase * sp + i) * R * 0.06f
                    val y = cy + oy * R + cos(twoPi * phase * sp * 0.8f + i) * R * 0.06f
                    glow(canvas, paint, x, y, R * rr, col, 0.30f)
                }
                paint.color = tint(WHITE, 0.95f)
                listOf(-0.1f to -0.05f, 0.28f to 0.22f, -0.3f to 0.3f).forEach { (ox, oy) ->
                    canvas.drawCircle(cx + ox * R * 1.6f, cy + oy * R * 1.6f, S * 0.012f, paint)
                }
            }
            11 -> { // 黑曜星
                glow(canvas, paint, cx, cy, R * 1.25f, GOLD, 0.42f + 0.1f * sin(twoPi * phase))
                paint.color = GOLD; paint.style = Paint.Style.STROKE; paint.strokeWidth = R * 0.12f
                canvas.drawCircle(cx, cy, R * 0.62f, paint)
                paint.color = tint(WHITE, 0.55f); paint.strokeWidth = 1.6f
                canvas.drawCircle(cx, cy, R * 0.5f, paint)
                paint.style = Paint.Style.FILL; paint.color = BG
                canvas.drawCircle(cx, cy, R * 0.48f, paint)
            }
            12 -> { // 花冠星
                val sway = sin(twoPi * phase) * 4f
                for (i in 0 until 8) {
                    val ang = Math.toRadians((i * 45 + sway).toDouble())
                    glow(
                        canvas, paint,
                        cx + (R * 0.52 * sin(ang)).toFloat(), cy - (R * 0.52 * cos(ang)).toFloat(),
                        R * 0.55f, if (i % 2 == 0) PINK else VIOLET, 0.30f
                    )
                }
                glow(canvas, paint, cx, cy, R * 0.7f, WHITE, 0.85f)
                paint.color = ROSE; canvas.drawCircle(cx, cy, R * 0.16f, paint)
            }
            13 -> { // 棱星
                val a = 0.75f + 0.25f * sin(twoPi * phase * 1.2f)
                paint.alpha = (a * 255).toInt()
                paint.shader = LinearGradient(cx - R, cy - R, cx + R, cy + R, RAINBOW, null, Shader.TileMode.CLAMP)
                canvas.drawPath(star4(cx, cy, R * 1.05f), paint)
                paint.shader = null; paint.alpha = 255
                paint.strokeWidth = 2f
                listOf(Triple(0f, -1f, RAINBOW[0]), Triple(1f, 0f, RAINBOW[3]), Triple(0f, 1f, RAINBOW[2]), Triple(-1f, 0f, RAINBOW[5])).forEach { (dx, dy, col) ->
                    paint.shader = LinearGradient(cx, cy, cx + dx * R * 1.45f, cy + dy * R * 1.45f, tint(col, 0.6f * a), 0x00000000, Shader.TileMode.CLAMP)
                    canvas.drawLine(cx, cy, cx + dx * R * 1.45f, cy + dy * R * 1.45f, paint)
                }
                paint.shader = null
            }
            14 -> { // 月痕星
                glow(canvas, paint, cx, cy, R * 1.3f, GOLD, 0.45f)
                paint.shader = LinearGradient(cx - R, cy - R, cx + R, cy + R, PALE_GOLD, AMBER, Shader.TileMode.CLAMP)
                canvas.drawCircle(cx, cy, R * 0.72f, paint); paint.shader = null
                paint.color = BG; canvas.drawCircle(cx - R * 0.34f, cy - R * 0.26f, R * 0.64f, paint)
                paint.color = tint(WHITE, 0.95f)
                canvas.drawCircle(cx + R * 0.62f, cy - R * 0.5f, S * 0.018f, paint)
                canvas.drawCircle(cx + R * 0.82f, cy + R * 0.1f, S * 0.013f, paint)
            }
            else -> { // 冰晶星
                glow(canvas, paint, cx, cy, R * 1.2f, ICE, 0.30f)
                canvas.save(); canvas.rotate(phase * 360f, cx, cy)
                paint.color = tint(ICE, 0.10f); canvas.drawPath(hex(cx, cy, R), paint)
                paint.style = Paint.Style.STROKE; paint.strokeWidth = 2.4f; paint.color = ICE
                canvas.drawPath(hex(cx, cy, R), paint)
                paint.color = tint(WHITE, 0.35f); paint.strokeWidth = 1.1f
                for (i in 0 until 6) {
                    val ang = -Math.PI / 2 + i * Math.PI / 3
                    canvas.drawLine(cx, cy, cx + (R * cos(ang)).toFloat(), cy + (R * sin(ang)).toFloat(), paint)
                }
                paint.color = tint(CYAN, 0.8f); paint.strokeWidth = 1.4f
                canvas.drawPath(hex(cx, cy, R * 0.5f, 0f), paint)
                canvas.restore(); paint.style = Paint.Style.FILL
            }
        }
    }
}
