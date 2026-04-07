package com.ricardocosteira.rite.presentation.ui.habitdetail

sealed interface HabitDetailEvent {
    data object NavigateBack : HabitDetailEvent
}
