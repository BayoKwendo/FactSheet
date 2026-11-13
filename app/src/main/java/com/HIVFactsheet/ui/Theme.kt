package com.HIVFactsheet.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand palette carried over from the original app.
val BrandPurple = Color(0xFF6E236B)
val BrandCyan = Color(0xFF7CDFE2)
val BrandCream = Color(0xFFFFE4B5)

private val FactSheetColors = lightColorScheme(
    primary = BrandPurple,
    onPrimary = BrandCyan,
    secondary = BrandPurple,
    background = BrandCream,
    onBackground = Color(0xFF1A1A1A),
    surface = BrandCream,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF3E6C8),
)

@Composable
fun FactSheetTheme(content: @Composable () -> Unit) {
    // The original app is light-only; keep a single consistent look.
    @Suppress("UNUSED_EXPRESSION")
    isSystemInDarkTheme()
    MaterialTheme(colorScheme = FactSheetColors, content = content)
}
