package com.ricardocosteira.rite.domain.models

import kotlinx.datetime.LocalTime

/**
 * Reminder configuration for a habit.
 */
data class HabitReminder(
    val id: String,
    val habitId: String,
    val reminderType: ReminderType,
    val time: LocalTime?,
    val intervalMinutes: Int?,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val isActive: Boolean
)

