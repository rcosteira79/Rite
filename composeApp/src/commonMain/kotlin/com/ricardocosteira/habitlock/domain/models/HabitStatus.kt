package com.ricardocosteira.habitlock.domain.models

/**
 * Status of a habit instance for a specific day.
 */
enum class HabitStatus {
    PENDING,
    COMPLETED,
    SKIPPED,
    FAILED,
    SUSPENDED
}


