package com.ricardocosteira.habitlock.presentation.ui.today

import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.ScheduleType
import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel
import kotlin.test.Test
import kotlin.test.assertEquals

class TodayCountsTest {

    private fun buildHabit(
        status: HabitStatus,
        cadence: ScheduleType = ScheduleType.DAILY
    ): TodayHabitUiModel = TodayHabitUiModel(
        instanceId = "id-${status.name}-${cadence.name}",
        habitId = "habit-${status.name}",
        name = "Test",
        description = null,
        type = HabitType.BINARY,
        status = status,
        completedValue = null,
        targetValue = null,
        unit = null,
        progressPercentage = 0f,
        isSkipLocked = false,
        currentStreak = 0,
        longestStreak = 0,
        scorePercentage = 0,
        cadence = cadence
    )

    @Test
    fun `given mix of statuses when computing counts then pending excludes non-pending`() {
        val inputHabits = listOf(
            buildHabit(HabitStatus.PENDING),
            buildHabit(HabitStatus.COMPLETED),
            buildHabit(HabitStatus.SKIPPED),
            buildHabit(HabitStatus.FAILED),
            buildHabit(HabitStatus.SUSPENDED)
        )
        val actualCounts = inputHabits.computeCounts()
        assertEquals(1, actualCounts.pendingCount)
    }

    @Test
    fun `given suspended habits when computing counts then excluded from all counts`() {
        val inputHabits = listOf(
            buildHabit(HabitStatus.SUSPENDED, ScheduleType.DAILY),
            buildHabit(HabitStatus.SUSPENDED, ScheduleType.WEEKLY)
        )
        val actualCounts = inputHabits.computeCounts()
        assertEquals(0, actualCounts.pendingCount)
        assertEquals(0, actualCounts.dailyTotal)
        assertEquals(0, actualCounts.weeklyTotal)
    }

    @Test
    fun `given failed daily habit when computing counts then counted in dailyCompleted not pending`() {
        val inputHabits = listOf(buildHabit(HabitStatus.FAILED, ScheduleType.DAILY))
        val actualCounts = inputHabits.computeCounts()
        assertEquals(0, actualCounts.pendingCount)
        assertEquals(1, actualCounts.dailyCompleted)
        assertEquals(1, actualCounts.dailyTotal)
    }

    @Test
    fun `given mix of daily habits when computing counts then dailyCompleted matches completed skipped failed`() {
        val inputHabits = listOf(
            buildHabit(HabitStatus.PENDING, ScheduleType.DAILY),
            buildHabit(HabitStatus.COMPLETED, ScheduleType.DAILY),
            buildHabit(HabitStatus.SKIPPED, ScheduleType.DAILY),
            buildHabit(HabitStatus.FAILED, ScheduleType.DAILY),
            buildHabit(HabitStatus.SUSPENDED, ScheduleType.DAILY)
        )
        val actualCounts = inputHabits.computeCounts()
        assertEquals(1, actualCounts.pendingCount)
        assertEquals(3, actualCounts.dailyCompleted)
        assertEquals(4, actualCounts.dailyTotal)
    }

    @Test
    fun `given weekly habits when computing counts then weekly totals populated correctly`() {
        val inputHabits = listOf(
            buildHabit(HabitStatus.PENDING, ScheduleType.WEEKLY),
            buildHabit(HabitStatus.COMPLETED, ScheduleType.WEEKLY),
            buildHabit(HabitStatus.SUSPENDED, ScheduleType.WEEKLY)
        )
        val actualCounts = inputHabits.computeCounts()
        assertEquals(1, actualCounts.pendingCount)
        assertEquals(1, actualCounts.weeklyCompleted)
        assertEquals(2, actualCounts.weeklyTotal)
        assertEquals(0, actualCounts.dailyTotal)
    }

    @Test
    fun `given empty list when computing counts then all counts are zero`() {
        val inputHabits = emptyList<TodayHabitUiModel>()
        val actualCounts = inputHabits.computeCounts()
        assertEquals(TodayCounts(), actualCounts)
    }
}
