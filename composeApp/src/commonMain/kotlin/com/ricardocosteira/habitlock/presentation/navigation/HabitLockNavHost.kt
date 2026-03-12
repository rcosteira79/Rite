package com.ricardocosteira.habitlock.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import com.ricardocosteira.habitlock.presentation.ui.archived.ArchivedHabitsEvent
import com.ricardocosteira.habitlock.presentation.ui.archived.ArchivedHabitsScreen
import com.ricardocosteira.habitlock.presentation.ui.archived.ArchivedHabitsViewModel
import com.ricardocosteira.habitlock.presentation.ui.calendar.CalendarScreen
import com.ricardocosteira.habitlock.presentation.ui.calendar.CalendarViewModel
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

private val navConfig: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.OnboardingPhilosophy::class)
            subclass(Route.OnboardingStrictness::class)
            subclass(Route.OnboardingFirstHabit::class)
            subclass(Route.Today::class)
            subclass(Route.HabitDetail::class)
            subclass(Route.CreateHabit::class)
            subclass(Route.EditHabit::class)
            subclass(Route.Calendar::class)
            subclass(Route.ArchivedHabits::class)
            subclass(Route.Settings::class)
        }
    }
}

/**
 * Main navigation host for the app using Navigation 3 (org.jetbrains.androidx.navigation3).
 */
@Composable
fun HabitLockNavHost(
    isOnboardingCompleted: Boolean,
    onboardingViewModel: OnboardingViewModel,
    todayViewModel: TodayViewModel,
    calendarViewModel: CalendarViewModel,
    settingsViewModel: SettingsViewModel,
    archivedHabitsViewModel: ArchivedHabitsViewModel,
    createHabitFormViewModel: (String?) -> HabitFormViewModel
) {
    val initialRoute: Route = if (isOnboardingCompleted) Route.Today else Route.OnboardingPhilosophy
    val backStack = rememberNavBackStack(navConfig, initialRoute)

    val snackbarHostState = remember { SnackbarHostState() }

    // Collect all VM events at the top level so they are never missed due to route changes
    LaunchedEffect(Unit) {
        onboardingViewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.NavigateToStrictness -> backStack.add(Route.OnboardingStrictness)
                OnboardingEvent.NavigateToFirstHabit -> backStack.add(Route.OnboardingFirstHabit)
                OnboardingEvent.NavigateToToday -> {
                    backStack.clear()
                    backStack.add(Route.Today)
                }
                is OnboardingEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    LaunchedEffect(Unit) {
        todayViewModel.events.collect { event ->
            when (event) {
                is TodayEvent.NavigateToHabitDetail ->
                    snackbarHostState.showSnackbar("Habit detail view coming soon")
                TodayEvent.NavigateToCreateHabit -> backStack.add(Route.CreateHabit)
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

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = Modifier.fillMaxSize(),
        entryProvider = entryProvider {
            entry<Route.OnboardingPhilosophy> {
                PhilosophyScreen(
                    onContinue = { onboardingViewModel.continueFromPhilosophy() },
                    onSkip = { onboardingViewModel.skipToToday() }
                )
            }

            entry<Route.OnboardingStrictness> {
                val state by onboardingViewModel.state.collectAsStateWithLifecycle()
                StrictnessScreen(
                    selectedPreset = state.selectedPreset,
                    isLoading = state.isApplyingPreset,
                    onPresetSelected = { onboardingViewModel.selectPreset(it) },
                    onContinue = { onboardingViewModel.continueFromStrictness() },
                    onSkip = { onboardingViewModel.skipToToday() }
                )
            }

            entry<Route.OnboardingFirstHabit> {
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

            entry<Route.Today> {
                val state by todayViewModel.state.collectAsStateWithLifecycle()

                TodayScreen(
                    state = state,
                    onCalendarClick = { backStack.add(Route.Calendar) },
                    onSettingsClick = { backStack.add(Route.Settings) },
                    onHabitClick = { todayViewModel.navigateToHabitDetail(it) },
                    onCompleteClick = { todayViewModel.completeHabit(it) },
                    onSkipClick = { todayViewModel.skipHabit(it) },
                    onUndoClick = { todayViewModel.undoHabit(it) },
                    onEditClick = { habitId -> backStack.add(Route.EditHabit(habitId)) },
                    onArchiveClick = { todayViewModel.archiveHabit(it) },
                    onAddHabitClick = { todayViewModel.navigateToCreateHabit() },
                    onDismissTimezoneWarning = { todayViewModel.dismissTimezoneWarning() },
                    snackbarHostState = snackbarHostState
                )

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

            entry<Route.Calendar> {
                val state by calendarViewModel.state.collectAsStateWithLifecycle()

                CalendarScreen(
                    state = state,
                    onBackClick = { backStack.removeLastOrNull() },
                    onPreviousMonth = { calendarViewModel.previousMonth() },
                    onNextMonth = { calendarViewModel.nextMonth() },
                    onDayClick = { calendarViewModel.selectDay(it.date) }
                )
            }

            entry<Route.Settings> {
                val state by settingsViewModel.state.collectAsStateWithLifecycle()

                SettingsScreen(
                    state = state,
                    snackbarHostState = snackbarHostState,
                    onBackClick = { backStack.removeLastOrNull() },
                    onUndoPolicyChange = { settingsViewModel.updateUndoPolicy(it) },
                    onMaxSnoozeDurationChange = { settingsViewModel.updateMaxSnoozeDuration(it) },
                    onMaxSnoozesPerDayChange = { settingsViewModel.updateMaxSnoozesPerDay(it) },
                    onMaxConsecutiveSkipsChange = { settingsViewModel.updateMaxConsecutiveSkips(it) },
                    onArchivedHabitsClick = { backStack.add(Route.ArchivedHabits) }
                )
            }

            entry<Route.ArchivedHabits> {
                val state by archivedHabitsViewModel.state.collectAsStateWithLifecycle()

                ArchivedHabitsScreen(
                    state = state,
                    snackbarHostState = snackbarHostState,
                    onBackClick = { backStack.removeLastOrNull() },
                    onUnarchiveClick = { archivedHabitsViewModel.unarchiveHabit(it) },
                    onDeleteClick = { archivedHabitsViewModel.deleteHabit(it) }
                )
            }

            entry<Route.CreateHabit> {
                val viewModel = remember { createHabitFormViewModel(null) }
                val state by viewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(viewModel) {
                    viewModel.events.collect { event ->
                        when (event) {
                            HabitFormEvent.NavigateBack -> {
                                backStack.removeLastOrNull()
                                todayViewModel.loadTodayHabits()
                            }
                            is HabitFormEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                        }
                    }
                }

                HabitFormScreen(
                    state = state,
                    onBackClick = { backStack.removeLastOrNull() },
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

            entry<Route.EditHabit> { route ->
                val viewModel = remember(route.habitId) { createHabitFormViewModel(route.habitId) }
                val state by viewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(viewModel) {
                    viewModel.events.collect { event ->
                        when (event) {
                            HabitFormEvent.NavigateBack -> {
                                backStack.removeLastOrNull()
                                todayViewModel.loadTodayHabits()
                            }
                            is HabitFormEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                        }
                    }
                }

                HabitFormScreen(
                    state = state,
                    onBackClick = { backStack.removeLastOrNull() },
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

            entry<Route.HabitDetail> {
                // TODO: Implement habit detail screen
                LaunchedEffect(Unit) {
                    backStack.removeLastOrNull()
                }
            }
        }
    )
}
