package com.ricardocosteira.habitlock.domain.usecases

import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.models.ScheduleType
import com.ricardocosteira.habitlock.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import com.ricardocosteira.habitlock.util.toLocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import me.tatarka.inject.annotations.Inject
import kotlin.time.Clock

/**
 * Processes end-of-day/end-of-week failure for habits.
 * - For DAILY habits: Marks PENDING instances from yesterday as FAILED
 * - For WEEKLY habits: Marks PENDING instances from last week as FAILED (if week ended)
 */
@Inject
class ProcessEndOfDay(
    private val userRepository: UserRepository,
    private val habitInstanceRepository: HabitInstanceRepository,
    private val habitRepository: HabitRepository,
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
                    completedAt = null,
                )

                // Reset streak for failed habit
                habitRepository.updateHabitStreak(
                    habitId = habit.id,
                    currentStreak = 0,
                    longestStreak = habit.longestStreak,
                )

                failedCount++
            }
        }

        return failedCount
    }

    /**
     * Process weekly habits if we're at the start of a new week.
     * Checks if any weekly habit from last week is still PENDING and marks it as FAILED.
     * SUSPENDED instances are not marked as FAILED.
     */
    private suspend fun processWeeklyHabits(today: LocalDate): Int {
        val activeHabits = habitRepository.getActiveHabits()
        var failedCount = 0

        for (habit in activeHabits) {
            val schedule = habitRepository.getScheduleForHabit(habit.id) ?: continue

            if (schedule.scheduleType == ScheduleType.WEEKLY) {
                // Check if today is the start of a new week for this habit
                if (today.dayOfWeek == schedule.weekStartDay) {
                    // Get last week's start date
                    val lastWeekStart = today.minus(7, DateTimeUnit.DAY)

                    // Check if there's an instance from last week
                    val lastWeekInstance =
                        habitInstanceRepository.getInstanceForHabitAndDate(
                            habitId = habit.id,
                            date = lastWeekStart,
                        )

                    // Only process PENDING instances (skip SUSPENDED)
                    if (lastWeekInstance != null && lastWeekInstance.status == HabitStatus.PENDING) {
                        // Check if quota was not met
                        val completedValue = lastWeekInstance.completedValue ?: 0
                        val quota = lastWeekInstance.targetValue ?: 1

                        if (completedValue < quota) {
                            habitInstanceRepository.updateInstanceStatus(
                                instanceId = lastWeekInstance.id,
                                status = HabitStatus.FAILED,
                                completedValue = completedValue,
                                completedAt = null,
                            )

                            // Reset streak for failed habit
                            habitRepository.updateHabitStreak(
                                habitId = habit.id,
                                currentStreak = 0,
                                longestStreak = habit.longestStreak,
                            )

                            failedCount++
                        } else {
                            // Quota was met, mark as completed
                            habitInstanceRepository.updateInstanceStatus(
                                instanceId = lastWeekInstance.id,
                                status = HabitStatus.COMPLETED,
                                completedValue = completedValue,
                                completedAt = Clock.System.now(),
                            )
                        }
                    }
                }
            }
        }

        return failedCount
    }
}
