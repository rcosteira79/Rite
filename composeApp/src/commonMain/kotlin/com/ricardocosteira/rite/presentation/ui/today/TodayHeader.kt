package com.ricardocosteira.rite.presentation.ui.today

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.domain.models.StrictnessPreset
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.today_header_all_done
import rite.composeapp.generated.resources.today_header_day_label
import rite.composeapp.generated.resources.today_header_done_label
import rite.composeapp.generated.resources.today_header_no_habits
import rite.composeapp.generated.resources.today_header_remaining
import org.jetbrains.compose.resources.stringResource

private val EXPANDED_HORIZONTAL_PADDING = 32.dp
private val EXPANDED_TOP_PADDING = 24.dp
private val COLLAPSED_HORIZONTAL_PADDING = 32.dp
private val COLLAPSED_TOP_PADDING = 12.dp
private val COLLAPSED_BOTTOM_PADDING = 12.dp
private val PROGRESS_RING_SIZE = 88.dp
private val PROGRESS_RING_STROKE_WIDTH = 5.dp
private val PILL_CORNER_PERCENT = 50
private val PILL_DOT_SIZE = 8.dp
private val COLLAPSED_PILL_DOT_SIZE = 6.dp
private const val PILL_BACKGROUND_ALPHA = 0.30f
private const val PILL_BORDER_ALPHA = 0.20f
private const val PULSING_DOT_MIN_ALPHA = 0.3f
private const val PULSING_DOT_MAX_ALPHA = 1.0f
private const val PULSING_DOT_DURATION_MS = 1200
private const val TRACK_ALPHA = 0.15f
private const val FULL_CIRCLE_DEGREES = 360f
private const val ARC_START_ANGLE = -90f
private const val PERCENTAGE_MULTIPLIER = 100
private const val PROGRESS_ANIMATION_DURATION = 400

@Composable
fun TodayHeader(
    motivationalTitle: String,
    pendingCount: Int,
    hasHabits: Boolean,
    strictnessPreset: StrictnessPreset?,
    dailyProgressDisplay: Int,
    dailyProgressExact: Float,
    dailyTotal: Int,
    isCollapsed: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = isCollapsed,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = modifier.fillMaxWidth(),
        label = "HeaderCollapse"
    ) { collapsed ->
        if (collapsed) {
            CollapsedHeader(
                motivationalTitle = motivationalTitle,
                pendingCount = pendingCount,
                hasHabits = hasHabits,
                strictnessPreset = strictnessPreset,
                dailyProgressDisplay = dailyProgressDisplay,
                dailyProgressExact = dailyProgressExact,
                dailyTotal = dailyTotal
            )
        } else {
            ExpandedHeader(
                motivationalTitle = motivationalTitle,
                pendingCount = pendingCount,
                hasHabits = hasHabits,
                strictnessPreset = strictnessPreset,
                dailyProgressDisplay = dailyProgressDisplay,
                dailyProgressExact = dailyProgressExact,
                dailyTotal = dailyTotal
            )
        }
    }
}

@Composable
private fun ExpandedHeader(
    motivationalTitle: String,
    pendingCount: Int,
    hasHabits: Boolean,
    strictnessPreset: StrictnessPreset?,
    dailyProgressDisplay: Int,
    dailyProgressExact: Float,
    dailyTotal: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start = EXPANDED_HORIZONTAL_PADDING,
                end = EXPANDED_HORIZONTAL_PADDING,
                top = EXPANDED_TOP_PADDING,
                bottom = 16.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = motivationalTitle,
                style = RiteAppTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 32.sp
                ),
                color = RiteAppTheme.colorScheme.primary
            )

            val subtitleText: String = if (!hasHabits) {
                stringResource(Res.string.today_header_no_habits)
            } else if (pendingCount > 0) {
                stringResource(Res.string.today_header_remaining, pendingCount)
            } else {
                stringResource(Res.string.today_header_all_done)
            }

            Text(
                text = subtitleText,
                style = RiteAppTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = RiteAppTheme.colorScheme.onSurfaceVariant
            )

            if (strictnessPreset != null) {
                StrictnessPresetPill(preset = strictnessPreset)
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        if (dailyTotal > 0) {
            DailyProgressRing(
                dailyProgressDisplay = dailyProgressDisplay,
                dailyProgressExact = dailyProgressExact,
                dailyTotal = dailyTotal
            )
        }
    }
}

@Composable
private fun CollapsedHeader(
    motivationalTitle: String,
    pendingCount: Int,
    hasHabits: Boolean,
    strictnessPreset: StrictnessPreset?,
    dailyProgressDisplay: Int,
    dailyProgressExact: Float,
    dailyTotal: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start = COLLAPSED_HORIZONTAL_PADDING,
                end = COLLAPSED_HORIZONTAL_PADDING,
                top = COLLAPSED_TOP_PADDING,
                bottom = COLLAPSED_BOTTOM_PADDING
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = motivationalTitle,
                style = RiteAppTheme.typography.headlineSmall.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = RiteAppTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val subtitleText: String = if (!hasHabits) {
                    stringResource(Res.string.today_header_no_habits)
                } else if (pendingCount > 0) {
                    stringResource(Res.string.today_header_remaining, pendingCount)
                } else {
                    stringResource(Res.string.today_header_all_done)
                }

                Text(
                    text = subtitleText,
                    style = RiteAppTheme.typography.bodySmall,
                    color = RiteAppTheme.colorScheme.onSurfaceVariant
                )

                if (strictnessPreset != null) {
                    StrictnessPresetPill(
                        preset = strictnessPreset,
                        isCompact = true
                    )
                }
            }
        }

        if (dailyTotal > 0) {
            val percentage: Int = if (dailyTotal > 0) {
                (dailyProgressExact * PERCENTAGE_MULTIPLIER / dailyTotal).toInt()
            } else {
                0
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$percentage%",
                    style = RiteAppTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = RiteAppTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(Res.string.today_header_done_label).uppercase(),
                    style = RiteAppTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = RiteAppTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StrictnessPresetPill(preset: StrictnessPreset, isCompact: Boolean = false) {
    val presetName: String = preset.name.lowercase().replaceFirstChar { it.uppercase() }

    Surface(
        shape = RoundedCornerShape(PILL_CORNER_PERCENT),
        color = RiteAppTheme.colorScheme.primaryContainer.copy(alpha = PILL_BACKGROUND_ALPHA),
        modifier = Modifier.border(
            width = 1.dp,
            color = RiteAppTheme.colorScheme.primaryContainer.copy(alpha = PILL_BORDER_ALPHA),
            shape = RoundedCornerShape(PILL_CORNER_PERCENT)
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (isCompact) 8.dp else 16.dp,
                vertical = if (isCompact) 2.dp else 6.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PulsingDot(
                modifier = Modifier.size(if (isCompact) COLLAPSED_PILL_DOT_SIZE else PILL_DOT_SIZE)
            )

            Text(
                text = presetName,
                style = RiteAppTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = RiteAppTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PulsingDot(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "PulsingDot")
    val alpha: Float by infiniteTransition.animateFloat(
        initialValue = PULSING_DOT_MAX_ALPHA,
        targetValue = PULSING_DOT_MIN_ALPHA,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = PULSING_DOT_DURATION_MS,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulsingDotAlpha"
    )

    val dotColor = RiteAppTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        drawCircle(
            color = dotColor.copy(alpha = alpha),
            radius = size.minDimension / 2f
        )
    }
}

@Composable
private fun DailyProgressRing(
    dailyProgressDisplay: Int,
    dailyProgressExact: Float,
    dailyTotal: Int
) {
    val percentage: Int = if (dailyTotal > 0) {
        (dailyProgressExact * PERCENTAGE_MULTIPLIER / dailyTotal).toInt()
    } else {
        0
    }
    val targetSweepAngle: Float = if (dailyTotal > 0) {
        FULL_CIRCLE_DEGREES * dailyProgressExact / dailyTotal
    } else {
        0f
    }
    val sweepAngle: Float by animateFloatAsState(
        targetValue = targetSweepAngle,
        animationSpec = tween(durationMillis = PROGRESS_ANIMATION_DURATION),
        label = "progress-ring"
    )

    val trackColor = RiteAppTheme.colorScheme.surfaceContainerHighest
    val progressColor = RiteAppTheme.colorScheme.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(PROGRESS_RING_SIZE)
        ) {
            Canvas(modifier = Modifier.size(PROGRESS_RING_SIZE)) {
                val strokeWidthPx: Float = PROGRESS_RING_STROKE_WIDTH.toPx()
                val radius: Float = (size.minDimension - strokeWidthPx) / 2f
                val topLeft = Offset(
                    x = center.x - radius,
                    y = center.y - radius
                )
                val arcSize = Size(radius * 2, radius * 2)

                drawArc(
                    color = trackColor,
                    startAngle = ARC_START_ANGLE,
                    sweepAngle = FULL_CIRCLE_DEGREES,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )

                drawArc(
                    color = progressColor,
                    startAngle = ARC_START_ANGLE,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$percentage%",
                    style = RiteAppTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = RiteAppTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(Res.string.today_header_day_label).uppercase(),
                    style = RiteAppTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = RiteAppTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
