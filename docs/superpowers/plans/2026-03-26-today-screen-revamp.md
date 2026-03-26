# Today Screen Revamp Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Full reimplementation of the Today screen with collapsing header, redesigned habit cards, and M3 bottom navigation.

**Architecture:** Incremental refactor — extend existing ViewModel/state, replace UI composables one at a time. One logical change per commit. Data layer changes first, then UI.

**Tech Stack:** Kotlin, Jetpack Compose (Multiplatform), Material 3, SQLDelight, kotlin-inject, Navigation3, kotlinx-datetime

**Design spec:** `docs/superpowers/specs/2026-03-26-today-screen-revamp-design.md`
**Design mockups:** `design system/today_dark_mode_expanded/`, `today_dark_mode_collapsed/`, `today_unified_completed_states_expanded/`, `today_unified_completed_states_collapsed/`

---

## File Map

### New Files
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/models/MotivationalTitles.kt` — title pool + date-seeded selector
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayHeader.kt` — collapsing header (expanded + collapsed)
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/HabitCardNew.kt` — redesigned habit cards (collapsed, expanded, resolved)
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/SectionHeader.kt` — "Today's Focus" / "Weekly Goals" headers
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/HabitLockBottomNav.kt` — NavigationBar composable
- `composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/domain/models/MotivationalTitlesTest.kt` — title function tests
- `composeApp/src/commonMain/composeResources/values/strings_today_revamp.xml` — new string resources

### Modified Files
- `composeApp/src/commonMain/sqldelight/com/ricardocosteira/habitlock/data/database/HabitLock.sq` — add `completedAt` to HabitInstance, `defaultIncrement` to Habit
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/models/HabitInstance.kt` — add `completedAt: Instant?`
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/models/Habit.kt` — add `defaultIncrement: Int`
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/models/StrictnessPreset.kt` — add `fromSettings()` reverse-mapping
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/repositories/HabitInstanceRepository.kt` — add `completedAt` to `updateInstanceStatus`
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/usecases/CompleteHabit.kt` — pass `completedAt` on complete
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/usecases/SkipHabit.kt` — pass `completedAt` on skip
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/usecases/UndoHabit.kt` — clear `completedAt` on undo
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/models/TodayHabitUiModel.kt` — add `completedAt`, `completedAtText`, `defaultIncrement`
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayState.kt` — add `motivationalTitle`, `strictnessPreset`; remove `weeklyResolved`, `weeklyTotal`
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayCounts.kt` — remove weekly counts
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayViewModel.kt` — load motivational title, strictness preset; updated state mapping
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayScreen.kt` — full rewrite of UI composables
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/QuantitativeInputBottomSheet.kt` — keep for "Custom" expanded action
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/HabitLockNavigation.kt` — add NavigationBar + FAB scaffold, update route wiring
- `composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayCountsTest.kt` — update for removed weekly counts
- SQLDelight implementation files (repository impls, data mappers) — update for new columns

### Files to Delete (final cleanup)
- Old composables within TodayScreen.kt: `HabitCard`, `ProgressRingRow`, `RingChip` (replaced by new files)

---

## Task 1: Add `completedAt` to HabitInstance

**Files:**
- Modify: `composeApp/src/commonMain/sqldelight/.../HabitLock.sq`
- Modify: `composeApp/src/commonMain/kotlin/.../domain/models/HabitInstance.kt`
- Modify: `composeApp/src/commonMain/kotlin/.../domain/repositories/HabitInstanceRepository.kt`
- Modify: SQLDelight-generated DAO impls (auto-generated after schema change)
- Modify: Repository implementation that maps DB rows to domain model

- [ ] **Step 1: Add `completedAt` column to HabitInstance table in SQLDelight schema**

In `HabitLock.sq`, add `completedAt` to the HabitInstance table:

```sql
CREATE TABLE HabitInstance (
    id TEXT NOT NULL PRIMARY KEY,
    habitId TEXT NOT NULL,
    date TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    completedValue INTEGER,
    targetValue INTEGER,
    consecutiveSkipsAtCreation INTEGER NOT NULL DEFAULT 0,
    createdAt TEXT NOT NULL,
    completedAt TEXT,
    FOREIGN KEY (habitId) REFERENCES Habit(id) ON DELETE CASCADE
);
```

Also update all queries that insert or select HabitInstance to include `completedAt`. Update `updateInstanceStatus` to accept and set `completedAt`:

```sql
updateInstanceStatus:
UPDATE HabitInstance
SET status = ?, completedValue = ?, completedAt = ?
WHERE id = ?;
```

And update `insertInstance` to include `completedAt`:

```sql
insertInstance:
INSERT INTO HabitInstance(id, habitId, date, status, completedValue, targetValue, consecutiveSkipsAtCreation, createdAt, completedAt)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
```

- [ ] **Step 2: Add `completedAt` field to domain model**

In `HabitInstance.kt`:

```kotlin
data class HabitInstance(
    val id: String,
    val habitId: String,
    val date: LocalDate,
    val status: HabitStatus,
    val completedValue: Int?,
    val targetValue: Int?,
    val consecutiveSkipsAtCreation: Int,
    val createdAt: Instant,
    val completedAt: Instant?
)
```

- [ ] **Step 3: Update repository interface**

In `HabitInstanceRepository.kt`, update `updateInstanceStatus` signature:

```kotlin
suspend fun updateInstanceStatus(
    instanceId: String,
    status: HabitStatus,
    completedValue: Int?,
    completedAt: Instant?
)
```

- [ ] **Step 4: Update repository implementation**

Update the SQLDelight repository impl to pass `completedAt` as an ISO string (same serialization as `createdAt`) to the query. Update the row mapper to parse `completedAt` from the DB row into `Instant?`.

- [ ] **Step 5: Update use cases to pass `completedAt`**

In `CompleteHabit.kt`, both `executeBinary` and `executeQuantitative`: pass `Clock.System.now()` as `completedAt` when calling `updateInstanceStatus` with a resolved status. For quantitative that stays PENDING, pass `null`.

In `SkipHabit.kt`: pass `Clock.System.now()` as `completedAt`.

In `UndoHabit.kt`: pass `null` as `completedAt` when resetting to PENDING.

- [ ] **Step 6: Build and verify compilation**

Run: `./gradlew composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Run existing tests**

Run: `./gradlew composeApp:jvmTest`
Expected: All existing tests pass (some may need updating if they call `updateInstanceStatus`)

- [ ] **Step 8: Commit**

```
feat: add completedAt timestamp to HabitInstance

Tracks when a habit was completed or skipped. Set on
complete/skip, cleared on undo. Persisted in SQLDelight DB.
```

---

## Task 2: Add `defaultIncrement` to Habit

**Files:**
- Modify: `composeApp/src/commonMain/sqldelight/.../HabitLock.sq`
- Modify: `composeApp/src/commonMain/kotlin/.../domain/models/Habit.kt`
- Modify: Repository implementation (row mapper)

- [ ] **Step 1: Add `defaultIncrement` column to Habit table**

In `HabitLock.sq`:

```sql
CREATE TABLE Habit (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    type TEXT NOT NULL DEFAULT 'BINARY',
    targetValue INTEGER,
    unit TEXT,
    defaultIncrement INTEGER NOT NULL DEFAULT 1,
    isActive INTEGER NOT NULL DEFAULT 1,
    isArchived INTEGER NOT NULL DEFAULT 0,
    currentStreak INTEGER NOT NULL DEFAULT 0,
    longestStreak INTEGER NOT NULL DEFAULT 0,
    totalCompletions INTEGER NOT NULL DEFAULT 0,
    expectedCompletions INTEGER NOT NULL DEFAULT 0,
    createdAt TEXT NOT NULL,
    archivedAt TEXT
);
```

Update `insertHabit` query to include `defaultIncrement`.

- [ ] **Step 2: Add `defaultIncrement` field to domain model**

In `Habit.kt`:

```kotlin
data class Habit(
    val id: String,
    val name: String,
    val description: String?,
    val type: HabitType,
    val targetValue: Int?,
    val unit: String?,
    val defaultIncrement: Int,
    val isActive: Boolean,
    val isArchived: Boolean,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCompletions: Int,
    val expectedCompletions: Int,
    val createdAt: Instant,
    val archivedAt: Instant?
)
```

- [ ] **Step 3: Update repository implementation and CreateHabit use case**

Update the row mapper to read `defaultIncrement` from the DB. In `CreateHabit.kt`, add `defaultIncrement` to `CreateHabitParams` (default 1) and pass it through when constructing the `Habit`:

```kotlin
data class CreateHabitParams(
    val name: String,
    val description: String?,
    val type: HabitType,
    val targetValue: Int?,
    val unit: String?,
    val defaultIncrement: Int = 1,
    val scheduleType: ScheduleType = ScheduleType.DAILY,
    val quota: Int = 1,
    val weekStartDay: DayOfWeek = DayOfWeek.MONDAY,
    val specificDays: Set<DayOfWeek>? = null,
    val reminder: HabitReminder?
)
```

- [ ] **Step 4: Build and run tests**

Run: `./gradlew composeApp:compileKotlinJvm && ./gradlew composeApp:jvmTest`
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 5: Commit**

```
feat: add defaultIncrement to Habit model

Quantitative habits can define a default increment for quick-add
buttons (e.g., +250 ML, +5 pages). Defaults to 1. No UI for
setting this yet — separate brainstorming session.
```

---

## Task 3: Add motivational titles

**Files:**
- Create: `composeApp/src/commonMain/kotlin/.../domain/models/MotivationalTitles.kt`
- Create: `composeApp/src/commonTest/kotlin/.../domain/models/MotivationalTitlesTest.kt`

- [ ] **Step 1: Write failing tests**

In `MotivationalTitlesTest.kt`:

```kotlin
package com.ricardocosteira.habitlock.domain.models

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MotivationalTitlesTest {

    @Test
    fun given_a_date_when_getting_title_then_returns_non_blank_string() {
        val date = LocalDate(2026, 3, 26)

        val actualTitle = motivationalTitleForDate(date)

        assertTrue(actualTitle.isNotBlank())
    }

    @Test
    fun given_same_date_when_getting_title_twice_then_returns_same_title() {
        val date = LocalDate(2026, 3, 26)

        val firstCall = motivationalTitleForDate(date)
        val secondCall = motivationalTitleForDate(date)

        assertEquals(firstCall, secondCall)
    }

    @Test
    fun given_different_dates_when_getting_titles_then_not_all_are_the_same() {
        val titles = (1..30).map { day ->
            motivationalTitleForDate(LocalDate(2026, 3, day))
        }.toSet()

        assertTrue(titles.size > 1, "Expected different titles for different dates, got: $titles")
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew composeApp:jvmTest --tests "*MotivationalTitlesTest*"`
Expected: FAIL — `motivationalTitleForDate` not found

- [ ] **Step 3: Implement**

In `MotivationalTitles.kt`:

```kotlin
package com.ricardocosteira.habitlock.domain.models

import kotlinx.datetime.LocalDate

private val TITLES: List<String> = listOf(
    "Quiet discipline",
    "Small steps, big change",
    "Trust the process",
    "One day at a time",
    "Show up for yourself",
    "Progress, not perfection",
    "Build the habit, build the life",
    "Consistency compounds",
    "The work is the reward",
    "Stay the course",
    "Begin again, always",
    "Discipline is freedom",
    "Earn your rest",
    "Action over intention",
    "Make it count"
)

fun motivationalTitleForDate(date: LocalDate): String {
    val seed: Int = date.toEpochDays()
    val index: Int = (seed % TITLES.size + TITLES.size) % TITLES.size
    return TITLES[index]
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew composeApp:jvmTest --tests "*MotivationalTitlesTest*"`
Expected: PASS — all 3 tests green

- [ ] **Step 5: Commit**

```
feat: add motivational titles pool with date-seeded rotation

Pure function that deterministically picks a title per day from
a curated pool. No DB or network dependency.
```

---

## Task 4: Add strictness preset derivation + update TodayState

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/.../domain/models/StrictnessPreset.kt`
- Modify: `composeApp/src/commonMain/kotlin/.../presentation/ui/today/TodayState.kt`
- Modify: `composeApp/src/commonMain/kotlin/.../presentation/ui/today/TodayCounts.kt`
- Modify: `composeApp/src/commonMain/kotlin/.../presentation/models/TodayHabitUiModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/.../presentation/ui/today/TodayViewModel.kt`
- Modify: `composeApp/src/commonTest/kotlin/.../presentation/ui/today/TodayCountsTest.kt`

- [ ] **Step 1: Add `fromSettings()` to StrictnessPreset**

In `StrictnessPreset.kt`, add a companion function:

```kotlin
companion object {
    val DEFAULT = BALANCED

    fun fromSettings(settings: UserStrictnessSettings): StrictnessPreset? {
        return entries.firstOrNull { it.toUserSettings() == settings }
    }
}
```

- [ ] **Step 2: Add `completedAt`, `completedAtText`, `defaultIncrement` to TodayHabitUiModel**

In `TodayHabitUiModel.kt`:

```kotlin
data class TodayHabitUiModel(
    val instanceId: String,
    val habitId: String,
    val name: String,
    val description: String?,
    val type: HabitType,
    val status: HabitStatus,
    val completedValue: Int?,
    val targetValue: Int?,
    val unit: String?,
    val defaultIncrement: Int,
    val progressPercentage: Float,
    val isSkipLocked: Boolean,
    val currentStreak: Int,
    val longestStreak: Int,
    val scorePercentage: Int,
    val cadence: ScheduleType,
    val completedAt: Instant?,
    val completedAtText: String?
)
```

Update `mapToTodayHabitUiModel` to populate the new fields:

```kotlin
fun mapToTodayHabitUiModel(
    instance: HabitInstance,
    habit: Habit,
    schedule: HabitSchedule,
    maxConsecutiveSkips: Int?,
    userTimezone: TimeZone
): TodayHabitUiModel {
    val score = habit.calculateScore()
    return TodayHabitUiModel(
        instanceId = instance.id,
        habitId = habit.id,
        name = habit.name,
        description = habit.description,
        type = habit.type,
        status = instance.status,
        completedValue = instance.completedValue,
        targetValue = instance.targetValue,
        unit = habit.unit,
        defaultIncrement = habit.defaultIncrement,
        progressPercentage = instance.progressPercentage(),
        isSkipLocked = instance.isSkipLocked(maxConsecutiveSkips),
        currentStreak = habit.currentStreak,
        longestStreak = habit.longestStreak,
        scorePercentage = score.percentage,
        cadence = schedule.scheduleType,
        completedAt = instance.completedAt,
        completedAtText = instance.completedAt?.let { formatCompletedAtTime(it, userTimezone) }
    )
}

private fun formatCompletedAtTime(instant: Instant, timezone: TimeZone): String {
    val localTime = instant.toLocalDateTime(timezone).time
    val hour = if (localTime.hour % 12 == 0) 12 else localTime.hour % 12
    val minute = localTime.minute.toString().padStart(2, '0')
    val amPm = if (localTime.hour < 12) "AM" else "PM"
    return "$hour:$minute $amPm"
}
```

Note: `mapToTodayHabitUiModel` now requires `userTimezone: TimeZone` — update call sites.

- [ ] **Step 3: Remove weekly counts from TodayCounts**

In `TodayCounts.kt`:

```kotlin
data class TodayCounts(
    val pendingCount: Int = 0,
    val dailyResolved: Int = 0,
    val dailyTotal: Int = 0
)

fun List<TodayHabitUiModel>.computeCounts(): TodayCounts {
    val resolvedStatuses = setOf(HabitStatus.COMPLETED, HabitStatus.SKIPPED, HabitStatus.FAILED)

    return TodayCounts(
        pendingCount = count { !it.isSuspended && it.isPending },
        dailyTotal = count { it.isDaily && !it.isSuspended },
        dailyResolved = count { it.isDaily && !it.isSuspended && it.status in resolvedStatuses }
    )
}
```

- [ ] **Step 4: Update TodayCountsTest**

Remove the weekly-related tests. Keep the daily and pending count tests. Update assertions that referenced `weeklyResolved` or `weeklyTotal`.

- [ ] **Step 5: Update TodayState**

In `TodayState.kt`:

```kotlin
data class TodayState(
    val habits: List<TodayHabitUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val showTimezoneWarning: Boolean = false,
    val previousTimezone: String? = null,
    val error: String? = null,
    val showQuantitativeInputFor: String? = null,
    val pendingCount: Int = 0,
    val dailyResolved: Int = 0,
    val dailyTotal: Int = 0,
    val motivationalTitle: String = "",
    val strictnessPreset: StrictnessPreset? = null
)
```

- [ ] **Step 6: Update TodayViewModel**

In `loadTodayHabits()`, after loading the user:

```kotlin
val userTimezone = user?.timezone ?: TimeZone.currentSystemDefault()
val today = Clock.System.now().toLocalDate(userTimezone)

// Derive strictness preset from user settings
val strictnessPreset = user?.let {
    val settings = UserStrictnessSettings(
        undoPolicy = it.undoPolicy,
        maxSnoozesPerHabitPerDay = it.maxSnoozesPerHabitPerDay,
        maxConsecutiveSkips = it.maxConsecutiveSkips,
        maxSnoozeDurationMinutes = it.maxSnoozeDurationMinutes
    )
    StrictnessPreset.fromSettings(settings)
}

val motivationalTitle = motivationalTitleForDate(today)
```

Update the mapper call to pass `userTimezone`. Update the state copy to include:

```kotlin
_state.update {
    it.copy(
        habits = habits,
        isLoading = false,
        pendingCount = counts.pendingCount,
        dailyResolved = counts.dailyResolved,
        dailyTotal = counts.dailyTotal,
        motivationalTitle = motivationalTitle,
        strictnessPreset = strictnessPreset
    )
}
```

- [ ] **Step 7: Build and run all tests**

Run: `./gradlew composeApp:compileKotlinJvm && ./gradlew composeApp:jvmTest`
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 8: Commit**

```
feat: add motivational title and strictness preset to TodayState

Derives strictness preset from user settings via reverse-mapping.
Removes weekly counts from TodayCounts (ring is daily-only).
Adds completedAt, completedAtText, and defaultIncrement to
TodayHabitUiModel.
```

---

## Task 5: Add string resources for new UI

**Files:**
- Create: `composeApp/src/commonMain/composeResources/values/strings_today_revamp.xml`

- [ ] **Step 1: Create new string resources**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Header -->
    <string name="today_header_remaining">%d habits remaining today</string>
    <string name="today_header_all_done">All done for today!</string>
    <string name="today_header_day_label">Day</string>
    <string name="today_header_done_label">Done</string>

    <!-- Sections -->
    <string name="today_section_focus">Today\'s Focus</string>
    <string name="today_section_weekly">Weekly Goals</string>
    <string name="today_section_this_week">This Week</string>

    <!-- Card badges -->
    <string name="today_badge_pending">Pending</string>
    <string name="today_badge_in_progress">In Progress</string>

    <!-- Card actions -->
    <string name="today_action_complete">Complete</string>
    <string name="today_action_custom">Custom</string>
    <string name="today_action_increment">+%d %s</string>
    <string name="today_action_increment_short">+%d</string>

    <!-- Resolved row -->
    <string name="today_resolved_completed_at">Completed at %s</string>
    <string name="today_resolved_skipped_at">Skipped at %s</string>
    <string name="today_resolved_failed">Failed</string>
    <string name="today_resolved_goal_reached">Goal reached: %d / %d %s</string>

    <!-- Bottom nav -->
    <string name="nav_today">Today</string>
    <string name="nav_history">History</string>
    <string name="nav_settings">Settings</string>
    <string name="nav_cd_add_habit">Add habit</string>
</resources>
```

- [ ] **Step 2: Build to verify resources compile**

Run: `./gradlew composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```
copy: add string resources for today screen revamp
```

---

## Task 6: Implement NavigationBar + FAB in scaffold

**Files:**
- Create: `composeApp/src/commonMain/kotlin/.../presentation/navigation/HabitLockBottomNav.kt`
- Modify: `composeApp/src/commonMain/kotlin/.../presentation/navigation/HabitLockNavigation.kt`

- [ ] **Step 1: Create HabitLockBottomNav composable**

In `HabitLockBottomNav.kt`:

```kotlin
package com.ricardocosteira.habitlock.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
// Import appropriate icons from composables.com/icons

enum class BottomNavTab {
    TODAY,
    HISTORY,
    SETTINGS
}

@Composable
fun HabitLockBottomNav(
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = currentTab == BottomNavTab.TODAY,
            onClick = { onTabSelected(BottomNavTab.TODAY) },
            icon = { /* Today icon — select from composables.com/icons */ },
            label = { Text(stringResource(Res.string.nav_today)) }
        )
        NavigationBarItem(
            selected = currentTab == BottomNavTab.HISTORY,
            onClick = { onTabSelected(BottomNavTab.HISTORY) },
            icon = { /* History icon */ },
            label = { Text(stringResource(Res.string.nav_history)) }
        )
        NavigationBarItem(
            selected = currentTab == BottomNavTab.SETTINGS,
            onClick = { onTabSelected(BottomNavTab.SETTINGS) },
            icon = { /* Settings icon */ },
            label = { Text(stringResource(Res.string.nav_settings)) }
        )
    }
}
```

Note: Select specific icons from composables.com/icons during implementation. Use filled variant for selected state, outlined for unselected.

- [ ] **Step 2: Update HabitLockNavigation to use Scaffold with bottom nav + FAB**

In `HabitLockNavigation.kt`, wrap the navigation content in a `Scaffold` with `bottomBar` and `floatingActionButton`. Derive `currentTab` from the current back stack entry. The NavigationBar should only show on Today, Calendar, and Settings routes (not on CreateHabit, EditHabit, etc.).

Remove `onCalendarClick` and `onSettingsClick` from TodayScreen entry — navigation is now handled by the bottom nav.

Update the Today route entry to remove those callbacks:

```kotlin
entry<Today> {
    TodayScreen(
        onNavigateToHabitDetail = { backStack.add(HabitDetail(it)) },
        onNavigateToCreateHabit = { backStack.add(CreateHabit) },
        onEditHabit = { backStack.add(EditHabit(it)) },
        snackbarHostState = snackbarHostState
    )
}
```

- [ ] **Step 3: Build and verify**

Run: `./gradlew composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```
feat: add M3 NavigationBar with Today/History/Settings tabs

Replaces TopAppBar calendar/settings icons. FAB for creating
habits positioned above the nav bar via Scaffold.
```

---

## Task 7: Implement collapsing header

**Files:**
- Create: `composeApp/src/commonMain/kotlin/.../presentation/ui/today/TodayHeader.kt`
- Modify: `composeApp/src/commonMain/kotlin/.../presentation/ui/today/TodayScreen.kt`

- [ ] **Step 1: Create TodayHeader composable**

In `TodayHeader.kt`, implement the expanded and collapsed header content slots. The user will provide a `DynamicCollapsingToolbar` composable — for now, use `AnimatedContent` with `ContentTransform(fadeIn, fadeOut)` as the crossfade mechanism, switching between expanded and collapsed based on a `isCollapsed: Boolean` parameter.

**Expanded content:**
- Row: left column (title + subtitle + pill) + right (progress ring)
- `MotivationalTitle`: Manrope extrabold, ~28sp, `MaterialTheme.colorScheme.primary`
- `PendingCountSubtitle`: "X habits remaining today" or "All done for today!"
- `StrictnessPresetPill`: rounded pill, primary-container bg at 30% alpha, border at 20% alpha, pulsing dot + preset name
- `DailyProgressRing`: 88dp Canvas arc, 5dp stroke, rounded caps, percentage + "Day" label

**Collapsed content:**
- Row: title (18sp, single line) + pill inline on left, percentage + "Done" on right
- No ring, no subtitle

Reference `design system/today_dark_mode_expanded/code.html` for exact spacing and styling values.

- [ ] **Step 2: Wire header into TodayScreen**

Replace the current `TopAppBar` in TodayScreen with `TodayHeader`. Pass state values: `motivationalTitle`, `pendingCount`, `strictnessPreset`, `dailyResolved`, `dailyTotal`. The `isCollapsed` state should be driven by `LazyListState.firstVisibleItemIndex > 0` or similar scroll detection.

- [ ] **Step 3: Build and verify visually**

Run: `./gradlew composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL. Run on device/emulator to verify header renders correctly.

- [ ] **Step 4: Commit**

```
feat: implement collapsing header with crossfade transition

Expanded: motivational title, pending count, strictness pill,
daily progress ring. Collapsed: single-line title, pill, and
percentage text. Crossfade for now, animated transition in
follow-up PR.
```

---

## Task 8: Implement section headers

**Files:**
- Create: `composeApp/src/commonMain/kotlin/.../presentation/ui/today/SectionHeader.kt`

- [ ] **Step 1: Create SectionHeader composable**

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SectionHeader(
    title: String,
    trailingLabel: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = trailingLabel.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

- [ ] **Step 2: Commit**

```
feat: add SectionHeader composable for Today's Focus / Weekly Goals
```

---

## Task 9: Implement redesigned habit cards

**Files:**
- Create: `composeApp/src/commonMain/kotlin/.../presentation/ui/today/HabitCardNew.kt`

This is the largest UI task. The file contains three main composables: `CollapsedPendingCard`, `ExpandedPendingCard`, and `ResolvedHabitRow`, plus a parent `HabitCard` that switches between them.

- [ ] **Step 1: Implement `CollapsedPendingCard`**

Compact row card. Binary: name + description left, checkmark + SKIP right. Quantitative: progress counter top + name below left, +N button + SKIP right, thin progress bar below. Corner radius 24dp, `surfaceContainerLow` background.

Reference `design system/today_dark_mode_collapsed/code.html` for exact styling.

- [ ] **Step 2: Implement `ExpandedPendingCard`**

Full card with action buttons. Binary: description subtitle + name + PENDING badge, COMPLETE + SKIP buttons. Quantitative: progress counter + name + badge, progress bar, +N UNIT + CUSTOM + SKIP + conditional UNDO. Badge shows "IN PROGRESS" when `completedValue > 0`.

CUSTOM button opens the existing `QuantitativeInputBottomSheet` via a callback.

Reference `design system/today_dark_mode_expanded/code.html` for exact styling.

- [ ] **Step 3: Implement `ResolvedHabitRow`**

Compact row with `primaryContainer` at 80% alpha, 16dp radius. Check icon circle (40dp) + name (uppercase) + subtitle (completedAtText or status) + undo button (only for completed/skipped).

Reference `design system/today_unified_completed_states_expanded/code.html` for exact styling.

- [ ] **Step 4: Implement parent `HabitCard` with expand/collapse**

```kotlin
@Composable
fun HabitCard(
    habit: TodayHabitUiModel,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    onIncrementProgress: () -> Unit,
    onCustomProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isResolved: Boolean = habit.isCompleted || habit.isSkipped || habit.isFailed

    if (isResolved) {
        ResolvedHabitRow(
            habit = habit,
            onUndo = onUndo,
            modifier = modifier
        )
    } else {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { expanded ->
            if (expanded) {
                ExpandedPendingCard(
                    habit = habit,
                    onComplete = onComplete,
                    onSkip = onSkip,
                    onUndo = onUndo,
                    onIncrementProgress = onIncrementProgress,
                    onCustomProgress = onCustomProgress,
                    onClick = onToggleExpand,
                    modifier = modifier
                )
            } else {
                CollapsedPendingCard(
                    habit = habit,
                    onComplete = onComplete,
                    onSkip = onSkip,
                    onIncrementProgress = onIncrementProgress,
                    onClick = onToggleExpand,
                    modifier = modifier
                )
            }
        }
    }
}
```

- [ ] **Step 5: Build and verify**

Run: `./gradlew composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```
feat: implement redesigned habit cards with expand/collapse

Collapsed: compact row with quick actions. Expanded: full card
with all action buttons. Resolved: compact green row with undo.
Accordion interaction — one card expanded at a time.
```

---

## Task 10: Rewrite TodayScreen LazyColumn with new sections

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/.../presentation/ui/today/TodayScreen.kt`

- [ ] **Step 1: Rewrite the private TodayScreen composable**

Replace the current Scaffold + TopAppBar + LazyColumn with the new structure:

- Remove TopAppBar (replaced by TodayHeader in Task 7)
- Remove FAB from here (now in HabitLockNavigation scaffold)
- Track `expandedCardId: String?` state for accordion behavior
- Split habits into daily/weekly, pending/resolved using partition

LazyColumn structure:
```
TodayHeader (sticky, already added in Task 7)
SectionHeader("Today's Focus", formattedDate)
  Pending daily cards (collapsed by default, tap to expand)
  Divider (if there are resolved daily habits)
  Resolved daily rows
SectionHeader("Weekly Goals", "This Week")
  Pending weekly cards
  Divider (if there are resolved weekly habits)
  Resolved weekly rows
Bottom spacer (for nav bar clearance)
```

Wire up `HabitCard` callbacks to ViewModel actions:
- `onComplete` → `viewModel.completeHabit(instanceId)` (for binary) or `viewModel.completeQuantitativeHabit(instanceId, defaultIncrement)` (for quantitative collapsed +N)
- `onIncrementProgress` → `viewModel.completeQuantitativeHabit(instanceId, habit.defaultIncrement)`
- `onCustomProgress` → trigger bottom sheet (set `showQuantitativeInputFor`)
- `onSkip` → `viewModel.skipHabit(instanceId)`
- `onUndo` → `viewModel.undoHabit(instanceId)`
- `onToggleExpand` → toggle `expandedCardId`

- [ ] **Step 2: Update the public TodayScreen entry point**

Remove `onCalendarClick` and `onSettingsClick` parameters (handled by bottom nav now). Update the signature:

```kotlin
@Composable
fun TodayScreen(
    onNavigateToHabitDetail: (String) -> Unit,
    onNavigateToCreateHabit: () -> Unit,
    onEditHabit: (String) -> Unit,
    snackbarHostState: SnackbarHostState
)
```

- [ ] **Step 3: Keep QuantitativeInputBottomSheet integration**

The bottom sheet is triggered from the expanded card's "CUSTOM" button. Keep the existing `ModalBottomSheet` wiring that shows when `state.showQuantitativeInputFor != null`.

- [ ] **Step 4: Remove old composables**

Delete from TodayScreen.kt: `HabitCard` (old), `ProgressRingRow`, `RingChip`, and their associated constants (`ACCENT_BAR_WIDTH`, `RING_CANVAS_SIZE`, etc.).

Keep: `TimezoneWarningBanner`, `EmptyHabitsMessage` (still used).

- [ ] **Step 5: Build and verify**

Run: `./gradlew composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL. Run on device/emulator for visual verification.

- [ ] **Step 6: Commit**

```
feat: rewrite TodayScreen with new sections and card layout

Today's Focus (daily) and Weekly Goals sections with collapsed
cards, expanded interaction, and resolved habit rows. Removes
old HabitCard, ProgressRingRow, and RingChip composables.
```

---

## Task 11: Add scroll-aware nav bar hide/show

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/.../presentation/navigation/HabitLockNavigation.kt`

- [ ] **Step 1: Implement scroll-aware visibility**

Add a `NestedScrollConnection` that tracks scroll direction. When scrolling down, animate the NavigationBar sliding off-screen. When scrolling up or reaching the end of the list, animate it back.

```kotlin
val navBarVisible = remember { mutableStateOf(true) }
val nestedScrollConnection = remember {
    object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (available.y < -1f) navBarVisible.value = false  // scrolling down
            if (available.y > 1f) navBarVisible.value = true    // scrolling up
            return Offset.Zero
        }
    }
}
```

Wrap the NavigationBar in an `AnimatedVisibility` with `slideInVertically`/`slideOutVertically`:

```kotlin
AnimatedVisibility(
    visible = navBarVisible.value,
    enter = slideInVertically(initialOffsetY = { it }),
    exit = slideOutVertically(targetOffsetY = { it })
) {
    HabitLockBottomNav(...)
}
```

Also detect when the LazyColumn reaches the bottom and force-show the nav bar. This can be done by observing `LazyListState.layoutInfo.visibleItemsInfo` and checking if the last item is visible. Pass this state up from TodayScreen via a callback or shared state.

- [ ] **Step 2: FAB stays visible**

Ensure the FAB is outside the `AnimatedVisibility` wrapper. It should always be visible regardless of nav bar state. Adjust FAB bottom padding to account for nav bar height when visible vs hidden.

- [ ] **Step 3: Build and verify**

Run on device/emulator. Scroll down — nav bar should slide out. Scroll up — nav bar should slide in. Reach bottom — nav bar should appear. FAB should always be visible.

- [ ] **Step 4: Commit**

```
feat: add scroll-aware hide/show for bottom navigation bar

Nav bar slides down on scroll-down, reappears on scroll-up or
when reaching the bottom of the list. FAB stays visible at all
times.
```

---

## Task 12: Update TodayViewModel for inline quantitative actions

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/.../presentation/ui/today/TodayViewModel.kt`

- [ ] **Step 1: Update `completeHabit` to handle inline increment**

Currently `completeHabit(instanceId)` opens the bottom sheet for quantitative habits. Now, collapsed cards use inline +N buttons that bypass the sheet. Add a new method:

```kotlin
fun incrementHabitProgress(instanceId: String) {
    viewModelScope.launch {
        val habit = _state.value.habits.find { it.instanceId == instanceId } ?: return@launch

        val result = completeHabit.executeQuantitative(
            instanceId = instanceId,
            deltaValue = habit.defaultIncrement,
            source = CompletionSource.IN_APP
        )

        result.fold(
            onSuccess = { updatedInstance ->
                loadTodayHabits()
                if (updatedInstance.isQuantitativeComplete()) {
                    _events.emit(TodayEvent.HabitCompleted)
                } else {
                    _events.emit(TodayEvent.ProgressAdded)
                }
            },
            onFailure = { error ->
                _events.emit(TodayEvent.ShowError(error.message))
            }
        )
    }
}
```

The existing `completeHabit(instanceId)` is still used for binary habits (complete button). The existing `completeQuantitativeHabit(instanceId, value)` is used by the "CUSTOM" bottom sheet.

- [ ] **Step 2: Build and verify**

Run: `./gradlew composeApp:compileKotlinJvm && ./gradlew composeApp:jvmTest`
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 3: Commit**

```
feat: add incrementHabitProgress for inline quick-add buttons

Uses defaultIncrement from the habit model. Called by collapsed
card +N buttons. Custom bottom sheet still available from
expanded card.
```

---

## Task 13: Final cleanup and verification

**Files:**
- Modify: Various — remove dead code, unused imports, stale string resources

- [ ] **Step 1: Remove unused code**

- Delete old string resources from `strings_today.xml` that are no longer referenced (e.g., `today_section_daily_habits`, `today_section_weekly_habits`, `today_section_suspended_habits`, `today_cd_calendar`, `today_cd_settings`)
- Remove `onCalendarClick` / `onSettingsClick` from any remaining references
- Remove unused imports across all modified files
- Remove old constants from TodayScreen.kt if any remain

- [ ] **Step 2: Full build and test**

Run: `./gradlew composeApp:compileKotlinJvm && ./gradlew composeApp:jvmTest`
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 3: Visual verification**

Run the app on a device/emulator. Verify:
- Header collapses/expands with crossfade on scroll
- Motivational title shows and strictness pill renders
- Progress ring shows daily progress
- Cards start collapsed, tap to expand works (accordion)
- Binary cards: checkmark + skip on collapsed, COMPLETE + SKIP on expanded
- Quantitative cards: +N + SKIP on collapsed, +N + CUSTOM + SKIP + UNDO on expanded
- "IN PROGRESS" badge for quantitative with partial progress
- Resolved rows show green with completion timestamp
- Bottom nav works for Today/History/Settings
- Nav bar hides on scroll down, shows on scroll up / at bottom
- FAB stays visible, navigates to create habit
- Bottom sheet opens from CUSTOM button on expanded quantitative card

- [ ] **Step 4: Commit**

```
refactor: clean up removed code and stale resources from today screen revamp
```
