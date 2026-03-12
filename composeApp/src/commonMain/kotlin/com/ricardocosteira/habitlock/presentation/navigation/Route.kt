package com.ricardocosteira.habitlock.presentation.navigation

/**
 * Navigation routes for the app.
 */
sealed interface Route {

    data object OnboardingPhilosophy : Route

    data object OnboardingStrictness : Route

    data object OnboardingFirstHabit : Route

    data object Today : Route

    data class HabitDetail(val instanceId: String) : Route

    data object CreateHabit : Route

    data class EditHabit(val habitId: String) : Route

    data object Calendar : Route

    data object ArchivedHabits : Route

    data object Settings : Route
}

