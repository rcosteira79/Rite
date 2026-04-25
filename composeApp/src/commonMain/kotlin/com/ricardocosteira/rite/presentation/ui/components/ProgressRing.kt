package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun ProgressRing(
    progress: Float, // 0f..1f
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    strokeWidth: Dp = 4.dp,
    label: String? = null
) {
    val clamped = progress.coerceIn(0f, 1f)
    val track = RiteAppTheme.colors.outlineVariant
    val bar = RiteAppTheme.colors.primary

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val s = this.size.minDimension
            val topLeft = Offset((this.size.width - s) / 2, (this.size.height - s) / 2)
            val arcSize = Size(s, s)
            val stroke = Stroke(width = strokeWidth.toPx())
            // Track
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
            // Progress
            drawArc(
                color = bar,
                startAngle = -90f,
                sweepAngle = 360f * clamped,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
        }
        if (label != null) {
            Text(text = label, style = RiteAppTheme.typography.displaySmall)
        }
    }
}
