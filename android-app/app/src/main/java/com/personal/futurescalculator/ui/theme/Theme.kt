package com.personal.futurescalculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.personal.futurescalculator.model.ThemeMode

val ProfitGreen = Color(0xFF3D856C)
val LossRed = Color(0xFFB9686D)
val WarningAmber = Color(0xFFB4874E)

private val LightColors = lightColorScheme(
    primary = Color(0xFF4E6FAE),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE7FA),
    onPrimaryContainer = Color(0xFF20345B),
    secondary = Color(0xFF78649A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAE2F4),
    onSecondaryContainer = Color(0xFF423454),
    tertiary = Color(0xFF3F7E7A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD7ECE9),
    onTertiaryContainer = Color(0xFF234744),
    background = Color(0xFFF4F5F8),
    onBackground = Color(0xFF282B31),
    surface = Color(0xFFFAFAFC),
    onSurface = Color(0xFF282B31),
    surfaceVariant = Color(0xFFE9EBF0),
    onSurfaceVariant = Color(0xFF5B606B),
    outline = Color(0xFF959BA8),
    error = LossRed
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB5C8F2),
    onPrimary = Color(0xFF23385F),
    primaryContainer = Color(0xFF354C78),
    onPrimaryContainer = Color(0xFFDCE6FF),
    secondary = Color(0xFFD1BCEA),
    onSecondary = Color(0xFF44345C),
    secondaryContainer = Color(0xFF57476C),
    onSecondaryContainer = Color(0xFFEEDFFF),
    tertiary = Color(0xFFA5D4CF),
    onTertiary = Color(0xFF244B48),
    tertiaryContainer = Color(0xFF365E5B),
    onTertiaryContainer = Color(0xFFD1F2EE),
    background = Color(0xFF17191E),
    onBackground = Color(0xFFE1E2E8),
    surface = Color(0xFF1E2026),
    onSurface = Color(0xFFE1E2E8),
    surfaceVariant = Color(0xFF343740),
    onSurfaceVariant = Color(0xFFC3C6D0),
    outline = Color(0xFF8D919E),
    error = Color(0xFFFFB3B0)
)

@Composable
fun FuturesCalculatorTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
