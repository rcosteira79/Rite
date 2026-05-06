package com.ricardocosteira.rite.presentation.ui.habitdetail.models

import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.StrictnessPreset
import com.ricardocosteira.rite.domain.models.UndoPolicy

data class HabitDetailUiModel(
    val habitId: String,
    val instanceId: String,
    val name: String,
    val description: String?,
    val type: HabitType,
    val unit: String?,
    val defaultIncrement: Int,
    val status: HabitStatus,
    val currentProgress: Int,
    val targetValue: Int?,
    val completedValue: Int?,
    val progressPercentage: Float,
    val isQuantitativeComplete: Boolean,
    val currentStreak: Int,
    val longestStreak: Int,
    val habitScore: Int,
    val maxConsecutiveSkips: Int?,
    val currentConsecutiveSkips: Int,
    val strictnessPreset: StrictnessPreset?,
    val undoPolicy: UndoPolicy,
    val snoozesUsedToday: Int,
    val maxSnoozesPerDay: Int?,
    val skipsThisWeek: Int
) {
    val skipsRemaining: Int? = maxConsecutiveSkips?.let { max ->
        (max - currentConsecutiveSkips).coerceAtLeast(0)
    }

    val isSkipLocked: Boolean = maxConsecutiveSkips?.let { max ->
        currentConsecutiveSkips >= max
    } ?: false

    val isCompleted: Boolean = status == HabitStatus.COMPLETED

    val isSkipped: Boolean = status == HabitStatus.SKIPPED

    val isFailed: Boolean = status == HabitStatus.FAILED

    val isResolved: Boolean = isCompleted || isSkipped || isFailed
}
