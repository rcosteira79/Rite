package com.ricardocosteira.habitlock.fakes

import com.ricardocosteira.habitlock.domain.models.UndoPolicy
import com.ricardocosteira.habitlock.domain.models.User
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.TimeZone

class FakeUserRepository : UserRepository {

    private val userFlow: MutableStateFlow<User?> = MutableStateFlow(buildDefaultUser())
    private var currentUser: User? = buildDefaultUser()

    fun setUser(user: User?) {
        currentUser = user
        userFlow.value = user
    }

    override fun observeUser(): Flow<User?> = userFlow.asStateFlow()

    override suspend fun getUser(): User? = currentUser

    override suspend fun createDefaultUser(timezone: TimeZone): User {
        val user: User = buildDefaultUser(timezone = timezone)
        currentUser = user
        userFlow.value = user
        return user
    }

    override suspend fun updateUser(user: User) {
        currentUser = user
        userFlow.value = user
    }

    override suspend fun updateTimezone(
        userId: String,
        newTimezone: TimeZone,
        previousTimezone: TimeZone
    ) {
        val user: User = currentUser ?: return
        val updatedUser: User = user.copy(
            timezone = newTimezone,
            previousTimezone = previousTimezone
        )
        currentUser = updatedUser
        userFlow.value = updatedUser
    }

    override suspend fun setOnboardingCompleted(userId: String, isCompleted: Boolean) {
        val user: User = currentUser ?: return
        val updatedUser: User = user.copy(isOnboardingCompleted = isCompleted)
        currentUser = updatedUser
        userFlow.value = updatedUser
    }

    companion object {
        fun buildDefaultUser(id: String = "user-1", timezone: TimeZone = TimeZone.UTC): User = User(
            id = id,
            timezone = timezone,
            previousTimezone = null,
            undoPolicy = UndoPolicy.ALL_HISTORY,
            maxSnoozeDurationMinutes = User.DEFAULT_MAX_SNOOZE_DURATION_MINUTES,
            maxSnoozesPerHabitPerDay = User.DEFAULT_MAX_SNOOZES_PER_HABIT_PER_DAY,
            maxConsecutiveSkips = User.DEFAULT_MAX_CONSECUTIVE_SKIPS,
            isOnboardingCompleted = true,
            dailySummaryTime = null,
            createdAt = Clock.System.now()
        )
    }
}
