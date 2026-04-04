package com.ricardocosteira.rite.presentation.models

import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitStatus
import kotlinx.datetime.LocalDate

/**
 * Classification of a day in the calendar based on habit completion.
 */
enum class DayClassification {
    /** All non-suspended habits completed, no failures */
    PERFECT,

    /** All non-suspended habits completed, but some habits were suspended */
    BEST_EFFORT,

    /** Some habits completed or skipped, at least one pending, no failures */
    PARTIAL,

    /** Some habits completed or skipped, at least one failure */
    ROUGH_DAY,

    /** All non-suspended habits failed */
    FAILED,

    /** Day has not yet occurred (future date) */
    FUTURE,

    /** No habits scheduled for this day */
    NONE
}

/**
 * Classifies a day based on the status of its habit instances.
 */
fun classifyDay(instances: List<HabitInstance>, isFutureDate: Boolean): DayClassification {
    // Future dates
    if (isFutureDate) {
        return DayClassification.FUTURE
    }

    // No habits for the day
    if (instances.isEmpty()) {
        return DayClassification.NONE
    }

    // Filter out suspended habits for classification
    val nonSuspended = instances.filter { it.status != HabitStatus.SUSPENDED }

    // If all habits are suspended, it's a BEST_EFFORT day
    if (nonSuspended.isEmpty()) {
        return DayClassification.BEST_EFFORT
    }

    // Count statuses
    val completed = nonSuspended.count { it.status == HabitStatus.COMPLETED }
    val failed = nonSuspended.count { it.status == HabitStatus.FAILED }
    val pending = nonSuspended.count { it.status == HabitStatus.PENDING }
    val skipped = nonSuspended.count { it.status == HabitStatus.SKIPPED }

    // Classification logic
    return when {
        // All non-suspended habits failed
        failed == nonSuspended.size -> DayClassification.FAILED

        // All non-suspended habits completed
        completed == nonSuspended.size -> {
            if (instances.any { it.status == HabitStatus.SUSPENDED }) {
                DayClassification.BEST_EFFORT
            } else {
                DayClassification.PERFECT
            }
        }

        // At least one failure
        failed > 0 -> DayClassification.ROUGH_DAY

        // At least one pending (no failures)
        pending > 0 -> DayClassification.PARTIAL

        // Mix of completed and skipped (no failures, no pending)
        completed > 0 && skipped > 0 -> DayClassification.ROUGH_DAY

        // All skipped (no completions, no failures)
        skipped == nonSuspended.size -> DayClassification.ROUGH_DAY

        // Default fallback
        else -> DayClassification.PARTIAL
    }
}

/**
 * UI model for a day in the calendar view.
 */
data class CalendarDayUiModel(
    val date: LocalDate,
    val classification: DayClassification,
    val completedCount: Int,
    val skippedCount: Int,
    val failedCount: Int,
    val totalCount: Int
)
