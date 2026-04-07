package com.ricardocosteira.rite.presentation.ui.today

import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.presentation.models.TodayHabitUiModel

data class TodayCounts(
    val pendingCount: Int = 0,
    val dailyProgressDisplay: Int = 0,
    val dailyProgressExact: Float = 0f,
    val dailyTotal: Int = 0
)

private val resolvedStatuses: Set<HabitStatus> = setOf(
    HabitStatus.COMPLETED,
    HabitStatus.SKIPPED,
    HabitStatus.FAILED
)

/**
 * Computes daily progress counting each resolved habit as 100% and each
 * pending quantitative habit at its current [TodayHabitUiModel.progressPercentage].
 * The result is floored so partial progress only bumps the counter once
 * a full "unit" of progress is reached (e.g. 1.8 → 1).
 */
fun List<TodayHabitUiModel>.computeCounts(): TodayCounts {
    val daily: List<TodayHabitUiModel> = filter {
        (it.isDaily || it.isFixedWeekly) &&
            !it.isSuspended
    }
    val progress: Double = daily.sumOf { habit ->
        when {
            habit.status in resolvedStatuses -> 1.0
            else -> habit.progressPercentage.toDouble()
        }
    }

    return TodayCounts(
        pendingCount = count { !it.isSuspended && it.isPending },
        dailyTotal = daily.size,
        dailyProgressDisplay = progress.toInt(),
        dailyProgressExact = progress.toFloat()
    )
}
