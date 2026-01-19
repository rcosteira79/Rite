package com.ricardocosteira.habitlock.domain.usecases

import me.tatarka.inject.annotations.Inject

import com.ricardocosteira.habitlock.domain.models.StrictnessPreset
import com.ricardocosteira.habitlock.domain.models.User
import com.ricardocosteira.habitlock.domain.repositories.UserRepository

/**
 * Applies a strictness preset to the user settings during onboarding.
 */
@Inject
class ApplyStrictnessPresetUseCase(
    private val userRepository: UserRepository
) {

    suspend fun execute(preset: StrictnessPreset): Result<User> {
        val user = userRepository.getUser()
            ?: return Result.failure(IllegalStateException("User not found"))

        val settings = preset.toUserSettings()

        val updatedUser = user.copy(
            undoPolicy = settings.undoPolicy,
            maxSnoozesPerHabitPerDay = settings.maxSnoozesPerHabitPerDay,
            maxSnoozeDurationMinutes = settings.maxSnoozeDurationMinutes,
            maxConsecutiveSkips = settings.maxConsecutiveSkips
        )

        userRepository.updateUser(updatedUser)

        return Result.success(updatedUser)
    }
}

