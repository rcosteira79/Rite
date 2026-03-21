package com.ricardocosteira.habitlock.presentation.ui.onboarding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class OnboardingStrictnessPresetTest {

    @Test
    fun `FLEXIBLE has correct collapsed summary`() {
        assertEquals(
            expected = "Undo: Unlimited · Snoozes: Unlimited",
            actual = OnboardingStrictnessPreset.FLEXIBLE.collapsedSummary
        )
    }

    @Test
    fun `BALANCED has correct collapsed summary`() {
        assertEquals(
            expected = "Undo: Within 5 min · Snoozes: 1/day",
            actual = OnboardingStrictnessPreset.BALANCED.collapsedSummary
        )
    }

    @Test
    fun `LOCKED has correct collapsed summary`() {
        assertEquals(
            expected = "No undo · Skips capped",
            actual = OnboardingStrictnessPreset.LOCKED.collapsedSummary
        )
    }

    @Test
    fun `BALANCED rules contain key-value pairs for Undo, Snoozes, Skips`() {
        val actualKeys = OnboardingStrictnessPreset.BALANCED.rules.map { it.key }
        assertEquals(expected = listOf("Undo", "Snoozes", "Skips"), actual = actualKeys)
    }

    @Test
    fun `BALANCED rules have correct values`() {
        val rules = OnboardingStrictnessPreset.BALANCED.rules
        assertEquals(expected = "Within 5 min", actual = rules[0].value)
        assertEquals(expected = "1 / day",       actual = rules[1].value)
        assertEquals(expected = "2 / month",     actual = rules[2].value)
    }

    @Test
    fun `FLEXIBLE rules all have value Unlimited`() {
        val rules = OnboardingStrictnessPreset.FLEXIBLE.rules
        assertTrue(rules.isNotEmpty())
        assertTrue(rules.all { it.value == "Unlimited" })
    }

    @Test
    fun `BALANCED is recommended, others are not`() {
        assertTrue(OnboardingStrictnessPreset.BALANCED.isRecommended)
        assertFalse(OnboardingStrictnessPreset.FLEXIBLE.isRecommended)
        assertFalse(OnboardingStrictnessPreset.LOCKED.isRecommended)
    }

    @Test
    fun `LOCKED rules contain key-value pairs for Undo, Snoozes, Skips`() {
        val rules = OnboardingStrictnessPreset.LOCKED.rules
        assertEquals(expected = listOf("Undo", "Snoozes", "Skips"), actual = rules.map { it.key })
        assertEquals(expected = "None",   actual = rules[0].value)
        assertEquals(expected = "Capped", actual = rules[1].value)
        assertEquals(expected = "Capped", actual = rules[2].value)
    }
}
