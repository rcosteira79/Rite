package com.ricardocosteira.habitlock.domain.repositories

import com.ricardocosteira.habitlock.domain.models.LeavePeriod
import kotlinx.datetime.LocalDate

/**
 * Repository for managing habit leave periods (suspensions).
 */
interface LeavePeriodRepository {

    /**
     * Creates a new leave period.
     */
    suspend fun createLeavePeriod(leavePeriod: LeavePeriod)

    /**
     * Gets a leave period by its ID.
     */
    suspend fun getLeavePeriodById(id: String): LeavePeriod?

    /**
     * Gets all leave periods for a specific habit, ordered by start date descending.
     */
    suspend fun getLeavePeriodsByHabit(habitId: String): List<LeavePeriod>

    /**
     * Gets the active leave period for a habit on a specific date.
     * Returns null if no active leave period exists.
     */
    suspend fun getActiveLeavePeriod(habitId: String, date: LocalDate): LeavePeriod?

    /**
     * Gets all active leave periods on a specific date across all habits.
     */
    suspend fun getAllActiveLeavePeriods(date: LocalDate): List<LeavePeriod>

    /**
     * Updates an existing leave period.
     */
    suspend fun updateLeavePeriod(leavePeriod: LeavePeriod)

    /**
     * Ends a leave period by setting its end date.
     */
    suspend fun endLeavePeriod(id: String, endDate: LocalDate)

    /**
     * Deletes a leave period.
     */
    suspend fun deleteLeavePeriod(id: String)

    /**
     * Deletes all leave periods for a specific habit.
     * Typically used when a habit is deleted.
     */
    suspend fun deleteLeavePeriodsForHabit(habitId: String)
}
