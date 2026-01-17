package com.ricardocosteira.habitlock.presentation.ui.calendar

import com.ricardocosteira.habitlock.presentation.models.CalendarDayUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

/**
 * State for the Calendar screen.
 */
data class CalendarState(
    val currentMonth: Month = Month.JANUARY,
    val currentYear: Int = 2026,
    val days: ImmutableList<CalendarDayUiModel> = persistentListOf(),
    val isLoading: Boolean = true,
    val selectedDay: LocalDate? = null,
    val perfectDaysCount: Int = 0,
    val totalDaysTracked: Int = 0
)
