package com.ticka.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ---- 深空色板（galaxy 风格） ----
val Bg = Color(0xFF070714)
val BgSoft = Color(0xFF0C0C20)
val Panel = Color(0xFF12122A)
val Indigo = Color(0xFF6366F1)
val Cyan = Color(0xFF22D3EE)
val Violet = Color(0xFF8B5CF6)
val Pink = Color(0xFFEC4899)
val Amber = Color(0xFFFBBF24)
val Red = Color(0xFFFB7185)
val Green = Color(0xFF34D399)

val TextMain = Color(0xFFEEF0FF)
val TextDim = Color(0x8EEEF0FF)
val TextFaint = Color(0x52EEF0FF)
val Glass = Color(0x0BFFFFFF)
val GlassHi = Color(0x14FFFFFF)
val Stroke = Color(0x17FFFFFF)
val StrokeHi = Color(0x2EFFFFFF)

val GradMain = listOf(Indigo, Cyan)

/** 目标配色预设（紫粉 / 靛青 / 蜜桃 / 薄荷 / 天空） */
val habitPalettes: List<List<Color>> = listOf(
    listOf(Violet, Pink),
    listOf(Indigo, Cyan),
    listOf(Color(0xFFF472B6), Amber),
    listOf(Green, Cyan),
    listOf(Color(0xFFA78BFA), Color(0xFF60A5FA)
    )
)

fun habitPalette(index: Int): List<Color> = habitPalettes[index.coerceIn(0, habitPalettes.size - 1)]

private val DarkScheme = darkColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    secondary = Cyan,
    onSecondary = Color(0xFF06222A),
    tertiary = Pink,
    background = Bg,
    onBackground = TextMain,
    surface = BgSoft,
    onSurface = TextMain,
    surfaceVariant = Panel,
    onSurfaceVariant = TextDim,
    outline = StrokeHi,
    outlineVariant = Stroke,
    primaryContainer = Color(0xFF26265E),
    onPrimaryContainer = TextMain
)

@Composable
fun TickaTheme(content: @Composable () -> Unit) {
    // Ticka 始终深空主题，不随系统切换
    isSystemInDarkTheme()
    MaterialTheme(colorScheme = DarkScheme, content = content)
}
