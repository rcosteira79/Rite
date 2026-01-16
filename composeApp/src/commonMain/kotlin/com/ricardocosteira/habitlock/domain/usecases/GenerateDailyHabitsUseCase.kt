package com.ricardocosteira.habitlock.domain.usecases

import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Generates daily habit instances for all active, non-archived habits.
 * Called on app launch and by daily background job.
 */
class GenerateDailyHabitsUseCase(
    private val userRepository: UserRepository,
    private val habitRepository: HabitRepository,
    private val habitInstanceRepository: HabitInstanceRepository,
    private val uuidProvider: UuidProvider
) {

    /**
     * Generate habit instances for today if they don't already exist.
     * @return List of newly created instances, empty if already generated.
     */
    suspend fun execute(): List<HabitInstance> {
        val user = userRepository.getUser() ?: return emptyList()
        val today = Clock.System.todayIn(user.timezone)

        // Check if we need to detect timezone change
        checkTimezoneChange(user.timezone)

        val activeHabits = habitRepository.getActiveHabits()
        val existingInstances = habitInstanceRepository.getInstancesForDate(today)
        val existingHabitIds = existingInstances.map { it.habitId }.toSet()

        val newInstances = mutableListOf<HabitInstance>()

        for (habit in activeHabits) {
            if (habit.id in existingHabitIds) continue

            // Calculate consecutive skips from previous instances
            val consecutiveSkips = calculateConsecutiveSkips(habit.id)

            val instance = HabitInstance(
                id = uuidProvider.generate(),
                habitId = habit.id,
                date = today,
                status = HabitStatus.PENDING,
                completedValue = if (habit.type == HabitType.QUANTITATIVE) 0 else null,
                targetValue = habit.targetValue,
                consecutiveSkipsAtCreation = consecutiveSkips,
                createdAt = Clock.System.now()
            )

            habitInstanceRepository.createInstance(instance)
            newInstances.add(instance)
        }

        return newInstances
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

private fun Clock.System.todayIn(timezone: TimeZone): LocalDate = now().toLocalDateTime(timezone).date
