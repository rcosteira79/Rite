package com.ricardocosteira.habitlock.util

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Converts this [Instant] to a [LocalDate] in the given [timezone].
 */
internal fun Instant.toLocalDate(timezone: TimeZone): LocalDate =
    toLocalDateTime(timezone).date

/**
 * Returns today's date in the given [timezone].
 */
internal fun Clock.System.todayIn(timezone: TimeZone): LocalDate =
    now().toLocalDate(timezone)

