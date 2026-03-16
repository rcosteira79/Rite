package com.ricardocosteira.habitlock.presentation.ui.today

import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel

data class TodayCounts(
    val pendingCount: Int = 0,
    val dailyCompleted: Int = 0,
    val dailyTotal: Int = 0,
    val weeklyCompleted: Int = 0,
    val weeklyTotal: Int = 0
)

fun List<TodayHabitUiModel>.computeCounts(): TodayCounts {
    val resolvedStatuses = setOf(HabitStatus.COMPLETED, HabitStatus.SKIPPED, HabitStatus.FAILED)
    return TodayCounts(
        pendingCount = count { !it.isSuspended && it.isPending },
        dailyTotal = count { it.isDaily && !it.isSuspended },
        dailyCompleted = count { it.isDaily && !it.isSuspended && it.status in resolvedStatuses },
        weeklyTotal = count { it.isWeekly && !it.isSuspended },
        weeklyCompleted = count { it.isWeekly && !it.isSuspended && it.status in resolvedStatuses }
    )
}
