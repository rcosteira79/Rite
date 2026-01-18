package com.ricardocosteira.habitlock.domain.usecases

import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitScore
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository

/**
 * Calculates the habit score for a given habit.
 * 
 * The score reflects long-term consistency and over-completion:
 * - Formula: min(overCompletionCap, (totalCompletions / expectedCompletions) * 100)
 * - Score ranges from 0 to overCompletionCap (default 150%)
 * - 100% = perfect completion rate
 * - >100% = over-completion (exceeding expectations)
 * - <100% = some completions were missed
 */
class CalculateHabitScoreUseCase(
    private val habitRepository: HabitRepository
) {

    /**
     * Calculate the score for a specific habit by ID.
     * Returns null if the habit is not found.
     */
    suspend fun execute(
        habitId: String,
        overCompletionCap: Int = HabitScore.DEFAULT_OVER_COMPLETION_CAP
    ): HabitScore? {
        val habit = habitRepository.getHabitById(habitId) ?: return null
        return calculateScoreForHabit(habit, overCompletionCap)
    }

    /**
     * Calculate the score for a given habit.
     */
    fun calculateScoreForHabit(
        habit: Habit,
        overCompletionCap: Int = HabitScore.DEFAULT_OVER_COMPLETION_CAP
    ): HabitScore {
        return HabitScore(
            totalCompletions = habit.totalCompletions,
            expectedCompletions = habit.expectedCompletions,
            overCompletionCap = overCompletionCap
        )
    }

    /**
     * Calculate scores for multiple habits.
     */
    fun calculateScoresForHabits(
        habits: List<Habit>,
        overCompletionCap: Int = HabitScore.DEFAULT_OVER_COMPLETION_CAP
    ): Map<String, HabitScore> {
        return habits.associate { habit ->
            habit.id to calculateScoreForHabit(habit, overCompletionCap)
        }
    }
}
