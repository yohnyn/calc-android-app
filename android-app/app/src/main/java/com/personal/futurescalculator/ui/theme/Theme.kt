package com.personal.futurescalculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ProfitGreen = Color(0xFF13A66A)
val LossRed = Color(0xFFD94B4B)
val WarningAmber = Color(0xFFE3A11A)

private val LightColors = lightColorScheme(
    primary = Color(0xFF006C66),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F1EA),
    onPrimaryContainer = Color(0xFF00201D),
    secondary = Color(0xFF5B5F00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4E86E),
    onSecondaryContainer = Color(0xFF1B1D00),
    background = Color(0xFFF6F7F4),
    onBackground = Color(0xFF191C1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFE0E5E2),
    onSurfaceVariant = Color(0xFF404946),
    outline = Color(0xFF717A76),
    error = LossRed
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF80D5CD),
    onPrimary = Color(0xFF003734),
    primaryContainer = Color(0xFF00504B),
    onPrimaryContainer = Color(0xFF9CF2EA),
    secondary = Color(0xFFC8CC55),
    onSecondary = Color(0xFF2F3300),
    secondaryContainer = Color(0xFF464A00),
    onSecondaryContainer = Color(0xFFE4E86E),
    background = Color(0xFF101413),
    onBackground = Color(0xFFE0E3E0),
    surface = Color(0xFF171B1A),
    onSurface = Color(0xFFE0E3E0),
    surfaceVariant = Color(0xFF404946),
    onSurfaceVariant = Color(0xFFC0C9C5),
    outline = Color(0xFF8A9490),
    error = Color(0xFFFFB3B0)
)

@Composable
fun FuturesCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
