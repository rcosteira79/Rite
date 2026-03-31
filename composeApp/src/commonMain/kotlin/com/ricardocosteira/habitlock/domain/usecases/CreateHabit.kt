package com.ricardocosteira.habitlock.domain.usecases

import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitReminder
import com.ricardocosteira.habitlock.domain.models.HabitSchedule
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.ScheduleType
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository
import kotlin.time.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import me.tatarka.inject.annotations.Inject

/**
 * Creates a new habit with schedule and optional reminder.
 */
@Inject
class CreateHabit(
    private val habitRepository: HabitRepository,
    private val uuidProvider: UuidProvider
) {
    data class CreateHabitParams(
        val name: String,
        val description: String?,
        val type: HabitType,
        val targetValue: Int?,
        val unit: String?,
        val defaultIncrement: Int = 1,
        val scheduleType: ScheduleType = ScheduleType.DAILY,
        val quota: Int = 1,
        val weekStartDay: DayOfWeek = DayOfWeek.MONDAY,
        val specificDays: Set<DayOfWeek>? = null,
        val reminder: HabitReminder?
    )

    suspend fun execute(params: CreateHabitParams, startDate: LocalDate): Result<Habit> {
        if (params.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Habit name cannot be empty"))
        }

        if (params.quota <= 0) {
            return Result.failure(IllegalArgumentException("Quota must be greater than 0"))
        }

        if (params.scheduleType == ScheduleType.WEEKLY && params.specificDays != null &&
            params.specificDays.isEmpty()
        ) {
            return Result.failure(
                IllegalArgumentException("Specific days cannot be empty for weekly schedules")
            )
        }

        val habitId = uuidProvider.generate()
        val now = Clock.System.now()

        val habit = Habit(
            id = habitId,
            name = params.name.trim(),
            description = params.description?.trim(),
            type = params.type,
            targetValue = params.targetValue,
            unit = params.unit?.trim(),
            defaultIncrement = params.defaultIncrement,
            isActive = true,
            isArchived = false,
            currentStreak = 0,
            longestStreak = 0,
            totalCompletions = 0,
            expectedCompletions = 0,
            createdAt = now,
            archivedAt = null
        )

        val schedule = HabitSchedule(
            id = uuidProvider.generate(),
            habitId = habitId,
            scheduleType = params.scheduleType,
            startDate = startDate,
            endDate = null,
            quota = params.quota,
            weekStartDay = params.weekStartDay,
            specificDays = params.specificDays
        )

        val reminder = params.reminder?.copy(
            id = uuidProvider.generate(),
            habitId = habitId
        )

        habitRepository.createHabit(habit, schedule, reminder)

        return Result.success(habit)
    }
}
