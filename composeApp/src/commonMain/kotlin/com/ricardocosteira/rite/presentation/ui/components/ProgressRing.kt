package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    strokeWidth: Dp = 6.dp,
    small: Boolean = false,
    capLabel: String? = "Day",
) {
    val clamped = progress.coerceIn(0f, 1f)
    val motion = RiteAppTheme.motion
    val colors = RiteAppTheme.colors
    val typography = RiteAppTheme.typography

    val animated by animateFloatAsState(
        targetValue = clamped,
        animationSpec = tween(
            durationMillis = motion.deliberate.inWholeMilliseconds.toInt(),
            easing = motion.easeQuiet
        ),
        label = "ring-progress"
    )

    val track = colors.outline
    val arc = colors.primary
    val dash = colors.outline

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val inset = 4.dp.toPx()
            val side = this.size.minDimension
            val arcRadius = (side - strokePx) / 2f - inset
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val arcTopLeft = Offset(center.x - arcRadius, center.y - arcRadius)
            val arcSize = Size(arcRadius * 2f, arcRadius * 2f)
            val stroke = Stroke(width = strokePx)

            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = stroke
            )
            if (animated > 0f) {
                drawArc(
                    color = arc,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = stroke
                )
            }
            if (!small) {
                val dashRadius = (side - 20.dp.toPx()) / 2f
                drawCircle(
                    color = dash,
                    center = center,
                    radius = dashRadius,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f), 0f)
                    )
                )
            }
        }

        if (small) {
            Text(
                text = "${(clamped * 100).toInt()}%",
                style = typography.mono.copy(
                    fontSize = 10.sp,
                    color = colors.onSurface,
                    letterSpacing = 0.4.sp
                )
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val numberText = buildAnnotatedString {
                    append("${(clamped * 100).toInt()}")
                    withStyle(
                        SpanStyle(
                            fontSize = 12.sp,
                            color = colors.onSurfaceSubtle
                        )
                    ) { append("%") }
                }
                Text(
                    text = numberText,
                    style = typography.displayLarge.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 24.sp,
                        lineHeight = 24.sp,
                        letterSpacing = (-0.48).sp,
                        color = colors.onSurface
                    )
                )
                if (capLabel != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = capLabel.uppercase(),
                        style = typography.mono.copy(
                            fontSize = 8.5.sp,
                            letterSpacing = 1.53.sp,
                            color = colors.onSurfaceSubtle
                        )
                    )
                }
            }
        }
    }
}
