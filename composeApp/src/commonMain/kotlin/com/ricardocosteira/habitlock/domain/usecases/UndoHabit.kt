package com.ricardocosteira.habitlock.domain.usecases

import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.models.UndoPolicy
import com.ricardocosteira.habitlock.domain.repositories.HabitCompletionEventRepository
import com.ricardocosteira.habitlock.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

/**
 * Undoes a habit completion or skip.
 */
@Inject
class UndoHabit(
    private val habitInstanceRepository: HabitInstanceRepository,
    private val habitCompletionEventRepository: HabitCompletionEventRepository,
    private val habitRepository: HabitRepository,
    private val userRepository: UserRepository
) {
    /**
     * Undo a habit action (completion or skip).
     * Respects user's undo policy.
     */
    suspend fun execute(instanceId: String): Result<HabitInstance> {
        val instance = habitInstanceRepository.getInstanceById(instanceId)
            ?: return Result.failure(IllegalArgumentException("Instance not found"))

        val user = userRepository.getUser()
            ?: return Result.failure(IllegalStateException("User not found"))

        // Check undo policy
        when (user.undoPolicy) {
            UndoPolicy.NONE -> {
                return Result.failure(UndoNotAllowedException("Undo is disabled"))
            }

            UndoPolicy.TODAY_ONLY -> {
                val today = Clock.System.now().toLocalDate(user.timezone)
                if (instance.date != today) {
                    return Result.failure(
                        UndoNotAllowedException("Can only undo today's habits")
                    )
                }
            }

            UndoPolicy.ALL_HISTORY -> {
                // Allowed for all dates
            }
        }

        if (instance.status == HabitStatus.PENDING) {
            return Result.failure(IllegalStateException("Instance is already pending"))
        }

        if (instance.status == HabitStatus.FAILED) {
            return Result.failure(IllegalStateException("Cannot undo failed habits"))
        }

        // Get the completed value before undoing (for score decrement)
        val completedValueToUndo = instance.completedValue ?: 0

        // Delete all completion events
        habitCompletionEventRepository.deleteEventsForInstance(instanceId)

        // Reset to pending with zero progress
        habitInstanceRepository.updateInstanceStatus(
            instanceId = instanceId,
            status = HabitStatus.PENDING,
            completedValue = 0,
            completedAt = null
        )

        // Recalculate streak and score (decrement if was completed)
        if (instance.status == HabitStatus.COMPLETED) {
            val habit = habitRepository.getHabitById(instance.habitId)
            if (habit != null && habit.currentStreak > 0) {
                habitRepository.updateHabitStreak(
                    habitId = habit.id,
                    currentStreak = habit.currentStreak - 1,
                    longestStreak = habit.longestStreak
                )
            }
        }

        // Decrement total completions by the completed value
        if (completedValueToUndo > 0) {
            habitRepository.decrementHabitTotalCompletions(
                habitId = instance.habitId,
                amount = completedValueToUndo
            )
        }

        val updatedInstance = habitInstanceRepository.getInstanceById(instanceId)
            ?: return Result.failure(
                IllegalStateException("Failed to retrieve updated instance")
            )

        return Result.success(updatedInstance)
    }
}

private fun kotlin.time.Instant.toLocalDate(timezone: TimeZone) =
    this.toLocalDateTime(timezone).date

class UndoNotAllowedException(message: String) : Exception(message)
