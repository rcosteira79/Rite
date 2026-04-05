package com.ricardocosteira.rite.presentation.ui.habitdetail

import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.datetime.LocalDate

class HabitDetailStateTest {

    private val baseHabit = Habit(
        id = "h1",
        name = "Read",
        description = null,
        type = HabitType.BINARY,
        targetValue = null,
        unit = null,
        defaultIncrement = 1,
        isTrackingEnabled = false,
        isActive = true,
        isArchived = false,
        currentStreak = 5,
        longestStreak = 10,
        totalCompletions = 20,
        expectedCompletions = 25,
        createdAt = Clock.System.now(),
        archivedAt = null
    )

    private val baseInstance = HabitInstance(
        id = "i1",
        habitId = "h1",
        date = LocalDate(2026, 4, 5),
        status = HabitStatus.PENDING,
        completedValue = null,
        targetValue = null,
        consecutiveSkipsAtCreation = 0,
        createdAt = Clock.System.now(),
        completedAt = null
    )

    @Test
    fun `given unlimited skips, skipsRemaining returns null`() {
        val state = HabitDetailState(
            habit = baseHabit,
            instance = baseInstance,
            maxConsecutiveSkips = null
        )

        assertNull(state.skipsRemaining)
    }

    @Test
    fun `given max 2 skips and 0 used, skipsRemaining returns 2`() {
        val state = HabitDetailState(
            habit = baseHabit,
            instance = baseInstance,
            maxConsecutiveSkips = 2,
            currentConsecutiveSkips = 0
        )

        assertEquals(2, state.skipsRemaining)
    }

    @Test
    fun `given max 2 skips and 2 used, skipsRemaining returns 0`() {
        val state = HabitDetailState(
            habit = baseHabit,
            instance = baseInstance,
            maxConsecutiveSkips = 2,
            currentConsecutiveSkips = 2
        )

        assertEquals(0, state.skipsRemaining)
    }

    @Test
    fun `given max 2 skips and 2 used, isSkipLocked returns true`() {
        val state = HabitDetailState(
            habit = baseHabit,
            instance = baseInstance,
            maxConsecutiveSkips = 2,
            currentConsecutiveSkips = 2
        )

        assertTrue(state.isSkipLocked)
    }

    @Test
    fun `given unlimited skips, isSkipLocked returns false`() {
        val state = HabitDetailState(
            habit = baseHabit,
            instance = baseInstance,
            maxConsecutiveSkips = null,
            currentConsecutiveSkips = 5
        )

        assertFalse(state.isSkipLocked)
    }

    @Test
    fun `given completed instance, isCompleted returns true`() {
        val state = HabitDetailState(
            instance = baseInstance.copy(status = HabitStatus.COMPLETED)
        )

        assertTrue(state.isCompleted)
        assertTrue(state.isResolved)
    }

    @Test
    fun `given pending instance, isResolved returns false`() {
        val state = HabitDetailState(
            instance = baseInstance.copy(status = HabitStatus.PENDING)
        )

        assertFalse(state.isResolved)
    }

    @Test
    fun `given skipped instance, isSkipped returns true`() {
        val state = HabitDetailState(
            instance = baseInstance.copy(status = HabitStatus.SKIPPED)
        )

        assertTrue(state.isSkipped)
        assertTrue(state.isResolved)
    }

    @Test
    fun `given failed instance, isFailed returns true`() {
        val state = HabitDetailState(
            instance = baseInstance.copy(status = HabitStatus.FAILED)
        )

        assertTrue(state.isFailed)
        assertTrue(state.isResolved)
    }
}
