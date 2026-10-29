package com.ticka.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ticka.app.data.Habit
import com.ticka.app.data.I18n
import com.ticka.app.data.Star
import com.ticka.app.data.Task
import com.ticka.app.data.TickaViewModel
import com.ticka.app.data.starKinds
import com.ticka.app.ui.HabitDetail
import com.ticka.app.ui.HabitEditor
import com.ticka.app.ui.HabitsScreen
import com.ticka.app.ui.PomodoroDialog
import com.ticka.app.ui.SettingsScreen
import com.ticka.app.ui.StarGlyph
import com.ticka.app.ui.StarSkyScreen
import com.ticka.app.ui.starSkySizes
import com.ticka.app.ui.TaskEditor
import com.ticka.app.ui.TasksScreen
import com.ticka.app.ui.theme.Amber
import com.ticka.app.ui.theme.Bg
import com.ticka.app.ui.theme.Cyan
import com.ticka.app.ui.theme.Indigo
import com.ticka.app.ui.theme.TextFaint
import com.ticka.app.ui.theme.TextMain
import com.ticka.app.ui.theme.TickaTheme
import com.ticka.app.ui.theme.Violet
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun TickaApp(vm: TickaViewModel = viewModel(), initialTab: Int = 0) {
    val data by vm.data.collectAsState()
    val now by vm.now.collectAsState()
    val awarded by vm.lastAwarded.collectAsState()
    val message by vm.message.collectAsState()

    var tab by remember { mutableStateOf(initialTab.coerceIn(0, 3)) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var focusTask by remember { mutableStateOf<Task?>(null) }
    var detailHabit by remember { mutableStateOf<Habit?>(null) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }

    val context = LocalContext.current

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { vm.exportTo(context, it) } }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.consumeMessage()
        }
    }

    // 编辑/详情弹层里的对象随数据实时同步
    val editingTaskLive = editingTask?.let { t ->
        if (t.id.isBlank()) t else data.tasks.find { it.id == t.id } ?: t
    }
    val focusTaskLive = focusTask?.let { t ->
        if (t.id.isBlank()) t else data.tasks.find { it.id == t.id } ?: t
    }
    val detailLive = detailHabit?.let { h -> data.habits.find { it.id == h.id } ?: h }
    val editingHabitLive = editingHabit?.let { h ->
        if (h.id.isBlank()) h else data.habits.find { it.id == h.id } ?: h
    }

    TickaTheme {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            NebulaBackground {
                // 星图背景：待办/目标页散布已收集的星星（设置可关）
                if ((tab == 0 || tab == 1) && data.settings.skyBackdrop) {
                    SkyBackdrop(stars = data.stars.filter { !it.revoked })
                }
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = { TickaNavBar(tab) { tab = it } }
                ) { padding ->
                    Box(Modifier.padding(padding)) {
                        when (tab) {
                            0 -> TasksScreen(
                                tasks = data.tasks,
                                now = now,
                                onAdd = { editingTask = Task(id = "", title = "") },
                                onToggle = vm::toggleTask,
                                onOpen = { editingTask = it },
                                onFocus = { focusTask = it },
                                onDelete = vm::deleteTask
                            )
                            1 -> HabitsScreen(
                                habits = data.habits,
                                onAdd = { editingHabit = Habit(id = "", name = "") },
                                onToggle = vm::toggleHabitToday,
                                onOpen = { detailHabit = it },
                                onDelete = vm::deleteHabit
                            )
                            2 -> StarSkyScreen(
                                stars = data.stars,
                                archives = data.archivedSkies,
                                capacity = data.settings.skyCapacity,
                                onArchive = vm::archiveSky,
                                onMessage = vm::showMessage
                            )
                            else -> SettingsScreen(
                                settings = data.settings,
                                activeStars = data.stars.count { !it.revoked },
                                archivedCount = data.archivedSkies.size,
                                onUpdateSync = { h, u, p, path -> vm.updateSyncConfig(h, u, p, path) },
                                onUpload = vm::uploadToServer,
                                onRestore = vm::restoreFromServer,
                                onExport = {
                                    exportLauncher.launch("ticka-backup.json")
                                },
                                onImport = { vm.importFrom(context, it) },
                                onSetSkyBackdrop = vm::setSkyBackdrop,
                                onSetLang = vm::setLang,
                                onSetSkyCapacity = vm::setSkyCapacity,
                                onClearAll = vm::clearAllData
                            )
                        }
                    }
                }
            }

            editingTaskLive?.let { t ->
                val isNew = t.id.isBlank()
                TaskEditor(
                    initial = if (isNew) null else t,
                    onDismiss = { editingTask = null },
                    onSave = {
                        vm.upsertTask(it)
                        editingTask = null
                    },
                    onDelete = if (isNew) null else ({
                        vm.deleteTask(t.id)
                        editingTask = null
                    })
                )
            }

            focusTaskLive?.let { t ->
                PomodoroDialog(
                    task = t,
                    onDismiss = { focusTask = null },
                    onFinishedFocus = {
                        vm.addPomodoro(t.id)
                        vm.awardStar(0, t.title)
                    }
                )
            }

            detailLive?.let { h ->
                HabitDetail(
                    habit = h,
                    onDismiss = { detailHabit = null },
                    onEdit = {
                        detailHabit = null
                        editingHabit = it
                    },
                    onDelete = {
                        vm.deleteHabit(h.id)
                        detailHabit = null
                    },
                    onToggleToday = { vm.toggleHabitToday(h.id) }
                )
            }

            editingHabitLive?.let { h ->
                val isNew = h.id.isBlank()
                HabitEditor(
                    initial = if (isNew) null else h,
                    onDismiss = { editingHabit = null },
                    onSave = {
                        vm.upsertHabit(it)
                        editingHabit = null
                    },
                    onDelete = if (isNew) null else ({
                        vm.deleteHabit(h.id)
                        editingHabit = null
                    })
                )
            }

            // 获得星星庆祝浮层
            awarded?.let { star ->
                LaunchedEffect(star.id) {
                    delay(2400)
                    vm.consumeAward()
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xB304040E))
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { vm.consumeAward() },
                    contentAlignment = Alignment.Center
                ) {
                    key(star.id) {
                        val scale = remember { Animatable(0.35f) }
                        LaunchedEffect(Unit) {
                            scale.animateTo(
                                1f,
                                spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.graphicsLayer {
                                scaleX = scale.value
                                scaleY = scale.value
                            }
                        ) {
                            StarGlyph(star.kind, 96.dp)
                            Text(
                                I18n.awarded(I18n.starName(star.kind)),
                                color = TextMain, fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 18.dp)
                            )
                            Text(
                                "${I18n.starRarity(star.kind)} · ${I18n.fromSrc(star.sourceName)}",
                                color = TextFaint, fontSize = 12.5.sp,
                                modifier = Modifier.padding(top = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TickaNavBar(selected: Int, onSelect: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color(0xF20A0A1C),
        contentColor = TextFaint
    ) {
        NavigationBarItem(
            selected = selected == 0,
            onClick = { onSelect(0) },
            icon = { Icon(Icons.Rounded.CheckCircle, null) },
            label = { Text(I18n.navTodos) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0x336366F1),
                selectedIconColor = Cyan,
                selectedTextColor = TextMain,
                unselectedIconColor = TextFaint,
                unselectedTextColor = TextFaint
            )
        )
        NavigationBarItem(
            selected = selected == 1,
            onClick = { onSelect(1) },
            icon = { Icon(Icons.Rounded.LocalFireDepartment, null) },
            label = { Text(I18n.navGoals) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0x33EC4899),
                selectedIconColor = Color(0xFFF9A8D4),
                selectedTextColor = TextMain,
                unselectedIconColor = TextFaint,
                unselectedTextColor = TextFaint
            )
        )
        NavigationBarItem(
            selected = selected == 2,
            onClick = { onSelect(2) },
            icon = { Icon(Icons.Rounded.AutoAwesome, null) },
            label = { Text(I18n.navSky) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0x33FBBF24),
                selectedIconColor = Amber,
                selectedTextColor = TextMain,
                unselectedIconColor = TextFaint,
                unselectedTextColor = TextFaint
            )
        )
        NavigationBarItem(
            selected = selected == 3,
            onClick = { onSelect(3) },
            icon = { Icon(Icons.Rounded.Settings, null) },
            label = { Text(I18n.navSettings) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0x338A8DB8),
                selectedIconColor = TextMain,
                selectedTextColor = TextMain,
                unselectedIconColor = TextFaint,
                unselectedTextColor = TextFaint
            )
        )
    }
}

/** 星图背景：与星空页完全一致的星星图腾，散布在待办/目标页底层 */
@Composable
private fun SkyBackdrop(stars: List<Star>) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val w = maxWidth
        val h = maxHeight
        stars.forEach { s ->
            val glyphSize = starSkySizes[s.kind % starSkySizes.size]
            Box(
                Modifier
                    .absoluteOffset(x = w * s.x - glyphSize / 2, y = h * s.y - glyphSize / 2)
                    .size(maxOf(glyphSize.value, 26f).dp)
                    .alpha(0.9f),
                contentAlignment = Alignment.Center
            ) {
                StarGlyph(s.kind, glyphSize, animate = true)
            }
        }
    }
}

/** 深空星云背景 */
@Composable
private fun NebulaBackground(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Indigo.copy(alpha = 0.16f), Color.Transparent),
                        radius = size.width * 0.55f
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.14f, size.height * 0.05f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF22D3EE).copy(alpha = 0.10f), Color.Transparent),
                        radius = size.width * 0.5f
                    ),
                    radius = size.width * 0.5f,
                    center = Offset(size.width * 0.95f, size.height * 0.94f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Violet.copy(alpha = 0.08f), Color.Transparent),
                        radius = size.width * 0.42f
                    ),
                    radius = size.width * 0.42f,
                    center = Offset(size.width * 0.6f, size.height * 0.42f)
                )
            }
    ) {
        val stars = remember {
            val rnd = kotlin.random.Random(7)
            List(46) { listOf(rnd.nextFloat(), rnd.nextFloat()) }
        }
        Canvas(Modifier.fillMaxSize()) {
            stars.forEachIndexed { i, xy ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f + (i % 4) * 0.08f),
                    radius = if (i % 5 == 0) 2.4f else 1.3f,
                    center = Offset(xy[0] * size.width, xy[1] * size.height)
                )
            }
        }
        content()
    }
}
