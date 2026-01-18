package com.ricardocosteira.habitlock.domain.models

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * Schedule defining when a habit is expected.
 * Supports both DAILY and WEEKLY cadences.
 *
 * @property id Unique identifier for the schedule
 * @property habitId ID of the associated habit
 * @property scheduleType Type of schedule (DAILY or WEEKLY)
 * @property startDate When the schedule becomes active
 * @property endDate When the schedule ends (null for ongoing)
 * @property quota Number of completions required per cadence window (default: 1)
 * @property weekStartDay Day the week starts on (for WEEKLY schedules, default: Monday)
 * @property specificDays Specific days when habit should be done (for WEEKLY, null = all days)
 */
data class HabitSchedule(
    val id: String,
    val habitId: String,
    val scheduleType: ScheduleType,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val quota: Int = 1,
    val weekStartDay: DayOfWeek = DayOfWeek.MONDAY,
    val specificDays: Set<DayOfWeek>? = null
) {
    init {
        require(quota > 0) { "Quota must be greater than 0" }
        if (scheduleType == ScheduleType.WEEKLY && specificDays != null) {
            require(specificDays.isNotEmpty()) { "Specific days cannot be empty for weekly schedules" }
        }
    }

    /**
     * Checks if the schedule is active on a given date.
     */
    fun isActiveOn(date: LocalDate): Boolean {
        if (date < startDate) return false
        if (endDate != null && date > endDate) return false

        return when (scheduleType) {
            ScheduleType.DAILY -> true
            ScheduleType.WEEKLY -> {
                specificDays?.contains(date.dayOfWeek) ?: true
            }
        }
    }
}

/**
 * Defines the cadence of a habit schedule.
 */
enum class ScheduleType {
    /**
     * Habit resets daily. Quota must be completed each day.
     */
    DAILY,

    /**
     * Habit resets weekly. Quota must be completed within the week.
     */
    WEEKLY
}
