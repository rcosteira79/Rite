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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ricardocosteira.rite.di.LocalAppComponent
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
    val createViewModel = LocalAppComponent.current.createHabitDetailViewModel
    val viewModel: HabitDetailViewModel = viewModel {
        createViewModel(instanceId)
    }
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
            name = customInputHabit.name,
            completedValue = customInputHabit.completedValue,
            targetValue = customInputHabit.targetValue,
            unit = customInputHabit.unit,
            onConfirm = { value ->
                viewModel.addCustomProgress(value)
                viewModel.dismissCustomInput()
            },
            onDismiss = viewModel::dismissCustomInput
        )
    }
}
