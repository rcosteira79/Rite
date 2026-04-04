package com.ricardocosteira.rite.domain.usecases

import com.ricardocosteira.rite.domain.models.SnoozeState
import com.ricardocosteira.rite.domain.repositories.SnoozeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.coroutines.test.runTest

class ClearSnoozeStateTest {

    private val mockSnoozeRepository = mockk<SnoozeRepository>(relaxed = true)
    private val useCase = ClearSnoozeState(mockSnoozeRepository)

    @Test
    fun `given instance id when clearing snooze then calls repository`() = runTest {
        // Given
        val inputInstanceId = "instance-1"

        // When
        val actualResult = useCase.execute(inputInstanceId)

        // Then
        assertTrue(actualResult.isSuccess)
        coVerify { mockSnoozeRepository.clearSnoozeState(inputInstanceId) }
    }

    @Test
    fun `given repository throws exception when clearing then returns failure`() = runTest {
        // Given
        val inputInstanceId = "instance-1"
        val expectedException = RuntimeException("Database error")

        coEvery { mockSnoozeRepository.clearSnoozeState(inputInstanceId) } throws expectedException

        // When
        val actualResult = useCase.execute(inputInstanceId)

        // Then
        assertTrue(actualResult.isFailure)
        assertTrue(actualResult.exceptionOrNull() is RuntimeException)
    }

    @Test
    fun `given multiple snooze states when clearing all then clears each one`() = runTest {
        // Given
        val mockSnoozeStates = listOf(
            SnoozeState(
                habitInstanceId = "instance-1",
                scheduledTime = Clock.System.now(),
                snoozeCount = 1
            ),
            SnoozeState(
                habitInstanceId = "instance-2",
                scheduledTime = Clock.System.now(),
                snoozeCount = 2
            ),
            SnoozeState(
                habitInstanceId = "instance-3",
                scheduledTime = Clock.System.now(),
                snoozeCount = 1
            )
        )

        coEvery { mockSnoozeRepository.getAllSnoozeStates() } returns mockSnoozeStates

        // When
        val actualResult = useCase.clearAll()

        // Then
        assertTrue(actualResult.isSuccess)
        coVerify(exactly = 3) { mockSnoozeRepository.clearSnoozeState(any()) }
        coVerify { mockSnoozeRepository.clearSnoozeState("instance-1") }
        coVerify { mockSnoozeRepository.clearSnoozeState("instance-2") }
        coVerify { mockSnoozeRepository.clearSnoozeState("instance-3") }
    }

    @Test
    fun `given no snooze states when clearing all then succeeds without errors`() = runTest {
        // Given
        coEvery { mockSnoozeRepository.getAllSnoozeStates() } returns emptyList()

        // When
        val actualResult = useCase.clearAll()

        // Then
        assertTrue(actualResult.isSuccess)
        coVerify(exactly = 0) { mockSnoozeRepository.clearSnoozeState(any()) }
    }

    @Test
    fun `given repository throws exception when clearing all then returns failure`() = runTest {
        // Given
        val expectedException = RuntimeException("Database error")

        coEvery { mockSnoozeRepository.getAllSnoozeStates() } throws expectedException

        // When
        val actualResult = useCase.clearAll()

        // Then
        assertTrue(actualResult.isFailure)
        assertTrue(actualResult.exceptionOrNull() is RuntimeException)
    }

    @Test
    fun `given one snooze fails when clearing all then returns failure`() = runTest {
        // Given
        val mockSnoozeStates = listOf(
            SnoozeState(
                habitInstanceId = "instance-1",
                scheduledTime = Clock.System.now(),
                snoozeCount = 1
            ),
            SnoozeState(
                habitInstanceId = "instance-2",
                scheduledTime = Clock.System.now(),
                snoozeCount = 2
            )
        )
        val expectedException = RuntimeException("Failed to clear instance-2")

        coEvery { mockSnoozeRepository.getAllSnoozeStates() } returns mockSnoozeStates
        coEvery { mockSnoozeRepository.clearSnoozeState("instance-1") } returns Unit
        coEvery { mockSnoozeRepository.clearSnoozeState("instance-2") } throws expectedException

        // When
        val actualResult = useCase.clearAll()

        // Then
        assertTrue(actualResult.isFailure)
        assertTrue(actualResult.exceptionOrNull() is RuntimeException)
    }

    @Test
    fun `given single snooze state when clearing all then clears it`() = runTest {
        // Given
        val mockSnoozeState = SnoozeState(
            habitInstanceId = "instance-1",
            scheduledTime = Clock.System.now(),
            snoozeCount = 1
        )

        coEvery { mockSnoozeRepository.getAllSnoozeStates() } returns listOf(mockSnoozeState)

        // When
        val actualResult = useCase.clearAll()

        // Then
        assertTrue(actualResult.isSuccess)
        coVerify(exactly = 1) { mockSnoozeRepository.clearSnoozeState("instance-1") }
    }
}
