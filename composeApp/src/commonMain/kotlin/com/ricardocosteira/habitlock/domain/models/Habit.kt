package com.ricardocosteira.habitlock.domain.models

import kotlin.time.Instant

/**
 * Habit definition/template representing a repeating behavior to track.
 */
data class Habit(
    val id: String,
    val name: String,
    val description: String?,
    val type: HabitType,
    val targetValue: Int?,
    val unit: String?,
    val isActive: Boolean,
    val isArchived: Boolean,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCompletions: Int,
    val expectedCompletions: Int,
    val createdAt: Instant,
    val archivedAt: Instant?
) {
    /**
     * Calculates the habit score based on total and expected completions.
     */
    fun calculateScore(overCompletionCap: Int = HabitScore.DEFAULT_OVER_COMPLETION_CAP): HabitScore {
        return HabitScore(
            totalCompletions = totalCompletions,
            expectedCompletions = expectedCompletions,
            overCompletionCap = overCompletionCap
        )
    }
}

