package com.ticka.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ticka.app.data.DayUtils
import com.ticka.app.data.I18n
import com.ticka.app.data.Habit
import com.ticka.app.ui.theme.Amber
import com.ticka.app.ui.theme.Panel
import com.ticka.app.ui.theme.Red
import com.ticka.app.ui.theme.TextDim
import com.ticka.app.ui.theme.TextFaint
import com.ticka.app.ui.theme.TextMain
import com.ticka.app.ui.theme.habitPalette
import java.time.LocalDate

@Composable
fun HabitsScreen(
    habits: List<Habit>,
    onAdd: () -> Unit,
    onToggle: (String) -> Unit,
    onOpen: (Habit) -> Unit,
    onDelete: (String) -> Unit
) {
    var deleteCandidate by remember { mutableStateOf<Habit?>(null) }
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                AppTitle(I18n.goals)
                Text(
                    DayUtils.todayLine(),
                    color = TextFaint, fontSize = 12.5.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            GradientButton(I18n.newGoal, icon = Icons.Rounded.Add, onClick = onAdd)
        }

        if (habits.isEmpty()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = I18n.noGoals,
                    sub = I18n.noGoalsSub
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(habits, key = { it.id }) { habit ->
                    HabitCard(
                        habit,
                        onToggle = onToggle,
                        onOpen = onOpen,
                        onLongPress = { deleteCandidate = habit }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    deleteCandidate?.let { habit ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text(I18n.deleteGoalQ, color = TextMain, fontWeight = FontWeight.Bold) },
            text = { Text(I18n.deleteGoalBody(habit.name), color = TextDim) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    onDelete(habit.id)
                    deleteCandidate = null
                }) { Text(I18n.delete, color = Red, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { deleteCandidate = null }) { Text(I18n.cancel, color = TextDim) }
            },
            containerColor = Panel
        )
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    onToggle: (String) -> Unit,
    onOpen: (Habit) -> Unit,
    onLongPress: () -> Unit
) {
    val palette = habitPalette(habit.palette)
    val today = DayUtils.tickaDate()
    val scheduledToday = DayUtils.scheduled(habit, today)
    val doneToday = habit.records.containsKey(DayUtils.todayKey())
    val streak = DayUtils.currentStreak(habit)

    GlassCard(
        Modifier.fillMaxWidth(),
        onClick = { onOpen(habit) },
        onLongClick = onLongPress,
        tint = Brush.linearGradient(palette.map { it.copy(alpha = 0.17f) })
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Brush.horizontalGradient(palette))
        )
        Column(Modifier.padding(horizontal = 16.dp, vertical = 15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(9.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.linearGradient(palette))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    habit.name,
                    color = TextMain, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                if (scheduledToday) I18n.dayN(DayUtils.dayNumber(habit.createdAt)) else I18n.notScheduledToday,
                color = TextFaint, fontSize = 11.5.sp,
                modifier = Modifier.padding(top = 5.dp, bottom = 13.dp)
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StreakBadge(streak)
                CheckCircle(
                    size = 46.dp,
                    checked = doneToday,
                    colors = palette,
                    onClick = { if (scheduledToday) onToggle(habit.id) }
                )
            }
        }
    }
}

@Composable
fun StreakBadge(streak: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            Icons.Rounded.LocalFireDepartment, null,
            tint = if (streak > 0) Amber else TextFaint,
            modifier = Modifier.size(15.dp)
        )
        Text(
            if (streak > 0) "$streak" else "0",
            color = if (streak > 0) Amber else TextFaint,
            fontWeight = FontWeight.Bold, fontSize = 14.sp
        )
        Text(
            I18n.daysStreak, color = TextFaint, fontSize = 11.sp,
            modifier = Modifier.padding(start = 0.dp)
        )
    }
}
