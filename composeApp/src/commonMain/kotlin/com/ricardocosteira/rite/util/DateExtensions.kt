package com.ricardocosteira.rite.util

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Converts this [Instant] to a [LocalDate] in the given [timezone].
 */
internal fun Instant.toLocalDate(timezone: TimeZone): LocalDate = toLocalDateTime(timezone).date

/**
 * Returns today's date in the given [timezone].
 */
internal fun Clock.System.todayIn(timezone: TimeZone): LocalDate = now().toLocalDate(timezone)

/**
 * Returns the three-letter English abbreviation for this [Month].
 */
internal fun formatMonthAbbreviation(month: Month): String = when (month) {
    Month.JANUARY -> "Jan"
    Month.FEBRUARY -> "Feb"
    Month.MARCH -> "Mar"
    Month.APRIL -> "Apr"
    Month.MAY -> "May"
    Month.JUNE -> "Jun"
    Month.JULY -> "Jul"
    Month.AUGUST -> "Aug"
    Month.SEPTEMBER -> "Sep"
    Month.OCTOBER -> "Oct"
    Month.NOVEMBER -> "Nov"
    Month.DECEMBER -> "Dec"
}
