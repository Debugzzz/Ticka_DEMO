package com.ticka.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ticka.app.data.I18n
import com.ticka.app.data.Task
import com.ticka.app.notify.ReminderScheduler
import com.ticka.app.ui.theme.Amber
import com.ticka.app.ui.theme.Cyan
import com.ticka.app.ui.theme.Glass
import com.ticka.app.ui.theme.GradMain
import com.ticka.app.ui.theme.Green
import com.ticka.app.ui.theme.Indigo
import com.ticka.app.ui.theme.Panel
import com.ticka.app.ui.theme.StrokeHi
import com.ticka.app.ui.theme.TextDim
import com.ticka.app.ui.theme.TextFaint
import kotlinx.coroutines.delay

private const val FOCUS_LEN = 25 * 60
private const val BREAK_LEN = 5 * 60

/** 番茄钟专注浮层：25 分钟专注 + 5 分钟休息 */
@Composable
fun PomodoroDialog(
    task: Task,
    onDismiss: () -> Unit,
    onFinishedFocus: () -> String
) {
    var isBreak by remember { mutableStateOf(false) }
    var remaining by remember { mutableStateOf(FOCUS_LEN) }
    var running by remember { mutableStateOf(false) }
    var starFlash by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(starFlash) {
        if (starFlash != null) {
            delay(2600)
            starFlash = null
        }
    }

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        while (true) {
            delay(1000)
            remaining -= 1
            if (remaining <= 0) {
                if (!isBreak) {
                    val starName = onFinishedFocus()
                    ReminderScheduler.notifyNow(
                        context, I18n.focusDoneTitle(),
                        I18n.focusDoneBody(task.title, starName)
                    )
                    com.ticka.app.notify.AlarmPlayer.play(context, 5L)
                    starFlash = starName
                    isBreak = true
                    remaining = BREAK_LEN
                } else {
                    ReminderScheduler.notifyNow(context, I18n.breakOverTitle, I18n.backToFocus)
                    isBreak = false
                    remaining = FOCUS_LEN
                }
                running = false
                break
            }
        }
    }

    val total = if (isBreak) BREAK_LEN else FOCUS_LEN
    val ringColors = if (isBreak) listOf(Green, Cyan) else GradMain

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val noRipple = remember { MutableInteractionSource() }
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xC704040E))
                .clickable(interactionSource = noRipple, indication = null) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xD9101024))
                    .border(1.dp, StrokeHi, RoundedCornerShape(24.dp))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                    .padding(horizontal = 40.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    task.title,
                    color = TextDim, fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 22.dp)
                )

                Box(contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(216.dp)) {
                        val stroke = 9.dp.toPx()
                        val glowStroke = stroke * 2.8f
                        val inset = glowStroke / 2f + 1f
                        val diameter = size.minDimension - inset * 2
                        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                        val arcSize = Size(diameter, diameter)
                        drawArc(
                            color = Color.White.copy(alpha = 0.07f),
                            startAngle = -90f, sweepAngle = 360f, useCenter = false,
                            topLeft = topLeft, size = arcSize,
                            style = Stroke(stroke, cap = StrokeCap.Round)
                        )
                        val frac = 1f - remaining.toFloat() / total
                        if (frac > 0.002f) {
                            val brush = Brush.sweepGradient(
                                listOf(ringColors[0], ringColors[1], ringColors[0]),
                                center
                            )
                            drawArc(
                                brush = brush, alpha = 0.22f,
                                startAngle = -90f, sweepAngle = 360f * frac, useCenter = false,
                                topLeft = topLeft, size = arcSize,
                                style = Stroke(glowStroke, cap = StrokeCap.Round)
                            )
                            drawArc(
                                brush = brush,
                                startAngle = -90f, sweepAngle = 360f * frac, useCenter = false,
                                topLeft = topLeft, size = arcSize,
                                style = Stroke(stroke, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (isBreak) I18n.brk else I18n.focus,
                            color = TextFaint, fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(letterSpacing = 6.sp)
                        )
                        Text(
                            "%02d:%02d".format(remaining / 60, remaining % 60),
                            style = TextStyle(
                                brush = Brush.linearGradient(ringColors),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 46.sp
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                starFlash?.let {
                    Text(
                        "✨ $it",
                        color = Amber, fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                Row(
                    Modifier.padding(top = 28.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GhostButton(I18n.reset) {
                        running = false
                        remaining = if (isBreak) BREAK_LEN else FOCUS_LEN
                    }
                    GradientButton(
                        if (running) I18n.pause else I18n.start,
                        large = true,
                        icon = if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow
                    ) { running = !running }
                    GhostButton(I18n.exit, onClick = onDismiss)
                }
            }
        }
    }
}
