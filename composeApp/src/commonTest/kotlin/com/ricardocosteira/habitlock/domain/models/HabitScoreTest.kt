package com.ricardocosteira.habitlock.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class HabitScoreTest {

    @Test
    fun `given zero completions when calculating percentage then returns 0`() {
        // Given
        val inputScore = HabitScore(
            totalCompletions = 0,
            expectedCompletions = 10
        )

        // When
        val actualPercentage = inputScore.percentage

        // Then
        assertEquals(0, actualPercentage)
    }

    @Test
    fun `given half completions when calculating percentage then returns 50`() {
        // Given
        val inputScore = HabitScore(
            totalCompletions = 5,
            expectedCompletions = 10
        )

        // When
        val actualPercentage = inputScore.percentage

        // Then
        assertEquals(50, actualPercentage)
    }

    @Test
    fun `given all completions when calculating percentage then returns 100`() {
        // Given
        val inputScore = HabitScore(
            totalCompletions = 10,
            expectedCompletions = 10
        )

        // When
        val actualPercentage = inputScore.percentage

        // Then
        assertEquals(100, actualPercentage)
        assertTrue(inputScore.isPerfect)
    }

    @Test
    fun `given over-completions when calculating percentage then caps at 150`() {
        // Given
        val inputScore = HabitScore(
            totalCompletions = 30,
            expectedCompletions = 10,
            overCompletionCap = 150
        )

        // When
        val actualPercentage = inputScore.percentage

        // Then
        assertEquals(150, actualPercentage)
        assertTrue(inputScore.isOverCompleted)
    }

    @Test
    fun `given excessive over-completions when calculating percentage then caps at limit`() {
        // Given
        val inputScore = HabitScore(
            totalCompletions = 100,
            expectedCompletions = 10,
            overCompletionCap = 150
        )

        // When
        val actualPercentage = inputScore.percentage

        // Then
        assertEquals(150, actualPercentage)
    }

    @Test
    fun `given zero expected when calculating percentage then returns 0`() {
        // Given
        val inputScore = HabitScore(
            totalCompletions = 5,
            expectedCompletions = 0
        )

        // When
        val actualPercentage = inputScore.percentage

        // Then
        assertEquals(0, actualPercentage)
    }

    @Test
    fun `given incomplete habit when checking isPerfect then returns false`() {
        // Given
        val inputScore = HabitScore(
            totalCompletions = 7,
            expectedCompletions = 10
        )

        // When
        val actualIsPerfect = inputScore.isPerfect

        // Then
        assertFalse(actualIsPerfect)
    }

    @Test
    fun `given over-completed habit when checking isOverCompleted then returns true`() {
        // Given
        val inputScore = HabitScore(
            totalCompletions = 12,
            expectedCompletions = 10
        )

        // When
        val actualIsOverCompleted = inputScore.isOverCompleted

        // Then
        assertTrue(actualIsOverCompleted)
    }

    @Test
    fun `given over-completions when getting overCompletionCount then returns correct count`() {
        // Given
        val inputScore = HabitScore(
            totalCompletions = 15,
            expectedCompletions = 10
        )

        // When
        val actualOverCompletionCount = inputScore.overCompletionCount

        // Then
        assertEquals(5, actualOverCompletionCount)
    }

    @Test
    fun `given missed completions when getting missedCompletionCount then returns correct count`() {
        // Given
        val inputScore = HabitScore(
            totalCompletions = 7,
            expectedCompletions = 10
        )

        // When
        val actualMissedCount = inputScore.missedCompletionCount

        // Then
        assertEquals(3, actualMissedCount)
    }

    @Test
    fun `given score when incrementing completion then returns updated score`() {
        // Given
        val inputScore = HabitScore(
            totalCompletions = 5,
            expectedCompletions = 10
        )

        // When
        val actualScore = inputScore.withIncrementedCompletion(2)

        // Then
        assertEquals(7, actualScore.totalCompletions)
        assertEquals(10, actualScore.expectedCompletions)
    }

    @Test
    fun `given score when incrementing expected then returns updated score`() {
        // Given
        val inputScore = HabitScore(
            totalCompletions = 5,
            expectedCompletions = 10
        )

        // When
        val actualScore = inputScore.withIncrementedExpected(3)

        // Then
        assertEquals(5, actualScore.totalCompletions)
        assertEquals(13, actualScore.expectedCompletions)
    }

    @Test
    fun `given score when decrementing completion then returns updated score`() {
        // Given
        val inputScore = HabitScore(
            totalCompletions = 5,
            expectedCompletions = 10
        )

        // When
        val actualScore = inputScore.withDecrementedCompletion(2)

        // Then
        assertEquals(3, actualScore.totalCompletions)
        assertEquals(10, actualScore.expectedCompletions)
    }

    @Test
    fun `given score when decrementing below zero then coerces to zero`() {
        // Given
        val inputScore = HabitScore(
            totalCompletions = 2,
            expectedCompletions = 10
        )

        // When
        val actualScore = inputScore.withDecrementedCompletion(5)

        // Then
        assertEquals(0, actualScore.totalCompletions)
    }

    @Test
    fun `given initial when creating then returns zero score`() {
        // When
        val actualScore = HabitScore.initial()

        // Then
        assertEquals(0, actualScore.totalCompletions)
        assertEquals(0, actualScore.expectedCompletions)
        assertEquals(HabitScore.DEFAULT_OVER_COMPLETION_CAP, actualScore.overCompletionCap)
    }

    @Test
    fun `given percentageFloat when calculated then returns correct float`() {
        // Given
        val inputScore = HabitScore(
            totalCompletions = 7,
            expectedCompletions = 10
        )

        // When
        val actualPercentageFloat = inputScore.percentageFloat

        // Then
        assertEquals(0.7f, actualPercentageFloat)
    }
}
