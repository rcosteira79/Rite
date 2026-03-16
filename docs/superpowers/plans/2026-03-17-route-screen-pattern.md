# Route/Screen Pattern Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor each screen file to own its ViewModel retrieval, state collection, and event handling via a public outer Route composable, leaving `HabitLockNavigation` as back-stack management only.

**Architecture:** Each `XxxScreen.kt` gets a public outer overload (the Route) that reads the ViewModel from `LocalAppComponent`, collects state, handles events via `LaunchedEffect`, and calls the private stateless inner Screen. `HabitLockNavigation` shrinks to `savedStateConfig` + `backStack` + `entryProvider` entries that call Routes with navigation lambdas.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Navigation 3 (`androidx.navigation3`), kotlin-inject (`me.tatarka.inject`)

**Spec:** `docs/superpowers/specs/2026-03-17-route-screen-pattern-design.md`

---

## Chunk 1: DI layer + onboarding screens + TodayScreen

### Task 1: Create `LocalAppComponent` CompositionLocal

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/di/LocalAppComponent.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/App.kt`

- [ ] **Step 1: Create `LocalAppComponent.kt`**

```kotlin
package com.ricardocosteira.habitlock.di

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppComponent = staticCompositionLocalOf<HabitLockAppComponent> {
    error("No HabitLockAppComponent provided")
}
```

- [ ] **Step 2: Update `App.kt` to provide `LocalAppComponent`**

Wrap the `HabitLockNavigation(...)` call inside `CompositionLocalProvider`. Keep all existing VM args on `HabitLockNavigation` for now — they will be removed in Task 10.

The `is StartupState.Ready ->` branch changes from:
```kotlin
is StartupState.Ready -> HabitLockNavigation(
    isOnboardingCompleted = currentState.isOnboardingCompleted,
    onboardingViewModel = appComponent.onboardingViewModel,
    todayViewModel = appComponent.todayViewModel,
    calendarViewModel = appComponent.calendarViewModel,
    settingsViewModel = appComponent.settingsViewModel,
    archivedHabitsViewModel = appComponent.archivedHabitsViewModel,
    createHabitFormViewModel = appComponent.habitFormViewModelFactory::create
)
```
to:
```kotlin
is StartupState.Ready -> CompositionLocalProvider(LocalAppComponent provides appComponent) {
    HabitLockNavigation(
        isOnboardingCompleted = currentState.isOnboardingCompleted,
        onboardingViewModel = appComponent.onboardingViewModel,
        todayViewModel = appComponent.todayViewModel,
        calendarViewModel = appComponent.calendarViewModel,
        settingsViewModel = appComponent.settingsViewModel,
        archivedHabitsViewModel = appComponent.archivedHabitsViewModel,
        createHabitFormViewModel = appComponent.habitFormViewModelFactory::create
    )
}
```

Add the required import at the top of `App.kt`:
```kotlin
import androidx.compose.runtime.CompositionLocalProvider
import com.ricardocosteira.habitlock.di.LocalAppComponent
```

- [ ] **Step 3: Build to verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/di/LocalAppComponent.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/App.kt
git commit -m "feat(nav): add LocalAppComponent CompositionLocal and provide at App root"
```

---

### Task 2: Add Route overload to `PhilosophyScreen.kt`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/PhilosophyScreen.kt`

The existing `PhilosophyScreen(onContinue, onSkip, modifier)` becomes the private inner Screen.
A new public Route overload is added at the top of the file.

- [ ] **Step 1: Add Route overload and make inner Screen `private` — NOT YET**

`HabitLockNavigation` still calls `PhilosophyScreen(onContinue = ..., onSkip = ...)` directly. Mark the inner Screen `private` only in Task 10. For now, add the Route overload above the existing function and leave the existing function public.

Add these imports to `PhilosophyScreen.kt`:
```kotlin
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.presentation.ui.onboarding.OnboardingEvent
```

Add the Route overload **above** the existing `fun PhilosophyScreen(...)`:

```kotlin
@Composable
fun PhilosophyScreen(
    onNavigateToStrictness: () -> Unit,
    onNavigateToToday: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = LocalAppComponent.current.onboardingViewModel

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.NavigateToStrictness -> onNavigateToStrictness()
                OnboardingEvent.NavigateToFirstHabit -> Unit  // not reachable from PhilosophyScreen
                OnboardingEvent.NavigateToToday -> onNavigateToToday()
                is OnboardingEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    PhilosophyScreen(
        onContinue = viewModel::continueFromPhilosophy,
        onSkip = viewModel::skipToToday
    )
}
```

- [ ] **Step 2: Build to verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/PhilosophyScreen.kt
git commit -m "feat(nav): add Route overload to PhilosophyScreen"
```

---

### Task 3: Add Route overload to `StrictnessScreen.kt`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/StrictnessScreen.kt`

The existing `StrictnessScreen(selectedPreset, isLoading, onPresetSelected, onContinue, onSkip, modifier)` stays public for now.

- [ ] **Step 1: Add imports to `StrictnessScreen.kt`**

```kotlin
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.presentation.ui.onboarding.OnboardingEvent
```

- [ ] **Step 2: Add Route overload above the existing `fun StrictnessScreen(...)`**

```kotlin
@Composable
fun StrictnessScreen(
    onNavigateToFirstHabit: () -> Unit,
    onNavigateToToday: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = LocalAppComponent.current.onboardingViewModel
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.NavigateToStrictness -> Unit  // not reachable from StrictnessScreen
                OnboardingEvent.NavigateToFirstHabit -> onNavigateToFirstHabit()
                OnboardingEvent.NavigateToToday -> onNavigateToToday()
                is OnboardingEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    StrictnessScreen(
        selectedPreset = state.selectedPreset,
        isLoading = state.isApplyingPreset,
        onPresetSelected = viewModel::selectPreset,
        onContinue = viewModel::continueFromStrictness,
        onSkip = viewModel::skipToToday
    )
}
```

- [ ] **Step 3: Build to verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/StrictnessScreen.kt
git commit -m "feat(nav): add Route overload to StrictnessScreen"
```

---

### Task 4: Add Route overload to `FirstHabitScreen.kt`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/FirstHabitScreen.kt`

- [ ] **Step 1: Add imports to `FirstHabitScreen.kt`**

```kotlin
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.presentation.ui.onboarding.OnboardingEvent
```

- [ ] **Step 2: Add Route overload above the existing `fun FirstHabitScreen(...)`**

```kotlin
@Composable
fun FirstHabitScreen(
    onNavigateToToday: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = LocalAppComponent.current.onboardingViewModel
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.NavigateToStrictness -> Unit  // not reachable from FirstHabitScreen
                OnboardingEvent.NavigateToFirstHabit -> Unit  // not reachable from FirstHabitScreen
                OnboardingEvent.NavigateToToday -> onNavigateToToday()
                is OnboardingEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    FirstHabitScreen(
        habitName = state.habitName,
        habitType = state.habitType,
        targetValue = state.targetValue,
        unit = state.unit,
        isLoading = state.isCreatingHabit,
        onHabitNameChange = viewModel::updateHabitName,
        onHabitTypeChange = viewModel::updateHabitType,
        onTargetValueChange = viewModel::updateTargetValue,
        onUnitChange = viewModel::updateUnit,
        onCreateHabit = viewModel::createFirstHabit,
        onSkip = viewModel::skipFirstHabit
    )
}
```

- [ ] **Step 3: Build to verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/FirstHabitScreen.kt
git commit -m "feat(nav): add Route overload to FirstHabitScreen"
```

---

### Task 5: Add Route overload to `TodayScreen.kt`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayScreen.kt`

This is the most complex Route — it has the most callbacks and also hosts `QuantitativeInputBottomSheet`.

- [ ] **Step 1: Add imports to `TodayScreen.kt`**

```kotlin
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.presentation.ui.today.TodayEvent
```

(Check whether `SnackbarHostState`, `collectAsStateWithLifecycle`, and `getValue` are already imported in the file — only add what's missing.)

- [ ] **Step 2: Add Route overload above the existing `fun TodayScreen(...)`**

The Route overload calls `QuantitativeInputBottomSheet` which uses `@ExperimentalMaterial3Api`, so it needs the same opt-in annotation as the inner Screen.

New imports needed (add alongside the existing imports):
```kotlin
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.presentation.ui.today.TodayEvent
```
(`SnackbarHostState` and `getValue` are already imported in `TodayScreen.kt`.)

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNavigateToHabitDetail: (String) -> Unit,
    onNavigateToCreateHabit: () -> Unit,
    onEditHabit: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = LocalAppComponent.current.todayViewModel
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is TodayEvent.NavigateToHabitDetail -> onNavigateToHabitDetail(event.instanceId)
                TodayEvent.NavigateToCreateHabit -> onNavigateToCreateHabit()
                is TodayEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is TodayEvent.ShowSuccess -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    TodayScreen(
        state = state,
        onCalendarClick = onCalendarClick,
        onSettingsClick = onSettingsClick,
        onHabitClick = viewModel::navigateToHabitDetail,
        onCompleteClick = viewModel::completeHabit,
        onSkipClick = viewModel::skipHabit,
        onUndoClick = viewModel::undoHabit,
        onEditClick = onEditHabit,
        onArchiveClick = viewModel::archiveHabit,
        onAddHabitClick = viewModel::navigateToCreateHabit,
        onDismissTimezoneWarning = viewModel::dismissTimezoneWarning,
        snackbarHostState = snackbarHostState
    )

    state.showQuantitativeInputFor?.let { instanceId ->
        val habit = state.habits.find { it.instanceId == instanceId }
        if (habit != null) {
            QuantitativeInputBottomSheet(
                habit = habit,
                onConfirm = { value -> viewModel.completeQuantitativeHabit(instanceId, value) },
                onDismiss = viewModel::dismissQuantitativeInput
            )
        }
    }
}
```

- [ ] **Step 3: Build to verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayScreen.kt
git commit -m "feat(nav): add Route overload to TodayScreen"
```

---

## Chunk 2: Remaining screens + wiring + privacy

### Task 6: Add Route overload to `CalendarScreen.kt`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/calendar/CalendarScreen.kt`

`CalendarViewModel` has no events — no `LaunchedEffect` needed.

- [ ] **Step 1: Add imports to `CalendarScreen.kt`**

```kotlin
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
```

- [ ] **Step 2: Add Route overload above the existing `fun CalendarScreen(...)`**

```kotlin
@Composable
fun CalendarScreen(onBackClick: () -> Unit) {
    val viewModel = LocalAppComponent.current.calendarViewModel
    val state by viewModel.state.collectAsStateWithLifecycle()

    CalendarScreen(
        state = state,
        onBackClick = onBackClick,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onDayClick = { viewModel.selectDay(it.date) }
    )
}
```

- [ ] **Step 3: Build to verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/calendar/CalendarScreen.kt
git commit -m "feat(nav): add Route overload to CalendarScreen"
```

---

### Task 7: Add Route overload to `SettingsScreen.kt`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/settings/SettingsScreen.kt`

- [ ] **Step 1: Add imports to `SettingsScreen.kt`**

```kotlin
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.presentation.ui.settings.SettingsEvent
```

- [ ] **Step 2: Add Route overload above the existing `fun SettingsScreen(...)`**

```kotlin
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onArchivedHabitsClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = LocalAppComponent.current.settingsViewModel
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ShowSuccess -> snackbarHostState.showSnackbar(event.message)
                is SettingsEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    SettingsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onUndoPolicyChange = viewModel::updateUndoPolicy,
        onMaxSnoozeDurationChange = viewModel::updateMaxSnoozeDuration,
        onMaxSnoozesPerDayChange = viewModel::updateMaxSnoozesPerDay,
        onMaxConsecutiveSkipsChange = viewModel::updateMaxConsecutiveSkips,
        onArchivedHabitsClick = onArchivedHabitsClick
    )
}
```

- [ ] **Step 3: Build to verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/settings/SettingsScreen.kt
git commit -m "feat(nav): add Route overload to SettingsScreen"
```

---

### Task 8: Add Route overload to `ArchivedHabitsScreen.kt`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/archived/ArchivedHabitsScreen.kt`

- [ ] **Step 1: Add imports to `ArchivedHabitsScreen.kt`**

```kotlin
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.presentation.ui.archived.ArchivedHabitsEvent
```

- [ ] **Step 2: Add Route overload above the existing `fun ArchivedHabitsScreen(...)`**

```kotlin
@Composable
fun ArchivedHabitsScreen(
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = LocalAppComponent.current.archivedHabitsViewModel
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ArchivedHabitsEvent.ShowSuccess -> snackbarHostState.showSnackbar(event.message)
                is ArchivedHabitsEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    ArchivedHabitsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onUnarchiveClick = viewModel::unarchiveHabit,
        onDeleteClick = viewModel::deleteHabit
    )
}
```

- [ ] **Step 3: Build to verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/archived/ArchivedHabitsScreen.kt
git commit -m "feat(nav): add Route overload to ArchivedHabitsScreen"
```

---

### Task 9: Add Route overload to `HabitFormScreen.kt`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormScreen.kt`

- [ ] **Step 1: Add imports to `HabitFormScreen.kt`**

```kotlin
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.presentation.ui.habit.HabitFormEvent
```

- [ ] **Step 2: Add Route overload above the existing `fun HabitFormScreen(...)`**

```kotlin
@Composable
fun HabitFormScreen(
    habitIdToEdit: String?,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val factory = LocalAppComponent.current.habitFormViewModelFactory
    val viewModel = remember { factory.create(habitIdToEdit) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HabitFormEvent.NavigateBack -> onNavigateBack()
                is HabitFormEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    HabitFormScreen(
        state = state,
        onBackClick = onNavigateBack,
        onNameChange = viewModel::updateName,
        onDescriptionChange = viewModel::updateDescription,
        onTypeChange = viewModel::updateType,
        onTargetValueChange = viewModel::updateTargetValue,
        onUnitChange = viewModel::updateUnit,
        onScheduleTypeChange = viewModel::updateScheduleType,
        onQuotaChange = viewModel::updateQuota,
        onHasReminderChange = viewModel::updateHasReminder,
        onReminderTypeChange = viewModel::updateReminderType,
        onIntervalChange = viewModel::updateIntervalMinutes,
        onSaveClick = viewModel::saveHabit,
        onDeleteClick = viewModel::deleteHabit
    )
}
```

**Note on `onBackClick`:** `onBackClick` maps to the "X" / back button (discard without saving). `onNavigateBack` (which `HabitLockNavigation` supplies as `backStack::removeLastOrNull` for the back button) is correct here. The save-and-reload path is triggered separately by `HabitFormEvent.NavigateBack` which fires after `viewModel.saveHabit()` succeeds — that event's handler calls the same `onNavigateBack` lambda, which in the `CreateHabit`/`EditHabit` entries also calls `todayViewModel.loadTodayHabits()`.

- [ ] **Step 3: Build to verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormScreen.kt
git commit -m "feat(nav): add Route overload to HabitFormScreen"
```

---

### Task 10: Refactor `HabitLockNavigation` + update `App.kt` + make inner Screens `private`

This task wires everything together. All Route overloads are in place; now `HabitLockNavigation` switches to calling them, loses its VM parameters, and the inner Screen composables become `private`.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/HabitLockNavigation.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/App.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/PhilosophyScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/StrictnessScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/FirstHabitScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/calendar/CalendarScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/settings/SettingsScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/archived/ArchivedHabitsScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormScreen.kt`

- [ ] **Step 1: Rewrite `HabitLockNavigation.kt`**

Replace the entire file with:

```kotlin
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
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import com.ricardocosteira.habitlock.presentation.ui.archived.ArchivedHabitsScreen
import com.ricardocosteira.habitlock.presentation.ui.calendar.CalendarScreen
import com.ricardocosteira.habitlock.presentation.ui.habit.HabitFormScreen
import com.ricardocosteira.habitlock.presentation.ui.onboarding.FirstHabitScreen
import com.ricardocosteira.habitlock.presentation.ui.onboarding.PhilosophyScreen
import com.ricardocosteira.habitlock.presentation.ui.onboarding.StrictnessScreen
import com.ricardocosteira.habitlock.presentation.ui.settings.SettingsScreen
import com.ricardocosteira.habitlock.presentation.ui.today.TodayScreen

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
```

- [ ] **Step 2: Update `App.kt` — remove VM args from `HabitLockNavigation` call**

Change the `HabitLockNavigation(...)` call inside the `is StartupState.Ready ->` branch to:
```kotlin
HabitLockNavigation(isOnboardingCompleted = currentState.isOnboardingCompleted)
```

`App.kt` does not import any ViewModel classes directly — those are in `HabitLockNavigation.kt`. No import changes needed in `App.kt`.

- [ ] **Step 3: Make all inner Screen composables `private`**

In each file listed below, add `private` to the existing (second, inner) `fun XxxScreen(...)` declaration. The Route overload added in Tasks 2–9 is the one at the top of the file and must remain public.

- `PhilosophyScreen.kt` — `fun PhilosophyScreen(onContinue, onSkip, modifier)` → `private fun PhilosophyScreen(...)`
- `StrictnessScreen.kt` — `fun StrictnessScreen(selectedPreset, isLoading, ...)` → `private fun StrictnessScreen(...)`
- `FirstHabitScreen.kt` — `fun FirstHabitScreen(habitName, habitType, ...)` → `private fun FirstHabitScreen(...)`
- `TodayScreen.kt` — `fun TodayScreen(state: TodayState, ...)` → `private fun TodayScreen(...)`
- `CalendarScreen.kt` — `fun CalendarScreen(state: CalendarState, ...)` → `private fun CalendarScreen(...)`
- `SettingsScreen.kt` — `fun SettingsScreen(state: SettingsState, ...)` → `private fun SettingsScreen(...)`
- `ArchivedHabitsScreen.kt` — `fun ArchivedHabitsScreen(state: ArchivedHabitsState, ...)` → `private fun ArchivedHabitsScreen(...)`
- `HabitFormScreen.kt` — `fun HabitFormScreen(state: HabitFormState, ...)` → `private fun HabitFormScreen(...)`

Note: `@OptIn` annotations on inner Screens must stay on the same line as the `private` modifier:
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(...)
```

- [ ] **Step 4: Build to verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Smoke test on device or emulator**

Launch the app. Verify:
1. App starts and splash screen dismisses
2. Onboarding flow navigates: Philosophy → Strictness → First Habit → Today (or skip at any step goes to Today)
3. Today screen loads habits, Calendar and Settings icons in top bar navigate correctly
4. Create / Edit habit works (form opens, save navigates back, today reloads)
5. Settings → Archived Habits sub-navigation works
6. Back button works throughout

- [ ] **Step 6: Commit**

```bash
git add \
  composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/HabitLockNavigation.kt \
  composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/App.kt \
  composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/PhilosophyScreen.kt \
  composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/StrictnessScreen.kt \
  composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/FirstHabitScreen.kt \
  composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayScreen.kt \
  composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/calendar/CalendarScreen.kt \
  composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/settings/SettingsScreen.kt \
  composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/archived/ArchivedHabitsScreen.kt \
  composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormScreen.kt
git commit -m "refactor(nav): wire Route overloads in HabitLockNavigation; make inner Screens private"
```
