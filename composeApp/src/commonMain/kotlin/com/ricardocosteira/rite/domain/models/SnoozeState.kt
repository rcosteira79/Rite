package com.ricardocosteira.rite.domain.models

import kotlin.time.Instant

/**
 * Persisted snooze state for a habit instance.
 */
data class SnoozeState(
    val habitInstanceId: String,
    val scheduledTime: Instant,
    val snoozeCount: Int
)

