package com.ricardocosteira.rite.presentation.ui.onboarding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingStrictnessPresetTest {

    @Test
    fun `BALANCED is recommended, others are not`() {
        assertTrue(OnboardingStrictnessPreset.BALANCED.isRecommended)
        assertFalse(OnboardingStrictnessPreset.FLEXIBLE.isRecommended)
        assertFalse(OnboardingStrictnessPreset.UNWAVERING.isRecommended)
    }

    @Test
    fun `FLEXIBLE has correct label`() {
        assertEquals(expected = "Flexible", actual = OnboardingStrictnessPreset.FLEXIBLE.label)
    }

    @Test
    fun `BALANCED has correct label`() {
        assertEquals(expected = "Balanced", actual = OnboardingStrictnessPreset.BALANCED.label)
    }

    @Test
    fun `UNWAVERING has correct label`() {
        assertEquals(expected = "Unwavering", actual = OnboardingStrictnessPreset.UNWAVERING.label)
    }

    @Test
    fun `each preset has a non-blank description`() {
        OnboardingStrictnessPreset.entries.forEach { preset ->
            assertTrue(
                preset.description.isNotBlank(),
                "Expected non-blank description for $preset"
            )
        }
    }

    @Test
    fun `each preset has exactly four rules`() {
        OnboardingStrictnessPreset.entries.forEach { preset ->
            assertEquals(
                expected = 4,
                actual = preset.rules.size,
                message = "Expected 4 rules for $preset"
            )
        }
    }

    @Test
    fun `each preset rule is non-blank`() {
        OnboardingStrictnessPreset.entries.forEach { preset ->
            preset.rules.forEach { rule ->
                assertTrue(rule.isNotBlank(), "Expected non-blank rule in $preset")
            }
        }
    }

    @Test
    fun `FLEXIBLE rules mention undo, snoozes, skips`() {
        val rules = OnboardingStrictnessPreset.FLEXIBLE.rules
        assertTrue(rules.any { it.startsWith("Undo:") })
        assertTrue(rules.any { it.startsWith("Snoozes:") })
        assertTrue(rules.any { it.startsWith("Skips:") })
    }

    @Test
    fun `BALANCED rules mention undo, snoozes, skips`() {
        val rules = OnboardingStrictnessPreset.BALANCED.rules
        assertTrue(rules.any { it.startsWith("Undo:") })
        assertTrue(rules.any { it.startsWith("Snoozes:") })
        assertTrue(rules.any { it.startsWith("Skips:") })
    }

    @Test
    fun `UNWAVERING rules mention undo, snoozes, skips`() {
        val rules = OnboardingStrictnessPreset.UNWAVERING.rules
        assertTrue(rules.any { it.startsWith("Undo:") })
        assertTrue(rules.any { it.startsWith("Snoozes:") })
        assertTrue(rules.any { it.startsWith("Skips:") })
    }
}
