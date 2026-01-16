package com.ricardocosteira.habitlock.domain.usecases

import com.ricardocosteira.habitlock.domain.models.StrictnessPreset
import com.ricardocosteira.habitlock.domain.models.UndoPolicy
import com.ricardocosteira.habitlock.domain.models.User
import com.ricardocosteira.habitlock.domain.repositories.UserRepository

/**
 * Applies a strictness preset to the user settings during onboarding.
 */
class ApplyStrictnessPresetUseCase(
    private val userRepository: UserRepository
) {

    suspend fun execute(preset: StrictnessPreset): Result<User> {
        val user = userRepository.getUser()
            ?: return Result.failure(IllegalStateException("User not found"))

        val updatedUser = when (preset) {
            StrictnessPreset.FLEXIBLE -> user.copy(
                undoPolicy = UndoPolicy.ALL_HISTORY,
                maxSnoozesPerHabitPerDay = null, // unlimited
                maxSnoozeDurationMinutes = 60,
                maxConsecutiveSkips = null // no skip limit
            )
            StrictnessPreset.BALANCED -> user.copy(
                undoPolicy = UndoPolicy.TODAY_ONLY,
                maxSnoozesPerHabitPerDay = 3,
                maxSnoozeDurationMinutes = 30,
                maxConsecutiveSkips = 2
            )
            StrictnessPreset.LOCKED -> user.copy(
                undoPolicy = UndoPolicy.NONE,
                maxSnoozesPerHabitPerDay = 1,
                maxSnoozeDurationMinutes = 15,
                maxConsecutiveSkips = 2
            )
        }

        userRepository.updateUser(updatedUser)

        return Result.success(updatedUser)
    }
}

