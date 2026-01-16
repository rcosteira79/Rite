package com.ricardocosteira.habitlock.domain.models

import kotlinx.datetime.LocalDate

/**
 * Schedule defining when a habit is expected.
 * MVP only supports DAILY schedules.
 */
data class HabitSchedule(
    val id: String,
    val habitId: String,
    val scheduleType: ScheduleType,
    val startDate: LocalDate,
    val endDate: LocalDate?
)

enum class ScheduleType {
    DAILY
}

