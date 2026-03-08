# MVP Completion Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Complete the HabitLock MVP: iOS activation, Application-level DI, Navigation migration, deferred UI screens (Settings strictness, Calendar day-detail, Leave Mode), and replace MockK with hand-written fakes in all tests.

**Architecture:**
- iOS is activated alongside Android — `MainViewController` uncommented and wired identically to `MainActivity`.
- A custom `HabitLockApplication` (`androidMain`) holds the `HabitLockAppComponent` singleton; Workers and Receivers retrieve it from `applicationContext`, eliminating the per-invocation `AppModule` pattern.
- Navigation migrates from a manual `var currentRoute by remember` state machine to `NavHost` + `NavController` from `org.jetbrains.androidx.navigation:navigation-compose` (the KMP-compatible fork).
- All tests use hand-written fake repository implementations stored in `commonTest/kotlin/…/fakes/`.

**Tech Stack:** Kotlin Multiplatform · Compose Multiplatform · SQLDelight · kotlin-inject · WorkManager · `org.jetbrains.androidx.navigation:navigation-compose`

---

## Answered Questions (from AGENTS.md)

| # | Question | Answer |
|---|----------|--------|
| Q1 | iOS and JVM targets? | iOS is an active MVP target. JVM stays wired but is a dev tool only. |
| Q2 | Navigation Component 3? | Migrate now. |
| Q3 | Worker DB strategy? | Move to Application-level shared component — no more per-Worker `AppModule`. |
| Q4 | Phase priority? | Deferred UI first, then testing everything. |
| Q5 | Test doubles? | Fakes everywhere possible; remove MockK. |

---

## Task 1 — iOS MVP Activation

**Files:**
- Modify: `composeApp/src/iosMain/kotlin/com/ricardocosteira/habitlock/MainViewController.kt`

**Step 1:** Uncomment the body of `MainViewController()`:

```kotlin
fun MainViewController() = ComposeUIViewController {
    val driverFactory = DatabaseDriverFactory()
    val appComponent = HabitLockAppComponent::class.create(driverFactory)
    App(appComponent = { appComponent })
}
```

**Step 2:** Verify the file compiles with no errors — check the `iosMain` KSP sources are generated (kotlin-inject runs for iOS via `kspIosArm64` / `kspIosSimulatorArm64`).

**Step 3:** Run `./gradlew :composeApp:compileKotlinIosSimulatorArm64` and confirm BUILD SUCCESSFUL (warnings OK).

**Step 4:** Commit: `feat(ios): activate iOS MVP entry point`

---

## Task 2 — Application-Level Shared DI Component (Android)

**Context:** Workers and BroadcastReceivers currently create `AppModule(driverFactory)` on every invocation — a fresh SQLite connection each time. Moving the singleton to `Application` ensures one DB connection is shared across the app process and all WorkManager workers.

**Files:**
- Create: `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/HabitLockApplication.kt`
- Modify: `composeApp/src/androidMain/AndroidManifest.xml` — add `android:name=".HabitLockApplication"`
- Modify: `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/MainActivity.kt`
- Modify: `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/workers/DailyHabitGenerationWorker.kt`
- Modify: `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/workers/EndOfDayProcessingWorker.kt`
- Modify: `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/notifications/NotificationActionReceiver.kt`

**Step 1:** Create `HabitLockApplication.kt`:

```kotlin
package com.ricardocosteira.habitlock

import android.app.Application
import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import com.ricardocosteira.habitlock.di.HabitLockAppComponent
import com.ricardocosteira.habitlock.di.create
import com.ricardocosteira.habitlock.notifications.NotificationChannels
import com.ricardocosteira.habitlock.workers.WorkManagerInitializer

class HabitLockApplication : Application() {

    lateinit var appComponent: HabitLockAppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        val driverFactory = DatabaseDriverFactory(this)
        appComponent = HabitLockAppComponent::class.create(driverFactory)
        NotificationChannels.createChannels(this)
        WorkManagerInitializer.initialize(this)
    }
}

val android.content.Context.habitLockApplication: HabitLockApplication
    get() = applicationContext as HabitLockApplication
```

**Step 2:** Register in `AndroidManifest.xml` — inside `<application>` tag add `android:name=".HabitLockApplication"`. Remove `NotificationChannels.createChannels` and `WorkManagerInitializer.initialize` calls from `MainActivity.onCreate` since they now live in `Application.onCreate`.

**Step 3:** Update `MainActivity.kt` — remove component creation, get it from Application:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    val appComponent = application.habitLockApplication.appComponent
    setContent { App(appComponent = { appComponent }) }
}
```

**Step 4:** Update `DailyHabitGenerationWorker.doWork()`:

```kotlin
override suspend fun doWork(): Result {
    return try {
        val appComponent = applicationContext.habitLockApplication.appComponent
        appComponent.processEndOfDayUseCase.execute()
        appComponent.generateDailyHabitsUseCase.execute()
        Result.success()
    } catch (e: Exception) {
        e.printStackTrace()
        Result.retry()
    }
}
```

> This requires exposing `processEndOfDayUseCase` and `generateDailyHabitsUseCase` as abstract getters on `HabitLockAppComponent`. Add them.

**Step 5:** Similarly update `EndOfDayProcessingWorker` and `NotificationActionReceiver` to use `applicationContext.habitLockApplication.appComponent` instead of creating `AppModule`.

**Step 6:** Delete `AppModule.kt` (it is no longer needed).

**Step 7:** Run `./gradlew :composeApp:assembleDebug` — BUILD SUCCESSFUL.

**Step 8:** Commit: `refactor(android): lift DI component to Application level, remove AppModule`

---

## Task 3 — Navigation: Add `navigation-compose` Dependency

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `composeApp/build.gradle.kts`

**Step 1:** Add to `libs.versions.toml`:

```toml
[versions]
androidx-navigation = "2.8.9"   # KMP-compatible navigation-compose

[libraries]
androidx-navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "androidx-navigation" }
```

**Step 2:** Add to `composeApp/build.gradle.kts` inside `commonMain.dependencies`:

```kotlin
implementation(libs.androidx.navigation.compose)
```

**Step 3:** Sync and verify the dependency resolves: `./gradlew dependencies --configuration commonMainImplementationDependenciesMetadata 2>&1 | grep navigation`

**Step 4:** Commit: `build: add navigation-compose KMP dependency`

---

## Task 4 — Navigation: Migrate `Route` to `NavHost`

**Context:** Current system is `var currentRoute by remember { mutableStateOf<Route>(...) }` with a `when` block. We replace this with `NavController` + `NavHost`. ViewModels stay wired the same way — passed as parameters to `HabitLockNavHost`.

The `Route` sealed interface becomes the set of string route constants. The `HabitLockNavHost` composable becomes a proper `NavHost { composable(...) { ... } }` structure. ViewModel lifecycle is managed by the NavBackStackEntry scope using `viewModel()` from the androidx lifecycle-viewmodel-compose library.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/Route.kt`
- Replace: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/HabitLockNavHost.kt`

**Step 1:** Rewrite `Route.kt` — replace sealed interface with a `object Routes` containing const String values, plus helper extensions for parameterized routes:

```kotlin
object Routes {
    const val ONBOARDING_PHILOSOPHY = "onboarding/philosophy"
    const val ONBOARDING_STRICTNESS = "onboarding/strictness"
    const val ONBOARDING_FIRST_HABIT = "onboarding/first_habit"
    const val TODAY = "today"
    const val CALENDAR = "calendar"
    const val CALENDAR_DAY_DETAIL = "calendar/day/{date}"
    const val SETTINGS = "settings"
    const val ARCHIVED_HABITS = "archived_habits"
    const val CREATE_HABIT = "habit/create"
    const val EDIT_HABIT = "habit/edit/{habitId}"

    fun calendarDayDetail(date: LocalDate): String = "calendar/day/$date"
    fun editHabit(habitId: String): String = "habit/edit/$habitId"
}
```

**Step 2:** Rewrite `HabitLockNavHost.kt` using `NavHost`:

```kotlin
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
    val navController = rememberNavController()
    val startDestination = if (isOnboardingCompleted) Routes.TODAY else Routes.ONBOARDING_PHILOSOPHY
    val snackbarHostState = remember { SnackbarHostState() }

    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(Routes.ONBOARDING_PHILOSOPHY) { PhilosophyDest(navController, onboardingViewModel, snackbarHostState) }
        composable(Routes.ONBOARDING_STRICTNESS) { StrictnessDest(navController, onboardingViewModel) }
        composable(Routes.ONBOARDING_FIRST_HABIT) { FirstHabitDest(navController, onboardingViewModel, todayViewModel) }
        composable(Routes.TODAY) { TodayDest(navController, todayViewModel, snackbarHostState) }
        composable(Routes.CALENDAR) { CalendarDest(navController, calendarViewModel) }
        composable(
            route = Routes.CALENDAR_DAY_DETAIL,
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStackEntry ->
            val dateString = backStackEntry.arguments?.getString("date") ?: return@composable
            CalendarDayDetailDest(navController, calendarViewModel, dateString)
        }
        composable(Routes.SETTINGS) { SettingsDest(navController, settingsViewModel, snackbarHostState) }
        composable(Routes.ARCHIVED_HABITS) { ArchivedHabitsDest(navController, archivedHabitsViewModel, snackbarHostState) }
        composable(Routes.CREATE_HABIT) { CreateHabitDest(navController, todayViewModel, createHabitFormViewModel, snackbarHostState) }
        composable(
            route = Routes.EDIT_HABIT,
            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: return@composable
            EditHabitDest(navController, todayViewModel, createHabitFormViewModel, habitId, snackbarHostState)
        }
    }
}
```

Each `*Dest` is a private `@Composable` fun in the same file, receiving `navController` and its ViewModel. LaunchedEffects for event collection live inside each `*Dest`.

**Step 3:** Run `./gradlew :composeApp:compileCommonMainKotlinMetadata` — BUILD SUCCESSFUL.

**Step 4:** Commit: `feat(nav): migrate to NavHost + NavController`

---

## Task 5 — Settings Screen: Strictness Preset Switcher

**Context:** The settings screen is missing the "Strictness Level" top section (FLEXIBLE / BALANCED / LOCKED). The backend (`ApplyStrictnessPresetUseCase`) is complete. The settings screen currently shows individual sliders/switches; the preset switcher should sit at the top and update all individual controls when selected.

**Files:**
- Modify: `…/presentation/ui/settings/SettingsState.kt` — add `currentPreset: StrictnessPreset?`
- Modify: `…/presentation/ui/settings/SettingsViewModel.kt` — add `applyPreset()`, load preset on init
- Modify: `…/presentation/ui/settings/SettingsScreen.kt` — add preset selection section at top
- Modify: `…/di/HabitLockAppComponent.kt` — expose `ApplyStrictnessPresetUseCase`

**Step 1:** Add to `SettingsState`:

```kotlin
val currentPreset: StrictnessPreset? = null,   // null = custom/mixed settings
```

**Step 2:** Add `applyPreset()` to `SettingsViewModel`. Inject `ApplyStrictnessPresetUseCase`. After applying, reload settings from `userRepository.observeUser()` (it already does this reactively).

Detect the current preset by comparing user settings to each preset's `toUserSettings()` output during `loadSettings()`. Set `currentPreset` accordingly, or `null` if no preset matches.

**Step 3:** Add `StrictnessSection` composable to `SettingsScreen.kt` as the first section, showing three radio-button cards for FLEXIBLE / BALANCED / LOCKED, calling `onPresetSelected`.

**Step 4:** Wire `onPresetSelected` callback from `HabitLockNavHost` → `SettingsScreen`.

**Step 5:** Compile and verify no regressions: `./gradlew :composeApp:compileCommonMainKotlinMetadata`

**Step 6:** Commit: `feat(settings): add strictness preset switcher`

---

## Task 6 — Calendar Day-Detail Screen

**Context:** When a user taps a day in the calendar, nothing happens yet. We need to navigate to a day-detail screen listing the habits for that day with their statuses.

**Files:**
- Create: `…/presentation/ui/calendar/CalendarDayDetailScreen.kt`
- Modify: `…/presentation/ui/calendar/CalendarViewModel.kt` — add `loadDayDetail(date)` + `DayDetailState`
- Modify: `…/presentation/ui/calendar/CalendarState.kt` — add `dayDetail: DayDetailState?`
- Modify: `…/presentation/navigation/HabitLockNavHost.kt` — wire `CalendarDayDetailDest`

**Step 1:** Add `DayDetailState` data class with `date: LocalDate`, `habits: List<TodayHabitUiModel>`, `isLoading: Boolean`.

**Step 2:** Add `loadDayDetail(date: LocalDate)` to `CalendarViewModel`:
- Load instances for the date from `habitInstanceRepository.getInstancesForDate(date)`
- Map each with `habitRepository.getHabitById` + `habitRepository.getScheduleForHabit` → `TodayHabitUiModel` (reuse existing mapper)
- Update `_state` with `dayDetail` sub-state

**Step 3:** Create `CalendarDayDetailScreen.kt`:
- Scaffold with back arrow, title = date formatted as "Monday, March 7"
- LazyColumn of read-only HabitStatusRow composables (name + status chip; no action buttons — it's history)

**Step 4:** Wire in NavHost — the `CalendarDayDetailDest` triggers `calendarViewModel.loadDayDetail(date)` and renders the screen.

**Step 5:** In the calendar's day cell click handler, navigate to `Routes.calendarDayDetail(date)` only for non-FUTURE/NONE days.

**Step 6:** Commit: `feat(calendar): day-detail screen on tap`

---

## Task 7 — Leave Mode UI

**Context:** Backend is fully implemented (`SuspendHabitUseCase`, `UnsuspendHabitUseCase`). We need:
1. **"Set Leave" menu option** on each habit card in the Today screen
2. **A date-range picker dialog** to select start + end dates
3. **"End Leave Early" button** on suspended habit cards in the Today screen

**Files:**
- Create: `…/presentation/ui/components/DatePickerDialog.kt` — KMP-compatible date picker
- Create: `…/presentation/ui/today/LeaveModeDialog.kt` — wraps DatePickerDialog for leave period selection
- Modify: `…/presentation/ui/today/TodayState.kt` — add `showLeaveModeDialogFor: String?`
- Modify: `…/presentation/ui/today/TodayViewModel.kt` — add `requestLeave()`, `confirmLeave()`, `endLeaveEarly()`
- Modify: `…/di/HabitLockAppComponent.kt` — expose `SuspendHabitUseCase`, `UnsuspendHabitUseCase`
- Modify: `…/presentation/ui/today/TodayScreen.kt` — add "Set Leave" to habit menu, "End Leave Early" button on suspended cards, show `LeaveModeDialog`

**Step 1:** Create `DatePickerDialog.kt` using Material 3's `DatePicker` composable (from `compose.material3`). Provide a `HabitLockDatePickerDialog(onConfirm: (LocalDate) -> Unit, onDismiss: () -> Unit)`.

**Step 2:** Create `LeaveModeDialog.kt` — two-step dialog: pick start date, then optional end date. Exposes `onConfirm(startDate: LocalDate, endDate: LocalDate?)`.

**Step 3:** Add to `TodayState`:
```kotlin
val showLeaveModeDialogFor: String? = null   // habitId
```

**Step 4:** Add to `TodayViewModel` (inject `SuspendHabitUseCase`, `UnsuspendHabitUseCase`):
```kotlin
fun requestLeave(habitId: String) { _state.update { it.copy(showLeaveModeDialogFor = habitId) } }
fun dismissLeaveDialog() { _state.update { it.copy(showLeaveModeDialogFor = null) } }
fun confirmLeave(habitId: String, startDate: LocalDate, endDate: LocalDate?) { … }
fun endLeaveEarly(instanceId: String) { … }   // calls UnsuspendHabitUseCase
```

**Step 5:** Add "Set Leave" `DropdownMenuItem` to `HabitCard`'s context menu (currently has Edit, Archive). Hidden for suspended habits.

**Step 6:** Add "End Leave Early" `TextButton` on the suspended habit card row.

**Step 7:** Show `LeaveModeDialog` when `state.showLeaveModeDialogFor != null`.

**Step 8:** Compile and smoke-test: `./gradlew :composeApp:compileCommonMainKotlinMetadata`

**Step 9:** Commit: `feat(today): leave mode UI — set leave and end leave early`

---

## Task 8 — Test Fakes Infrastructure

**Context:** All existing tests use MockK. We switch to hand-written fakes for all repository interfaces, giving full control over state and removing MockK as a dependency.

**Files:**
- Create: `composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/fakes/FakeHabitRepository.kt`
- Create: `…/fakes/FakeHabitInstanceRepository.kt`
- Create: `…/fakes/FakeHabitCompletionEventRepository.kt`
- Create: `…/fakes/FakeLeavePeriodRepository.kt`
- Create: `…/fakes/FakeSnoozeRepository.kt`
- Create: `…/fakes/FakeUserRepository.kt`
- Create: `…/fakes/TestBuilders.kt` — builder functions for domain model instances
- Modify: `gradle/libs.versions.toml` — remove `mockk`
- Modify: `composeApp/build.gradle.kts` — remove `implementation(libs.mockk)` from `commonTest.dependencies`

**Step 1:** Create each fake as a `class Fake*Repository : *Repository` with an internal `MutableList` (or `MutableMap`) backing store. Implement every interface method against this in-memory state. Expose `givenHabits(vararg habits: Habit)` style helpers for test setup.

Example skeleton:
```kotlin
class FakeHabitRepository : HabitRepository {
    private val habits = mutableListOf<Habit>()
    private val schedules = mutableMapOf<String, HabitSchedule>()

    fun givenHabit(habit: Habit, schedule: HabitSchedule) {
        habits.add(habit); schedules[habit.id] = schedule
    }

    override fun observeActiveHabits(): Flow<List<Habit>> = flowOf(habits.filter { it.isActive && !it.isArchived })
    override suspend fun getActiveHabits(): List<Habit> = habits.filter { it.isActive && !it.isArchived }
    // … all other methods
}
```

**Step 2:** Create `TestBuilders.kt`:
```kotlin
fun buildHabit(id: String = "habit-1", name: String = "Test", …): Habit = Habit(…)
fun buildHabitInstance(id: String = "inst-1", habitId: String = "habit-1", date: LocalDate = LocalDate(2030, 1, 1), …): HabitInstance = HabitInstance(…)
fun buildUser(…): User = User(…)
```

**Step 3:** Commit: `test: add fake repository implementations and test builders`

---

## Task 9 — Migrate Existing Tests to Fakes

**Files:** All 8 existing test files in `commonTest/domain/usecases/` and `commonTest/domain/models/`.

**Step 1:** For each test file:
1. Replace `mockk<XRepository>()` fields with `FakeXRepository()` instances
2. Replace `coEvery { repo.method(…) } returns value` setup with `fake.givenXxx(…)` helpers
3. Remove `coVerify { … }` call-count assertions (fakes maintain state instead)
4. Replace interaction-based assertions with state-based assertions (`assertEquals(expectedStatus, fake.getInstanceById(id)?.status)`)

**Step 2:** Run `./gradlew :composeApp:jvmTest` — 84+ tests pass.

**Step 3:** Remove MockK from `libs.versions.toml` and `build.gradle.kts`. Re-run tests.

**Step 4:** Commit: `test: migrate all tests from MockK to fakes`

---

## Task 10 — New Tests for Untested Use Cases

**Context:** `CompleteHabitUseCase`, `SkipHabitUseCase`, `UndoHabitUseCase`, `GenerateDailyHabitsUseCase`, `ProcessEndOfDayUseCase` have zero tests (Phase 5 debt).

**Files:**
- Create: `…/domain/usecases/CompleteHabitUseCaseTest.kt`
- Create: `…/domain/usecases/SkipHabitUseCaseTest.kt`
- Create: `…/domain/usecases/UndoHabitUseCaseTest.kt`
- Create: `…/domain/usecases/GenerateDailyHabitsUseCaseTest.kt`
- Create: `…/domain/usecases/ProcessEndOfDayUseCaseTest.kt`

**Step 1 per file:** Write failing tests using fakes. Example for `CompleteHabitUseCase`:
```kotlin
@Test fun `given pending binary habit when completing then status is COMPLETED`() = runTest {
    // Given
    val inputInstance = buildHabitInstance(status = HabitStatus.PENDING)
    fakeHabitInstanceRepository.givenInstance(inputInstance)
    fakeHabitRepository.givenHabit(buildHabit(id = inputInstance.habitId))

    // When
    val actualResult = useCase.executeBinary(inputInstance.id, CompletionSource.IN_APP)

    // Then
    assertTrue(actualResult.isSuccess)
    assertEquals(HabitStatus.COMPLETED, fakeHabitInstanceRepository.getInstanceById(inputInstance.id)?.status)
}
```

**Step 2 per file:** Run tests, verify they pass.

**Step 3:** Commit: `test: add unit tests for CompleteHabit, Skip, Undo, Generate, ProcessEndOfDay use cases`

---

## Completion

After all tasks pass:
- Run `./gradlew :composeApp:jvmTest` — all tests green.
- Run `./gradlew :composeApp:assembleDebug` — BUILD SUCCESSFUL.
- Run `./gradlew :composeApp:compileKotlinIosSimulatorArm64` — BUILD SUCCESSFUL.
- Update `AGENTS.md` with answers filled in for Q1–Q5.

