package com.ricardocosteira.rite.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.ricardocosteira.rite.data.database.RiteDatabase
import com.ricardocosteira.rite.domain.models.UndoPolicy
import com.ricardocosteira.rite.domain.models.User
import com.ricardocosteira.rite.domain.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import me.tatarka.inject.annotations.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Inject
class UserRepositoryImpl(
    private val database: RiteDatabase
) : UserRepository {

    private val queries = database.riteQueries

    override fun observeUser(): Flow<User?> {
        return queries.getUser()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { userEntity: com.ricardocosteira.rite.data.database.User? ->
                userEntity?.let { mapToUser(it) }
            }
    }

    override suspend fun getUser(): User? = withContext(Dispatchers.IO) {
        queries.getUser()
            .executeAsOneOrNull()
            ?.let { mapToUser(it) }
    }

    override suspend fun createDefaultUser(timezone: TimeZone): User = withContext(Dispatchers.IO) {
        val userId = generateUuid()
        val now = Clock.System.now()

        queries.insertUser(
            id = userId,
            timezone = timezone.id,
            previousTimezone = null,
            undoPolicy = UndoPolicy.TODAY_ONLY.name,
            maxSnoozeDurationMinutes = User.DEFAULT_MAX_SNOOZE_DURATION_MINUTES.toLong(),
            maxSnoozesPerHabitPerDay = User.DEFAULT_MAX_SNOOZES_PER_HABIT_PER_DAY.toLong(),
            maxConsecutiveSkips = User.DEFAULT_MAX_CONSECUTIVE_SKIPS.toLong(),
            onboardingCompleted = 0,
            dailySummaryTime = null,
            createdAt = now.toString()
        )

        User(
            id = userId,
            timezone = timezone,
            previousTimezone = null,
            undoPolicy = UndoPolicy.TODAY_ONLY,
            maxSnoozeDurationMinutes = User.DEFAULT_MAX_SNOOZE_DURATION_MINUTES,
            maxSnoozesPerHabitPerDay = User.DEFAULT_MAX_SNOOZES_PER_HABIT_PER_DAY,
            maxConsecutiveSkips = User.DEFAULT_MAX_CONSECUTIVE_SKIPS,
            isOnboardingCompleted = false,
            dailySummaryTime = null,
            createdAt = now
        )
    }

    override suspend fun updateUser(user: User): Unit = withContext(Dispatchers.IO) {
        queries.updateUser(
            timezone = user.timezone.id,
            previousTimezone = user.previousTimezone?.id,
            undoPolicy = user.undoPolicy.name,
            maxSnoozeDurationMinutes = user.maxSnoozeDurationMinutes.toLong(),
            maxSnoozesPerHabitPerDay = user.maxSnoozesPerHabitPerDay?.toLong(),
            maxConsecutiveSkips = user.maxConsecutiveSkips?.toLong(),
            onboardingCompleted = if (user.isOnboardingCompleted) 1 else 0,
            dailySummaryTime = user.dailySummaryTime?.toString(),
            id = user.id
        )
    }

    override suspend fun updateTimezone(
        userId: String,
        newTimezone: TimeZone,
        previousTimezone: TimeZone
    ): Unit = withContext(Dispatchers.IO) {
        queries.updateUserTimezone(
            timezone = newTimezone.id,
            previousTimezone = previousTimezone.id,
            id = userId
        )
    }

    override suspend fun setOnboardingCompleted(
        userId: String,
        isCompleted: Boolean
    ): Unit = withContext(Dispatchers.IO) {
        queries.updateUserOnboardingCompleted(
            onboardingCompleted = if (isCompleted) 1 else 0,
            id = userId
        )
    }

    private fun mapToUser(entity: com.ricardocosteira.rite.data.database.User): User {
        return User(
            id = entity.id,
            timezone = TimeZone.of(entity.timezone),
            previousTimezone = entity.previousTimezone?.let { TimeZone.of(it) },
            undoPolicy = UndoPolicy.valueOf(entity.undoPolicy),
            maxSnoozeDurationMinutes = entity.maxSnoozeDurationMinutes.toInt(),
            maxSnoozesPerHabitPerDay = entity.maxSnoozesPerHabitPerDay?.toInt(),
            maxConsecutiveSkips = entity.maxConsecutiveSkips?.toInt(),
            isOnboardingCompleted = entity.onboardingCompleted == 1L,
            dailySummaryTime = entity.dailySummaryTime?.let { LocalTime.parse(it) },
            createdAt = kotlin.time.Instant.parse(entity.createdAt)
        )
    }
}

internal expect fun generateUuid(): String
