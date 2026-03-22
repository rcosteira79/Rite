package com.ricardocosteira.habitlock.domain.usecases

import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitScore
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlinx.coroutines.test.runTest

class CalculateHabitScoreTest {

    private val mockHabitRepository = mockk<HabitRepository>()
    private val useCase = CalculateHabitScore(mockHabitRepository)

    @Test
    fun `given habit exists when calculating score then returns correct score`() = runTest {
        // Given
        val inputHabitId = "habit-1"
        val mockHabit = Habit(
            id = inputHabitId,
            name = "Test Habit",
            description = null,
            type = HabitType.BINARY,
            targetValue = null,
            unit = null,
            isActive = true,
            isArchived = false,
            currentStreak = 5,
            longestStreak = 10,
            totalCompletions = 15,
            expectedCompletions = 20,
            createdAt = Clock.System.now(),
            archivedAt = null
        )
        coEvery { mockHabitRepository.getHabitById(inputHabitId) } returns mockHabit

        // When
        val actualResult = useCase.execute(inputHabitId)

        // Then
        val expectedScore = HabitScore(
            totalCompletions = 15,
            expectedCompletions = 20,
            overCompletionCap = 150
        )
        assertEquals(expectedScore.percentage, actualResult?.percentage)
        assertEquals(75, actualResult?.percentage)
    }

    @Test
    fun `given habit does not exist when calculating score then returns null`() = runTest {
        // Given
        val inputHabitId = "non-existent"
        coEvery { mockHabitRepository.getHabitById(inputHabitId) } returns null

        // When
        val actualResult = useCase.execute(inputHabitId)

        // Then
        assertNull(actualResult)
    }

    @Test
    fun `given habit with perfect score when calculating then returns 100 percent`() = runTest {
        // Given
        val inputHabitId = "habit-1"
        val mockHabit = Habit(
            id = inputHabitId,
            name = "Perfect Habit",
            description = null,
            type = HabitType.BINARY,
            targetValue = null,
            unit = null,
            isActive = true,
            isArchived = false,
            currentStreak = 30,
            longestStreak = 30,
            totalCompletions = 30,
            expectedCompletions = 30,
            createdAt = Clock.System.now(),
            archivedAt = null
        )
        coEvery { mockHabitRepository.getHabitById(inputHabitId) } returns mockHabit

        // When
        val actualResult = useCase.execute(inputHabitId)

        // Then
        assertEquals(100, actualResult?.percentage)
        assertEquals(true, actualResult?.isPerfect)
    }

    @Test
    fun `given habit with over-completion when calculating then caps at 150 percent`() = runTest {
        // Given
        val inputHabitId = "habit-1"
        val mockHabit = Habit(
            id = inputHabitId,
            name = "Over-achiever",
            description = null,
            type = HabitType.QUANTITATIVE,
            targetValue = 10,
            unit = "reps",
            isActive = true,
            isArchived = false,
            currentStreak = 10,
            longestStreak = 10,
            totalCompletions = 200,
            expectedCompletions = 100,
            createdAt = Clock.System.now(),
            archivedAt = null
        )
        coEvery { mockHabitRepository.getHabitById(inputHabitId) } returns mockHabit

        // When
        val actualResult = useCase.execute(inputHabitId)

        // Then
        assertEquals(150, actualResult?.percentage)
        assertEquals(true, actualResult?.isOverCompleted)
        assertEquals(100, actualResult?.overCompletionCount)
    }

    @Test
    fun `given habit with custom cap when calculating then respects custom cap`() = runTest {
        // Given
        val inputHabitId = "habit-1"
        val mockHabit = Habit(
            id = inputHabitId,
            name = "Custom Cap Habit",
            description = null,
            type = HabitType.BINARY,
            targetValue = null,
            unit = null,
            isActive = true,
            isArchived = false,
            currentStreak = 10,
            longestStreak = 10,
            totalCompletions = 250,
            expectedCompletions = 100,
            createdAt = Clock.System.now(),
            archivedAt = null
        )
        coEvery { mockHabitRepository.getHabitById(inputHabitId) } returns mockHabit

        // When
        val actualResult = useCase.execute(inputHabitId, overCompletionCap = 200)

        // Then
        assertEquals(200, actualResult?.percentage)
    }

    @Test
    fun `given multiple habits when calculating scores then returns map with all scores`() {
        // Given
        val inputHabits = listOf(
            Habit(
                id = "habit-1",
                name = "Habit 1",
                description = null,
                type = HabitType.BINARY,
                targetValue = null,
                unit = null,
                isActive = true,
                isArchived = false,
                currentStreak = 5,
                longestStreak = 5,
                totalCompletions = 10,
                expectedCompletions = 10,
                createdAt = Clock.System.now(),
                archivedAt = null
            ),
            Habit(
                id = "habit-2",
                name = "Habit 2",
                description = null,
                type = HabitType.BINARY,
                targetValue = null,
                unit = null,
                isActive = true,
                isArchived = false,
                currentStreak = 3,
                longestStreak = 5,
                totalCompletions = 5,
                expectedCompletions = 10,
                createdAt = Clock.System.now(),
                archivedAt = null
            )
        )

        // When
        val actualScores = useCase.calculateScoresForHabits(inputHabits)

        // Then
        assertEquals(2, actualScores.size)
        assertEquals(100, actualScores["habit-1"]?.percentage)
        assertEquals(50, actualScores["habit-2"]?.percentage)
    }

    @Test
    fun `given habit with zero expected when calculating then returns zero percent`() = runTest {
        // Given
        val inputHabitId = "habit-1"
        val mockHabit = Habit(
            id = inputHabitId,
            name = "New Habit",
            description = null,
            type = HabitType.BINARY,
            targetValue = null,
            unit = null,
            isActive = true,
            isArchived = false,
            currentStreak = 0,
            longestStreak = 0,
            totalCompletions = 5,
            expectedCompletions = 0,
            createdAt = Clock.System.now(),
            archivedAt = null
        )
        coEvery { mockHabitRepository.getHabitById(inputHabitId) } returns mockHabit

        // When
        val actualResult = useCase.execute(inputHabitId)

        // Then
        assertEquals(0, actualResult?.percentage)
    }
}
