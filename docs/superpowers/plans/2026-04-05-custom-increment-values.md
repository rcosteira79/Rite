# Custom Increment Values Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users configure the quick-increment amount for quantitative habits via the create/edit habit form.

**Architecture:** Presentation-only change. Add `defaultIncrement` to `HabitFormState`, wire it through `HabitFormViewModel` to the existing `CreateHabitParams.defaultIncrement` field, and add an `UnderlineTextField` in the form UI inside the existing quantitative `AnimatedVisibility` block.

**Tech Stack:** Kotlin, Jetpack Compose, Compose Resources (XML strings), kotlin-inject, kotlinx-datetime

---

### Task 1: Add `DefaultIncrementChanged` to `HabitFormUiAction`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormUiAction.kt`

- [ ] **Step 1: Add the new action variant**

Add after line 16 (`data class UnitChanged`):

```kotlin
data class DefaultIncrementChanged(val value: String) : HabitFormUiAction
```

- [ ] **Step 2: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormUiAction.kt
git commit -m "feat(habit-form): add DefaultIncrementChanged UI action"
```

---

### Task 2: Add `defaultIncrement` to `HabitFormState`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormState.kt`

- [ ] **Step 1: Add the field to the data class**

Add after the `unit` field (line 18):

```kotlin
val defaultIncrement: String = "1",
```

- [ ] **Step 2: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormState.kt
git commit -m "feat(habit-form): add defaultIncrement field to HabitFormState"
```

---

### Task 3: Wire `defaultIncrement` through `HabitFormViewModel`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormViewModel.kt`

- [ ] **Step 1: Add `updateDefaultIncrement` method**

Add after the `updateUnit` method (after line 146):

```kotlin
fun updateDefaultIncrement(value: String) {
    _state.update { it.copy(defaultIncrement = value) }
}
```

- [ ] **Step 2: Populate `defaultIncrement` when loading existing habit for editing**

In the `loadHabit` method, inside the `_state.update` block (around line 87-107), add `defaultIncrement` to the `copy` call, after `unit`:

```kotlin
defaultIncrement = habit.defaultIncrement.toString(),
```

- [ ] **Step 3: Pass `defaultIncrement` in `createNewHabit`**

In the `createNewHabit` method (around line 262-278), add `defaultIncrement` to the `CreateHabitParams` constructor call, after the `unit` parameter:

```kotlin
defaultIncrement = state.defaultIncrement.toIntOrNull() ?: 1,
```

- [ ] **Step 4: Pass `defaultIncrement` in `updateExistingHabit`**

In the `updateExistingHabit` method (around line 297-308), add `defaultIncrement` to the `existingHabit.copy()` call, after `unit`:

```kotlin
defaultIncrement = state.defaultIncrement.toIntOrNull() ?: 1,
```

- [ ] **Step 5: Handle the action in the screen's `onAction` dispatch**

In `HabitFormScreen.kt`, in the `onAction` lambda (around line 176-229), add a branch after the `UnitChanged` handler (after line 189):

```kotlin
is HabitFormUiAction.DefaultIncrementChanged ->
    viewModel.updateDefaultIncrement(action.value)
```

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormViewModel.kt
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormScreen.kt
git commit -m "feat(habit-form): wire defaultIncrement through ViewModel and screen dispatch"
```

---

### Task 4: Add string resources

**Files:**
- Modify: `composeApp/src/commonMain/composeResources/values/strings_habit_form.xml`

- [ ] **Step 1: Add label and placeholder strings**

Add before the closing `</resources>` tag:

```xml
<string name="habit_form_increment_label">INCREMENT BY</string>
<string name="habit_form_placeholder_increment">1</string>
<string name="habit_form_increment_hint">Quick-increment amount on the Today screen</string>
```

- [ ] **Step 2: Commit**

```bash
git add composeApp/src/commonMain/composeResources/values/strings_habit_form.xml
git commit -m "feat(habit-form): add string resources for increment field"
```

---

### Task 5: Add the `UnderlineTextField` to the form UI

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormScreen.kt`

- [ ] **Step 1: Add the import for the new string resources**

Add these imports alongside the existing `Res.string` imports (around line 112-126):

```kotlin
import rite.composeapp.generated.resources.habit_form_increment_hint
import rite.composeapp.generated.resources.habit_form_increment_label
import rite.composeapp.generated.resources.habit_form_placeholder_increment
```

- [ ] **Step 2: Add the increment field inside the quantitative AnimatedVisibility block**

The existing `AnimatedVisibility` block for quantitative fields is at lines 434-448. Replace the `Column` content inside it to add the increment field after the unit field:

Replace lines 439-447:
```kotlin
Column {
    Spacer(modifier = Modifier.height(12.dp))
    UnderlineTextField(
        value = state.unit,
        onValueChange = { onAction(HabitFormUiAction.UnitChanged(it)) },
        label = stringResource(Res.string.habit_form_unit_label),
        placeholder = stringResource(Res.string.habit_form_placeholder_unit)
    )
}
```

With:
```kotlin
Column {
    Spacer(modifier = Modifier.height(12.dp))
    UnderlineTextField(
        value = state.unit,
        onValueChange = { onAction(HabitFormUiAction.UnitChanged(it)) },
        label = stringResource(Res.string.habit_form_unit_label),
        placeholder = stringResource(Res.string.habit_form_placeholder_unit)
    )

    Spacer(modifier = Modifier.height(12.dp))

    SectionLabel(Res.string.habit_form_increment_label)
    Spacer(modifier = Modifier.height(8.dp))
    UnderlineTextField(
        value = state.defaultIncrement,
        onValueChange = { onAction(HabitFormUiAction.DefaultIncrementChanged(it)) },
        placeholder = stringResource(Res.string.habit_form_placeholder_increment),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    Text(
        text = stringResource(Res.string.habit_form_increment_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp)
    )
}
```

- [ ] **Step 3: Add the `KeyboardType` import**

Add with the other imports:

```kotlin
import androidx.compose.ui.text.input.KeyboardType
```

- [ ] **Step 4: Verify it compiles**

Run: `cd /Users/ricardocosteira/Documents/Rite/.claude/worktrees/feature+custom-increment-values && ./gradlew composeApp:compileKotlinDesktop 2>&1 | tail -5`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormScreen.kt
git commit -m "feat(habit-form): add increment-by field to quantitative habit form"
```

---

### Task 6: Write and run unit tests

**Files:**
- Modify: `composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormStateTest.kt`

- [ ] **Step 1: Write test for default increment value in state**

Add to `HabitFormStateTest`:

```kotlin
@Test
fun `given default state when checking defaultIncrement then it is 1`() {
    // Given
    val inputState = HabitFormState()

    // When
    val actualDefaultIncrement = inputState.defaultIncrement

    // Then
    assertTrue(actualDefaultIncrement == "1")
}
```

- [ ] **Step 2: Write test for custom increment value preserved in state**

```kotlin
@Test
fun `given state with custom defaultIncrement when checking value then it is preserved`() {
    // Given
    val inputState = HabitFormState(
        name = "Drink Water",
        type = HabitType.QUANTITATIVE,
        targetValue = "2000",
        unit = "mL",
        defaultIncrement = "500"
    )

    // When
    val actualDefaultIncrement = inputState.defaultIncrement

    // Then
    assertTrue(actualDefaultIncrement == "500")
}
```

- [ ] **Step 3: Run tests to verify they pass**

Run: `cd /Users/ricardocosteira/Documents/Rite/.claude/worktrees/feature+custom-increment-values && ./gradlew composeApp:desktopTest --tests "com.ricardocosteira.rite.presentation.ui.habit.HabitFormStateTest" 2>&1 | tail -10`

Expected: `BUILD SUCCESSFUL` with all tests passing.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormStateTest.kt
git commit -m "test(habit-form): add unit tests for defaultIncrement in HabitFormState"
```

---

### Task 7: Re-record screenshot goldens

**Files:**
- Check: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormScreenshotTest.kt`

- [ ] **Step 1: Check if the screenshot test covers quantitative form state**

Read `HabitFormScreenshotTest.kt` and identify if any golden tests render the form with `type = HabitType.QUANTITATIVE`. If so, the new field will change the screenshot.

- [ ] **Step 2: Re-record goldens if needed**

Run: `cd /Users/ricardocosteira/Documents/Rite/.claude/worktrees/feature+custom-increment-values && ./gradlew composeApp:updateDebugScreenshotTest 2>&1 | tail -10`

Expected: `BUILD SUCCESSFUL` — new golden images written.

- [ ] **Step 3: Verify screenshot tests pass with new goldens**

Run: `cd /Users/ricardocosteira/Documents/Rite/.claude/worktrees/feature+custom-increment-values && ./gradlew composeApp:validateDebugScreenshotTest 2>&1 | tail -10`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit updated goldens**

```bash
git add composeApp/src/androidUnitTest/
git commit -m "test(habit-form): re-record goldens after adding increment field"
```
