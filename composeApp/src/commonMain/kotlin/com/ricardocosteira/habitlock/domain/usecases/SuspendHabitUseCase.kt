package com.ricardocosteira.habitlock.domain.usecases

import com.ricardocosteira.habitlock.domain.models.LeavePeriod
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository
import com.ricardocosteira.habitlock.domain.repositories.LeavePeriodRepository
import kotlin.time.Clock
import kotlinx.datetime.LocalDate

/**
 * Suspends a habit for a specified period (Leave Mode).
 * 
 * During suspension:
 * - Habit instances are created with SUSPENDED status
 * - No notifications are sent
 * - Streaks are not affected
 * - Failures are not counted
 */
class SuspendHabitUseCase(
    private val habitRepository: HabitRepository,
    private val leavePeriodRepository: LeavePeriodRepository,
    private val uuidProvider: UuidProvider
) {

    /**
     * Suspends a habit for a specified date range.
     * 
     * @param habitId The ID of the habit to suspend
     * @param startDate The first day of suspension (inclusive)
     * @param endDate The last day of suspension (inclusive, null for indefinite)
     * @param reason Optional explanation for the suspension
     * @return Result with the created LeavePeriod, or failure if habit doesn't exist
     */
    suspend fun execute(
        habitId: String,
        startDate: LocalDate,
        endDate: LocalDate?,
        reason: String? = null
    ): Result<LeavePeriod> {
        // Verify habit exists
        val habit = habitRepository.getHabitById(habitId)
            ?: return Result.failure(IllegalArgumentException("Habit not found"))

        // Validate dates
        if (endDate != null && endDate < startDate) {
            return Result.failure(IllegalArgumentException("End date must be after start date"))
        }

        // Check for overlapping leave periods
        val existingLeavePeriods = leavePeriodRepository.getLeavePeriodsByHabit(habitId)
        val hasOverlap = existingLeavePeriods.any { existing ->
            existing.overlaps(startDate, endDate)
        }

        if (hasOverlap) {
            return Result.failure(
                IllegalStateException("Leave period overlaps with an existing suspension")
            )
        }

        // Create leave period
        val leavePeriod = LeavePeriod(
            id = uuidProvider.generate(),
            habitId = habitId,
            startDate = startDate,
            endDate = endDate,
            reason = reason,
            createdAt = Clock.System.now()
        )

        leavePeriodRepository.createLeavePeriod(leavePeriod)

        return Result.success(leavePeriod)
    }

    /**
     * Checks if two date ranges overlap.
     */
    private fun LeavePeriod.overlaps(newStart: LocalDate, newEnd: LocalDate?): Boolean {
        // If this leave period has ended, no overlap
        if (this.endDate != null && this.endDate < newStart) return false
        
        // If new period ends before this one starts, no overlap
        if (newEnd != null && newEnd < this.startDate) return false
        
        // Otherwise, there's an overlap
        return true
    }
}
