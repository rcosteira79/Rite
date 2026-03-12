# Navigation 3 Migration Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Wire the existing back stack infrastructure in `HabitLockNavHost` to the official Navigation 3 KMP library (`org.jetbrains.androidx.navigation3:navigation3-ui`), making routes `@Serializable + NavKey` and replacing the manual `when` renderer with `NavDisplay + entryProvider`.

**Architecture:** `rememberNavBackStack(navConfig, initialRoute)` replaces `remember { mutableStateListOf(...) }`. `NavDisplay` with `entryProvider { entry<Route.X> { ... } }` replaces the manual `when (backStack.last())` block. A top-level `SavedStateConfiguration` registers all `Route` subclasses via `subclassesOfSealed<Route>()` for KMP-safe state saving. The back stack manipulation calls (`backStack.add`, `backStack.removeLastOrNull`, `backStack.clear`) remain unchanged.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform 1.10.0, `org.jetbrains.androidx.navigation3:navigation3-ui 1.0.0-alpha05`, `kotlinx-serialization` (Kotlin plugin 2.3.0, runtime transitive)

**Spec:** `docs/superpowers/specs/2026-03-12-navigation3-migration-design.md`

**Branch:** `feature/navigation3-migration`

**Already committed (do not redo):**
- Task 1: `Route.Onboarding` removed
- Task 2: Drawer replaced with icon buttons; back stack already using `mutableStateListOf`

---

## Chunk 1: Dependencies and route types

### Task 3: Add Navigation 3 and serialization dependencies

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `composeApp/build.gradle.kts`

- [ ] **Step 1: Add version, library, and plugin entries to `libs.versions.toml`**

Add to the `[versions]` section:
```toml
navigation3 = "1.0.0-alpha05"
```

Add to the `[libraries]` section:
```toml
jetbrains-navigation3-ui = { module = "org.jetbrains.androidx.navigation3:navigation3-ui", version.ref = "navigation3" }
```

Add to the `[plugins]` section:
```toml
kotlinxSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

- [ ] **Step 2: Apply serialization plugin and add `navigation3-ui` dependency in `build.gradle.kts`**

In the `plugins { }` block, add after the existing aliases:
```kotlin
alias(libs.plugins.kotlinxSerialization)
```

In `commonMain.dependencies { }`, add:
```kotlin
implementation(libs.jetbrains.navigation3.ui)
```

- [ ] **Step 3: Verify the build resolves the new dependency**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

Expected: `BUILD SUCCESSFUL` (dependency resolved, no compile errors yet)

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml composeApp/build.gradle.kts
git commit -m "build: add navigation3-ui and kotlinx-serialization dependencies"
```

---

### Task 4: Make `Route` `@Serializable` and implement `NavKey`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/Route.kt`

Navigation 3 requires every route type to be `@Serializable` and implement `NavKey`. The `subclassesOfSealed<Route>()` helper used in the next task relies on this.

- [ ] **Step 1: Update `Route.kt`**

Replace the entire file content with:

```kotlin
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
```

- [ ] **Step 2: Verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Run tests**

```bash
./gradlew :composeApp:jvmTest
```

Expected: `BUILD SUCCESSFUL`, all tests pass

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/Route.kt
git commit -m "refactor(nav): make Route @Serializable and implement NavKey"
```

---

## Chunk 2: NavDisplay migration and cleanup

### Task 5: Migrate `HabitLockNavHost` to `NavDisplay`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/HabitLockNavHost.kt`

Replace `remember { mutableStateListOf(...) }` with `rememberNavBackStack`, and replace the `when (backStack.last())` block with `NavDisplay + entryProvider`. All back stack mutation calls (`backStack.add`, `backStack.removeLastOrNull`, `backStack.clear`) are unchanged.

- [ ] **Step 1: Add the top-level `navConfig` constant**

Above the `@Composable` annotation for `HabitLockNavHost`, add this private top-level val:

```kotlin
private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<Route>()
        }
    }
}
```

- [ ] **Step 2: Replace `remember { mutableStateListOf(...) }` with `rememberNavBackStack`**

**Replace:**
```kotlin
val backStack = remember {
    mutableStateListOf<Route>(
        if (isOnboardingCompleted) Route.Today else Route.OnboardingPhilosophy
    )
}
```

**With:**
```kotlin
val initialRoute: Route = if (isOnboardingCompleted) Route.Today else Route.OnboardingPhilosophy
val backStack = rememberNavBackStack(navConfig, initialRoute)
```

- [ ] **Step 3: Replace the `when` block with `NavDisplay + entryProvider`**

**Replace** the entire block starting from `when (val route = backStack.last()) {` through the closing `}` of the `when` expression.

**With:**

```kotlin
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
```

- [ ] **Step 4: Update imports**

**Remove:**
```kotlin
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
```

**Add:**
```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.SavedStateConfiguration
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed
```

Note: `remember` is still needed (for ViewModels inside entries) so re-add it. Remove `mutableStateListOf` — no longer used.

- [ ] **Step 5: Update the doc comment**

**Replace:**
```kotlin
/**
 * Main navigation host for the app.
 * For now, this uses manual navigation state since Navigation 3 setup with Metro DI
 * requires additional configuration. This can be refactored to use Navigation 3 NavHost.
 */
```
**With:**
```kotlin
/**
 * Main navigation host for the app using Navigation 3 (org.jetbrains.androidx.navigation3).
 */
```

- [ ] **Step 6: Verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Run tests**

```bash
./gradlew :composeApp:jvmTest
```

Expected: `BUILD SUCCESSFUL`, all tests pass

- [ ] **Step 8: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/HabitLockNavHost.kt
git commit -m "refactor(nav): migrate HabitLockNavHost to NavDisplay + entryProvider"
```

---

### Task 6: Delete `AppNavigationDrawer.kt`

**Files:**
- Delete: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/components/AppNavigationDrawer.kt`

All usages of `AppNavigationDrawer` and `DrawerDestination` were removed in Task 2. The file is now dead code.

- [ ] **Step 1: Confirm no remaining usages**

```bash
grep -r "AppNavigationDrawer\|DrawerDestination" composeApp/src
```

Expected: no output

- [ ] **Step 2: Delete the file**

```bash
rm composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/components/AppNavigationDrawer.kt
```

- [ ] **Step 3: Verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Run tests**

```bash
./gradlew :composeApp:jvmTest
```

Expected: `BUILD SUCCESSFUL`, all tests pass

- [ ] **Step 5: Commit**

```bash
git add -u composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/components/AppNavigationDrawer.kt
git commit -m "refactor(nav): delete AppNavigationDrawer — replaced by icon buttons"
```
