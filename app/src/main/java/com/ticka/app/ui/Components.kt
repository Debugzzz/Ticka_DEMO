package com.ticka.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ticka.app.data.CountdownState
import com.ticka.app.ui.theme.Bg
import com.ticka.app.ui.theme.Glass
import com.ticka.app.ui.theme.GradMain
import com.ticka.app.ui.theme.Indigo
import com.ticka.app.ui.theme.Stroke
import com.ticka.app.ui.theme.StrokeHi
import com.ticka.app.ui.theme.TextDim
import com.ticka.app.ui.theme.TextFaint
import com.ticka.app.ui.theme.TextMain
import com.ticka.app.ui.theme.Violet
import com.ticka.app.ui.theme.Amber
import com.ticka.app.ui.theme.Red
import com.ticka.app.ui.theme.Cyan
import com.ticka.app.ui.theme.Green

/** 彩色辉光：绘制在组件背后 */
fun Modifier.glow(color: Color, alpha: Float = 0.38f, scale: Float = 1.15f): Modifier =
    drawBehind {
        val radius = size.minDimension * scale
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = alpha), Color.Transparent),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = radius
            ),
            radius = radius,
            center = Offset(size.width / 2f, size.height / 2f)
        )
    }

/** 玻璃卡片容器（可叠加一层半透明渐变染色，支持点击与长按） */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    border: Color = Stroke,
    tint: Brush? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickMod = when {
        onClick != null && onLongClick != null ->
            Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
        onClick != null -> Modifier.clickable(onClick = onClick)
        else -> Modifier
    }
    val base = modifier
        .clip(shape)
        .background(Glass)
        .then(if (tint != null) Modifier.background(tint) else Modifier)
        .border(1.dp, border, shape)
        .then(clickMod)
    Column(base, content = content)
}

/** 渐变主按钮 */
@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    large: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(if (large) 13.dp else 10.dp))
            .background(Brush.linearGradient(GradMain))
            .glow(Indigo, 0.32f)
            .clickable(onClick = onClick)
            .padding(horizontal = if (large) 28.dp else 16.dp, vertical = if (large) 13.dp else 9.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(if (large) 18.dp else 14.dp))
        }
        Text(
            text, color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = if (large) 14.sp else 13.sp
        )
    }
}

/** 玻璃描边按钮 */
@Composable
fun GhostButton(
    text: String,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Glass)
            .border(1.dp, if (danger) Color(0x59FB7185) else StrokeHi, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text, color = if (danger) Red else TextDim,
            fontWeight = FontWeight.SemiBold, fontSize = 13.sp
        )
    }
}

/** 圆形勾选：未勾玻璃描边，勾选渐变填充 + 辉光 + 对勾 */
@Composable
fun CheckCircle(
    size: Dp,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: List<Color> = GradMain
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (checked) Modifier
                    .background(Brush.linearGradient(colors))
                    .glow(colors[1], 0.45f)
                else Modifier.background(Color(0x08FFFFFF))
            )
            .border(1.6.dp, if (checked) Color.Transparent else StrokeHi, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                Icons.Rounded.Check, null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}

/** 倒计时胶囊 */
@Composable
fun CountdownChip(text: String, state: CountdownState, modifier: Modifier = Modifier) {
    val (bg, border, fg) = when (state) {
        CountdownState.NORMAL -> Triple(Color(0x1A22D3EE), Color(0x5222D3EE), Cyan)
        CountdownState.SOON -> Triple(Color(0x1AFBBF24), Color(0x59FBBF24), Amber)
        CountdownState.OVERDUE -> Triple(Color(0x1EFB7185), Color(0x66FB7185), Red)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(50))
            .padding(horizontal = 11.dp, vertical = 5.dp)
    ) {
        Text(
            text, color = fg, fontSize = 12.sp,
            fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
        )
    }
}

/** 玻璃小徽章 */
@Composable
fun Chip(text: String, modifier: Modifier = Modifier, color: Color = TextDim, icon: ImageVector? = null) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Glass)
            .border(1.dp, Stroke, RoundedCornerShape(50))
            .padding(horizontal = 9.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) Icon(icon, null, tint = color, modifier = Modifier.size(11.dp))
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** 详情统计块 */
@Composable
fun StatTile(
    label: String,
    num: String? = null,
    value: String? = null,
    numColor: List<Color> = GradMain,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Glass)
            .border(1.dp, Stroke, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp)
    ) {
        if (num != null) {
            Text(
                num, fontWeight = FontWeight.Bold, fontSize = 21.sp,
                fontFamily = FontFamily.Monospace,
                color = numColor[0]
            )
        } else if (value != null) {
            Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextMain)
        }
        Text(label, color = TextFaint, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
    }
}

/** 空状态 */
@Composable
fun EmptyState(title: String, sub: String, modifier: Modifier = Modifier) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .size(58.dp)
                .glow(Violet, 0.35f, 1.6f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x808B5CF6), Color(0x2422D3EE), Color.Transparent)
                    )
                )
                .border(1.dp, Stroke, CircleShape)
        )
        Text(title, color = TextDim, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(sub, color = TextFaint, fontSize = 12.5.sp)
    }
}

/** 章节小标题 */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text, modifier = modifier,
        color = TextDim, fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun AppTitle(text: String, modifier: Modifier = Modifier, size: TextUnit = 24.sp) {
    Text(
        text, modifier = modifier,
        color = TextMain, fontSize = size,
        fontWeight = FontWeight.Bold
    )
}
