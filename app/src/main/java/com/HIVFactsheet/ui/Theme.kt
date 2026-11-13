package com.HIVFactsheet.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.HIVFactsheet.data.Prefs

// Brand palette carried over from the original app.
val BrandPurple = Color(0xFF6E236B)
val BrandCyan = Color(0xFF7CDFE2)
val BrandCream = Color(0xFFFFE4B5)

private val LightColors = lightColorScheme(
    primary = BrandPurple,
    onPrimary = BrandCyan,
    secondary = BrandPurple,
    background = BrandCream,
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF3E6C8),
)

private val DarkColors = darkColorScheme(
    primary = BrandPurple,
    onPrimary = BrandCyan,
    secondary = Color(0xFFD7A9D5),
    background = Color(0xFF141019),
    onBackground = Color(0xFFEDE4EC),
    surface = Color(0xFF241E2B),
    onSurface = Color(0xFFEDE4EC),
    surfaceVariant = Color(0xFF2E2736),
)

@Composable
fun FactSheetTheme(content: @Composable () -> Unit) {
    val dark = Prefs.darkMode ?: isSystemInDarkTheme()
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
