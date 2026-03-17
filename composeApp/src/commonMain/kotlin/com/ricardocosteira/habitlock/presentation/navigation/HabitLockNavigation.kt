package com.ricardocosteira.habitlock.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.presentation.ui.archived.ArchivedHabitsEvent
import com.ricardocosteira.habitlock.presentation.ui.archived.ArchivedHabitsScreen
import com.ricardocosteira.habitlock.presentation.ui.calendar.CalendarScreen
import com.ricardocosteira.habitlock.presentation.ui.habit.HabitFormScreen
import com.ricardocosteira.habitlock.presentation.ui.settings.SettingsEvent
import com.ricardocosteira.habitlock.presentation.ui.settings.SettingsScreen
import com.ricardocosteira.habitlock.presentation.ui.today.TodayEvent
import com.ricardocosteira.habitlock.presentation.ui.today.TodayScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private val savedStateConfig: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Onboarding::class)
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
    val initialRoute: Route = if (isOnboardingCompleted) Today else Onboarding
    val backStack = rememberNavBackStack(savedStateConfig, initialRoute)
    val snackbarHostState = remember { SnackbarHostState() }
    val appComponent = LocalAppComponent.current

    LaunchedEffect(Unit) {
        appComponent.todayViewModel.events.collect { event ->
            when (event) {
                is TodayEvent.NavigateToHabitDetail ->
                    backStack.add(HabitDetail(event.instanceId))
                TodayEvent.NavigateToCreateHabit -> backStack.add(CreateHabit)
                is TodayEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is TodayEvent.ShowSuccess -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    LaunchedEffect(Unit) {
        appComponent.settingsViewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ShowSuccess -> snackbarHostState.showSnackbar(event.message)
                is SettingsEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    LaunchedEffect(Unit) {
        appComponent.archivedHabitsViewModel.events.collect { event ->
            when (event) {
                is ArchivedHabitsEvent.ShowSuccess -> snackbarHostState.showSnackbar(event.message)
                is ArchivedHabitsEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }


    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = Modifier.fillMaxSize(),
        entryProvider = entryProvider {
            entry<Onboarding> {
                // TODO: replace with OnboardingRoute in Task 10
                androidx.compose.material3.Text("Onboarding coming soon")
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
                LaunchedEffect(Unit) {
                    backStack.removeLastOrNull()
                }
            }
        }
    )
}
