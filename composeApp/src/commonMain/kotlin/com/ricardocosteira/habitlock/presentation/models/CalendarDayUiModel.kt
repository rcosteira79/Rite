package com.ricardocosteira.habitlock.presentation.models

import kotlinx.datetime.LocalDate

/**
 * Classification of a day in the calendar.
 */
enum class DayClassification {
    /** No habits failed on this day */
    PERFECT,
    
    /** Some habits completed/skipped, but at least one failed */
    PARTIAL,
    
    /** At least one habit failed */
    FAILED,
    
    /** No data for this day */
    NONE
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

