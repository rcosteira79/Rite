package com.ricardocosteira.habitlock.presentation.ui.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.common_skip
import habitlock.composeapp.generated.resources.today_action_complete
import habitlock.composeapp.generated.resources.today_action_custom
import habitlock.composeapp.generated.resources.today_action_increment
import habitlock.composeapp.generated.resources.today_action_increment_short
import habitlock.composeapp.generated.resources.today_badge_in_progress
import habitlock.composeapp.generated.resources.today_badge_pending
import habitlock.composeapp.generated.resources.today_cd_undo
import habitlock.composeapp.generated.resources.today_resolved_completed_at
import habitlock.composeapp.generated.resources.today_resolved_failed
import habitlock.composeapp.generated.resources.today_resolved_skipped_at
import org.jetbrains.compose.resources.stringResource

private val CARD_CORNER_RADIUS = 24.dp
private val RESOLVED_CORNER_RADIUS = 16.dp
private val BUTTON_CORNER_RADIUS = 12.dp
private val BUTTON_HEIGHT = 56.dp
private val ACTION_ROW_GAP = 12.dp
private val PROGRESS_BAR_CORNER_RADIUS = 99.dp

private val COLLAPSED_VERTICAL_PADDING = 16.dp
private val COLLAPSED_HORIZONTAL_PADDING = 24.dp
private val EXPANDED_PADDING = 24.dp

private val HABIT_NAME_COLLAPSED_SIZE = 15.sp
private val HABIT_NAME_EXPANDED_SIZE = 18.sp

private val PROGRESS_BAR_COLLAPSED_HEIGHT = 3.dp
private val PROGRESS_BAR_EXPANDED_HEIGHT = 8.dp

private val RESOLVED_ICON_SIZE = 40.dp
private val RESOLVED_ALPHA = 0.8f
private val FAILED_ALPHA = 0.5f

@Composable
fun HabitCard(
    habit: TodayHabitUiModel,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    onIncrementProgress: () -> Unit,
    onCustomProgress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isResolved: Boolean = habit.isCompleted || habit.isSkipped || habit.isFailed

    if (isResolved) {
        ResolvedHabitRow(
            habit = habit,
            onUndo = onUndo,
            modifier = modifier,
        )
    } else {
        PendingHabitCard(
            habit = habit,
            isExpanded = isExpanded,
            onToggleExpand = onToggleExpand,
            onComplete = onComplete,
            onSkip = onSkip,
            onUndo = onUndo,
            onIncrementProgress = onIncrementProgress,
            onCustomProgress = onCustomProgress,
            modifier = modifier,
        )
    }
}

@Composable
private fun PendingHabitCard(
    habit: TodayHabitUiModel,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    onIncrementProgress: () -> Unit,
    onCustomProgress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentValue: Int = habit.completedValue ?: 0
    val targetValue: Int = habit.targetValue ?: 0
    val unitText: String = habit.unit?.uppercase() ?: ""
    val isQuantitative: Boolean = habit.type == HabitType.QUANTITATIVE
    val hasProgress: Boolean = currentValue > 0

    Surface(
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CARD_CORNER_RADIUS))
                .clickable(onClick = onToggleExpand),
    ) {
        Column(
            modifier =
                Modifier.padding(
                    horizontal = COLLAPSED_HORIZONTAL_PADDING,
                    vertical = COLLAPSED_VERTICAL_PADDING,
                ),
        ) {
            // Header row: always visible
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Quantitative: progress counter
                    if (isQuantitative) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$currentValue",
                                style =
                                    MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "/ $targetValue $unitText".trim(),
                                style =
                                    MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                    ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }
                    }

                    // Habit name: always visible
                    Text(
                        text = habit.name.uppercase(),
                        style =
                            MaterialTheme.typography.titleSmall.copy(
                                fontSize = if (isExpanded) HABIT_NAME_EXPANDED_SIZE else HABIT_NAME_COLLAPSED_SIZE,
                                fontWeight = FontWeight.Bold,
                            ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (isExpanded) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    // Description: always visible if present
                    if (habit.description != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isExpanded) habit.description.uppercase() else habit.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (isExpanded) 2 else 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(ACTION_ROW_GAP))

                // Collapsed quick actions
                AnimatedVisibility(
                    visible = !isExpanded,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(ACTION_ROW_GAP),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isQuantitative) {
                            val incrementLabel: String =
                                stringResource(
                                    Res.string.today_action_increment_short,
                                    habit.defaultIncrement,
                                )
                            Surface(
                                shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                onClick = onIncrementProgress,
                            ) {
                                Text(
                                    text = incrementLabel,
                                    style =
                                        MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                onClick = onComplete,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = stringResource(Res.string.today_action_complete),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(8.dp),
                                )
                            }
                        }

                        if (!habit.isSkipLocked) {
                            Text(
                                text = stringResource(Res.string.common_skip).uppercase(),
                                style =
                                    MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier =
                                    Modifier
                                        .clip(RoundedCornerShape(BUTTON_CORNER_RADIUS))
                                        .clickable(onClick = onSkip)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }

                // Expanded badge
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    StatusBadge(
                        text =
                            if (isQuantitative && hasProgress) {
                                stringResource(Res.string.today_badge_in_progress)
                            } else {
                                stringResource(Res.string.today_badge_pending)
                            },
                    )
                }
            }

            // Progress bar: quantitative only, height animates
            if (isQuantitative) {
                Spacer(modifier = Modifier.height(if (isExpanded) 16.dp else 10.dp))
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(if (isExpanded) PROGRESS_BAR_EXPANDED_HEIGHT else PROGRESS_BAR_COLLAPSED_HEIGHT)
                            .clip(RoundedCornerShape(PROGRESS_BAR_CORNER_RADIUS))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(fraction = habit.progressPercentage)
                                .height(if (isExpanded) PROGRESS_BAR_EXPANDED_HEIGHT else PROGRESS_BAR_COLLAPSED_HEIGHT)
                                .clip(RoundedCornerShape(PROGRESS_BAR_CORNER_RADIUS))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    )
                }
            }

            // Expanded action buttons (slide in from bottom)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ACTION_ROW_GAP),
                    ) {
                        if (isQuantitative) {
                            val incrementLabel: String =
                                stringResource(
                                    Res.string.today_action_increment,
                                    habit.defaultIncrement,
                                    unitText,
                                )
                            ActionButton(
                                text = incrementLabel,
                                onClick = onIncrementProgress,
                                isPrimary = true,
                                modifier = Modifier.weight(1f),
                            )
                            ActionButton(
                                text = stringResource(Res.string.today_action_custom),
                                onClick = onCustomProgress,
                                isPrimary = false,
                            )
                        } else {
                            ActionButton(
                                text = stringResource(Res.string.today_action_complete),
                                onClick = onComplete,
                                isPrimary = true,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        if (!habit.isSkipLocked) {
                            ActionButton(
                                text = stringResource(Res.string.common_skip),
                                onClick = onSkip,
                                isPrimary = false,
                            )
                        }

                        if (isQuantitative && hasProgress) {
                            IconButton(onClick = onUndo) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = stringResource(Res.string.today_cd_undo),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollapsedPendingCard(
    habit: TodayHabitUiModel,
    onToggleExpand: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onIncrementProgress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CARD_CORNER_RADIUS))
                .clickable(onClick = onToggleExpand),
    ) {
        Column(
            modifier =
                Modifier.padding(
                    horizontal = COLLAPSED_HORIZONTAL_PADDING,
                    vertical = COLLAPSED_VERTICAL_PADDING,
                ),
        ) {
            when (habit.type) {
                HabitType.BINARY -> {
                    CollapsedBinaryContent(
                        habit = habit,
                        onComplete = onComplete,
                        onSkip = onSkip,
                    )
                }

                HabitType.QUANTITATIVE -> {
                    CollapsedQuantitativeContent(
                        habit = habit,
                        onIncrementProgress = onIncrementProgress,
                        onSkip = onSkip,
                    )
                }
            }
        }
    }
}

@Composable
private fun CollapsedBinaryContent(
    habit: TodayHabitUiModel,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.name.uppercase(),
                style =
                    MaterialTheme.typography.titleSmall.copy(
                        fontSize = HABIT_NAME_COLLAPSED_SIZE,
                        fontWeight = FontWeight.Bold,
                    ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (habit.description != null) {
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.width(ACTION_ROW_GAP))

        Row(
            horizontalArrangement = Arrangement.spacedBy(ACTION_ROW_GAP),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
                color = MaterialTheme.colorScheme.primaryContainer,
                onClick = onComplete,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(Res.string.today_action_complete),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(8.dp),
                )
            }

            if (!habit.isSkipLocked) {
                Text(
                    text = stringResource(Res.string.common_skip).uppercase(),
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(BUTTON_CORNER_RADIUS))
                            .clickable(onClick = onSkip)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun CollapsedQuantitativeContent(
    habit: TodayHabitUiModel,
    onIncrementProgress: () -> Unit,
    onSkip: () -> Unit,
) {
    val currentValue: Int = habit.completedValue ?: 0
    val targetValue: Int = habit.targetValue ?: 0
    val unitText: String = habit.unit?.uppercase() ?: ""

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$currentValue",
                        style =
                            MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    Text(
                        text = "/ $targetValue $unitText".trim(),
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                            ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }

                Text(
                    text = habit.name.uppercase(),
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontSize = HABIT_NAME_COLLAPSED_SIZE,
                            fontWeight = FontWeight.Bold,
                        ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(ACTION_ROW_GAP))

            Row(
                horizontalArrangement = Arrangement.spacedBy(ACTION_ROW_GAP),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    onClick = onIncrementProgress,
                ) {
                    Text(
                        text =
                            stringResource(
                                Res.string.today_action_increment_short,
                                habit.defaultIncrement,
                            ),
                        style =
                            MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }

                if (!habit.isSkipLocked) {
                    Text(
                        text = stringResource(Res.string.common_skip).uppercase(),
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(BUTTON_CORNER_RADIUS))
                                .clickable(onClick = onSkip)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(PROGRESS_BAR_COLLAPSED_HEIGHT)
                    .clip(RoundedCornerShape(PROGRESS_BAR_CORNER_RADIUS))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(fraction = habit.progressPercentage.coerceIn(0f, 1f))
                        .height(PROGRESS_BAR_COLLAPSED_HEIGHT)
                        .clip(RoundedCornerShape(PROGRESS_BAR_CORNER_RADIUS))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            )
        }
    }
}

@Composable
private fun ExpandedPendingCard(
    habit: TodayHabitUiModel,
    onToggleExpand: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    onIncrementProgress: () -> Unit,
    onCustomProgress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CARD_CORNER_RADIUS))
                .clickable(onClick = onToggleExpand),
    ) {
        Column(
            modifier = Modifier.padding(EXPANDED_PADDING),
        ) {
            when (habit.type) {
                HabitType.BINARY -> {
                    ExpandedBinaryContent(
                        habit = habit,
                        onComplete = onComplete,
                        onSkip = onSkip,
                    )
                }

                HabitType.QUANTITATIVE -> {
                    ExpandedQuantitativeContent(
                        habit = habit,
                        onSkip = onSkip,
                        onUndo = onUndo,
                        onIncrementProgress = onIncrementProgress,
                        onCustomProgress = onCustomProgress,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedBinaryContent(
    habit: TodayHabitUiModel,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
) {
    // Header row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.name.uppercase(),
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontSize = HABIT_NAME_EXPANDED_SIZE,
                        fontWeight = FontWeight.Bold,
                    ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (habit.description != null) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = habit.description.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.width(ACTION_ROW_GAP))

        StatusBadge(text = stringResource(Res.string.today_badge_pending).uppercase())
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Action row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ACTION_ROW_GAP),
    ) {
        ActionButton(
            text = stringResource(Res.string.today_action_complete).uppercase(),
            onClick = onComplete,
            isPrimary = true,
            modifier = Modifier.weight(1f),
        )

        if (!habit.isSkipLocked) {
            ActionButton(
                text = stringResource(Res.string.common_skip).uppercase(),
                onClick = onSkip,
                isPrimary = false,
                modifier = Modifier,
            )
        }
    }
}

@Composable
private fun ExpandedQuantitativeContent(
    habit: TodayHabitUiModel,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    onIncrementProgress: () -> Unit,
    onCustomProgress: () -> Unit,
) {
    val currentValue: Int = habit.completedValue ?: 0
    val targetValue: Int = habit.targetValue ?: 0
    val unitText: String = habit.unit?.uppercase() ?: ""
    val hasProgress: Boolean = currentValue > 0

    // Header row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$currentValue",
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.width(2.dp))

                Text(
                    text = "/ $targetValue $unitText".trim(),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = habit.name.uppercase(),
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontSize = HABIT_NAME_EXPANDED_SIZE,
                        fontWeight = FontWeight.Bold,
                    ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(ACTION_ROW_GAP))

        val badgeText: String =
            if (hasProgress) {
                stringResource(Res.string.today_badge_in_progress).uppercase()
            } else {
                stringResource(Res.string.today_badge_pending).uppercase()
            }
        StatusBadge(text = badgeText)
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Progress bar
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(PROGRESS_BAR_EXPANDED_HEIGHT)
                .clip(RoundedCornerShape(PROGRESS_BAR_CORNER_RADIUS))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(fraction = habit.progressPercentage.coerceIn(0f, 1f))
                    .height(PROGRESS_BAR_EXPANDED_HEIGHT)
                    .clip(RoundedCornerShape(PROGRESS_BAR_CORNER_RADIUS))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Action row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ACTION_ROW_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionButton(
            text =
                stringResource(
                    Res.string.today_action_increment,
                    habit.defaultIncrement,
                    unitText,
                ).trim(),
            onClick = onIncrementProgress,
            isPrimary = true,
            modifier = Modifier.weight(1f),
        )

        ActionButton(
            text = stringResource(Res.string.today_action_custom).uppercase(),
            onClick = onCustomProgress,
            isPrimary = false,
            modifier = Modifier,
        )

        if (!habit.isSkipLocked) {
            ActionButton(
                text = stringResource(Res.string.common_skip).uppercase(),
                onClick = onSkip,
                isPrimary = false,
                modifier = Modifier,
            )
        }

        if (hasProgress) {
            IconButton(onClick = onUndo) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = stringResource(Res.string.today_cd_undo),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ResolvedHabitRow(
    habit: TodayHabitUiModel,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFailed: Boolean = habit.isFailed
    val rowAlpha: Float = if (isFailed) FAILED_ALPHA else RESOLVED_ALPHA

    Surface(
        shape = RoundedCornerShape(RESOLVED_CORNER_RADIUS),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = rowAlpha),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Check icon circle
            Box(
                modifier =
                    Modifier
                        .size(RESOLVED_ICON_SIZE)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name and subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name.uppercase(),
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (isFailed) Modifier.alpha(FAILED_ALPHA) else Modifier,
                )

                val subtitle: String =
                    when {
                        habit.isCompleted -> {
                            stringResource(
                                Res.string.today_resolved_completed_at,
                                habit.completedAtText ?: "",
                            ).uppercase()
                        }

                        habit.isSkipped -> {
                            stringResource(
                                Res.string.today_resolved_skipped_at,
                                habit.completedAtText ?: "",
                            ).uppercase()
                        }

                        habit.isFailed -> {
                            stringResource(Res.string.today_resolved_failed).uppercase()
                        }

                        else -> {
                            ""
                        }
                    }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = if (isFailed) Modifier.alpha(FAILED_ALPHA) else Modifier,
                )
            }

            // Undo button — only for completed and skipped, not failed
            if (!isFailed) {
                IconButton(onClick = onUndo) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = stringResource(Res.string.today_cd_undo),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Text(
            text = text,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier,
) {
    val backgroundColor: Color =
        if (isPrimary) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        }

    val textColor: Color =
        if (isPrimary) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    Surface(
        shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
        color = backgroundColor,
        onClick = onClick,
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .height(BUTTON_HEIGHT)
                    .then(if (isPrimary) Modifier.fillMaxWidth() else Modifier)
                    .padding(horizontal = 16.dp),
        ) {
            Text(
                text = text,
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = textColor,
            )
        }
    }
}
