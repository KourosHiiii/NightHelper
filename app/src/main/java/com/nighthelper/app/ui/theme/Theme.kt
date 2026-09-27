package com.nighthelper.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val NightPurple = Color(0xFF12101F)
val NightPurpleDeep = Color(0xFF191236)
val NightPurpleHigh = Color(0xFF231A45)
val NightSurface = Color(0xFF1C1830)
val NightSurfaceHigh = Color(0xFF262040)
val Lavender = Color(0xFFC3B3FF)
val LavenderDeep = Color(0xFF231A45)
val MoonGold = Color(0xFFFFD98E)
val MintSoft = Color(0xFF8FE3C6)
val RoseSoft = Color(0xFFFF8FA3)
val TextPrimary = Color(0xFFEDE9FF)
val TextDim = Color(0xFFA79FC9)
val Hairline = Color(0xFF322A56)

val NightColors: ColorScheme = darkColorScheme(
    primary = Lavender,
    onPrimary = LavenderDeep,
    secondary = MoonGold,
    onSecondary = Color(0xFF3A2E12),
    tertiary = MintSoft,
    onTertiary = Color(0xFF123A2C),
    background = NightPurple,
    onBackground = TextPrimary,
    surface = NightSurface,
    onSurface = TextPrimary,
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = TextDim,
    error = RoseSoft,
    onError = Color(0xFF43101C),
    outline = Color(0xFF443A6B),
    outlineVariant = Color(0xFF2E2750)
)

val NightShapes: Shapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun NightHelperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NightColors,
        shapes = NightShapes,
        content = content
    )
}
