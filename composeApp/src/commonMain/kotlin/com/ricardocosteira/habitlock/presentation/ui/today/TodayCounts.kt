package com.ricardocosteira.habitlock.presentation.ui.today

import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel

data class TodayCounts(
    val pendingCount: Int = 0,
    val dailyResolved: Int = 0,
    val dailyTotal: Int = 0,
)

private val resolvedStatuses: Set<HabitStatus> =
    setOf(
        HabitStatus.COMPLETED,
        HabitStatus.SKIPPED,
        HabitStatus.FAILED,
    )

fun List<TodayHabitUiModel>.computeCounts(): TodayCounts =
    TodayCounts(
        pendingCount = count { !it.isSuspended && it.isPending },
        dailyTotal = count { it.isDaily && !it.isSuspended },
        dailyResolved = count { it.isDaily && !it.isSuspended && it.status in resolvedStatuses },
    )
