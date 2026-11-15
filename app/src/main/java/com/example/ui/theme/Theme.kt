package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ShiftFlowLightColorScheme = lightColorScheme(
    primary = CoralPeach,
    onPrimary = Color.White,
    primaryContainer = CoralPeachLight,
    onPrimaryContainer = DeepNavy,
    secondary = DeepNavy,
    onSecondary = Color.White,
    secondaryContainer = SoftNavySurface,
    onSecondaryContainer = DeepNavy,
    tertiary = EmeraldText,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldBg,
    onTertiaryContainer = EmeraldText,
    background = WarmCreamBg,
    onBackground = TextPrimaryNavy,
    surface = WarmCardSurface,
    onSurface = TextPrimaryNavy,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = TextSecondarySlate,
    outline = WarmBorder,
    error = RoseText,
    onError = Color.White,
    errorContainer = RoseBg,
    onErrorContainer = RoseText
)

private val ShiftFlowDarkColorScheme = darkColorScheme(
    primary = CoralPeach,
    onPrimary = Color.White,
    primaryContainer = CoralPeachDark,
    onPrimaryContainer = Color.White,
    secondary = SoftNavySurface,
    onSecondary = DeepNavy,
    secondaryContainer = SlateNavy,
    onSecondaryContainer = Color.White,
    tertiary = EmeraldBorder,
    onTertiary = DeepNavy,
    background = Color(0xFF0E1726),
    onBackground = Color(0xFFF5F7FA),
    surface = Color(0xFF162235),
    onSurface = Color(0xFFF5F7FA),
    surfaceVariant = Color(0xFF1F2F47),
    onSurfaceVariant = Color(0xFFB8C4D6),
    outline = Color(0xFF2E405B)
)

val ShiftFlowShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ShiftFlowDarkColorScheme else ShiftFlowLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ShiftFlowShapes,
        content = content
    )
}
