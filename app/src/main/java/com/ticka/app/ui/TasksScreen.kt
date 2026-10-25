package com.ticka.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ticka.app.data.DayUtils
import com.ticka.app.data.I18n
import com.ticka.app.data.Task
import com.ticka.app.ui.theme.Glass
import com.ticka.app.ui.theme.GradMain
import com.ticka.app.ui.theme.Panel
import com.ticka.app.ui.theme.Red
import com.ticka.app.ui.theme.Stroke
import com.ticka.app.ui.theme.StrokeHi
import com.ticka.app.ui.theme.TextDim
import com.ticka.app.ui.theme.TextFaint
import com.ticka.app.ui.theme.TextMain

@Composable
fun TasksScreen(
    tasks: List<Task>,
    now: Long,
    onAdd: () -> Unit,
    onToggle: (String) -> Unit,
    onOpen: (Task) -> Unit,
    onFocus: (Task) -> Unit,
    onDelete: (String) -> Unit
) {
    var filter by remember { mutableStateOf("all") }
    var deleteCandidate by remember { mutableStateOf<Task?>(null) }
    val shown = when (filter) {
        "active" -> tasks.filter { !it.completed }
        "done" -> tasks.filter { it.completed }
        else -> tasks.sortedWith(compareBy({ it.completed }, { it.dueAt ?: Long.MAX_VALUE }))
    }

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
                AppTitle(I18n.todos)
                Text(
                    DayUtils.todayLine(),
                    color = TextFaint, fontSize = 12.5.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            GradientButton(I18n.newTodo, icon = Icons.Rounded.Add, onClick = onAdd)
        }

        SegmentedFilter(filter = filter, onSelect = { filter = it })

        if (shown.isEmpty()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = if (tasks.isEmpty()) I18n.emptyUniverse else I18n.emptyNoTodo,
                    sub = if (tasks.isEmpty()) I18n.emptyAddFirst else I18n.emptyTryFilter
                )
            }
        } else {
            LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(shown, key = { it.id }) { task ->
                    TaskCard(
                        task, now,
                        onToggle = onToggle,
                        onOpen = onOpen,
                        onFocus = onFocus,
                        onLongPress = { deleteCandidate = task }
                    )
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }

    deleteCandidate?.let { task ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text(I18n.deleteTodoQ, color = TextMain, fontWeight = FontWeight.Bold) },
            text = { Text(I18n.deleteTodoBody(task.title), color = TextDim) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(task.id)
                    deleteCandidate = null
                }) { Text(I18n.delete, color = Red, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) { Text(I18n.cancel, color = TextDim) }
            },
            containerColor = Panel
        )
    }
}

@Composable
private fun SegmentedFilter(filter: String, onSelect: (String) -> Unit) {
    Row(
        Modifier
            .padding(top = 18.dp, bottom = 16.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Glass)
            .border(1.dp, Stroke, RoundedCornerShape(11.dp))
            .padding(3.dp)
    ) {
        listOf("all" to I18n.filterAll, "active" to I18n.filterActive, "done" to I18n.filterDone).forEach { (key, label) ->
            val selected = filter == key
            Text(
                label,
                color = if (selected) Color.White else TextDim,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.5.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selected) Brush.linearGradient(GradMain)
                        else androidx.compose.ui.graphics.SolidColor(Color.Transparent)
                    )
                    .clickable { onSelect(key) }
                    .padding(horizontal = 15.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    now: Long,
    onToggle: (String) -> Unit,
    onOpen: (Task) -> Unit,
    onFocus: (Task) -> Unit,
    onLongPress: () -> Unit
) {
    val overdue = !task.completed && task.dueAt != null && task.dueAt < now
    GlassCard(
        Modifier.fillMaxWidth(),
        onClick = { onOpen(task) },
        onLongClick = onLongPress,
        border = if (overdue) StrokeHi else Stroke
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CheckCircle(
                size = 24.dp,
                checked = task.completed,
                onClick = { onToggle(task.id) }
            )
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    task.title,
                    color = if (task.completed) TextFaint else TextMain,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null
                )
                if (task.notes.isNotBlank()) {
                    Text(
                        task.notes,
                        color = TextFaint, fontSize = 12.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Row(
                    Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (task.dueAt != null) Chip("· ${DayUtils.dueText(task.dueAt)}")
                    if (task.pomodoros > 0) Chip("🍅 ×${task.pomodoros}")
                }
            }
            if (task.dueAt != null && !task.completed) {
                val (text, state) = DayUtils.countdown(task.dueAt, now)
                CountdownChip(text = text, state = state)
                Spacer(Modifier.width(6.dp))
            }
            if (!task.completed) {
                IconButton(onClick = { onFocus(task) }, modifier = Modifier.size(30.dp)) {
                    Icon(
                        Icons.Rounded.PlayArrow, "Focus",
                        tint = TextDim
                    )
                }
            }
        }
    }
}
