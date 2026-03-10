package com.ricardocosteira.habitlock.presentation.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.presentation.ui.archived.ArchivedHabitsEvent
import com.ricardocosteira.habitlock.presentation.ui.archived.ArchivedHabitsScreen
import com.ricardocosteira.habitlock.presentation.ui.archived.ArchivedHabitsViewModel
import com.ricardocosteira.habitlock.presentation.ui.calendar.CalendarScreen
import com.ricardocosteira.habitlock.presentation.ui.calendar.CalendarViewModel
import com.ricardocosteira.habitlock.presentation.ui.components.AppNavigationDrawer
import com.ricardocosteira.habitlock.presentation.ui.components.DrawerDestination
import com.ricardocosteira.habitlock.presentation.ui.habit.HabitFormEvent
import com.ricardocosteira.habitlock.presentation.ui.habit.HabitFormScreen
import com.ricardocosteira.habitlock.presentation.ui.habit.HabitFormViewModel
import com.ricardocosteira.habitlock.presentation.ui.onboarding.FirstHabitScreen
import com.ricardocosteira.habitlock.presentation.ui.onboarding.OnboardingEvent
import com.ricardocosteira.habitlock.presentation.ui.onboarding.OnboardingViewModel
import com.ricardocosteira.habitlock.presentation.ui.onboarding.PhilosophyScreen
import com.ricardocosteira.habitlock.presentation.ui.onboarding.StrictnessScreen
import com.ricardocosteira.habitlock.presentation.ui.settings.SettingsEvent
import com.ricardocosteira.habitlock.presentation.ui.settings.SettingsScreen
import com.ricardocosteira.habitlock.presentation.ui.settings.SettingsViewModel
import com.ricardocosteira.habitlock.presentation.ui.today.QuantitativeInputBottomSheet
import com.ricardocosteira.habitlock.presentation.ui.today.TodayEvent
import com.ricardocosteira.habitlock.presentation.ui.today.TodayScreen
import com.ricardocosteira.habitlock.presentation.ui.today.TodayViewModel
import kotlinx.coroutines.launch

/**
 * Main navigation host for the app.
 * For now, this uses manual navigation state since Navigation 3 setup with Metro DI
 * requires additional configuration. This can be refactored to use Navigation 3 NavHost.
 */
@Composable
fun HabitLockNavHost(
    isOnboardingCompleted: Boolean,
    onboardingViewModel: OnboardingViewModel,
    todayViewModel: TodayViewModel,
    calendarViewModel: CalendarViewModel,
    settingsViewModel: SettingsViewModel,
    archivedHabitsViewModel: ArchivedHabitsViewModel,
    createHabitFormViewModel: (String?) -> HabitFormViewModel,
    modifier: Modifier = Modifier
) {
    var currentRoute by remember {
        mutableStateOf<Route>(
            if (isOnboardingCompleted) Route.Today else Route.OnboardingPhilosophy
        )
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Derive drawer selection from route — never mutate during composition
    val selectedDrawerDestination = when (currentRoute) {
        Route.Calendar -> DrawerDestination.CALENDAR
        Route.Settings -> DrawerDestination.SETTINGS
        else -> DrawerDestination.TODAY
    }

    // Collect all VM events at the top level so they are never missed due to route changes
    LaunchedEffect(Unit) {
        onboardingViewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.NavigateToStrictness -> currentRoute = Route.OnboardingStrictness
                OnboardingEvent.NavigateToFirstHabit -> currentRoute = Route.OnboardingFirstHabit
                OnboardingEvent.NavigateToToday -> currentRoute = Route.Today
                is OnboardingEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    LaunchedEffect(Unit) {
        todayViewModel.events.collect { event ->
            when (event) {
                is TodayEvent.NavigateToHabitDetail ->
                    snackbarHostState.showSnackbar("Habit detail view coming soon")
                TodayEvent.NavigateToCreateHabit -> currentRoute = Route.CreateHabit
                is TodayEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is TodayEvent.ShowSuccess -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    LaunchedEffect(Unit) {
        settingsViewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ShowSuccess -> snackbarHostState.showSnackbar(event.message)
                is SettingsEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    LaunchedEffect(Unit) {
        archivedHabitsViewModel.events.collect { event ->
            when (event) {
                is ArchivedHabitsEvent.ShowSuccess -> snackbarHostState.showSnackbar(event.message)
                is ArchivedHabitsEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    when (val route = currentRoute) {
        // Onboarding screens
        Route.OnboardingPhilosophy, Route.Onboarding -> {
            PhilosophyScreen(
                onContinue = { onboardingViewModel.continueFromPhilosophy() },
                onSkip = { onboardingViewModel.skipToToday() }
            )
        }

        Route.OnboardingStrictness -> {
            val state by onboardingViewModel.state.collectAsStateWithLifecycle()
            StrictnessScreen(
                selectedPreset = state.selectedPreset,
                isLoading = state.isApplyingPreset,
                onPresetSelected = { onboardingViewModel.selectPreset(it) },
                onContinue = { onboardingViewModel.continueFromStrictness() },
                onSkip = { onboardingViewModel.skipToToday() }
            )
        }

        Route.OnboardingFirstHabit -> {
            val state by onboardingViewModel.state.collectAsStateWithLifecycle()
            FirstHabitScreen(
                habitName = state.habitName,
                habitType = state.habitType,
                targetValue = state.targetValue,
                unit = state.unit,
                isLoading = state.isCreatingHabit,
                onHabitNameChange = { onboardingViewModel.updateHabitName(it) },
                onHabitTypeChange = { onboardingViewModel.updateHabitType(it) },
                onTargetValueChange = { onboardingViewModel.updateTargetValue(it) },
                onUnitChange = { onboardingViewModel.updateUnit(it) },
                onCreateHabit = { onboardingViewModel.createFirstHabit() },
                onSkip = { onboardingViewModel.skipFirstHabit() }
            )
        }

        // Main screens with drawer
        Route.Today -> {
            val state by todayViewModel.state.collectAsStateWithLifecycle()

            AppNavigationDrawer(
                drawerState = drawerState,
                selectedDestination = selectedDrawerDestination,
                onDestinationClick = { destination ->
                    scope.launch { drawerState.close() }
                    currentRoute = when (destination) {
                        DrawerDestination.TODAY -> Route.Today
                        DrawerDestination.CALENDAR -> Route.Calendar
                        DrawerDestination.SETTINGS -> Route.Settings
                    }
                }
            ) {
                TodayScreen(
                    state = state,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onHabitClick = { todayViewModel.navigateToHabitDetail(it) },
                    onCompleteClick = { todayViewModel.completeHabit(it) },
                    onSkipClick = { todayViewModel.skipHabit(it) },
                    onUndoClick = { todayViewModel.undoHabit(it) },
                    onEditClick = { habitId -> currentRoute = Route.EditHabit(habitId) },
                    onArchiveClick = { todayViewModel.archiveHabit(it) },
                    onAddHabitClick = { todayViewModel.navigateToCreateHabit() },
                    onDismissTimezoneWarning = { todayViewModel.dismissTimezoneWarning() },
                    snackbarHostState = snackbarHostState
                )

                // Show quantitative input bottom sheet if needed
                state.showQuantitativeInputFor?.let { instanceId ->
                    val habit = state.habits.find { it.instanceId == instanceId }
                    if (habit != null) {
                        QuantitativeInputBottomSheet(
                            habit = habit,
                            onConfirm = { value ->
                                todayViewModel.completeQuantitativeHabit(instanceId, value)
                            },
                            onDismiss = { todayViewModel.dismissQuantitativeInput() }
                        )
                    }
                }
            }
        }

        Route.Calendar -> {
            val state by calendarViewModel.state.collectAsStateWithLifecycle()

            CalendarScreen(
                state = state,
                onBackClick = { currentRoute = Route.Today },
                onPreviousMonth = { calendarViewModel.previousMonth() },
                onNextMonth = { calendarViewModel.nextMonth() },
                onDayClick = { calendarViewModel.selectDay(it.date) }
            )
        }

        Route.Settings -> {
            val state by settingsViewModel.state.collectAsStateWithLifecycle()

            SettingsScreen(
                state = state,
                snackbarHostState = snackbarHostState,
                onBackClick = { currentRoute = Route.Today },
                onUndoPolicyChange = { settingsViewModel.updateUndoPolicy(it) },
                onMaxSnoozeDurationChange = { settingsViewModel.updateMaxSnoozeDuration(it) },
                onMaxSnoozesPerDayChange = { settingsViewModel.updateMaxSnoozesPerDay(it) },
                onMaxConsecutiveSkipsChange = { settingsViewModel.updateMaxConsecutiveSkips(it) },
                onArchivedHabitsClick = { currentRoute = Route.ArchivedHabits }
            )
        }

        Route.ArchivedHabits -> {
            val state by archivedHabitsViewModel.state.collectAsStateWithLifecycle()

            ArchivedHabitsScreen(
                state = state,
                snackbarHostState = snackbarHostState,
                onBackClick = { currentRoute = Route.Settings },
                onUnarchiveClick = { archivedHabitsViewModel.unarchiveHabit(it) },
                onDeleteClick = { archivedHabitsViewModel.deleteHabit(it) }
            )
        }

        Route.CreateHabit -> {
            val viewModel = remember { createHabitFormViewModel(null) }
            val state by viewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(viewModel) {
                viewModel.events.collect { event ->
                    when (event) {
                        HabitFormEvent.NavigateBack -> {
                            currentRoute = Route.Today
                            todayViewModel.loadTodayHabits()
                        }
                        is HabitFormEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                    }
                }
            }

            HabitFormScreen(
                state = state,
                onBackClick = { currentRoute = Route.Today },
                onNameChange = { viewModel.updateName(it) },
                onDescriptionChange = { viewModel.updateDescription(it) },
                onTypeChange = { viewModel.updateType(it) },
                onTargetValueChange = { viewModel.updateTargetValue(it) },
                onUnitChange = { viewModel.updateUnit(it) },
                onScheduleTypeChange = { viewModel.updateScheduleType(it) },
                onQuotaChange = { viewModel.updateQuota(it) },
                onHasReminderChange = { viewModel.updateHasReminder(it) },
                onReminderTypeChange = { viewModel.updateReminderType(it) },
                onIntervalChange = { viewModel.updateIntervalMinutes(it) },
                onSaveClick = { viewModel.saveHabit() },
                onDeleteClick = { viewModel.deleteHabit() }
            )
        }

        is Route.EditHabit -> {
            val viewModel = remember(route.habitId) { createHabitFormViewModel(route.habitId) }
            val state by viewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(viewModel) {
                viewModel.events.collect { event ->
                    when (event) {
                        HabitFormEvent.NavigateBack -> {
                            currentRoute = Route.Today
                            todayViewModel.loadTodayHabits()
                        }
                        is HabitFormEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                    }
                }
            }

            HabitFormScreen(
                state = state,
                onBackClick = { currentRoute = Route.Today },
                onNameChange = { viewModel.updateName(it) },
                onDescriptionChange = { viewModel.updateDescription(it) },
                onTypeChange = { viewModel.updateType(it) },
                onTargetValueChange = { viewModel.updateTargetValue(it) },
                onUnitChange = { viewModel.updateUnit(it) },
                onScheduleTypeChange = { viewModel.updateScheduleType(it) },
                onQuotaChange = { viewModel.updateQuota(it) },
                onHasReminderChange = { viewModel.updateHasReminder(it) },
                onReminderTypeChange = { viewModel.updateReminderType(it) },
                onIntervalChange = { viewModel.updateIntervalMinutes(it) },
                onSaveClick = { viewModel.saveHabit() },
                onDeleteClick = { viewModel.deleteHabit() }
            )
        }

        is Route.HabitDetail -> {
            // TODO: Implement habit detail screen
            LaunchedEffect(route) {
                currentRoute = Route.Today
            }
        }
    }
}

