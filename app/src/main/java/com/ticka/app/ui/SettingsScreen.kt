package com.ticka.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.ticka.app.data.Settings
import com.ticka.app.ui.theme.Cyan
import com.ticka.app.ui.theme.GradMain
import com.ticka.app.ui.theme.Indigo
import com.ticka.app.ui.theme.Panel
import com.ticka.app.ui.theme.Red
import com.ticka.app.ui.theme.TextDim
import com.ticka.app.ui.theme.TextFaint
import com.ticka.app.ui.theme.TextMain

/** 语言选择小胶囊 */
@Composable
private fun LanguageChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        color = if (selected) Color.White else TextDim,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.5.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) Brush.linearGradient(GradMain)
                else androidx.compose.ui.graphics.SolidColor(Color(0x14FFFFFF))
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}

/** 同步配置输入行：小标签 + 输入框（支持密码模式） */
@Composable
private fun SyncField(
    label: String,
    value: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    password: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    onValueChange: (String) -> Unit
) {
    Column(modifier.padding(top = 10.dp)) {
        Text(label, color = TextDim, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
        TickaTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            visualTransformation = if (password && !showPassword)
                androidx.compose.ui.text.input.PasswordVisualTransformation()
            else androidx.compose.ui.text.input.VisualTransformation.None,
            trailingIcon = if (password && onTogglePassword != null) {
                {
                    androidx.compose.material3.IconButton(onClick = onTogglePassword) {
                        Icon(
                            if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            if (showPassword) "隐藏密码" else "显示密码",
                            tint = TextFaint
                        )
                    }
                }
            } else null
        )
    }
}

@Composable
fun SettingsScreen(
    settings: Settings,
    activeStars: Int,
    archivedCount: Int,
    onUpdateSync: (host: String, username: String, password: String, path: String) -> Unit,
    onUpload: () -> Unit,
    onRestore: () -> Unit,
    onExport: () -> Unit,
    onImport: (android.net.Uri) -> Unit,
    onSetSkyBackdrop: (Boolean) -> Unit,
    onSetLang: (String) -> Unit,
    onSetSkyCapacity: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    var host by remember(settings.host) { mutableStateOf(settings.host) }
    var username by remember(settings.username) { mutableStateOf(settings.username) }
    var password by remember(settings.password) { mutableStateOf(settings.password) }
    var path by remember(settings.path) { mutableStateOf(settings.path) }
    var showPassword by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    var confirmRestore by remember { mutableStateOf(false) }
    var pendingImport by remember { mutableStateOf<android.net.Uri?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { pendingImport = it } }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        AppTitle("设置")

        SectionLabel(I18n.syncSection, Modifier.padding(top = 22.dp, bottom = 10.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    I18n.syncDesc,
                    color = TextFaint, fontSize = 12.sp
                )
                SyncField(I18n.serverAddr, host, I18n.phServer) {
                    host = it
                }
                SyncField(I18n.usernameL, username, I18n.phUser) { username = it }
                SyncField(I18n.passwordL, password, I18n.phPass, password = true, showPassword = showPassword,
                    onTogglePassword = { showPassword = !showPassword }) { password = it }
                SyncField(I18n.pathL, path, I18n.phPath) { path = it }
                Row(
                    Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GhostButton(I18n.saveCfg) {
                        onUpdateSync(host, username, password, path)
                    }
                    Spacer(Modifier.weight(1f))
                    GhostButton(I18n.restore) { confirmRestore = true }
                    GradientButton(I18n.upload) { onUpload() }
                }
                Text(
                    settings.lastSyncAt?.let { I18n.lastSync(DayUtils.dueText(it)) } ?: I18n.neverSync,
                    color = TextFaint, fontSize = 11.5.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }

        SectionLabel(I18n.appearance, Modifier.padding(top = 22.dp, bottom = 10.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(I18n.skyBackdrop, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
                    Text(
                        I18n.skyBackdropDesc,
                        color = TextFaint, fontSize = 12.sp,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
                Switch(
                    checked = settings.skyBackdrop,
                    onCheckedChange = { onSetSkyBackdrop(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Indigo.copy(alpha = 0.75f),
                        uncheckedThumbColor = TextDim,
                        uncheckedTrackColor = Color(0x22FFFFFF),
                        uncheckedBorderColor = Color(0x33FFFFFF)
                    )
                )
            }
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(I18n.skyCapacity, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
                    Text(
                        I18n.skyCapacityDesc(settings.skyCapacity),
                        color = TextFaint, fontSize = 12.sp,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GhostButton("−") { onSetSkyCapacity(settings.skyCapacity - 1) }
                    Text(
                        "${settings.skyCapacity}",
                        color = TextMain, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    GhostButton("＋") { onSetSkyCapacity(settings.skyCapacity + 1) }
                }
            }
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(I18n.language, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp, modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    LanguageChip("中文", settings.lang != "en") { onSetLang("zh") }
                    LanguageChip("English", settings.lang == "en") { onSetLang("en") }
                }
            }
        }

        SectionLabel(I18n.dataSection, Modifier.padding(top = 22.dp, bottom = 10.dp))
        GlassCard(Modifier.fillMaxWidth(), onClick = onExport) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(I18n.exportAll, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
                Text(I18n.exportSub, color = TextFaint, fontSize = 12.sp)
            }
        }
        GlassCard(Modifier.fillMaxWidth(), onClick = { importLauncher.launch(arrayOf("*/*")) }) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(I18n.importData, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
                Text(I18n.importSub, color = TextFaint, fontSize = 12.sp)
            }
        }
        GlassCard(Modifier.fillMaxWidth(), onClick = { confirmClear = true }) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(I18n.clearAll, color = Red, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
                Text(I18n.clearSub, color = TextFaint, fontSize = 12.sp)
            }
        }

        SectionLabel(I18n.about, Modifier.padding(top = 22.dp, bottom = 10.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(I18n.version, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
                Text(
                    I18n.aboutStats(activeStars, archivedCount),
                    color = TextFaint, fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    I18n.settle3am,
                    color = TextFaint, fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Spacer(Modifier.padding(bottom = 16.dp))
    }

    if (confirmRestore) {
        AlertDialog(
            onDismissRequest = { confirmRestore = false },
            title = { Text(I18n.restoreQ, color = TextMain, fontWeight = FontWeight.Bold) },
            text = { Text(I18n.restoreBody, color = TextDim) },
            confirmButton = {
                TextButton(onClick = { confirmRestore = false; onRestore() }) {
                    Text(I18n.restoreBtn, color = Red, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmRestore = false }) { Text("取消", color = TextDim) }
            },
            containerColor = Panel
        )
    }

    pendingImport?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text(I18n.importQ, color = TextMain, fontWeight = FontWeight.Bold) },
            text = { Text(I18n.importBody, color = TextDim) },
            confirmButton = {
                TextButton(onClick = {
                    val u = uri
                    pendingImport = null
                    onImport(u)
                }) { Text(I18n.importBtn, color = Red, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) { Text("取消", color = TextDim) }
            },
            containerColor = Panel
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(I18n.clearQ, color = TextMain, fontWeight = FontWeight.Bold) },
            text = { Text(I18n.clearBody, color = TextDim) },
            confirmButton = {
                TextButton(onClick = { confirmClear = false; onClearAll() }) {
                    Text(I18n.clearBtn, color = Red, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("取消", color = TextDim) }
            },
            containerColor = Panel
        )
    }
}
