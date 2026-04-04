package com.ricardocosteira.rite.domain.usecases

import me.tatarka.inject.annotations.Inject

import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.ScheduleType
import com.ricardocosteira.rite.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.rite.domain.repositories.HabitRepository
import com.ricardocosteira.rite.domain.repositories.LeavePeriodRepository
import com.ricardocosteira.rite.domain.repositories.UserRepository
import com.ricardocosteira.rite.util.todayIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.time.Clock
import kotlinx.datetime.TimeZone

/**
 * Generates habit instances for all active, non-archived habits.
 * Handles both DAILY and WEEKLY schedules.
 * Creates SUSPENDED instances for habits with active leave periods.
 * Called on app launch and by daily background job.
 */
@Inject
class GenerateDailyHabits(
    private val userRepository: UserRepository,
    private val habitRepository: HabitRepository,
    private val habitInstanceRepository: HabitInstanceRepository,
    private val leavePeriodRepository: LeavePeriodRepository,
    private val uuidProvider: UuidProvider
) {

    /**
     * Generate habit instances for today if they don't already exist.
     * For DAILY habits: Creates one instance per day
     * For WEEKLY habits: Creates one instance at start of week (per weekStartDay)
     * @return List of newly created instances, empty if already generated.
     */
    suspend fun execute(): List<HabitInstance> {
        val user = userRepository.getUser() ?: return emptyList()
        val today = Clock.System.todayIn(user.timezone)

        // Check if we need to detect timezone change
        checkTimezoneChange(user.timezone)

        val activeHabits = habitRepository.getActiveHabits()
        val newInstances = mutableListOf<HabitInstance>()

        for (habit in activeHabits) {
            val schedule = habitRepository.getScheduleForHabit(habit.id) ?: continue

            // Check if schedule is active today
            if (!schedule.isActiveOn(today)) continue

            // Check if habit is on leave (suspended)
            val activeLeavePeriod = leavePeriodRepository.getActiveLeavePeriod(habit.id, today)
            val isSuspended = activeLeavePeriod != null

            when (schedule.scheduleType) {
                ScheduleType.DAILY -> {
                    // Check if instance already exists for today
                    val existingInstance = habitInstanceRepository.getInstanceForHabitAndDate(habit.id, today)
                    if (existingInstance != null) continue

                    val instance = if (isSuspended) {
                        createSuspendedDailyInstance(habit.id, habit.type, habit.targetValue, today)
                    } else {
                        createDailyInstance(habit.id, habit.type, habit.targetValue, today)
                    }
                    
                    habitInstanceRepository.createInstance(instance)
                    newInstances.add(instance)
                    
                    // Only increment expected completions for non-suspended habits
                    if (!isSuspended) {
                        habitRepository.incrementHabitExpectedCompletions(habit.id, amount = 1)
                    }
                }
                ScheduleType.WEEKLY -> {
                    // Check if we're at the start of a new week
                    if (isStartOfWeek(today, schedule.weekStartDay)) {
                        // Check if instance already exists for this week
                        val weekStart = getWeekStart(today, schedule.weekStartDay)
                        val existingInstance = habitInstanceRepository.getInstanceForHabitAndDate(habit.id, weekStart)
                        if (existingInstance != null) continue

                        val instance = if (isSuspended) {
                            createSuspendedWeeklyInstance(habit.id, habit.type, habit.targetValue, weekStart, schedule.quota)
                        } else {
                            createWeeklyInstance(habit.id, habit.type, habit.targetValue, weekStart, schedule.quota)
                        }
                        
                        habitInstanceRepository.createInstance(instance)
                        newInstances.add(instance)
                        
                        // Only increment expected completions for non-suspended habits
                        if (!isSuspended) {
                            habitRepository.incrementHabitExpectedCompletions(habit.id, amount = schedule.quota)
                        }
                    }
                }
            }
        }

        return newInstances
    }

    /**
     * Creates a daily habit instance.
     */
    private suspend fun createDailyInstance(
        habitId: String,
        habitType: HabitType,
        targetValue: Int?,
        date: LocalDate
    ): HabitInstance {
        val consecutiveSkips = calculateConsecutiveSkips(habitId)

        return HabitInstance(
            id = uuidProvider.generate(),
            habitId = habitId,
            date = date,
            status = HabitStatus.PENDING,
            completedValue = if (habitType == HabitType.QUANTITATIVE) 0 else null,
            targetValue = targetValue,
            consecutiveSkipsAtCreation = consecutiveSkips,
            createdAt = Clock.System.now()
        )
    }

    /**
     * Creates a suspended daily habit instance.
     */
    private fun createSuspendedDailyInstance(
        habitId: String,
        habitType: HabitType,
        targetValue: Int?,
        date: LocalDate
    ): HabitInstance {
        return HabitInstance(
            id = uuidProvider.generate(),
            habitId = habitId,
            date = date,
            status = HabitStatus.SUSPENDED,
            completedValue = if (habitType == HabitType.QUANTITATIVE) 0 else null,
            targetValue = targetValue,
            consecutiveSkipsAtCreation = 0, // Suspended habits don't track skips
            createdAt = Clock.System.now()
        )
    }

    /**
     * Creates a weekly habit instance.
     * For weekly habits, the targetValue is the quota (completions per week).
     */
    private suspend fun createWeeklyInstance(
        habitId: String,
        habitType: HabitType,
        targetValue: Int?,
        weekStartDate: LocalDate,
        quota: Int
    ): HabitInstance {
        val consecutiveSkips = calculateConsecutiveSkips(habitId)

        return HabitInstance(
            id = uuidProvider.generate(),
            habitId = habitId,
            date = weekStartDate,
            status = HabitStatus.PENDING,
            completedValue = if (habitType == HabitType.QUANTITATIVE) 0 else null,
            targetValue = quota, // For weekly habits, target is the quota
            consecutiveSkipsAtCreation = consecutiveSkips,
            createdAt = Clock.System.now()
        )
    }

    /**
     * Creates a suspended weekly habit instance.
     */
    private fun createSuspendedWeeklyInstance(
        habitId: String,
        habitType: HabitType,
        targetValue: Int?,
        weekStartDate: LocalDate,
        quota: Int
    ): HabitInstance {
        return HabitInstance(
            id = uuidProvider.generate(),
            habitId = habitId,
            date = weekStartDate,
            status = HabitStatus.SUSPENDED,
            completedValue = if (habitType == HabitType.QUANTITATIVE) 0 else null,
            targetValue = quota,
            consecutiveSkipsAtCreation = 0, // Suspended habits don't track skips
            createdAt = Clock.System.now()
        )
    }

    /**
     * Checks if the given date is the start of a week based on weekStartDay.
     */
    private fun isStartOfWeek(date: LocalDate, weekStartDay: DayOfWeek): Boolean {
        return date.dayOfWeek == weekStartDay
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

    private suspend fun checkTimezoneChange(currentTimezone: TimeZone) {
        val systemTimezone = TimeZone.currentSystemDefault()
        if (systemTimezone != currentTimezone) {
            val user = userRepository.getUser() ?: return
            userRepository.updateTimezone(
                userId = user.id,
                newTimezone = systemTimezone,
                previousTimezone = currentTimezone
            )
        }
    }

    private suspend fun calculateConsecutiveSkips(habitId: String): Int {
        val instances = habitInstanceRepository.getInstancesForHabit(habitId)
            .sortedByDescending { it.date }

        var count = 0
        for (instance in instances) {
            if (instance.status == HabitStatus.SKIPPED) {
                count++
            } else {
                break
            }
        }
        return count
    }
}

