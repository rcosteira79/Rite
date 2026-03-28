package com.ricardocosteira.habitlock.presentation.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricardocosteira.habitlock.di.AppScope
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import com.ricardocosteira.habitlock.presentation.models.CalendarDayUiModel
import com.ricardocosteira.habitlock.presentation.models.DayClassification
import com.ricardocosteira.habitlock.util.toLocalDate
import kotlin.time.Clock
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
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
class CalendarViewModel(
    private val userRepository: UserRepository,
    private val habitInstanceRepository: HabitInstanceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CalendarState())
    val state: StateFlow<CalendarState> = _state.asStateFlow()

    init {
        val now = Clock.System.now()
        val today = now.toLocalDate(TimeZone.currentSystemDefault())
        _state.update {
            it.copy(
                currentMonth = today.month,
                currentYear = today.year
            )
        }
        loadMonth()
    }

    fun previousMonth() {
        val current = _state.value
        val newDate = LocalDate(current.currentYear, current.currentMonth, 1)
            .minus(DatePeriod(months = 1))
        _state.update {
            it.copy(
                currentMonth = newDate.month,
                currentYear = newDate.year
            )
        }
        loadMonth()
    }

    fun nextMonth() {
        val current = _state.value
        val newDate = LocalDate(current.currentYear, current.currentMonth, 1)
            .plus(DatePeriod(months = 1))
        _state.update {
            it.copy(
                currentMonth = newDate.month,
                currentYear = newDate.year
            )
        }
        loadMonth()
    }

    fun selectDay(date: LocalDate) {
        _state.update { it.copy(selectedDay = date) }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedDay = null) }
    }

    private fun loadMonth() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val current = _state.value
                val startDate = LocalDate(current.currentYear, current.currentMonth, 1)
                val endDate = startDate.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))

                val instances = habitInstanceRepository.getInstancesInDateRange(startDate, endDate)

                // Group by date
                val instancesByDate = instances.groupBy { it.date }

                val days = mutableListOf<CalendarDayUiModel>()
                var perfectCount = 0
                var trackedCount = 0

                var currentDate = startDate
                while (currentDate <= endDate) {
                    val dayInstances = instancesByDate[currentDate] ?: emptyList()

                    if (dayInstances.isNotEmpty()) {
                        trackedCount++

                        val completedCount = dayInstances.count {
                            it.status == HabitStatus.COMPLETED
                        }
                        val skippedCount = dayInstances.count { it.status == HabitStatus.SKIPPED }
                        val failedCount = dayInstances.count { it.status == HabitStatus.FAILED }

                        val classification = when {
                            failedCount > 0 -> DayClassification.FAILED

                            completedCount + skippedCount == dayInstances.size -> {
                                perfectCount++
                                DayClassification.PERFECT
                            }

                            else -> DayClassification.PARTIAL
                        }

                        days.add(
                            CalendarDayUiModel(
                                date = currentDate,
                                classification = classification,
                                completedCount = completedCount,
                                skippedCount = skippedCount,
                                failedCount = failedCount,
                                totalCount = dayInstances.size
                            )
                        )
                    } else {
                        days.add(
                            CalendarDayUiModel(
                                date = currentDate,
                                classification = DayClassification.NONE,
                                completedCount = 0,
                                skippedCount = 0,
                                failedCount = 0,
                                totalCount = 0
                            )
                        )
                    }

                    currentDate = currentDate.plus(DatePeriod(days = 1))
                }

                _state.update {
                    it.copy(
                        days = days.toImmutableList(),
                        isLoading = false,
                        perfectDaysCount = perfectCount,
                        totalDaysTracked = trackedCount
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
