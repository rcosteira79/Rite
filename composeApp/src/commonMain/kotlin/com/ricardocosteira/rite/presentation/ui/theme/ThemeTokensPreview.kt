package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun ThemeTokensPreview(modifier: Modifier = Modifier) {
    val colors = RiteAppTheme.colors
    val type = RiteAppTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(RiteAppTheme.spacing.gap4),
        verticalArrangement = Arrangement.spacedBy(RiteAppTheme.spacing.gap4)
    ) {
        Text("Colors", style = type.headlineSmall, color = colors.onSurface)
        ColorRow("primary", colors.primary, colors.onPrimary)
        ColorRow("primaryContainer", colors.primaryContainer, colors.onPrimaryContainer)
        ColorRow("secondary", colors.secondary, colors.onSecondary)
        ColorRow("error", colors.error, colors.onError)
        ColorRow("warn", colors.warn, colors.onWarn)
        ColorRow("suspend", colors.suspend, colors.onSuspend)
        ColorRow("surface", colors.surface, colors.onSurface)
        ColorRow("surfaceDim", colors.surfaceDim, colors.onSurface)
        ColorRow("surfaceBright", colors.surfaceBright, colors.onSurface)
        ColorRow("onSurfaceMuted", colors.surface, colors.onSurfaceMuted)
        ColorRow("onSurfaceSubtle", colors.surface, colors.onSurfaceSubtle)

        Text("Day classification", style = type.headlineSmall, color = colors.onSurface)
        DayRow("Perfect", colors.dayPerfect)
        DayRow("BestEffort", colors.dayBestEffort)
        DayRow("Partial", colors.dayPartial)
        DayRow("RoughDay", colors.dayRoughDay)
        DayRow("Failed", colors.dayFailed)
        DayRow("Skipped", colors.daySkipped)
        DayRow("Future", colors.dayFuture)

        Text("Typography", style = type.headlineSmall, color = colors.onSurface)
        Text("displayLarge — Rite", style = type.displayLarge, color = colors.onSurface)
        Text(
            "displayMedium — Quiet discipline",
            style = type.displayMedium,
            color = colors.onSurface
        )
        Text(
            "displaySmall — Structure your day.",
            style = type.displaySmall,
            color = colors.onSurface
        )
        Text("titleLarge italic", style = type.titleLarge, color = colors.onSurface)
        Text("bodyLarge — regular body copy", style = type.bodyLarge, color = colors.onSurface)
        Text("bodyMedium — secondary body", style = type.bodyMedium, color = colors.onSurfaceMuted)
        Text("eyebrow", style = type.eyebrow, color = colors.onSurfaceMuted)
        Text("mono", style = type.mono, color = colors.onSurfaceMuted)

        Text("Shapes", style = type.headlineSmall, color = colors.onSurface)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShapeSwatch("xs", RiteAppTheme.shapes.xs)
            ShapeSwatch("sm", RiteAppTheme.shapes.sm)
            ShapeSwatch("md", RiteAppTheme.shapes.md)
            ShapeSwatch("lg", RiteAppTheme.shapes.lg)
            ShapeSwatch("xl", RiteAppTheme.shapes.xl)
            ShapeSwatch("xxl", RiteAppTheme.shapes.xxl)
            ShapeSwatch("pill", RiteAppTheme.shapes.pill)
        }
    }
}

@Composable
private fun ColorRow(name: String, bg: Color, fg: Color) {
    Surface(
        color = bg,
        contentColor = fg,
        shape = RiteAppTheme.shapes.sm,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(name, style = RiteAppTheme.typography.labelLarge, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun DayRow(name: String, c: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.size(20.dp).background(c, RiteAppTheme.shapes.xs))
        Text(
            name,
            style = RiteAppTheme.typography.labelMedium,
            color = RiteAppTheme.colors.onSurface
        )
    }
}

@Composable
private fun ShapeSwatch(name: String, shape: Shape) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(32.dp).background(RiteAppTheme.colors.primaryContainer, shape))
        Text(
            name,
            style = RiteAppTheme.typography.labelSmall,
            color = RiteAppTheme.colors.onSurfaceMuted
        )
    }
}
