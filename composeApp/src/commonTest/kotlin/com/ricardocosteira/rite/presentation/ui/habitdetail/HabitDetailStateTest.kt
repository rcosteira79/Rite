package com.ricardocosteira.rite.presentation.ui.habitdetail

import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HabitDetailStateTest {

    private val baseUiModel = HabitDetailUiModel(
        habitId = "h1",
        instanceId = "i1",
        name = "Read",
        description = null,
        type = HabitType.BINARY,
        unit = null,
        defaultIncrement = 1,
        status = HabitStatus.PENDING,
        currentProgress = 0,
        targetValue = null,
        completedValue = null,
        progressPercentage = 0f,
        isQuantitativeComplete = false,
        currentStreak = 5,
        longestStreak = 10,
        habitScore = 80,
        maxConsecutiveSkips = null,
        currentConsecutiveSkips = 0
    )

    @Test
    fun `given unlimited skips, skipsRemaining returns null`() {
        val model = baseUiModel.copy(maxConsecutiveSkips = null)

        assertNull(model.skipsRemaining)
    }

    @Test
    fun `given max 2 skips and 0 used, skipsRemaining returns 2`() {
        val model = baseUiModel.copy(maxConsecutiveSkips = 2, currentConsecutiveSkips = 0)

        assertEquals(2, model.skipsRemaining)
    }

    @Test
    fun `given max 2 skips and 2 used, skipsRemaining returns 0`() {
        val model = baseUiModel.copy(maxConsecutiveSkips = 2, currentConsecutiveSkips = 2)

        assertEquals(0, model.skipsRemaining)
    }

    @Test
    fun `given max 2 skips and 2 used, isSkipLocked returns true`() {
        val model = baseUiModel.copy(maxConsecutiveSkips = 2, currentConsecutiveSkips = 2)

        assertTrue(model.isSkipLocked)
    }

    @Test
    fun `given unlimited skips, isSkipLocked returns false`() {
        val model = baseUiModel.copy(maxConsecutiveSkips = null, currentConsecutiveSkips = 5)

        assertFalse(model.isSkipLocked)
    }

    @Test
    fun `given completed status, isCompleted returns true`() {
        val model = baseUiModel.copy(status = HabitStatus.COMPLETED)

        assertTrue(model.isCompleted)
        assertTrue(model.isResolved)
    }

    @Test
    fun `given pending status, isResolved returns false`() {
        val model = baseUiModel.copy(status = HabitStatus.PENDING)

        assertFalse(model.isResolved)
    }

    @Test
    fun `given skipped status, isSkipped returns true`() {
        val model = baseUiModel.copy(status = HabitStatus.SKIPPED)

        assertTrue(model.isSkipped)
        assertTrue(model.isResolved)
    }

    @Test
    fun `given failed status, isFailed returns true`() {
        val model = baseUiModel.copy(status = HabitStatus.FAILED)

        assertTrue(model.isFailed)
        assertTrue(model.isResolved)
    }
}
