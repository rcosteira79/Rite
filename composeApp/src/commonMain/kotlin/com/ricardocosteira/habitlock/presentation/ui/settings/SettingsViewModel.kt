package com.ricardocosteira.habitlock.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricardocosteira.habitlock.di.AppScope
import com.ricardocosteira.habitlock.domain.models.UndoPolicy
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
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
class SettingsViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            userRepository.observeUser().collect { user ->
                if (user != null) {
                    _state.update {
                        it.copy(
                            undoPolicy = user.undoPolicy,
                            maxSnoozeDurationMinutes = user.maxSnoozeDurationMinutes,
                            maxSnoozesPerHabitPerDay = user.maxSnoozesPerHabitPerDay,
                            maxConsecutiveSkips = user.maxConsecutiveSkips,
                            dailySummaryTime = user.dailySummaryTime,
                            currentTimezone = user.timezone.id,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun updateUndoPolicy(policy: UndoPolicy) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                val user = userRepository.getUser() ?: return@launch
                userRepository.updateUser(user.copy(undoPolicy = policy))
                _events.emit(SettingsEvent.SettingsSaved)
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowError(e.message))
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    fun updateMaxSnoozeDuration(minutes: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                val user = userRepository.getUser() ?: return@launch
                val cappedMinutes = minutes.coerceIn(5, 60)
                userRepository.updateUser(user.copy(maxSnoozeDurationMinutes = cappedMinutes))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowError(e.message))
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    fun updateMaxSnoozesPerDay(count: Int?) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                val user = userRepository.getUser() ?: return@launch
                userRepository.updateUser(user.copy(maxSnoozesPerHabitPerDay = count))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowError(e.message))
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    fun updateMaxConsecutiveSkips(count: Int?) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                val user = userRepository.getUser() ?: return@launch
                userRepository.updateUser(user.copy(maxConsecutiveSkips = count))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowError(e.message))
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    fun updateDailySummaryTime(time: LocalTime?) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                val user = userRepository.getUser() ?: return@launch
                userRepository.updateUser(user.copy(dailySummaryTime = time))
                _events.emit(SettingsEvent.DailySummaryUpdated)
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowError(e.message))
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }
}

sealed interface SettingsEvent {
    data object SettingsSaved : SettingsEvent
    data object DailySummaryUpdated : SettingsEvent
    data class ShowError(val message: String?) : SettingsEvent
}
