package com.ricardocosteira.habitlock.domain.usecases

import com.ricardocosteira.habitlock.domain.models.CompletionSource
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.repositories.HabitCompletionEventRepository
import com.ricardocosteira.habitlock.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository

/**
 * Completes a habit instance (binary or quantitative).
 */
class CompleteHabitUseCase(
    private val habitInstanceRepository: HabitInstanceRepository,
    private val habitRepository: HabitRepository,
    private val habitCompletionEventRepository: HabitCompletionEventRepository
) {

    /**
     * Complete a binary habit.
     */
    suspend fun executeBinary(instanceId: String, source: CompletionSource): Result<HabitInstance> {
        val instance = habitInstanceRepository.getInstanceById(instanceId)
            ?: return Result.failure(IllegalArgumentException("Instance not found"))
        
        if (instance.status != HabitStatus.PENDING) {
            return Result.failure(IllegalStateException("Instance is not pending"))
        }
        
        // Record completion event
        habitCompletionEventRepository.recordEvent(
            instanceId = instanceId,
            deltaValue = 1,
            source = source
        )
        
        // Update instance status
        habitInstanceRepository.updateInstanceStatus(
            instanceId = instanceId,
            status = HabitStatus.COMPLETED,
            completedValue = 1
        )
        
        // Update streak and score
        updateStreak(instance.habitId)
        incrementTotalCompletions(instance.habitId, amount = 1)
        
        val updatedInstance = habitInstanceRepository.getInstanceById(instanceId)
            ?: return Result.failure(IllegalStateException("Failed to retrieve updated instance"))
        
        return Result.success(updatedInstance)
    }

    /**
     * Add progress to a quantitative habit.
     * @param deltaValue The amount to add (typically 1 or a custom value).
     */
    suspend fun executeQuantitative(
        instanceId: String,
        deltaValue: Int,
        source: CompletionSource
    ): Result<HabitInstance> {
        val instance = habitInstanceRepository.getInstanceById(instanceId)
            ?: return Result.failure(IllegalArgumentException("Instance not found"))
        
        if (instance.status == HabitStatus.COMPLETED) {
            return Result.failure(IllegalStateException("Instance is already completed"))
        }
        
        if (instance.status == HabitStatus.FAILED) {
            return Result.failure(IllegalStateException("Instance has failed"))
        }
        
        // Record completion event
        habitCompletionEventRepository.recordEvent(
            instanceId = instanceId,
            deltaValue = deltaValue,
            source = source
        )
        
        // Calculate new total
        val newCompletedValue = habitCompletionEventRepository.calculateCompletedValue(instanceId)
        
        // Check if completed
        val isComplete = checkQuantitativeComplete(newCompletedValue, instance.targetValue)
        val newStatus = if (isComplete) HabitStatus.COMPLETED else HabitStatus.PENDING
        
        habitInstanceRepository.updateInstanceStatus(
            instanceId = instanceId,
            status = newStatus,
            completedValue = newCompletedValue
        )
        
        // Update score (always increment, even for partial progress)
        incrementTotalCompletions(instance.habitId, amount = deltaValue)
        
        if (isComplete) {
            updateStreak(instance.habitId)
        }
        
        val updatedInstance = habitInstanceRepository.getInstanceById(instanceId)
            ?: return Result.failure(IllegalStateException("Failed to retrieve updated instance"))
        
        return Result.success(updatedInstance)
    }
    
    private fun checkQuantitativeComplete(completedValue: Int, targetValue: Int?): Boolean {
        return if (targetValue != null) {
            completedValue >= targetValue
        } else {
            completedValue > 0
        }
    }
    
    private suspend fun updateStreak(habitId: String) {
        val habit = habitRepository.getHabitById(habitId) ?: return
        val newCurrentStreak = habit.currentStreak + 1
        val newLongestStreak = maxOf(habit.longestStreak, newCurrentStreak)
        
        habitRepository.updateHabitStreak(
            habitId = habitId,
            currentStreak = newCurrentStreak,
            longestStreak = newLongestStreak
        )
    }
    
    private suspend fun incrementTotalCompletions(habitId: String, amount: Int) {
        habitRepository.incrementHabitTotalCompletions(habitId, amount)
    }
}

