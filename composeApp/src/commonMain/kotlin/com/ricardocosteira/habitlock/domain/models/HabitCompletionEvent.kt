package com.ricardocosteira.habitlock.domain.models

import kotlin.time.Instant

/**
 * Represents a user action on a habit instance.
 * All derived state is recomputable from these events.
 */
data class HabitCompletionEvent(
    val id: String,
    val habitInstanceId: String,
    val timestamp: Instant,
    val deltaValue: Int,
    val source: CompletionSource
)

