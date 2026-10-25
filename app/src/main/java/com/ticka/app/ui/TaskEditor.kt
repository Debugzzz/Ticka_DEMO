package com.ticka.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ticka.app.data.DayUtils
import com.ticka.app.data.I18n
import com.ticka.app.data.Task
import com.ticka.app.ui.theme.Cyan
import com.ticka.app.ui.theme.Glass
import com.ticka.app.ui.theme.GlassHi
import com.ticka.app.ui.theme.GradMain
import com.ticka.app.ui.theme.Indigo
import com.ticka.app.ui.theme.Panel
import com.ticka.app.ui.theme.Red
import com.ticka.app.ui.theme.Stroke
import com.ticka.app.ui.theme.StrokeHi
import com.ticka.app.ui.theme.TextDim
import com.ticka.app.ui.theme.TextFaint
import com.ticka.app.ui.theme.TextMain
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

/** 快速提前档位（分钟），-1 不提醒 / 0 截止时 */
private val REMIND_QUICK = listOf(-1, 0, 5, 30, 60, 1440)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditor(
    initial: Task?,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isNew = initial == null
    val initialTime = initial?.dueAt?.let {
        Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault())
    }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var dueDate by remember { mutableStateOf(initial?.dueAt?.let { DayUtils.localDate(it) }) }
    var hour by remember { mutableStateOf(initialTime?.hour ?: 18) }
    var minute by remember { mutableStateOf(initialTime?.minute ?: 0) }
    var remindBefore by remember { mutableStateOf(initial?.remindBeforeMin ?: 0) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showRemindPicker by remember { mutableStateOf(false) }

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
                if (isNew) I18n.newTodo else I18n.editTodo,
                fontWeight = FontWeight.Bold, fontSize = 17.sp
            )

            SectionLabel(I18n.titleField, Modifier.padding(top = 18.dp, bottom = 7.dp))
            TickaTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = I18n.titlePlaceholder
            )

            SectionLabel(I18n.notesField, Modifier.padding(top = 14.dp, bottom = 7.dp))
            TickaTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = I18n.notesPlaceholder,
                singleLine = false
            )

            SectionLabel(I18n.dueTime, Modifier.padding(top = 14.dp, bottom = 7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val dateFmt = DateTimeFormatter.ofPattern(if (I18n.en) "MMM d (EEE)" else "M月d日 EEEE", if (I18n.en) Locale.US else Locale.CHINESE)
                Pill(
                    text = dueDate?.format(dateFmt) ?: I18n.pickDate,
                    active = dueDate != null,
                    modifier = Modifier.weight(1f)
                ) { showDatePicker = true }
                Pill(
                    text = "%02d:%02d".format(hour, minute),
                    active = dueDate != null,
                    modifier = Modifier.weight(1f)
                ) { if (dueDate == null) dueDate = LocalDate.now(); showTimePicker = true }
                if (dueDate != null) {
                    Pill(text = "✕", active = false) { dueDate = null }
                }
            }

            SectionLabel(I18n.remind, Modifier.padding(top = 14.dp, bottom = 7.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val customSelected = remindBefore > 0 && REMIND_QUICK.none { it == remindBefore }
                val chips: List<Triple<Int, String, Boolean>> =
                    REMIND_QUICK.map { min ->
                        val label = when (min) {
                            -1 -> I18n.remindOff
                            0 -> I18n.remindDue
                            else -> I18n.remindQuick(min)
                        }
                        Triple(min, label, remindBefore == min)
                    } + Triple(
                        Int.MAX_VALUE,
                        if (customSelected) "${I18n.remindCustom.dropLast(1)} ${I18n.advance(remindBefore)}…" else I18n.remindCustom,
                        customSelected
                    )
                chips.forEach { (min, label, selected) ->
                    Text(
                        label,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) Color.White else TextDim,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (selected) Brush.linearGradient(GradMain)
                                else androidx.compose.ui.graphics.SolidColor(Glass)
                            )
                            .border(1.dp, if (selected) Color.Transparent else Stroke, RoundedCornerShape(50))
                            .clickable {
                                if (min == Int.MAX_VALUE) showRemindPicker = true
                                else remindBefore = min
                            }
                            .padding(horizontal = 13.dp, vertical = 7.dp)
                    )
                }
            }
            // 常驻提示行（高度固定，避免选项区上下跳动）
            Text(
                when {
                    dueDate == null -> I18n.hintNeedDue
                    remindBefore > 0 -> I18n.hintBefore(I18n.advance(remindBefore))
                    remindBefore == 0 -> I18n.hintAtDue
                    else -> I18n.hintOff
                },
                fontSize = 11.5.sp, color = TextFaint,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .heightIn(min = 17.dp)
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isNew && onDelete != null) {
                    GhostButton(I18n.delete, danger = true, onClick = onDelete)
                }
                Spacer(Modifier.weight(1f))
                GhostButton(I18n.cancel, onClick = onDismiss)
                GradientButton(
                    I18n.save,
                    onClick = {
                        val dueAt = dueDate?.let { DayUtils.toEpochMs(it, hour, minute) }
                        val remindAt =
                            if (dueAt != null && remindBefore >= 0) dueAt - remindBefore * 60_000L
                            else null
                        onSave(
                            Task(
                                id = initial?.id ?: UUID.randomUUID().toString(),
                                title = title.trim(),
                                notes = notes.trim(),
                                dueAt = dueAt,
                                remindBeforeMin = if (dueAt == null) -1 else remindBefore,
                                remindAt = remindAt,
                                remindFired = false,
                                completed = initial?.completed ?: false,
                                completedAt = initial?.completedAt,
                                pomodoros = initial?.pomodoros ?: 0,
                                starId = initial?.starId,
                                createdAt = initial?.createdAt ?: System.currentTimeMillis()
                            )
                        )
                    }
                )
            }
        }
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        dueDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text(I18n.ok, color = Cyan, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(I18n.cancel, color = TextDim) }
            },
            colors = DatePickerDefaults.colors(containerColor = Panel)
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    hour = timeState.hour
                    minute = timeState.minute
                    showTimePicker = false
                }) { Text(I18n.ok, color = Cyan, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(I18n.cancel, color = TextDim) }
            },
            containerColor = Panel,
            text = { TimePicker(state = timeState) }
        )
    }

    if (showRemindPicker) {
        val initTotal = remindBefore.coerceAtLeast(0)
        var pd by remember { mutableStateOf(initTotal / 1440) }
        var ph by remember { mutableStateOf(initTotal % 1440 / 60) }
        var pm by remember { mutableStateOf(initTotal % 60) }
        val valid = pd + ph + pm > 0
        AlertDialog(
            onDismissRequest = { showRemindPicker = false },
            title = { Text(I18n.remindPickerTitle, color = TextMain, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    StepperRow(I18n.unitDays, pd) { pd = (pd + it).coerceIn(0, 30) }
                    StepperRow(I18n.unitHours, ph) { ph = (ph + it).coerceIn(0, 23) }
                    StepperRow(I18n.unitMinutes, pm) { pm = (pm + it).coerceIn(0, 59) }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = valid,
                    onClick = {
                        remindBefore = pd * 1440 + ph * 60 + pm
                        showRemindPicker = false
                    }
                ) {
                    Text(
                        I18n.ok,
                        color = if (valid) Cyan else TextFaint,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemindPicker = false }) { Text(I18n.cancel, color = TextDim) }
            },
            containerColor = Panel
        )
    }
}

/** 天/小时/分钟步进选择行 */
@Composable
private fun StepperRow(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextDim, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GhostButton("−") { onChange(-1) }
            Text(
                "$value",
                color = TextMain, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                modifier = Modifier.widthIn(min = 30.dp),
                textAlign = TextAlign.Center
            )
            GhostButton("＋") { onChange(1) }
        }
    }
}

/** 玻璃药丸按钮（日期/时间选择） */
@Composable
private fun Pill(
    text: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Text(
        text,
        color = if (active) TextMain else TextFaint,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (active) GlassHi else Glass)
            .border(1.dp, if (active) StrokeHi else Stroke, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    )
}

@Composable
fun TickaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextFaint) },
        singleLine = singleLine,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.fillMaxWidth(),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextMain,
            unfocusedTextColor = TextMain,
            focusedBorderColor = Indigo,
            unfocusedBorderColor = Stroke,
            focusedContainerColor = Glass,
            unfocusedContainerColor = Glass,
            cursorColor = Cyan,
            focusedPlaceholderColor = TextFaint,
            unfocusedPlaceholderColor = TextFaint
        )
    )
}
