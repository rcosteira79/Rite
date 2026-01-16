package com.ricardocosteira.habitlock.domain.usecases

import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitReminder
import com.ricardocosteira.habitlock.domain.models.HabitSchedule
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.ScheduleType
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository
import kotlin.time.Clock
import kotlinx.datetime.LocalDate

/**
 * Creates a new habit with schedule and optional reminder.
 */
class CreateHabitUseCase(
    private val habitRepository: HabitRepository,
    private val uuidProvider: UuidProvider
) {

    data class CreateHabitParams(
        val name: String,
        val description: String?,
        val type: HabitType,
        val targetValue: Int?,
        val unit: String?,
        val reminder: HabitReminder?
    )

    suspend fun execute(params: CreateHabitParams, startDate: LocalDate): Result<Habit> {
        if (params.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Habit name cannot be empty"))
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
            isActive = true,
            isArchived = false,
            currentStreak = 0,
            longestStreak = 0,
            createdAt = now,
            archivedAt = null
        )
        
        val schedule = HabitSchedule(
            id = uuidProvider.generate(),
            habitId = habitId,
            scheduleType = ScheduleType.DAILY,
            startDate = startDate,
            endDate = null
        )
        
        val reminder = params.reminder?.copy(
            id = uuidProvider.generate(),
            habitId = habitId
        )
        
        habitRepository.createHabit(habit, schedule, reminder)
        
        return Result.success(habit)
    }
}

