package com.ricardocosteira.habitlock.domain.models

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/**
 * Represents one habit instance on a specific day.
 * Created daily for each active, non-archived habit.
 */
data class HabitInstance(
    val id: String,
    val habitId: String,
    val date: LocalDate,
    val status: HabitStatus,
    val completedValue: Int?,
    val targetValue: Int?,
    val consecutiveSkipsAtCreation: Int,
    val createdAt: Instant,
    val completedAt: Instant? = null,
) {
    /**
     * Whether skip is disabled due to reaching the consecutive skip limit.
     * This is derived based on user settings.
     */
    fun isSkipLocked(maxConsecutiveSkips: Int?): Boolean {
        if (maxConsecutiveSkips == null) return false
        return consecutiveSkipsAtCreation >= maxConsecutiveSkips
    }

    /**
     * Whether this quantitative habit is complete based on progress.
     */
    fun isQuantitativeComplete(): Boolean {
        val target = targetValue ?: return (completedValue ?: 0) > 0
        return (completedValue ?: 0) >= target
    }

    /**
     * Progress percentage for quantitative habits (0.0 to 1.0).
     */
    fun progressPercentage(): Float {
        val target = targetValue ?: return if ((completedValue ?: 0) > 0) 1f else 0f
        if (target <= 0) return 0f
        return ((completedValue ?: 0).toFloat() / target).coerceIn(0f, 1f)
    }

    /**
     * Current progress for quantitative habits.
     * Returns 0 if no completions have been recorded.
     */
    val currentProgress: Int
        get() = completedValue ?: 0
}
