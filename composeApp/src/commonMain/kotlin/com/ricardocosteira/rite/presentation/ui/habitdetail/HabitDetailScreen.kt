package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.components.PrimaryButton
import com.ricardocosteira.rite.presentation.ui.components.toolbar.DynamicCollapsingToolbar
import com.ricardocosteira.rite.presentation.ui.components.toolbar.pinnedExitUntilCollapsedToolbarSpec
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
    val toolbarSpec = pinnedExitUntilCollapsedToolbarSpec(
        collapsedToolbarHeight = 64.dp
    )

    Scaffold(
        modifier = modifier.nestedScroll(toolbarSpec.nestedScrollConnection),
        topBar = {
            if (!state.isLoading && state.habit != null) {
                DynamicCollapsingToolbar(
                    toolbarSpec = toolbarSpec,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    centerContent = false,
                    stackVertically = true,
                    collapsedElevation = 0.dp,
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.common_cd_back)
                            )
                        }
                    },
                    actions = {
                        Row {
                            IconButton(onClick = onEditHabit) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(
                                        Res.string.habit_detail_cd_edit
                                    ),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            IconButton(onClick = onArchiveHabit) {
                                Icon(
                                    imageVector = Icons.Outlined.Inventory2,
                                    contentDescription = stringResource(
                                        Res.string.habit_form_cd_archive
                                    ),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            IconButton(onClick = onDeleteHabit) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = stringResource(
                                        Res.string.habit_form_cd_delete
                                    ),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                ) { scrollProgress ->
                    HabitDetailToolbarContent(
                        habitName = state.habit.name.uppercase(),
                        categoryLabel = if (state.habit.type == HabitType.BINARY) {
                            stringResource(Res.string.habit_detail_category_binary)
                        } else {
                            stringResource(Res.string.habit_detail_category_quantitative)
                        },
                        scrollProgress = scrollProgress
                    )
                }
            }
        }
    ) { paddingValues ->
        if (state.isLoading || state.habit == null || state.instance == null) {
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
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

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
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
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
private fun HabitDetailToolbarContent(
    habitName: String,
    categoryLabel: String,
    scrollProgress: Float,
    modifier: Modifier = Modifier
) {
    // Expanded: category label + habit name (full width, left-aligned)
    // As user scrolls, category fades out and name shrinks/fades
    // The collapsing toolbar handles the height reduction
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Category label — fades out first
        Text(
            text = categoryLabel,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.alpha((1f - scrollProgress * 2f).coerceAtLeast(0f))
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Habit name
        Text(
            text = habitName,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ProgressRingCard(state: HabitDetailState, modifier: Modifier = Modifier) {
    val instance = state.instance ?: return
    val habit = state.habit ?: return

    val targetProgress: Float = if (habit.type == HabitType.BINARY) {
        if (state.isCompleted) 1f else 0f
    } else {
        instance.progressPercentage().coerceIn(0f, 1f)
    }

    val percentage: Int = if (habit.type == HabitType.BINARY) {
        if (state.isCompleted) PERCENTAGE_MULTIPLIER else 0
    } else {
        (instance.progressPercentage() * PERCENTAGE_MULTIPLIER).toInt()
    }

    val sweepAngle: Float by animateFloatAsState(
        targetValue = FULL_CIRCLE_DEGREES * targetProgress,
        animationSpec = tween(durationMillis = PROGRESS_ANIMATION_DURATION),
        label = "detail-progress-ring"
    )

    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val progressColor = MaterialTheme.colorScheme.primary

    Surface(
        shape = RoundedCornerShape(CARD_CORNER),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
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
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // "X of Y UNIT" below ring (quantitative only)
            if (habit.type == HabitType.QUANTITATIVE) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        Res.string.habit_detail_progress,
                        instance.currentProgress,
                        instance.targetValue ?: 0,
                        habit.unit?.uppercase() ?: ""
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
            value = "${state.habitScore}",
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
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (suffix != null) "$value $suffix" else value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SkipLimitsCard(state: HabitDetailState, modifier: Modifier = Modifier) {
    val text: String = when {
        state.maxConsecutiveSkips == null -> stringResource(Res.string.habit_detail_skips_unlimited)
        state.isSkipLocked -> stringResource(Res.string.habit_detail_skips_none)
        else -> stringResource(Res.string.habit_detail_skips_remaining, state.skipsRemaining ?: 0)
    }

    Surface(
        shape = RoundedCornerShape(CARD_CORNER),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
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
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.isResolved && (state.isCompleted || state.isSkipped)) {
            PrimaryButton(onClick = onUndo) {
                Text(stringResource(Res.string.habit_detail_action_undo))
            }
        } else {
            PrimaryButton(
                onClick = onComplete,
                enabled = !state.isResolved
            ) {
                Text(stringResource(Res.string.habit_detail_action_complete))
            }
        }

        if (!state.isResolved) {
            SkipRow(onSkip = onSkip, isSkipLocked = state.isSkipLocked)
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
    val instance = state.instance ?: return
    val hasProgress: Boolean = instance.currentProgress > 0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.isResolved && (state.isCompleted || state.isSkipped)) {
            PrimaryButton(onClick = onUndo) {
                Text(stringResource(Res.string.habit_detail_action_undo))
            }
        } else {
            // Quantity stepper
            Surface(
                shape = RoundedCornerShape(CARD_CORNER),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        onClick = onUndoIncrement,
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        enabled = hasProgress,
                        modifier = Modifier.size(STEPPER_BUTTON_SIZE)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "−",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (hasProgress) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                }
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${instance.currentProgress}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = habit.unit?.uppercase() ?: "",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        onClick = onIncrementProgress,
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(STEPPER_BUTTON_SIZE)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        if (!state.isResolved) {
            SkipRow(
                onSkip = onSkip,
                isSkipLocked = state.isSkipLocked,
                trailingButton = {
                    Surface(
                        onClick = onCustomProgress,
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.size(STEPPER_BUTTON_SIZE)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(
                                    Res.string.habit_detail_action_custom
                                ),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            onClick = onSkip,
            shape = RoundedCornerShape(CARD_CORNER),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
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
                    tint = if (!isSkipLocked) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.38f)
                    }
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = stringResource(Res.string.habit_detail_action_skip),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (!isSkipLocked) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.38f)
                    }
                )
            }
        }

        if (trailingButton != null) {
            trailingButton()
        }
    }
}
