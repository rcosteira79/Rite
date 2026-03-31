package com.ricardocosteira.habitlock.presentation.ui.archived

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricardocosteira.habitlock.di.AppScope
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

/**
 * Scoped to the application lifetime via [AppScope] rather than a
 * [androidx.lifecycle.ViewModelStoreOwner] because this app uses a single-activity
 * architecture and all ViewModels are obtained directly from the DI component.
 * Composable-scoped ViewModels (rememberViewModelStoreOwner) would be more
 * semantically correct but require lifecycle 2.11.0-alpha02 and a custom
 * ViewModelProvider.Factory bridge. Revisit when that API stabilises.
 */
@AppScope
@Inject
class ArchivedHabitsViewModel(private val habitRepository: HabitRepository) : ViewModel() {

    private val _state = MutableStateFlow(ArchivedHabitsState())
    val state: StateFlow<ArchivedHabitsState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ArchivedHabitsEvent>()
    val events: SharedFlow<ArchivedHabitsEvent> = _events.asSharedFlow()

    init {
        observeArchivedHabits()
    }

    private fun observeArchivedHabits() {
        viewModelScope.launch {
            habitRepository.observeArchivedHabits().collect { habits ->
                _state.update {
                    it.copy(
                        habits = habits,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun unarchiveHabit(habitId: String) {
        viewModelScope.launch {
            try {
                habitRepository.unarchiveHabit(habitId)
                _events.emit(ArchivedHabitsEvent.HabitRestored)
            } catch (e: Exception) {
                _events.emit(ArchivedHabitsEvent.ShowError(e.message))
            }
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            try {
                habitRepository.deleteHabit(habitId)
                _events.emit(ArchivedHabitsEvent.HabitDeleted)
            } catch (e: Exception) {
                _events.emit(ArchivedHabitsEvent.ShowError(e.message))
            }
        }
    }
}

sealed interface ArchivedHabitsEvent {
    data object HabitRestored : ArchivedHabitsEvent
    data object HabitDeleted : ArchivedHabitsEvent
    data class ShowError(val message: String?) : ArchivedHabitsEvent
}
