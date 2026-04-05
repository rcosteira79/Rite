package com.ricardocosteira.rite.notifications

import kotlinx.datetime.LocalTime

/**
 * Pure computation of fire times for periodic reminders within a time window.
 * Shared across platforms (commonMain).
 */
object PeriodicReminderCalculator {

    fun computeFireTimes(
        startTime: LocalTime,
        endTime: LocalTime,
        intervalMinutes: Int,
    ): List<LocalTime> {
        val fireTimes: MutableList<LocalTime> = mutableListOf()
        var currentMinuteOfDay: Int = startTime.toSecondOfDay() / 60
        val endMinuteOfDay: Int = endTime.toSecondOfDay() / 60

        while (currentMinuteOfDay <= endMinuteOfDay) {
            fireTimes.add(LocalTime.fromSecondOfDay(currentMinuteOfDay * 60))
            currentMinuteOfDay += intervalMinutes
        }

        return fireTimes
    }

    fun filterFutureFireTimes(
        fireTimes: List<LocalTime>,
        currentTime: LocalTime,
    ): List<LocalTime> = fireTimes.filter { it > currentTime }

    fun computeSlotCount(startTime: LocalTime, endTime: LocalTime, intervalMinutes: Int,): Int {
        val startMinute: Int = startTime.toSecondOfDay() / 60
        val endMinute: Int = endTime.toSecondOfDay() / 60
        val windowMinutes: Int = endMinute - startMinute
        return (windowMinutes / intervalMinutes) + 1
    }
}
