package com.ricardocosteira.rite.domain.models

import kotlinx.datetime.LocalDate

const val MOTIVATIONAL_TITLE_COUNT: Int = 14

fun motivationalTitleIndexForDate(date: LocalDate): Int {
    val seed: Long = date.toEpochDays()
    val size: Long = MOTIVATIONAL_TITLE_COUNT.toLong()
    return ((seed % size + size) % size).toInt()
}
