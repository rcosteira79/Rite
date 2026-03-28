package com.ricardocosteira.habitlock.presentation.models

import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitSchedule
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.ScheduleType
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * UI model for displaying a habit on the Today screen.
 */
data class TodayHabitUiModel(
    val instanceId: String,
    val habitId: String,
    val name: String,
    val description: String?,
    val type: HabitType,
    val status: HabitStatus,
    val completedValue: Int?,
    val targetValue: Int?,
    val unit: String?,
    val defaultIncrement: Int,
    val progressPercentage: Float,
    val isSkipLocked: Boolean,
    val currentStreak: Int,
    val longestStreak: Int,
    val scorePercentage: Int,
    val cadence: ScheduleType,
    val completedAt: Instant?,
    val completedAtText: String?,
) {
    val isCompleted: Boolean get() = status == HabitStatus.COMPLETED
    val isSkipped: Boolean get() = status == HabitStatus.SKIPPED
    val isFailed: Boolean get() = status == HabitStatus.FAILED
    val isPending: Boolean get() = status == HabitStatus.PENDING
    val isSuspended: Boolean get() = status == HabitStatus.SUSPENDED

    val progressText: String get() {
        return if (type == HabitType.QUANTITATIVE && targetValue != null) {
            "${completedValue ?: 0}/${targetValue}${unit?.let { " $it" } ?: ""}"
        } else {
            ""
        }
    }

    val scoreText: String get() = "$scorePercentage%"

    val isDaily: Boolean get() = cadence == ScheduleType.DAILY
    val isWeekly: Boolean get() = cadence == ScheduleType.WEEKLY
}

/**
 * Mapper from domain models to UI model.
 */
fun mapToTodayHabitUiModel(
    instance: HabitInstance,
    habit: Habit,
    schedule: HabitSchedule,
    maxConsecutiveSkips: Int?,
    userTimezone: TimeZone,
): TodayHabitUiModel {
    val score = habit.calculateScore()
    return TodayHabitUiModel(
        instanceId = instance.id,
        habitId = habit.id,
        name = habit.name,
        description = habit.description,
        type = habit.type,
        status = instance.status,
        completedValue = instance.completedValue,
        targetValue = instance.targetValue,
        unit = habit.unit,
        defaultIncrement = habit.defaultIncrement,
        progressPercentage = instance.progressPercentage(),
        isSkipLocked = instance.isSkipLocked(maxConsecutiveSkips),
        currentStreak = habit.currentStreak,
        longestStreak = habit.longestStreak,
        scorePercentage = score.percentage,
        cadence = schedule.scheduleType,
        completedAt = instance.completedAt,
        completedAtText = instance.completedAt?.let { formatCompletedAtTime(it, userTimezone) },
    )
}

private fun formatCompletedAtTime(
    instant: Instant,
    timezone: TimeZone,
): String {
    val localTime: LocalTime = instant.toLocalDateTime(timezone).time
    val hour: Int = if (localTime.hour % 12 == 0) 12 else localTime.hour % 12
    val minute: String = localTime.minute.toString().padStart(2, '0')
    val amPm: String = if (localTime.hour < 12) "AM" else "PM"
    return "$hour:$minute $amPm"
}
