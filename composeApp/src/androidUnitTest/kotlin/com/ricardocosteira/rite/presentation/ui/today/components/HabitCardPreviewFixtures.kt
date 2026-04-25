package com.ricardocosteira.rite.presentation.ui.today.components

import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.ScheduleType
import com.ricardocosteira.rite.presentation.models.TodayHabitUiModel

internal fun binaryDaily(
    status: HabitStatus = HabitStatus.PENDING,
    streak: Int = 14,
    description: String? = "12 min",
    completedAtText: String? = null,
    skipLocked: Boolean = false
): TodayHabitUiModel = TodayHabitUiModel(
    instanceId = "inst-${status.name}",
    habitId = "habit-1",
    name = "Morning sit",
    description = description,
    type = HabitType.BINARY,
    status = status,
    completedValue = null,
    targetValue = null,
    unit = null,
    defaultIncrement = 1,
    progressPercentage = if (status == HabitStatus.COMPLETED) 1f else 0f,
    isSkipLocked = skipLocked,
    currentStreak = streak,
    longestStreak = 42,
    scorePercentage = 86,
    cadence = ScheduleType.DAILY,
    completedAtText = completedAtText
)

internal fun quantDaily(
    status: HabitStatus = HabitStatus.PENDING,
    cur: Int = 12,
    target: Int = 30,
    unit: String? = "pg",
    streak: Int = 9,
    completedAtText: String? = null
): TodayHabitUiModel = TodayHabitUiModel(
    instanceId = "inst-q-${status.name}",
    habitId = "habit-2",
    name = "Read before sleep",
    description = null,
    type = HabitType.QUANTITATIVE,
    status = status,
    completedValue = cur,
    targetValue = target,
    unit = unit,
    defaultIncrement = 5,
    progressPercentage = cur.toFloat() / target.toFloat(),
    isSkipLocked = false,
    currentStreak = streak,
    longestStreak = 21,
    scorePercentage = 76,
    cadence = ScheduleType.DAILY,
    completedAtText = completedAtText
)

internal fun flexibleWeeklyQuant(
    status: HabitStatus = HabitStatus.PENDING,
    cur: Int = 15,
    target: Int = 30,
    unit: String? = "km",
    streak: Int = 4
): TodayHabitUiModel = TodayHabitUiModel(
    instanceId = "inst-flex-${status.name}",
    habitId = "habit-3",
    name = "Long run",
    description = null,
    type = HabitType.QUANTITATIVE,
    status = status,
    completedValue = cur,
    targetValue = target,
    unit = unit,
    defaultIncrement = 5,
    progressPercentage = cur.toFloat() / target.toFloat(),
    isSkipLocked = false,
    currentStreak = streak,
    longestStreak = 8,
    scorePercentage = 64,
    cadence = ScheduleType.FLEXIBLE_WEEKLY,
    completedAtText = null
)

internal fun flexibleWeeklyBinary(
    status: HabitStatus = HabitStatus.PENDING,
    streak: Int = 2,
    completedAtText: String? = null
): TodayHabitUiModel = TodayHabitUiModel(
    instanceId = "inst-flex-bin-${status.name}",
    habitId = "habit-4",
    name = "Call mum",
    description = "3× per week",
    type = HabitType.BINARY,
    status = status,
    completedValue = null,
    targetValue = null,
    unit = null,
    defaultIncrement = 1,
    progressPercentage = if (status == HabitStatus.COMPLETED) 1f else 0f,
    isSkipLocked = false,
    currentStreak = streak,
    longestStreak = 6,
    scorePercentage = 52,
    cadence = ScheduleType.FLEXIBLE_WEEKLY,
    completedAtText = completedAtText
)
