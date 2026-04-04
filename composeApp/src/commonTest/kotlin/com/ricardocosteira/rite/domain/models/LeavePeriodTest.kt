package com.ricardocosteira.rite.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.datetime.LocalDate

class LeavePeriodTest {

    @Test
    fun `given end date before start date when creating then throws exception`() {
        // Given
        val inputStartDate = LocalDate(2026, 1, 20)
        val inputEndDate = LocalDate(2026, 1, 15)

        // When/Then
        try {
            LeavePeriod(
                id = "1",
                habitId = "habit1",
                startDate = inputStartDate,
                endDate = inputEndDate,
                reason = null,
                createdAt = Clock.System.now()
            )
            throw AssertionError("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("End date must be after or equal to start date", e.message)
        }
    }

    @Test
    fun `given end date equal to start date when creating then succeeds`() {
        // Given
        val inputDate = LocalDate(2026, 1, 20)

        // When
        val actualLeavePeriod = LeavePeriod(
            id = "1",
            habitId = "habit1",
            startDate = inputDate,
            endDate = inputDate,
            reason = null,
            createdAt = Clock.System.now()
        )

        // Then
        assertEquals(inputDate, actualLeavePeriod.startDate)
        assertEquals(inputDate, actualLeavePeriod.endDate)
    }

    @Test
    fun `given date before start when checking isActiveOn then returns false`() {
        // Given
        val inputLeavePeriod = LeavePeriod(
            id = "1",
            habitId = "habit1",
            startDate = LocalDate(2026, 1, 20),
            endDate = LocalDate(2026, 1, 25),
            reason = null,
            createdAt = Clock.System.now()
        )
        val inputDate = LocalDate(2026, 1, 19)

        // When
        val actualIsActive = inputLeavePeriod.isActiveOn(inputDate)

        // Then
        assertFalse(actualIsActive)
    }

    @Test
    fun `given date on start when checking isActiveOn then returns true`() {
        // Given
        val inputStartDate = LocalDate(2026, 1, 20)
        val inputLeavePeriod = LeavePeriod(
            id = "1",
            habitId = "habit1",
            startDate = inputStartDate,
            endDate = LocalDate(2026, 1, 25),
            reason = null,
            createdAt = Clock.System.now()
        )

        // When
        val actualIsActive = inputLeavePeriod.isActiveOn(inputStartDate)

        // Then
        assertTrue(actualIsActive)
    }

    @Test
    fun `given date between start and end when checking isActiveOn then returns true`() {
        // Given
        val inputLeavePeriod = LeavePeriod(
            id = "1",
            habitId = "habit1",
            startDate = LocalDate(2026, 1, 20),
            endDate = LocalDate(2026, 1, 25),
            reason = null,
            createdAt = Clock.System.now()
        )
        val inputDate = LocalDate(2026, 1, 22)

        // When
        val actualIsActive = inputLeavePeriod.isActiveOn(inputDate)

        // Then
        assertTrue(actualIsActive)
    }

    @Test
    fun `given date on end when checking isActiveOn then returns true`() {
        // Given
        val inputEndDate = LocalDate(2026, 1, 25)
        val inputLeavePeriod = LeavePeriod(
            id = "1",
            habitId = "habit1",
            startDate = LocalDate(2026, 1, 20),
            endDate = inputEndDate,
            reason = null,
            createdAt = Clock.System.now()
        )

        // When
        val actualIsActive = inputLeavePeriod.isActiveOn(inputEndDate)

        // Then
        assertTrue(actualIsActive)
    }

    @Test
    fun `given date after end when checking isActiveOn then returns false`() {
        // Given
        val inputLeavePeriod = LeavePeriod(
            id = "1",
            habitId = "habit1",
            startDate = LocalDate(2026, 1, 20),
            endDate = LocalDate(2026, 1, 25),
            reason = null,
            createdAt = Clock.System.now()
        )
        val inputDate = LocalDate(2026, 1, 26)

        // When
        val actualIsActive = inputLeavePeriod.isActiveOn(inputDate)

        // Then
        assertFalse(actualIsActive)
    }

    @Test
    fun `given indefinite leave period when checking isActiveOn then returns true for future dates`() {
        // Given
        val inputLeavePeriod = LeavePeriod(
            id = "1",
            habitId = "habit1",
            startDate = LocalDate(2026, 1, 20),
            endDate = null,
            reason = null,
            createdAt = Clock.System.now()
        )
        val inputDate = LocalDate(2026, 12, 31)

        // When
        val actualIsActive = inputLeavePeriod.isActiveOn(inputDate)

        // Then
        assertTrue(actualIsActive)
    }

    @Test
    fun `given leave period with end date when checking hasEnded then returns correct result`() {
        // Given
        val inputLeavePeriod = LeavePeriod(
            id = "1",
            habitId = "habit1",
            startDate = LocalDate(2026, 1, 20),
            endDate = LocalDate(2026, 1, 25),
            reason = null,
            createdAt = Clock.System.now()
        )

        // When/Then
        assertFalse(inputLeavePeriod.hasEnded(LocalDate(2026, 1, 24)))
        assertFalse(inputLeavePeriod.hasEnded(LocalDate(2026, 1, 25)))
        assertTrue(inputLeavePeriod.hasEnded(LocalDate(2026, 1, 26)))
    }

    @Test
    fun `given indefinite leave period when checking hasEnded then returns false`() {
        // Given
        val inputLeavePeriod = LeavePeriod(
            id = "1",
            habitId = "habit1",
            startDate = LocalDate(2026, 1, 20),
            endDate = null,
            reason = null,
            createdAt = Clock.System.now()
        )

        // When
        val actualHasEnded = inputLeavePeriod.hasEnded(LocalDate(2026, 12, 31))

        // Then
        assertFalse(actualHasEnded)
    }

    @Test
    fun `given leave period with end date when checking isIndefinite then returns false`() {
        // Given
        val inputLeavePeriod = LeavePeriod(
            id = "1",
            habitId = "habit1",
            startDate = LocalDate(2026, 1, 20),
            endDate = LocalDate(2026, 1, 25),
            reason = null,
            createdAt = Clock.System.now()
        )

        // When
        val actualIsIndefinite = inputLeavePeriod.isIndefinite

        // Then
        assertFalse(actualIsIndefinite)
    }

    @Test
    fun `given indefinite leave period when checking isIndefinite then returns true`() {
        // Given
        val inputLeavePeriod = LeavePeriod(
            id = "1",
            habitId = "habit1",
            startDate = LocalDate(2026, 1, 20),
            endDate = null,
            reason = null,
            createdAt = Clock.System.now()
        )

        // When
        val actualIsIndefinite = inputLeavePeriod.isIndefinite

        // Then
        assertTrue(actualIsIndefinite)
    }

    @Test
    fun `given leave period when calculating duration then returns correct days`() {
        // Given
        val inputLeavePeriod = LeavePeriod(
            id = "1",
            habitId = "habit1",
            startDate = LocalDate(2026, 1, 20),
            endDate = LocalDate(2026, 1, 25),
            reason = null,
            createdAt = Clock.System.now()
        )

        // When
        val actualDuration = inputLeavePeriod.durationInDays

        // Then
        assertEquals(6, actualDuration) // Inclusive: 20, 21, 22, 23, 24, 25
    }

    @Test
    fun `given indefinite leave period when calculating duration then returns null`() {
        // Given
        val inputLeavePeriod = LeavePeriod(
            id = "1",
            habitId = "habit1",
            startDate = LocalDate(2026, 1, 20),
            endDate = null,
            reason = null,
            createdAt = Clock.System.now()
        )

        // When
        val actualDuration = inputLeavePeriod.durationInDays

        // Then
        assertEquals(null, actualDuration)
    }

    @Test
    fun `given leave period when updating end date then returns new leave period`() {
        // Given
        val inputLeavePeriod = LeavePeriod(
            id = "1",
            habitId = "habit1",
            startDate = LocalDate(2026, 1, 20),
            endDate = LocalDate(2026, 1, 25),
            reason = null,
            createdAt = Clock.System.now()
        )
        val inputNewEndDate = LocalDate(2026, 1, 30)

        // When
        val actualLeavePeriod = inputLeavePeriod.withEndDate(inputNewEndDate)

        // Then
        assertEquals(inputNewEndDate, actualLeavePeriod.endDate)
        assertEquals(inputLeavePeriod.id, actualLeavePeriod.id)
        assertEquals(inputLeavePeriod.habitId, actualLeavePeriod.habitId)
        assertEquals(inputLeavePeriod.startDate, actualLeavePeriod.startDate)
    }

    @Test
    fun `given leave period when checking isCurrentlyActive then returns correct result`() {
        // Given
        val inputLeavePeriod = LeavePeriod(
            id = "1",
            habitId = "habit1",
            startDate = LocalDate(2026, 1, 20),
            endDate = LocalDate(2026, 1, 25),
            reason = null,
            createdAt = Clock.System.now()
        )

        // When/Then
        assertFalse(inputLeavePeriod.isCurrentlyActive(LocalDate(2026, 1, 19)))
        assertTrue(inputLeavePeriod.isCurrentlyActive(LocalDate(2026, 1, 20)))
        assertTrue(inputLeavePeriod.isCurrentlyActive(LocalDate(2026, 1, 22)))
        assertTrue(inputLeavePeriod.isCurrentlyActive(LocalDate(2026, 1, 25)))
        assertFalse(inputLeavePeriod.isCurrentlyActive(LocalDate(2026, 1, 26)))
    }
}
