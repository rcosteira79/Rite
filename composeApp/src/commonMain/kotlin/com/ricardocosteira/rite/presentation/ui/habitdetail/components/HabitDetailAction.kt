package com.ricardocosteira.rite.presentation.ui.habitdetail.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.components.RiteButton
import com.ricardocosteira.rite.presentation.ui.components.StepperButton
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.habit_detail_action_complete
import rite.composeapp.generated.resources.habit_detail_action_custom
import rite.composeapp.generated.resources.habit_detail_action_skip
import rite.composeapp.generated.resources.habit_detail_action_undo

private val STEPPER_BUTTON_SIZE = 48.dp

/**
 * Action area for the habit detail screen.
 *
 * Renders the appropriate set of interactive controls based on [type]:
 * - [HabitType.BINARY]: a complete/undo button and an optional skip row.
 * - [HabitType.QUANTITATIVE]: a stepper widget for incrementing [currentProgress], an optional
 *   skip row with a custom-entry shortcut, and an undo button once resolved.
 *
 * All state is expressed as flat primitives so this composable has no dependency on
 * `HabitDetailUiModel` or any screen-level state holder.
 *
 * @param type Whether this is a binary or quantitative habit.
 * @param status The current resolution status of today's habit instance.
 * @param currentProgress The accumulated progress count (quantitative habits only).
 * @param unit Optional unit label displayed below the progress counter (e.g. "ml").
 * @param isSkipLocked Whether the skip action is locked (enforced by strictness limits).
 * @param onComplete Called when the user taps the "Complete" button (binary).
 * @param onIncrementProgress Called when the user taps "+" (quantitative).
 * @param onCustomProgress Called when the user taps the edit icon for custom entry (quantitative).
 * @param onSkip Called when the user taps the skip button.
 * @param onUndo Called when the user taps "Undo" after completing or skipping.
 * @param onUndoIncrement Called when the user taps "−" to decrement progress (quantitative).
 * @param modifier Optional modifier for the root [Column].
 */
@Composable
fun HabitDetailAction(
    type: HabitType,
    status: HabitStatus,
    currentProgress: Int,
    unit: String?,
    isSkipLocked: Boolean,
    onComplete: () -> Unit,
    onIncrementProgress: () -> Unit,
    onCustomProgress: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    onUndoIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted: Boolean = status == HabitStatus.COMPLETED
    val isSkipped: Boolean = status == HabitStatus.SKIPPED
    val isResolved: Boolean = isCompleted || isSkipped || status == HabitStatus.FAILED

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (type == HabitType.BINARY) {
            BinaryBlock(
                isResolved = isResolved,
                isCompletedOrSkipped = isCompleted || isSkipped,
                isSkipLocked = isSkipLocked,
                onComplete = onComplete,
                onUndo = onUndo,
                onSkip = onSkip
            )
        } else {
            QuantitativeBlock(
                isResolved = isResolved,
                isCompletedOrSkipped = isCompleted || isSkipped,
                isSkipLocked = isSkipLocked,
                currentProgress = currentProgress,
                unit = unit,
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
private fun BinaryBlock(
    isResolved: Boolean,
    isCompletedOrSkipped: Boolean,
    isSkipLocked: Boolean,
    onComplete: () -> Unit,
    onUndo: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isResolved && isCompletedOrSkipped) {
            RiteButton(onClick = onUndo) {
                Text(stringResource(Res.string.habit_detail_action_undo))
            }
        } else {
            RiteButton(onClick = onComplete, enabled = !isResolved) {
                Text(stringResource(Res.string.habit_detail_action_complete))
            }
        }
        if (!isResolved) {
            SkipRow(onSkip = onSkip, isSkipLocked = isSkipLocked)
        }
    }
}

@Composable
private fun QuantitativeBlock(
    isResolved: Boolean,
    isCompletedOrSkipped: Boolean,
    isSkipLocked: Boolean,
    currentProgress: Int,
    unit: String?,
    onIncrementProgress: () -> Unit,
    onUndoIncrement: () -> Unit,
    onCustomProgress: () -> Unit,
    onUndo: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasProgress: Boolean = currentProgress > 0
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isResolved && isCompletedOrSkipped) {
            RiteButton(onClick = onUndo) {
                Text(stringResource(Res.string.habit_detail_action_undo))
            }
        } else {
            Surface(
                shape = RiteAppTheme.shapes.xl,
                color = RiteAppTheme.colors.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StepperButton(text = "−", onClick = onUndoIncrement, enabled = hasProgress)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$currentProgress",
                            style = RiteAppTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = RiteAppTheme.colors.onSurface
                        )
                        Text(
                            text = unit?.uppercase() ?: "",
                            style = RiteAppTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                            color = RiteAppTheme.colors.onSurfaceVariant
                        )
                    }
                    StepperButton(text = "+", onClick = onIncrementProgress)
                }
            }
        }
        if (!isResolved) {
            SkipRow(
                onSkip = onSkip,
                isSkipLocked = isSkipLocked,
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
private fun IconSurface(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RiteAppTheme.shapes.lg,
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
    trailingButton: (@Composable () -> Unit)? = null
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
            shape = RiteAppTheme.shapes.xl,
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
                Spacer(Modifier.size(6.dp))
                Text(
                    text = stringResource(Res.string.habit_detail_action_skip),
                    style = RiteAppTheme.typography.labelLarge,
                    color = contentColor
                )
            }
        }
        trailingButton?.invoke()
    }
}
