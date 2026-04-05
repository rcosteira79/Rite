package com.ricardocosteira.rite.notifications

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalTime

class PeriodicReminderCalculatorTest {

    @Test
    fun `given 2 hour interval from 8am to 10pm when computing fire times then returns all slots`() {
        // Given
        val inputStartTime = LocalTime(8, 0)
        val inputEndTime = LocalTime(22, 0)
        val inputIntervalMinutes = 120

        // When
        val actualFireTimes: List<LocalTime> = PeriodicReminderCalculator.computeFireTimes(
            startTime = inputStartTime,
            endTime = inputEndTime,
            intervalMinutes = inputIntervalMinutes
        )

        // Then
        val expectedFireTimes: List<LocalTime> = listOf(
            LocalTime(8, 0),
            LocalTime(10, 0),
            LocalTime(12, 0),
            LocalTime(14, 0),
            LocalTime(16, 0),
            LocalTime(18, 0),
            LocalTime(20, 0),
            LocalTime(22, 0)
        )
        assertEquals(expectedFireTimes, actualFireTimes)
    }

    @Test
    fun `given 90 minute interval from 9am to 12pm when computing fire times then returns correct slots`() {
        val inputStartTime = LocalTime(9, 0)
        val inputEndTime = LocalTime(12, 0)
        val inputIntervalMinutes = 90

        val actualFireTimes: List<LocalTime> = PeriodicReminderCalculator.computeFireTimes(
            startTime = inputStartTime,
            endTime = inputEndTime,
            intervalMinutes = inputIntervalMinutes
        )

        val expectedFireTimes: List<LocalTime> =
            listOf(LocalTime(9, 0), LocalTime(10, 30), LocalTime(12, 0))
        assertEquals(expectedFireTimes, actualFireTimes)
    }

    @Test
    fun `given interval that does not evenly divide window when computing fire times then last slot before end is included`() {
        val inputStartTime = LocalTime(8, 0)
        val inputEndTime = LocalTime(11, 0)
        val inputIntervalMinutes = 120

        val actualFireTimes: List<LocalTime> = PeriodicReminderCalculator.computeFireTimes(
            startTime = inputStartTime,
            endTime = inputEndTime,
            intervalMinutes = inputIntervalMinutes
        )

        val expectedFireTimes: List<LocalTime> = listOf(LocalTime(8, 0), LocalTime(10, 0))
        assertEquals(expectedFireTimes, actualFireTimes)
    }

    @Test
    fun `given start time equals end time when computing fire times then returns single slot`() {
        val inputStartTime = LocalTime(9, 0)
        val inputEndTime = LocalTime(9, 0)
        val inputIntervalMinutes = 60

        val actualFireTimes: List<LocalTime> = PeriodicReminderCalculator.computeFireTimes(
            startTime = inputStartTime,
            endTime = inputEndTime,
            intervalMinutes = inputIntervalMinutes
        )

        val expectedFireTimes: List<LocalTime> = listOf(LocalTime(9, 0))
        assertEquals(expectedFireTimes, actualFireTimes)
    }

    @Test
    fun `given current time within window when filtering past times then skips past slots`() {
        val inputFireTimes: List<LocalTime> = listOf(
            LocalTime(8, 0),
            LocalTime(10, 0),
            LocalTime(12, 0),
            LocalTime(14, 0)
        )
        val inputCurrentTime = LocalTime(11, 0)

        val actualFutureTimes: List<LocalTime> = PeriodicReminderCalculator.filterFutureFireTimes(
            fireTimes = inputFireTimes,
            currentTime = inputCurrentTime
        )

        val expectedFutureTimes: List<LocalTime> = listOf(LocalTime(12, 0), LocalTime(14, 0))
        assertEquals(expectedFutureTimes, actualFutureTimes)
    }

    @Test
    fun `given current time before window when filtering past times then returns all slots`() {
        val inputFireTimes: List<LocalTime> =
            listOf(LocalTime(8, 0), LocalTime(10, 0), LocalTime(12, 0))
        val inputCurrentTime = LocalTime(7, 0)

        val actualFutureTimes: List<LocalTime> = PeriodicReminderCalculator.filterFutureFireTimes(
            fireTimes = inputFireTimes,
            currentTime = inputCurrentTime
        )

        assertEquals(inputFireTimes, actualFutureTimes)
    }

    @Test
    fun `given fire times when computing slot count then returns total number of fire times`() {
        val inputStartTime = LocalTime(8, 0)
        val inputEndTime = LocalTime(14, 0)
        val inputIntervalMinutes = 120

        val actualSlotCount: Int = PeriodicReminderCalculator.computeSlotCount(
            startTime = inputStartTime,
            endTime = inputEndTime,
            intervalMinutes = inputIntervalMinutes
        )

        assertEquals(4, actualSlotCount)
    }
}
