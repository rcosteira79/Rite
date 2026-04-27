package com.ricardocosteira.rite.presentation.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricardocosteira.rite.di.AppScope
import com.ricardocosteira.rite.di.DefaultDispatcher
import com.ricardocosteira.rite.domain.models.CompletionSource
import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitReminder
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.ScheduleType
import com.ricardocosteira.rite.domain.models.StrictnessPreset
import com.ricardocosteira.rite.domain.models.User
import com.ricardocosteira.rite.domain.models.UserStrictnessSettings
import com.ricardocosteira.rite.domain.models.motivationalTitleIndexForDate
import com.ricardocosteira.rite.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.rite.domain.repositories.HabitRepository
import com.ricardocosteira.rite.domain.repositories.UserRepository
import com.ricardocosteira.rite.domain.time.CurrentDateProvider
import com.ricardocosteira.rite.domain.usecases.CompleteHabit
import com.ricardocosteira.rite.domain.usecases.GenerateDailyHabits
import com.ricardocosteira.rite.domain.usecases.ProcessEndOfDay
import com.ricardocosteira.rite.domain.usecases.SkipHabit
import com.ricardocosteira.rite.domain.usecases.SkipLockedException
import com.ricardocosteira.rite.domain.usecases.UndoHabit
import com.ricardocosteira.rite.domain.usecases.UndoLastIncrement
import com.ricardocosteira.rite.notifications.HabitNotification
import com.ricardocosteira.rite.notifications.TrackedHabitInfo
import com.ricardocosteira.rite.presentation.mappers.motivationalTitleResource
import com.ricardocosteira.rite.presentation.models.TodayHabitUiModel
import com.ricardocosteira.rite.presentation.models.mapToTodayHabitUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import me.tatarka.inject.annotations.Inject

/**
 * Scoped to the application lifetime via [AppScope] rather than a
 * [androidx.lifecycle.ViewModelStoreOwner] because this app uses a single-activity
 * architecture and all ViewModels are obtained directly from the DI component.
 *
 * State derives reactively from [currentDateProvider] (current local date) and the
 * [HabitInstanceRepository] / [UserRepository] flows. The screen automatically
 * re-renders when the date changes (app foreground after midnight, midnight tick) or
 * when the database is updated by any actor (action handlers, workers, the
 * notification action receiver, the create/edit habit screen).
 *
 * The [_state] MutableStateFlow is the single source of truth exposed to the UI.
 * The reactive pipeline writes to it asynchronously on every date/DB change.
 * Delete operations also write to it synchronously (to allow immediate visual feedback
 * before the 5-second undo timeout commits the change to the DB).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@AppScope
@Inject
class TodayViewModel(
    private val userRepository: UserRepository,
    private val habitRepository: HabitRepository,
    private val habitInstanceRepository: HabitInstanceRepository,
    private val generateDailyHabits: GenerateDailyHabits,
    private val processEndOfDay: ProcessEndOfDay,
    private val completeHabit: CompleteHabit,
    private val skipHabit: SkipHabit,
    private val undoHabit: UndoHabit,
    private val undoLastIncrement: UndoLastIncrement,
    private val habitNotification: HabitNotification,
    private val currentDateProvider: CurrentDateProvider,
    private val defaultDispatcher: DefaultDispatcher
) : ViewModel() {

    private val _events = MutableSharedFlow<TodayEvent>()
    val events: SharedFlow<TodayEvent> = _events.asSharedFlow()

    private val _state = MutableStateFlow(TodayState(isLoading = true))
    val state: StateFlow<TodayState> = _state.asStateFlow()

    private val _pendingDelete = MutableStateFlow<PendingDelete?>(null)
    private val _quantitativeInputFor = MutableStateFlow<String?>(null)
    private val _timezoneWarningDismissed = MutableStateFlow(false)

    private var undoJob: Job? = null

    init {
        // Reactive pipeline: whenever the date changes, re-derive state from DB flows.
        viewModelScope.launch {
            currentDateProvider.today
                .flatMapLatest { today -> observeTodayState(today) }
                .collect { newState -> _state.value = newState }
        }
        // Side effects: process previous-day failures and generate today's instances on
        // every date change. Both use cases are idempotent.
        viewModelScope.launch {
            currentDateProvider.today.collect {
                try {
                    processEndOfDay.execute()
                    generateDailyHabits.execute()
                } catch (e: Exception) {
                    _events.emit(TodayEvent.ShowError(e.message ?: "Failed to refresh today"))
                }
            }
        }
    }

    private fun observeTodayState(today: LocalDate) = combine(
        userRepository.observeUser(),
        habitInstanceRepository.observeInstancesInDateRange(
            startDate = today.minus(DAY_RANGE, DateTimeUnit.DAY),
            endDate = today
        ),
        _pendingDelete,
        _quantitativeInputFor,
        _timezoneWarningDismissed
    ) { user, instances, pendingDelete, quantitativeInputFor, timezoneWarningDismissed ->
        buildState(
            user = user,
            instances = instances,
            today = today,
            pendingDelete = pendingDelete,
            quantitativeInputFor = quantitativeInputFor,
            timezoneWarningDismissed = timezoneWarningDismissed
        )
    }

    private suspend fun buildState(
        user: User?,
        instances: List<HabitInstance>,
        today: LocalDate,
        pendingDelete: PendingDelete?,
        quantitativeInputFor: String?,
        timezoneWarningDismissed: Boolean
    ): TodayState {
        val userTimezone: TimeZone = user?.timezone ?: TimeZone.currentSystemDefault()
        val strictnessPreset: StrictnessPreset? = user?.let {
            StrictnessPreset.fromSettings(
                UserStrictnessSettings(
                    undoPolicy = it.undoPolicy,
                    maxSnoozesPerHabitPerDay = it.maxSnoozesPerHabitPerDay,
                    maxConsecutiveSkips = it.maxConsecutiveSkips,
                    maxSnoozeDurationMinutes = it.maxSnoozeDurationMinutes
                )
            )
        }

        val motivationalTitleRes = motivationalTitleResource(
            motivationalTitleIndexForDate(today)
        )

        val habits: ImmutableList<TodayHabitUiModel> = withContext(defaultDispatcher) {
            coroutineScope {
                instances.mapNotNull { instance ->
                    val habitDeferred = async { habitRepository.getHabitById(instance.habitId) }
                    val scheduleDeferred =
                        async { habitRepository.getScheduleForHabit(instance.habitId) }
                    val habit = habitDeferred.await() ?: return@mapNotNull null
                    val schedule = scheduleDeferred.await() ?: return@mapNotNull null

                    if (schedule.scheduleType == ScheduleType.DAILY && instance.date != today) {
                        return@mapNotNull null
                    }

                    mapToTodayHabitUiModel(
                        instance = instance,
                        habit = habit,
                        schedule = schedule,
                        maxConsecutiveSkips = user?.maxConsecutiveSkips,
                        userTimezone = userTimezone
                    )
                }
                    .filterNot { it.habitId == pendingDelete?.habitId }
                    .toImmutableList()
            }
        }

        val counts: TodayCounts = habits.computeCounts()
        val resolvedStatuses: Set<HabitStatus> = setOf(
            HabitStatus.COMPLETED,
            HabitStatus.SKIPPED,
            HabitStatus.FAILED
        )

        val dailyHabits: List<TodayHabitUiModel> = habits.filter {
            (it.isDaily || it.isFixedWeekly) && !it.isSuspended
        }
        val weeklyHabits: List<TodayHabitUiModel> = habits.filter {
            it.isFlexibleWeekly && !it.isSuspended
        }

        val (pendingDaily, resolvedDaily) =
            dailyHabits.partition { it.status !in resolvedStatuses }
        val (pendingWeekly, resolvedWeekly) =
            weeklyHabits.partition { it.status !in resolvedStatuses }

        refreshTrackingNotification(today)

        return TodayState(
            habits = habits,
            pendingDaily = pendingDaily.toImmutableList(),
            resolvedDaily = resolvedDaily.toImmutableList(),
            pendingWeekly = pendingWeekly.toImmutableList(),
            resolvedWeekly = resolvedWeekly.toImmutableList(),
            isLoading = false,
            pendingCount = counts.pendingCount,
            dailyProgressDisplay = counts.dailyProgressDisplay,
            dailyProgressExact = counts.dailyProgressExact,
            dailyTotal = counts.dailyTotal,
            motivationalTitleRes = motivationalTitleRes,
            strictnessPreset = strictnessPreset,
            showTimezoneWarning = user?.previousTimezone != null && !timezoneWarningDismissed,
            previousTimezone = user?.previousTimezone?.id,
            pendingDelete = pendingDelete,
            showQuantitativeInputFor = quantitativeInputFor
        )
    }

    private suspend fun cancelReminderForHabit(instanceId: String, habitId: String) {
        val reminder: HabitReminder? = habitRepository.getRemindersForHabit(habitId).firstOrNull()
        habitNotification.cancelReminder(instanceId, reminder)
    }

    fun completeHabit(instanceId: String) {
        viewModelScope.launch {
            val habit = state.value.habits.find { it.instanceId == instanceId } ?: return@launch
            if (habit.type == HabitType.QUANTITATIVE) {
                _quantitativeInputFor.value = instanceId
                return@launch
            }
            completeHabit.executeBinary(instanceId, CompletionSource.IN_APP)
                .onSuccess { cancelReminderForHabit(instanceId, habit.habitId) }
                .onFailure {
                    _events.emit(TodayEvent.ShowError(it.message ?: "Something went wrong"))
                }
        }
    }

    fun completeQuantitativeHabit(instanceId: String, value: Int) {
        viewModelScope.launch {
            val habit = state.value.habits.find { it.instanceId == instanceId }
            _quantitativeInputFor.value = null
            completeHabit.executeQuantitative(
                instanceId = instanceId,
                deltaValue = value,
                source = CompletionSource.IN_APP
            )
                .onSuccess { updatedInstance ->
                    if (updatedInstance.isQuantitativeComplete() && habit != null) {
                        cancelReminderForHabit(instanceId, habit.habitId)
                    }
                }
                .onFailure {
                    _events.emit(TodayEvent.ShowError(it.message ?: "Something went wrong"))
                }
        }
    }

    fun incrementHabitProgress(instanceId: String) {
        viewModelScope.launch {
            val habit = state.value.habits.find { it.instanceId == instanceId } ?: return@launch
            completeHabit.executeQuantitative(
                instanceId = instanceId,
                deltaValue = habit.defaultIncrement,
                source = CompletionSource.IN_APP
            )
                .onSuccess { updated ->
                    if (updated.isQuantitativeComplete()) {
                        cancelReminderForHabit(
                            instanceId,
                            habit.habitId
                        )
                    }
                }
                .onFailure {
                    _events.emit(TodayEvent.ShowError(it.message ?: "Something went wrong"))
                }
        }
    }

    fun showQuantitativeInput(instanceId: String) {
        _quantitativeInputFor.value = instanceId
    }
    fun dismissQuantitativeInput() {
        _quantitativeInputFor.value = null
    }

    fun skipHabit(instanceId: String) {
        viewModelScope.launch {
            val habit = state.value.habits.find { it.instanceId == instanceId }
            skipHabit.execute(instanceId)
                .onSuccess { if (habit != null) cancelReminderForHabit(instanceId, habit.habitId) }
                .onFailure { error ->
                    if (error is SkipLockedException) {
                        _events.emit(TodayEvent.SkipLimitReached)
                    } else {
                        _events.emit(TodayEvent.ShowError(error.message ?: "Something went wrong"))
                    }
                }
        }
    }

    fun undoHabit(instanceId: String) {
        viewModelScope.launch {
            undoHabit.execute(instanceId)
                .onFailure {
                    _events.emit(TodayEvent.ShowError(it.message ?: "Something went wrong"))
                }
        }
    }

    fun undoLastIncrement(instanceId: String) {
        viewModelScope.launch {
            undoLastIncrement.execute(instanceId)
                .onFailure {
                    _events.emit(TodayEvent.ShowError(it.message ?: "Something went wrong"))
                }
        }
    }

    fun deleteHabit(habitId: String) {
        val habit: TodayHabitUiModel = state.value.habits.find { it.habitId == habitId } ?: return

        commitPendingDeleteAndCancelJob()

        // Update _pendingDelete so the reactive pipeline reflects the deletion on next emit.
        _pendingDelete.value = PendingDelete(habitId, habit.name)

        viewModelScope.launch { _events.emit(TodayEvent.HabitDeleted(habit.name)) }

        undoJob = viewModelScope.launch {
            delay(UNDO_TIMEOUT_MS)
            try {
                habitRepository.deleteHabit(habitId)
                _pendingDelete.value = null
            } catch (e: Exception) {
                _events.emit(TodayEvent.ShowError(e.message ?: "Failed to delete habit"))
                _pendingDelete.value = null
            }
        }
    }

    fun undoDelete() {
        undoJob?.cancel()
        undoJob = null
        _pendingDelete.value = null
        viewModelScope.launch { _events.emit(TodayEvent.UndoCompleted) }
    }

    private fun commitPendingDeleteAndCancelJob() {
        val previousDelete: PendingDelete? = _pendingDelete.value
        undoJob?.cancel()
        undoJob = null

        if (previousDelete == null) return

        viewModelScope.launch {
            try {
                habitRepository.deleteHabit(previousDelete.habitId)
            } catch (e: Exception) {
                _events.emit(TodayEvent.ShowError(e.message ?: "Failed to delete habit"))
            }
        }
    }

    fun archiveHabit(habitId: String) {
        viewModelScope.launch {
            try {
                val today: LocalDate = currentDateProvider.today.value
                val instance: HabitInstance? =
                    habitInstanceRepository.getInstanceForHabitAndDate(habitId, today)
                if (instance != null) {
                    habitNotification.cancelAllForHabit(habitId, listOf(instance.id))
                }
                habitRepository.archiveHabit(habitId)
            } catch (e: Exception) {
                _events.emit(TodayEvent.ShowError(e.message ?: "Something went wrong"))
            }
        }
    }

    fun dismissTimezoneWarning() {
        // Local-only dismissal — matches the original behavior where the warning re-appears
        // on the next load if the user.previousTimezone is still set in DB. Resets to false
        // when the user repository emits a new previousTimezone (handled in buildState).
        _timezoneWarningDismissed.value = true
    }

    fun navigateToHabitDetail(instanceId: String) {
        viewModelScope.launch { _events.emit(TodayEvent.NavigateToHabitDetail(instanceId)) }
    }

    fun navigateToCreateHabit() {
        viewModelScope.launch { _events.emit(TodayEvent.NavigateToCreateHabit) }
    }

    private suspend fun refreshTrackingNotification(today: LocalDate) {
        val trackedHabits: List<Habit> = habitRepository.getHabitsWithTrackingEnabled()
        if (trackedHabits.isEmpty()) {
            habitNotification.hideTrackingNotification()
            return
        }

        val trackedInfoList: List<TrackedHabitInfo> = trackedHabits.mapNotNull { habit ->
            val instance: HabitInstance = habitInstanceRepository.getInstanceForHabitAndDate(
                habit.id,
                today
            ) ?: return@mapNotNull null

            TrackedHabitInfo(
                instanceId = instance.id,
                habitId = habit.id,
                habitName = habit.name,
                type = habit.type,
                currentProgress = instance.currentProgress,
                targetValue = instance.targetValue,
                unit = habit.unit,
                defaultIncrement = habit.defaultIncrement,
                isCompleted = instance.status == HabitStatus.COMPLETED
            )
        }

        if (trackedInfoList.isEmpty()) {
            habitNotification.hideTrackingNotification()
        } else {
            habitNotification.updateTrackingNotification(trackedInfoList)
        }
    }

    private companion object {
        const val UNDO_TIMEOUT_MS: Long = 5_000L
        const val DAY_RANGE: Int = 6
    }
}
