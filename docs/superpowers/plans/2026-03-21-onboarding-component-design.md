# Onboarding Component Design Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Apply the Forest Discipline visual design to all three onboarding screens — button styles, architectural background, strictness card redesign, and first-habit input/card redesign.

**Architecture:** Pure UI changes across 5 files; no state, navigation, or ViewModel changes. The data model (`OnboardingStrictnessPreset`) is updated first since the UI depends on it, then each composable file is changed independently.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform (commonMain), Material Icons Extended, Material 3

---

## File Map

| File | Change |
|------|--------|
| `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingStrictnessPreset.kt` | Add `PresetRule` data class; add `collapsedSummary` field; replace `List<String>` rules with `List<PresetRule>`; update enum values |
| `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingCta.kt` | Add `ButtonDefaults.buttonColors` + `RoundedCornerShape(12.dp)` to every `Button` |
| `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/PhilosophyStep.kt` | Replace lock watermark composable with architectural background ring |
| `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/StrictnessStep.kt` | Full `PresetCard` redesign — icons, ghost border, selected `primaryContainer` card, key-value rule rows, `animateColorAsState` |
| `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/FirstHabitStep.kt` | `OutlinedTextField` → filled `TextField`; type cards add icon + 24dp radius + updated colors |
| `composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingStrictnessPresetTest.kt` | Unit tests for `PresetRule` data and `collapsedSummary` values |

---

## Task 1: Update `OnboardingStrictnessPreset` data model

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingStrictnessPreset.kt`
- Create: `composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingStrictnessPresetTest.kt`

- [ ] **Step 1: Write failing tests**

Create the test file at the path above:

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.onboarding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OnboardingStrictnessPresetTest {

    @Test
    fun `FLEXIBLE has correct collapsed summary`() {
        assertEquals(
            expected = "Undo: Unlimited · Snoozes: Unlimited",
            actual = OnboardingStrictnessPreset.FLEXIBLE.collapsedSummary
        )
    }

    @Test
    fun `BALANCED has correct collapsed summary`() {
        assertEquals(
            expected = "Undo: Within 5 min · Snoozes: 1/day",
            actual = OnboardingStrictnessPreset.BALANCED.collapsedSummary
        )
    }

    @Test
    fun `LOCKED has correct collapsed summary`() {
        assertEquals(
            expected = "No undo · Skips capped",
            actual = OnboardingStrictnessPreset.LOCKED.collapsedSummary
        )
    }

    @Test
    fun `BALANCED rules contain key-value pairs for Undo, Snoozes, Skips`() {
        val actualKeys = OnboardingStrictnessPreset.BALANCED.rules.map { it.key }
        assertEquals(expected = listOf("Undo", "Snoozes", "Skips"), actual = actualKeys)
    }

    @Test
    fun `BALANCED rules have correct values`() {
        val rules = OnboardingStrictnessPreset.BALANCED.rules
        assertEquals(expected = "Within 5 min", actual = rules[0].value)
        assertEquals(expected = "1 / day",       actual = rules[1].value)
        assertEquals(expected = "2 / month",     actual = rules[2].value)
    }

    @Test
    fun `FLEXIBLE rules all have value Unlimited`() {
        val rules = OnboardingStrictnessPreset.FLEXIBLE.rules
        assertTrue(rules.isNotEmpty())
        assertTrue(rules.all { it.value == "Unlimited" })
    }

    @Test
    fun `BALANCED is recommended, others are not`() {
        assertTrue(OnboardingStrictnessPreset.BALANCED.isRecommended)
        assertTrue(!OnboardingStrictnessPreset.FLEXIBLE.isRecommended)
        assertTrue(!OnboardingStrictnessPreset.LOCKED.isRecommended)
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
./gradlew :composeApp:jvmTest --tests "*.OnboardingStrictnessPresetTest" 2>&1 | tail -20
```

Expected: compilation error — `collapsedSummary`, `PresetRule`, and `.rules` typed fields don't exist yet.

- [ ] **Step 3: Update `OnboardingStrictnessPreset.kt`**

Replace the entire file content:

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.onboarding

data class PresetRule(val key: String, val value: String)

enum class OnboardingStrictnessPreset(
    val label: String,
    val description: String,
    val collapsedSummary: String,
    val rules: List<PresetRule>,
    val isRecommended: Boolean = false
) {
    FLEXIBLE(
        label = "Flexible",
        description = "Gentle support, maximum forgiveness.",
        collapsedSummary = "Undo: Unlimited · Snoozes: Unlimited",
        rules = listOf(
            PresetRule("Undo", "Unlimited"),
            PresetRule("Snoozes", "Unlimited"),
            PresetRule("Skips", "Unlimited")
        )
    ),
    BALANCED(
        label = "Balanced",
        description = "The middle path. Enough grace to fail, enough structure to win.",
        collapsedSummary = "Undo: Within 5 min · Snoozes: 1/day",
        rules = listOf(
            PresetRule("Undo", "Within 5 min"),
            PresetRule("Snoozes", "1 / day"),
            PresetRule("Skips", "2 / month")
        ),
        isRecommended = true
    ),
    LOCKED(
        label = "Locked",
        description = "No excuses. Full accountability.",
        collapsedSummary = "No undo · Skips capped",
        rules = listOf(
            PresetRule("Undo", "None"),
            PresetRule("Snoozes", "Capped"),
            PresetRule("Skips", "Capped")
        )
    )
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
./gradlew :composeApp:jvmTest --tests "*.OnboardingStrictnessPresetTest" 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`, all 7 tests pass.

- [ ] **Step 5: Verify `StrictnessStep.kt` still compiles** (it references `preset.rules` — the type changed from `List<String>` to `List<PresetRule>`, so it will fail to compile until Task 4 is done; that is expected and fine at this stage)

```bash
./gradlew :composeApp:compileDebugKotlinAndroid 2>&1 | grep -E "error:|warning:" | head -20
```

Expected: errors in `StrictnessStep.kt` only (references to old `rules` type). No errors in `OnboardingStrictnessPreset.kt` itself.

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingStrictnessPreset.kt \
        composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingStrictnessPresetTest.kt
git commit -m "feat(onboarding): add PresetRule data class and collapsedSummary to strictness presets"
```

---

## Task 2: Update CTA buttons

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingCta.kt` (lines 74, 91, 114)

No new test needed — this is a pure visual change with no logic. Verification is compilation + visual inspection.

- [ ] **Step 1: Add `ButtonDefaults` import**

Open `OnboardingCta.kt`. Add this import (keep existing imports):

```kotlin
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
```

- [ ] **Step 2: Update `PhilosophyStepCta` button (line ~74)**

Change from:
```kotlin
Button(onClick = onAdvance, modifier = Modifier.fillMaxWidth()) {
```
To:
```kotlin
Button(
    onClick = onAdvance,
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
) {
```

- [ ] **Step 3: Update `StrictnessStepCta` button (line ~91)**

Change from:
```kotlin
Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
```
To:
```kotlin
Button(
    onClick = onContinue,
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
) {
```

- [ ] **Step 4: Update `FirstHabitStepCta` button (line ~114)**

Change from:
```kotlin
Button(
    onClick = onCreateHabit,
    enabled = isEnabled,
    modifier = Modifier.fillMaxWidth()
) {
```
To:
```kotlin
Button(
    onClick = onCreateHabit,
    enabled = isEnabled,
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
) {
```

- [ ] **Step 5: Verify compilation**

```bash
./gradlew :composeApp:compileDebugKotlinAndroid 2>&1 | grep -E "error:" | grep "OnboardingCta" | head -10
```

Expected: no errors in `OnboardingCta.kt`.

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingCta.kt
git commit -m "feat(onboarding): update CTA buttons to 12dp rounded rect with primaryContainer color"
```

---

## Task 3: Replace Philosophy screen lock watermark with architectural ring

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/PhilosophyStep.kt`

No new test needed — pure visual change. Verification is compilation + visual inspection.

- [ ] **Step 1: Remove lock watermark — delete `lockAlpha` state and its animation**

In `PhilosophyStep.kt`:

a) Remove `val lockAlpha = remember { Animatable(0f) }` (line ~41).

b) In the `reduceMotion` branch of `LaunchedEffect`, remove:
```kotlin
lockAlpha.snapTo(0.45f)
```

c) In the animated branch, remove the last `delay` + `lockAlpha.animateTo(...)` call (currently the final 2 lines of the `LaunchedEffect` block):
```kotlin
// Lock watermark: fade in — 200ms, 300ms delay
delay(160) // 140 + 160 = 300ms total
lockAlpha.animateTo(0.45f, tween(200))
```

d) Remove the entire lock watermark `Box` composable (lines ~117–135):
```kotlin
// Lock watermark — decorative, bottom-right
Box(
    modifier = Modifier
        .align(Alignment.BottomEnd)
        ...
) {
    Icon(...)
}
```

e) Remove the now-unused imports: `import androidx.compose.material.icons.filled.Lock` and `import androidx.compose.material3.Icon`. The architectural ring `Box` uses no `Icon` composable, so removing these is safe.

- [ ] **Step 2: Add architectural ring**

a) Add these imports (keep existing ones):
```kotlin
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
```
(Note: `graphicsLayer` and `dp` may already be imported — check before adding.)

b) Inside the outer `Box`, **before** the `Column`, add the ring:

```kotlin
// Architectural background ring — decorative, non-interactive
Box(
    modifier = Modifier
        .align(Alignment.TopEnd)
        .offset(x = 120.dp, y = (-80).dp)
        .size(420.dp)
        .graphicsLayer { rotationZ = 12f }
        .border(
            width = 40.dp,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
            shape = CircleShape
        )
)
```

The full `Box` structure after the change looks like:

```kotlin
Box(
    modifier = modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp)
) {
    // Background ring (non-interactive, drawn before foreground content)
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 120.dp, y = (-80).dp)
            .size(420.dp)
            .graphicsLayer { rotationZ = 12f }
            .border(
                width = 40.dp,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
                shape = CircleShape
            )
    )

    // Foreground content (unchanged)
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(...)   // headline
        ...
    }
}
```

- [ ] **Step 3: Verify compilation**

```bash
./gradlew :composeApp:compileDebugKotlinAndroid 2>&1 | grep -E "error:" | grep "PhilosophyStep" | head -10
```

Expected: no errors.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/PhilosophyStep.kt
git commit -m "feat(onboarding): replace lock watermark with architectural background ring on Philosophy screen"
```

---

## Task 4: Redesign Strictness screen preset cards

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/StrictnessStep.kt`

This task fixes the compile error from Task 1 and fully redesigns the card UI.

- [ ] **Step 1: Replace imports**

Remove these imports:
```kotlin
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
```

Add these imports:
```kotlin
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
```

- [ ] **Step 2: Remove old constants and extension function**

Delete these from the top of the file (after the imports):
```kotlin
private val ColorDotFlexible = Color(0xFF4CAF50)
private val ColorDotBalanced = Color(0xFFFF9800)

@Composable
private fun OnboardingStrictnessPreset.dotColor(): Color = when (this) { ... }
```

Also remove `import androidx.compose.ui.graphics.Color` if it is no longer used after removing those constants (check — it may still be needed for `Color.Transparent` if used elsewhere; if not, remove it).

- [ ] **Step 3: Add icon mapping function**

Add this private function at the bottom of the file (before `PresetCard`):

```kotlin
private fun OnboardingStrictnessPreset.icon(): ImageVector = when (this) {
    OnboardingStrictnessPreset.FLEXIBLE -> Icons.Outlined.EditNote
    OnboardingStrictnessPreset.BALANCED -> Icons.Filled.Balance
    OnboardingStrictnessPreset.LOCKED   -> Icons.Filled.Lock
}
```

- [ ] **Step 4: Rewrite `PresetCard`**

Replace the entire `PresetCard` composable with the following. The new design does **not** use `AnimatedVisibility` — the selected/unselected states are fully separate layouts controlled by a plain `if (isSelected)` branch. The `semantics` block carries over unchanged. Remove `reduceMotion` from the parameter list; it is no longer used.

```kotlin
@Composable
private fun PresetCard(
    preset: OnboardingStrictnessPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(200),
        label = "presetCardBackground"
    )

    val cornerRadius = if (isSelected) 24.dp else 16.dp

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isSelected) Modifier.shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(cornerRadius)
                    ) else Modifier
                )
                .clip(RoundedCornerShape(cornerRadius))
                .background(backgroundColor)
                .then(
                    if (!isSelected) Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(cornerRadius)
                    ) else Modifier
                )
                .clickable { onClick() }
                .semantics {
                    role = Role.RadioButton
                    selected = isSelected
                    stateDescription = if (isSelected) "Selected" else "Not selected"
                }
                .padding(16.dp)
        ) {
            if (isSelected) {
                // === SELECTED STATE ===
                Icon(
                    imageVector = preset.icon(),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = preset.label,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = preset.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                preset.rules.forEachIndexed { index, rule ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.07f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = rule.key,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                        )
                        Text(
                            text = rule.value,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            } else {
                // === COLLAPSED (UNSELECTED) STATE ===
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val iconTint = if (preset == OnboardingStrictnessPreset.LOCKED) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Icon(
                        imageVector = preset.icon(),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = iconTint
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = preset.collapsedSummary,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }

        // "RECOMMENDED" pill badge — positioned above the selected card
        if (isSelected && preset.isRecommended) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-12).dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(999.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "RECOMMENDED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.12.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
```

- [ ] **Step 5: Update the `PresetCard` call site in `StrictnessStep`**

In the `StrictnessStep` composable, the `PresetCard` call currently passes `reduceMotion = reduceMotion`. Remove that argument since the parameter no longer exists:
```kotlin
PresetCard(
    preset = preset,
    isSelected = preset == selectedPreset,
    onClick = { onPresetSelected(preset) }
)
```

- [ ] **Step 6: Add missing imports for `Arrangement`, `FontWeight`, etc.**

Ensure the import list includes:
```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.font.FontWeight
```
(Both are likely already present — check before adding.)

- [ ] **Step 7: Verify compilation**

```bash
./gradlew :composeApp:compileDebugKotlinAndroid 2>&1 | grep -E "error:" | head -20
```

Expected: no errors in any file.

- [ ] **Step 8: Run all unit tests to confirm nothing regressed**

```bash
./gradlew :composeApp:jvmTest 2>&1 | tail -15
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 9: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/StrictnessStep.kt
git commit -m "feat(onboarding): redesign strictness preset cards with icons, ghost border, and key-value expanded view"
```

---

## Task 5: Update First Habit screen — filled TextField and type card icons

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/FirstHabitStep.kt`

- [ ] **Step 1: Replace `OutlinedTextField` import with `TextField` and `TextFieldDefaults`**

Remove:
```kotlin
import androidx.compose.material3.OutlinedTextField
```

Add:
```kotlin
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Icon
```

- [ ] **Step 2: Replace habit name `OutlinedTextField` with filled `TextField`**

Find (around line 121):
```kotlin
OutlinedTextField(
    value = habitName,
    onValueChange = onHabitNameChange,
    label = { Text(stringResource(Res.string.first_habit_label_name)) },
    placeholder = { Text(stringResource(Res.string.common_placeholder_habit_name)) },
    singleLine = true,
    modifier = Modifier.fillMaxWidth()
)
```

Replace with:
```kotlin
TextField(
    value = habitName,
    onValueChange = onHabitNameChange,
    label = { Text(stringResource(Res.string.first_habit_label_name)) },
    placeholder = { Text(stringResource(Res.string.common_placeholder_habit_name)) },
    singleLine = true,
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
    colors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent
    )
)
```

- [ ] **Step 3: Replace quantitative `OutlinedTextField` fields inside `expandedContent`**

Inside `HabitTypeCard`'s `expandedContent` lambda (around lines 152–171), replace both `OutlinedTextField` calls:

```kotlin
// Target value field
TextField(
    value = targetValue,
    onValueChange = onTargetValueChange,
    label = { Text(stringResource(Res.string.first_habit_label_target_value)) },
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
    colors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent
    )
)
Spacer(modifier = Modifier.height(8.dp))
// Unit field
TextField(
    value = unit,
    onValueChange = onUnitChange,
    label = { Text(stringResource(Res.string.first_habit_label_unit)) },
    placeholder = { Text(stringResource(Res.string.first_habit_placeholder_unit)) },
    singleLine = true,
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
    colors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent
    )
)
```

- [ ] **Step 4: Update `HabitTypeCard` call sites to pass icons**

Binary card call site (around line 133):
```kotlin
HabitTypeCard(
    icon = Icons.Outlined.CheckCircle,
    label = stringResource(Res.string.first_habit_type_binary),
    description = stringResource(Res.string.first_habit_type_binary_description),
    isSelected = habitType == HabitType.BINARY,
    onClick = {
        onHabitTypeChange(HabitType.BINARY)
        onTargetValueChange("")
        onUnitChange("")
    },
    expandedContent = null
)
```

Quantitative card call site (around line 147):
```kotlin
HabitTypeCard(
    icon = Icons.Outlined.ShowChart,
    label = stringResource(Res.string.common_quantitative),
    description = stringResource(Res.string.first_habit_type_quantitative_description),
    isSelected = habitType == HabitType.QUANTITATIVE,
    onClick = { onHabitTypeChange(HabitType.QUANTITATIVE) },
    expandedContent = { ... }   // unchanged
)
```

- [ ] **Step 5: Update `HabitTypeCard` composable signature and internals**

Add `icon: ImageVector` as the first parameter:

```kotlin
@Composable
private fun HabitTypeCard(
    icon: ImageVector,
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    expandedContent: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier
)
```

Update the `Card` shape and colors:
```kotlin
Card(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    border = if (isSelected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer)
    } else {
        BorderStroke(2.dp, Color.Transparent)
    },
    colors = CardDefaults.cardColors(
        containerColor = if (isSelected) {
            MaterialTheme.colorScheme.surfaceContainerHighest
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }
    )
)
```

Inside the `Column(modifier = Modifier.padding(16.dp))`, add the icon before the label:
```kotlin
Column(modifier = Modifier.padding(16.dp)) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(26.dp),
        tint = if (isSelected) MaterialTheme.colorScheme.primary
               else MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    if (expandedContent != null) {
        AnimatedVisibility(
            visible = isSelected,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            expandedContent()
        }
    }
}
```

- [ ] **Step 6: Verify compilation**

```bash
./gradlew :composeApp:compileDebugKotlinAndroid 2>&1 | grep -E "error:" | head -20
```

Expected: no errors.

- [ ] **Step 7: Run all unit tests**

```bash
./gradlew :composeApp:jvmTest 2>&1 | tail -15
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 8: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/FirstHabitStep.kt
git commit -m "feat(onboarding): filled TextField, type card icons, 24dp radius and updated colors on First Habit screen"
```
