package com.ticka.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ticka.app.ui.theme.Amber
import com.ticka.app.ui.theme.Cyan
import com.ticka.app.ui.theme.Green
import com.ticka.app.ui.theme.Indigo
import com.ticka.app.ui.theme.Pink
import com.ticka.app.ui.theme.Violet
import kotlin.math.cos
import kotlin.math.sin

private fun star4Path(cx: Float, cy: Float, r: Float, innerRatio: Float = 0.30f): Path {
    val path = Path()
    for (i in 0 until 8) {
        val ang = -Math.PI / 2 + i * Math.PI / 4
        val rad = if (i % 2 == 0) r else r * innerRatio
        val x = cx + (rad * cos(ang)).toFloat()
        val y = cy + (rad * sin(ang)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun hexPath(cx: Float, cy: Float, r: Float, startAngle: Float = (-Math.PI / 2).toFloat()): Path {
    val path = Path()
    for (i in 0 until 6) {
        val a = (startAngle + i * Math.PI / 3).toFloat()
        val x = cx + (r * cos(a)).toFloat()
        val y = cy + (r * sin(a)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun glow(color: Color, alpha: Float): Brush =
    Brush.radialGradient(listOf(color.copy(alpha = alpha), color.copy(alpha = 0f)))

// 额外色板
private val Orange = Color(0xFFFB923C)
private val Ice = Color(0xFFA5F3FC)
private val IceDeep = Color(0xFF67E8F9)
private val Gold = Color(0xFFFDE68A)
private val GoldDeep = Color(0xFFF59E0B)
private val Cream = Color(0xFFFFFBEB)
private val Blue = Color(0xFF3B82F6)
private val Rose = Color(0xFFFDF2F8)
private val PaleGold = Color(0xFFFEF9C3)
private val Rainbow = listOf(
    Color(0xFFF87171), Color(0xFFFBBF24), Color(0xFF34D399),
    Color(0xFF22D3EE), Color(0xFF818CF8), Color(0xFFE879F9)
)

/** 各星星动态周期（毫秒），0 = 静态 */
private val kindDuration = intArrayOf(
    0, 3000, 4000, 0, 0, 0,       // 尘星/辉星/焰星/双子/超新星/彗星
    6000, 0, 2100, 1600,          // 极光/星环/钻石/脉冲
    7000, 4200, 5000, 2800, 0, 8000 // 星云/黑曜/花冠/棱星/月痕/冰晶
)

/** 各种类在星空画布/背景上的尺寸（kind 0-15，传说/史诗更大） */
val starSkySizes = listOf(
    9.dp, 15.dp, 17.dp, 19.dp, 26.dp, 24.dp,        // 稀有：尘/辉/焰/双子/超新星/彗星
    26.dp, 22.dp, 24.dp, 24.dp,                      // 极光/星环/钻石/脉冲
    30.dp, 26.dp, 24.dp, 26.dp, 24.dp, 22.dp         // 星云/黑曜/花冠/棱星/月痕/冰晶
)

/**
 * 星星图腾：16 种画法，kind 0-15；animate = true 时带闪烁/流动/旋转等动态
 */
@Composable
fun StarGlyph(kind: Int, size: Dp, modifier: Modifier = Modifier, dim: Boolean = false, animate: Boolean = false) {
    val duration = kindDuration.getOrElse(kind) { 0 }
    val phase: Float
    if (animate && duration > 0) {
        val transition = rememberInfiniteTransition(label = "star$kind")
        val p by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(duration, easing = LinearEasing), RepeatMode.Restart),
            label = "phase$kind"
        )
        phase = p
    } else {
        phase = 0f
    }
    val stagger = remember(kind) { kotlin.random.Random.nextFloat() }
    Canvas(modifier.size(size).alpha(if (dim) 0.15f else 1f)) {
        drawStarKind(kind, (phase + stagger) % 1f)
    }
}

private fun DrawScope.drawStarKind(kind: Int, phase: Float) {
    val c = Offset(size.width / 2f, size.height / 2f)
    val R = size.minDimension * 0.36f
    val S = size.minDimension
    val twoPi = (2 * Math.PI).toFloat()
    val tw = 0.7f + 0.3f * sin(twoPi * phase)
    when (kind) {
        0 -> { // 尘星
            drawCircle(glow(TextGlow, 0.45f), radius = R, center = c)
            drawCircle(Color(0xFFF5F6FF), radius = R * 0.30f, center = c)
        }
        1 -> { // 辉星
            drawCircle(glow(Cyan, 0.35f + 0.15f * sin(twoPi * phase)), radius = R * 1.15f, center = c)
            drawPath(star4Path(c.x, c.y, R, 0.30f), Cyan)
        }
        2 -> { // 焰星
            drawCircle(glow(Amber, 0.40f + 0.10f * sin(twoPi * phase)), radius = R * 1.2f, center = c)
            drawPath(
                star4Path(c.x, c.y, R, 0.30f),
                Brush.linearGradient(listOf(Amber, Orange), start = Offset(c.x - R, c.y - R), end = Offset(c.x + R, c.y + R))
            )
            drawCircle(Gold, radius = R * 0.10f, center = c)
        }
        3 -> { // 双子星
            val dx = R * 0.52f
            val a = Offset(c.x - dx, c.y + R * 0.25f)
            val b = Offset(c.x + dx, c.y - R * 0.25f)
            drawLine(Color(0x66FFFFFF), a, b, strokeWidth = S * 0.012f)
            drawCircle(glow(Violet, 0.5f), radius = R * 0.8f, center = a)
            drawCircle(glow(Pink, 0.5f), radius = R * 0.8f, center = b)
            drawPath(star4Path(a.x, a.y, R * 0.62f, 0.30f), Violet)
            drawPath(star4Path(b.x, b.y, R * 0.62f, 0.30f), Pink)
        }
        4 -> { // 超新星
            drawCircle(glow(Indigo, 0.5f), radius = R, center = c)
            for (i in 0 until 8) {
                val ang = i * Math.PI / 4
                drawLine(
                    Color(0xB3FFFFFF),
                    Offset(c.x + (R * 0.55 * sin(ang)).toFloat(), c.y - (R * 0.55 * cos(ang)).toFloat()),
                    Offset(c.x + (R * 0.95 * sin(ang)).toFloat(), c.y - (R * 0.95 * cos(ang)).toFloat()),
                    strokeWidth = S * 0.012f
                )
            }
            drawArc(
                Brush.sweepGradient(listOf(Indigo, Cyan, Indigo), c), -90f, 360f, false,
                style = Stroke(S * 0.02f),
                topLeft = Offset(c.x - R * 0.5f, c.y - R * 0.5f),
                size = Size(R, R)
            )
            drawCircle(Color.White, radius = R * 0.15f, center = c)
        }
        5 -> { // 彗星
            withTransform({ rotate(38f, c) }) {
                val tailLen = R * 1.9f
                val tail = Path().apply {
                    moveTo(c.x + R * 0.1f, c.y - R * 0.34f)
                    lineTo(c.x - tailLen, c.y)
                    lineTo(c.x + R * 0.1f, c.y + R * 0.34f)
                    close()
                }
                drawPath(
                    tail,
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Cyan.copy(alpha = 0.75f)),
                        startX = c.x - tailLen, endX = c.x
                    )
                )
                drawCircle(glow(Cyan, 0.55f), radius = R * 0.9f, center = c)
                drawCircle(Violet, radius = R * 0.34f, center = c, alpha = 0.6f)
                drawCircle(Color.White, radius = R * 0.22f, center = c)
            }
        }
        6 -> { // 极光星：三色光带流动
            val cols = listOf(Green, Cyan, Violet)
            cols.forEachIndexed { k, col ->
                val sway = sin(twoPi * phase + k * 1.4f) * R * 0.16f
                val path = Path().apply {
                    moveTo(c.x - R * 0.5f + sway, c.y - R * 1.15f)
                    cubicTo(
                        c.x + R * 0.55f + sway, c.y - R * 0.4f,
                        c.x - R * 0.6f + sway, c.y + R * 0.3f,
                        c.x + R * 0.45f + sway, c.y + R * 1.15f
                    )
                }
                drawPath(
                    path,
                    Brush.verticalGradient(
                        listOf(col.copy(alpha = 0f), col.copy(alpha = 0.75f), col.copy(alpha = 0f)),
                        startY = c.y - R * 1.15f, endY = c.y + R * 1.15f
                    ),
                    style = Stroke((R * 0.26f - k * R * 0.04f), cap = StrokeCap.Round)
                )
            }
            drawCircle(glow(Color.White, 0.8f), radius = R * 0.5f, center = Offset(c.x, c.y - R * 0.9f))
        }
        7 -> { // 星环星
            withTransform({ rotate(-22f, c) }) {
                drawOval(
                    Brush.linearGradient(listOf(Cyan, Amber), start = Offset(c.x - R * 1.05f, c.y), end = Offset(c.x + R * 1.05f, c.y)),
                    topLeft = Offset(c.x - R * 1.05f, c.y - R * 0.34f),
                    size = Size(R * 2.1f, R * 0.68f),
                    style = Stroke(R * 0.13f)
                )
                drawOval(
                    Color.White.copy(alpha = 0.25f),
                    topLeft = Offset(c.x - R * 1.28f, c.y - R * 0.44f),
                    size = Size(R * 2.56f, R * 0.88f),
                    style = Stroke(R * 0.04f)
                )
            }
            drawCircle(glow(Amber, 0.5f), radius = R * 1.1f, center = c)
            drawCircle(
                Brush.radialGradient(listOf(Cream, GoldDeep), center = Offset(c.x - R * 0.1f, c.y - R * 0.1f), radius = R * 0.36f),
                radius = R * 0.36f, center = c
            )
        }
        8 -> { // 钻石星
            drawCircle(glow(Ice, 0.35f * tw), radius = R * 0.8f, center = c)
            drawPath(
                star4Path(c.x, c.y, R * 1.1f, 0.09f),
                Brush.verticalGradient(listOf(Color.White, Ice), startY = c.y - R * 1.1f, endY = c.y + R * 1.1f),
                alpha = tw
            )
            withTransform({ rotate(45f, c) }) {
                drawPath(star4Path(c.x, c.y, R * 0.5f, 0.14f), Color.White.copy(alpha = 0.85f * tw))
            }
        }
        9 -> { // 脉冲星
            val pulse = 0.6f + 0.4f * sin(twoPi * phase)
            withTransform({ rotate(45f, c) }) {
                drawLine(
                    Brush.verticalGradient(
                        listOf(Cyan.copy(alpha = 0f), IceDeep.copy(alpha = 0.85f * pulse), Cyan.copy(alpha = 0f)),
                        startY = c.y - R * 1.3f, endY = c.y + R * 1.3f
                    ),
                    Offset(c.x, c.y - R * 1.3f), Offset(c.x, c.y + R * 1.3f),
                    strokeWidth = R * 0.12f * pulse + 1f,
                    cap = StrokeCap.Round
                )
            }
            drawCircle(glow(Cyan, 0.2f + 0.5f * pulse), radius = R * 0.9f, center = c)
            drawCircle(Color.White, radius = R * 0.20f, center = c)
        }
        10 -> { // 星云
            val blobs = listOf(
                listOf(Pink, -0.35f, -0.2f, 0.55f, 0.9f),
                listOf(Violet, 0.32f, 0.1f, 0.62f, 0.7f),
                listOf(Blue, -0.1f, 0.38f, 0.5f, 1.1f),
                listOf(IceDeep, 0.15f, -0.42f, 0.4f, 0.8f)
            )
            blobs.forEachIndexed { i, b ->
                val col = b[0] as Color
                val ox = b[1] as Float
                val oy = b[2] as Float
                val rr = b[3] as Float
                val sp = b[4] as Float
                val x = c.x + ox * R + sin(twoPi * phase * sp + i) * R * 0.06f
                val y = c.y + oy * R + cos(twoPi * phase * sp * 0.8f + i) * R * 0.06f
                drawCircle(glow(col, 0.30f), radius = R * rr, center = Offset(x, y))
            }
            listOf(-0.1f to -0.05f, 0.28f to 0.22f, -0.3f to 0.3f).forEach { (ox, oy) ->
                drawCircle(Color(0xF2FFFFFF), radius = S * 0.012f, center = Offset(c.x + ox * R * 1.6f, c.y + oy * R * 1.6f))
            }
        }
        11 -> { // 黑曜星
            drawCircle(glow(Gold, 0.42f + 0.1f * sin(twoPi * phase)), radius = R * 1.25f, center = c)
            drawCircle(Gold, radius = R * 0.62f, center = c, style = Stroke(R * 0.12f))
            drawCircle(Color(0x8CFFFFFF), radius = R * 0.5f, center = c, style = Stroke(1.6f))
            drawCircle(Color(0xFF070714), radius = R * 0.48f, center = c)
        }
        12 -> { // 花冠星
            val sway = sin(twoPi * phase) * 4f
            for (i in 0 until 8) {
                val ang = Math.toRadians((i * 45 + sway).toDouble())
                val x = c.x + (R * 0.52 * sin(ang)).toFloat()
                val y = c.y - (R * 0.52 * cos(ang)).toFloat()
                drawCircle(glow(if (i % 2 == 0) Pink else Violet, 0.30f), radius = R * 0.55f, center = Offset(x, y))
            }
            drawCircle(glow(Color.White, 0.85f), radius = R * 0.7f, center = c)
            drawCircle(Rose, radius = R * 0.16f, center = c)
        }
        13 -> { // 棱星
            val a = 0.75f + 0.25f * sin(twoPi * phase * 1.2f)
            drawPath(
                star4Path(c.x, c.y, R * 1.05f, 0.30f),
                Brush.linearGradient(Rainbow, start = Offset(c.x - R, c.y - R), end = Offset(c.x + R, c.y + R)),
                alpha = a
            )
            listOf(
                Triple(0f, -1f, Rainbow[0]), Triple(1f, 0f, Rainbow[3]),
                Triple(0f, 1f, Rainbow[2]), Triple(-1f, 0f, Rainbow[5])
            ).forEach { (dx, dy, col) ->
                drawLine(
                    Brush.linearGradient(listOf(col.copy(alpha = 0.6f * a), col.copy(alpha = 0f)), start = c, end = Offset(c.x + dx * R * 1.45f, c.y + dy * R * 1.45f)),
                    c, Offset(c.x + dx * R * 1.45f, c.y + dy * R * 1.45f),
                    strokeWidth = 2f
                )
            }
        }
        14 -> { // 月痕星
            drawCircle(glow(Gold, 0.45f), radius = R * 1.3f, center = c)
            drawCircle(
                Brush.linearGradient(listOf(PaleGold, Amber), start = Offset(c.x - R, c.y - R), end = Offset(c.x + R, c.y + R)),
                radius = R * 0.72f, center = c
            )
            drawCircle(Color(0xFF070714), radius = R * 0.64f, center = Offset(c.x - R * 0.34f, c.y - R * 0.26f))
            drawCircle(Color(0xF2FFFFFF), radius = S * 0.018f, center = Offset(c.x + R * 0.62f, c.y - R * 0.5f))
            drawCircle(Color(0xF2FFFFFF), radius = S * 0.013f, center = Offset(c.x + R * 0.82f, c.y + R * 0.1f))
        }
        else -> { // 15 冰晶星
            drawCircle(glow(Ice, 0.30f), radius = R * 1.2f, center = c)
            withTransform({ rotate(phase * 360f, c) }) {
                drawPath(hexPath(c.x, c.y, R), Ice.copy(alpha = 0.10f))
                drawPath(hexPath(c.x, c.y, R), Ice, style = Stroke(2.4f))
                for (i in 0 until 6) {
                    val ang = -Math.PI / 2 + i * Math.PI / 3
                    drawLine(
                        Color(0x59FFFFFF),
                        c,
                        Offset(c.x + (R * cos(ang)).toFloat(), c.y + (R * sin(ang)).toFloat()),
                        strokeWidth = 1.1f
                    )
                }
                drawPath(hexPath(c.x, c.y, R * 0.5f, startAngle = 0f), Cyan.copy(alpha = 0.8f), style = Stroke(1.4f))
            }
        }
    }
}

private val TextGlow = Color(0xFFEEF0FF)
