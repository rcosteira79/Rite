package com.ricardocosteira.habitlock.domain.repositories

import com.ricardocosteira.habitlock.domain.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone

/**
 * Repository for user data and settings.
 */
interface UserRepository {

    /**
     * Observe the current user. Emits null if no user exists.
     */
    fun observeUser(): Flow<User?>

    /**
     * Get the current user synchronously.
     */
    suspend fun getUser(): User?

    /**
     * Create the initial user with default settings.
     */
    suspend fun createDefaultUser(timezone: TimeZone): User

    /**
     * Update the user settings.
     */
    suspend fun updateUser(user: User)

    /**
     * Update user timezone and store previous timezone.
     */
    suspend fun updateTimezone(userId: String, newTimezone: TimeZone, previousTimezone: TimeZone)

    /**
     * Mark onboarding as completed.
     */
    suspend fun setOnboardingCompleted(userId: String, isCompleted: Boolean)
}


