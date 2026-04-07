package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.rite.di.LocalAppComponent
import com.ricardocosteira.rite.domain.models.ScheduleType
import com.ricardocosteira.rite.presentation.models.TodayHabitUiModel
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import com.ricardocosteira.rite.presentation.ui.today.QuantitativeInputBottomSheet
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.habit_form_delete_dialog_body
import rite.composeapp.generated.resources.habit_form_delete_dialog_cancel
import rite.composeapp.generated.resources.habit_form_delete_dialog_confirm
import rite.composeapp.generated.resources.habit_form_delete_dialog_title

@Composable
fun HabitDetailRoute(
    instanceId: String,
    onNavigateBack: () -> Unit,
    onEditHabit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val factory = LocalAppComponent.current.habitDetailViewModelFactory
    val viewModel = remember { factory.create(instanceId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isDeleteDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HabitDetailEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    HabitDetailScreen(
        state = state,
        onBackClick = onNavigateBack,
        onComplete = viewModel::completeBinary,
        onIncrementProgress = viewModel::incrementProgress,
        onCustomProgress = viewModel::showCustomInput,
        onSkip = viewModel::skip,
        onUndo = viewModel::undo,
        onUndoIncrement = viewModel::undoIncrement,
        onEditHabit = { state.habit?.habitId?.let(onEditHabit) },
        onArchiveHabit = viewModel::archiveHabit,
        onDeleteHabit = { isDeleteDialogVisible = true },
        modifier = modifier
    )

    if (isDeleteDialogVisible) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogVisible = false },
            title = { Text(stringResource(Res.string.habit_form_delete_dialog_title)) },
            text = { Text(stringResource(Res.string.habit_form_delete_dialog_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleteDialogVisible = false
                        viewModel.deleteHabit()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = RiteAppTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(Res.string.habit_form_delete_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { isDeleteDialogVisible = false }) {
                    Text(stringResource(Res.string.habit_form_delete_dialog_cancel))
                }
            }
        )
    }

    val customInputHabit = state.habit
    if (state.showCustomInput && customInputHabit != null) {
        QuantitativeInputBottomSheet(
            habit = TodayHabitUiModel(
                instanceId = customInputHabit.instanceId,
                habitId = customInputHabit.habitId,
                name = customInputHabit.name,
                description = customInputHabit.description,
                type = customInputHabit.type,
                status = customInputHabit.status,
                targetValue = customInputHabit.targetValue,
                completedValue = customInputHabit.completedValue,
                unit = customInputHabit.unit,
                defaultIncrement = customInputHabit.defaultIncrement,
                progressPercentage = customInputHabit.progressPercentage,
                isSkipLocked = customInputHabit.isSkipLocked,
                currentStreak = customInputHabit.currentStreak,
                longestStreak = customInputHabit.longestStreak,
                scorePercentage = customInputHabit.habitScore,
                cadence = ScheduleType.DAILY,
                completedAtText = null
            ),
            onConfirm = { value ->
                viewModel.addCustomProgress(value)
                viewModel.dismissCustomInput()
            },
            onDismiss = viewModel::dismissCustomInput
        )
    }
}
