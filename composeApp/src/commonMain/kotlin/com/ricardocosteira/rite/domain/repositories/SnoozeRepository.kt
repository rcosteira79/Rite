package com.ricardocosteira.rite.domain.repositories

import com.ricardocosteira.rite.domain.models.SnoozeState
import kotlin.time.Instant

/**
 * Repository for snooze state persistence.
 */
interface SnoozeRepository {

    /**
     * Get the current snooze state for an instance.
     */
    suspend fun getSnoozeState(instanceId: String): SnoozeState?

    /**
     * Save or update snooze state.
     */
    suspend fun saveSnoozeState(instanceId: String, scheduledTime: Instant, snoozeCount: Int)

    /**
     * Clear snooze state for an instance.
     */
    suspend fun clearSnoozeState(instanceId: String)

    /**
     * Get all active snooze states (for rescheduling after reboot).
     */
    suspend fun getAllSnoozeStates(): List<SnoozeState>
}

