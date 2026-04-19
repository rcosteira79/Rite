package com.ricardocosteira.rite.presentation.ui.habitdetail

import com.ricardocosteira.rite.presentation.ui.habitdetail.models.HabitDetailUiModel
import com.ricardocosteira.rite.presentation.ui.habitdetail.models.HeatmapDay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class HabitDetailState(
    val habit: HabitDetailUiModel? = null,
    val heatmapData: ImmutableList<HeatmapDay> = persistentListOf(),
    val isLoading: Boolean = true,
    val showCustomInput: Boolean = false
)
