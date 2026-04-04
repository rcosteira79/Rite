package com.ricardocosteira.rite.domain.usecases

import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.LeavePeriod
import com.ricardocosteira.rite.domain.repositories.HabitRepository
import com.ricardocosteira.rite.domain.repositories.LeavePeriodRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class SuspendHabitTest {

    private val mockHabitRepository = mockk<HabitRepository>()
    private val mockLeavePeriodRepository = mockk<LeavePeriodRepository>(relaxed = true)
    private val mockUuidProvider = mockk<UuidProvider>()
    private val useCase = SuspendHabit(
        mockHabitRepository,
        mockLeavePeriodRepository,
        mockUuidProvider
    )

    @Test
    fun `given valid habit when suspending then creates leave period`() = runTest {
        // Given
        val inputHabitId = "habit-1"
        val inputStartDate = LocalDate(2026, 1, 20)
        val inputEndDate = LocalDate(2026, 1, 27)
        val inputReason = "Vacation"
        val mockHabit = createMockHabit(inputHabitId)
        val generatedId = "leave-1"

        coEvery { mockHabitRepository.getHabitById(inputHabitId) } returns mockHabit
        coEvery { mockLeavePeriodRepository.getLeavePeriodsByHabit(inputHabitId) } returns emptyList()
        coEvery { mockUuidProvider.generate() } returns generatedId

        // When
        val actualResult = useCase.execute(inputHabitId, inputStartDate, inputEndDate, inputReason)

        // Then
        assertTrue(actualResult.isSuccess)
        val leavePeriod = actualResult.getOrNull()!!
        assertEquals(generatedId, leavePeriod.id)
        assertEquals(inputHabitId, leavePeriod.habitId)
        assertEquals(inputStartDate, leavePeriod.startDate)
        assertEquals(inputEndDate, leavePeriod.endDate)
        assertEquals(inputReason, leavePeriod.reason)

        coVerify { mockLeavePeriodRepository.createLeavePeriod(any()) }
    }

    @Test
    fun `given habit does not exist when suspending then returns failure`() = runTest {
        // Given
        val inputHabitId = "non-existent"
        val inputStartDate = LocalDate(2026, 1, 20)
        val inputEndDate = LocalDate(2026, 1, 27)

        coEvery { mockHabitRepository.getHabitById(inputHabitId) } returns null

        // When
        val actualResult = useCase.execute(inputHabitId, inputStartDate, inputEndDate)

        // Then
        assertTrue(actualResult.isFailure)
        assertEquals("Habit not found", actualResult.exceptionOrNull()?.message)
    }

    @Test
    fun `given end date before start date when suspending then returns failure`() = runTest {
        // Given
        val inputHabitId = "habit-1"
        val inputStartDate = LocalDate(2026, 1, 27)
        val inputEndDate = LocalDate(2026, 1, 20)  // Before start date
        val mockHabit = createMockHabit(inputHabitId)

        coEvery { mockHabitRepository.getHabitById(inputHabitId) } returns mockHabit

        // When
        val actualResult = useCase.execute(inputHabitId, inputStartDate, inputEndDate)

        // Then
        assertTrue(actualResult.isFailure)
        assertEquals("End date must be after start date", actualResult.exceptionOrNull()?.message)
    }

    @Test
    fun `given overlapping leave period when suspending then returns failure`() = runTest {
        // Given
        val inputHabitId = "habit-1"
        val inputStartDate = LocalDate(2026, 1, 20)
        val inputEndDate = LocalDate(2026, 1, 27)
        val mockHabit = createMockHabit(inputHabitId)
        
        val existingLeavePeriod = LeavePeriod(
            id = "leave-existing",
            habitId = inputHabitId,
            startDate = LocalDate(2026, 1, 22),  // Overlaps with input
            endDate = LocalDate(2026, 1, 25),
            reason = null,
            createdAt = Clock.System.now()
        )

        coEvery { mockHabitRepository.getHabitById(inputHabitId) } returns mockHabit
        coEvery { mockLeavePeriodRepository.getLeavePeriodsByHabit(inputHabitId) } returns listOf(existingLeavePeriod)

        // When
        val actualResult = useCase.execute(inputHabitId, inputStartDate, inputEndDate)

        // Then
        assertTrue(actualResult.isFailure)
        assertEquals("Leave period overlaps with an existing suspension", actualResult.exceptionOrNull()?.message)
    }

    @Test
    fun `given indefinite suspension when creating then accepts null end date`() = runTest {
        // Given
        val inputHabitId = "habit-1"
        val inputStartDate = LocalDate(2026, 1, 20)
        val inputEndDate: LocalDate? = null  // Indefinite
        val mockHabit = createMockHabit(inputHabitId)
        val generatedId = "leave-1"

        coEvery { mockHabitRepository.getHabitById(inputHabitId) } returns mockHabit
        coEvery { mockLeavePeriodRepository.getLeavePeriodsByHabit(inputHabitId) } returns emptyList()
        coEvery { mockUuidProvider.generate() } returns generatedId

        // When
        val actualResult = useCase.execute(inputHabitId, inputStartDate, inputEndDate)

        // Then
        assertTrue(actualResult.isSuccess)
        val leavePeriod = actualResult.getOrNull()!!
        assertEquals(null, leavePeriod.endDate)
        assertTrue(leavePeriod.isIndefinite)
    }

    @Test
    fun `given non-overlapping leave periods when suspending then succeeds`() = runTest {
        // Given
        val inputHabitId = "habit-1"
        val inputStartDate = LocalDate(2026, 2, 1)
        val inputEndDate = LocalDate(2026, 2, 7)
        val mockHabit = createMockHabit(inputHabitId)
        
        val existingLeavePeriod = LeavePeriod(
            id = "leave-existing",
            habitId = inputHabitId,
            startDate = LocalDate(2026, 1, 20),
            endDate = LocalDate(2026, 1, 27),  // Ends before new period starts
            reason = null,
            createdAt = Clock.System.now()
        )

        coEvery { mockHabitRepository.getHabitById(inputHabitId) } returns mockHabit
        coEvery { mockLeavePeriodRepository.getLeavePeriodsByHabit(inputHabitId) } returns listOf(existingLeavePeriod)
        coEvery { mockUuidProvider.generate() } returns "leave-new"

        // When
        val actualResult = useCase.execute(inputHabitId, inputStartDate, inputEndDate)

        // Then
        assertTrue(actualResult.isSuccess)
        coVerify { mockLeavePeriodRepository.createLeavePeriod(any()) }
    }

    @Test
    fun `given suspension without reason when creating then accepts null reason`() = runTest {
        // Given
        val inputHabitId = "habit-1"
        val inputStartDate = LocalDate(2026, 1, 20)
        val inputEndDate = LocalDate(2026, 1, 27)
        val mockHabit = createMockHabit(inputHabitId)

        coEvery { mockHabitRepository.getHabitById(inputHabitId) } returns mockHabit
        coEvery { mockLeavePeriodRepository.getLeavePeriodsByHabit(inputHabitId) } returns emptyList()
        coEvery { mockUuidProvider.generate() } returns "leave-1"

        // When
        val actualResult = useCase.execute(inputHabitId, inputStartDate, inputEndDate, reason = null)

        // Then
        assertTrue(actualResult.isSuccess)
        assertEquals(null, actualResult.getOrNull()?.reason)
    }

    private fun createMockHabit(id: String) = Habit(
        id = id,
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
}
