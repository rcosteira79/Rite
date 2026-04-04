package com.ricardocosteira.rite.domain.repositories

import com.ricardocosteira.rite.domain.models.CompletionSource
import com.ricardocosteira.rite.domain.models.HabitCompletionEvent

/**
 * Repository for habit completion events (event sourcing).
 */
interface HabitCompletionEventRepository {
    
    /**
     * Get all events for a specific instance.
     */
    suspend fun getEventsForInstance(instanceId: String): List<HabitCompletionEvent>
    
    /**
     * Record a new completion event.
     */
    suspend fun recordEvent(
        instanceId: String,
        deltaValue: Int,
        source: CompletionSource
    ): HabitCompletionEvent
    
    /**
     * Delete a specific event (for undo).
     */
    suspend fun deleteEvent(eventId: String)
    
    /**
     * Delete all events for an instance (for full undo).
     */
    suspend fun deleteEventsForInstance(instanceId: String)
    
    /**
     * Calculate the total completed value from all events for an instance.
     */
    suspend fun calculateCompletedValue(instanceId: String): Int
}

