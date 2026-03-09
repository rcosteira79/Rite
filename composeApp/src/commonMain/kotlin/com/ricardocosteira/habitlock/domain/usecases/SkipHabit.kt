package com.ricardocosteira.habitlock.domain.usecases

import me.tatarka.inject.annotations.Inject

import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.habitlock.domain.repositories.UserRepository

/**
 * Skips a habit instance.
 */
@Inject
class SkipHabit(
    private val habitInstanceRepository: HabitInstanceRepository,
    private val userRepository: UserRepository
) {

    /**
     * Skip a habit instance.
     * @return Result with updated instance or error if skip not allowed.
     */
    suspend fun execute(instanceId: String): Result<HabitInstance> {
        val instance = habitInstanceRepository.getInstanceById(instanceId)
            ?: return Result.failure(IllegalArgumentException("Instance not found"))
        
        if (instance.status == HabitStatus.SUSPENDED) {
            return Result.failure(IllegalStateException("Cannot skip suspended habit"))
        }
        
        if (instance.status != HabitStatus.PENDING) {
            return Result.failure(IllegalStateException("Instance is not pending"))
        }
        
        val user = userRepository.getUser()
            ?: return Result.failure(IllegalStateException("User not found"))
        
        // Check if skip is locked
        if (instance.isSkipLocked(user.maxConsecutiveSkips)) {
            return Result.failure(
                SkipLockedException("Cannot skip: you have reached the maximum consecutive skip limit")
            )
        }
        
        habitInstanceRepository.updateInstanceStatus(
            instanceId = instanceId,
            status = HabitStatus.SKIPPED,
            completedValue = instance.completedValue
        )
        
        val updatedInstance = habitInstanceRepository.getInstanceById(instanceId)
            ?: return Result.failure(IllegalStateException("Failed to retrieve updated instance"))
        
        return Result.success(updatedInstance)
    }
}

class SkipLockedException(message: String) : Exception(message)

