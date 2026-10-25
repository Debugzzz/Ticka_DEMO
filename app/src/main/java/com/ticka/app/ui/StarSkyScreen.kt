package com.ticka.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ticka.app.data.DayUtils
import com.ticka.app.data.I18n
import com.ticka.app.data.Star
import com.ticka.app.data.StarArchive
import com.ticka.app.data.starKinds
import com.ticka.app.ui.theme.Amber
import com.ticka.app.ui.theme.Cyan
import com.ticka.app.ui.theme.Panel
import com.ticka.app.ui.theme.Stroke
import com.ticka.app.ui.theme.TextDim
import com.ticka.app.ui.theme.TextFaint
import com.ticka.app.ui.theme.TextMain
import kotlinx.coroutines.launch

/** 星空容纳上限的提示由设置里的容量决定 */

@Composable
fun StarSkyScreen(
    stars: List<Star>,
    archives: List<StarArchive>,
    capacity: Int,
    onArchive: () -> Unit,
    onMessage: (String) -> Unit
) {
    var selected by remember { mutableStateOf<Star?>(null) }
    var showArchiveConfirm by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val visible = stars.filter { !it.revoked }
    val today = DayUtils.tickaDate()
    val todayCount = visible.count { DayUtils.tickaDate(it.obtainedAt) == today }
    val pageCount = archives.size + 1
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val page = pagerState.currentPage

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                AppTitle(I18n.sky)
                Text(
                    "${I18n.skiesTotal(pageCount)} · ${I18n.todayPlus(todayCount)}",
                    color = TextFaint, fontSize = 12.5.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }

        if (pageCount > 1) {
            Text(
                I18n.swipeHint + I18n.pageCurrent(page + 1) + if (page > 0) I18n.archivedTag else I18n.collectingTag,
                color = TextFaint, fontSize = 11.5.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            pageSpacing = 12.dp
        ) { p ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (p == 0) {
                    SkyBoard(
                        stars = visible,
                        selected = selected,
                        onSelect = { selected = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    val archive = archives[archives.size - p]
                    SkyBoard(
                        stars = archive.stars,
                        selected = null,
                        onSelect = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        I18n.archiveLine(archives.size - p + 1, archive.stars.size, DayUtils.dueText(archive.archivedAt)),
                        color = TextFaint, fontSize = 11.5.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
            }
        }

        // 导出当前页星图为图片
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.End
        ) {
            GhostButton(I18n.exportImage) {
                scope.launch {
                    try {
                        val board = if (page == 0) visible else archives[archives.size - page].stars
                        onMessage(saveSkyImage(context, board))
                    } catch (e: Exception) {
                        onMessage(I18n.opFail(e.message ?: ""))
                    }
                }
            }
        }

        // 选中星星的来历（仅当前星空）
        if (page == 0) {
            AnimatedVisibility(visible = selected != null, enter = fadeIn(), exit = fadeOut()) {
                selected?.let { s ->
                    val kind = starKinds[s.kind]
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StarGlyph(s.kind, 22.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "${kind.name} · ${kind.rarity} · 来自「${s.sourceName}」 · ${DayUtils.dueText(s.obtainedAt)}",
                            color = TextMain, fontSize = 12.5.sp, fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 星空渐满：提示封存换新（容量可在设置调整，满时也会自动封存）
            if (visible.size >= capacity) {
                GlassCard(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                I18n.skyFullT(visible.size, capacity),
                                color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp
                            )
                            Text(
                                I18n.skyFullS,
                                color = TextFaint, fontSize = 11.5.sp
                            )
                        }
                        GradientButton(I18n.archiveBtn) { showArchiveConfirm = true }
                    }
                }
            }
        }

        SectionLabel(I18n.collection, Modifier.padding(top = 22.dp, bottom = 10.dp))
        // 图鉴统计包含被封存的星星（收回的不算）
        val allKept = stars + archives.flatMap { it.stars }
        starKinds.forEachIndexed { index, kind ->
            val owned = allKept.count { it.kind == index && !it.revoked }
            GlassCard(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 9.dp)
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StarGlyph(index, 34.dp, dim = owned == 0, animate = true)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (owned > 0) I18n.starName(index) else "？？？",
                            color = if (owned > 0) TextMain else TextFaint,
                            fontWeight = FontWeight.Bold, fontSize = 14.sp
                        )
                        Text(I18n.starRarity(index), color = TextFaint, fontSize = 11.sp)
                    }
                    Text(
                        if (owned > 0) "×$owned" else I18n.locked,
                        color = if (owned > 0) Amber else TextFaint,
                        fontWeight = FontWeight.Bold, fontSize = 13.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }

    if (showArchiveConfirm) {
        AlertDialog(
            onDismissRequest = { showArchiveConfirm = false },
            title = { Text(I18n.archiveQ, color = TextMain, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "${I18n.archiveBody(visible.size)}",
                    color = TextDim
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showArchiveConfirm = false
                    selected = null
                    onArchive()
                }) { Text(I18n.archiveBtn, color = Cyan, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveConfirm = false }) { Text(I18n.cancel, color = TextDim) }
            },
            containerColor = Panel
        )
    }
}

/** 星空画布：收集的星星按存储坐标散布 */
@Composable
private fun SkyBoard(
    stars: List<Star>,
    selected: Star?,
    onSelect: (Star) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier
            .height(300.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(listOf(Color(0xFF05050F), Color(0xFF0A0A24)))
            )
            .border(1.dp, Stroke, RoundedCornerShape(20.dp))
    ) {
        val w = maxWidth
        val h = maxHeight
        // 背景星点
        val backdrop = remember {
            val rnd = kotlin.random.Random(42)
            List(34) { listOf(rnd.nextFloat(), rnd.nextFloat()) }
        }
        Canvas(Modifier.fillMaxSize()) {
            backdrop.forEachIndexed { i, xy ->
                drawCircle(
                    Color.White.copy(alpha = 0.06f + (i % 3) * 0.05f),
                    radius = if (i % 6 == 0) 1.8f else 1.0f,
                    center = androidx.compose.ui.geometry.Offset(xy[0] * size.width, xy[1] * size.height)
                )
            }
        }

        if (stars.isEmpty()) {
            Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StarGlyph(4, 40.dp, dim = true, animate = true)
                Text(I18n.emptySky1, color = TextFaint, fontSize = 13.sp)
                Text(I18n.emptySky2, color = TextFaint, fontSize = 12.sp)
            }
        } else {
            stars.forEach { s ->
                val size = starSkySizes[s.kind]
                Box(
                    Modifier
                        .absoluteOffset(x = w * s.x - size / 2, y = h * s.y - size / 2)
                        .size(maxOf(size.value, 26f).dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(s) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selected?.id == s.id) {
                        Box(
                            Modifier
                                .size(maxOf(size.value, 26f).dp + 10.dp)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0x3DFFFFFF), Color.Transparent)
                                    )
                                )
                        )
                    }
                    StarGlyph(s.kind, size, animate = true)
                }
            }
        }
    }
}

/** 用 SkyPoster 渲染高清星图并保存到相册（Android 10+），旧版本走系统分享 */
private suspend fun saveSkyImage(context: android.content.Context, stars: List<Star>): String =
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val bitmap = SkyPoster.render(stars)
        val stamp = java.text.SimpleDateFormat("yyyyMMdd-HHmmss", java.util.Locale.US).format(java.util.Date())
        val name = "ticka-sky-$stamp.png"
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, name)
                put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Ticka")
            }
            val uri = context.contentResolver.insert(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            ) ?: error("无法创建图片文件")
            context.contentResolver.openOutputStream(uri)?.use {
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            } ?: error("无法写入图片")
            I18n.savedGallery
        } else {
            val dir = java.io.File(context.cacheDir, "share").apply { mkdirs() }
            val file = java.io.File(dir, name)
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", file
            )
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                android.content.Intent.createChooser(intent, "分享星图")
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            I18n.generatedShare
        }
    }
