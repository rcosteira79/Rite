package com.ricardocosteira.habitlock.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.ricardocosteira.habitlock.data.database.HabitLockDatabase
import com.ricardocosteira.habitlock.data.mappers.EntityMappers.toDomain
import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitReminder
import com.ricardocosteira.habitlock.domain.models.HabitSchedule
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class HabitRepositoryImpl(
    private val database: HabitLockDatabase
) : HabitRepository {

    private val queries = database.habitLockQueries

    override fun observeActiveHabits(): Flow<List<Habit>> {
        return queries.getActiveHabits()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun observeArchivedHabits(): Flow<List<Habit>> {
        return queries.getArchivedHabits()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getActiveHabits(): List<Habit> = withContext(Dispatchers.IO) {
        queries.getActiveHabits().executeAsList().map { it.toDomain() }
    }

    override suspend fun getHabitById(habitId: String): Habit? = withContext(Dispatchers.IO) {
        queries.getHabitById(habitId).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun createHabit(
        habit: Habit,
        schedule: HabitSchedule,
        reminder: HabitReminder?
    ): Unit = withContext(Dispatchers.IO) {
        database.transaction {
            queries.insertHabit(
                id = habit.id,
                name = habit.name,
                description = habit.description,
                type = habit.type.name,
                targetValue = habit.targetValue?.toLong(),
                unit = habit.unit,
                isActive = if (habit.isActive) 1 else 0,
                isArchived = if (habit.isArchived) 1 else 0,
                currentStreak = habit.currentStreak.toLong(),
                longestStreak = habit.longestStreak.toLong(),
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

    override suspend fun updateHabit(habit: Habit): Unit = withContext(Dispatchers.IO) {
        queries.updateHabit(
            name = habit.name,
            description = habit.description,
            type = habit.type.name,
            targetValue = habit.targetValue?.toLong(),
            unit = habit.unit,
            isActive = if (habit.isActive) 1 else 0,
            isArchived = if (habit.isArchived) 1 else 0,
            archivedAt = habit.archivedAt?.toString(),
            id = habit.id
        )
    }

    override suspend fun updateHabitStreak(
        habitId: String,
        currentStreak: Int,
        longestStreak: Int
    ): Unit = withContext(Dispatchers.IO) {
        queries.updateHabitStreak(
            currentStreak = currentStreak.toLong(),
            longestStreak = longestStreak.toLong(),
            id = habitId
        )
    }

    override suspend fun archiveHabit(habitId: String): Unit = withContext(Dispatchers.IO) {
        queries.archiveHabit(
            archivedAt = Clock.System.now().toString(),
            id = habitId
        )
    }

    override suspend fun unarchiveHabit(habitId: String): Unit = withContext(Dispatchers.IO) {
        queries.unarchiveHabit(habitId)
    }

    override suspend fun deleteHabit(habitId: String): Unit = withContext(Dispatchers.IO) {
        queries.deleteHabit(habitId)
    }

    override suspend fun getScheduleForHabit(habitId: String): HabitSchedule? = 
        withContext(Dispatchers.IO) {
            queries.getScheduleForHabit(habitId).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun getRemindersForHabit(habitId: String): List<HabitReminder> =
        withContext(Dispatchers.IO) {
            queries.getRemindersForHabit(habitId).executeAsList().map { it.toDomain() }
        }

    override suspend fun updateReminder(reminder: HabitReminder): Unit = 
        withContext(Dispatchers.IO) {
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

    override suspend fun deleteReminder(reminderId: String): Unit = withContext(Dispatchers.IO) {
        queries.deleteReminder(reminderId)
    }
}
