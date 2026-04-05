package com.ricardocosteira.rite.presentation.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitReminder
import com.ricardocosteira.rite.domain.models.HabitSchedule
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.ReminderType
import com.ricardocosteira.rite.domain.models.ScheduleType
import com.ricardocosteira.rite.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.rite.domain.repositories.HabitRepository
import com.ricardocosteira.rite.domain.usecases.CreateHabit
import com.ricardocosteira.rite.domain.usecases.UuidProvider
import com.ricardocosteira.rite.notifications.HabitNotification
import com.ricardocosteira.rite.util.toLocalDate
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import me.tatarka.inject.annotations.Inject

private class HabitNotFoundException : Exception()

@Inject
class HabitFormViewModel(
    private val habitRepository: HabitRepository,
    private val habitInstanceRepository: HabitInstanceRepository,
    private val createHabit: CreateHabit,
    private val uuidProvider: UuidProvider,
    private val habitNotification: HabitNotification,
    private val habitIdToEdit: String? = null
) : ViewModel() {
    private val _state = MutableStateFlow(HabitFormState())

    private companion object {
        private val DEFAULT_REMINDER_TIME = HabitFormState.DEFAULT_REMINDER_TIME
        private val DEFAULT_PERIODIC_START_TIME = LocalTime(8, 0)
        private val DEFAULT_PERIODIC_END_TIME = LocalTime(22, 0)
    }

    val state: StateFlow<HabitFormState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<HabitFormEvent>()
    val events: SharedFlow<HabitFormEvent> = _events.asSharedFlow()

    private var originalState: HabitFormState? = null

    init {
        _state.update {
            it.copy(
                isNotificationPermissionGranted = habitNotification.isNotificationPermissionGranted()
            )
        }
        if (habitIdToEdit != null) {
            loadHabit(habitIdToEdit)
        }
    }

    /**
     * Factory interface for creating HabitFormViewModel instances.
     * Used by dependency injection to allow dynamic habit ID parameter.
     */
    interface Factory {
        fun create(habitIdToEdit: String? = null): HabitFormViewModel
    }

    private fun loadHabit(habitId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val habit = habitRepository.getHabitById(habitId)
                val reminders = habitRepository.getRemindersForHabit(habitId)
                val reminder = reminders.firstOrNull()
                val schedule = habitRepository.getScheduleForHabit(habitId)

                if (habit != null) {
                    _state.update {
                        it.copy(
                            habitId = habit.id,
                            name = habit.name,
                            description = habit.description ?: "",
                            type = habit.type,
                            targetValue = habit.targetValue?.toString() ?: "",
                            unit = habit.unit ?: "",
                            scheduleType = schedule?.scheduleType ?: ScheduleType.DAILY,
                            selectedDays = schedule?.specificDays ?: DayOfWeek.entries.toSet(),
                            quota = schedule?.quota?.toString() ?: "1",
                            hasReminder = reminder != null,
                            reminderType = reminder?.reminderType ?: ReminderType.FIXED,
                            reminderTime = reminder?.time,
                            intervalMinutes = reminder?.intervalMinutes?.toString() ?: "60",
                            startTime = reminder?.startTime,
                            endTime = reminder?.endTime,
                            isTrackingEnabled = habit.isTrackingEnabled,
                            isLoading = false
                        )
                    }
                    originalState = _state.value
                } else {
                    _events.emit(HabitFormEvent.HabitNotFound)
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateName(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun updateDescription(description: String) {
        _state.update { it.copy(description = description) }
    }

    fun updateType(type: HabitType) {
        _state.update { it.copy(type = type) }
    }

    fun updateTargetValue(targetValue: String) {
        _state.update { it.copy(targetValue = targetValue) }
    }

    fun updateUnit(unit: String) {
        _state.update { it.copy(unit = unit) }
    }

    fun updateScheduleType(scheduleType: ScheduleType) {
        _state.update { it.copy(scheduleType = scheduleType) }
    }

    fun updateSelectedDays(days: Set<DayOfWeek>) {
        _state.update { it.copy(selectedDays = days) }
    }

    fun updateQuota(quota: String) {
        _state.update { it.copy(quota = quota) }
    }

    fun updateHasReminder(hasReminder: Boolean) {
        _state.update {
            it.copy(
                hasReminder = hasReminder,
                reminderTime = if (hasReminder && it.reminderTime == null) {
                    DEFAULT_REMINDER_TIME
                } else {
                    it.reminderTime
                }
            )
        }
    }

    fun updateReminderType(reminderType: ReminderType) {
        _state.update {
            when (reminderType) {
                ReminderType.PERIODIC -> it.copy(
                    reminderType = reminderType,
                    startTime = it.startTime ?: DEFAULT_PERIODIC_START_TIME,
                    endTime = it.endTime ?: DEFAULT_PERIODIC_END_TIME
                )

                ReminderType.FIXED -> it.copy(
                    reminderType = reminderType,
                    startTime = null,
                    endTime = null
                )
            }
        }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        _state.update { it.copy(reminderTime = LocalTime(hour, minute)) }
    }

    fun updateIntervalMinutes(interval: String) {
        _state.update { it.copy(intervalMinutes = interval) }
    }

    fun updateStartTime(time: LocalTime) {
        _state.update { it.copy(startTime = time) }
    }

    fun updateEndTime(time: LocalTime) {
        _state.update { it.copy(endTime = time) }
    }

    fun updatePeriodicStartTime(hour: Int, minute: Int) {
        _state.update { it.copy(startTime = LocalTime(hour, minute)) }
    }

    fun updatePeriodicEndTime(hour: Int, minute: Int) {
        _state.update { it.copy(endTime = LocalTime(hour, minute)) }
    }

    fun discardDraft() {
        viewModelScope.launch { _events.emit(HabitFormEvent.NavigateBack) }
    }

    fun discardChanges() {
        originalState?.let { _state.value = it }
        viewModelScope.launch { _events.emit(HabitFormEvent.NavigateBack) }
    }

    fun saveHabit() {
        val currentState = _state.value

        if (!currentState.isValid) {
            viewModelScope.launch {
                _events.emit(HabitFormEvent.RequiredFieldsMissing)
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            try {
                val reminder = buildReminder(currentState)

                if (currentState.isEditing) {
                    updateExistingHabit(currentState, reminder)
                } else {
                    createNewHabit(currentState, reminder)
                }

                _events.emit(HabitFormEvent.NavigateBack)
            } catch (e: HabitNotFoundException) {
                _state.update { it.copy(isSaving = false) }
                _events.emit(HabitFormEvent.HabitNotFound)
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message) }
                _events.emit(HabitFormEvent.ShowError(e.message))
            }
        }
    }

    private fun buildReminder(state: HabitFormState): HabitReminder? {
        if (!state.hasReminder) return null
        return HabitReminder(
            id = "",
            habitId = "",
            reminderType = state.reminderType,
            time = if (state.reminderType == ReminderType.FIXED) state.reminderTime else null,
            intervalMinutes = if (state.reminderType == ReminderType.PERIODIC) {
                state.intervalMinutes.toIntOrNull()
            } else {
                null
            },
            startTime = if (state.reminderType == ReminderType.PERIODIC) state.startTime else null,
            endTime = if (state.reminderType == ReminderType.PERIODIC) state.endTime else null,
            isActive = true
        )
    }

    private suspend fun createNewHabit(state: HabitFormState, reminder: HabitReminder?) {
        val today = Clock.System.now().toLocalDate(TimeZone.currentSystemDefault())
        val specificDays = if (state.scheduleType == ScheduleType.WEEKLY) {
            state.selectedDays
        } else {
            null
        }

        val habit: Habit = createHabit
            .execute(
                params = CreateHabit.CreateHabitParams(
                    name = state.name.trim(),
                    description = state.description.trim().takeIf { it.isNotEmpty() },
                    type = state.type,
                    targetValue = if (state.type == HabitType.QUANTITATIVE) {
                        state.targetValue.toIntOrNull()
                    } else {
                        null
                    },
                    unit = state.unit.trim().takeIf { it.isNotEmpty() },
                    scheduleType = state.scheduleType,
                    quota = state.quota.toIntOrNull() ?: 1,
                    specificDays = specificDays,
                    reminder = reminder,
                    isTrackingEnabled = state.isTrackingEnabled
                ),
                startDate = today
            ).getOrThrow()

        if (reminder != null) {
            val savedReminders: List<HabitReminder> = habitRepository.getRemindersForHabit(habit.id)
            val savedReminder: HabitReminder? = savedReminders.firstOrNull()
            val instance: HabitInstance? =
                habitInstanceRepository.getInstanceForHabitAndDate(habit.id, today)
            if (savedReminder != null && instance != null) {
                habitNotification.scheduleReminder(habit, savedReminder, instance)
            }
        }
    }

    private suspend fun updateExistingHabit(state: HabitFormState, reminder: HabitReminder?) {
        val habitId = state.habitId!!
        val existingHabit = habitRepository.getHabitById(habitId) ?: throw HabitNotFoundException()

        val updatedHabit = existingHabit.copy(
            name = state.name.trim(),
            description = state.description.trim().takeIf { it.isNotEmpty() },
            type = state.type,
            targetValue = if (state.type == HabitType.QUANTITATIVE) {
                state.targetValue.toIntOrNull()
            } else {
                null
            },
            unit = state.unit.trim().takeIf { it.isNotEmpty() },
            isTrackingEnabled = state.isTrackingEnabled
        )

        habitRepository.updateHabit(updatedHabit)

        val specificDays = if (state.scheduleType == ScheduleType.WEEKLY) {
            state.selectedDays
        } else {
            null
        }
        val existingSchedule = habitRepository.getScheduleForHabit(habitId)

        if (existingSchedule != null) {
            habitRepository.updateSchedule(
                existingSchedule.copy(
                    scheduleType = state.scheduleType,
                    quota = state.quota.toIntOrNull() ?: 1,
                    specificDays = specificDays
                )
            )
        } else {
            val today = Clock.System.now().toLocalDate(TimeZone.currentSystemDefault())
            habitRepository.createScheduleForHabit(
                HabitSchedule(
                    id = uuidProvider.generate(),
                    habitId = habitId,
                    scheduleType = state.scheduleType,
                    startDate = today,
                    endDate = null,
                    quota = state.quota.toIntOrNull() ?: 1,
                    specificDays = specificDays
                )
            )
        }

        val existingReminders = habitRepository.getRemindersForHabit(habitId)
        existingReminders.forEach { habitRepository.deleteReminder(it.id) }

        if (reminder != null) {
            habitRepository.createReminderForHabit(
                reminder.copy(habitId = habitId, id = uuidProvider.generate())
            )
        }

        val today: LocalDate =
            Clock.System.now().toLocalDate(TimeZone.currentSystemDefault())
        val todayInstance: HabitInstance? =
            habitInstanceRepository.getInstanceForHabitAndDate(habitId, today)

        if (todayInstance != null) {
            habitNotification.cancelReminder(todayInstance.id)

            if (reminder != null) {
                val savedReminders: List<HabitReminder> =
                    habitRepository.getRemindersForHabit(habitId)
                val savedReminder: HabitReminder? = savedReminders.firstOrNull()
                if (savedReminder != null) {
                    habitNotification.scheduleReminder(updatedHabit, savedReminder, todayInstance)
                }
            }
        }
    }

    fun deleteHabit() {
        val habitId: String = _state.value.habitId ?: return

        viewModelScope.launch {
            try {
                val today: LocalDate =
                    Clock.System.now().toLocalDate(TimeZone.currentSystemDefault())
                val instance: HabitInstance? =
                    habitInstanceRepository.getInstanceForHabitAndDate(habitId, today)
                if (instance != null) {
                    habitNotification.cancelAllForHabit(habitId, listOf(instance.id))
                }

                habitRepository.deleteHabit(habitId)
                _events.emit(HabitFormEvent.NavigateBack)
            } catch (e: Exception) {
                _events.emit(HabitFormEvent.ShowError(e.message))
            }
        }
    }

    fun archiveHabit() {
        val habitId: String = _state.value.habitId ?: return

        viewModelScope.launch {
            try {
                habitRepository.archiveHabit(habitId)
                _events.emit(HabitFormEvent.NavigateBack)
            } catch (e: Exception) {
                _events.emit(HabitFormEvent.ShowError(e.message))
            }
        }
    }

    fun updateIsTrackingEnabled(isEnabled: Boolean) {
        _state.update { it.copy(isTrackingEnabled = isEnabled) }
    }

    fun openNotificationSettings() {
        habitNotification.openNotificationSettings()
    }

    fun refreshNotificationPermission() {
        _state.update {
            it.copy(
                isNotificationPermissionGranted = habitNotification.isNotificationPermissionGranted()
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
