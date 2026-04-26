package com.ricardocosteira.rite.domain.repositories

import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitStatus
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Repository for habit instances (daily occurrences).
 */
interface HabitInstanceRepository {
    /**
     * Observe habit instances for a specific date.
     */
    fun observeInstancesForDate(date: LocalDate): Flow<List<HabitInstance>>

    /**
     * Get habit instances for a specific date.
     */
    suspend fun getInstancesForDate(date: LocalDate): List<HabitInstance>

    /**
     * Get pending instances for a specific date.
     */
    suspend fun getPendingInstancesForDate(date: LocalDate): List<HabitInstance>

    /**
     * Get a specific instance by ID.
     */
    suspend fun getInstanceById(instanceId: String): HabitInstance?

    /**
     * Get the instance for a specific habit on a specific date.
     */
    suspend fun getInstanceForHabitAndDate(habitId: String, date: LocalDate): HabitInstance?

    /**
     * Get all instances for a habit (for history/streaks).
     */
    suspend fun getInstancesForHabit(habitId: String): List<HabitInstance>

    /**
     * Get instances in a date range (for calendar view).
     */
    suspend fun getInstancesInDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<HabitInstance>

    /**
     * Observe instances in a date range. Re-emits whenever any instance in the
     * range is inserted, updated, or deleted.
     */
    fun observeInstancesInDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HabitInstance>>

    /**
     * Create a new habit instance.
     */
    suspend fun createInstance(instance: HabitInstance)

    /**
     * Update the status and completed value of an instance.
     */
    suspend fun updateInstanceStatus(
        instanceId: String,
        status: HabitStatus,
        completedValue: Int?,
        completedAt: Instant?
    )

    /**
     * Update just the completed value of an instance.
     */
    suspend fun updateInstanceCompletedValue(instanceId: String, completedValue: Int)

    /**
     * Update the target value of an instance.
     */
    suspend fun updateInstanceTargetValue(instanceId: String, targetValue: Int?)
}
