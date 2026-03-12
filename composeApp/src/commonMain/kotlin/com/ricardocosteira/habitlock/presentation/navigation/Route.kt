package com.ricardocosteira.habitlock.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation routes for the app.
 */
@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object OnboardingPhilosophy : Route

    @Serializable
    data object OnboardingStrictness : Route

    @Serializable
    data object OnboardingFirstHabit : Route

    @Serializable
    data object Today : Route

    @Serializable
    data class HabitDetail(val instanceId: String) : Route

    @Serializable
    data object CreateHabit : Route

    @Serializable
    data class EditHabit(val habitId: String) : Route

    @Serializable
    data object Calendar : Route

    @Serializable
    data object ArchivedHabits : Route

    @Serializable
    data object Settings : Route
}

