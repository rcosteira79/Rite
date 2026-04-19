package com.ricardocosteira.rite.presentation.ui.habitdetail

import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.presentation.ui.habitdetail.models.HeatmapDay
import kotlin.test.Test
import kotlin.test.assertEquals

class HeatmapDataTest {

    @Test
    fun `given completed instance, completionPercentage is 1`() {
        val day = HeatmapDay(
            date = "2026-04-01",
            completionPercentage = 1.0f,
            status = HabitStatus.COMPLETED
        )

        assertEquals(1.0f, day.completionPercentage)
        assertEquals(HabitStatus.COMPLETED, day.status)
    }

    @Test
    fun `given partial progress instance, completionPercentage reflects progress`() {
        val day = HeatmapDay(
            date = "2026-04-01",
            completionPercentage = 0.5f,
            status = HabitStatus.PENDING
        )

        assertEquals(0.5f, day.completionPercentage)
    }

    @Test
    fun `given skipped instance, status is SKIPPED`() {
        val day = HeatmapDay(
            date = "2026-04-01",
            completionPercentage = 0f,
            status = HabitStatus.SKIPPED
        )

        assertEquals(HabitStatus.SKIPPED, day.status)
    }
}
