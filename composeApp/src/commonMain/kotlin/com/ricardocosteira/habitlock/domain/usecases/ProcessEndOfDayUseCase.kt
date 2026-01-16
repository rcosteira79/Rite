package com.ricardocosteira.habitlock.domain.usecases

import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * Processes end-of-day failure for habits from the previous day.
 * Marks all PENDING instances from yesterday as FAILED.
 */
class ProcessEndOfDayUseCase(
    private val userRepository: UserRepository,
    private val habitInstanceRepository: HabitInstanceRepository,
    private val habitRepository: HabitRepository
) {

    /**
     * Process end-of-day for the previous day.
     * @return Number of instances marked as FAILED.
     */
    suspend fun execute(): Int {
        val user = userRepository.getUser() ?: return 0
        val today = Clock.System.now().toLocalDate(user.timezone)
        val yesterday = today.minus(DatePeriod(days = 1))

        val pendingInstances = habitInstanceRepository.getPendingInstancesForDate(yesterday)

        var failedCount = 0
        for (instance in pendingInstances) {
            habitInstanceRepository.updateInstanceStatus(
                instanceId = instance.id,
                status = HabitStatus.FAILED,
                completedValue = instance.completedValue
            )

            // Reset streak for failed habit
            val habit = habitRepository.getHabitById(instance.habitId)
            if (habit != null) {
                habitRepository.updateHabitStreak(
                    habitId = habit.id,
                    currentStreak = 0,
                    longestStreak = habit.longestStreak
                )
            }

            failedCount++
        }

        return failedCount
    }
}

private fun kotlin.time.Instant.toLocalDate(timezone: TimeZone) =
    this.toLocalDateTime(timezone).date
