package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

fun isoWeekNumber(date: LocalDate): Int {
    val daysToThursday: Int = DayOfWeek.THURSDAY.ordinal - date.dayOfWeek.ordinal
    val thursday: LocalDate = when {
        daysToThursday == 0 -> date
        daysToThursday > 0 -> date.plus(DatePeriod(days = daysToThursday))
        else -> date.minus(DatePeriod(days = -daysToThursday))
    }
    val yearStart = LocalDate(thursday.year, 1, 1)
    val firstThursdayOffset: Int =
        ((DayOfWeek.THURSDAY.ordinal - yearStart.dayOfWeek.ordinal) + 7) % 7
    val firstThursday: LocalDate = yearStart.plus(DatePeriod(days = firstThursdayOffset))
    val daysBetween: Int = thursday.toEpochDays().toInt() - firstThursday.toEpochDays().toInt()
    return (daysBetween / 7) + 1
}

fun formatWeekRange(from: LocalDate, to: LocalDate): String {
    val startWeek: Int = isoWeekNumber(from)
    val endWeek: Int = isoWeekNumber(to)
    fun pad(n: Int): String = n.toString().padStart(2, '0')
    return "W${pad(startWeek)} — W${pad(endWeek)}"
}
