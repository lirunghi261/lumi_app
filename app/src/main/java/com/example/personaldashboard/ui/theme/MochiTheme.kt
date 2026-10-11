package com.example.personaldashboard.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.personaldashboard.model.ThemePreset

data class ThemePack(
    val preset: ThemePreset,
    val top: Color,
    val bottom: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val primary: Color,
    val secondary: Color,
    val ink: Color,
    val mutedInk: Color,
    val border: Color,
    val sparkle: Color,
)

val themePacks = listOf(
    ThemePack(
        ThemePreset.COZY,
        top = Color(0xFFC9C3FF), bottom = Color(0xFFFFE9D7),
        surface = Color(0xFFFDF7FF), surfaceAlt = Color(0xFFFFEEF4),
        primary = Color(0xFF7A5CB8), secondary = Color(0xFFFF719F),
        ink = Color(0xFF34234F), mutedInk = Color(0xFF766986),
        border = Color(0xFF7C6AA8), sparkle = Color(0xFFFFE38B),
    ),
    ThemePack(
        ThemePreset.COSMIC,
        top = Color(0xFF17173A), bottom = Color(0xFF44307B),
        surface = Color(0xF02B2858), surfaceAlt = Color(0xF037316A),
        primary = Color(0xFFBDA7FF), secondary = Color(0xFF7BE7FF),
        ink = Color(0xFFF8F2FF), mutedInk = Color(0xFFD2C8EA),
        border = Color(0xFF8C7ED1), sparkle = Color(0xFFFFD66B),
    ),
    ThemePack(
        ThemePreset.SAKURA,
        top = Color(0xFFFFD7E6), bottom = Color(0xFFFFF4E8),
        surface = Color(0xFFFFFAFC), surfaceAlt = Color(0xFFFFE9F1),
        primary = Color(0xFFB4557C), secondary = Color(0xFFED7FA9),
        ink = Color(0xFF4C2A3B), mutedInk = Color(0xFF826474),
        border = Color(0xFFD181A0), sparkle = Color(0xFFFFC95F),
    ),
    ThemePack(
        ThemePreset.MINIMAL,
        top = Color(0xFFEAF5EF), bottom = Color(0xFFF7F1E8),
        surface = Color(0xFFFDFDFC), surfaceAlt = Color(0xFFF1F5F2),
        primary = Color(0xFF427965), secondary = Color(0xFFE09A68),
        ink = Color(0xFF24332E), mutedInk = Color(0xFF63716C),
        border = Color(0xFF78978B), sparkle = Color(0xFFF4C95D),
    ),
)

val AppFontFamily = FontFamily.SansSerif

private val MochiTypography = Typography(
    displayLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 44.sp),
    headlineLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 26.sp),
    titleLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp),
    labelLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp),
    bodyLarge = TextStyle(fontFamily = AppFontFamily, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp),
)

fun packFor(preset: ThemePreset): ThemePack = themePacks.first { it.preset == preset }

private fun colors(pack: ThemePack): ColorScheme = lightColorScheme(
    primary = pack.primary,
    secondary = pack.secondary,
    background = pack.bottom,
    surface = pack.surface,
    surfaceVariant = pack.surfaceAlt,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = pack.ink,
    onSurface = pack.ink,
    onSurfaceVariant = pack.mutedInk,
    outline = pack.border,
)

@Composable
fun MochiTheme(pack: ThemePack, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colors(pack),
        typography = MochiTypography,
        content = content,
    )
}
