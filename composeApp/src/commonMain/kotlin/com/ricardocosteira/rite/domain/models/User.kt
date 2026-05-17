package com.ricardocosteira.rite.domain.models

import kotlin.time.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

/**
 * User entity containing settings and preferences.
 */
data class User(
    val id: String,
    val timezone: TimeZone,
    val previousTimezone: TimeZone?,
    val undoPolicy: UndoPolicy,
    val maxSnoozeDurationMinutes: Int,
    val maxSnoozesPerHabitPerDay: Int?,
    val maxConsecutiveSkips: Int?,
    val isOnboardingCompleted: Boolean,
    val dailySummaryTime: LocalTime?,
    val createdAt: Instant
) {
    /**
     * Returns the [StrictnessPreset] matching this user's stored strictness fields,
     * or null if the values don't match any preset (custom settings).
     */
    fun toStrictnessPreset(): StrictnessPreset? = StrictnessPreset.fromSettings(
        UserStrictnessSettings(
            undoPolicy = undoPolicy,
            maxSnoozesPerHabitPerDay = maxSnoozesPerHabitPerDay,
            maxConsecutiveSkips = maxConsecutiveSkips,
            maxSnoozeDurationMinutes = maxSnoozeDurationMinutes
        )
    )

    companion object {
        const val DEFAULT_MAX_SNOOZE_DURATION_MINUTES: Int = 30
        const val DEFAULT_MAX_SNOOZES_PER_HABIT_PER_DAY: Int = 3
        const val DEFAULT_MAX_CONSECUTIVE_SKIPS: Int = 2
    }
}
