package com.ricardocosteira.habitlock.domain.usecases

import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.models.SnoozeState
import com.ricardocosteira.habitlock.domain.models.UndoPolicy
import com.ricardocosteira.habitlock.domain.models.User
import com.ricardocosteira.habitlock.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.habitlock.domain.repositories.SnoozeRepository
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
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
import kotlinx.datetime.TimeZone

class SnoozeHabitUseCaseTest {

    private val mockHabitInstanceRepository = mockk<HabitInstanceRepository>()
    private val mockSnoozeRepository = mockk<SnoozeRepository>(relaxed = true)
    private val mockUserRepository = mockk<UserRepository>()
    private val useCase = SnoozeHabitUseCase(
        mockHabitInstanceRepository,
        mockSnoozeRepository,
        mockUserRepository
    )

    private val mockUser = User(
        id = "user-1",
        timezone = TimeZone.UTC,
        previousTimezone = null,
        undoPolicy = UndoPolicy.TODAY_ONLY,
        maxSnoozeDurationMinutes = 60,
        maxSnoozesPerHabitPerDay = 3,
        maxConsecutiveSkips = 2,
        isOnboardingCompleted = true,
        dailySummaryTime = null,
        createdAt = Clock.System.now()
    )

    @Test
    fun `given pending instance when snoozing then creates snooze state`() = runTest {
        // Given
        val inputInstanceId = "instance-1"
        val inputDuration = 30
        val mockInstance = createMockInstance(inputInstanceId, HabitStatus.PENDING)

        coEvery { mockHabitInstanceRepository.getInstanceById(inputInstanceId) } returns mockInstance
        coEvery { mockUserRepository.getUser() } returns mockUser
        coEvery { mockSnoozeRepository.getSnoozeState(inputInstanceId) } returns null

        // When
        val actualResult = useCase.execute(inputInstanceId, inputDuration)

        // Then
        assertTrue(actualResult.isSuccess)
        val snoozeState = actualResult.getOrNull()!!
        assertEquals(inputInstanceId, snoozeState.habitInstanceId)
        assertEquals(1, snoozeState.snoozeCount)
        coVerify { mockSnoozeRepository.saveSnoozeState(inputInstanceId, any(), 1) }
    }

    @Test
    fun `given instance does not exist when snoozing then returns failure`() = runTest {
        // Given
        val inputInstanceId = "non-existent"
        val inputDuration = 30

        coEvery { mockHabitInstanceRepository.getInstanceById(inputInstanceId) } returns null

        // When
        val actualResult = useCase.execute(inputInstanceId, inputDuration)

        // Then
        assertTrue(actualResult.isFailure)
        assertEquals("Instance not found", actualResult.exceptionOrNull()?.message)
    }

    @Test
    fun `given completed instance when snoozing then returns failure`() = runTest {
        // Given
        val inputInstanceId = "instance-1"
        val inputDuration = 30
        val mockInstance = createMockInstance(inputInstanceId, HabitStatus.COMPLETED)

        coEvery { mockHabitInstanceRepository.getInstanceById(inputInstanceId) } returns mockInstance

        // When
        val actualResult = useCase.execute(inputInstanceId, inputDuration)

        // Then
        assertTrue(actualResult.isFailure)
        assertEquals("Only pending habits can be snoozed", actualResult.exceptionOrNull()?.message)
    }

    @Test
    fun `given suspended instance when snoozing then returns failure`() = runTest {
        // Given
        val inputInstanceId = "instance-1"
        val inputDuration = 30
        val mockInstance = createMockInstance(inputInstanceId, HabitStatus.SUSPENDED)

        coEvery { mockHabitInstanceRepository.getInstanceById(inputInstanceId) } returns mockInstance

        // When
        val actualResult = useCase.execute(inputInstanceId, inputDuration)

        // Then
        assertTrue(actualResult.isFailure)
        assertEquals("Only pending habits can be snoozed", actualResult.exceptionOrNull()?.message)
    }

    @Test
    fun `given max snoozes reached when snoozing then returns failure`() = runTest {
        // Given
        val inputInstanceId = "instance-1"
        val inputDuration = 30
        val mockInstance = createMockInstance(inputInstanceId, HabitStatus.PENDING)
        val existingSnooze = SnoozeState(
            habitInstanceId = inputInstanceId,
            scheduledTime = Clock.System.now(),
            snoozeCount = 3  // Already at max
        )

        coEvery { mockHabitInstanceRepository.getInstanceById(inputInstanceId) } returns mockInstance
        coEvery { mockUserRepository.getUser() } returns mockUser
        coEvery { mockSnoozeRepository.getSnoozeState(inputInstanceId) } returns existingSnooze

        // When
        val actualResult = useCase.execute(inputInstanceId, inputDuration)

        // Then
        assertTrue(actualResult.isFailure)
        assertTrue(actualResult.exceptionOrNull() is SnoozeLimitReachedException)
        assertEquals("Maximum snoozes reached for today", actualResult.exceptionOrNull()?.message)
    }

    @Test
    fun `given existing snooze when snoozing again then increments count`() = runTest {
        // Given
        val inputInstanceId = "instance-1"
        val inputDuration = 30
        val mockInstance = createMockInstance(inputInstanceId, HabitStatus.PENDING)
        val existingSnooze = SnoozeState(
            habitInstanceId = inputInstanceId,
            scheduledTime = Clock.System.now(),
            snoozeCount = 1
        )

        coEvery { mockHabitInstanceRepository.getInstanceById(inputInstanceId) } returns mockInstance
        coEvery { mockUserRepository.getUser() } returns mockUser
        coEvery { mockSnoozeRepository.getSnoozeState(inputInstanceId) } returns existingSnooze

        // When
        val actualResult = useCase.execute(inputInstanceId, inputDuration)

        // Then
        assertTrue(actualResult.isSuccess)
        val snoozeState = actualResult.getOrNull()!!
        assertEquals(2, snoozeState.snoozeCount)
        coVerify { mockSnoozeRepository.saveSnoozeState(inputInstanceId, any(), 2) }
    }

    @Test
    fun `given duration exceeds max when snoozing then caps to max duration`() = runTest {
        // Given
        val inputInstanceId = "instance-1"
        val inputDuration = 120  // Exceeds max of 60
        val mockInstance = createMockInstance(inputInstanceId, HabitStatus.PENDING)

        coEvery { mockHabitInstanceRepository.getInstanceById(inputInstanceId) } returns mockInstance
        coEvery { mockUserRepository.getUser() } returns mockUser
        coEvery { mockSnoozeRepository.getSnoozeState(inputInstanceId) } returns null

        val scheduledTimeSlot = slot<kotlin.time.Instant>()
        coEvery { 
            mockSnoozeRepository.saveSnoozeState(any(), capture(scheduledTimeSlot), any()) 
        } returns Unit

        // When
        val actualResult = useCase.execute(inputInstanceId, inputDuration)

        // Then
        assertTrue(actualResult.isSuccess)
        // Verify that duration was capped (scheduled time should be ~60 minutes from now, not 120)
        coVerify { mockSnoozeRepository.saveSnoozeState(any(), any(), any()) }
    }

    @Test
    fun `given no user when snoozing then returns failure`() = runTest {
        // Given
        val inputInstanceId = "instance-1"
        val inputDuration = 30
        val mockInstance = createMockInstance(inputInstanceId, HabitStatus.PENDING)

        coEvery { mockHabitInstanceRepository.getInstanceById(inputInstanceId) } returns mockInstance
        coEvery { mockUserRepository.getUser() } returns null

        // When
        val actualResult = useCase.execute(inputInstanceId, inputDuration)

        // Then
        assertTrue(actualResult.isFailure)
        assertEquals("User not found", actualResult.exceptionOrNull()?.message)
    }

    @Test
    fun `given user with no snooze limit when snoozing then allows unlimited snoozes`() = runTest {
        // Given
        val inputInstanceId = "instance-1"
        val inputDuration = 30
        val mockInstance = createMockInstance(inputInstanceId, HabitStatus.PENDING)
        val userWithNoLimit = mockUser.copy(maxSnoozesPerHabitPerDay = null)
        val existingSnooze = SnoozeState(
            habitInstanceId = inputInstanceId,
            scheduledTime = Clock.System.now(),
            snoozeCount = 100  // High count, but no limit set
        )

        coEvery { mockHabitInstanceRepository.getInstanceById(inputInstanceId) } returns mockInstance
        coEvery { mockUserRepository.getUser() } returns userWithNoLimit
        coEvery { mockSnoozeRepository.getSnoozeState(inputInstanceId) } returns existingSnooze

        // When
        val actualResult = useCase.execute(inputInstanceId, inputDuration)

        // Then
        assertTrue(actualResult.isSuccess)
        assertEquals(101, actualResult.getOrNull()?.snoozeCount)
    }
}

private fun createMockInstance(id: String, status: HabitStatus) = HabitInstance(
    id = id,
    habitId = "habit-1",
    date = LocalDate(2026, 1, 20),
    status = status,
    completedValue = null,
    targetValue = null,
    consecutiveSkipsAtCreation = 0,
    createdAt = Clock.System.now()
)
