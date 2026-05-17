package com.ricardocosteira.rite.domain.usecases

import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitSchedule
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.ScheduleType
import com.ricardocosteira.rite.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.rite.domain.repositories.HabitRepository
import com.ricardocosteira.rite.domain.repositories.LeavePeriodRepository
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import me.tatarka.inject.annotations.Inject

/**
 * Ensures today's instance exists for a single habit, given its current schedule.
 *
 * Used as the per-habit step in [GenerateDailyHabits] for batch generation, and
 * directly after [CreateHabit] / schedule edits so the data invariant
 * "if the schedule is active today, an instance exists" is maintained regardless
 * of which actor mutated the habit. Idempotent: returns null when no work is
 * needed (schedule inactive today, or instance already exists).
 */
@Inject
class GenerateInstanceForHabit(
    private val habitRepository: HabitRepository,
    private val habitInstanceRepository: HabitInstanceRepository,
    private val leavePeriodRepository: LeavePeriodRepository,
    private val uuidProvider: UuidProvider
) {
    suspend fun execute(habit: Habit, schedule: HabitSchedule, today: LocalDate): HabitInstance? {
        if (!schedule.isActiveOn(today)) return null

        val isSuspended: Boolean =
            leavePeriodRepository.getActiveLeavePeriod(habit.id, today) != null

        return when (schedule.scheduleType) {
            ScheduleType.DAILY -> generateDaily(habit, today, isSuspended)

            ScheduleType.WEEKLY,
            ScheduleType.FLEXIBLE_WEEKLY -> generateWeekly(habit, schedule, today, isSuspended)
        }
    }

    private suspend fun generateDaily(
        habit: Habit,
        today: LocalDate,
        isSuspended: Boolean
    ): HabitInstance? {
        if (habitInstanceRepository.getInstanceForHabitAndDate(habit.id, today) != null) {
            return null
        }

        val instance: HabitInstance = if (isSuspended) {
            buildDailyInstance(habit, today, HabitStatus.SUSPENDED, consecutiveSkips = 0)
        } else {
            buildDailyInstance(
                habit = habit,
                date = today,
                status = HabitStatus.PENDING,
                consecutiveSkips = calculateConsecutiveSkips(habit.id)
            )
        }
        habitInstanceRepository.createInstance(instance)
        if (!isSuspended) {
            habitRepository.incrementHabitExpectedCompletions(habit.id, amount = 1)
        }
        return instance
    }

    private suspend fun generateWeekly(
        habit: Habit,
        schedule: HabitSchedule,
        today: LocalDate,
        isSuspended: Boolean
    ): HabitInstance? {
        val weekStart: LocalDate = getWeekStart(today, schedule.weekStartDay)
        if (habitInstanceRepository.getInstanceForHabitAndDate(habit.id, weekStart) != null) {
            return null
        }

        val instance: HabitInstance = if (isSuspended) {
            buildWeeklyInstance(habit, weekStart, schedule.quota, HabitStatus.SUSPENDED, 0)
        } else {
            buildWeeklyInstance(
                habit = habit,
                weekStart = weekStart,
                quota = schedule.quota,
                status = HabitStatus.PENDING,
                consecutiveSkips = calculateConsecutiveSkips(habit.id)
            )
        }
        habitInstanceRepository.createInstance(instance)
        if (!isSuspended) {
            habitRepository.incrementHabitExpectedCompletions(habit.id, amount = schedule.quota)
        }
        return instance
    }

    private fun buildDailyInstance(
        habit: Habit,
        date: LocalDate,
        status: HabitStatus,
        consecutiveSkips: Int
    ): HabitInstance = HabitInstance(
        id = uuidProvider.generate(),
        habitId = habit.id,
        date = date,
        status = status,
        completedValue = if (habit.type == HabitType.QUANTITATIVE) 0 else null,
        targetValue = habit.targetValue,
        consecutiveSkipsAtCreation = consecutiveSkips,
        createdAt = Clock.System.now()
    )

    private fun buildWeeklyInstance(
        habit: Habit,
        weekStart: LocalDate,
        quota: Int,
        status: HabitStatus,
        consecutiveSkips: Int
    ): HabitInstance = HabitInstance(
        id = uuidProvider.generate(),
        habitId = habit.id,
        date = weekStart,
        status = status,
        completedValue = if (habit.type == HabitType.QUANTITATIVE) 0 else null,
        targetValue = quota,
        consecutiveSkipsAtCreation = consecutiveSkips,
        createdAt = Clock.System.now()
    )

    private fun getWeekStart(date: LocalDate, weekStartDay: DayOfWeek): LocalDate {
        var current = date
        while (current.dayOfWeek != weekStartDay) {
            current = current.minus(1, DateTimeUnit.DAY)
        }
        return current
    }

    private suspend fun calculateConsecutiveSkips(habitId: String): Int {
        val instances = habitInstanceRepository.getInstancesForHabit(habitId)
            .sortedByDescending { it.date }
        var count = 0
        for (instance in instances) {
            if (instance.status == HabitStatus.SKIPPED) count++ else break
        }
        return count
    }
}
