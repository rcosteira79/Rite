package com.ricardocosteira.habitlock.domain.usecases

import com.ricardocosteira.habitlock.domain.models.LeavePeriod
import com.ricardocosteira.habitlock.domain.models.User
import com.ricardocosteira.habitlock.domain.models.UndoPolicy
import com.ricardocosteira.habitlock.domain.repositories.LeavePeriodRepository
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

class UnsuspendHabitTest {

    private val mockLeavePeriodRepository = mockk<LeavePeriodRepository>(relaxed = true)
    private val mockUserRepository = mockk<UserRepository>()
    private val useCase = UnsuspendHabit(mockLeavePeriodRepository, mockUserRepository)

    private val mockUser = User(
        id = "user-1",
        timezone = TimeZone.UTC,
        previousTimezone = null,
        undoPolicy = UndoPolicy.TODAY_ONLY,
        maxSnoozeDurationMinutes = 30,
        maxSnoozesPerHabitPerDay = 3,
        maxConsecutiveSkips = 2,
        isOnboardingCompleted = true,
        dailySummaryTime = null,
        createdAt = Clock.System.now()
    )

    @Test
    fun `given active leave period when unsuspending then ends it today`() = runTest {
        // Given
        // NOTE: dates must straddle the real current date since Clock.System is not injected yet.
        // TODO: inject Clock into UnsuspendHabit for proper time-controlled testing.
        val inputLeavePeriodId = "leave-1"
        val mockLeavePeriod = LeavePeriod(
            id = inputLeavePeriodId,
            habitId = "habit-1",
            startDate = LocalDate(2026, 3, 1),   // Started before today
            endDate = LocalDate(2099, 12, 31),   // Far-future end date
            reason = "Vacation",
            createdAt = Clock.System.now()
        )

        coEvery { mockLeavePeriodRepository.getLeavePeriodById(inputLeavePeriodId) } returnsMany listOf(
            mockLeavePeriod,
            mockLeavePeriod.copy(endDate = LocalDate(2099, 12, 31))
        )
        coEvery { mockUserRepository.getUser() } returns mockUser.copy(timezone = TimeZone.UTC)

        // When
        val actualResult = useCase.execute(inputLeavePeriodId)

        // Then
        assertTrue(actualResult.isSuccess)
        coVerify { mockLeavePeriodRepository.endLeavePeriod(inputLeavePeriodId, any()) }
    }

    @Test
    fun `given future leave period when unsuspending then deletes it`() = runTest {
        // Given
        // NOTE: startDate must be in the future relative to the real clock.
        // TODO: inject Clock into UnsuspendHabit for proper time-controlled testing.
        val inputLeavePeriodId = "leave-1"
        val mockLeavePeriod = LeavePeriod(
            id = inputLeavePeriodId,
            habitId = "habit-1",
            startDate = LocalDate(2099, 6, 1),   // Far-future start date
            endDate = LocalDate(2099, 12, 31),
            reason = "Future vacation",
            createdAt = Clock.System.now()
        )

        coEvery { mockLeavePeriodRepository.getLeavePeriodById(inputLeavePeriodId) } returns mockLeavePeriod
        coEvery { mockUserRepository.getUser() } returns mockUser.copy(timezone = TimeZone.UTC)

        // When
        val actualResult = useCase.execute(inputLeavePeriodId)

        // Then
        assertTrue(actualResult.isSuccess)
        assertNull(actualResult.getOrNull())  // Returns null for deleted periods
        coVerify { mockLeavePeriodRepository.deleteLeavePeriod(inputLeavePeriodId) }
    }

    @Test
    fun `given ended leave period when unsuspending then returns failure`() = runTest {
        // Given
        val inputLeavePeriodId = "leave-1"
        val today = LocalDate(2026, 1, 20)
        val mockLeavePeriod = LeavePeriod(
            id = inputLeavePeriodId,
            habitId = "habit-1",
            startDate = LocalDate(2026, 1, 1),
            endDate = LocalDate(2026, 1, 10),  // Already ended
            reason = "Past vacation",
            createdAt = Clock.System.now()
        )

        coEvery { mockLeavePeriodRepository.getLeavePeriodById(inputLeavePeriodId) } returns mockLeavePeriod
        coEvery { mockUserRepository.getUser() } returns mockUser.copy(timezone = TimeZone.UTC)

        // When
        val actualResult = useCase.execute(inputLeavePeriodId)

        // Then
        assertTrue(actualResult.isFailure)
        assertEquals("Leave period has already ended", actualResult.exceptionOrNull()?.message)
    }

    @Test
    fun `given non-existent leave period when unsuspending then returns failure`() = runTest {
        // Given
        val inputLeavePeriodId = "non-existent"

        coEvery { mockLeavePeriodRepository.getLeavePeriodById(inputLeavePeriodId) } returns null

        // When
        val actualResult = useCase.execute(inputLeavePeriodId)

        // Then
        assertTrue(actualResult.isFailure)
        assertEquals("Leave period not found", actualResult.exceptionOrNull()?.message)
    }

    @Test
    fun `given no user when unsuspending then returns failure`() = runTest {
        // Given
        val inputLeavePeriodId = "leave-1"
        val mockLeavePeriod = LeavePeriod(
            id = inputLeavePeriodId,
            habitId = "habit-1",
            startDate = LocalDate(2026, 1, 15),
            endDate = LocalDate(2026, 1, 30),
            reason = null,
            createdAt = Clock.System.now()
        )

        coEvery { mockLeavePeriodRepository.getLeavePeriodById(inputLeavePeriodId) } returns mockLeavePeriod
        coEvery { mockUserRepository.getUser() } returns null

        // When
        val actualResult = useCase.execute(inputLeavePeriodId)

        // Then
        assertTrue(actualResult.isFailure)
        assertEquals("User not found", actualResult.exceptionOrNull()?.message)
    }

    @Test
    fun `given leave period when deleting then removes it completely`() = runTest {
        // Given
        val inputLeavePeriodId = "leave-1"
        val mockLeavePeriod = LeavePeriod(
            id = inputLeavePeriodId,
            habitId = "habit-1",
            startDate = LocalDate(2026, 1, 15),
            endDate = LocalDate(2026, 1, 30),
            reason = null,
            createdAt = Clock.System.now()
        )

        coEvery { mockLeavePeriodRepository.getLeavePeriodById(inputLeavePeriodId) } returns mockLeavePeriod

        // When
        val actualResult = useCase.delete(inputLeavePeriodId)

        // Then
        assertTrue(actualResult.isSuccess)
        coVerify { mockLeavePeriodRepository.deleteLeavePeriod(inputLeavePeriodId) }
    }

    @Test
    fun `given non-existent leave period when deleting then returns failure`() = runTest {
        // Given
        val inputLeavePeriodId = "non-existent"

        coEvery { mockLeavePeriodRepository.getLeavePeriodById(inputLeavePeriodId) } returns null

        // When
        val actualResult = useCase.delete(inputLeavePeriodId)

        // Then
        assertTrue(actualResult.isFailure)
        assertEquals("Leave period not found", actualResult.exceptionOrNull()?.message)
    }

    @Test
    fun `given indefinite leave period when unsuspending then ends it today`() = runTest {
        // Given
        val inputLeavePeriodId = "leave-1"
        val mockLeavePeriod = LeavePeriod(
            id = inputLeavePeriodId,
            habitId = "habit-1",
            startDate = LocalDate(2026, 1, 15),
            endDate = null,  // Indefinite
            reason = "Indefinite break",
            createdAt = Clock.System.now()
        )

        coEvery { mockLeavePeriodRepository.getLeavePeriodById(inputLeavePeriodId) } returns mockLeavePeriod
        coEvery { mockUserRepository.getUser() } returns mockUser.copy(timezone = TimeZone.UTC)

        // When
        val actualResult = useCase.execute(inputLeavePeriodId)

        // Then
        assertTrue(actualResult.isSuccess)
        coVerify { mockLeavePeriodRepository.endLeavePeriod(inputLeavePeriodId, any()) }
    }
}
