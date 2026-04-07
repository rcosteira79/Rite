package com.ricardocosteira.rite.domain.usecases

import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.repositories.HabitCompletionEventRepository
import com.ricardocosteira.rite.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.rite.domain.repositories.HabitRepository
import me.tatarka.inject.annotations.Inject

/**
 * Removes the most recent increment from a quantitative habit instance.
 * Works on both PENDING (in-progress) and COMPLETED instances.
 * When undoing from COMPLETED, reverts status to PENDING and adjusts streak/score.
 */
@Inject
class UndoLastIncrement(
    private val habitInstanceRepository: HabitInstanceRepository,
    private val habitCompletionEventRepository: HabitCompletionEventRepository,
    private val habitRepository: HabitRepository
) {

    suspend fun execute(instanceId: String): Result<HabitInstance> {
        val instance = habitInstanceRepository.getInstanceById(instanceId)
            ?: return Result.failure(IllegalArgumentException("Instance not found"))

        if (instance.status != HabitStatus.PENDING && instance.status != HabitStatus.COMPLETED) {
            return Result.failure(
                IllegalStateException("Can only undo increments on pending or completed instances")
            )
        }

        val events = habitCompletionEventRepository.getEventsForInstance(instanceId)
        if (events.isEmpty()) {
            return Result.failure(IllegalStateException("No progress to undo"))
        }

        val wasCompleted: Boolean = instance.status == HabitStatus.COMPLETED
        val latestEvent = events.maxBy { it.timestamp }
        val undoneAmount: Int = latestEvent.deltaValue

        habitCompletionEventRepository.deleteEvent(latestEvent.id)

        val newCompletedValue: Int =
            habitCompletionEventRepository.calculateCompletedValue(instanceId)

        habitInstanceRepository.updateInstanceStatus(
            instanceId = instanceId,
            status = HabitStatus.PENDING,
            completedValue = newCompletedValue,
            completedAt = null
        )

        // If reverting from COMPLETED, adjust streak and total completions
        if (wasCompleted) {
            val habit = habitRepository.getHabitById(instance.habitId)
            if (habit != null && habit.currentStreak > 0) {
                val newCurrentStreak: Int = habit.currentStreak - 1
                val newLongestStreak: Int = if (habit.currentStreak == habit.longestStreak) {
                    newCurrentStreak
                } else {
                    habit.longestStreak
                }
                habitRepository.updateHabitStreak(
                    habitId = habit.id,
                    currentStreak = newCurrentStreak,
                    longestStreak = newLongestStreak
                )
            }
            habitRepository.decrementHabitTotalCompletions(
                habitId = instance.habitId,
                amount = undoneAmount
            )
        }

        return habitInstanceRepository.getInstanceById(instanceId)
            ?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("Failed to retrieve updated instance"))
    }
}
