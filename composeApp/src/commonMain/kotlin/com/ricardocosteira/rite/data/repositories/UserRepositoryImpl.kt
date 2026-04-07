package com.ricardocosteira.rite.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.ricardocosteira.rite.data.database.RiteDatabase
import com.ricardocosteira.rite.di.IoDispatcher
import com.ricardocosteira.rite.domain.models.UndoPolicy
import com.ricardocosteira.rite.domain.models.User
import com.ricardocosteira.rite.domain.repositories.UserRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import me.tatarka.inject.annotations.Inject

@Inject
class UserRepositoryImpl(
    private val database: RiteDatabase,
    private val ioDispatcher: IoDispatcher
) : UserRepository {

    private val queries = database.riteQueries

    override fun observeUser(): Flow<User?> = queries.getUser()
        .asFlow()
        .mapToOneOrNull(ioDispatcher)
        .map { userEntity: com.ricardocosteira.rite.data.database.User? ->
            userEntity?.let { mapToUser(it) }
        }

    override suspend fun getUser(): User? = withContext(ioDispatcher) {
        queries.getUser()
            .executeAsOneOrNull()
            ?.let { mapToUser(it) }
    }

    override suspend fun createDefaultUser(timezone: TimeZone): User = withContext(ioDispatcher) {
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

    override suspend fun updateUser(user: User): Unit = withContext(ioDispatcher) {
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
    ): Unit = withContext(ioDispatcher) {
        queries.updateUserTimezone(
            timezone = newTimezone.id,
            previousTimezone = previousTimezone.id,
            id = userId
        )
    }

    override suspend fun setOnboardingCompleted(userId: String, isCompleted: Boolean): Unit =
        withContext(ioDispatcher) {
            queries.updateUserOnboardingCompleted(
                onboardingCompleted = if (isCompleted) 1 else 0,
                id = userId
            )
        }

    private fun mapToUser(entity: com.ricardocosteira.rite.data.database.User): User = User(
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

internal expect fun generateUuid(): String
