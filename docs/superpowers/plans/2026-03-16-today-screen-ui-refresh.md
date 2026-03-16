# Today Screen UI Refresh Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refresh the Today screen with a pinned progress ring bar, dynamic subtitle, improved habit card visual hierarchy, and section headers with completion counts.

**Architecture:** Add five count fields to `TodayState` computed from the habits list; expose them via a pure extension function `List<TodayHabitUiModel>.computeCounts()` to keep the ViewModel lean and the logic unit-testable. All UI changes are confined to `TodayScreen.kt`.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Material Design 3, kotlin.test + mockk + kotlinx-coroutines-test, Gradle (`:composeApp:jvmTest`)

---

## Chunk 1: State counts — data model, computation, and tests

### Task 1: Add `TodayCounts` helper and `computeCounts` extension

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayCounts.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayState.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayViewModel.kt`

- [ ] **Step 1: Write the failing test**

Create `composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayCountsTest.kt`:

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.today

import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.ScheduleType
import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel
import kotlin.test.Test
import kotlin.test.assertEquals

class TodayCountsTest {

    private fun buildHabit(
        status: HabitStatus,
        cadence: ScheduleType = ScheduleType.DAILY
    ): TodayHabitUiModel = TodayHabitUiModel(
        instanceId = "id-${status.name}-${cadence.name}",
        habitId = "habit-${status.name}",
        name = "Test",
        description = null,
        type = HabitType.BINARY,
        status = status,
        completedValue = null,
        targetValue = null,
        unit = null,
        progressPercentage = 0f,
        isSkipLocked = false,
        currentStreak = 0,
        longestStreak = 0,
        scorePercentage = 0,
        cadence = cadence
    )

    @Test
    fun `given mix of statuses when computing counts then pending excludes non-pending`() {
        // Given
        val inputHabits = listOf(
            buildHabit(HabitStatus.PENDING),
            buildHabit(HabitStatus.COMPLETED),
            buildHabit(HabitStatus.SKIPPED),
            buildHabit(HabitStatus.FAILED),
            buildHabit(HabitStatus.SUSPENDED)
        )

        // When
        val actualCounts = inputHabits.computeCounts()

        // Then
        assertEquals(1, actualCounts.pendingCount)
    }

    @Test
    fun `given suspended habits when computing counts then excluded from all counts`() {
        // Given
        val inputHabits = listOf(
            buildHabit(HabitStatus.SUSPENDED, ScheduleType.DAILY),
            buildHabit(HabitStatus.SUSPENDED, ScheduleType.WEEKLY)
        )

        // When
        val actualCounts = inputHabits.computeCounts()

        // Then
        assertEquals(0, actualCounts.pendingCount)
        assertEquals(0, actualCounts.dailyTotal)
        assertEquals(0, actualCounts.weeklyTotal)
    }

    @Test
    fun `given failed daily habit when computing counts then counted in dailyCompleted not pending`() {
        // Given
        val inputHabits = listOf(
            buildHabit(HabitStatus.FAILED, ScheduleType.DAILY)
        )

        // When
        val actualCounts = inputHabits.computeCounts()

        // Then
        assertEquals(0, actualCounts.pendingCount)
        assertEquals(1, actualCounts.dailyCompleted)
        assertEquals(1, actualCounts.dailyTotal)
    }

    @Test
    fun `given mix of daily habits when computing counts then dailyCompleted matches completed skipped failed`() {
        // Given
        val inputHabits = listOf(
            buildHabit(HabitStatus.PENDING, ScheduleType.DAILY),
            buildHabit(HabitStatus.COMPLETED, ScheduleType.DAILY),
            buildHabit(HabitStatus.SKIPPED, ScheduleType.DAILY),
            buildHabit(HabitStatus.FAILED, ScheduleType.DAILY),
            buildHabit(HabitStatus.SUSPENDED, ScheduleType.DAILY)
        )

        // When
        val actualCounts = inputHabits.computeCounts()

        // Then
        assertEquals(1, actualCounts.pendingCount)
        assertEquals(3, actualCounts.dailyCompleted)
        assertEquals(4, actualCounts.dailyTotal) // suspended excluded
    }

    @Test
    fun `given weekly habits when computing counts then weekly totals populated correctly`() {
        // Given
        val inputHabits = listOf(
            buildHabit(HabitStatus.PENDING, ScheduleType.WEEKLY),
            buildHabit(HabitStatus.COMPLETED, ScheduleType.WEEKLY),
            buildHabit(HabitStatus.SUSPENDED, ScheduleType.WEEKLY)
        )

        // When
        val actualCounts = inputHabits.computeCounts()

        // Then
        assertEquals(1, actualCounts.pendingCount)
        assertEquals(1, actualCounts.weeklyCompleted)
        assertEquals(2, actualCounts.weeklyTotal) // suspended excluded
        assertEquals(0, actualCounts.dailyTotal)
    }

    @Test
    fun `given empty list when computing counts then all counts are zero`() {
        // Given
        val inputHabits = emptyList<TodayHabitUiModel>()

        // When
        val actualCounts = inputHabits.computeCounts()

        // Then
        val expectedCounts = TodayCounts()
        assertEquals(expectedCounts, actualCounts)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd .worktrees/ui-ux-improvements && ./gradlew :composeApp:jvmTest --tests "*.TodayCountsTest" 2>&1 | tail -20
```

Expected: compilation error — `computeCounts` and `TodayCounts` not found.

- [ ] **Step 3: Create `TodayCounts.kt`**

Create `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayCounts.kt`:

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.today

import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel

data class TodayCounts(
    val pendingCount: Int = 0,
    val dailyCompleted: Int = 0,
    val dailyTotal: Int = 0,
    val weeklyCompleted: Int = 0,
    val weeklyTotal: Int = 0
)

fun List<TodayHabitUiModel>.computeCounts(): TodayCounts {
    val resolvedStatuses = setOf(HabitStatus.COMPLETED, HabitStatus.SKIPPED, HabitStatus.FAILED)

    return TodayCounts(
        pendingCount = count { !it.isSuspended && it.isPending },
        dailyTotal = count { it.isDaily && !it.isSuspended },
        dailyCompleted = count { it.isDaily && !it.isSuspended && it.status in resolvedStatuses },
        weeklyTotal = count { it.isWeekly && !it.isSuspended },
        weeklyCompleted = count { it.isWeekly && !it.isSuspended && it.status in resolvedStatuses }
    )
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
cd .worktrees/ui-ux-improvements && ./gradlew :composeApp:jvmTest --tests "*.TodayCountsTest" 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL` — all 6 tests pass.

- [ ] **Step 5: Add 5 fields to `TodayState`**

In `TodayState.kt`, add the five fields from `TodayCounts` directly to the data class (keeping the ViewModel's `_state.update` call simple):

```kotlin
data class TodayState(
    val habits: List<TodayHabitUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val showTimezoneWarning: Boolean = false,
    val previousTimezone: String? = null,
    val error: String? = null,
    val showQuantitativeInputFor: String? = null,
    val pendingCount: Int = 0,
    val dailyCompleted: Int = 0,
    val dailyTotal: Int = 0,
    val weeklyCompleted: Int = 0,
    val weeklyTotal: Int = 0
)
```

- [ ] **Step 6: Populate counts in `TodayViewModel`**

In `TodayViewModel.kt`, in `loadTodayHabits()`, after the `mapNotNull` block that builds `habits`, compute counts and include them in the final `_state.update`:

```kotlin
// Map to UI models
val habits = instances.mapNotNull { instance ->
    val habit = habitRepository.getHabitById(instance.habitId) ?: return@mapNotNull null
    val schedule = habitRepository.getScheduleForHabit(habit.id) ?: return@mapNotNull null
    mapToTodayHabitUiModel(
        instance = instance,
        habit = habit,
        schedule = schedule,
        maxConsecutiveSkips = user?.maxConsecutiveSkips
    )
}

val counts = habits.computeCounts()

_state.update {
    it.copy(
        habits = habits,
        isLoading = false,
        pendingCount = counts.pendingCount,
        dailyCompleted = counts.dailyCompleted,
        dailyTotal = counts.dailyTotal,
        weeklyCompleted = counts.weeklyCompleted,
        weeklyTotal = counts.weeklyTotal
    )
}
```

Add the import at the top of `TodayViewModel.kt`:
```kotlin
import com.ricardocosteira.habitlock.presentation.ui.today.computeCounts
```

- [ ] **Step 7: Run all tests**

```bash
cd .worktrees/ui-ux-improvements && ./gradlew :composeApp:jvmTest 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL` — all existing tests still pass.

- [ ] **Step 8: Commit**

```bash
cd .worktrees/ui-ux-improvements
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayCounts.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayState.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayViewModel.kt \
        composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayCountsTest.kt
git commit -m "feat(today): add count fields to TodayState and computeCounts helper"
```

---

## Chunk 2: TodayScreen UI changes

> **Note:** All commands in this chunk use `cd .worktrees/ui-ux-improvements`. Confirm the worktree is active before starting — it should have been created in the project setup phase at `.worktrees/ui-ux-improvements`.

### Task 2: Add `ProgressRingRow` composable

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayScreen.kt`

- [ ] **Step 1: Add `ProgressRingRow` private composable**

Add the following private composable to `TodayScreen.kt`, after the existing `HabitCard` function:

```kotlin
@Composable
private fun ProgressRingRow(state: TodayState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (state.dailyTotal > 0) {
            RingChip(
                completed = state.dailyCompleted,
                total = state.dailyTotal,
                label = "Daily",
                incompleteColor = MaterialTheme.colorScheme.primary,
                modifier = if (state.weeklyTotal > 0) Modifier.weight(1f) else Modifier
            )
        }
        if (state.weeklyTotal > 0) {
            RingChip(
                completed = state.weeklyCompleted,
                total = state.weeklyTotal,
                label = "Weekly",
                incompleteColor = MaterialTheme.colorScheme.secondary,
                modifier = if (state.dailyTotal > 0) Modifier.weight(1f) else Modifier
            )
        }
    }
}

@Composable
private fun RingChip(
    completed: Int,
    total: Int,
    label: String,
    incompleteColor: Color,
    modifier: Modifier = Modifier
) {
    val isComplete = completed == total
    val ringColor = if (isComplete) MaterialTheme.colorScheme.tertiary else incompleteColor
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val sweepAngle = if (total > 0) 360f * completed / total else 0f

    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.size(28.dp)) {
                val strokeWidthPx = 5.dp.toPx()
                val radius = (size.minDimension - strokeWidthPx) / 2f
                val topLeft = Offset(
                    x = center.x - radius,
                    y = center.y - radius
                )
                val arcSize = Size(radius * 2, radius * 2)

                // Background track
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
                // Filled arc
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
            Column {
                Text(
                    text = "$completed / $total",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

Add the required imports (if not already present):

```kotlin
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
```

- [ ] **Step 2: Wire `ProgressRingRow` into the `else` branch**

In `TodayScreen`, inside the `else` branch of the `when` block, insert the ring row immediately before the `LazyColumn`. The three `val dailyHabits / weeklyHabits / suspendedHabits` lines are unchanged — only insert the `if` block. Replace only this exact anchor:

```kotlin
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
```

With:

```kotlin
                    if (state.dailyTotal > 0 || state.weeklyTotal > 0) {
                        ProgressRingRow(state = state)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
```

- [ ] **Step 3: Build to verify no compilation errors**

```bash
cd .worktrees/ui-ux-improvements && ./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -30
```

Expected: `BUILD SUCCESSFUL`

---

### Task 3: Update TopAppBar title with subtitle

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayScreen.kt`

- [ ] **Step 1: Replace plain title with two-line title composable**

In `TodayScreen`, replace the `TopAppBar` `title` slot:

```kotlin
// Before:
title = { Text("Today") },
```

```kotlin
// After:
title = {
    Column {
        Text(
            text = "Today",
            style = MaterialTheme.typography.titleLarge
        )
        if (!state.isLoading && (state.dailyTotal > 0 || state.weeklyTotal > 0)) {
            val subtitleText = if (state.pendingCount > 0) {
                "${state.pendingCount} ${if (state.pendingCount == 1) "habit" else "habits"} to go"
            } else {
                "All done for today 🎉"
            }
            val subtitleColor = if (state.pendingCount > 0) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.tertiary
            }
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.labelMedium,
                color = subtitleColor
            )
        }
    }
},
```

- [ ] **Step 2: Build to verify no compilation errors**

```bash
cd .worktrees/ui-ux-improvements && ./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -30
```

Expected: `BUILD SUCCESSFUL`

---

### Task 4: Update section headers

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayScreen.kt`

- [ ] **Step 1: Replace Daily section header**

In `TodayScreen`, in the Daily section inside `LazyColumn`, replace:

```kotlin
// Before:
item {
    Text(
        text = "Daily Habits",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
```

```kotlin
// After:
item {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "DAILY HABITS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "${state.dailyCompleted} / ${state.dailyTotal}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

- [ ] **Step 2: Replace Weekly section header**

```kotlin
// Before:
item {
    Text(
        text = "Weekly Habits",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
```

```kotlin
// After:
item {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "WEEKLY HABITS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "${state.weeklyCompleted} / ${state.weeklyTotal}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

- [ ] **Step 3: Replace Suspended section header**

```kotlin
// Before:
item {
    Text(
        text = "Suspended Habits",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
```

```kotlin
// After:
item {
    Text(
        text = "SUSPENDED HABITS",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
```

- [ ] **Step 4: Add required imports**

Add if not already present:
```kotlin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
```

- [ ] **Step 5: Build to verify**

```bash
cd .worktrees/ui-ux-improvements && ./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -30
```

Expected: `BUILD SUCCESSFUL`

---

### Task 5: Update HabitCard — accent bar, button, alpha

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayScreen.kt`

- [ ] **Step 1: Update `HabitCard` signature to accept cadence**

`HabitCard` currently receives a `TodayHabitUiModel` which already has `isDaily`, `isWeekly`, and `isSuspended`. No signature change needed — all information is on `habit`.

- [ ] **Step 2: Replace `cardColor` alpha expressions and add `Modifier.alpha`**

In `HabitCard`, replace the `cardColor` block:

```kotlin
// Before:
val cardColor = when (habit.status) {
    HabitStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    HabitStatus.SKIPPED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    HabitStatus.FAILED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
    HabitStatus.SUSPENDED -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
    HabitStatus.PENDING -> MaterialTheme.colorScheme.surface
}
```

```kotlin
// After:
val cardColor = when (habit.status) {
    HabitStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
    HabitStatus.SKIPPED -> MaterialTheme.colorScheme.surfaceVariant
    HabitStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
    HabitStatus.SUSPENDED -> MaterialTheme.colorScheme.secondaryContainer
    HabitStatus.PENDING -> MaterialTheme.colorScheme.surface
}

val isResolved = habit.status == HabitStatus.COMPLETED
    || habit.status == HabitStatus.SKIPPED
    || habit.status == HabitStatus.FAILED
```

Then update the `Card` modifier to apply alpha when resolved:

```kotlin
// Before:
Card(
    modifier = Modifier
        .fillMaxWidth()
        .combinedClickable(
            onClick = onClick,
            onLongClick = { showMenu = true }
        ),
    colors = CardDefaults.cardColors(containerColor = cardColor)
) {
```

```kotlin
// After:
Card(
    modifier = Modifier
        .fillMaxWidth()
        .then(if (isResolved) Modifier.alpha(0.65f) else Modifier)
        .combinedClickable(
            onClick = onClick,
            onLongClick = { showMenu = true }
        ),
    colors = CardDefaults.cardColors(containerColor = cardColor)
) {
```

Add import if not present:
```kotlin
import androidx.compose.ui.draw.alpha
```

- [ ] **Step 3: Add left accent bar inside the `Box`**

Inside `HabitCard`, replace:

```kotlin
// Before:
Card(...) {
    Box {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
```

```kotlin
// After:
Card(...) {
    Box {
        // Accent bar — rendered first (behind content)
        val accentColor = when {
            habit.isSuspended -> MaterialTheme.colorScheme.surfaceVariant
            habit.isDaily && habit.isPending -> MaterialTheme.colorScheme.primary
            habit.isDaily -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            habit.isWeekly && habit.isPending -> MaterialTheme.colorScheme.secondary
            habit.isWeekly -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
        Box(
            modifier = Modifier
                .matchParentSize() // fills Box height without crashing in LazyColumn (unlike fillMaxHeight)
                .width(3.dp)       // then constrains width to the bar
                .align(Alignment.TopStart)
                .background(accentColor)
        )

        Column(
            modifier = Modifier.padding(start = 19.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
```

Add import if not present:
```kotlin
import androidx.compose.foundation.background
```

- [ ] **Step 4: Replace `IconButton(Check)` with `OutlinedButton`**

In `HabitCard`, inside the `HabitStatus.PENDING` branch, replace:

```kotlin
// Before:
IconButton(onClick = onCompleteClick) {
    Icon(
        Icons.Default.Check,
        contentDescription = "Complete",
        tint = MaterialTheme.colorScheme.primary
    )
}
```

```kotlin
// After:
OutlinedButton(
    onClick = onCompleteClick,
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
) {
    Icon(Icons.Default.Check, contentDescription = "Complete")
}
```

Add imports if not present:
```kotlin
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.OutlinedButton
```

Also in `TodayScreen`, the `LazyColumn`'s `contentPadding` argument uses a fully-qualified reference:
```kotlin
contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
```
Once the import is added, replace that with the bare class name:
```kotlin
contentPadding = PaddingValues(16.dp),
```

Remove the unused import `androidx.compose.material3.IconButton` if `IconButton` is no longer used (the Undo button still uses it, so keep it).

- [ ] **Step 5: Run all tests**

```bash
cd .worktrees/ui-ux-improvements && ./gradlew :composeApp:jvmTest 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit**

```bash
cd .worktrees/ui-ux-improvements
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayScreen.kt
git commit -m "feat(today): refresh UI with progress rings, subtitle, section counts, and card improvements"
```

---

## Final verification

- [ ] **Build the Android app and run on emulator/device**

```bash
cd .worktrees/ui-ux-improvements && ./gradlew :composeApp:assembleDebug 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL` — APK produced in `composeApp/build/outputs/apk/debug/`

- [ ] **Verify on device: all habits pending** — Title shows "N habits to go" in primary colour, rings fill at 0
- [ ] **Verify on device: complete a habit** — Subtitle count decrements, ring fills, card dims and strikethrough appears
- [ ] **Verify on device: all habits done** — Subtitle shows "All done for today 🎉" in green, rings turn green
- [ ] **Verify on device: all habits suspended** — Subtitle hidden, rings hidden, only "Today" in TopAppBar
