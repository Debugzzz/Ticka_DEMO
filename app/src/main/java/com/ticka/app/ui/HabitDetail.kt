package com.ticka.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ticka.app.data.DayUtils
import com.ticka.app.data.I18n
import com.ticka.app.data.Habit
import com.ticka.app.ui.theme.Panel
import com.ticka.app.ui.theme.Red
import com.ticka.app.ui.theme.Stroke
import com.ticka.app.ui.theme.StrokeHi
import com.ticka.app.ui.theme.TextDim
import com.ticka.app.ui.theme.TextFaint
import com.ticka.app.ui.theme.TextMain
import com.ticka.app.ui.theme.habitPalette
import java.time.LocalDate
import java.util.UUID

/** 目标详情：开始日期 / 连续统计 / 12 周热力图 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetail(
    habit: Habit,
    onDismiss: () -> Unit,
    onEdit: (Habit) -> Unit,
    onDelete: () -> Unit,
    onToggleToday: () -> Unit
) {
    val palette = habitPalette(habit.palette)
    val today = DayUtils.tickaDate()
    val todayKey = DayUtils.todayKey()
    val scheduledToday = DayUtils.scheduled(habit, today)
    val doneToday = habit.records.containsKey(todayKey)
    val current = DayUtils.currentStreak(habit)
    val longest = DayUtils.longestStreak(habit)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        contentColor = TextMain
    ) {
        Column(
            Modifier
                .padding(horizontal = 22.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(bottom = 30.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Brush.linearGradient(palette))
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    habit.name,
                    fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onEdit(habit) }) {
                    Icon(Icons.Rounded.Edit, "编辑", tint = TextDim)
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(I18n.currentStreakL, num = "$current", numColor = palette, modifier = Modifier.weight(1f))
                StatTile(I18n.longestStreakL, num = "$longest", numColor = palette, modifier = Modifier.weight(1f))
                StatTile(I18n.totalDone, num = "${habit.records.size}", numColor = palette, modifier = Modifier.weight(1f))
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(I18n.startDateL, value = DayUtils.startDate(habit.createdAt), modifier = Modifier.weight(2f))
                StatTile(I18n.daysKept, num = "${DayUtils.dayNumber(habit.createdAt)}", numColor = palette, modifier = Modifier.weight(1f))
            }

            SectionLabel(I18n.last12w, Modifier.padding(top = 20.dp, bottom = 10.dp))
            Heatmap(habit, palette)

            Text(
                I18n.weekly + DayUtils.weekdayNames(habit.weekdays),
                color = TextFaint, fontSize = 12.5.sp,
                modifier = Modifier.padding(top = 14.dp)
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CheckCircle(
                    size = 40.dp,
                    checked = doneToday,
                    colors = palette,
                    onClick = onToggleToday
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        when {
                            doneToday -> I18n.doneTodayL
                            scheduledToday -> I18n.checkTodayL
                            else -> I18n.offScheduleL
                        },
                        color = if (scheduledToday || doneToday) TextMain else TextFaint,
                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp
                    )
                    if (streakHint(current)) {
                        Text(
                            I18n.streakHint(current),
                            color = TextFaint, fontSize = 11.5.sp
                        )
                    }
                }
                GhostButton(I18n.deleteGoalBtn, danger = true, onClick = onDelete)
            }
        }
    }
}

private fun streakHint(current: Int) = current > 0

/** 12 周热力图：周一为每列首行 */
@Composable
private fun Heatmap(habit: Habit, palette: List<Color>) {
    val today = DayUtils.tickaDate()
    val thisMonday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    val weeks = (11 downTo 0).map { thisMonday.minusWeeks(it.toLong()) }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(3.5.dp)) {
        items(weeks.size) { w ->
            val weekStart = weeks[w]
            Column(verticalArrangement = Arrangement.spacedBy(3.5.dp)) {
                (0..6).forEach { d ->
                    val date = weekStart.plusDays(d.toLong())
                    HeatCell(habit, date, today, palette)
                }
            }
        }
    }
}

@Composable
private fun HeatCell(habit: Habit, date: LocalDate, today: LocalDate, palette: List<Color>) {
    val future = date.isAfter(today)
    val scheduled = DayUtils.scheduled(habit, date)
    val done = habit.records.containsKey(DayUtils.keyOf(date))
    val miss = scheduled && !done && date.isBefore(today)
    val shape = RoundedCornerShape(3.5.dp)
    val base = Modifier.size(13.dp).clip(shape)
    when {
        future -> Box(base.background(Color(0x05FFFFFF)))
        done -> Box(
            base
                .background(Brush.linearGradient(palette))
        )
        miss -> Box(base.border(1.dp, Color(0x4DFB7185), shape))
        scheduled -> Box(base.background(Color(0x0DFFFFFF)))
        else -> Box(base.background(Color(0x05FFFFFF)))
    }
}

/** 目标编辑器 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEditor(
    initial: Habit?,
    onDismiss: () -> Unit,
    onSave: (Habit) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isNew = initial == null
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var paletteIdx by remember { mutableStateOf(initial?.palette ?: 0) }
    var weekdays by remember { mutableStateOf(initial?.weekdays ?: setOf(1, 2, 3, 4, 5, 6, 7)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        contentColor = TextMain
    ) {
        Column(
            Modifier
                .padding(horizontal = 22.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(bottom = 28.dp)
        ) {
            Text(
                if (isNew) "新目标" else "编辑目标",
                fontWeight = FontWeight.Bold, fontSize = 17.sp
            )

            SectionLabel("名称", Modifier.padding(top = 18.dp, bottom = 7.dp))
            TickaTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "每天坚持的事，如：阅读 30 分钟"
            )

            SectionLabel("配色", Modifier.padding(top = 14.dp, bottom = 9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                com.ticka.app.ui.theme.habitPalettes.forEachIndexed { index, colors ->
                    val selected = paletteIdx == index
                    Box(
                        Modifier
                            .size(width = 42.dp, height = 28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(colors))
                            .border(
                                2.dp,
                                if (selected) Color.White else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { paletteIdx = index }
                    )
                }
            }

            SectionLabel("重复", Modifier.padding(top = 16.dp, bottom = 9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("一", "二", "三", "四", "五", "六", "日").forEachIndexed { i, label ->
                    val day = i + 1
                    val selected = day in weekdays
                    Text(
                        label,
                        color = if (selected) Color.White else TextDim,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) Brush.linearGradient(com.ticka.app.ui.theme.GradMain)
                                else androidx.compose.ui.graphics.SolidColor(Color(0x0BFFFFFF))
                            )
                            .border(
                                1.dp,
                                if (selected) Color.Transparent else Stroke,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                weekdays = if (selected) (weekdays - day).ifEmpty { weekdays }
                                else weekdays + day
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 26.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isNew && onDelete != null) {
                    GhostButton("删除", danger = true, onClick = onDelete)
                }
                Spacer(Modifier.weight(1f))
                GhostButton("取消", onClick = onDismiss)
                GradientButton(
                    "保存",
                    onClick = {
                        onSave(
                            Habit(
                                id = initial?.id ?: UUID.randomUUID().toString(),
                                name = name.trim(),
                                palette = paletteIdx,
                                weekdays = weekdays.sorted().toSet(),
                                createdAt = initial?.createdAt ?: System.currentTimeMillis(),
                                records = initial?.records ?: emptyMap()
                            )
                        )
                    }
                )
            }
        }
    }
}
