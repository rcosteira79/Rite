package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.rite.di.LocalAppComponent
import com.ricardocosteira.rite.domain.models.ScheduleType
import com.ricardocosteira.rite.presentation.models.TodayHabitUiModel
import com.ricardocosteira.rite.presentation.ui.today.QuantitativeInputBottomSheet

@Composable
fun HabitDetailRoute(
    instanceId: String,
    onNavigateBack: () -> Unit,
    onEditHabit: (String) -> Unit,
    onArchiveHabit: () -> Unit,
    onDeleteHabit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val factory = LocalAppComponent.current.habitDetailViewModelFactory
    val viewModel = remember { factory.create(instanceId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    HabitDetailScreen(
        state = state,
        onBackClick = onNavigateBack,
        onComplete = viewModel::completeBinary,
        onIncrementProgress = viewModel::incrementProgress,
        onCustomProgress = viewModel::showCustomInput,
        onSkip = viewModel::skip,
        onUndo = viewModel::undo,
        onUndoIncrement = viewModel::undoIncrement,
        onEditHabit = { state.habit?.id?.let(onEditHabit) },
        onArchiveHabit = onArchiveHabit,
        onDeleteHabit = onDeleteHabit,
        modifier = modifier
    )

    if (state.showCustomInput && state.habit != null && state.instance != null) {
        val habit = state.habit!!
        val instance = state.instance!!
        QuantitativeInputBottomSheet(
            habit = TodayHabitUiModel(
                instanceId = instance.id,
                habitId = habit.id,
                name = habit.name,
                description = habit.description,
                type = habit.type,
                status = instance.status,
                targetValue = instance.targetValue,
                completedValue = instance.completedValue,
                unit = habit.unit,
                defaultIncrement = habit.defaultIncrement,
                progressPercentage = instance.progressPercentage(),
                isSkipLocked = state.isSkipLocked,
                currentStreak = habit.currentStreak,
                longestStreak = habit.longestStreak,
                scorePercentage = state.habitScore,
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
