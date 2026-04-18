package com.ricardocosteira.rite.presentation.ui.today.habitcard

import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import kotlin.test.Test
import kotlin.test.assertEquals

class HabitCardVisualsTest {

    @Test
    fun `pending binary resolves to Pending state with zero fill`() {
        val result = visualsFor(
            status = HabitStatus.PENDING,
            type = HabitType.BINARY,
            progressPercentage = 0f
        )
        assertEquals(HabitCardState.Pending, result.state)
        assertEquals(0f, result.fillFraction)
    }

    @Test
    fun `pending quantitative in-progress reports progress fraction`() {
        val result = visualsFor(
            status = HabitStatus.PENDING,
            type = HabitType.QUANTITATIVE,
            progressPercentage = 0.4f
        )
        assertEquals(HabitCardState.PendingInProgress, result.state)
        assertEquals(0.4f, result.fillFraction)
    }

    @Test
    fun `completed always reports fill one regardless of type`() {
        val binary = visualsFor(
            HabitStatus.COMPLETED,
            HabitType.BINARY,
            progressPercentage = 0f
        )
        val quant = visualsFor(
            HabitStatus.COMPLETED,
            HabitType.QUANTITATIVE,
            progressPercentage = 1f
        )
        assertEquals(HabitCardState.Completed, binary.state)
        assertEquals(1f, binary.fillFraction)
        assertEquals(HabitCardState.Completed, quant.state)
        assertEquals(1f, quant.fillFraction)
    }

    @Test
    fun `failed reports failed state with full fill for terracotta rule`() {
        val result = visualsFor(
            HabitStatus.FAILED,
            HabitType.BINARY,
            progressPercentage = 0f
        )
        assertEquals(HabitCardState.Failed, result.state)
        assertEquals(1f, result.fillFraction)
    }

    @Test
    fun `skipped reports skipped state with zero fill`() {
        val result = visualsFor(
            HabitStatus.SKIPPED,
            HabitType.BINARY,
            progressPercentage = 0f
        )
        assertEquals(HabitCardState.Skipped, result.state)
        assertEquals(0f, result.fillFraction)
    }

    @Test
    fun `suspended reports suspended state with zero fill`() {
        val result = visualsFor(
            HabitStatus.SUSPENDED,
            HabitType.QUANTITATIVE,
            progressPercentage = 0.25f
        )
        assertEquals(HabitCardState.Suspended, result.state)
        assertEquals(0f, result.fillFraction)
    }

    @Test
    fun `progressPercentage above one clamps to one`() {
        val result = visualsFor(
            HabitStatus.PENDING,
            HabitType.QUANTITATIVE,
            progressPercentage = 1.4f
        )
        assertEquals(HabitCardState.PendingInProgress, result.state)
        assertEquals(1f, result.fillFraction)
    }

    @Test
    fun `pending quantitative with zero progress stays in Pending not PendingInProgress`() {
        val result = visualsFor(
            HabitStatus.PENDING,
            HabitType.QUANTITATIVE,
            progressPercentage = 0f
        )
        assertEquals(HabitCardState.Pending, result.state)
        assertEquals(0f, result.fillFraction)
    }
}
