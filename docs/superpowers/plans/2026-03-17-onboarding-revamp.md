# Onboarding Revamp Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the three separate onboarding Nav3 routes with a single self-contained animated wizard that follows the editorial design (Approach B) with M3 teal colour scheme and staggered animations.

**Architecture:** A single `Route.Onboarding` hosts `OnboardingRoute`, which collects VM events and owns `currentStep` state. `OnboardingWizard` renders top chrome, `AnimatedContent` for steps, and the CTA outside the animated region. Step composables are pure UI; all business logic stays in the unchanged `OnboardingViewModel`.

**Tech Stack:** Kotlin Multiplatform, Jetpack Compose Multiplatform, Material Design 3, Nav3 (`androidx.navigation3`), `kotlin.test`

---

## File Map

| Action | File | Responsibility |
|--------|------|----------------|
| Modify | `presentation/ui/theme/Theme.kt` | Custom M3 light/dark `ColorScheme` constants |
| Modify | `presentation/ui/theme/Theme.android.kt` | Use custom colours on pre-S devices |
| Modify | `presentation/navigation/Route.kt` | Replace 3 onboarding routes with `Onboarding` |
| Modify | `presentation/ui/onboarding/OnboardingState.kt` | Remove `NavigateToStrictness` event |
| Modify | `presentation/ui/onboarding/OnboardingViewModel.kt` | Remove `continueFromPhilosophy()` |
| Create | `presentation/ui/onboarding/OnboardingRoute.kt` | Event collection, step state, nav to Today |
| Create | `presentation/ui/onboarding/OnboardingWizard.kt` | `AnimatedContent` step transitions, `BackHandler` |
| Create | `presentation/ui/onboarding/OnboardingTopChrome.kt` | Expanding dots + Skip button |
| Create | `presentation/ui/onboarding/OnboardingCta.kt` | Step-aware bottom CTA |
| Create | `presentation/ui/onboarding/PhilosophyStep.kt` | Step 0 UI — headline, body, lock watermark |
| Create | `presentation/ui/onboarding/StrictnessStep.kt` | Step 1 UI — preset cards with accordion |
| Create | `presentation/ui/onboarding/FirstHabitStep.kt` | Step 2 UI — habit name form |
| Modify | `presentation/navigation/HabitLockNavigation.kt` | Replace 3 entries with single `Onboarding` entry |
| Delete | `presentation/ui/onboarding/PhilosophyScreen.kt` | Replaced by `PhilosophyStep` |
| Delete | `presentation/ui/onboarding/StrictnessScreen.kt` | Replaced by `StrictnessStep` |
| Delete | `presentation/ui/onboarding/FirstHabitScreen.kt` | Replaced by `FirstHabitStep` |

All paths are relative to `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/`.

**Note on testing:** This revamp is primarily UI composition. The `OnboardingViewModel` business logic is unchanged and already tested. New composables have no extractable pure Kotlin logic to unit test — visual correctness is verified by running the app. Compile checks after each task catch integration errors early.

---

## Task 1: Custom M3 colour scheme

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/theme/Theme.kt`
- Modify: `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/theme/Theme.android.kt`

- [ ] **Step 1: Replace default colour schemes in `Theme.kt`**

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TealPrimary = Color(0xFF006A6B)
val TealPrimaryContainer = Color(0xFFC0EDED)
val TealOnPrimaryContainer = Color(0xFF002020)
val TealSurface = Color(0xFFFAFCFC)
val TealOnSurface = Color(0xFF191C1C)
val TealOnSurfaceVariant = Color(0xFF3F4948)

val TealDarkPrimary = Color(0xFF4CDADA)
val TealDarkPrimaryContainer = Color(0xFF004F50)
val TealDarkOnPrimaryContainer = Color(0xFF9FF3F3)
val TealDarkSurface = Color(0xFF1B2030)
val TealDarkOnSurface = Color(0xFFDCE4E4)
val TealDarkOnSurfaceVariant = Color(0xFFBEC9C8)

val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = TealOnPrimaryContainer,
    surface = TealSurface,
    onSurface = TealOnSurface,
    onSurfaceVariant = TealOnSurfaceVariant,
)

val DarkColorScheme = darkColorScheme(
    primary = TealDarkPrimary,
    primaryContainer = TealDarkPrimaryContainer,
    onPrimaryContainer = TealDarkOnPrimaryContainer,
    surface = TealDarkSurface,
    onSurface = TealDarkOnSurface,
    onSurfaceVariant = TealDarkOnSurfaceVariant,
)

@Composable
expect fun HabitLockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
)

@Composable
fun HabitLockThemeFallback(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
```

- [ ] **Step 2: Update `Theme.android.kt` to use custom colours on pre-S devices**

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun HabitLockTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
```

- [ ] **Step 3: Compile check**

```bash
./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -20
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/theme/Theme.kt \
        composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/theme/Theme.android.kt
git commit -m "feat(theme): apply custom M3 teal colour scheme"
```

---

## Task 2: Route and navigation cleanup

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/Route.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/HabitLockNavigation.kt`

- [ ] **Step 1: Replace three onboarding routes with one in `Route.kt`**

Remove `OnboardingPhilosophy`, `OnboardingStrictness`, `OnboardingFirstHabit`. Add:

```kotlin
@Serializable
data object Onboarding : Route
```

- [ ] **Step 2: Update `HabitLockNavigation.kt` — serializers module and initial route**

In `savedStateConfig`, replace the three subclass registrations:
```kotlin
// Remove:
subclass(OnboardingPhilosophy::class)
subclass(OnboardingStrictness::class)
subclass(OnboardingFirstHabit::class)
// Add:
subclass(Onboarding::class)
```

Update the initial route:
```kotlin
val initialRoute: Route = if (isOnboardingCompleted) Today else Onboarding
```

- [ ] **Step 3: Replace three onboarding entries with a stub in `HabitLockNavigation.kt`**

Remove the `entry<OnboardingPhilosophy>`, `entry<OnboardingStrictness>`, and `entry<OnboardingFirstHabit>` blocks. Add a temporary stub so the file compiles while the new composables are being built:

```kotlin
entry<Onboarding> {
    // TODO: replace with OnboardingRoute in Task 7
    androidx.compose.material3.Text("Onboarding coming soon")
}
```

Also remove the now-unused imports for `PhilosophyScreen`, `StrictnessScreen`, `FirstHabitScreen`, `OnboardingEvent`, and the onboarding `LaunchedEffect` block — these will be handled by `OnboardingRoute`. Remove the `onboardingViewModel` parameter from `HabitLockNavigation` for now — it will be re-added in Task 7.

> **Note:** The `LaunchedEffect` that collected `onboardingViewModel.events` in `HabitLockNavigation` is fully removed — that responsibility moves to `OnboardingRoute`.

- [ ] **Step 4: Compile check**

```bash
./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -20
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/Route.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/HabitLockNavigation.kt
git commit -m "feat(nav): replace three onboarding routes with single Onboarding route"
```

---

## Task 3: Clean up OnboardingViewModel and OnboardingState

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingState.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingViewModel.kt`

- [ ] **Step 1: Remove `NavigateToStrictness` from `OnboardingEvent`**

In `OnboardingState.kt`, remove `data object NavigateToStrictness : OnboardingEvent`.

- [ ] **Step 2: Remove `continueFromPhilosophy()` from `OnboardingViewModel`**

This method only emitted `NavigateToStrictness`, which is now unused. Delete the method entirely.

- [ ] **Step 3: Compile check**

```bash
./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -20
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingState.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingViewModel.kt
git commit -m "refactor(onboarding): remove obsolete NavigateToStrictness event and continueFromPhilosophy"
```

---

## Task 4: OnboardingTopChrome

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingTopChrome.kt`

The top chrome renders the expanding dot progress indicator (left) and a Skip button (right). It lives outside `AnimatedContent` so it does not re-animate on step transitions.

- [ ] **Step 1: Create `OnboardingTopChrome.kt`**

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private const val TOTAL_STEPS = 3
private const val DONE_DOT_ALPHA = 0.45f

@Composable
fun OnboardingTopChrome(
    currentStep: Int,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ProgressDots(
            currentStep = currentStep,
            modifier = Modifier
                .weight(1f)
                .semantics {
                    contentDescription = "Step ${currentStep + 1} of $TOTAL_STEPS"
                }
        )

        TextButton(onClick = onSkip) {
            Text(
                text = "Skip",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProgressDots(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(TOTAL_STEPS) { index ->
            StepDot(
                state = when {
                    index < currentStep -> DotState.Done
                    index == currentStep -> DotState.Active
                    else -> DotState.Inactive
                }
            )
        }
    }
}

private enum class DotState { Active, Done, Inactive }

@Composable
private fun StepDot(
    state: DotState,
    modifier: Modifier = Modifier
) {
    val targetWidth = if (state == DotState.Active) 20.dp else 6.dp
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dot_width"
    )

    val targetAlpha = if (state == DotState.Inactive) 1f else if (state == DotState.Done) DONE_DOT_ALPHA else 1f
    val animatedAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        label = "dot_alpha"
    )

    val color = if (state == DotState.Inactive) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .width(animatedWidth)
            .height(6.dp)
            .alpha(animatedAlpha)
            .background(color = color, shape = RoundedCornerShape(3.dp))
    )
}
```

- [ ] **Step 2: Compile check**

```bash
./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -20
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingTopChrome.kt
git commit -m "feat(onboarding): add OnboardingTopChrome with expanding dot progress indicator"
```

---

## Task 5: PhilosophyStep

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/PhilosophyStep.kt`

Editorial layout: bold headline, teal accent underline, body copy, and a tonal lock icon watermark anchored bottom-right. Entry animation staggers headline → accent line → body → lock watermark.

- [ ] **Step 1: Create `PhilosophyStep.kt`**

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.text.font.FontWeight

@Composable
fun PhilosophyStep(modifier: Modifier = Modifier) {
    val headlineAlpha = remember { Animatable(0f) }
    val headlineTranslateY = remember { Animatable(12f) }
    val accentWidth = remember { Animatable(0f) }
    val bodyAlpha = remember { Animatable(0f) }
    val lockAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Headline: fade + translate up — 200ms, 0ms delay
        launch {
            headlineAlpha.animateTo(1f, tween(200))
        }
        launch {
            headlineTranslateY.animateTo(0f, tween(200))
        }
        // Accent line: width draws in — 250ms, 80ms delay
        kotlinx.coroutines.delay(80)
        launch {
            accentWidth.animateTo(1f, tween(250))
        }
        // Body: fade in — 200ms, 140ms delay
        kotlinx.coroutines.delay(60) // 80 + 60 = 140ms total
        launch {
            bodyAlpha.animateTo(1f, tween(200))
        }
        // Lock watermark: fade in — 200ms, 300ms delay
        kotlinx.coroutines.delay(160) // 140 + 160 = 300ms total
        lockAlpha.animateTo(0.45f, tween(200))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enforce what\nyou commit to.",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .alpha(headlineAlpha.value)
                    .graphicsLayer { translationY = headlineTranslateY.value.dp.toPx() }
                    .semantics { heading() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Animated accent line
            Box(
                modifier = Modifier
                    .width((36 * accentWidth.value).dp)
                    .height(3.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Keep promises to yourself — even on hard days.\n\n" +
                        "You choose the rules. HabitLock helps you stick to them.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(bodyAlpha.value)
            )
        }

        // Lock watermark — decorative, bottom-right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp)
                .size(88.dp)
                .alpha(lockAlpha.value)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
```

- [ ] **Step 2: Compile check**

```bash
./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -20
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/PhilosophyStep.kt
git commit -m "feat(onboarding): add PhilosophyStep with editorial layout and staggered entry animation"
```

---

## Task 6: StrictnessStep

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/StrictnessStep.kt`

Three preset cards. Only the selected card shows its rules (accordion). Cards use `OutlinedCard` styling. Balanced is pre-selected with a "Recommended" chip.

- [ ] **Step 1: Create `StrictnessStep.kt`**

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.domain.models.StrictnessPreset

private data class PresetInfo(
    val preset: StrictnessPreset,
    val label: String,
    val description: String,
    val dotColor: Color,
    val rules: List<String>,
    val isRecommended: Boolean = false
)

private val PRESETS = listOf(
    PresetInfo(
        preset = StrictnessPreset.FLEXIBLE,
        label = "Flexible",
        description = "Gentle support, maximum forgiveness.",
        dotColor = Color(0xFF4CAF50),
        rules = listOf(
            "Unlimited undo",
            "Unlimited snoozes",
            "Skips allowed without limits",
            "Missed habits tracked lightly"
        )
    ),
    PresetInfo(
        preset = StrictnessPreset.BALANCED,
        label = "Balanced",
        description = "Structure with room for real life.",
        dotColor = Color(0xFFFF9800),
        rules = listOf(
            "Undo allowed for today only",
            "Snoozes are limited",
            "Skips are limited",
            "Missed habits fail at end of day"
        ),
        isRecommended = true
    ),
    PresetInfo(
        preset = StrictnessPreset.LOCKED,
        label = "Locked",
        description = "No excuses. Full accountability.",
        dotColor = Color(0xFFF44336),
        rules = listOf(
            "No undo",
            "Snoozes are capped",
            "Skips are capped",
            "Missed habits always fail"
        )
    )
)

@Composable
fun StrictnessStep(
    selectedPreset: StrictnessPreset,
    onPresetSelected: (StrictnessPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "How strict\nshould it be?",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .width(36.dp)
                .height(3.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(2.dp)
                )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "You're always in control. Change this anytime.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        PRESETS.forEach { info ->
            PresetCard(
                info = info,
                isSelected = info.preset == selectedPreset,
                onClick = { onPresetSelected(info.preset) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PresetCard(
    info: PresetInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                stateDescription = if (isSelected) "Selected" else "Not selected"
            }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = info.dotColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = info.label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (info.isRecommended) {
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = "Recommended",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = info.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp)
        )

        AnimatedVisibility(
            visible = isSelected,
            enter = expandVertically(tween(250)) + fadeIn(tween(250)),
            exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
        ) {
            Column(modifier = Modifier.padding(top = 10.dp, start = 16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(1.dp)
                        )
                )
                Spacer(modifier = Modifier.height(10.dp))
                info.rules.forEach { rule ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(bottom = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = rule,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Compile check**

```bash
./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -20
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/StrictnessStep.kt
git commit -m "feat(onboarding): add StrictnessStep with accordion preset cards"
```

---

## Task 7: FirstHabitStep

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/FirstHabitStep.kt`

Form with habit name field, Yes/No + Quantitative type chips, and conditional target value / unit fields for quantitative habits.

- [ ] **Step 1: Create `FirstHabitStep.kt`**

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.domain.models.HabitType

@Composable
fun FirstHabitStep(
    habitName: String,
    habitType: HabitType,
    targetValue: String,
    unit: String,
    onHabitNameChange: (String) -> Unit,
    onHabitTypeChange: (HabitType) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Lock in your\nfirst habit.",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .width(36.dp)
                .height(3.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(2.dp)
                )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Start small. One habit is enough to begin.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = habitName,
            onValueChange = onHabitNameChange,
            label = { Text("Habit name") },
            placeholder = { Text("E.g. Drink water") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Type",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = habitType == HabitType.BINARY,
                onClick = { onHabitTypeChange(HabitType.BINARY) },
                label = { Text("Yes / No") }
            )
            FilterChip(
                selected = habitType == HabitType.QUANTITATIVE,
                onClick = { onHabitTypeChange(HabitType.QUANTITATIVE) },
                label = { Text("Quantitative") }
            )
        }

        AnimatedVisibility(
            visible = habitType == HabitType.QUANTITATIVE,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = targetValue,
                    onValueChange = onTargetValueChange,
                    label = { Text("Target value") },
                    placeholder = { Text("E.g. 8") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = unit,
                    onValueChange = onUnitChange,
                    label = { Text("Unit (optional)") },
                    placeholder = { Text("E.g. glasses, pages, minutes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
```

- [ ] **Step 2: Compile check**

```bash
./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -20
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/FirstHabitStep.kt
git commit -m "feat(onboarding): add FirstHabitStep with habit form"
```

---

## Task 8: OnboardingCta

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingCta.kt`

Step-aware CTA. Dispatches different actions per step. Shows a loading indicator during async operations on steps 1 and 2.

- [ ] **Step 1: Create `OnboardingCta.kt`**

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.domain.models.HabitType

@Composable
fun OnboardingCta(
    currentStep: Int,
    state: OnboardingState,
    onAdvance: () -> Unit,
    onContinueFromStrictness: () -> Unit,
    onCreateHabit: () -> Unit,
    onSkipFirstHabit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val translateY = remember { Animatable(16f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        launch { translateY.animateTo(0f, tween(200)) }
        launch { alpha.animateTo(1f, tween(200)) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .alpha(alpha.value)
            .graphicsLayer { translationY = translateY.value.dp.toPx() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isLoading = when (currentStep) {
            1 -> state.isApplyingPreset
            2 -> state.isCreatingHabit
            else -> false
        }

        val isEnabled = when (currentStep) {
            2 -> state.habitName.isNotBlank() &&
                    (state.habitType == HabitType.BINARY || state.targetValue.isNotBlank())
            else -> true
        }

        val ctaLabel = when (currentStep) {
            2 -> "Create habit"
            else -> "Continue"
        }

        val onCtaClick: () -> Unit = when (currentStep) {
            0 -> onAdvance
            1 -> onContinueFromStrictness
            2 -> onCreateHabit
            else -> onAdvance
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(36.dp))
        } else {
            Button(
                onClick = onCtaClick,
                enabled = isEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(ctaLabel)
            }
        }

        if (currentStep == 2) {
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(
                onClick = onSkipFirstHabit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Skip for now",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

- [ ] **Step 2: Compile check**

```bash
./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -20
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingCta.kt
git commit -m "feat(onboarding): add OnboardingCta with step-aware dispatch and loading state"
```

---

## Task 9: OnboardingWizard

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingWizard.kt`

Assembles top chrome, `AnimatedContent` for step composables, and CTA. Owns `BackHandler` for intra-wizard navigation. Pure UI — no event collection.

- [ ] **Step 1: Create `OnboardingWizard.kt`**

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private const val ENTER_DURATION_MS = 300
private const val EXIT_DURATION_MS = 200

// M3 motion easing (spec §7)
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

@Composable
fun OnboardingWizard(
    state: OnboardingState,
    currentStep: Int,
    snackbarHostState: SnackbarHostState,
    onStepChange: (Int) -> Unit,
    onSkip: () -> Unit,
    onContinueFromStrictness: () -> Unit,
    onCreateHabit: () -> Unit,
    onSkipFirstHabit: () -> Unit,
    onPresetSelected: (com.ricardocosteira.habitlock.domain.models.StrictnessPreset) -> Unit,
    onHabitNameChange: (String) -> Unit,
    onHabitTypeChange: (com.ricardocosteira.habitlock.domain.models.HabitType) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = currentStep > 0) {
        onStepChange(currentStep - 1)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OnboardingTopChrome(
                currentStep = currentStep,
                onSkip = onSkip,
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val isForward = targetState > initialState
                    val enterSlide = slideInHorizontally(
                        tween(ENTER_DURATION_MS, easing = EmphasizedDecelerate)
                    ) { if (isForward) it else -it } + fadeIn(tween(ENTER_DURATION_MS))
                    val exitSlide = slideOutHorizontally(
                        tween(EXIT_DURATION_MS, easing = EmphasizedAccelerate)
                    ) { if (isForward) -it else it } + fadeOut(tween(EXIT_DURATION_MS))
                    enterSlide togetherWith exitSlide
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    0 -> PhilosophyStep(modifier = Modifier.fillMaxSize())
                    1 -> StrictnessStep(
                        selectedPreset = state.selectedPreset,
                        onPresetSelected = onPresetSelected,
                        modifier = Modifier.fillMaxSize()
                    )
                    2 -> FirstHabitStep(
                        habitName = state.habitName,
                        habitType = state.habitType,
                        targetValue = state.targetValue,
                        unit = state.unit,
                        onHabitNameChange = onHabitNameChange,
                        onHabitTypeChange = onHabitTypeChange,
                        onTargetValueChange = onTargetValueChange,
                        onUnitChange = onUnitChange,
                        modifier = Modifier.fillMaxSize()
                    )
                    else -> Unit
                }
            }

            OnboardingCta(
                currentStep = currentStep,
                state = state,
                onAdvance = { onStepChange(currentStep + 1) },
                onContinueFromStrictness = onContinueFromStrictness,
                onCreateHabit = onCreateHabit,
                onSkipFirstHabit = onSkipFirstHabit,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

- [ ] **Step 2: Compile check**

```bash
./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -20
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingWizard.kt
git commit -m "feat(onboarding): add OnboardingWizard with AnimatedContent step transitions"
```

---

## Task 10: OnboardingRoute and final navigation wiring

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingRoute.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/HabitLockNavigation.kt`

`OnboardingRoute` owns event collection and step state. `HabitLockNavigation` replaces the stub entry with the real route.

- [ ] **Step 1: Create `OnboardingRoute.kt`**

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OnboardingRoute(
    viewModel: OnboardingViewModel,
    snackbarHostState: SnackbarHostState,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var currentStep by remember { mutableIntStateOf(0) }

    // Collect events:
    //   NavigateToFirstHabit → advance to step 2
    //   NavigateToToday      → call onFinished
    //   ShowError            → show snackbar
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.NavigateToFirstHabit -> currentStep = 2
                OnboardingEvent.NavigateToToday -> onFinished()
                is OnboardingEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    OnboardingWizard(
        state = state,
        currentStep = currentStep,
        snackbarHostState = snackbarHostState,
        onStepChange = { currentStep = it },
        onSkip = viewModel::skipToToday,
        onContinueFromStrictness = viewModel::continueFromStrictness,
        onCreateHabit = viewModel::createFirstHabit,
        onSkipFirstHabit = viewModel::skipFirstHabit,
        onPresetSelected = viewModel::selectPreset,
        onHabitNameChange = viewModel::updateHabitName,
        onHabitTypeChange = viewModel::updateHabitType,
        onTargetValueChange = viewModel::updateTargetValue,
        onUnitChange = viewModel::updateUnit,
        modifier = modifier
    )
}
```

- [ ] **Step 2: Wire `OnboardingRoute` into `HabitLockNavigation`**

Re-add `onboardingViewModel: OnboardingViewModel` to `HabitLockNavigation`'s parameter list.

`snackbarHostState` already exists in `HabitLockNavigation` (it is declared at the top of the composable and shared across routes). Pass it into `OnboardingRoute` as-is — no new declaration needed.

Replace the stub `entry<Onboarding>` block with:

```kotlin
entry<Onboarding> {
    OnboardingRoute(
        viewModel = onboardingViewModel,
        snackbarHostState = snackbarHostState,
        onFinished = {
            backStack.clear()
            backStack.add(Today)
        }
    )
}
```

Add the necessary imports: `OnboardingRoute`, `Onboarding`.

- [ ] **Step 3: Compile check**

```bash
./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -20
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Run tests to confirm nothing is broken**

```bash
./gradlew :composeApp:jvmTest 2>&1 | tail -30
```
Expected: `BUILD SUCCESSFUL` with all existing tests passing.

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingRoute.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/navigation/HabitLockNavigation.kt
git commit -m "feat(onboarding): wire OnboardingRoute into nav host"
```

---

## Task 11: Delete old screen files

**Files:**
- Delete: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/PhilosophyScreen.kt`
- Delete: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/StrictnessScreen.kt`
- Delete: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/FirstHabitScreen.kt`

- [ ] **Step 1: Delete the old screen files**

```bash
git rm composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/PhilosophyScreen.kt \
       composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/StrictnessScreen.kt \
       composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/FirstHabitScreen.kt
```

- [ ] **Step 2: Compile check**

```bash
./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -20
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Run full test suite**

```bash
./gradlew :composeApp:jvmTest 2>&1 | tail -30
```
Expected: all existing tests pass.

- [ ] **Step 4: Commit**

```bash
git commit -m "refactor(onboarding): delete old PhilosophyScreen, StrictnessScreen, FirstHabitScreen"
```

---

## Task 12: Reduced motion support

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingWizard.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/PhilosophyStep.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/StrictnessStep.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingCta.kt`

Compose Multiplatform does not yet expose `LocalReducedMotion` in commonMain. Use the equivalent available in `androidx.compose.ui.platform` on Android: check `LocalConfiguration` or wrap in `expect/actual`. For now, add a `reduceMotion: Boolean` parameter to the composables that animate, defaulting to `false`, so it can be wired up per-platform when needed.

- [ ] **Step 1: Add `reduceMotion` parameter to `OnboardingWizard`**

Add `reduceMotion: Boolean = false` to `OnboardingWizard`. Add `import androidx.compose.animation.Crossfade`. When `true`, use `Crossfade` in place of `AnimatedContent` for step transitions (spec §7 — "step transitions fall back to `Crossfade`"):

```kotlin
if (reduceMotion) {
    Crossfade(
        targetState = currentStep,
        modifier = Modifier.fillMaxWidth().weight(1f),
        label = "onboarding_step"
    ) { step ->
        when (step) {
            0 -> PhilosophyStep(reduceMotion = true, modifier = Modifier.fillMaxSize())
            1 -> StrictnessStep(
                selectedPreset = state.selectedPreset,
                onPresetSelected = onPresetSelected,
                reduceMotion = true,
                modifier = Modifier.fillMaxSize()
            )
            2 -> FirstHabitStep(...)
            else -> Unit
        }
    }
} else {
    AnimatedContent(...) { step -> ... }
}
```

- [ ] **Step 2: Add `reduceMotion` parameter to `PhilosophyStep`**

Add `reduceMotion: Boolean = false`. When `true`, skip `LaunchedEffect` animations — set all `Animatable` targets to their final values immediately:

```kotlin
LaunchedEffect(Unit) {
    if (reduceMotion) {
        headlineAlpha.snapTo(1f)
        headlineTranslateY.snapTo(0f)
        accentWidth.snapTo(1f)
        bodyAlpha.snapTo(1f)
        lockAlpha.snapTo(0.45f)
        return@LaunchedEffect
    }
    // existing stagger logic
}
```

- [ ] **Step 3: Add `reduceMotion` parameter to `StrictnessStep`**

Add `reduceMotion: Boolean = false` to `StrictnessStep`. Pass it into `PresetCard`. In `PresetCard`, change the `AnimatedVisibility` for rule expansion to use `EnterTransition.None` / `ExitTransition.None` when reduced motion is on (spec §7):

```kotlin
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition

AnimatedVisibility(
    visible = isSelected,
    enter = if (reduceMotion) EnterTransition.None else expandVertically(tween(250)) + fadeIn(tween(250)),
    exit = if (reduceMotion) ExitTransition.None else shrinkVertically(tween(200)) + fadeOut(tween(200))
) {
    // rules column
}
```

- [ ] **Step 4: Add `reduceMotion` parameter to `OnboardingCta`**

Same pattern — `snapTo` final values immediately when `reduceMotion = true`.

- [ ] **Step 5: Pass `reduceMotion` down from `OnboardingWizard` through to step composables**

- [ ] **Step 6: Compile check**

```bash
./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -20
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingWizard.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/PhilosophyStep.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/StrictnessStep.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingCta.kt
git commit -m "feat(onboarding): add reduceMotion support to wizard animations"
```

---

## Done

At this point the onboarding revamp is complete. Verify end-to-end on device:
1. Fresh install (or clear app data) — lands on Philosophy step
2. Continue through all 3 steps, check animations and dot advancement
3. Test back navigation from step 2 → 1 → no-op on step 0
4. Test Skip from each step
5. Test Strictness accordion — select each preset, confirm rules expand/collapse
6. Test First Habit with Yes/No and Quantitative types
7. Verify dark mode on a device or emulator with dark theme enabled
