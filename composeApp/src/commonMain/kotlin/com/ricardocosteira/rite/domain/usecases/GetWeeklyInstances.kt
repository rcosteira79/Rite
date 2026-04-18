package com.ricardocosteira.rite.domain.usecases

import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.ScheduleType
import com.ricardocosteira.rite.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.rite.domain.repositories.HabitRepository
import com.ricardocosteira.rite.domain.repositories.UserRepository
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

/**
 * Retrieves habit instances for the current week, handling weekly cadence habits.
 *
 * For DAILY habits: Returns all instances from the week
 * For WEEKLY habits: Returns the single instance for the week
 */
@Inject
class GetWeeklyInstances(
    private val userRepository: UserRepository,
    private val habitRepository: HabitRepository,
    private val habitInstanceRepository: HabitInstanceRepository
) {

    /**
     * Gets all habit instances for the current week.
     * @param weekStartDay Optional override for week start day (defaults to user's habit schedule settings)
     * @return List of habit instances for the current week
     */
    suspend fun execute(weekStartDay: DayOfWeek? = null): Result<WeeklyInstancesResult> {
        val user = userRepository.getUser()
            ?: return Result.failure(IllegalStateException("User not found"))

        val today = Clock.System.now().toLocalDateTime(user.timezone).date
        val activeHabits = habitRepository.getActiveHabits()

        val dailyInstances = mutableListOf<HabitInstance>()
        val weeklyInstances = mutableListOf<HabitInstance>()

        for (habit in activeHabits) {
            val schedule = habitRepository.getScheduleForHabit(habit.id) ?: continue

            when (schedule.scheduleType) {
                ScheduleType.DAILY -> {
                    // Get all daily instances for this week
                    val weekStart = getWeekStart(today, weekStartDay ?: schedule.weekStartDay)
                    val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)

                    val instances = habitInstanceRepository.getInstancesInDateRange(
                        weekStart,
                        weekEnd
                    )
                        .filter { it.habitId == habit.id }

                    dailyInstances.addAll(instances)
                }

                ScheduleType.WEEKLY, ScheduleType.FLEXIBLE_WEEKLY -> {
                    // Get the single weekly instance
                    val weekStart = getWeekStart(today, schedule.weekStartDay)
                    val instance = habitInstanceRepository.getInstanceForHabitAndDate(
                        habit.id,
                        weekStart
                    )

                    if (instance != null) {
                        weeklyInstances.add(instance)
                    }
                }
            }
        }

        val weekStart = getWeekStart(today, weekStartDay ?: DayOfWeek.MONDAY)
        val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)

        return Result.success(
            WeeklyInstancesResult(
                weekStart = weekStart,
                weekEnd = weekEnd,
                dailyInstances = dailyInstances,
                weeklyInstances = weeklyInstances
            )
        )
    }

    /**
     * Gets habit instances for a specific week.
     */
    suspend fun executeForWeek(
        weekStartDate: LocalDate,
        weekStartDay: DayOfWeek = DayOfWeek.MONDAY
    ): Result<WeeklyInstancesResult> {
        val activeHabits = habitRepository.getActiveHabits()
        val weekEnd = weekStartDate.plus(6, DateTimeUnit.DAY)

        val dailyInstances = mutableListOf<HabitInstance>()
        val weeklyInstances = mutableListOf<HabitInstance>()

        for (habit in activeHabits) {
            val schedule = habitRepository.getScheduleForHabit(habit.id) ?: continue

            when (schedule.scheduleType) {
                ScheduleType.DAILY -> {
                    val instances = habitInstanceRepository.getInstancesInDateRange(
                        weekStartDate,
                        weekEnd
                    )
                        .filter { it.habitId == habit.id }
                    dailyInstances.addAll(instances)
                }

                ScheduleType.WEEKLY, ScheduleType.FLEXIBLE_WEEKLY -> {
                    val instance = habitInstanceRepository.getInstanceForHabitAndDate(
                        habit.id,
                        weekStartDate
                    )
                    if (instance != null) {
                        weeklyInstances.add(instance)
                    }
                }
            }
        }

        return Result.success(
            WeeklyInstancesResult(
                weekStart = weekStartDate,
                weekEnd = weekEnd,
                dailyInstances = dailyInstances,
                weeklyInstances = weeklyInstances
            )
        )
    }

    /**
     * Gets the start date of the week containing the given date.
     */
    private fun getWeekStart(date: LocalDate, weekStartDay: DayOfWeek): LocalDate {
        var current = date
        while (current.dayOfWeek != weekStartDay) {
            current = current.minus(1, DateTimeUnit.DAY)
        }
        return current
    }
}

/**
 * Result containing weekly habit instances.
 *
 * @property weekStart First day of the week
 * @property weekEnd Last day of the week
 * @property dailyInstances List of daily habit instances for this week
 * @property weeklyInstances List of weekly habit instances (one per weekly habit)
 */
data class WeeklyInstancesResult(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val dailyInstances: List<HabitInstance>,
    val weeklyInstances: List<HabitInstance>
) {
    /**
     * All instances for the week combined.
     */
    val allInstances: List<HabitInstance>
        get() = dailyInstances + weeklyInstances

    /**
     * Gets daily instances for a specific date within the week.
     */
    fun getInstancesForDate(date: LocalDate): List<HabitInstance> =
        dailyInstances.filter { it.date == date }

    /**
     * Gets completion progress for weekly habits.
     * Returns map of habitId to (completed, quota).
     */
    fun getWeeklyProgress(): Map<String, Pair<Int, Int>> = weeklyInstances.associate { instance ->
        val completed = instance.completedValue ?: 0
        val quota = instance.targetValue ?: 1
        instance.habitId to (completed to quota)
    }
}
