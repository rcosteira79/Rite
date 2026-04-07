package com.ricardocosteira.rite.presentation.ui.today

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.ScheduleType
import com.ricardocosteira.rite.presentation.models.TodayHabitUiModel
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.common_skip
import rite.composeapp.generated.resources.today_action_complete
import rite.composeapp.generated.resources.today_action_increment_short
import rite.composeapp.generated.resources.today_cd_undo
import rite.composeapp.generated.resources.today_resolved_completed_at
import rite.composeapp.generated.resources.today_resolved_failed
import rite.composeapp.generated.resources.today_resolved_skipped_at

private val CARD_CORNER_RADIUS = 24.dp
private val RESOLVED_CORNER_RADIUS = 16.dp
private val BUTTON_CORNER_RADIUS = 12.dp
private val ACTION_ROW_GAP = 12.dp
private val PROGRESS_BAR_CORNER_RADIUS = 99.dp

private val CARD_VERTICAL_PADDING = 16.dp
private val CARD_HORIZONTAL_PADDING = 24.dp

private val HABIT_NAME_SIZE = 15.sp

private val PROGRESS_BAR_HEIGHT = 3.dp

private val RESOLVED_ICON_SIZE = 40.dp

private const val RESOLVED_ALPHA = 0.8f
private const val FAILED_ALPHA = 0.5f

@Composable
fun HabitCard(
    habit: TodayHabitUiModel,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    onIncrementProgress: () -> Unit,
    onCustomProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isResolved: Boolean = habit.isCompleted || habit.isSkipped || habit.isFailed

    if (isResolved) {
        ResolvedHabitRow(
            habit = habit,
            onUndo = onUndo,
            onClick = onClick,
            modifier = modifier
        )
    } else {
        PendingHabitCard(
            habit = habit,
            onClick = onClick,
            onComplete = onComplete,
            onSkip = onSkip,
            onUndo = onUndo,
            onIncrementProgress = onIncrementProgress,
            onCustomProgress = onCustomProgress,
            modifier = modifier
        )
    }
}

@Composable
private fun PendingHabitCard(
    habit: TodayHabitUiModel,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    onIncrementProgress: () -> Unit,
    onCustomProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentValue: Int = habit.completedValue ?: 0
    val targetValue: Int = habit.targetValue ?: 0
    val unitText: String = habit.unit?.uppercase() ?: ""
    val isQuantitative: Boolean = habit.type == HabitType.QUANTITATIVE

    Surface(
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CARD_CORNER_RADIUS))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = CARD_HORIZONTAL_PADDING,
                vertical = CARD_VERTICAL_PADDING
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (isQuantitative) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$currentValue",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "/ $targetValue $unitText".trim(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = habit.name.uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = HABIT_NAME_SIZE,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (habit.description != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(ACTION_ROW_GAP))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(ACTION_ROW_GAP),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isQuantitative) {
                        val incrementLabel: String = stringResource(
                            Res.string.today_action_increment_short,
                            habit.defaultIncrement
                        )
                        Surface(
                            shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            onClick = onIncrementProgress
                        ) {
                            Text(
                                text = incrementLabel,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            onClick = onComplete
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(
                                    Res.string.today_action_complete
                                ),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    if (!habit.isSkipLocked) {
                        Text(
                            text = stringResource(Res.string.common_skip).uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(BUTTON_CORNER_RADIUS))
                                .clickable(onClick = onSkip)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (isQuantitative) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PROGRESS_BAR_HEIGHT)
                        .clip(RoundedCornerShape(PROGRESS_BAR_CORNER_RADIUS))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = habit.progressPercentage)
                            .height(PROGRESS_BAR_HEIGHT)
                            .clip(RoundedCornerShape(PROGRESS_BAR_CORNER_RADIUS))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

@Composable
private fun ResolvedHabitRow(
    habit: TodayHabitUiModel,
    onUndo: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFailed: Boolean = habit.isFailed
    val rowAlpha: Float = if (isFailed) FAILED_ALPHA else RESOLVED_ALPHA

    Surface(
        shape = RoundedCornerShape(RESOLVED_CORNER_RADIUS),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = rowAlpha),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Check icon circle
            Box(
                modifier = Modifier
                    .size(RESOLVED_ICON_SIZE)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name and subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name.uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (isFailed) Modifier.alpha(FAILED_ALPHA) else Modifier
                )

                val subtitle: String = when {
                    habit.isCompleted -> {
                        stringResource(
                            Res.string.today_resolved_completed_at,
                            habit.completedAtText ?: ""
                        ).uppercase()
                    }

                    habit.isSkipped -> {
                        stringResource(
                            Res.string.today_resolved_skipped_at,
                            habit.completedAtText ?: ""
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
                    modifier = if (isFailed) Modifier.alpha(FAILED_ALPHA) else Modifier
                )
            }

            // Undo button — only for completed and skipped, not failed
            if (!isFailed) {
                IconButton(onClick = onUndo) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = stringResource(Res.string.today_cd_undo),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

// --- Previews ---

@Preview
@Composable
private fun BinaryCollapsedPreview() {
    RiteThemeFallback {
        HabitCard(
            habit = previewBinaryHabit(),
            onClick = {},
            onComplete = {},
            onSkip = {},
            onUndo = {},
            onIncrementProgress = {},
            onCustomProgress = {}
        )
    }
}

private fun previewBinaryHabit(
    status: HabitStatus = HabitStatus.PENDING,
    completedAtText: String? = null
): TodayHabitUiModel = TodayHabitUiModel(
    instanceId = "preview-1",
    habitId = "habit-1",
    name = "Morning Meditation",
    description = "10 minutes of mindfulness",
    type = HabitType.BINARY,
    status = status,
    completedValue = null,
    targetValue = null,
    unit = null,
    defaultIncrement = 1,
    progressPercentage = 0f,
    isSkipLocked = false,
    currentStreak = 5,
    longestStreak = 12,
    scorePercentage = 85,
    cadence = ScheduleType.DAILY,
    completedAtText = completedAtText
)
