package com.ricardocosteira.habitlock.domain.usecases

import com.ricardocosteira.habitlock.domain.repositories.SnoozeRepository

/**
 * Clears the snooze state for a habit instance.
 * 
 * This is typically called when:
 * - A habit is completed
 * - A habit is skipped
 * - A habit fails (end of day)
 * - User manually cancels the snooze
 */
class ClearSnoozeStateUseCase(
    private val snoozeRepository: SnoozeRepository
) {

    /**
     * Clears the snooze state for a specific instance.
     * 
     * @param instanceId The ID of the habit instance
     * @return Result indicating success or failure
     */
    suspend fun execute(instanceId: String): Result<Unit> {
        return try {
            snoozeRepository.clearSnoozeState(instanceId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clears all snooze states.
     * Useful for cleanup operations or testing.
     * 
     * @return Result indicating success or failure
     */
    suspend fun clearAll(): Result<Unit> {
        return try {
            val allSnoozes = snoozeRepository.getAllSnoozeStates()
            allSnoozes.forEach { snooze ->
                snoozeRepository.clearSnoozeState(snooze.habitInstanceId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
