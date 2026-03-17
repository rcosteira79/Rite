package com.ricardocosteira.habitlock.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey

@Serializable
data object Onboarding : Route

@Serializable
data object Today : Route

@Serializable
data object CreateHabit : Route

@Serializable
data class EditHabit(val habitId: String) : Route

@Serializable
data class HabitDetail(val instanceId: String) : Route

@Serializable
data object Calendar : Route

@Serializable
data object ArchivedHabits : Route

@Serializable
data object Settings : Route

