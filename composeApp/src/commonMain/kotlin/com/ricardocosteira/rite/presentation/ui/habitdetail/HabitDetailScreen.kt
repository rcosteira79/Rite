package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.components.PrimaryButton
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.common_cd_back
import rite.composeapp.generated.resources.habit_detail_action_complete
import rite.composeapp.generated.resources.habit_detail_action_custom
import rite.composeapp.generated.resources.habit_detail_action_skip
import rite.composeapp.generated.resources.habit_detail_action_undo
import rite.composeapp.generated.resources.habit_detail_action_undo_last
import rite.composeapp.generated.resources.habit_detail_category_binary
import rite.composeapp.generated.resources.habit_detail_category_quantitative
import rite.composeapp.generated.resources.habit_detail_cd_edit
import rite.composeapp.generated.resources.habit_detail_enforcement_limits
import rite.composeapp.generated.resources.habit_detail_heatmap_title
import rite.composeapp.generated.resources.habit_detail_progress
import rite.composeapp.generated.resources.habit_detail_skips_none
import rite.composeapp.generated.resources.habit_detail_skips_remaining
import rite.composeapp.generated.resources.habit_detail_skips_unlimited
import rite.composeapp.generated.resources.habit_detail_stat_current_streak
import rite.composeapp.generated.resources.habit_detail_stat_days
import rite.composeapp.generated.resources.habit_detail_stat_habit_score
import rite.composeapp.generated.resources.habit_detail_stat_longest_streak
import rite.composeapp.generated.resources.habit_form_cd_archive
import rite.composeapp.generated.resources.habit_form_cd_delete

private val RING_SIZE = 120.dp
private val RING_STROKE_WIDTH = 6.dp
private const val FULL_CIRCLE_DEGREES = 360f
private const val ARC_START_ANGLE = -90f
private const val PROGRESS_ANIMATION_DURATION = 400
private const val PERCENTAGE_MULTIPLIER = 100
private val CARD_CORNER = 16.dp
private val STEPPER_BUTTON_SIZE = 48.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    state: HabitDetailState,
    onBackClick: () -> Unit,
    onComplete: () -> Unit,
    onIncrementProgress: () -> Unit,
    onCustomProgress: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    onUndoIncrement: () -> Unit,
    onEditHabit: () -> Unit,
    onArchiveHabit: () -> Unit,
    onDeleteHabit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState: ScrollState = rememberScrollState()
    val isScrolled: Boolean by remember { derivedStateOf { scrollState.value > 0 } }
    val filledColor: Color = RiteAppTheme.colors.surfaceContainerHighest
    val targetColor: Color = if (isScrolled) filledColor else filledColor.copy(alpha = 0f)
    val iconContainerColor: Color by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 200),
        label = "iconContainerColor"
    )

    Scaffold(
        modifier = modifier,
        containerColor = RiteAppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = iconContainerColor
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onEditHabit,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = iconContainerColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(
                                Res.string.habit_detail_cd_edit
                            ),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick = onArchiveHabit,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = iconContainerColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = stringResource(
                                Res.string.habit_form_cd_archive
                            ),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteHabit,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = iconContainerColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(
                                Res.string.habit_form_cd_delete
                            ),
                            tint = RiteAppTheme.colors.error,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading || state.habit == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {
                // Category label
                Text(
                    text = if (state.habit.type == HabitType.BINARY) {
                        stringResource(Res.string.habit_detail_category_binary)
                    } else {
                        stringResource(Res.string.habit_detail_category_quantitative)
                    },
                    style = RiteAppTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = RiteAppTheme.colors.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Habit name
                Text(
                    text = state.habit.name.uppercase(),
                    style = RiteAppTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = RiteAppTheme.colors.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Progress ring in card
                ProgressRingCard(state = state)

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons — right under the ring for quick access
                ActionButtons(
                    state = state,
                    onComplete = onComplete,
                    onIncrementProgress = onIncrementProgress,
                    onCustomProgress = onCustomProgress,
                    onSkip = onSkip,
                    onUndo = onUndo,
                    onUndoIncrement = onUndoIncrement
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Stats row in cards
                StatsRow(state = state)

                Spacer(modifier = Modifier.height(12.dp))

                // Skip limits in card
                SkipLimitsCard(state = state)

                Spacer(modifier = Modifier.height(24.dp))

                // Heatmap
                Text(
                    text = stringResource(Res.string.habit_detail_heatmap_title),
                    style = RiteAppTheme.typography.titleMedium,
                    color = RiteAppTheme.colors.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                HeatmapGrid(
                    heatmapData = state.heatmapData,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProgressRingCard(state: HabitDetailState, modifier: Modifier = Modifier) {
    val habit = state.habit ?: return

    val targetProgress: Float = if (habit.type == HabitType.BINARY) {
        if (habit.isCompleted) 1f else 0f
    } else {
        habit.progressPercentage.coerceIn(0f, 1f)
    }

    val percentage: Int = if (habit.type == HabitType.BINARY) {
        if (habit.isCompleted) PERCENTAGE_MULTIPLIER else 0
    } else {
        (habit.progressPercentage * PERCENTAGE_MULTIPLIER).toInt()
    }

    val sweepAngle: Float by animateFloatAsState(
        targetValue = FULL_CIRCLE_DEGREES * targetProgress,
        animationSpec = tween(durationMillis = PROGRESS_ANIMATION_DURATION),
        label = "detail-progress-ring"
    )

    val trackColor = RiteAppTheme.colors.surfaceContainerHighest
    val progressColor = RiteAppTheme.colors.primary

    Surface(
        shape = RoundedCornerShape(CARD_CORNER),
        color = RiteAppTheme.colors.surfaceContainerLow,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(RING_SIZE)
            ) {
                Canvas(modifier = Modifier.size(RING_SIZE)) {
                    val strokeWidthPx: Float = RING_STROKE_WIDTH.toPx()
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

                Text(
                    text = "$percentage%",
                    style = RiteAppTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = RiteAppTheme.colors.primary
                )
            }

            // "X of Y UNIT" below ring (quantitative only)
            if (habit.type == HabitType.QUANTITATIVE) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        Res.string.habit_detail_progress,
                        habit.currentProgress,
                        habit.targetValue ?: 0,
                        habit.unit?.uppercase() ?: ""
                    ),
                    style = RiteAppTheme.typography.bodySmall,
                    color = RiteAppTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatsRow(state: HabitDetailState, modifier: Modifier = Modifier) {
    val habit = state.habit ?: return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            value = "${habit.currentStreak}",
            label = stringResource(Res.string.habit_detail_stat_current_streak),
            suffix = stringResource(Res.string.habit_detail_stat_days),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "${habit.longestStreak}",
            label = stringResource(Res.string.habit_detail_stat_longest_streak),
            suffix = stringResource(Res.string.habit_detail_stat_days),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "${habit.habitScore}",
            label = stringResource(Res.string.habit_detail_stat_habit_score),
            suffix = null,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(value: String, label: String, suffix: String?, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(CARD_CORNER),
        color = RiteAppTheme.colors.surfaceContainerLow,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                style = RiteAppTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                color = RiteAppTheme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (suffix != null) "$value $suffix" else value,
                style = RiteAppTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = RiteAppTheme.colors.onSurface
            )
        }
    }
}

@Composable
private fun SkipLimitsCard(state: HabitDetailState, modifier: Modifier = Modifier) {
    val habit = state.habit ?: return
    val text: String = when {
        habit.maxConsecutiveSkips == null -> stringResource(Res.string.habit_detail_skips_unlimited)
        habit.isSkipLocked -> stringResource(Res.string.habit_detail_skips_none)
        else -> stringResource(Res.string.habit_detail_skips_remaining, habit.skipsRemaining ?: 0)
    }

    Surface(
        shape = RoundedCornerShape(CARD_CORNER),
        color = RiteAppTheme.colors.surfaceContainerLow,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.habit_detail_enforcement_limits),
                    style = RiteAppTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                    color = RiteAppTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = text,
                    style = RiteAppTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = RiteAppTheme.colors.primary
                )
            }
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = RiteAppTheme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionButtons(
    state: HabitDetailState,
    onComplete: () -> Unit,
    onIncrementProgress: () -> Unit,
    onCustomProgress: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    onUndoIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = state.habit ?: return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (habit.type == HabitType.BINARY) {
            BinaryActions(
                state = state,
                onComplete = onComplete,
                onUndo = onUndo,
                onSkip = onSkip
            )
        } else {
            QuantitativeActions(
                state = state,
                onIncrementProgress = onIncrementProgress,
                onUndoIncrement = onUndoIncrement,
                onCustomProgress = onCustomProgress,
                onUndo = onUndo,
                onSkip = onSkip
            )
        }
    }
}

@Composable
private fun BinaryActions(
    state: HabitDetailState,
    onComplete: () -> Unit,
    onUndo: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = state.habit ?: return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (habit.isResolved && (habit.isCompleted || habit.isSkipped)) {
            PrimaryButton(onClick = onUndo) {
                Text(stringResource(Res.string.habit_detail_action_undo))
            }
        } else {
            PrimaryButton(
                onClick = onComplete,
                enabled = !habit.isResolved
            ) {
                Text(stringResource(Res.string.habit_detail_action_complete))
            }
        }

        if (!habit.isResolved) {
            SkipRow(onSkip = onSkip, isSkipLocked = habit.isSkipLocked)
        }
    }
}

@Composable
private fun QuantitativeActions(
    state: HabitDetailState,
    onIncrementProgress: () -> Unit,
    onUndoIncrement: () -> Unit,
    onCustomProgress: () -> Unit,
    onUndo: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = state.habit ?: return
    val hasProgress: Boolean = habit.currentProgress > 0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (habit.isResolved && (habit.isCompleted || habit.isSkipped)) {
            PrimaryButton(onClick = onUndo) {
                Text(stringResource(Res.string.habit_detail_action_undo))
            }
        } else {
            // Quantity stepper
            Surface(
                shape = RoundedCornerShape(CARD_CORNER),
                color = RiteAppTheme.colors.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StepperButton(
                        text = "−",
                        onClick = onUndoIncrement,
                        enabled = hasProgress
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${habit.currentProgress}",
                            style = RiteAppTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = RiteAppTheme.colors.onSurface
                        )
                        Text(
                            text = habit.unit?.uppercase() ?: "",
                            style = RiteAppTheme.typography.labelSmall.copy(
                                letterSpacing = 0.5.sp
                            ),
                            color = RiteAppTheme.colors.onSurfaceVariant
                        )
                    }

                    StepperButton(text = "+", onClick = onIncrementProgress)
                }
            }
        }

        if (!habit.isResolved) {
            SkipRow(
                onSkip = onSkip,
                isSkipLocked = habit.isSkipLocked,
                trailingButton = {
                    IconSurface(
                        onClick = onCustomProgress,
                        icon = Icons.Default.Edit,
                        contentDescription = stringResource(Res.string.habit_detail_action_custom)
                    )
                }
            )
        }
    }
}

@Composable
private fun StepperButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = RiteAppTheme.colors.surface,
        enabled = enabled,
        modifier = modifier.size(STEPPER_BUTTON_SIZE)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = text,
                style = RiteAppTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = if (enabled) {
                    RiteAppTheme.colors.onSurface
                } else {
                    RiteAppTheme.colors.onSurface.copy(alpha = 0.38f)
                }
            )
        }
    }
}

@Composable
private fun IconSurface(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = RiteAppTheme.colors.surfaceContainerHigh,
        modifier = modifier.size(STEPPER_BUTTON_SIZE)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp),
                tint = RiteAppTheme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SkipRow(
    onSkip: () -> Unit,
    isSkipLocked: Boolean,
    modifier: Modifier = Modifier,
    trailingButton: @Composable (() -> Unit)? = null
) {
    val contentColor = if (!isSkipLocked) {
        RiteAppTheme.colors.onSecondaryContainer
    } else {
        RiteAppTheme.colors.onSecondaryContainer.copy(alpha = 0.38f)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            onClick = onSkip,
            shape = RoundedCornerShape(CARD_CORNER),
            color = RiteAppTheme.colors.secondaryContainer.copy(alpha = 0.4f),
            enabled = !isSkipLocked,
            modifier = Modifier.weight(1f).height(STEPPER_BUTTON_SIZE)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.SkipNext,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = stringResource(Res.string.habit_detail_action_skip),
                    style = RiteAppTheme.typography.labelLarge,
                    color = contentColor
                )
            }
        }

        if (trailingButton != null) {
            trailingButton()
        }
    }
}
