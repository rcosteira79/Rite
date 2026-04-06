package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Shared colour palette for day classification across the app (heatmap, calendar).
 * Extracted from the calendar design system.
 */
@Immutable
data class DayClassificationColors(
    val perfect: Color,
    val bestEffort: Color,
    val partial: Color,
    val roughDay: Color,
    val failed: Color,
    val noData: Color,
    val skipped: Color
)

// Dark theme — from calendar design
private val DarkDayClassificationColors = DayClassificationColors(
    perfect = Color(0xFF2D6B4A),
    bestEffort = Color(0xFF2D5F5F),
    partial = Color(0xFF3B5998),
    roughDay = Color(0xFF8B5E3C),
    failed = Color(0xFF8B3A3A),
    noData = Color(0xFF333333),
    skipped = Color(0xFF5A5A5A)
)

// Light theme
private val LightDayClassificationColors = DayClassificationColors(
    perfect = Color(0xFF4CAF7A),
    bestEffort = Color(0xFF4DB6AC),
    partial = Color(0xFF5C7CDB),
    roughDay = Color(0xFFCD8B62),
    failed = Color(0xFFE57373),
    noData = Color(0xFFE0E0E0),
    skipped = Color(0xFFBDBDBD)
)

val LocalDayClassificationColors = staticCompositionLocalOf { DarkDayClassificationColors }

@Composable
fun dayClassificationColors(isDarkTheme: Boolean): DayClassificationColors =
    if (isDarkTheme) DarkDayClassificationColors else LightDayClassificationColors
