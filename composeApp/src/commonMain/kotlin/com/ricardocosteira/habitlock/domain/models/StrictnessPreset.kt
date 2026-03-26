package com.ricardocosteira.habitlock.domain.models

/**
 * Preset strictness levels chosen during onboarding.
 *
 * Each preset defines a combination of policies that determine
 * how forgiving or strict the app is with the user's habit tracking.
 */
enum class StrictnessPreset {
    /**
     * Gentle support with maximum forgiveness.
     * - Unlimited undo for all history
     * - Unlimited snoozes
     * - Unlimited skips
     * - Longer snooze duration (60 minutes)
     */
    FLEXIBLE,

    /**
     * Structure with room for real life (default/recommended).
     * - Undo allowed for today only
     * - Limited snoozes (3 per habit per day)
     * - Limited consecutive skips (2)
     * - Moderate snooze duration (30 minutes)
     */
    BALANCED,

    /**
     * No excuses, full accountability.
     * - No undo allowed
     * - Very limited snoozes (1 per habit per day)
     * - No skips allowed (0)
     * - Short snooze duration (15 minutes)
     */
    LOCKED,

    ;

    /**
     * Converts this preset to user settings.
     */
    fun toUserSettings(): UserStrictnessSettings =
        when (this) {
            FLEXIBLE -> {
                UserStrictnessSettings(
                    undoPolicy = UndoPolicy.ALL_HISTORY,
                    maxSnoozesPerHabitPerDay = null,
                    maxConsecutiveSkips = null,
                    maxSnoozeDurationMinutes = 60,
                )
            }

            BALANCED -> {
                UserStrictnessSettings(
                    undoPolicy = UndoPolicy.TODAY_ONLY,
                    maxSnoozesPerHabitPerDay = 3,
                    maxConsecutiveSkips = 2,
                    maxSnoozeDurationMinutes = 30,
                )
            }

            LOCKED -> {
                UserStrictnessSettings(
                    undoPolicy = UndoPolicy.NONE,
                    maxSnoozesPerHabitPerDay = 1,
                    maxConsecutiveSkips = 0,
                    maxSnoozeDurationMinutes = 15,
                )
            }
        }

    companion object {
        /**
         * Default preset recommended for most users.
         */
        val DEFAULT = BALANCED

        /**
         * Reverse-maps user settings back to a preset, or null if the settings
         * don't match any known preset (i.e. the user customised them).
         */
        fun fromSettings(settings: UserStrictnessSettings): StrictnessPreset? = entries.firstOrNull { it.toUserSettings() == settings }
    }
}

/**
 * User strictness settings derived from a preset.
 *
 * @property undoPolicy How far back can the user undo actions
 * @property maxSnoozesPerHabitPerDay Maximum snoozes per habit per day (null = unlimited)
 * @property maxConsecutiveSkips Maximum consecutive skips allowed (null = unlimited)
 * @property maxSnoozeDurationMinutes Maximum duration for each snooze in minutes
 */
data class UserStrictnessSettings(
    val undoPolicy: UndoPolicy,
    val maxSnoozesPerHabitPerDay: Int?,
    val maxConsecutiveSkips: Int?,
    val maxSnoozeDurationMinutes: Int,
) {
    init {
        if (maxSnoozesPerHabitPerDay != null) {
            require(maxSnoozesPerHabitPerDay > 0) { "Max snoozes must be positive" }
        }
        if (maxConsecutiveSkips != null) {
            require(maxConsecutiveSkips >= 0) { "Max consecutive skips must be non-negative" }
        }
        require(maxSnoozeDurationMinutes > 0) { "Max snooze duration must be positive" }
    }

    /**
     * Whether undo is completely disabled.
     */
    val isUndoDisabled: Boolean
        get() = undoPolicy == UndoPolicy.NONE

    /**
     * Whether snoozes are unlimited.
     */
    val hasUnlimitedSnoozes: Boolean
        get() = maxSnoozesPerHabitPerDay == null

    /**
     * Whether skips are unlimited.
     */
    val hasUnlimitedSkips: Boolean
        get() = maxConsecutiveSkips == null

    /**
     * Whether skips are completely disabled.
     */
    val isSkipDisabled: Boolean
        get() = maxConsecutiveSkips == 0
}
