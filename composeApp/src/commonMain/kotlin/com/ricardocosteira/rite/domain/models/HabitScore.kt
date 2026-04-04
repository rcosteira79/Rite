package com.ricardocosteira.rite.domain.models

import kotlinx.datetime.LocalDate

/**
 * Represents the cumulative performance score for a habit.
 *
 * HabitScore tracks long-term consistency and over-completion,
 * providing a percentage that reflects how well the user has
 * maintained the habit relative to expectations.
 *
 * Formula: min(overCompletionCap, (totalCompletions / expectedCompletions) * 100)
 *
 * @property totalCompletions Total number of completions across all time
 * @property expectedCompletions Total number of expected completions based on cadence
 * @property overCompletionCap Maximum score percentage (default: 150, allows 50% over-completion)
 * @property lastCalculatedAt Date when score was last calculated (for caching)
 */
data class HabitScore(
    val totalCompletions: Int,
    val expectedCompletions: Int,
    val overCompletionCap: Int = DEFAULT_OVER_COMPLETION_CAP,
    val lastCalculatedAt: LocalDate? = null
) {
    init {
        require(totalCompletions >= 0) { "Total completions cannot be negative" }
        require(expectedCompletions >= 0) { "Expected completions cannot be negative" }
        require(overCompletionCap > 0) { "Over-completion cap must be positive" }
    }

    /**
     * The habit score as a percentage (0-overCompletionCap).
     */
    val percentage: Int
        get() {
            if (expectedCompletions == 0) return 0
            val rawPercentage = (totalCompletions * 100) / expectedCompletions
            return rawPercentage.coerceIn(0, overCompletionCap)
        }

    /**
     * The habit score as a float (0.0 to overCompletionCap/100.0).
     * Useful for progress bars and visual representations.
     */
    val percentageFloat: Float
        get() = percentage / 100f

    /**
     * Whether the habit has reached or exceeded 100% completion rate.
     */
    val isPerfect: Boolean
        get() = percentage >= 100

    /**
     * Whether the habit has over-completed beyond 100%.
     */
    val isOverCompleted: Boolean
        get() = percentage > 100

    /**
     * Number of extra completions beyond what was expected.
     */
    val overCompletionCount: Int
        get() = (totalCompletions - expectedCompletions).coerceAtLeast(0)

    /**
     * Number of missed completions (if any).
     */
    val missedCompletionCount: Int
        get() = (expectedCompletions - totalCompletions).coerceAtLeast(0)

    /**
     * Creates a new HabitScore with incremented completion count.
     */
    fun withIncrementedCompletion(amount: Int = 1): HabitScore {
        return copy(totalCompletions = totalCompletions + amount)
    }

    /**
     * Creates a new HabitScore with incremented expected count.
     * Used when a new cadence window is added (e.g., new day or week).
     */
    fun withIncrementedExpected(amount: Int = 1): HabitScore {
        return copy(expectedCompletions = expectedCompletions + amount)
    }

    /**
     * Creates a new HabitScore with decremented completion count.
     * Used when undoing a completion.
     */
    fun withDecrementedCompletion(amount: Int = 1): HabitScore {
        return copy(totalCompletions = (totalCompletions - amount).coerceAtLeast(0))
    }

    companion object {
        /**
         * Default over-completion cap at 150% (allows 50% over-completion).
         */
        const val DEFAULT_OVER_COMPLETION_CAP: Int = 150

        /**
         * Creates a new HabitScore starting from zero.
         */
        fun initial(overCompletionCap: Int = DEFAULT_OVER_COMPLETION_CAP): HabitScore {
            return HabitScore(
                totalCompletions = 0,
                expectedCompletions = 0,
                overCompletionCap = overCompletionCap
            )
        }
    }
}
