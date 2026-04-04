package com.ricardocosteira.rite.presentation.ui.habit

import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.ScheduleType
import kotlinx.datetime.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HabitFormStateTest {
    @Test
    fun `given weekly schedule with no days selected when checking validity then isValid is false`() {
        // Given
        val inputState = HabitFormState(
            name = "Run",
            scheduleType = ScheduleType.WEEKLY,
            selectedDays = emptySet()
        )

        // When
        val actualIsValid = inputState.isValid

        // Then
        assertFalse(actualIsValid)
    }

    @Test
    fun `given weekly schedule with at least one day selected when checking validity then isValid is true`() {
        // Given
        val inputState = HabitFormState(
            name = "Run",
            scheduleType = ScheduleType.WEEKLY,
            selectedDays = setOf(DayOfWeek.MONDAY)
        )

        // When
        val actualIsValid = inputState.isValid

        // Then
        assertTrue(actualIsValid)
    }

    @Test
    fun `given daily schedule with no days selected when checking validity then isValid is true`() {
        // Given
        val inputState = HabitFormState(
            name = "Run",
            scheduleType = ScheduleType.DAILY,
            selectedDays = emptySet()
        )

        // When
        val actualIsValid = inputState.isValid

        // Then
        assertTrue(actualIsValid)
    }

    @Test
    fun `given valid quantitative state when checking validity then isValid is true`() {
        // Given
        val inputState = HabitFormState(
            name = "Run",
            type = HabitType.QUANTITATIVE,
            targetValue = "5",
            quota = "1",
            scheduleType = ScheduleType.DAILY,
            selectedDays = DayOfWeek.entries.toSet()
        )

        // When
        val actualIsValid = inputState.isValid

        // Then
        assertTrue(actualIsValid)
    }

    @Test
    fun `given quantitative state with zero target value when checking validity then isValid is false`() {
        // Given
        val inputState = HabitFormState(
            name = "Run",
            type = HabitType.QUANTITATIVE,
            targetValue = "0",
            quota = "1",
            scheduleType = ScheduleType.DAILY,
            selectedDays = DayOfWeek.entries.toSet()
        )

        // When
        val actualIsValid = inputState.isValid

        // Then
        assertFalse(actualIsValid)
    }
}
