package com.ricardocosteira.habitlock.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.presentation.ui.archived.ArchivedHabitsScreen
import com.ricardocosteira.habitlock.presentation.ui.calendar.CalendarScreen
import com.ricardocosteira.habitlock.presentation.ui.habit.HabitFormScreen
import com.ricardocosteira.habitlock.presentation.ui.onboarding.FirstHabitScreen
import com.ricardocosteira.habitlock.presentation.ui.onboarding.PhilosophyScreen
import com.ricardocosteira.habitlock.presentation.ui.onboarding.StrictnessScreen
import com.ricardocosteira.habitlock.presentation.ui.settings.SettingsScreen
import com.ricardocosteira.habitlock.presentation.ui.today.TodayScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private val savedStateConfig: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(OnboardingPhilosophy::class)
            subclass(OnboardingStrictness::class)
            subclass(OnboardingFirstHabit::class)
            subclass(Today::class)
            subclass(HabitDetail::class)
            subclass(CreateHabit::class)
            subclass(EditHabit::class)
            subclass(Calendar::class)
            subclass(ArchivedHabits::class)
            subclass(Settings::class)
        }
    }
}

@Composable
fun HabitLockNavigation(isOnboardingCompleted: Boolean) {
    val initialRoute: Route = if (isOnboardingCompleted) Today else OnboardingPhilosophy
    val backStack = rememberNavBackStack(savedStateConfig, initialRoute)
    val snackbarHostState = remember { SnackbarHostState() }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = Modifier.fillMaxSize(),
        entryProvider = entryProvider {
            entry<OnboardingPhilosophy> {
                PhilosophyScreen(
                    onNavigateToStrictness = { backStack.add(OnboardingStrictness) },
                    onNavigateToToday = {
                        backStack.clear()
                        backStack.add(Today)
                    },
                    snackbarHostState = snackbarHostState
                )
            }

            entry<OnboardingStrictness> {
                StrictnessScreen(
                    onNavigateToFirstHabit = { backStack.add(OnboardingFirstHabit) },
                    onNavigateToToday = {
                        backStack.clear()
                        backStack.add(Today)
                    },
                    snackbarHostState = snackbarHostState
                )
            }

            entry<OnboardingFirstHabit> {
                FirstHabitScreen(
                    onNavigateToToday = {
                        backStack.clear()
                        backStack.add(Today)
                    },
                    snackbarHostState = snackbarHostState
                )
            }

            entry<Today> {
                TodayScreen(
                    onCalendarClick = { backStack.add(Calendar) },
                    onSettingsClick = { backStack.add(Settings) },
                    onNavigateToHabitDetail = { backStack.add(HabitDetail(it)) },
                    onNavigateToCreateHabit = { backStack.add(CreateHabit) },
                    onEditHabit = { backStack.add(EditHabit(it)) },
                    snackbarHostState = snackbarHostState
                )
            }

            entry<Calendar> {
                CalendarScreen(onBackClick = backStack::removeLastOrNull)
            }

            entry<Settings> {
                SettingsScreen(
                    onBackClick = backStack::removeLastOrNull,
                    onArchivedHabitsClick = { backStack.add(ArchivedHabits) },
                    snackbarHostState = snackbarHostState
                )
            }

            entry<ArchivedHabits> {
                ArchivedHabitsScreen(
                    onBackClick = backStack::removeLastOrNull,
                    snackbarHostState = snackbarHostState
                )
            }

            entry<CreateHabit> {
                // Capture todayViewModel inside the @Composable lambda — required because
                // LocalAppComponent.current cannot be called from a non-composable callback.
                val todayViewModel = LocalAppComponent.current.todayViewModel
                HabitFormScreen(
                    habitIdToEdit = null,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                        todayViewModel.loadTodayHabits()
                    },
                    snackbarHostState = snackbarHostState
                )
            }

            entry<EditHabit> { route ->
                val todayViewModel = LocalAppComponent.current.todayViewModel
                HabitFormScreen(
                    habitIdToEdit = route.habitId,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                        todayViewModel.loadTodayHabits()
                    },
                    snackbarHostState = snackbarHostState
                )
            }

            entry<HabitDetail> {
                // TODO: Implement habit detail screen
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    backStack.removeLastOrNull()
                }
            }
        }
    )
}
