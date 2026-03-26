package com.ricardocosteira.habitlock.data.mappers

import com.ricardocosteira.habitlock.domain.models.CompletionSource
import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitCompletionEvent
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitReminder
import com.ricardocosteira.habitlock.domain.models.HabitSchedule
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.LeavePeriod
import com.ricardocosteira.habitlock.domain.models.ReminderType
import com.ricardocosteira.habitlock.domain.models.ScheduleType
import com.ricardocosteira.habitlock.domain.models.SnoozeState
import com.ricardocosteira.habitlock.domain.models.UndoPolicy
import com.ricardocosteira.habitlock.domain.models.User
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.time.Instant
import com.ricardocosteira.habitlock.data.database.Habit as DbHabit
import com.ricardocosteira.habitlock.data.database.HabitCompletionEvent as DbHabitCompletionEvent
import com.ricardocosteira.habitlock.data.database.HabitInstance as DbHabitInstance
import com.ricardocosteira.habitlock.data.database.HabitReminder as DbHabitReminder
import com.ricardocosteira.habitlock.data.database.HabitSchedule as DbHabitSchedule
import com.ricardocosteira.habitlock.data.database.LeavePeriod as DbLeavePeriod
import com.ricardocosteira.habitlock.data.database.SnoozeState as DbSnoozeState
import com.ricardocosteira.habitlock.data.database.User as DbUser

/**
 * Mappers between SQLDelight entities and domain models.
 */
object EntityMappers {
    // User mappers
    fun DbUser.toDomain(): User =
        User(
            id = id,
            timezone = TimeZone.of(timezone),
            previousTimezone = previousTimezone?.let { TimeZone.of(it) },
            undoPolicy = UndoPolicy.valueOf(undoPolicy),
            maxSnoozeDurationMinutes = maxSnoozeDurationMinutes.toInt(),
            maxSnoozesPerHabitPerDay = maxSnoozesPerHabitPerDay?.toInt(),
            maxConsecutiveSkips = maxConsecutiveSkips?.toInt(),
            isOnboardingCompleted = onboardingCompleted == 1L,
            dailySummaryTime = dailySummaryTime?.let { LocalTime.parse(it) },
            createdAt = Instant.parse(createdAt),
        )

    // Habit mappers
    fun DbHabit.toDomain(): Habit =
        Habit(
            id = id,
            name = name,
            description = description,
            type = HabitType.valueOf(type),
            targetValue = targetValue?.toInt(),
            unit = unit,
            isActive = isActive == 1L,
            isArchived = isArchived == 1L,
            currentStreak = currentStreak.toInt(),
            longestStreak = longestStreak.toInt(),
            totalCompletions = totalCompletions.toInt(),
            expectedCompletions = expectedCompletions.toInt(),
            createdAt = Instant.parse(createdAt),
            archivedAt = archivedAt?.let { Instant.parse(it) },
        )

    // HabitSchedule mappers
    fun DbHabitSchedule.toDomain(): HabitSchedule =
        HabitSchedule(
            id = id,
            habitId = habitId,
            scheduleType = ScheduleType.valueOf(scheduleType),
            startDate = LocalDate.parse(startDate),
            endDate = endDate?.let { LocalDate.parse(it) },
            quota = quota.toInt(),
            weekStartDay = DayOfWeek.valueOf(weekStartDay),
            specificDays =
                specificDays
                    ?.split(",")
                    ?.filter { it.isNotBlank() }
                    ?.map { DayOfWeek.valueOf(it.trim()) }
                    ?.toSet(),
        )

    // HabitReminder mappers
    fun DbHabitReminder.toDomain(): HabitReminder =
        HabitReminder(
            id = id,
            habitId = habitId,
            reminderType = ReminderType.valueOf(reminderType),
            time = time?.let { LocalTime.parse(it) },
            intervalMinutes = intervalMinutes?.toInt(),
            startTime = startTime?.let { LocalTime.parse(it) },
            endTime = endTime?.let { LocalTime.parse(it) },
            isActive = isActive == 1L,
        )

    // HabitInstance mappers
    fun DbHabitInstance.toDomain(): HabitInstance =
        HabitInstance(
            id = id,
            habitId = habitId,
            date = LocalDate.parse(date),
            status = HabitStatus.valueOf(status),
            completedValue = completedValue?.toInt(),
            targetValue = targetValue?.toInt(),
            consecutiveSkipsAtCreation = consecutiveSkipsAtCreation.toInt(),
            createdAt = Instant.parse(createdAt),
            completedAt = completedAt?.let { Instant.parse(it) },
        )

    // HabitCompletionEvent mappers
    fun DbHabitCompletionEvent.toDomain(): HabitCompletionEvent =
        HabitCompletionEvent(
            id = id,
            habitInstanceId = habitInstanceId,
            timestamp = Instant.parse(timestamp),
            deltaValue = deltaValue.toInt(),
            source = CompletionSource.valueOf(source),
        )

    // SnoozeState mappers
    fun DbSnoozeState.toDomain(): SnoozeState =
        SnoozeState(
            habitInstanceId = habitInstanceId,
            scheduledTime = Instant.parse(scheduledTime),
            snoozeCount = snoozeCount.toInt(),
        )

    // LeavePeriod mappers
    fun DbLeavePeriod.toDomain(): LeavePeriod =
        LeavePeriod(
            id = id,
            habitId = habitId,
            startDate = LocalDate.parse(startDate),
            endDate = endDate?.let { LocalDate.parse(it) },
            reason = reason,
            createdAt = Instant.parse(createdAt),
        )
}
