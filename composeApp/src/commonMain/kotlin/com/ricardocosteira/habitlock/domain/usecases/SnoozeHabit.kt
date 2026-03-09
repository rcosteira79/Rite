package com.ricardocosteira.habitlock.domain.usecases

import me.tatarka.inject.annotations.Inject

import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.models.SnoozeState
import com.ricardocosteira.habitlock.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.habitlock.domain.repositories.SnoozeRepository
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * Snoozes a habit reminder.
 * 
 * Snoozing delays the notification for a habit without marking it as skipped or completed.
 * The habit remains in PENDING status and can be completed later.
 */
@Inject
class SnoozeHabit(
    private val habitInstanceRepository: HabitInstanceRepository,
    private val snoozeRepository: SnoozeRepository,
    private val userRepository: UserRepository
) {

    /**
     * Snooze a habit reminder.
     * @param durationMinutes How long to snooze (will be capped to user's max).
     * @return Result with the scheduled snooze time, or error if not allowed.
     */
    suspend fun execute(instanceId: String, durationMinutes: Int): Result<SnoozeState> {
        val instance = habitInstanceRepository.getInstanceById(instanceId)
            ?: return Result.failure(IllegalArgumentException("Instance not found"))
        
        // Only PENDING instances can be snoozed
        if (instance.status != HabitStatus.PENDING) {
            return Result.failure(IllegalStateException("Only pending habits can be snoozed"))
        }
        
        val user = userRepository.getUser()
            ?: return Result.failure(IllegalStateException("User not found"))
        
        // Get current snooze state
        val currentSnooze = snoozeRepository.getSnoozeState(instanceId)
        val currentCount = currentSnooze?.snoozeCount ?: 0
        
        // Check if snooze limit reached
        val maxSnoozes = user.maxSnoozesPerHabitPerDay
        if (maxSnoozes != null && currentCount >= maxSnoozes) {
            return Result.failure(
                SnoozeLimitReachedException("Maximum snoozes reached for today")
            )
        }
        
        // Cap duration to user's max
        val actualDuration = minOf(durationMinutes, user.maxSnoozeDurationMinutes)
        
        // Calculate scheduled time
        val scheduledTime = Clock.System.now().plus(actualDuration.minutes)
        val newCount = currentCount + 1
        
        snoozeRepository.saveSnoozeState(
            instanceId = instanceId,
            scheduledTime = scheduledTime,
            snoozeCount = newCount
        )
        
        return Result.success(
            SnoozeState(
                habitInstanceId = instanceId,
                scheduledTime = scheduledTime,
                snoozeCount = newCount
            )
        )
    }
}

class SnoozeLimitReachedException(message: String) : Exception(message)

