# Swipe Actions on Habit Cards — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add swipe-to-archive (right), swipe-to-edit (left), and swipe-to-delete (far left) gestures on all habit cards in the Today screen.

**Architecture:** A `SwipeableHabitCard` composable wraps existing habit cards with an `AnchoredDraggableState` providing three snap zones. The ViewModel handles deferred delete/archive with undo via snackbar events. A KMP `expect`/`actual` `HapticController` provides distinct haptic feedback per zone on each platform.

**Tech Stack:** Compose Foundation `AnchoredDraggableState`, Material3 color tokens, KMP `expect`/`actual` for haptics, Turbine for Flow testing, Roborazzi for screenshot tests.

**Design spec:** `docs/superpowers/specs/2026-03-31-swipe-actions-design.md`

---

## File Map

### New Files

| File | Responsibility |
|------|---------------|
| `composeApp/src/commonMain/kotlin/.../presentation/ui/today/SwipeableHabitCard.kt` | Composable wrapping habit cards with AnchoredDraggable swipe gesture layer |
| `composeApp/src/commonMain/kotlin/.../presentation/ui/haptics/HapticController.kt` | `expect class` defining `tick()`, `click()`, `heavyClick()` |
| `composeApp/src/androidMain/kotlin/.../presentation/ui/haptics/HapticController.android.kt` | Android `actual class` with layered VibrationEffect strategy |
| `composeApp/src/iosMain/kotlin/.../presentation/ui/haptics/HapticController.ios.kt` | iOS `actual class` with UIImpactFeedbackGenerator |
| `composeApp/src/jvmMain/kotlin/.../presentation/ui/haptics/HapticController.jvm.kt` | JVM `actual class` (no-op for desktop) |
| `composeApp/src/commonMain/composeResources/values/strings_swipe_actions.xml` | String resources for snackbar messages |
| `composeApp/src/commonTest/kotlin/.../presentation/ui/today/TodayViewModelSwipeTest.kt` | Integration tests for delete/undo and archive/undo flows |

### Modified Files

| File | Changes |
|------|---------|
| `composeApp/src/commonMain/kotlin/.../presentation/ui/today/TodayScreen.kt` | Wrap `HabitCard` calls in `SwipeableHabitCard`, add archive/edit/delete callbacks, handle undo snackbar events |
| `composeApp/src/commonMain/kotlin/.../presentation/ui/today/TodayViewModel.kt` | Add `deleteHabit()`, `undoDelete()`, `archiveHabitWithUndo()`, `undoArchive()` methods |
| `composeApp/src/commonMain/kotlin/.../presentation/ui/today/TodayState.kt` | Add `pendingUndo: UndoState?` sealed class for tracking deferred operations |

All paths under `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/`.

---

## Task 1: String Resources

**Files:**
- Create: `composeApp/src/commonMain/composeResources/values/strings_swipe_actions.xml`

- [ ] **Step 1: Create string resources file**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="swipe_habit_archived">Habit archived</string>
    <string name="swipe_habit_deleted">Habit deleted</string>
    <string name="swipe_undo">Undo</string>
</resources>
```

- [ ] **Step 2: Verify resources compile**

Run: `./gradlew composeApp:generateComposeResClass`
Expected: BUILD SUCCESSFUL — `Res.string.swipe_habit_archived`, `Res.string.swipe_habit_deleted`, `Res.string.swipe_undo` generated.

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/composeResources/values/strings_swipe_actions.xml
git commit -m "feat(swipe): add string resources for swipe action snackbars"
```

---

## Task 2: Undo State in TodayState

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayState.kt`

- [ ] **Step 1: Add UndoOperation sealed interface and pendingUndo field**

Add after the existing imports in `TodayState.kt`:

```kotlin
sealed interface UndoOperation {
    val habitId: String
    val habitName: String

    data class Delete(
        override val habitId: String,
        override val habitName: String
    ) : UndoOperation

    data class Archive(
        override val habitId: String,
        override val habitName: String
    ) : UndoOperation
}
```

Add to the `TodayState` data class:

```kotlin
val pendingUndo: UndoOperation? = null,
```

- [ ] **Step 2: Add undo snackbar events to TodayEvent**

Add inside the `TodayEvent` sealed interface:

```kotlin
data class HabitArchived(val habitName: String) : TodayEvent
data class HabitDeleted(val habitName: String) : TodayEvent
data object UndoCompleted : TodayEvent
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayState.kt
git commit -m "feat(swipe): add UndoOperation state and swipe events to TodayState"
```

---

## Task 3: ViewModel — Delete with Deferred Undo

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayViewModel.kt`
- Test: `composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayViewModelSwipeTest.kt`

Before implementing, check the existing test infrastructure — there are no ViewModel tests yet. We need to create fakes for the data sources. Check what data sources `HabitRepositoryImpl` depends on and create in-memory fakes. The repository and use case layers should be real.

- [ ] **Step 1: Explore data source dependencies**

Read `HabitRepositoryImpl` constructor to identify what data sources (database queries, etc.) need to be faked. Read the use case classes (`CompleteHabit`, `SkipHabit`, `UndoHabit`, `UndoLastIncrement`, `GenerateDailyHabits`, `ProcessEndOfDay`) to understand their constructor dependencies. This will inform the test setup.

- [ ] **Step 2: Create test fakes and test setup**

Create `TodayViewModelSwipeTest.kt` with:
- In-memory fake data sources (based on what you discovered in Step 1)
- Real repository implementations backed by those fakes
- Real use case instances
- A helper to create the ViewModel with all real dependencies except the data source layer

- [ ] **Step 3: Write the failing test for deleteHabit**

```kotlin
@Test
fun `deleteHabit removes habit from state and emits HabitDeleted event`() = runTest {
    // Given
    val inputHabitId = "habit-1"
    val inputHabitName = "Morning Meditation"
    // ... set up a habit in the fake data source

    // When
    viewModel.deleteHabit(inputHabitId)

    // Then
    val actualState = viewModel.state.value
    assertTrue(actualState.pendingDaily.none { it.habitId == inputHabitId })
    assertNotNull(actualState.pendingUndo)
    assertTrue(actualState.pendingUndo is UndoOperation.Delete)
    assertEquals(inputHabitId, actualState.pendingUndo?.habitId)
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `./gradlew composeApp:jvmTest --tests "*TodayViewModelSwipeTest*" -x verifyRoborazziJvm`
Expected: FAIL — `deleteHabit` method does not exist.

- [ ] **Step 5: Implement deleteHabit in TodayViewModel**

Add to `TodayViewModel`:

```kotlin
private var undoJob: Job? = null

fun deleteHabit(habitId: String) {
    val habit: TodayHabitUiModel = _state.value.habits.find { it.habitId == habitId } ?: return

    // Remove from UI immediately
    removeHabitFromState(habitId)

    // Set undo state
    _state.update { it.copy(pendingUndo = UndoOperation.Delete(habitId, habit.name)) }

    // Emit event for snackbar
    viewModelScope.launch { _events.emit(TodayEvent.HabitDeleted(habit.name)) }

    // Schedule actual delete after snackbar timeout
    undoJob?.cancel()
    undoJob = viewModelScope.launch {
        delay(UNDO_TIMEOUT_MS)
        try {
            habitRepository.deleteHabit(habitId)
            _state.update { it.copy(pendingUndo = null) }
        } catch (e: Exception) {
            _events.emit(TodayEvent.ShowError(e.message ?: "Failed to delete habit"))
            loadTodayHabits()
        }
    }
}

fun undoDelete() {
    undoJob?.cancel()
    _state.update { it.copy(pendingUndo = null) }
    loadTodayHabits()
}

private fun removeHabitFromState(habitId: String) {
    _state.update { state ->
        state.copy(
            habits = state.habits.filter { it.habitId != habitId }.toImmutableList(),
            pendingDaily = state.pendingDaily.filter { it.habitId != habitId }.toImmutableList(),
            resolvedDaily = state.resolvedDaily.filter { it.habitId != habitId }.toImmutableList(),
            pendingWeekly = state.pendingWeekly.filter { it.habitId != habitId }.toImmutableList(),
            resolvedWeekly = state.resolvedWeekly.filter { it.habitId != habitId }.toImmutableList()
        )
    }
}

private companion object {
    const val UNDO_TIMEOUT_MS: Long = 5_000L
}
```

- [ ] **Step 6: Run test to verify it passes**

Run: `./gradlew composeApp:jvmTest --tests "*TodayViewModelSwipeTest*" -x verifyRoborazziJvm`
Expected: PASS

- [ ] **Step 7: Write the failing test for undoDelete**

```kotlin
@Test
fun `undoDelete cancels pending delete and restores habit to state`() = runTest {
    // Given — a habit that was deleted (in pending undo state)
    val inputHabitId = "habit-1"
    // ... set up habit, call deleteHabit

    // When
    viewModel.undoDelete()

    // Then
    val actualState = viewModel.state.value
    assertNull(actualState.pendingUndo)
    assertTrue(actualState.pendingDaily.any { it.habitId == inputHabitId })
    // Verify the habit still exists in the data source (delete was cancelled)
}
```

- [ ] **Step 8: Run test to verify it passes** (undoDelete already implemented)

Run: `./gradlew composeApp:jvmTest --tests "*TodayViewModelSwipeTest*" -x verifyRoborazziJvm`
Expected: PASS

- [ ] **Step 9: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayViewModel.kt
git add composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayViewModelSwipeTest.kt
git commit -m "feat(swipe): add deleteHabit with deferred undo in TodayViewModel"
```

---

## Task 4: ViewModel — Archive with Deferred Undo

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayViewModel.kt`
- Modify: `composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayViewModelSwipeTest.kt`

- [ ] **Step 1: Write the failing test for archiveHabitWithUndo**

```kotlin
@Test
fun `archiveHabitWithUndo removes habit from state and emits HabitArchived event`() = runTest {
    // Given
    val inputHabitId = "habit-1"
    val inputHabitName = "Morning Meditation"
    // ... set up habit

    // When
    viewModel.archiveHabitWithUndo(inputHabitId)

    // Then
    val actualState = viewModel.state.value
    assertTrue(actualState.pendingDaily.none { it.habitId == inputHabitId })
    assertNotNull(actualState.pendingUndo)
    assertTrue(actualState.pendingUndo is UndoOperation.Archive)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew composeApp:jvmTest --tests "*TodayViewModelSwipeTest*archiveHabitWithUndo*" -x verifyRoborazziJvm`
Expected: FAIL — `archiveHabitWithUndo` method does not exist.

- [ ] **Step 3: Implement archiveHabitWithUndo and undoArchive**

Add to `TodayViewModel`:

```kotlin
fun archiveHabitWithUndo(habitId: String) {
    val habit: TodayHabitUiModel = _state.value.habits.find { it.habitId == habitId } ?: return

    removeHabitFromState(habitId)

    _state.update { it.copy(pendingUndo = UndoOperation.Archive(habitId, habit.name)) }

    viewModelScope.launch { _events.emit(TodayEvent.HabitArchived(habit.name)) }

    undoJob?.cancel()
    undoJob = viewModelScope.launch {
        delay(UNDO_TIMEOUT_MS)
        try {
            habitRepository.archiveHabit(habitId)
            _state.update { it.copy(pendingUndo = null) }
        } catch (e: Exception) {
            _events.emit(TodayEvent.ShowError(e.message ?: "Failed to archive habit"))
            loadTodayHabits()
        }
    }
}

fun undoArchive() {
    undoJob?.cancel()
    _state.update { it.copy(pendingUndo = null) }
    loadTodayHabits()
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew composeApp:jvmTest --tests "*TodayViewModelSwipeTest*" -x verifyRoborazziJvm`
Expected: PASS

- [ ] **Step 5: Write edge case tests**

Add tests for:
- Deleting while a previous undo is pending (should cancel the previous operation and start a new one)
- Archiving after a delete undo is pending (should cancel the delete undo)

```kotlin
@Test
fun `second delete cancels first pending delete`() = runTest {
    // Given — two habits, first one already in pending delete
    // When — delete second habit
    // Then — first habit should be actually deleted, second in pending undo
}
```

- [ ] **Step 6: Implement any edge case handling** (if tests fail, adjust the undo logic)

- [ ] **Step 7: Run all tests**

Run: `./gradlew composeApp:jvmTest --tests "*TodayViewModelSwipeTest*" -x verifyRoborazziJvm`
Expected: ALL PASS

- [ ] **Step 8: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayViewModel.kt
git add composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayViewModelSwipeTest.kt
git commit -m "feat(swipe): add archiveHabitWithUndo with deferred undo in TodayViewModel"
```

---

## Task 5: HapticController — KMP expect/actual

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/haptics/HapticController.kt`
- Create: `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/haptics/HapticController.android.kt`
- Create: `composeApp/src/iosMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/haptics/HapticController.ios.kt`
- Create: `composeApp/src/jvmMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/haptics/HapticController.jvm.kt`

- [ ] **Step 1: Create expect class in commonMain**

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.haptics

expect class HapticController {
    fun tick()
    fun click()
    fun heavyClick()
}
```

- [ ] **Step 2: Create Android actual class**

Check the latest available APIs using the android-source-search skill or docs. Implement with layered backward compatibility:

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

actual class HapticController(private val context: Context) {

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    actual fun tick() {
        vibrate(
            primitive = VibrationEffect.Composition.PRIMITIVE_TICK,
            primitiveScale = 0.4f,
            predefined = VibrationEffect.EFFECT_TICK,
            fallbackDuration = 20L,
            fallbackAmplitude = 80
        )
    }

    actual fun click() {
        vibrate(
            primitive = VibrationEffect.Composition.PRIMITIVE_CLICK,
            primitiveScale = 0.6f,
            predefined = VibrationEffect.EFFECT_CLICK,
            fallbackDuration = 30L,
            fallbackAmplitude = 150
        )
    }

    actual fun heavyClick() {
        vibrate(
            primitive = VibrationEffect.Composition.PRIMITIVE_THUD,
            primitiveScale = 1.0f,
            predefined = VibrationEffect.EFFECT_HEAVY_CLICK,
            fallbackDuration = 50L,
            fallbackAmplitude = 255
        )
    }

    private fun vibrate(
        primitive: Int,
        primitiveScale: Float,
        predefined: Int,
        fallbackDuration: Long,
        fallbackAmplitude: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            vibrator.arePrimitivesSupported(primitive)
        ) {
            vibrator.vibrate(
                VibrationEffect.startComposition()
                    .addPrimitive(primitive, primitiveScale)
                    .compose()
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(predefined))
        } else {
            vibrator.vibrate(
                VibrationEffect.createOneShot(fallbackDuration, fallbackAmplitude)
            )
        }
    }
}
```

- [ ] **Step 3: Create iOS actual class**

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.haptics

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

actual class HapticController {

    actual fun tick() {
        val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
        generator.prepare()
        generator.impactOccurred()
    }

    actual fun click() {
        val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
        generator.prepare()
        generator.impactOccurred()
    }

    actual fun heavyClick() {
        val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)
        generator.prepare()
        generator.impactOccurred()
    }
}
```

- [ ] **Step 4: Create JVM actual class (no-op)**

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.haptics

actual class HapticController {
    actual fun tick() { /* no-op on desktop */ }
    actual fun click() { /* no-op on desktop */ }
    actual fun heavyClick() { /* no-op on desktop */ }
}
```

- [ ] **Step 5: Verify all platforms compile**

Run: `./gradlew composeApp:compileKotlinJvm composeApp:compileKotlinIosArm64`
Expected: BUILD SUCCESSFUL for both.

Note: Android compilation will be verified in the next step via `assembleDebug`.

- [ ] **Step 6: Verify Android compilation**

Run: `./gradlew composeApp:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/haptics/HapticController.kt
git add composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/haptics/HapticController.android.kt
git add composeApp/src/iosMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/haptics/HapticController.ios.kt
git add composeApp/src/jvmMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/haptics/HapticController.jvm.kt
git commit -m "feat(swipe): add KMP HapticController with platform implementations"
```

---

## Task 6: SwipeableHabitCard Composable

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/SwipeableHabitCard.kt`

This is the core gesture composable. It uses `AnchoredDraggableState` with 4 anchors.

- [ ] **Step 1: Define the SwipeAction enum and composable signature**

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.presentation.ui.haptics.HapticController
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlin.math.abs
import kotlin.math.roundToInt

enum class SwipeAction {
    REST,
    ARCHIVE,
    EDIT,
    DELETE
}

private const val ARCHIVE_THRESHOLD_FRACTION = 0.3f
private const val EDIT_THRESHOLD_FRACTION = -0.3f
private const val DELETE_THRESHOLD_FRACTION = -0.6f
private val CORNER_RADIUS = 16.dp

@Composable
fun SwipeableHabitCard(
    onArchive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    hapticController: HapticController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Implementation in next steps
}
```

- [ ] **Step 2: Implement the AnchoredDraggableState setup and background**

Inside `SwipeableHabitCard`, implement:
- `AnchoredDraggableState` with anchors computed from measured card width
- Background layer that shows the zone color and icon based on current offset
- Color logic: determine the current zone from the offset, map to `MaterialTheme.colorScheme` tokens:
  - Archive: `surfaceContainerHighest` background, `onSurface` icon tint, `Icons.Outlined.Inventory2`
  - Edit: `secondaryContainer` background, `onSecondaryContainer` icon tint, `Icons.Outlined.Edit`
  - Delete: `errorContainer` background, `onErrorContainer` icon tint, `Icons.Filled.DeleteForever`
- Foreground content offset by `state.offset`

```kotlin
@Composable
fun SwipeableHabitCard(
    onArchive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    hapticController: HapticController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    var cardWidth: Float by remember { mutableStateOf(0f) }

    val anchoredDraggableState: AnchoredDraggableState<SwipeAction> = remember {
        AnchoredDraggableState(
            initialValue = SwipeAction.REST,
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 125.dp.toPx() } },
            confirmValueChange = { true }
        )
    }

    // Update anchors when card width changes
    LaunchedEffect(cardWidth) {
        if (cardWidth > 0f) {
            anchoredDraggableState.updateAnchors(
                DraggableAnchors {
                    SwipeAction.REST at 0f
                    SwipeAction.ARCHIVE at cardWidth * ARCHIVE_THRESHOLD_FRACTION
                    SwipeAction.EDIT at cardWidth * EDIT_THRESHOLD_FRACTION
                    SwipeAction.DELETE at cardWidth * DELETE_THRESHOLD_FRACTION
                }
            )
        }
    }

    // Determine current zone from offset
    val currentOffset: Float = anchoredDraggableState.offset.takeIf { !it.isNaN() } ?: 0f
    val currentZone: SwipeAction = when {
        currentOffset > 0f && cardWidth > 0f &&
            currentOffset >= cardWidth * ARCHIVE_THRESHOLD_FRACTION * 0.5f -> SwipeAction.ARCHIVE
        currentOffset < 0f && cardWidth > 0f &&
            abs(currentOffset) >= cardWidth * abs(DELETE_THRESHOLD_FRACTION) * 0.75f -> SwipeAction.DELETE
        currentOffset < 0f && cardWidth > 0f &&
            abs(currentOffset) >= cardWidth * abs(EDIT_THRESHOLD_FRACTION) * 0.5f -> SwipeAction.EDIT
        else -> SwipeAction.REST
    }

    // Zone colors
    // Snap color — no animation per spec
    val backgroundColor = when (currentZone) {
        SwipeAction.ARCHIVE -> MaterialTheme.colorScheme.surfaceContainerHighest
        SwipeAction.EDIT -> MaterialTheme.colorScheme.secondaryContainer
        SwipeAction.DELETE -> MaterialTheme.colorScheme.errorContainer
        SwipeAction.REST -> MaterialTheme.colorScheme.surface
    }

    val iconTint = when (currentZone) {
        SwipeAction.ARCHIVE -> MaterialTheme.colorScheme.onSurface
        SwipeAction.EDIT -> MaterialTheme.colorScheme.onSecondaryContainer
        SwipeAction.DELETE -> MaterialTheme.colorScheme.onErrorContainer
        SwipeAction.REST -> MaterialTheme.colorScheme.onSurface
    }

    val icon: ImageVector? = when (currentZone) {
        SwipeAction.ARCHIVE -> Icons.Outlined.Inventory2
        SwipeAction.EDIT -> Icons.Outlined.Edit
        SwipeAction.DELETE -> Icons.Filled.DeleteForever
        SwipeAction.REST -> null
    }

    // Haptic feedback on zone change
    LaunchedEffect(anchoredDraggableState) {
        snapshotFlow { currentZone }
            .distinctUntilChanged()
            .filter { it != SwipeAction.REST }
            .collect { zone: SwipeAction ->
                when (zone) {
                    SwipeAction.ARCHIVE -> hapticController.tick()
                    SwipeAction.EDIT -> hapticController.click()
                    SwipeAction.DELETE -> hapticController.heavyClick()
                    SwipeAction.REST -> { /* no haptic */ }
                }
            }
    }

    // Handle settle at non-rest anchors
    LaunchedEffect(anchoredDraggableState) {
        snapshotFlow { anchoredDraggableState.currentValue }
            .distinctUntilChanged()
            .filter { it != SwipeAction.REST }
            .collect { action: SwipeAction ->
                when (action) {
                    SwipeAction.ARCHIVE -> onArchive()
                    SwipeAction.EDIT -> {
                        anchoredDraggableState.animateTo(SwipeAction.REST)
                        onEdit()
                    }
                    SwipeAction.DELETE -> onDelete()
                    SwipeAction.REST -> { /* handled by filter */ }
                }
            }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { size -> cardWidth = size.width.toFloat() }
            .clip(RoundedCornerShape(CORNER_RADIUS))
            .background(backgroundColor)
    ) {
        // Background icon
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .align(
                        if (currentOffset > 0f) Alignment.CenterStart else Alignment.CenterEnd
                    )
                    .padding(horizontal = 24.dp)
            )
        }

        // Foreground card content
        Box(
            modifier = Modifier
                .offset { IntOffset(x = currentOffset.roundToInt(), y = 0) }
                .anchoredDraggable(
                    state = anchoredDraggableState,
                    orientation = Orientation.Horizontal
                )
        ) {
            content()
        }
    }
}
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/SwipeableHabitCard.kt
git commit -m "feat(swipe): add SwipeableHabitCard composable with AnchoredDraggable zones"
```

---

## Task 7: Wire SwipeableHabitCard into TodayScreen

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayScreen.kt`

- [ ] **Step 1: Add swipe callbacks to the outer TodayScreen composable**

In the outer `TodayScreen` composable (line 87), update the event handling `LaunchedEffect` to handle the new undo snackbar events:

```kotlin
is TodayEvent.HabitArchived -> {
    val result = snackbarHostState.showSnackbar(
        message = getString(Res.string.swipe_habit_archived),
        actionLabel = getString(Res.string.swipe_undo),
        duration = SnackbarDuration.Short
    )
    if (result == SnackbarResult.ActionPerformed) {
        viewModel.undoArchive()
    }
}

is TodayEvent.HabitDeleted -> {
    val result = snackbarHostState.showSnackbar(
        message = getString(Res.string.swipe_habit_deleted),
        actionLabel = getString(Res.string.swipe_undo),
        duration = SnackbarDuration.Short
    )
    if (result == SnackbarResult.ActionPerformed) {
        viewModel.undoDelete()
    }
}

TodayEvent.UndoCompleted -> {
    snackbarHostState.currentSnackbarData?.dismiss()
}
```

Also pass the new callbacks to the inner `TodayScreen`:

```kotlin
TodayScreen(
    state = state,
    // ... existing callbacks ...
    onArchive = viewModel::archiveHabitWithUndo,
    onEdit = { habitId -> onEditHabit(habitId) },
    onDelete = viewModel::deleteHabit
)
```

- [ ] **Step 2: Add swipe callbacks to the inner TodayScreen composable signature**

Update the internal `TodayScreen` composable signature (line 139) to accept the new callbacks:

```kotlin
@Composable
internal fun TodayScreen(
    state: TodayState,
    onComplete: (String) -> Unit,
    onSkip: (String) -> Unit,
    onUndo: (String) -> Unit,
    onUndoLastIncrement: (String) -> Unit,
    onIncrementProgress: (String) -> Unit,
    onCustomProgress: (String) -> Unit,
    onArchive: (String) -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDismissTimezoneWarning: () -> Unit,
    onAddFirstHabit: () -> Unit,
    modifier: Modifier = Modifier
)
```

- [ ] **Step 3: Create the HapticController instance and wrap HabitCard calls**

Inside the inner `TodayScreen`, create the haptic controller. Then wrap each `HabitCard(...)` call in the LazyColumn with `SwipeableHabitCard`. There are 4 call sites (pendingDaily, resolvedDaily, pendingWeekly, resolvedWeekly).

For each `items` block, the pattern is:

```kotlin
items(
    items = state.pendingDaily,
    key = { it.instanceId }
) { habit ->
    SwipeableHabitCard(
        onArchive = { onArchive(habit.habitId) },
        onEdit = { onEdit(habit.habitId) },
        onDelete = { onDelete(habit.habitId) },
        hapticController = hapticController,
        modifier = Modifier.animateItem()
    ) {
        HabitCard(
            habit = habit,
            isExpanded = habit.instanceId in expandedCardIds,
            // ... existing callbacks unchanged ...
        )
    }
}
```

Remove `modifier = Modifier.animateItem()` from the `HabitCard` calls — it moves to `SwipeableHabitCard`.

Apply this pattern to all 4 `items` blocks (lines 240, 286, 317, 362).

- [ ] **Step 4: Verify it compiles**

Run: `./gradlew composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Run existing tests to verify no regressions**

Run: `./gradlew composeApp:jvmTest -x verifyRoborazziJvm`
Expected: ALL PASS

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayScreen.kt
git commit -m "feat(swipe): wire SwipeableHabitCard into TodayScreen with undo snackbars"
```

---

## Task 8: Screenshot Tests for Swipe Zone Backgrounds

**Files:**
- Create: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/SwipeableHabitCardScreenshotTest.kt`

Since `AnchoredDraggableState` offset is gesture-driven and hard to control in screenshot tests, extract the background rendering into a standalone `@Composable` that takes a `SwipeAction` zone directly. This makes it trivially testable.

- [ ] **Step 1: Extract SwipeBackground composable**

In `SwipeableHabitCard.kt`, extract a `SwipeBackground` composable that takes the zone as a parameter:

```kotlin
@Composable
internal fun SwipeBackground(
    zone: SwipeAction,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (zone) {
        SwipeAction.ARCHIVE -> MaterialTheme.colorScheme.surfaceContainerHighest
        SwipeAction.EDIT -> MaterialTheme.colorScheme.secondaryContainer
        SwipeAction.DELETE -> MaterialTheme.colorScheme.errorContainer
        SwipeAction.REST -> MaterialTheme.colorScheme.surface
    }

    val iconTint = when (zone) {
        SwipeAction.ARCHIVE -> MaterialTheme.colorScheme.onSurface
        SwipeAction.EDIT -> MaterialTheme.colorScheme.onSecondaryContainer
        SwipeAction.DELETE -> MaterialTheme.colorScheme.onErrorContainer
        SwipeAction.REST -> MaterialTheme.colorScheme.onSurface
    }

    val icon: ImageVector? = when (zone) {
        SwipeAction.ARCHIVE -> Icons.Outlined.Inventory2
        SwipeAction.EDIT -> Icons.Outlined.Edit
        SwipeAction.DELETE -> Icons.Filled.DeleteForever
        SwipeAction.REST -> null
    }

    val alignment: Alignment = when (zone) {
        SwipeAction.ARCHIVE -> Alignment.CenterStart
        else -> Alignment.CenterEnd
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CORNER_RADIUS))
            .background(backgroundColor)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .align(alignment)
                    .padding(horizontal = 24.dp)
            )
        }
    }
}
```

Then have `SwipeableHabitCard` call `SwipeBackground(zone = currentZone)` internally instead of duplicating the color/icon logic.

- [ ] **Step 2: Create screenshot test file**

Follow the existing pattern in `TodayScreenScreenshotTest.kt`. Create `SwipeableHabitCardScreenshotTest.kt`:

```kotlin
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SwipeableHabitCardScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun swipeBackground_archive_lightTheme() {
        composeRule.setContent {
            HabitLockTheme(darkTheme = false) {
                SwipeBackground(
                    zone = SwipeAction.ARCHIVE,
                    modifier = Modifier.height(72.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun swipeBackground_archive_darkTheme() {
        composeRule.setContent {
            HabitLockTheme(darkTheme = true) {
                SwipeBackground(
                    zone = SwipeAction.ARCHIVE,
                    modifier = Modifier.height(72.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // Same pattern for EDIT (light/dark) and DELETE (light/dark) — 6 tests total
}
```

- [ ] **Step 3: Generate reference images**

Run: `./gradlew composeApp:recordRoborazziJvm`
Expected: New reference images generated for each swipe zone state.

- [ ] **Step 4: Run verification**

Run: `./gradlew composeApp:verifyRoborazziJvm`
Expected: ALL PASS (images match references).

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/SwipeableHabitCard.kt
git add composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/SwipeableHabitCardScreenshotTest.kt
git add composeApp/src/androidUnitTest/snapshots/
git commit -m "test(swipe): add screenshot tests for swipe zone backgrounds"
```

---

## Task 9: Final Verification

- [ ] **Step 1: Run the full test suite**

Run: `./gradlew composeApp:jvmTest`
Expected: ALL PASS (including screenshot verification).

- [ ] **Step 2: Run a debug build**

Run: `./gradlew composeApp:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Verify iOS compiles**

Run: `./gradlew composeApp:compileKotlinIosArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Final commit (if any remaining changes)**

```bash
git status
# If any unstaged changes, review and commit
```
