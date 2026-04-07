package com.ricardocosteira.rite.presentation.ui.habitdetail

import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

data class HabitDetailState(
    val habit: Habit? = null,
    val instance: HabitInstance? = null,
    val maxConsecutiveSkips: Int? = null,
    val currentConsecutiveSkips: Int = 0,
    val heatmapData: ImmutableList<HeatmapDay> = persistentListOf(),
    val isLoading: Boolean = true,
    val showCustomInput: Boolean = false
) {
    val habitScore: Int = habit?.calculateScore()?.percentage ?: 0

    val skipsRemaining: Int? = maxConsecutiveSkips?.let { max ->
        (max - currentConsecutiveSkips).coerceAtLeast(0)
    }

    val isSkipLocked: Boolean = maxConsecutiveSkips?.let { max ->
        currentConsecutiveSkips >= max
    } ?: false

    val isCompleted: Boolean = instance?.status == HabitStatus.COMPLETED

    val isSkipped: Boolean = instance?.status == HabitStatus.SKIPPED

    val isFailed: Boolean = instance?.status == HabitStatus.FAILED

    val isResolved: Boolean = isCompleted || isSkipped || isFailed

    val isQuantitativeComplete: Boolean = instance?.isQuantitativeComplete() ?: false
}

data class HeatmapDay(val date: LocalDate, val completionPercentage: Float, val status: HabitStatus)
