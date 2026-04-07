package com.ricardocosteira.rite.domain.usecases

import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.ScheduleType
import com.ricardocosteira.rite.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.rite.domain.repositories.HabitRepository
import com.ricardocosteira.rite.domain.repositories.UserRepository
import com.ricardocosteira.rite.util.toLocalDate
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import me.tatarka.inject.annotations.Inject

/**
 * Processes end-of-day/end-of-week failure for habits.
 * - For DAILY habits: Marks PENDING instances from yesterday as FAILED
 * - For WEEKLY habits: Marks PENDING instances from last week as FAILED (if week ended)
 */
@Inject
class ProcessEndOfDay(
    private val userRepository: UserRepository,
    private val habitInstanceRepository: HabitInstanceRepository,
    private val habitRepository: HabitRepository
) {
    /**
     * Process end-of-day for the previous day and end-of-week for weekly habits.
     * @return Pair of (daily failures, weekly failures).
     */
    suspend fun execute(): Pair<Int, Int> {
        val user = userRepository.getUser() ?: return Pair(0, 0)
        val today = Clock.System.now().toLocalDate(user.timezone)

        val dailyFailures = processDailyHabits(today)
        val weeklyFailures = processWeeklyHabits(today)

        return Pair(dailyFailures, weeklyFailures)
    }

    /**
     * Process daily habits from yesterday.
     * SUSPENDED instances are not marked as FAILED.
     */
    private suspend fun processDailyHabits(today: LocalDate): Int {
        val yesterday = today.minus(DatePeriod(days = 1))
        val yesterdayInstances = habitInstanceRepository.getInstancesForDate(yesterday)

        var failedCount = 0
        for (instance in yesterdayInstances) {
            // Skip if not PENDING (includes SUSPENDED)
            if (instance.status != HabitStatus.PENDING) continue

            // Check if this is a daily habit
            val habit = habitRepository.getHabitById(instance.habitId) ?: continue
            val schedule = habitRepository.getScheduleForHabit(habit.id) ?: continue

            if (schedule.scheduleType == ScheduleType.DAILY) {
                habitInstanceRepository.updateInstanceStatus(
                    instanceId = instance.id,
                    status = HabitStatus.FAILED,
                    completedValue = instance.completedValue,
                    completedAt = null
                )

                // Reset streak for failed habit
                habitRepository.updateHabitStreak(
                    habitId = habit.id,
                    currentStreak = 0,
                    longestStreak = habit.longestStreak
                )

                failedCount++
            }
        }

        return failedCount
    }

    /**
     * Process weekly habits:
     * - WEEKLY (fixed days): Evaluate the day after the last specificDay in the week.
     * - FLEXIBLE_WEEKLY: Evaluate at week boundary (today == weekStartDay).
     * SUSPENDED instances are not marked as FAILED.
     */
    private suspend fun processWeeklyHabits(today: LocalDate): Int {
        val activeHabits = habitRepository.getActiveHabits()
        var failedCount = 0

        for (habit in activeHabits) {
            val schedule = habitRepository.getScheduleForHabit(habit.id) ?: continue

            val shouldEvaluate: Boolean = when (schedule.scheduleType) {
                ScheduleType.DAILY -> false

                ScheduleType.FLEXIBLE_WEEKLY -> {
                    today.dayOfWeek == schedule.weekStartDay
                }

                ScheduleType.WEEKLY -> {
                    val lastSpecificDay: DayOfWeek = findLastSpecificDay(
                        specificDays = schedule.specificDays ?: continue,
                        weekStartDay = schedule.weekStartDay
                    )
                    val dayAfterLast: DayOfWeek = lastSpecificDay.next()
                    today.dayOfWeek == dayAfterLast
                }
            }

            if (!shouldEvaluate) continue

            val weekStart: LocalDate = getWeekStartForEvaluation(today, schedule.weekStartDay)

            val lastWeekInstance = habitInstanceRepository.getInstanceForHabitAndDate(
                habitId = habit.id,
                date = weekStart
            )

            // Only process PENDING instances (skip SUSPENDED)
            if (lastWeekInstance != null && lastWeekInstance.status == HabitStatus.PENDING) {
                val completedValue: Int = lastWeekInstance.completedValue ?: 0
                val quota: Int = lastWeekInstance.targetValue ?: 1

                if (completedValue < quota) {
                    habitInstanceRepository.updateInstanceStatus(
                        instanceId = lastWeekInstance.id,
                        status = HabitStatus.FAILED,
                        completedValue = completedValue,
                        completedAt = null
                    )

                    habitRepository.updateHabitStreak(
                        habitId = habit.id,
                        currentStreak = 0,
                        longestStreak = habit.longestStreak
                    )

                    failedCount++
                } else {
                    habitInstanceRepository.updateInstanceStatus(
                        instanceId = lastWeekInstance.id,
                        status = HabitStatus.COMPLETED,
                        completedValue = completedValue,
                        completedAt = Clock.System.now()
                    )
                }
            }
        }

        return failedCount
    }

    /**
     * Finds the last specific day in the week, ordered from weekStartDay.
     * E.g., with weekStartDay=MONDAY and specificDays={MON, WED, FRI}, returns FRIDAY.
     */
    private fun findLastSpecificDay(
        specificDays: Set<DayOfWeek>,
        weekStartDay: DayOfWeek
    ): DayOfWeek = specificDays.maxByOrNull { day ->
        (day.ordinal - weekStartDay.ordinal + 7) % 7
    } ?: weekStartDay

    /**
     * Gets the week start date for evaluation purposes.
     * For FLEXIBLE_WEEKLY (evaluated on weekStartDay): the previous week started 7 days ago.
     * For WEEKLY (evaluated day after last specific day): the current week's start.
     */
    private fun getWeekStartForEvaluation(today: LocalDate, weekStartDay: DayOfWeek): LocalDate {
        if (today.dayOfWeek == weekStartDay) {
            return today.minus(7, DateTimeUnit.DAY)
        }
        var current: LocalDate = today
        while (current.dayOfWeek != weekStartDay) {
            current = current.minus(1, DateTimeUnit.DAY)
        }
        return current
    }

    private fun DayOfWeek.next(): DayOfWeek = DayOfWeek.entries[(this.ordinal + 1) % 7]
}
