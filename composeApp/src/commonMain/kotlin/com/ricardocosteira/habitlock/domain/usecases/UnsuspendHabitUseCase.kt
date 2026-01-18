package com.ricardocosteira.habitlock.domain.usecases

import com.ricardocosteira.habitlock.domain.models.LeavePeriod
import com.ricardocosteira.habitlock.domain.repositories.LeavePeriodRepository
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Ends a habit suspension (Leave Mode) early or removes it entirely.
 * 
 * This use case allows users to:
 * - End an ongoing suspension early (sets end date to today)
 * - Remove a future suspension (deletes the leave period)
 */
class UnsuspendHabitUseCase(
    private val leavePeriodRepository: LeavePeriodRepository,
    private val userRepository: UserRepository
) {

    /**
     * Ends a leave period early by setting its end date to today.
     * If the leave period hasn't started yet, it will be deleted instead.
     * 
     * @param leavePeriodId The ID of the leave period to end
     * @return Result with the updated LeavePeriod, or failure if not found
     */
    suspend fun execute(leavePeriodId: String): Result<LeavePeriod?> {
        val leavePeriod = leavePeriodRepository.getLeavePeriodById(leavePeriodId)
            ?: return Result.failure(IllegalArgumentException("Leave period not found"))

        val user = userRepository.getUser()
            ?: return Result.failure(IllegalStateException("User not found"))

        val today = Clock.System.now().toLocalDate(user.timezone)

        return when {
            // If leave period hasn't started yet, delete it
            today < leavePeriod.startDate -> {
                leavePeriodRepository.deleteLeavePeriod(leavePeriodId)
                Result.success(null)
            }
            
            // If leave period has already ended, nothing to do
            leavePeriod.hasEnded(today) -> {
                Result.failure(IllegalStateException("Leave period has already ended"))
            }
            
            // If leave period is currently active, end it today
            else -> {
                leavePeriodRepository.endLeavePeriod(leavePeriodId, today)
                val updatedLeavePeriod = leavePeriodRepository.getLeavePeriodById(leavePeriodId)
                Result.success(updatedLeavePeriod)
            }
        }
    }

    /**
     * Deletes a leave period entirely, regardless of its status.
     * Use this when the user wants to completely remove a suspension.
     * 
     * @param leavePeriodId The ID of the leave period to delete
     * @return Result indicating success or failure
     */
    suspend fun delete(leavePeriodId: String): Result<Unit> {
        val leavePeriod = leavePeriodRepository.getLeavePeriodById(leavePeriodId)
            ?: return Result.failure(IllegalArgumentException("Leave period not found"))

        leavePeriodRepository.deleteLeavePeriod(leavePeriodId)
        return Result.success(Unit)
    }
}

private fun kotlin.time.Instant.toLocalDate(timezone: TimeZone) =
    this.toLocalDateTime(timezone).date
