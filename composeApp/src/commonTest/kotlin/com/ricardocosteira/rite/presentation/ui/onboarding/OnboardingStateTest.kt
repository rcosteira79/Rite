package com.ricardocosteira.rite.presentation.ui.onboarding

import kotlin.test.Test
import kotlin.test.assertEquals

class OnboardingStateTest {

    @Test
    fun `given showNotificationStep is false, totalSteps returns 3`() {
        val state = OnboardingState(showNotificationStep = false)
        assertEquals(3, state.totalSteps)
    }

    @Test
    fun `given showNotificationStep is true, totalSteps returns 4`() {
        val state = OnboardingState(showNotificationStep = true)
        assertEquals(4, state.totalSteps)
    }

    @Test
    fun `firstHabitStepIndex is always 2 (third step) regardless of notification step`() {
        assertEquals(2, OnboardingState(showNotificationStep = false).firstHabitStepIndex)
        assertEquals(2, OnboardingState(showNotificationStep = true).firstHabitStepIndex)
    }

    @Test
    fun `given showNotificationStep is true, notificationStepIndex returns 3 (fourth step)`() {
        val state = OnboardingState(showNotificationStep = true)
        assertEquals(3, state.notificationStepIndex)
    }
}
