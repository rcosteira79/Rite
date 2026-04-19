package com.ricardocosteira.rite.presentation.ui.habitdetail.models

import com.ricardocosteira.rite.domain.models.HabitStatus

data class HeatmapDay(val date: String, val completionPercentage: Float, val status: HabitStatus)
