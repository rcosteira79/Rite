package com.ricardocosteira.rite.presentation.ui.habit

import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.ScheduleType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek

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

    @Test
    fun `given default state when checking defaultIncrement then it is 1`() {
        // Given
        val inputState = HabitFormState()

        // When
        val actualDefaultIncrement = inputState.defaultIncrement

        // Then
        assertTrue(actualDefaultIncrement == "1")
    }

    @Test
    fun `given state with custom defaultIncrement when checking value then it is preserved`() {
        // Given
        val inputState = HabitFormState(
            name = "Drink Water",
            type = HabitType.QUANTITATIVE,
            targetValue = "2000",
            unit = "mL",
            defaultIncrement = "500"
        )

        // When
        val actualDefaultIncrement = inputState.defaultIncrement

        // Then
        assertTrue(actualDefaultIncrement == "500")
    }
}
