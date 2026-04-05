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
    fun `given showNotificationStep is false, firstHabitStepIndex returns 2`() {
        val state = OnboardingState(showNotificationStep = false)
        assertEquals(2, state.firstHabitStepIndex)
    }

    @Test
    fun `given showNotificationStep is true, firstHabitStepIndex returns 3`() {
        val state = OnboardingState(showNotificationStep = true)
        assertEquals(3, state.firstHabitStepIndex)
    }

    @Test
    fun `given showNotificationStep is true, notificationStepIndex returns 2`() {
        val state = OnboardingState(showNotificationStep = true)
        assertEquals(2, state.notificationStepIndex)
    }
}
