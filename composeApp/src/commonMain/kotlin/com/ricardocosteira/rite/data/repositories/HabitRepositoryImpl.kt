package com.ricardocosteira.rite.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.ricardocosteira.rite.data.database.HabitLockDatabase
import com.ricardocosteira.rite.data.mappers.EntityMappers.toDomain
import com.ricardocosteira.rite.di.IoDispatcher
import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitReminder
import com.ricardocosteira.rite.domain.models.HabitSchedule
import com.ricardocosteira.rite.domain.repositories.HabitRepository
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class HabitRepositoryImpl(
    private val database: HabitLockDatabase,
    private val ioDispatcher: IoDispatcher
) : HabitRepository {
    private val queries = database.habitLockQueries

    override fun observeActiveHabits(): Flow<List<Habit>> = queries
        .getActiveHabits()
        .asFlow()
        .mapToList(ioDispatcher)
        .map { list -> list.map { it.toDomain() } }

    override fun observeArchivedHabits(): Flow<List<Habit>> = queries
        .getArchivedHabits()
        .asFlow()
        .mapToList(ioDispatcher)
        .map { list -> list.map { it.toDomain() } }

    override suspend fun getActiveHabits(): List<Habit> = withContext(ioDispatcher) {
        queries.getActiveHabits().executeAsList().map { it.toDomain() }
    }

    override suspend fun getHabitsWithTrackingEnabled(): List<Habit> = withContext(ioDispatcher) {
        queries.getHabitsWithTrackingEnabled().executeAsList().map { it.toDomain() }
    }

    override suspend fun getHabitById(habitId: String): Habit? = withContext(ioDispatcher) {
        queries.getHabitById(habitId).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun createHabit(
        habit: Habit,
        schedule: HabitSchedule,
        reminder: HabitReminder?
    ) {
        withContext(ioDispatcher) {
            database.transaction {
                queries.insertHabit(
                    id = habit.id,
                    name = habit.name,
                    description = habit.description,
                    type = habit.type.name,
                    targetValue = habit.targetValue?.toLong(),
                    unit = habit.unit,
                    defaultIncrement = habit.defaultIncrement.toLong(),
                    isTrackingEnabled = if (habit.isTrackingEnabled) 1L else 0L,
                    isActive = if (habit.isActive) 1 else 0,
                    isArchived = if (habit.isArchived) 1 else 0,
                    currentStreak = habit.currentStreak.toLong(),
                    longestStreak = habit.longestStreak.toLong(),
                    totalCompletions = habit.totalCompletions.toLong(),
                    expectedCompletions = habit.expectedCompletions.toLong(),
                    createdAt = habit.createdAt.toString(),
                    archivedAt = habit.archivedAt?.toString()
                )

                queries.insertSchedule(
                    id = schedule.id,
                    habitId = schedule.habitId,
                    scheduleType = schedule.scheduleType.name,
                    startDate = schedule.startDate.toString(),
                    endDate = schedule.endDate?.toString(),
                    quota = schedule.quota.toLong(),
                    weekStartDay = schedule.weekStartDay.name,
                    specificDays = schedule.specificDays?.joinToString(",") { it.name }
                )

                reminder?.let {
                    queries.insertReminder(
                        id = it.id,
                        habitId = it.habitId,
                        reminderType = it.reminderType.name,
                        time = it.time?.toString(),
                        intervalMinutes = it.intervalMinutes?.toLong(),
                        startTime = it.startTime?.toString(),
                        endTime = it.endTime?.toString(),
                        isActive = if (it.isActive) 1 else 0
                    )
                }
            }
        }
    }

    override suspend fun updateHabit(habit: Habit) {
        withContext(ioDispatcher) {
            queries.updateHabit(
                name = habit.name,
                description = habit.description,
                type = habit.type.name,
                targetValue = habit.targetValue?.toLong(),
                unit = habit.unit,
                isTrackingEnabled = if (habit.isTrackingEnabled) 1L else 0L,
                isActive = if (habit.isActive) 1 else 0,
                isArchived = if (habit.isArchived) 1 else 0,
                archivedAt = habit.archivedAt?.toString(),
                id = habit.id
            )
        }
    }

    override suspend fun updateHabitStreak(
        habitId: String,
        currentStreak: Int,
        longestStreak: Int
    ) {
        withContext(ioDispatcher) {
            queries.updateHabitStreak(
                currentStreak = currentStreak.toLong(),
                longestStreak = longestStreak.toLong(),
                id = habitId
            )
        }
    }

    override suspend fun updateHabitScore(
        habitId: String,
        totalCompletions: Int,
        expectedCompletions: Int
    ) {
        withContext(ioDispatcher) {
            queries.updateHabitScore(
                totalCompletions = totalCompletions.toLong(),
                expectedCompletions = expectedCompletions.toLong(),
                id = habitId
            )
        }
    }

    override suspend fun incrementHabitTotalCompletions(habitId: String, amount: Int) {
        withContext(ioDispatcher) {
            queries.incrementHabitTotalCompletions(
                totalCompletions = amount.toLong(),
                id = habitId
            )
        }
    }

    override suspend fun decrementHabitTotalCompletions(habitId: String, amount: Int) {
        withContext(ioDispatcher) {
            queries.decrementHabitTotalCompletions(
                totalCompletions = amount.toLong(),
                id = habitId,
                totalCompletions_ = amount.toLong()
            )
        }
    }

    override suspend fun incrementHabitExpectedCompletions(habitId: String, amount: Int) {
        withContext(ioDispatcher) {
            queries.incrementHabitExpectedCompletions(
                expectedCompletions = amount.toLong(),
                id = habitId
            )
        }
    }

    override suspend fun archiveHabit(habitId: String) {
        withContext(ioDispatcher) {
            queries.archiveHabit(
                archivedAt = Clock.System.now().toString(),
                id = habitId
            )
        }
    }

    override suspend fun unarchiveHabit(habitId: String) {
        withContext(ioDispatcher) {
            queries.unarchiveHabit(habitId)
        }
    }

    override suspend fun deleteHabit(habitId: String) {
        withContext(ioDispatcher) {
            queries.deleteHabit(habitId)
        }
    }

    override suspend fun getScheduleForHabit(habitId: String): HabitSchedule? =
        withContext(ioDispatcher) {
            queries.getScheduleForHabit(habitId).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun updateSchedule(schedule: HabitSchedule) {
        withContext(ioDispatcher) {
            queries.updateSchedule(
                scheduleType = schedule.scheduleType.name,
                startDate = schedule.startDate.toString(),
                endDate = schedule.endDate?.toString(),
                quota = schedule.quota.toLong(),
                weekStartDay = schedule.weekStartDay.name,
                specificDays = schedule.specificDays?.joinToString(",") { it.name },
                id = schedule.id
            )
        }
    }

    override suspend fun createScheduleForHabit(schedule: HabitSchedule) {
        withContext(ioDispatcher) {
            queries.insertSchedule(
                id = schedule.id,
                habitId = schedule.habitId,
                scheduleType = schedule.scheduleType.name,
                startDate = schedule.startDate.toString(),
                endDate = schedule.endDate?.toString(),
                quota = schedule.quota.toLong(),
                weekStartDay = schedule.weekStartDay.name,
                specificDays = schedule.specificDays?.joinToString(",") { it.name }
            )
        }
    }

    override suspend fun getRemindersForHabit(habitId: String): List<HabitReminder> =
        withContext(ioDispatcher) {
            queries.getRemindersForHabit(habitId).executeAsList().map { it.toDomain() }
        }

    override suspend fun updateReminder(reminder: HabitReminder) {
        withContext(ioDispatcher) {
            queries.updateReminder(
                reminderType = reminder.reminderType.name,
                time = reminder.time?.toString(),
                intervalMinutes = reminder.intervalMinutes?.toLong(),
                startTime = reminder.startTime?.toString(),
                endTime = reminder.endTime?.toString(),
                isActive = if (reminder.isActive) 1 else 0,
                id = reminder.id
            )
        }
    }

    override suspend fun deleteReminder(reminderId: String) {
        withContext(ioDispatcher) {
            queries.deleteReminder(reminderId)
        }
    }

    override suspend fun createReminderForHabit(reminder: HabitReminder) {
        withContext(ioDispatcher) {
            queries.insertReminder(
                id = reminder.id,
                habitId = reminder.habitId,
                reminderType = reminder.reminderType.name,
                time = reminder.time?.toString(),
                intervalMinutes = reminder.intervalMinutes?.toLong(),
                startTime = reminder.startTime?.toString(),
                endTime = reminder.endTime?.toString(),
                isActive = if (reminder.isActive) 1 else 0
            )
        }
    }
}
