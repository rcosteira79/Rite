package com.ricardocosteira.habitlock.domain.models

import kotlinx.datetime.LocalDate

private val TITLES: List<String> =
    listOf(
        "Quiet discipline",
        "Small steps, big change",
        "Trust the process",
        "One day at a time",
        "Show up for yourself",
        "Progress, not perfection",
        "Build the habit, build the life",
        "Consistency compounds",
        "The work is the reward",
        "Stay the course",
        "Begin again, always",
        "Discipline is freedom",
        "Earn your rest",
        "Action over intention",
        "Make it count",
    )

fun motivationalTitleForDate(date: LocalDate): String {
    val seed: Long = date.toEpochDays()
    val size: Long = TITLES.size.toLong()
    val index: Int = ((seed % size + size) % size).toInt()
    return TITLES[index]
}
