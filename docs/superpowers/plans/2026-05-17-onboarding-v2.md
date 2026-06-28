# Onboarding V2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Port the four onboarding steps (Philosophy → Strictness → First habit → Notifications) to the V2 visual design from `/tmp/rite-design/rite/project/src/screens-a.jsx:25-301`.

**Architecture:** UI-only rewrite. The wizard's state shape, events, navigation flow, and ViewModel logic stay unchanged — only composables, copy, and goldens are touched. Shared chrome moves: the top progress chrome (`OnboardingTopChrome.kt`) is deleted; each step grows a mono "STEP X OF N · NAME" strap at the top of its own content; and a horizontal-bar `OnboardingStepIndicator` lives above the bottom CTA buttons (inside `CtaContainer`).

**Tech Stack:** Compose Multiplatform (commonMain), `RiteAppTheme` V2 design tokens, Compose Resources for strings, Roborazzi for screenshot tests, kotlin-inject for DI (no DI changes needed).

**Branch:** `feature/design-system-v2-04-onboarding` off `feature/design-system-v2` (worktree at `.worktrees/design-system-onboarding/`).

**Spec interpretation note — schedule chips:** The design replaces the per-day SchedulePicker with two chips: `Daily` and `Weekly`. The JSX is silent on what `Weekly` means in data terms. The plan maps `Daily` → `selectedDays = DayOfWeek.entries.toSet()` (existing DAILY-schedule path, no change) and `Weekly` → `selectedDays = emptySet()` with a new `scheduleKind` flag on state so the ViewModel can route to `ScheduleType.FLEXIBLE_WEEKLY` (any day, once per week). If "Weekly" should mean "WEEKLY with one specific day" instead, flag before Task 7.

---

## File Structure

**Created (new files):**
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingStepStrap.kt` — mono "STEP X OF N · NAME" header, used at the top of each step's content.
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingStepIndicator.kt` — horizontal-bar progress + `N / total` mono label, used inside the bottom CTA block.
- `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingStepStrapScreenshotTest.kt` — covers a single rendering case (light/dark).
- `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingStepIndicatorScreenshotTest.kt` — covers step 1/3, 2/3, 3/3, and step 1/4 … 4/4 (light/dark).

**Modified:**
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingWizard.kt` — drop `OnboardingTopChrome` row from the layout; thread `currentStep` + `totalSteps` into each step's CTA so the indicator can render.
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingCta.kt` — `CtaContainer` accepts `step`/`totalSteps`, renders `OnboardingStepIndicator` above its content; each `*StepCta` forwards.
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/PhilosophyStep.kt` — full rewrite: strap, italic-accent hero, 2-paragraph body, 3 promise rows.
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/StrictnessStep.kt` — full rewrite: accordion (one open card; default BALANCED) with radio dot + chevron, expanded body shows description + bulleted rules.
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingStrictnessPreset.kt` — update `description`, `collapsedSummary`, and `rules` to match design verbatim.
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/FirstHabitStep.kt` — full rewrite: strap, italic-accent hero, name field, 2-col type cards (vertical text only, no icons), conditional target+unit grid, two schedule chips.
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionStep.kt` — full rewrite: strap, italic-accent hero, 3 feature rows (icon tile + title + subtitle).
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingState.kt` — add `scheduleKind: OnboardingScheduleKind = DAILY` enum (DAILY, WEEKLY).
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingViewModel.kt` — add `updateScheduleKind(...)`, route to `ScheduleType.FLEXIBLE_WEEKLY` when `WEEKLY`.
- `composeApp/src/commonMain/composeResources/values/strings_onboarding_philosophy.xml` — replace heading/body, add 6 promise keys, add strap key.
- `composeApp/src/commonMain/composeResources/values/strings_onboarding_strictness.xml` — rewrite preset rules to match design, add strap key, change CTA copy to `Continue with {preset}`.
- `composeApp/src/commonMain/composeResources/values/strings_onboarding_first_habit.xml` — replace heading/subtext, type-description copy, add schedule chip + strap keys.
- `composeApp/src/commonMain/composeResources/values/strings_onboarding_notifications.xml` — replace body copy, add strap key.
- `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/PhilosophyScreenshotTest.kt` — re-record goldens.
- `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/StrictnessStepScreenshotTest.kt` — re-record + add expanded-Flexible / expanded-Unwavering cases.
- `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/FirstHabitStepScreenshotTest.kt` — re-record empty/filled/quant/errors variants.
- `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionStepScreenshotTest.kt` — re-record.

**Deleted:**
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingTopChrome.kt` — no longer used; no replacement (each step renders its own strap; progress bars move to bottom).

---

## Design-token cheat sheet

CSS variable from the bundle → `RiteAppTheme` mapping (these are the conventions used in slices 1–3; mirror them):

| CSS var | Compose binding |
|---|---|
| `--c-ink` | `RiteAppTheme.colors.onSurface` |
| `--c-ink-2` | `RiteAppTheme.colors.onSurfaceVariant` |
| `--c-ink-3` | `RiteAppTheme.colors.outline` |
| `--c-rule` | `RiteAppTheme.colors.outlineVariant` |
| `--c-rule-strong` | `RiteAppTheme.colors.outline` |
| `--c-bg` | `RiteAppTheme.colors.background` |
| `--c-surface` | `RiteAppTheme.colors.surface` |
| `--c-fail` | `RiteAppTheme.colors.error` |
| `var(--font-display)` italic | `AnnotatedString` with `SpanStyle(fontStyle = FontStyle.Italic)` |
| `.mono` (10–10.5px, 0.18–0.22em letter-spacing, uppercase) | `RiteAppTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp)`, `text = string.uppercase()` |

---

## Task 1: `OnboardingStepIndicator` component

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingStepIndicator.kt`
- Create: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingStepIndicatorScreenshotTest.kt`

- [ ] **Step 1: Write the screenshot test**

```kotlin
package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OnboardingStepIndicatorScreenshotTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun step1of4_lightTheme() = renderAndCapture(step = 1, total = 4, dark = false)
    @Test fun step2of4_lightTheme() = renderAndCapture(step = 2, total = 4, dark = false)
    @Test fun step4of4_darkTheme() = renderAndCapture(step = 4, total = 4, dark = true)
    @Test fun step1of3_lightTheme() = renderAndCapture(step = 1, total = 3, dark = false)

    private fun renderAndCapture(step: Int, total: Int, dark: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                OnboardingStepIndicator(
                    step = step,
                    totalSteps = total,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd .worktrees/design-system-onboarding && \
./gradlew :composeApp:testDebugUnitTest --tests \
  'com.ricardocosteira.rite.presentation.ui.onboarding.OnboardingStepIndicatorScreenshotTest'
```

Expected: `FAILED` with `Unresolved reference 'OnboardingStepIndicator'`.

- [ ] **Step 3: Write the component**

```kotlin
package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun OnboardingStepIndicator(
    step: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(totalSteps) { index ->
                val isFilled = index < step
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (isFilled) RiteAppTheme.colors.onSurface
                            else RiteAppTheme.colors.outline
                        )
                )
            }
        }
        Text(
            text = "$step / $totalSteps",
            style = RiteAppTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
            color = RiteAppTheme.colors.outline
        )
    }
}
```

- [ ] **Step 4: Record goldens**

```bash
./gradlew :composeApp:recordRoborazziDebug --tests \
  'com.ricardocosteira.rite.presentation.ui.onboarding.OnboardingStepIndicatorScreenshotTest'
```

Expected: `BUILD SUCCESSFUL`. Inspect the four PNGs under `composeApp/src/androidUnitTest/snapshots/images/` and confirm they match the JSX `StepIndicator` (2dp bars, gap 3dp, mono N/total on the right).

- [ ] **Step 5: Verify goldens lock**

```bash
./gradlew :composeApp:verifyRoborazziDebug --tests \
  'com.ricardocosteira.rite.presentation.ui.onboarding.OnboardingStepIndicatorScreenshotTest'
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingStepIndicator.kt \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingStepIndicatorScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/com.ricardocosteira.rite.presentation.ui.onboarding.OnboardingStepIndicatorScreenshotTest.*.png
git commit -m "onboarding(v2): add OnboardingStepIndicator component"
```

---

## Task 2: `OnboardingStepStrap` component

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingStepStrap.kt`
- Create: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingStepStrapScreenshotTest.kt`

- [ ] **Step 1: Write the screenshot test**

```kotlin
package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OnboardingStepStrapScreenshotTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun philosophy_step_lightTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                OnboardingStepStrap(
                    step = 1,
                    totalSteps = 4,
                    stepName = "Philosophy",
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test fun first_habit_step_darkTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = true) {
                OnboardingStepStrap(
                    step = 3,
                    totalSteps = 4,
                    stepName = "First habit",
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :composeApp:testDebugUnitTest --tests \
  'com.ricardocosteira.rite.presentation.ui.onboarding.OnboardingStepStrapScreenshotTest'
```

Expected: `FAILED` with `Unresolved reference 'OnboardingStepStrap'`.

- [ ] **Step 3: Write the component**

```kotlin
package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun OnboardingStepStrap(
    step: Int,
    totalSteps: Int,
    stepName: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "STEP $step OF $totalSteps · ${stepName.uppercase()}",
        style = RiteAppTheme.typography.labelSmall.copy(letterSpacing = 2.2.sp),
        color = RiteAppTheme.colors.outline,
        modifier = modifier
    )
}
```

- [ ] **Step 4: Record goldens**

```bash
./gradlew :composeApp:recordRoborazziDebug --tests \
  'com.ricardocosteira.rite.presentation.ui.onboarding.OnboardingStepStrapScreenshotTest'
```

Expected: `BUILD SUCCESSFUL`. Inspect PNGs against JSX `mono` style (uppercase, 0.22em-ish tracking).

- [ ] **Step 5: Verify**

```bash
./gradlew :composeApp:verifyRoborazziDebug --tests \
  'com.ricardocosteira.rite.presentation.ui.onboarding.OnboardingStepStrapScreenshotTest'
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingStepStrap.kt \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingStepStrapScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/com.ricardocosteira.rite.presentation.ui.onboarding.OnboardingStepStrapScreenshotTest.*.png
git commit -m "onboarding(v2): add OnboardingStepStrap component"
```

---

## Task 3: Restructure wizard — drop top chrome, move indicator into CTA

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingWizard.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingCta.kt`
- Delete: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingTopChrome.kt`

This task only restructures plumbing. Step composables still render their old content until Tasks 5–8 rewrite them. Goldens for the affected screenshot tests will fail after this task — that's expected; the per-step tasks re-record them.

- [ ] **Step 1: Delete `OnboardingTopChrome.kt`**

```bash
git rm composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingTopChrome.kt
```

- [ ] **Step 2: Update `OnboardingCta.kt` — `CtaContainer` renders the indicator above its content**

Replace `CtaContainer`:

```kotlin
@Composable
private fun CtaContainer(
    step: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val translateYAnim = remember { Animatable(16f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (reduceMotion) {
            translateYAnim.snapTo(0f)
            alphaAnim.snapTo(1f)
            return@LaunchedEffect
        }
        delay(200)
        launch { translateYAnim.animateTo(0f, tween(200)) }
        launch { alphaAnim.animateTo(1f, tween(200)) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .graphicsLayer {
                alpha = alphaAnim.value
                translationY = translateYAnim.value.dp.toPx()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OnboardingStepIndicator(
            step = step,
            totalSteps = totalSteps,
            modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp)
        )
        content()
    }
}
```

Update each `*StepCta` signature to forward `step`/`totalSteps`:

```kotlin
@Composable
internal fun PhilosophyStepCta(
    step: Int,
    totalSteps: Int,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    CtaContainer(step = step, totalSteps = totalSteps, modifier = modifier, reduceMotion = reduceMotion) {
        RiteButton(onClick = onAdvance) {
            Text(stringResource(Res.string.philosophy_cta_accept))
        }
    }
}

@Composable
internal fun StrictnessStepCta(
    step: Int,
    totalSteps: Int,
    state: OnboardingState,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    CtaContainer(step = step, totalSteps = totalSteps, modifier = modifier, reduceMotion = reduceMotion) {
        if (state.isApplyingPreset) {
            CircularProgressIndicator(modifier = Modifier.size(36.dp))
        } else {
            RiteButton(onClick = onContinue) {
                Text(
                    stringResource(
                        Res.string.strictness_cta_continue_with_preset,
                        state.selectedPreset.label
                    )
                )
            }
        }
    }
}

@Composable
internal fun FirstHabitStepCta(
    step: Int,
    totalSteps: Int,
    state: OnboardingState,
    onCreateHabit: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    val isEnabled = state.habitName.isNotBlank() &&
        (state.habitType == HabitType.BINARY || state.targetValue.isNotBlank()) &&
        state.selectedDays.isNotEmpty()

    CtaContainer(step = step, totalSteps = totalSteps, modifier = modifier, reduceMotion = reduceMotion) {
        if (state.isCreatingHabit) {
            CircularProgressIndicator(modifier = Modifier.size(36.dp))
        } else {
            RiteButton(onClick = onCreateHabit, enabled = isEnabled) {
                Text(stringResource(Res.string.first_habit_button_create))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(Res.string.first_habit_button_skip),
                style = RiteAppTheme.typography.labelLarge,
                color = RiteAppTheme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun NotificationPermissionStepCta(
    step: Int,
    totalSteps: Int,
    onEnableNotifications: () -> Unit,
    onMaybeLater: () -> Unit,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    CtaContainer(step = step, totalSteps = totalSteps, modifier = modifier, reduceMotion = reduceMotion) {
        RiteButton(onClick = onEnableNotifications) {
            Text(stringResource(Res.string.notifications_cta_enable))
        }

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(onClick = onMaybeLater, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(Res.string.notifications_cta_later),
                style = RiteAppTheme.typography.labelLarge,
                color = RiteAppTheme.colors.onSurfaceVariant
            )
        }
    }
}
```

- [ ] **Step 3: Update `OnboardingWizard.kt` — drop top chrome, pass step into each CTA**

Replace the `Scaffold` body so the `Column` no longer contains `OnboardingTopChrome` and so each `*StepCta` receives `step = currentStep + 1` and `totalSteps = state.totalSteps`. Drop the `onSkip` parameter on `OnboardingWizard` (top Skip button is gone in the design); also drop the call site in `OnboardingRoute.kt` that passes `onSkip = viewModel::skipToToday`.

```kotlin
@Composable
fun OnboardingWizard(
    state: OnboardingState,
    currentStep: Int,
    snackbarHostState: SnackbarHostState,
    onStepChange: (Int) -> Unit,
    onContinueFromNotificationPermission: () -> Unit,
    onEnableNotifications: () -> Unit,
    onContinueFromStrictness: () -> Unit,
    onCreateHabit: () -> Unit,
    onSkipFirstHabit: () -> Unit,
    onPresetSelected: (OnboardingStrictnessPreset) -> Unit,
    onHabitNameChange: (String) -> Unit,
    onHabitTypeChange: (HabitType) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onScheduleKindChange: (OnboardingScheduleKind) -> Unit,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = currentStep > 0) { onStepChange(currentStep - 1) }

    Scaffold(modifier = modifier.fillMaxSize(), snackbarHost = {
        SnackbarHost(snackbarHostState)
    }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // ... existing AnimatedContent / Crossfade block, but each step branch
            //     passes step = currentStep + 1 and totalSteps = state.totalSteps
            //     into its *StepCta; FirstHabitStep also receives state.scheduleKind +
            //     onScheduleKindChange instead of selectedDays/onSelectedDaysChange.
        }
    }
}
```

Update the four step branches to pass `step` + `totalSteps`. Example for Philosophy:

```kotlin
0 -> {
    PhilosophyStep(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        reduceMotion = reduceMotion
    )
    PhilosophyStepCta(
        step = currentStep + 1,
        totalSteps = state.totalSteps,
        onAdvance = { onStepChange(step + 1) },
        modifier = Modifier.fillMaxWidth(),
        reduceMotion = reduceMotion
    )
}
```

Mirror for Strictness / FirstHabit / Notifications. (`FirstHabitStep`'s `selectedDays` / `onSelectedDaysChange` parameters get replaced by `scheduleKind` / `onScheduleKindChange` in Task 7; this task leaves them.)

Also update `OnboardingRoute.kt`: drop `onSkip = viewModel::skipToToday` from the `OnboardingWizard` call; keep `viewModel::skipToToday` itself (it's called by `onSkipFirstHabit` and the Notification "Maybe later" branch path indirectly via existing flow).

- [ ] **Step 4: Verify the project still compiles**

```bash
./gradlew :composeApp:compileDebugKotlinAndroid
```

Expected: `BUILD SUCCESSFUL` (warnings about unused imports OK; remove them if any).

- [ ] **Step 5: Run unit tests + JVM tests (no goldens yet)**

```bash
./gradlew :composeApp:jvmTest :composeApp:testDebugUnitTest
```

Expected: existing onboarding screenshot tests fail (visual diff — `verifyRoborazziDebug` against stale goldens). JVM tests pass. That's the desired state for this task.

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingWizard.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingCta.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingRoute.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingTopChrome.kt
git commit -m "onboarding(v2): drop top chrome, move step indicator into CTA"
```

---

## Task 4: Update preset data + strictness strings

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingStrictnessPreset.kt`
- Modify: `composeApp/src/commonMain/composeResources/values/strings_onboarding_strictness.xml`

- [ ] **Step 1: Replace strictness strings file content**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="strictness_strap_label">Strictness</string>
    <string name="strictness_heading_first">How strict should</string>
    <string name="strictness_heading_accent">Rite</string>
    <string name="strictness_heading_tail">be?</string>
    <string name="strictness_subtext">This is the rule engine. You can change it later — but try not to. That defeats the point.</string>

    <string name="strictness_flexible_label">Flexible</string>
    <string name="strictness_flexible_description">For habits still finding their shape. Generous with undos and skips — the point is to build the daily motion first.</string>
    <string name="strictness_flexible_rule_1">Undo: unlimited, across all history</string>
    <string name="strictness_flexible_rule_2">Snoozes: unlimited · 60-min duration</string>
    <string name="strictness_flexible_rule_3">Skips: unlimited</string>
    <string name="strictness_flexible_rule_4">Consecutive skips: no cap</string>

    <string name="strictness_balanced_label">Balanced</string>
    <string name="strictness_balanced_description">The default for most adults. Small safety net for life, firm enough to build consistency.</string>
    <string name="strictness_balanced_rule_1">Undo: today only</string>
    <string name="strictness_balanced_rule_2">Snoozes: 3 per habit per day · 30-min duration</string>
    <string name="strictness_balanced_rule_3">Skips: allowed</string>
    <string name="strictness_balanced_rule_4">Consecutive skips: max 2</string>

    <string name="strictness_locked_label">Unwavering</string>
    <string name="strictness_locked_description">For habits you\'ve already decided are non-negotiable. No soft landings, no retroactive edits.</string>
    <string name="strictness_locked_rule_1">Undo: disabled</string>
    <string name="strictness_locked_rule_2">Snoozes: 1 per habit per day · 15-min duration</string>
    <string name="strictness_locked_rule_3">Skips: not permitted</string>
    <string name="strictness_locked_rule_4">Consecutive skips: zero</string>

    <string name="strictness_badge_recommended">RECOMMENDED</string>
    <string name="strictness_preset_cd_selected">Selected</string>
    <string name="strictness_preset_cd_not_selected">Not selected</string>

    <string name="strictness_cta_continue_with_preset">Continue with %1$s</string>
</resources>
```

- [ ] **Step 2: Replace `OnboardingStrictnessPreset.kt`**

The enum now carries description + rules text verbatim from the design. (Rules are stored as plain strings — no bullet glyph in the data; bullets are drawn in the composable.)

```kotlin
package com.ricardocosteira.rite.presentation.ui.onboarding

enum class OnboardingStrictnessPreset(
    val label: String,
    val description: String,
    val rules: List<String>,
    val isRecommended: Boolean = false
) {
    FLEXIBLE(
        label = "Flexible",
        description = "For habits still finding their shape. Generous with undos and skips — the point is to build the daily motion first.",
        rules = listOf(
            "Undo: unlimited, across all history",
            "Snoozes: unlimited · 60-min duration",
            "Skips: unlimited",
            "Consecutive skips: no cap"
        )
    ),
    BALANCED(
        label = "Balanced",
        description = "The default for most adults. Small safety net for life, firm enough to build consistency.",
        rules = listOf(
            "Undo: today only",
            "Snoozes: 3 per habit per day · 30-min duration",
            "Skips: allowed",
            "Consecutive skips: max 2"
        ),
        isRecommended = true
    ),
    UNWAVERING(
        label = "Unwavering",
        description = "For habits you've already decided are non-negotiable. No soft landings, no retroactive edits.",
        rules = listOf(
            "Undo: disabled",
            "Snoozes: 1 per habit per day · 15-min duration",
            "Skips: not permitted",
            "Consecutive skips: zero"
        )
    )
}
```

Note the removed fields: `collapsedSummary` and `PresetRule` (key/value). Both are no longer needed — collapsed cards now show only the preset name, and rules are flat strings (the bullet character is rendered by the composable).

- [ ] **Step 3: Run compile to catch any reference to deleted fields**

```bash
./gradlew :composeApp:compileDebugKotlinAndroid
```

Expected: compile fails inside `StrictnessStep.kt` because it references `preset.collapsedSummary` and `PresetRule.key/value`. That's fine — Task 6 rewrites StrictnessStep. If anything outside `StrictnessStep.kt` referenced these (verify with `grep -r 'collapsedSummary\|PresetRule' composeApp/src`), surface the call site and adapt before continuing.

- [ ] **Step 4: Commit preset data + strings (compile still red — wizard rebuild is staged across tasks)**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingStrictnessPreset.kt \
        composeApp/src/commonMain/composeResources/values/strings_onboarding_strictness.xml
git commit -m "onboarding(v2): update strictness preset copy + remove unused fields"
```

---

## Task 5: PhilosophyStep rewrite

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/PhilosophyStep.kt`
- Modify: `composeApp/src/commonMain/composeResources/values/strings_onboarding_philosophy.xml`
- Modify: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/PhilosophyScreenshotTest.kt`

- [ ] **Step 1: Replace strings file**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="philosophy_strap_label">Philosophy</string>
    <string name="philosophy_heading_first">Show up for</string>
    <string name="philosophy_heading_accent">yourself.</string>
    <string name="philosophy_body_intro">Rite is a habit tracker reframed as a quiet instrument. No streaks to guilt you, no badges to collect, no feeds to scroll — only the habits you choose, held to the standard you set.</string>
    <string name="philosophy_body_close">You decide how strict it should be. Rite holds you to it.</string>

    <string name="philosophy_promise_local_title">Local-only</string>
    <string name="philosophy_promise_local_subtitle">Your data stays on this device.</string>
    <string name="philosophy_promise_undistracted_title">Undistracted</string>
    <string name="philosophy_promise_undistracted_subtitle">No social layer, no accounts, no sync-by-default.</string>
    <string name="philosophy_promise_open_title">Open-source</string>
    <string name="philosophy_promise_open_subtitle">Free, transparent, and yours, forever.</string>

    <string name="philosophy_cta_accept">I\'m Ready</string>
</resources>
```

(The old `philosophy_heading` and `philosophy_body` keys are removed; the rewrite splits the title across two text spans for the italic accent.)

- [ ] **Step 2: Replace `PhilosophyStep.kt`**

```kotlin
package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.philosophy_body_close
import rite.composeapp.generated.resources.philosophy_body_intro
import rite.composeapp.generated.resources.philosophy_heading_accent
import rite.composeapp.generated.resources.philosophy_heading_first
import rite.composeapp.generated.resources.philosophy_promise_local_subtitle
import rite.composeapp.generated.resources.philosophy_promise_local_title
import rite.composeapp.generated.resources.philosophy_promise_open_subtitle
import rite.composeapp.generated.resources.philosophy_promise_open_title
import rite.composeapp.generated.resources.philosophy_promise_undistracted_subtitle
import rite.composeapp.generated.resources.philosophy_promise_undistracted_title
import rite.composeapp.generated.resources.philosophy_strap_label

@Composable
fun PhilosophyStep(modifier: Modifier = Modifier, reduceMotion: Boolean = false) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        OnboardingStepStrap(
            step = 1,
            totalSteps = 4,
            stepName = stringResource(Res.string.philosophy_strap_label)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = headingAnnotated(),
            style = RiteAppTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Normal,
                lineHeight = RiteAppTheme.typography.displayMedium.fontSize
            ),
            color = RiteAppTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = stringResource(Res.string.philosophy_body_intro),
            style = RiteAppTheme.typography.bodyLarge,
            color = RiteAppTheme.colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(Res.string.philosophy_body_close),
            style = RiteAppTheme.typography.bodyLarge,
            color = RiteAppTheme.colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(34.dp))

        PromiseRow(
            title = stringResource(Res.string.philosophy_promise_local_title),
            subtitle = stringResource(Res.string.philosophy_promise_local_subtitle)
        )
        Spacer(modifier = Modifier.height(12.dp))
        PromiseRow(
            title = stringResource(Res.string.philosophy_promise_undistracted_title),
            subtitle = stringResource(Res.string.philosophy_promise_undistracted_subtitle)
        )
        Spacer(modifier = Modifier.height(12.dp))
        PromiseRow(
            title = stringResource(Res.string.philosophy_promise_open_title),
            subtitle = stringResource(Res.string.philosophy_promise_open_subtitle)
        )
    }
}

@Composable
private fun headingAnnotated(): AnnotatedString = buildAnnotatedString {
    append(stringResource(Res.string.philosophy_heading_first))
    append(" ")
    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = RiteAppTheme.colors.onSurfaceVariant)) {
        append(stringResource(Res.string.philosophy_heading_accent))
    }
}

@Composable
private fun PromiseRow(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(2.dp))
                .border(
                    width = 1.dp,
                    color = RiteAppTheme.colors.outline,
                    shape = RoundedCornerShape(2.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(RiteAppTheme.colors.onSurface)
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.padding(top = 1.dp)) {
            Text(
                text = title,
                style = RiteAppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = RiteAppTheme.colors.onSurface
            )
            Text(
                text = subtitle,
                style = RiteAppTheme.typography.bodySmall,
                color = RiteAppTheme.colors.outline
            )
        }
    }
}
```

(Animation choreography from the old file is dropped — the design doesn't reference one and slices 1–3 omitted analogous chrome animations. Re-add later if the user wants it.)

- [ ] **Step 3: Update `PhilosophyScreenshotTest.kt`**

If the file already covers light + dark, leave the test names but ensure the composable call passes the correct shape (no signature changes; just verify imports compile after Task 4 / 5).

- [ ] **Step 4: Record + verify goldens**

```bash
./gradlew :composeApp:recordRoborazziDebug --tests \
  'com.ricardocosteira.rite.presentation.ui.onboarding.PhilosophyScreenshotTest'
./gradlew :composeApp:verifyRoborazziDebug --tests \
  'com.ricardocosteira.rite.presentation.ui.onboarding.PhilosophyScreenshotTest'
```

Expected: both succeed. Visually inspect: strap reads `STEP 1 OF 4 · PHILOSOPHY`; heading "Show up for *yourself.*" with italic on the second word; 3 promise rows visible; CTA at bottom shows progress bars + "I'm Ready" button.

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/PhilosophyStep.kt \
        composeApp/src/commonMain/composeResources/values/strings_onboarding_philosophy.xml \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/PhilosophyScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/com.ricardocosteira.rite.presentation.ui.onboarding.PhilosophyScreenshotTest.*.png
git commit -m "onboarding(v2): rewrite PhilosophyStep to match design"
```

---

## Task 6: StrictnessStep rewrite (accordion)

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/StrictnessStep.kt`
- Modify: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/StrictnessStepScreenshotTest.kt`

- [ ] **Step 1: Replace `StrictnessStep.kt`**

```kotlin
package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.strictness_badge_recommended
import rite.composeapp.generated.resources.strictness_heading_accent
import rite.composeapp.generated.resources.strictness_heading_first
import rite.composeapp.generated.resources.strictness_heading_tail
import rite.composeapp.generated.resources.strictness_preset_cd_not_selected
import rite.composeapp.generated.resources.strictness_preset_cd_selected
import rite.composeapp.generated.resources.strictness_strap_label
import rite.composeapp.generated.resources.strictness_subtext

@Composable
fun StrictnessStep(
    selectedPreset: OnboardingStrictnessPreset,
    onPresetSelected: (OnboardingStrictnessPreset) -> Unit,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        OnboardingStepStrap(
            step = 2,
            totalSteps = 4,
            stepName = stringResource(Res.string.strictness_strap_label)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = headingAnnotated(),
            style = RiteAppTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Normal),
            color = RiteAppTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(Res.string.strictness_subtext),
            style = RiteAppTheme.typography.bodySmall,
            color = RiteAppTheme.colors.outline
        )

        Spacer(modifier = Modifier.height(22.dp))

        OnboardingStrictnessPreset.entries.forEach { preset ->
            PresetAccordionCard(
                preset = preset,
                isOpen = preset == selectedPreset,
                onClick = { onPresetSelected(preset) },
                reduceMotion = reduceMotion
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun headingAnnotated(): AnnotatedString = buildAnnotatedString {
    append(stringResource(Res.string.strictness_heading_first))
    append(" ")
    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = RiteAppTheme.colors.onSurfaceVariant)) {
        append(stringResource(Res.string.strictness_heading_accent))
    }
    append(" ")
    append(stringResource(Res.string.strictness_heading_tail))
}

@Composable
private fun PresetAccordionCard(
    preset: OnboardingStrictnessPreset,
    isOpen: Boolean,
    onClick: () -> Unit,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val selectedDesc = stringResource(Res.string.strictness_preset_cd_selected)
    val notSelectedDesc = stringResource(Res.string.strictness_preset_cd_not_selected)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = 1.dp,
                color = if (isOpen) RiteAppTheme.colors.onSurface else RiteAppTheme.colors.outline,
                shape = RoundedCornerShape(4.dp)
            )
            .background(if (isOpen) RiteAppTheme.colors.surface else RiteAppTheme.colors.background)
            .clickable { onClick() }
            .semantics {
                role = Role.RadioButton
                selected = isOpen
                stateDescription = if (isOpen) selectedDesc else notSelectedDesc
            }
            .padding(horizontal = 18.dp, vertical = if (isOpen) 18.dp else 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioDot(isOpen = isOpen)
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = preset.label,
                style = RiteAppTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp,
                    letterSpacing = (-0.1).sp
                ),
                color = RiteAppTheme.colors.onSurface
            )
            if (preset.isRecommended) {
                Spacer(modifier = Modifier.size(8.dp))
                RecommendedBadge()
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (isOpen) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = RiteAppTheme.colors.outline,
                modifier = Modifier.size(16.dp)
            )
        }

        AnimatedVisibility(
            visible = isOpen,
            enter = expandVertically(tween(180)) + fadeIn(tween(180)),
            exit = shrinkVertically(tween(180)) + fadeOut(tween(180))
        ) {
            Column(modifier = Modifier.padding(start = 28.dp, top = 12.dp)) {
                Text(
                    text = preset.description,
                    style = RiteAppTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = RiteAppTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                preset.rules.forEach { rule ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 9.dp)
                                .size(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(RiteAppTheme.colors.onSurface)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = rule,
                            style = RiteAppTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = RiteAppTheme.colors.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun RadioDot(isOpen: Boolean) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(50))
            .border(
                width = 1.5.dp,
                color = if (isOpen) RiteAppTheme.colors.onSurface else RiteAppTheme.colors.outline,
                shape = RoundedCornerShape(50)
            )
            .background(if (isOpen) RiteAppTheme.colors.onSurface else RiteAppTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        if (isOpen) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(RiteAppTheme.colors.background)
            )
        }
    }
}

@Composable
private fun RecommendedBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(RiteAppTheme.colors.onSurface)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = stringResource(Res.string.strictness_badge_recommended),
            style = RiteAppTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = RiteAppTheme.colors.background
        )
    }
}
```

- [ ] **Step 2: Update `StrictnessStepScreenshotTest.kt`**

Add tests for the three accordion states (light + dark for each), matching slice 1–3 naming:

```kotlin
@Test fun balanced_open_lightTheme() = render(OnboardingStrictnessPreset.BALANCED, dark = false)
@Test fun balanced_open_darkTheme() = render(OnboardingStrictnessPreset.BALANCED, dark = true)
@Test fun flexible_open_lightTheme() = render(OnboardingStrictnessPreset.FLEXIBLE, dark = false)
@Test fun unwavering_open_lightTheme() = render(OnboardingStrictnessPreset.UNWAVERING, dark = false)

private fun render(preset: OnboardingStrictnessPreset, dark: Boolean) {
    composeRule.setContent {
        RiteThemeFallback(darkTheme = dark) {
            StrictnessStep(
                selectedPreset = preset,
                onPresetSelected = {},
                modifier = Modifier.padding(0.dp)
            )
        }
    }
    composeRule.onRoot().captureRoboImage()
}
```

- [ ] **Step 3: Record + verify**

```bash
./gradlew :composeApp:recordRoborazziDebug --tests \
  'com.ricardocosteira.rite.presentation.ui.onboarding.StrictnessStepScreenshotTest'
./gradlew :composeApp:verifyRoborazziDebug --tests \
  'com.ricardocosteira.rite.presentation.ui.onboarding.StrictnessStepScreenshotTest'
```

Expected: both succeed. Visually verify: strap `STEP 2 OF 4 · STRICTNESS`; heading "How strict should *Rite* be?"; BALANCED open by default with RECOMMENDED badge + 4 bulleted rules.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/StrictnessStep.kt \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/StrictnessStepScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/com.ricardocosteira.rite.presentation.ui.onboarding.StrictnessStepScreenshotTest.*.png
git commit -m "onboarding(v2): rewrite StrictnessStep as accordion"
```

---

## Task 7: FirstHabitStep rewrite

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/FirstHabitStep.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingState.kt` (add `OnboardingScheduleKind` + `scheduleKind` field)
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingViewModel.kt`
- Modify: `composeApp/src/commonMain/composeResources/values/strings_onboarding_first_habit.xml`
- Modify: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/FirstHabitStepScreenshotTest.kt`

- [ ] **Step 1: Add `OnboardingScheduleKind` and update state**

In `OnboardingState.kt`, append:

```kotlin
enum class OnboardingScheduleKind { DAILY, WEEKLY }
```

…and update the `OnboardingState` data class:

```kotlin
data class OnboardingState(
    val selectedPreset: OnboardingStrictnessPreset = OnboardingStrictnessPreset.BALANCED,
    val habitName: String = "",
    val habitType: HabitType = HabitType.BINARY,
    val targetValue: String = "",
    val unit: String = "",
    val scheduleKind: OnboardingScheduleKind = OnboardingScheduleKind.DAILY,
    val isCreatingHabit: Boolean = false,
    val isApplyingPreset: Boolean = false,
    val error: String? = null,
    val currentStep: Int = 0,
    val showNotificationStep: Boolean = false
) {
    val totalSteps: Int get() = if (showNotificationStep) 4 else 3
    val firstHabitStepIndex: Int get() = if (showNotificationStep) 3 else 2
    val notificationStepIndex: Int get() = 2
}
```

Remove the previous `selectedDays: Set<DayOfWeek>` field (the new design has no per-day picker).

- [ ] **Step 2: Update `OnboardingViewModel.kt`**

Replace `updateSelectedDays` with `updateScheduleKind`, and rewrite `createFirstHabit` to map the schedule kind to `CreateHabitParams`:

```kotlin
fun updateScheduleKind(kind: OnboardingScheduleKind) {
    _state.update { it.copy(scheduleKind = kind) }
}
```

In `createFirstHabit`, replace the `selectedDays` / `specificDays` block with:

```kotlin
val scheduleType: ScheduleType = when (_state.value.scheduleKind) {
    OnboardingScheduleKind.DAILY -> ScheduleType.DAILY
    OnboardingScheduleKind.WEEKLY -> ScheduleType.FLEXIBLE_WEEKLY
}

val result = createHabit.execute(
    params = CreateHabit.CreateHabitParams(
        name = habitName,
        description = null,
        type = habitType,
        targetValue = targetValue,
        unit = unit,
        scheduleType = scheduleType,
        quota = 1,
        specificDays = null,
        reminder = null
    ),
    startDate = today
)
```

Add `import com.ricardocosteira.rite.domain.models.ScheduleType` if missing.

- [ ] **Step 3: Update strings file**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="first_habit_strap_label">First habit</string>
    <string name="first_habit_heading_first">Your first</string>
    <string name="first_habit_heading_accent">ritual.</string>
    <string name="first_habit_subtext">One habit is enough. Name it; commit to it; add more later.</string>

    <string name="first_habit_label_name">Habit name</string>
    <string name="first_habit_type_binary">Binary</string>
    <string name="first_habit_type_binary_description">Did it · didn\'t.</string>
    <string name="first_habit_type_quantitative_description">Count toward a target.</string>
    <string name="first_habit_label_target_value">Target</string>
    <string name="first_habit_label_unit">Unit</string>
    <string name="first_habit_placeholder_unit">E.g. ml, pages, minutes</string>

    <string name="first_habit_label_schedule">Schedule</string>
    <string name="first_habit_schedule_daily">Daily</string>
    <string name="first_habit_schedule_weekly">Weekly</string>

    <string name="first_habit_button_create">Create habit</string>
    <string name="first_habit_button_skip">Skip for now</string>

    <string name="first_habit_error_empty_name">Please enter a habit name</string>
    <string name="first_habit_error_missing_target_value">Please enter a target value for quantitative habit</string>
    <string name="first_habit_error_invalid_target_value">Target value must be a positive number</string>
</resources>
```

- [ ] **Step 4: Replace `FirstHabitStep.kt`**

```kotlin
package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.first_habit_heading_accent
import rite.composeapp.generated.resources.first_habit_heading_first
import rite.composeapp.generated.resources.first_habit_label_name
import rite.composeapp.generated.resources.first_habit_label_schedule
import rite.composeapp.generated.resources.first_habit_label_target_value
import rite.composeapp.generated.resources.first_habit_label_unit
import rite.composeapp.generated.resources.first_habit_placeholder_unit
import rite.composeapp.generated.resources.first_habit_schedule_daily
import rite.composeapp.generated.resources.first_habit_schedule_weekly
import rite.composeapp.generated.resources.first_habit_strap_label
import rite.composeapp.generated.resources.first_habit_subtext
import rite.composeapp.generated.resources.first_habit_type_binary
import rite.composeapp.generated.resources.first_habit_type_binary_description
import rite.composeapp.generated.resources.first_habit_type_quantitative_description

@Composable
fun FirstHabitStep(
    habitName: String,
    habitType: HabitType,
    targetValue: String,
    unit: String,
    scheduleKind: OnboardingScheduleKind,
    onHabitNameChange: (String) -> Unit,
    onHabitTypeChange: (HabitType) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onScheduleKindChange: (OnboardingScheduleKind) -> Unit,
    modifier: Modifier = Modifier
) {
    val isQuantitative = habitType == HabitType.QUANTITATIVE

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        OnboardingStepStrap(
            step = 3,
            totalSteps = 4,
            stepName = stringResource(Res.string.first_habit_strap_label)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = headingAnnotated(),
            style = RiteAppTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Normal),
            color = RiteAppTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(Res.string.first_habit_subtext),
            style = RiteAppTheme.typography.bodySmall,
            color = RiteAppTheme.colors.outline
        )

        Spacer(modifier = Modifier.height(18.dp))

        FieldGroup(label = stringResource(Res.string.first_habit_label_name)) {
            TextField(
                value = habitName,
                onValueChange = onHabitNameChange,
                placeholder = {
                    Text(stringResource(Res.string.first_habit_placeholder_unit))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = RiteAppTheme.colors.surface,
                    unfocusedContainerColor = RiteAppTheme.colors.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        FieldGroup(label = "Type") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TypeCard(
                    label = stringResource(Res.string.first_habit_type_binary),
                    description = stringResource(Res.string.first_habit_type_binary_description),
                    isSelected = !isQuantitative,
                    onClick = { onHabitTypeChange(HabitType.BINARY) },
                    modifier = Modifier.weight(1f)
                )
                TypeCard(
                    label = "Quantitative",
                    description = stringResource(Res.string.first_habit_type_quantitative_description),
                    isSelected = isQuantitative,
                    onClick = { onHabitTypeChange(HabitType.QUANTITATIVE) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        AnimatedVisibility(
            visible = isQuantitative,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FieldGroup(
                        label = stringResource(Res.string.first_habit_label_target_value),
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = targetValue,
                            onValueChange = onTargetValueChange,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = RiteAppTheme.colors.surface,
                                unfocusedContainerColor = RiteAppTheme.colors.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    FieldGroup(
                        label = stringResource(Res.string.first_habit_label_unit),
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = unit,
                            onValueChange = onUnitChange,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = RiteAppTheme.colors.surface,
                                unfocusedContainerColor = RiteAppTheme.colors.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        FieldGroup(label = stringResource(Res.string.first_habit_label_schedule)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScheduleChip(
                    label = stringResource(Res.string.first_habit_schedule_daily),
                    isSelected = scheduleKind == OnboardingScheduleKind.DAILY,
                    onClick = { onScheduleKindChange(OnboardingScheduleKind.DAILY) }
                )
                ScheduleChip(
                    label = stringResource(Res.string.first_habit_schedule_weekly),
                    isSelected = scheduleKind == OnboardingScheduleKind.WEEKLY,
                    onClick = { onScheduleKindChange(OnboardingScheduleKind.WEEKLY) }
                )
            }
        }
    }
}

@Composable
private fun headingAnnotated(): AnnotatedString = buildAnnotatedString {
    append(stringResource(Res.string.first_habit_heading_first))
    append(" ")
    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = RiteAppTheme.colors.onSurfaceVariant)) {
        append(stringResource(Res.string.first_habit_heading_accent))
    }
}

@Composable
private fun FieldGroup(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = RiteAppTheme.typography.labelSmall,
            color = RiteAppTheme.colors.outline,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        content()
    }
}

@Composable
private fun TypeCard(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = 1.dp,
                color = if (isSelected) RiteAppTheme.colors.onSurface else RiteAppTheme.colors.outline,
                shape = RoundedCornerShape(4.dp)
            )
            .background(if (isSelected) RiteAppTheme.colors.surface else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            style = RiteAppTheme.typography.titleSmall,
            color = RiteAppTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            style = RiteAppTheme.typography.bodySmall,
            color = RiteAppTheme.colors.outline
        )
    }
}

@Composable
private fun ScheduleChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(
                width = 1.dp,
                color = if (isSelected) RiteAppTheme.colors.onSurface else RiteAppTheme.colors.outline,
                shape = RoundedCornerShape(50)
            )
            .background(if (isSelected) RiteAppTheme.colors.onSurface else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = RiteAppTheme.typography.labelLarge,
            color = if (isSelected) RiteAppTheme.colors.surface else RiteAppTheme.colors.onSurface
        )
    }
}
```

(The existing `SchedulePicker` is no longer referenced from this screen. If `SchedulePicker` is only used by onboarding, leave it alone — `HabitFormScreen` still uses it for richer scheduling. Verify with `grep -r 'SchedulePicker' composeApp/src` before assuming.)

- [ ] **Step 5: Update `OnboardingWizard.kt` First Habit branch to pass new params**

```kotlin
step == state.firstHabitStepIndex -> {
    FirstHabitStep(
        habitName = state.habitName,
        habitType = state.habitType,
        targetValue = state.targetValue,
        unit = state.unit,
        scheduleKind = state.scheduleKind,
        onHabitNameChange = onHabitNameChange,
        onHabitTypeChange = onHabitTypeChange,
        onTargetValueChange = onTargetValueChange,
        onUnitChange = onUnitChange,
        onScheduleKindChange = onScheduleKindChange,
        modifier = Modifier.weight(1f).fillMaxWidth()
    )
    FirstHabitStepCta(
        step = currentStep + 1,
        totalSteps = state.totalSteps,
        state = state,
        onCreateHabit = onCreateHabit,
        onSkip = onSkipFirstHabit,
        modifier = Modifier.fillMaxWidth(),
        reduceMotion = reduceMotion
    )
}
```

Also update the `CtaContainer` enable-check inside `FirstHabitStepCta` — replace `state.selectedDays.isNotEmpty()` with `true` (schedule kind always has a value):

```kotlin
val isEnabled = state.habitName.isNotBlank() &&
    (state.habitType == HabitType.BINARY || state.targetValue.isNotBlank())
```

And in `OnboardingRoute.kt`, replace `onSelectedDaysChange = viewModel::updateSelectedDays` with `onScheduleKindChange = viewModel::updateScheduleKind`.

- [ ] **Step 6: Update `FirstHabitStepScreenshotTest.kt`**

Variants from the design: `empty`, `filled_binary`, `filled_quant`, `error_missing_name`, `error_missing_target`. Light + dark for at least 2 of them; check existing test for variant coverage and re-record everything that exists. Example fixture wiring:

```kotlin
@Test fun firstHabit_empty_lightTheme() = render(state = empty(), dark = false)
@Test fun firstHabit_filledBinary_darkTheme() = render(state = filledBinary(), dark = true)
@Test fun firstHabit_filledQuant_lightTheme() = render(state = filledQuant(), dark = false)

private fun empty() = FirstHabitFixture()
private fun filledBinary() = FirstHabitFixture(name = "Morning sit")
private fun filledQuant() = FirstHabitFixture(
    name = "Drink water",
    type = HabitType.QUANTITATIVE,
    target = "2000",
    unit = "ml"
)
```

(Adapt `FirstHabitFixture` to the existing test helpers — slice 1–3 tests use plain data classes for fixtures; mirror their shape.)

- [ ] **Step 7: Record + verify**

```bash
./gradlew :composeApp:recordRoborazziDebug --tests \
  'com.ricardocosteira.rite.presentation.ui.onboarding.FirstHabitStepScreenshotTest'
./gradlew :composeApp:verifyRoborazziDebug --tests \
  'com.ricardocosteira.rite.presentation.ui.onboarding.FirstHabitStepScreenshotTest'
```

Expected: both succeed.

- [ ] **Step 8: Run JVM tests to confirm ViewModel logic still works**

```bash
./gradlew :composeApp:jvmTest
```

Expected: passes. If `OnboardingViewModelScheduleTest.kt` exists and still asserts against `selectedDays` / `specificDays`, replace its assertions to drive `scheduleKind` instead (the test's intent is "the right ScheduleType reaches CreateHabit"; rewrite to:

```kotlin
@Test fun `given DAILY scheduleKind when creating habit then schedule type is DAILY`() = runTest {
    val vm = buildViewModel()
    vm.updateHabitName("Run")
    vm.updateScheduleKind(OnboardingScheduleKind.DAILY)
    vm.createFirstHabit()
    assertEquals(ScheduleType.DAILY, fakeHabitRepository.capturedSchedule?.scheduleType)
}

@Test fun `given WEEKLY scheduleKind when creating habit then schedule type is FLEXIBLE_WEEKLY`() = runTest {
    val vm = buildViewModel()
    vm.updateHabitName("Rest")
    vm.updateScheduleKind(OnboardingScheduleKind.WEEKLY)
    vm.createFirstHabit()
    assertEquals(ScheduleType.FLEXIBLE_WEEKLY, fakeHabitRepository.capturedSchedule?.scheduleType)
}
```

- [ ] **Step 9: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/FirstHabitStep.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingState.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingViewModel.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingWizard.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingRoute.kt \
        composeApp/src/commonMain/composeResources/values/strings_onboarding_first_habit.xml \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/FirstHabitStepScreenshotTest.kt \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingViewModelScheduleTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/com.ricardocosteira.rite.presentation.ui.onboarding.FirstHabitStepScreenshotTest.*.png
git commit -m "onboarding(v2): rewrite FirstHabitStep + switch to Daily/Weekly chips"
```

---

## Task 8: NotificationPermissionStep rewrite

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionStep.kt`
- Modify: `composeApp/src/commonMain/composeResources/values/strings_onboarding_notifications.xml`
- Modify: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionStepScreenshotTest.kt`

- [ ] **Step 1: Update strings**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="notifications_strap_label">Notifications</string>
    <string name="notifications_heading_first">With you</string>
    <string name="notifications_heading_accent">every step.</string>
    <string name="notifications_body">Rite can send you a handful of quiet, structured nudges. Permission stays yours — you can deny this and still use the app.</string>

    <string name="notifications_card_reminders_title">Habit reminders</string>
    <string name="notifications_card_reminders_subtitle">A quiet nudge at the time you set — once, or on a periodic window.</string>
    <string name="notifications_card_warnings_title">Deadline warnings</string>
    <string name="notifications_card_warnings_subtitle">A gentle notice when a quantitative habit is close to missing its window.</string>
    <string name="notifications_card_tracking_title">Progress tracking</string>
    <string name="notifications_card_tracking_subtitle">A silent, persistent notification mirroring today\'s rituals in the shade.</string>

    <string name="notifications_cta_enable">Enable notifications</string>
    <string name="notifications_cta_later">Maybe later</string>
</resources>
```

- [ ] **Step 2: Replace `NotificationPermissionStep.kt`**

```kotlin
package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.notifications_body
import rite.composeapp.generated.resources.notifications_card_reminders_subtitle
import rite.composeapp.generated.resources.notifications_card_reminders_title
import rite.composeapp.generated.resources.notifications_card_tracking_subtitle
import rite.composeapp.generated.resources.notifications_card_tracking_title
import rite.composeapp.generated.resources.notifications_card_warnings_subtitle
import rite.composeapp.generated.resources.notifications_card_warnings_title
import rite.composeapp.generated.resources.notifications_heading_accent
import rite.composeapp.generated.resources.notifications_heading_first
import rite.composeapp.generated.resources.notifications_strap_label

@Composable
fun NotificationPermissionStep(modifier: Modifier = Modifier, reduceMotion: Boolean = false) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        OnboardingStepStrap(
            step = 4,
            totalSteps = 4,
            stepName = stringResource(Res.string.notifications_strap_label)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = headingAnnotated(),
            style = RiteAppTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Normal),
            color = RiteAppTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(Res.string.notifications_body),
            style = RiteAppTheme.typography.bodySmall,
            color = RiteAppTheme.colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        NotificationFeatureRow(
            icon = Icons.Outlined.NotificationsActive,
            title = stringResource(Res.string.notifications_card_reminders_title),
            subtitle = stringResource(Res.string.notifications_card_reminders_subtitle)
        )
        Spacer(modifier = Modifier.height(10.dp))
        NotificationFeatureRow(
            icon = Icons.Outlined.Warning,
            title = stringResource(Res.string.notifications_card_warnings_title),
            subtitle = stringResource(Res.string.notifications_card_warnings_subtitle)
        )
        Spacer(modifier = Modifier.height(10.dp))
        NotificationFeatureRow(
            icon = Icons.Outlined.ShowChart,
            title = stringResource(Res.string.notifications_card_tracking_title),
            subtitle = stringResource(Res.string.notifications_card_tracking_subtitle)
        )
    }
}

@Composable
private fun headingAnnotated(): AnnotatedString = buildAnnotatedString {
    append(stringResource(Res.string.notifications_heading_first))
    append(" ")
    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = RiteAppTheme.colors.onSurfaceVariant)) {
        append(stringResource(Res.string.notifications_heading_accent))
    }
}

@Composable
private fun NotificationFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = 1.dp,
                color = RiteAppTheme.colors.outlineVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .background(RiteAppTheme.colors.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(3.dp))
                .border(
                    width = 1.dp,
                    color = RiteAppTheme.colors.outline,
                    shape = RoundedCornerShape(3.dp)
                )
                .background(RiteAppTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = RiteAppTheme.colors.onSurface
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column {
            Text(
                text = title,
                style = RiteAppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = RiteAppTheme.colors.onSurface
            )
            Text(
                text = subtitle,
                style = RiteAppTheme.typography.bodySmall,
                color = RiteAppTheme.colors.onSurfaceVariant
            )
        }
    }
}
```

(The body Text styling above is intentionally simple. If the existing typography is missing a body-line-height variant, adopt the closest one and remove the inline math.)

- [ ] **Step 3: Record + verify**

```bash
./gradlew :composeApp:recordRoborazziDebug --tests \
  'com.ricardocosteira.rite.presentation.ui.onboarding.NotificationPermissionStepScreenshotTest'
./gradlew :composeApp:verifyRoborazziDebug --tests \
  'com.ricardocosteira.rite.presentation.ui.onboarding.NotificationPermissionStepScreenshotTest'
```

Expected: both succeed.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionStep.kt \
        composeApp/src/commonMain/composeResources/values/strings_onboarding_notifications.xml \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionStepScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/com.ricardocosteira.rite.presentation.ui.onboarding.NotificationPermissionStepScreenshotTest.*.png
git commit -m "onboarding(v2): rewrite NotificationPermissionStep to match design"
```

---

## Task 9: Final integration sweep + push PR

**Files:** none — verification only.

- [ ] **Step 1: Full verify Roborazzi + unit tests**

```bash
./gradlew :composeApp:verifyRoborazziDebug :composeApp:jvmTest :composeApp:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`. If anything red, fix and re-record locally before pushing.

- [ ] **Step 2: Confirm no orphans**

```bash
grep -rn "OnboardingTopChrome\|collapsedSummary\|PresetRule\|onSelectedDaysChange\|selectedDays" \
  composeApp/src
```

Expected: no hits outside `HabitFormScreen.kt` / unrelated code (those still legitimately use `selectedDays` for the full form). If onboarding references remain, surface and fix.

- [ ] **Step 3: Push branch**

```bash
git push -u origin feature/design-system-v2-04-onboarding
```

- [ ] **Step 4: Open PR into V2**

```bash
gh pr create --base feature/design-system-v2 --title "Slice 4: Onboarding on v2 primitives" \
  --body "$(cat <<'EOF'
## Summary
- Port Philosophy / Strictness / First habit / Notifications to the V2 design (`/tmp/rite-design/.../screens-a.jsx:25-301`).
- Drop top progress chrome; each step shows a mono "STEP X OF N · NAME" strap inline, and the bottom CTA carries a horizontal-bar `OnboardingStepIndicator`.
- Strictness is now an accordion with one open card; rules + descriptions match design verbatim.
- First-habit schedule simplifies to `Daily` / `Weekly` chips; `Weekly` maps to `ScheduleType.FLEXIBLE_WEEKLY`.

## Test Plan
- [x] `:composeApp:verifyRoborazziDebug` green on freshly recorded onboarding goldens
- [x] `:composeApp:jvmTest` + `:composeApp:testDebugUnitTest` green
- [ ] Visual spot-check: light + dark, each step (strap, hero, body, CTA + indicator)
- [ ] Manual flow on device: Philosophy → Strictness (pick Flexible) → First habit (Weekly) → finish without enabling notifications

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

- [ ] **Step 5: Watch checks**

```bash
gh pr checks
```

Expected: CI green. If red, address before merge.

---

## Self-review

**Spec coverage**

| Spec element (screens-a.jsx) | Implemented in |
|---|---|
| 4-step horizontal-bar `StepIndicator` (lines 12–23) | Task 1 |
| "STEP N OF 4 · NAME" mono strap (lines 29, 153, 182, 270) | Task 2 |
| Wizard layout (no top chrome, indicator at bottom) | Task 3 |
| `OnbPhilosophy` (lines 25–62) | Task 5 |
| `STRICT_PRESETS` data + accordion (lines 65–171) | Tasks 4, 6 |
| `OnbFirstHabit` form + Daily/Weekly chips (lines 173–264) | Task 7 |
| `OnbNotif` 3 feature rows (lines 266–301) | Task 8 |
| PR + CI gates | Task 9 |

**Placeholder scan:** no "TBD"/"TODO"/"handle edge cases" — code blocks and exact commands throughout. One judgment call documented inline: schedule `Weekly` → `FLEXIBLE_WEEKLY` (called out in the spec interpretation note at the top so the user can override before Task 7).

**Type consistency:** `OnboardingScheduleKind` defined in Task 7 Step 1; referenced in Tasks 3, 7, and 9. `step` + `totalSteps` parameters added to all four `*StepCta` signatures in Task 3 and used unchanged in subsequent tasks. `OnboardingStrictnessPreset.collapsedSummary` and the `PresetRule` class are removed in Task 4 — verified no other module references them (the existing call site is `StrictnessStep.kt` which gets rewritten in Task 6).
