# Navigation 3 Migration Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the single `mutableStateOf<Route>` in `HabitLockNavHost` with a `SnapshotStateList<Route>` back stack (the Navigation 3 pattern), remove the navigation drawer, and replace it with Calendar/Settings icon buttons in `TodayScreen`'s top bar.

**Architecture:** The back stack is a `mutableStateListOf<Route>` owned in `HabitLockNavHost`. The current screen is `backStack.last()`; navigating forward calls `backStack.add(route)`, and going back calls `backStack.removeLastOrNull()`. Onboarding completion calls `backStack.clear()` then `backStack.add(Route.Today)` to prevent returning to the onboarding flow. No new library dependency.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform 1.10.0, kotlin-inject 0.9.0, commonMain (shared across Android + iOS + JVM)

**Spec:** `docs/superpowers/specs/2026-03-12-navigation3-migration-design.md`

---

## Chunk 1: Route cleanup and TodayScreen top bar update

### Task 1: Remove `Route.Onboarding`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/Route.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/HabitLockNavHost.kt`

`Route.Onboarding` is a dead `data object` — it was never routed to anywhere other than `PhilosophyScreen`, which `Route.OnboardingPhilosophy` already handles. Remove it from the sealed interface and drop its branch from the `when` block.

- [ ] **Step 1: Remove `Route.Onboarding` from `Route.kt`**

Delete this line from `Route.kt`:

```kotlin
data object Onboarding : Route
```

- [ ] **Step 2: Remove the `Route.Onboarding` branch from `HabitLockNavHost.kt`**

In the `when` block, change:

```kotlin
Route.OnboardingPhilosophy, Route.Onboarding -> {
```

to:

```kotlin
Route.OnboardingPhilosophy -> {
```

- [ ] **Step 3: Verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Run existing tests**

```bash
./gradlew :composeApp:jvmTest
```

Expected: `BUILD SUCCESSFUL`, all tests pass

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/Route.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/HabitLockNavHost.kt
git commit -m "refactor(nav): remove unused Route.Onboarding"
```

---

### Task 2: Replace drawer with Calendar/Settings icon buttons

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/HabitLockNavHost.kt`

Replace the hamburger menu icon with two `IconButton`s (Calendar and Settings) in `TodayScreen`'s `TopAppBar`. Simultaneously remove the `AppNavigationDrawer` wrapper and all drawer-related state from `HabitLockNavHost`'s `Today` branch (the scope and drawerState are only used for drawer open/close and become dead code).

Note: after Task 1, the `when` branch in `HabitLockNavHost.kt` already reads `Route.OnboardingPhilosophy ->` (the `Route.Onboarding` part was removed in Task 1). The "old" snippets below reflect the **post-Task-1** state of the file.

- [ ] **Step 1: Update `TodayScreen` signature and top bar**

In `TodayScreen.kt`:

**Replace** the import:
```kotlin
import androidx.compose.material.icons.filled.Menu
```
**With:**
```kotlin
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
```

**Replace** the parameter:
```kotlin
onMenuClick: () -> Unit,
```
**With:**
```kotlin
onCalendarClick: () -> Unit,
onSettingsClick: () -> Unit,
```

**Replace** the `TopAppBar` navigation icon block:
```kotlin
topBar = {
    TopAppBar(
        title = { Text("Today") },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        }
    )
},
```
**With:**
```kotlin
topBar = {
    TopAppBar(
        title = { Text("Today") },
        actions = {
            IconButton(onClick = onCalendarClick) {
                Icon(Icons.Default.DateRange, contentDescription = "Calendar")
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    )
},
```

- [ ] **Step 2: Update `HabitLockNavHost` Today branch**

In `HabitLockNavHost.kt`, remove the three drawer-related declarations. `drawerState` and `scope` appear **before** `snackbarHostState`; `selectedDrawerDestination` appears **after** it. `snackbarHostState` itself must remain.

```kotlin
val drawerState = rememberDrawerState(DrawerValue.Closed)
val scope = rememberCoroutineScope()
```

and the derived val:

```kotlin
// Derive drawer selection from route — never mutate during composition
val selectedDrawerDestination = when (currentRoute) {
    Route.Calendar -> DrawerDestination.CALENDAR
    Route.Settings -> DrawerDestination.SETTINGS
    else -> DrawerDestination.TODAY
}
```

In the `Route.Today` branch, **replace** the entire `AppNavigationDrawer` wrapper:

```kotlin
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
```

**With** (no drawer wrapper, updated TodayScreen params, `currentRoute` assignments preserved for now):

```kotlin
Route.Today -> {
    val state by todayViewModel.state.collectAsStateWithLifecycle()

    TodayScreen(
        state = state,
        onCalendarClick = { currentRoute = Route.Calendar },
        onSettingsClick = { currentRoute = Route.Settings },
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
```

Also remove the now-unused imports from `HabitLockNavHost.kt`:

```kotlin
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import com.ricardocosteira.habitlock.presentation.ui.components.AppNavigationDrawer
import com.ricardocosteira.habitlock.presentation.ui.components.DrawerDestination
import kotlinx.coroutines.launch
```

- [ ] **Step 3: Verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Run existing tests**

```bash
./gradlew :composeApp:jvmTest
```

Expected: `BUILD SUCCESSFUL`, all tests pass

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayScreen.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/HabitLockNavHost.kt
git commit -m "refactor(nav): replace drawer with Calendar/Settings icon buttons in TodayScreen"
```

---

## Chunk 2: Back stack migration and cleanup

### Task 3: Migrate `HabitLockNavHost` to `SnapshotStateList` back stack

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/HabitLockNavHost.kt`

This is the main migration step. Replace the `currentRoute` `mutableStateOf` with a `mutableStateListOf` back stack, update all navigation assignments to `backStack.add` / `backStack.removeLastOrNull()` / `backStack.clear()`, and change the rendering `when` to use `backStack.last()`.

- [ ] **Step 1: Replace `currentRoute` declaration with `backStack`**

**Replace:**
```kotlin
var currentRoute by remember {
    mutableStateOf<Route>(
        if (isOnboardingCompleted) Route.Today else Route.OnboardingPhilosophy
    )
}
```

**With:**
```kotlin
val backStack = remember {
    mutableStateListOf<Route>(
        if (isOnboardingCompleted) Route.Today else Route.OnboardingPhilosophy
    )
}
```

Also remove these now-unused imports:
```kotlin
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
```

And add:
```kotlin
import androidx.compose.runtime.mutableStateListOf
```

Note: `getValue` is still needed for `collectAsStateWithLifecycle` delegates further down — only remove `mutableStateOf` and `setValue`.

- [ ] **Step 2: Update onboarding event collector**

**Replace** the onboarding `LaunchedEffect` body:
```kotlin
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
```

**With:**
```kotlin
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
```

- [ ] **Step 3: Update today event collector**

**Replace:**
```kotlin
TodayEvent.NavigateToCreateHabit -> currentRoute = Route.CreateHabit
```

**With:**
```kotlin
TodayEvent.NavigateToCreateHabit -> backStack.add(Route.CreateHabit)
```

- [ ] **Step 4: Update the `when` renderer to use `backStack.last()`**

**Replace:**
```kotlin
when (val route = currentRoute) {
```

**With:**
```kotlin
when (val route = backStack.last()) {
```

- [ ] **Step 5: Update Today branch navigation lambdas**

**Replace** the two inline navigation assignments in the Today branch (from Task 2):
```kotlin
onCalendarClick = { currentRoute = Route.Calendar },
onSettingsClick = { currentRoute = Route.Settings },
```

**With:**
```kotlin
onCalendarClick = { backStack.add(Route.Calendar) },
onSettingsClick = { backStack.add(Route.Settings) },
```

And the edit click:
```kotlin
onEditClick = { habitId -> currentRoute = Route.EditHabit(habitId) },
```
**With:**
```kotlin
onEditClick = { habitId -> backStack.add(Route.EditHabit(habitId)) },
```

- [ ] **Step 6: Update Calendar branch**

**Replace:**
```kotlin
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
```

**With:**
```kotlin
Route.Calendar -> {
    val state by calendarViewModel.state.collectAsStateWithLifecycle()

    CalendarScreen(
        state = state,
        onBackClick = { backStack.removeLastOrNull() },
        onPreviousMonth = { calendarViewModel.previousMonth() },
        onNextMonth = { calendarViewModel.nextMonth() },
        onDayClick = { calendarViewModel.selectDay(it.date) }
    )
}
```

- [ ] **Step 7: Update Settings branch**

**Replace:**
```kotlin
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
```

**With:**
```kotlin
Route.Settings -> {
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
```

- [ ] **Step 8: Update ArchivedHabits branch**

**Replace:**
```kotlin
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
```

**With:**
```kotlin
Route.ArchivedHabits -> {
    val state by archivedHabitsViewModel.state.collectAsStateWithLifecycle()

    ArchivedHabitsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBackClick = { backStack.removeLastOrNull() },
        onUnarchiveClick = { archivedHabitsViewModel.unarchiveHabit(it) },
        onDeleteClick = { archivedHabitsViewModel.deleteHabit(it) }
    )
}
```

- [ ] **Step 9: Update CreateHabit branch**

**Replace:**
```kotlin
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
        ...
    )
}
```

**With:**
```kotlin
Route.CreateHabit -> {
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
```

- [ ] **Step 10: Update EditHabit branch**

**Replace** both `currentRoute` assignments in the `EditHabit` branch:
```kotlin
HabitFormEvent.NavigateBack -> {
    currentRoute = Route.Today
    todayViewModel.loadTodayHabits()
}
```
and:
```kotlin
onBackClick = { currentRoute = Route.Today },
```

**With:**
```kotlin
HabitFormEvent.NavigateBack -> {
    backStack.removeLastOrNull()
    todayViewModel.loadTodayHabits()
}
```
and:
```kotlin
onBackClick = { backStack.removeLastOrNull() },
```

- [ ] **Step 11: Update `HabitDetail` branch**

**Replace:**
```kotlin
is Route.HabitDetail -> {
    // TODO: Implement habit detail screen
    LaunchedEffect(route) {
        currentRoute = Route.Today
    }
}
```

**With:**
```kotlin
is Route.HabitDetail -> {
    // TODO: Implement habit detail screen
    LaunchedEffect(route) {
        backStack.removeLastOrNull()
    }
}
```

- [ ] **Step 12: Remove unused `modifier` parameter**

`HabitLockNavHost` has `modifier: Modifier = Modifier` in its signature, but after removing the `AppNavigationDrawer` wrapper it is never applied anywhere inside the composable body. Remove it from the signature. `App.kt` does not pass `modifier` at the call site, so the removal is safe. Also remove `import androidx.compose.ui.Modifier` — it is no longer referenced.

- [ ] **Step 13: Verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 14: Run existing tests**

```bash
./gradlew :composeApp:jvmTest
```

Expected: `BUILD SUCCESSFUL`, all tests pass

- [ ] **Step 15: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/HabitLockNavHost.kt
git commit -m "refactor(nav): migrate HabitLockNavHost to SnapshotStateList back stack"
```

---

### Task 4: Delete `AppNavigationDrawer.kt`

**Files:**
- Delete: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/components/AppNavigationDrawer.kt`

All usages of `AppNavigationDrawer` and `DrawerDestination` were removed in Tasks 2 and 3. The file can now be deleted.

- [ ] **Step 1: Confirm no remaining usages**

```bash
grep -r "AppNavigationDrawer\|DrawerDestination" composeApp/src
```

Expected: no output (zero matches)

- [ ] **Step 2: Delete the file**

```bash
rm composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/components/AppNavigationDrawer.kt
```

- [ ] **Step 3: Verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Run existing tests**

```bash
./gradlew :composeApp:jvmTest
```

Expected: `BUILD SUCCESSFUL`, all tests pass

- [ ] **Step 5: Commit**

```bash
git add -u composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/components/AppNavigationDrawer.kt
git commit -m "refactor(nav): delete AppNavigationDrawer — replaced by icon buttons"
```
