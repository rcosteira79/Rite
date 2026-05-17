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
    fun `each preset exposes exactly four rule resources`() {
        OnboardingStrictnessPreset.entries.forEach { preset ->
            assertEquals(
                expected = 4,
                actual = preset.ruleResources.size,
                message = "Expected 4 rule resources for $preset"
            )
        }
    }
}
