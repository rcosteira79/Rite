package com.ricardocosteira.rite.presentation.ui.habitdetail

import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitStatus
import kotlinx.datetime.LocalDate

data class HabitDetailState(
    val habit: Habit? = null,
    val instance: HabitInstance? = null,
    val maxConsecutiveSkips: Int? = null,
    val currentConsecutiveSkips: Int = 0,
    val heatmapData: List<HeatmapDay> = emptyList(),
    val isLoading: Boolean = true,
    val showCustomInput: Boolean = false
) {
    val habitScore: Int
        get() = habit?.calculateScore()?.percentage ?: 0

    val skipsRemaining: Int?
        get() {
            val max: Int = maxConsecutiveSkips ?: return null
            return (max - currentConsecutiveSkips).coerceAtLeast(0)
        }

    val isSkipLocked: Boolean
        get() {
            val max: Int = maxConsecutiveSkips ?: return false
            return currentConsecutiveSkips >= max
        }

    val isCompleted: Boolean
        get() {
            val inst: HabitInstance = instance ?: return false
            return inst.status == HabitStatus.COMPLETED
        }

    val isSkipped: Boolean
        get() {
            val inst: HabitInstance = instance ?: return false
            return inst.status == HabitStatus.SKIPPED
        }

    val isFailed: Boolean
        get() {
            val inst: HabitInstance = instance ?: return false
            return inst.status == HabitStatus.FAILED
        }

    val isResolved: Boolean
        get() = isCompleted || isSkipped || isFailed

    val isQuantitativeComplete: Boolean
        get() = instance?.isQuantitativeComplete() ?: false
}

data class HeatmapDay(val date: LocalDate, val completionPercentage: Float, val status: HabitStatus)
