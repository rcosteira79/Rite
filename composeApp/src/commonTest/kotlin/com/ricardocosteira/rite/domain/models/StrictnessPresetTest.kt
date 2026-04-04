package com.ricardocosteira.rite.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StrictnessPresetTest {

    @Test
    fun `given FLEXIBLE preset when converting to settings then returns correct values`() {
        // Given
        val inputPreset = StrictnessPreset.FLEXIBLE

        // When
        val actualSettings = inputPreset.toUserSettings()

        // Then
        assertEquals(UndoPolicy.ALL_HISTORY, actualSettings.undoPolicy)
        assertNull(actualSettings.maxSnoozesPerHabitPerDay)
        assertNull(actualSettings.maxConsecutiveSkips)
        assertEquals(60, actualSettings.maxSnoozeDurationMinutes)
    }

    @Test
    fun `given BALANCED preset when converting to settings then returns correct values`() {
        // Given
        val inputPreset = StrictnessPreset.BALANCED

        // When
        val actualSettings = inputPreset.toUserSettings()

        // Then
        assertEquals(UndoPolicy.TODAY_ONLY, actualSettings.undoPolicy)
        assertEquals(3, actualSettings.maxSnoozesPerHabitPerDay)
        assertEquals(2, actualSettings.maxConsecutiveSkips)
        assertEquals(30, actualSettings.maxSnoozeDurationMinutes)
    }

    @Test
    fun `given UNWAVERING preset when converting to settings then returns correct values`() {
        // Given
        val inputPreset = StrictnessPreset.UNWAVERING

        // When
        val actualSettings = inputPreset.toUserSettings()

        // Then
        assertEquals(UndoPolicy.NONE, actualSettings.undoPolicy)
        assertEquals(1, actualSettings.maxSnoozesPerHabitPerDay)
        assertEquals(0, actualSettings.maxConsecutiveSkips)
        assertEquals(15, actualSettings.maxSnoozeDurationMinutes)
    }

    @Test
    fun `given default preset when accessed then returns BALANCED`() {
        // When
        val actualDefault = StrictnessPreset.DEFAULT

        // Then
        assertEquals(StrictnessPreset.BALANCED, actualDefault)
    }

    @Test
    fun `given FLEXIBLE settings when checking flags then returns correct values`() {
        // Given
        val inputSettings = StrictnessPreset.FLEXIBLE.toUserSettings()

        // When/Then
        assertFalse(inputSettings.isUndoDisabled)
        assertTrue(inputSettings.hasUnlimitedSnoozes)
        assertTrue(inputSettings.hasUnlimitedSkips)
        assertFalse(inputSettings.isSkipDisabled)
    }

    @Test
    fun `given BALANCED settings when checking flags then returns correct values`() {
        // Given
        val inputSettings = StrictnessPreset.BALANCED.toUserSettings()

        // When/Then
        assertFalse(inputSettings.isUndoDisabled)
        assertFalse(inputSettings.hasUnlimitedSnoozes)
        assertFalse(inputSettings.hasUnlimitedSkips)
        assertFalse(inputSettings.isSkipDisabled)
    }

    @Test
    fun `given UNWAVERING settings when checking flags then returns correct values`() {
        // Given
        val inputSettings = StrictnessPreset.UNWAVERING.toUserSettings()

        // When/Then
        assertTrue(inputSettings.isUndoDisabled)
        assertFalse(inputSettings.hasUnlimitedSnoozes)
        assertFalse(inputSettings.hasUnlimitedSkips)
        assertTrue(inputSettings.isSkipDisabled)
    }

    @Test
    fun `given settings with zero max snoozes when creating then throws exception`() {
        // When/Then
        try {
            UserStrictnessSettings(
                undoPolicy = UndoPolicy.NONE,
                maxSnoozesPerHabitPerDay = 0,
                maxConsecutiveSkips = 2,
                maxSnoozeDurationMinutes = 15
            )
            throw AssertionError("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Max snoozes must be positive", e.message)
        }
    }

    @Test
    fun `given settings with negative max skips when creating then throws exception`() {
        // When/Then
        try {
            UserStrictnessSettings(
                undoPolicy = UndoPolicy.NONE,
                maxSnoozesPerHabitPerDay = 1,
                maxConsecutiveSkips = -1,
                maxSnoozeDurationMinutes = 15
            )
            throw AssertionError("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Max consecutive skips must be non-negative", e.message)
        }
    }

    @Test
    fun `given settings with zero max snooze duration when creating then throws exception`() {
        // When/Then
        try {
            UserStrictnessSettings(
                undoPolicy = UndoPolicy.NONE,
                maxSnoozesPerHabitPerDay = 1,
                maxConsecutiveSkips = 2,
                maxSnoozeDurationMinutes = 0
            )
            throw AssertionError("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Max snooze duration must be positive", e.message)
        }
    }

    @Test
    fun `given FLEXIBLE preset when comparing limits then is most lenient`() {
        // Given
        val flexibleSettings = StrictnessPreset.FLEXIBLE.toUserSettings()
        val balancedSettings = StrictnessPreset.BALANCED.toUserSettings()
        val unwaveringSettings = StrictnessPreset.UNWAVERING.toUserSettings()

        // When/Then - Verify FLEXIBLE is most lenient
        assertTrue(flexibleSettings.hasUnlimitedSnoozes)
        assertTrue(flexibleSettings.hasUnlimitedSkips)
        assertEquals(60, flexibleSettings.maxSnoozeDurationMinutes)

        // Verify order of strictness for snooze duration
        assertTrue(flexibleSettings.maxSnoozeDurationMinutes > balancedSettings.maxSnoozeDurationMinutes)
        assertTrue(balancedSettings.maxSnoozeDurationMinutes > unwaveringSettings.maxSnoozeDurationMinutes)
    }

    @Test
    fun `given UNWAVERING preset when comparing limits then is most strict`() {
        // Given
        val unwaveringSettings = StrictnessPreset.UNWAVERING.toUserSettings()
        val balancedSettings = StrictnessPreset.BALANCED.toUserSettings()

        // When/Then - Verify LOCKED is most strict
        assertTrue(unwaveringSettings.isUndoDisabled)
        assertTrue(unwaveringSettings.isSkipDisabled)
        assertEquals(1, unwaveringSettings.maxSnoozesPerHabitPerDay)
        assertEquals(15, unwaveringSettings.maxSnoozeDurationMinutes)

        // Verify it's stricter than BALANCED
        val lockedSnoozes = unwaveringSettings.maxSnoozesPerHabitPerDay ?: Int.MAX_VALUE
        val balancedSnoozes = balancedSettings.maxSnoozesPerHabitPerDay ?: Int.MAX_VALUE
        assertTrue(lockedSnoozes < balancedSnoozes)
    }
}
