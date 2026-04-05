package com.ricardocosteira.rite.presentation.ui.habitdetail

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.components.PrimaryButton
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.common_cd_back
import rite.composeapp.generated.resources.habit_detail_action_complete
import rite.composeapp.generated.resources.habit_detail_action_completed
import rite.composeapp.generated.resources.habit_detail_action_custom
import rite.composeapp.generated.resources.habit_detail_action_goal_reached
import rite.composeapp.generated.resources.habit_detail_action_skip
import rite.composeapp.generated.resources.habit_detail_action_skipped
import rite.composeapp.generated.resources.habit_detail_action_undo
import rite.composeapp.generated.resources.habit_detail_category_binary
import rite.composeapp.generated.resources.habit_detail_category_quantitative
import rite.composeapp.generated.resources.habit_detail_heatmap_title
import rite.composeapp.generated.resources.habit_detail_progress
import rite.composeapp.generated.resources.habit_detail_skips_none
import rite.composeapp.generated.resources.habit_detail_skips_remaining
import rite.composeapp.generated.resources.habit_detail_skips_unlimited
import rite.composeapp.generated.resources.habit_detail_stat_current_streak
import rite.composeapp.generated.resources.habit_detail_stat_habit_score
import rite.composeapp.generated.resources.habit_detail_stat_longest_streak

private const val FULL_PROGRESS = 100

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
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_cd_back)
                        )
                    }
                }
            )
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
                // Category label
                Text(
                    text = if (state.habit.type == HabitType.BINARY) {
                        stringResource(Res.string.habit_detail_category_binary)
                    } else {
                        stringResource(Res.string.habit_detail_category_quantitative)
                    },
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Habit name
                Text(
                    text = state.habit.name.uppercase(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Progress indicator — unified ring for both types
                ProgressRing(state = state)

                Spacer(modifier = Modifier.height(24.dp))

                // Stats row
                StatsRow(state = state)

                Spacer(modifier = Modifier.height(16.dp))

                // Accountability limits
                SkipLimitsRow(state = state)

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

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                ActionButtons(
                    state = state,
                    onComplete = onComplete,
                    onIncrementProgress = onIncrementProgress,
                    onCustomProgress = onCustomProgress,
                    onSkip = onSkip,
                    onUndo = onUndo
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProgressRing(state: HabitDetailState, modifier: Modifier = Modifier) {
    val instance = state.instance ?: return
    val habit = state.habit ?: return

    val progress: Float = if (habit.type == HabitType.BINARY) {
        if (state.isCompleted) 1f else 0f
    } else {
        instance.progressPercentage().coerceIn(0f, 1f)
    }

    val percentageText: String = if (habit.type == HabitType.BINARY) {
        if (state.isCompleted) "$FULL_PROGRESS%" else "0%"
    } else {
        "${(instance.progressPercentage() * FULL_PROGRESS).toInt()}%"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(88.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(88.dp),
                strokeWidth = 5.dp,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
            Text(
                text = percentageText,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onSurface
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

@Composable
private fun StatsRow(state: HabitDetailState, modifier: Modifier = Modifier) {
    val habit = state.habit ?: return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            value = "${habit.currentStreak}",
            label = stringResource(Res.string.habit_detail_stat_current_streak)
        )
        StatItem(
            value = "${habit.longestStreak}",
            label = stringResource(Res.string.habit_detail_stat_longest_streak)
        )
        StatItem(
            value = "${state.habitScore}",
            label = stringResource(Res.string.habit_detail_stat_habit_score)
        )
    }
}

@Composable
private fun StatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SkipLimitsRow(state: HabitDetailState, modifier: Modifier = Modifier) {
    val text: String = when {
        state.maxConsecutiveSkips == null -> stringResource(Res.string.habit_detail_skips_unlimited)
        state.isSkipLocked -> stringResource(Res.string.habit_detail_skips_none)
        else -> stringResource(Res.string.habit_detail_skips_remaining, state.skipsRemaining ?: 0)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.SkipNext,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
    modifier: Modifier = Modifier
) {
    val habit = state.habit ?: return
    val canUndo: Boolean = state.isCompleted || state.isSkipped

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.isResolved && canUndo) {
            // Undo button when completed or skipped
            PrimaryButton(onClick = onUndo) {
                Text(stringResource(Res.string.habit_detail_action_undo))
            }
        } else if (habit.type == HabitType.BINARY) {
            PrimaryButton(
                onClick = onComplete,
                enabled = !state.isResolved
            ) {
                Text(
                    text = if (state.isCompleted) {
                        stringResource(Res.string.habit_detail_action_completed)
                    } else {
                        stringResource(Res.string.habit_detail_action_complete)
                    }
                )
            }
        } else {
            // Quantitative: +N button
            PrimaryButton(
                onClick = onIncrementProgress,
                enabled = !state.isResolved
            ) {
                val increment: Int = habit.defaultIncrement
                val unit: String = habit.unit?.uppercase() ?: ""
                Text(
                    text = if (state.isQuantitativeComplete) {
                        stringResource(Res.string.habit_detail_action_goal_reached)
                    } else {
                        "+$increment $unit"
                    }
                )
            }

            // Custom button
            OutlinedButton(
                onClick = onCustomProgress,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSkipped && !state.isFailed
            ) {
                Text(stringResource(Res.string.habit_detail_action_custom))
            }
        }

        // Skip button (when not resolved)
        if (!state.isResolved) {
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSkipLocked
            ) {
                Text(
                    text = stringResource(Res.string.habit_detail_action_skip),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (!state.isSkipLocked) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    }
                )
            }
        }
    }
}
