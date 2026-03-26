package com.ricardocosteira.habitlock.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.presentation.ui.archived.ArchivedHabitsScreen
import com.ricardocosteira.habitlock.presentation.ui.calendar.CalendarScreen
import com.ricardocosteira.habitlock.presentation.ui.habit.HabitFormScreen
import com.ricardocosteira.habitlock.presentation.ui.onboarding.OnboardingRoute
import com.ricardocosteira.habitlock.presentation.ui.settings.SettingsScreen
import com.ricardocosteira.habitlock.presentation.ui.today.TodayScreen
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.today_cd_add_habit
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.stringResource

private val savedStateConfig: SavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
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

private val topLevelRoutes: Set<Route> = setOf(Today, Calendar, Settings)

@Composable
fun HabitLockNavigation(isOnboardingCompleted: Boolean) {
    val initialRoute: Route = if (isOnboardingCompleted) Today else Onboarding
    val backStack = rememberNavBackStack(savedStateConfig, initialRoute)
    val snackbarHostState = remember { SnackbarHostState() }
    val appComponent = LocalAppComponent.current

    val currentTab: BottomNavTab? by remember {
        derivedStateOf {
            when (backStack.lastOrNull()) {
                is Today -> BottomNavTab.TODAY
                is Calendar -> BottomNavTab.HISTORY
                is Settings -> BottomNavTab.SETTINGS
                else -> null
            }
        }
    }

    val isTopLevel: Boolean by remember {
        derivedStateOf { backStack.lastOrNull() as? Route in topLevelRoutes }
    }
    val isTodayRoute: Boolean by remember {
        derivedStateOf { backStack.lastOrNull() is Today }
    }

    val isNavBarVisible: MutableState<Boolean> = remember { mutableStateOf(true) }
    val nestedScrollConnection: NestedScrollConnection =
        remember {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    if (available.y < -1f) isNavBarVisible.value = false
                    if (available.y > 1f) isNavBarVisible.value = true
                    return Offset.Zero
                }
            }
        }

    Scaffold(
        bottomBar = {
            if (isTopLevel && currentTab != null) {
                AnimatedVisibility(
                    visible = isNavBarVisible.value,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                ) {
                    HabitLockBottomNav(
                        currentTab = currentTab!!,
                        onTabSelected = { tab ->
                            handleTabSelection(tab, backStack)
                        },
                    )
                }
            }
        },
        floatingActionButton = {
            if (isTodayRoute) {
                FloatingActionButton(onClick = { backStack.add(CreateHabit) }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.today_cd_add_habit),
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { scaffoldPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
                    .nestedScroll(nestedScrollConnection),
            entryProvider =
                entryProvider {
                    entry<Onboarding> {
                        OnboardingRoute(
                            viewModel = appComponent.onboardingViewModel,
                            snackbarHostState = snackbarHostState,
                            onFinished = {
                                backStack.clear()
                                backStack.add(Today)
                            },
                        )
                    }

                    entry<Today> {
                        TodayScreen(
                            onNavigateToHabitDetail = { backStack.add(HabitDetail(it)) },
                            onNavigateToCreateHabit = { backStack.add(CreateHabit) },
                            onEditHabit = { backStack.add(EditHabit(it)) },
                            snackbarHostState = snackbarHostState,
                        )
                    }

                    entry<Calendar> {
                        CalendarScreen(onBackClick = backStack::removeLastOrNull)
                    }

                    entry<Settings> {
                        SettingsScreen(
                            onBackClick = backStack::removeLastOrNull,
                            onArchivedHabitsClick = { backStack.add(ArchivedHabits) },
                            snackbarHostState = snackbarHostState,
                        )
                    }

                    entry<ArchivedHabits> {
                        ArchivedHabitsScreen(
                            onBackClick = backStack::removeLastOrNull,
                            snackbarHostState = snackbarHostState,
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
                            snackbarHostState = snackbarHostState,
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
                            snackbarHostState = snackbarHostState,
                        )
                    }

                    entry<HabitDetail> {
                        // TODO: Implement habit detail screen
                        LaunchedEffect(Unit) {
                            backStack.removeLastOrNull()
                        }
                    }
                },
        )
    }
}

private fun handleTabSelection(
    tab: BottomNavTab,
    backStack: MutableList<NavKey>,
) {
    when (tab) {
        BottomNavTab.TODAY -> {
            // Pop back to Today (remove everything above it)
            while (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        }

        BottomNavTab.HISTORY -> {
            // Replace current top-level with Calendar
            backStack.removeLastOrNull()
            backStack.add(Calendar)
        }

        BottomNavTab.SETTINGS -> {
            // Replace current top-level with Settings
            backStack.removeLastOrNull()
            backStack.add(Settings)
        }
    }
}
