# Notification Permission Onboarding Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a conditional notification permission step to the onboarding wizard (between Strictness and First Habit) that explains what notifications Rite sends and requests the Android 13+ `POST_NOTIFICATIONS` permission.

**Architecture:** New composable step (`NotificationPermissionStep`) inserted into the existing `OnboardingWizard` at index 2 (conditional on API 33+). Permission request uses Compose's `rememberLauncherForActivityResult` in an Android-only wrapper, exposed to common code via an `expect/actual` composable. The existing cold permission request in `MainActivity` is removed.

**Tech Stack:** Kotlin, Compose Multiplatform, Material 3, Compose Resources, Roborazzi

---

## File Structure

| Action | File | Responsibility |
|--------|------|----------------|
| Create | `composeApp/src/commonMain/composeResources/values/strings_onboarding_notifications.xml` | String resources for the notification permission step |
| Create | `composeApp/src/commonMain/kotlin/.../onboarding/NotificationPermissionStep.kt` | Step composable: headline, accent line, body, three notification preview cards |
| Create | `composeApp/src/commonMain/kotlin/.../onboarding/NotificationPermissionStepCta.kt` | CTA area: "Enable notifications" button + "Maybe later" text button |
| Create | `composeApp/src/commonMain/kotlin/.../onboarding/NotificationPermissionRequester.kt` | `expect` composable that provides `requestPermission: () -> Unit` to common code |
| Create | `composeApp/src/androidMain/kotlin/.../onboarding/NotificationPermissionRequester.android.kt` | `actual` using `rememberLauncherForActivityResult` for `POST_NOTIFICATIONS` |
| Create | `composeApp/src/desktopMain/kotlin/.../onboarding/NotificationPermissionRequester.desktop.kt` | `actual` no-op (desktop has no runtime notification permission) |
| Create | `composeApp/src/iosMain/kotlin/.../onboarding/NotificationPermissionRequester.ios.kt` | `actual` no-op stub |
| Modify | `composeApp/src/commonMain/kotlin/.../onboarding/OnboardingWizard.kt` | Add step 2 (notification permission) and shift First Habit to step 3 |
| Modify | `composeApp/src/commonMain/kotlin/.../onboarding/OnboardingTopChrome.kt` | Make `TOTAL_STEPS` dynamic based on whether notification step is shown |
| Modify | `composeApp/src/commonMain/kotlin/.../onboarding/OnboardingCta.kt` | Add `NotificationPermissionStepCta` composable |
| Modify | `composeApp/src/commonMain/kotlin/.../onboarding/OnboardingRoute.kt` | Thread permission requester and new callbacks |
| Modify | `composeApp/src/commonMain/kotlin/.../onboarding/OnboardingViewModel.kt` | Add `continueFromNotificationPermission()`, handle dynamic step count |
| Modify | `composeApp/src/commonMain/kotlin/.../onboarding/OnboardingState.kt` | Add `showNotificationStep: Boolean` to state |
| Modify | `composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/MainActivity.kt` | Remove `requestNotificationPermissionIfNeeded()` and `requestPermissionLauncher` |
| Create | `composeApp/src/androidUnitTest/kotlin/.../onboarding/NotificationPermissionStepScreenshotTest.kt` | Screenshot tests for light + dark themes |
| Modify | `composeApp/src/commonTest/kotlin/.../onboarding/OnboardingViewModelTest.kt` | Test dynamic step indexing and navigation |

**Note:** All `...` paths above expand to `com/ricardocosteira/rite/presentation/ui`.

---

### Task 1: Add String Resources

**Files:**
- Create: `composeApp/src/commonMain/composeResources/values/strings_onboarding_notifications.xml`

- [ ] **Step 1: Create the string resource file**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="notifications_heading">With you\nevery step.</string>
    <string name="notifications_body">Rite uses notifications to keep you on track — reminders when it\'s time, and warnings before a habit is marked as failed.</string>
    <string name="notifications_card_reminders_title">Habit reminders</string>
    <string name="notifications_card_reminders_subtitle">A nudge when it\'s time to show up</string>
    <string name="notifications_card_warnings_title">Deadline warnings</string>
    <string name="notifications_card_warnings_subtitle">A last chance before it\'s marked as failed</string>
    <string name="notifications_card_tracking_title">Progress tracking</string>
    <string name="notifications_card_tracking_subtitle">Your day\'s progress from the notification shade</string>
    <string name="notifications_cta_enable">Enable notifications</string>
    <string name="notifications_cta_later">Maybe later</string>
</resources>
```

- [ ] **Step 2: Build to verify resources compile**

Run: `./gradlew :composeApp:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/composeResources/values/strings_onboarding_notifications.xml
git commit -m "feat(onboarding): add string resources for notification permission step"
```

---

### Task 2: Create the NotificationPermissionStep Composable

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionStep.kt`

- [ ] **Step 1: Create the composable**

```kotlin
package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.notifications_body
import rite.composeapp.generated.resources.notifications_card_reminders_subtitle
import rite.composeapp.generated.resources.notifications_card_reminders_title
import rite.composeapp.generated.resources.notifications_card_tracking_subtitle
import rite.composeapp.generated.resources.notifications_card_tracking_title
import rite.composeapp.generated.resources.notifications_card_warnings_subtitle
import rite.composeapp.generated.resources.notifications_card_warnings_title
import rite.composeapp.generated.resources.notifications_heading

@Composable
fun NotificationPermissionStep(modifier: Modifier = Modifier, reduceMotion: Boolean = false) {
    val headlineAlpha = remember { Animatable(0f) }
    val headlineTranslateY = remember { Animatable(12f) }
    val accentWidth = remember { Animatable(0f) }
    val bodyAlpha = remember { Animatable(0f) }
    val cardsAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (reduceMotion) {
            headlineAlpha.snapTo(1f)
            headlineTranslateY.snapTo(0f)
            accentWidth.snapTo(1f)
            bodyAlpha.snapTo(1f)
            cardsAlpha.snapTo(1f)
            return@LaunchedEffect
        }
        launch { headlineAlpha.animateTo(1f, tween(200)) }
        launch { headlineTranslateY.animateTo(0f, tween(200)) }
        delay(80)
        launch { accentWidth.animateTo(1f, tween(250)) }
        delay(60)
        launch { bodyAlpha.animateTo(1f, tween(200)) }
        delay(100)
        launch { cardsAlpha.animateTo(1f, tween(300)) }
    }

    Box(modifier = modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        // Decorative background ring — same as PhilosophyStep
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 120.dp, y = (-160).dp)
                .requiredSize(420.dp)
                .graphicsLayer { rotationZ = 12f }
                .border(
                    width = 40.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = CircleShape
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.notifications_heading),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .alpha(headlineAlpha.value)
                    .graphicsLayer { translationY = headlineTranslateY.value.dp.toPx() }
                    .semantics { heading() }
            )

            Spacer(modifier = Modifier.height(14.dp))

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
                text = stringResource(Res.string.notifications_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(bodyAlpha.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.alpha(cardsAlpha.value)
            ) {
                NotificationPreviewCard(
                    icon = Icons.Outlined.NotificationsActive,
                    title = stringResource(Res.string.notifications_card_reminders_title),
                    subtitle = stringResource(Res.string.notifications_card_reminders_subtitle)
                )
                Spacer(modifier = Modifier.height(10.dp))
                NotificationPreviewCard(
                    icon = Icons.Outlined.Warning,
                    title = stringResource(Res.string.notifications_card_warnings_title),
                    subtitle = stringResource(Res.string.notifications_card_warnings_subtitle)
                )
                Spacer(modifier = Modifier.height(10.dp))
                NotificationPreviewCard(
                    icon = Icons.Outlined.ShowChart,
                    title = stringResource(Res.string.notifications_card_tracking_title),
                    subtitle = stringResource(Res.string.notifications_card_tracking_subtitle)
                )
            }
        }
    }
}

@Composable
private fun NotificationPreviewCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

- [ ] **Step 2: Build to verify it compiles**

Run: `./gradlew :composeApp:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionStep.kt
git commit -m "feat(onboarding): add NotificationPermissionStep composable"
```

---

### Task 3: Create the expect/actual Permission Requester

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionRequester.kt`
- Create: `composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionRequester.android.kt`
- Create: `composeApp/src/desktopMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionRequester.desktop.kt`
- Create: `composeApp/src/iosMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionRequester.ios.kt`

- [ ] **Step 1: Create the expect declaration in commonMain**

```kotlin
package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.runtime.Composable

/**
 * Returns whether the notification permission step should be shown on this platform/API level,
 * and a lambda to request the permission.
 *
 * On Android 13+ (API 33+), [shouldShow] is true and [requestPermission] launches the
 * system POST_NOTIFICATIONS permission dialog. On all other platforms and older Android versions,
 * [shouldShow] is false.
 */
data class NotificationPermissionState(
    val shouldShow: Boolean,
    val requestPermission: (onResult: (Boolean) -> Unit) -> Unit
)

@Composable
expect fun rememberNotificationPermissionState(): NotificationPermissionState
```

- [ ] **Step 2: Create the Android actual**

```kotlin
package com.ricardocosteira.rite.presentation.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
actual fun rememberNotificationPermissionState(): NotificationPermissionState {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return NotificationPermissionState(
            shouldShow = false,
            requestPermission = { onResult -> onResult(true) }
        )
    }

    val callbackRef = remember { mutableStateOf<((Boolean) -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        callbackRef.value?.invoke(isGranted)
        callbackRef.value = null
    }

    return remember {
        NotificationPermissionState(
            shouldShow = true,
            requestPermission = { onResult ->
                callbackRef.value = onResult
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        )
    }
}
```

- [ ] **Step 3: Create the desktop actual (no-op)**

```kotlin
package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.runtime.Composable

@Composable
actual fun rememberNotificationPermissionState(): NotificationPermissionState {
    return NotificationPermissionState(
        shouldShow = false,
        requestPermission = { onResult -> onResult(true) }
    )
}
```

- [ ] **Step 4: Create the iOS actual (no-op stub)**

```kotlin
package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.runtime.Composable

@Composable
actual fun rememberNotificationPermissionState(): NotificationPermissionState {
    return NotificationPermissionState(
        shouldShow = false,
        requestPermission = { onResult -> onResult(true) }
    )
}
```

- [ ] **Step 5: Build to verify all actuals compile**

Run: `./gradlew :composeApp:compileKotlinDesktop :composeApp:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionRequester.kt \
      composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionRequester.android.kt \
      composeApp/src/desktopMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionRequester.desktop.kt \
      composeApp/src/iosMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionRequester.ios.kt
git commit -m "feat(onboarding): add expect/actual NotificationPermissionRequester"
```

---

### Task 4: Add NotificationPermissionStepCta

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingCta.kt`

- [ ] **Step 1: Add the new CTA composable**

Add the following composable at the end of `OnboardingCta.kt`, after `FirstHabitStepCta`:

```kotlin
@Composable
internal fun NotificationPermissionStepCta(
    onEnableNotifications: () -> Unit,
    onMaybeLater: () -> Unit,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    CtaContainer(modifier = modifier, reduceMotion = reduceMotion) {
        PrimaryButton(onClick = onEnableNotifications) {
            Text(stringResource(Res.string.notifications_cta_enable))
        }

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(onClick = onMaybeLater, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(Res.string.notifications_cta_later),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

Add these imports to the existing import block:

```kotlin
import rite.composeapp.generated.resources.notifications_cta_enable
import rite.composeapp.generated.resources.notifications_cta_later
```

- [ ] **Step 2: Build to verify it compiles**

Run: `./gradlew :composeApp:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingCta.kt
git commit -m "feat(onboarding): add NotificationPermissionStepCta"
```

---

### Task 5: Update OnboardingState and ViewModel for Dynamic Step Count

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingState.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingViewModel.kt`

- [ ] **Step 1: Add `showNotificationStep` to OnboardingState**

In `OnboardingState.kt`, add the field:

```kotlin
data class OnboardingState(
    val selectedPreset: OnboardingStrictnessPreset = OnboardingStrictnessPreset.BALANCED,
    val habitName: String = "",
    val habitType: HabitType = HabitType.BINARY,
    val targetValue: String = "",
    val unit: String = "",
    val selectedDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
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

- [ ] **Step 2: Add `setShowNotificationStep` to OnboardingViewModel**

Add this method to `OnboardingViewModel`:

```kotlin
fun setShowNotificationStep(show: Boolean) {
    _state.update { it.copy(showNotificationStep = show) }
}
```

- [ ] **Step 3: Update `continueFromStrictness` to advance to correct step**

Replace the `currentStep = 2` in `continueFromStrictness` with dynamic index:

```kotlin
fun continueFromStrictness() {
    viewModelScope.launch {
        _state.update { it.copy(isApplyingPreset = true) }

        val result = applyStrictnessPreset.execute(_state.value.selectedPreset.toDomain())

        result.fold(
            onSuccess = {
                _state.update {
                    val nextStep: Int = if (it.showNotificationStep) {
                        it.notificationStepIndex
                    } else {
                        it.firstHabitStepIndex
                    }
                    it.copy(isApplyingPreset = false, currentStep = nextStep)
                }
            },
            onFailure = { error ->
                _state.update { it.copy(isApplyingPreset = false, error = error.message) }
            }
        )
    }
}
```

- [ ] **Step 4: Add `continueFromNotificationPermission` method**

Add this method to `OnboardingViewModel`:

```kotlin
fun continueFromNotificationPermission() {
    _state.update { it.copy(currentStep = it.firstHabitStepIndex) }
}
```

- [ ] **Step 5: Build to verify it compiles**

Run: `./gradlew :composeApp:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingState.kt \
      composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingViewModel.kt
git commit -m "feat(onboarding): add dynamic step count for notification permission"
```

---

### Task 6: Wire Notification Step into OnboardingWizard

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingWizard.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingTopChrome.kt`

- [ ] **Step 1: Update OnboardingTopChrome to accept dynamic total steps**

Change `OnboardingTopChrome` to accept `totalSteps` as a parameter instead of using the hardcoded constant. In `OnboardingTopChrome.kt`:

Replace the `TOTAL_STEPS` constant and the function signature:

```kotlin
// Remove: private const val TOTAL_STEPS = 3

@Composable
fun OnboardingTopChrome(
    currentStep: Int,
    totalSteps: Int,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stepDescription =
        stringResource(Res.string.common_cd_onboarding_step, currentStep + 1, totalSteps)

    Row(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ProgressDots(
            currentStep = currentStep,
            totalSteps = totalSteps,
            modifier = Modifier
                .weight(1f)
                .semantics {
                    contentDescription = stepDescription
                }
        )

        TextButton(onClick = onSkip) {
            Text(
                text = stringResource(Res.string.common_skip),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProgressDots(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
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
```

- [ ] **Step 2: Update OnboardingWizard signature and add notification step**

Add new parameters to `OnboardingWizard`:

```kotlin
@Composable
fun OnboardingWizard(
    state: OnboardingState,
    currentStep: Int,
    snackbarHostState: SnackbarHostState,
    onStepChange: (Int) -> Unit,
    onSkip: () -> Unit,
    onContinueFromStrictness: () -> Unit,
    onContinueFromNotificationPermission: () -> Unit,
    onEnableNotifications: () -> Unit,
    onCreateHabit: () -> Unit,
    onSkipFirstHabit: () -> Unit,
    onPresetSelected: (OnboardingStrictnessPreset) -> Unit,
    onHabitNameChange: (String) -> Unit,
    onHabitTypeChange: (HabitType) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onSelectedDaysChange: (Set<DayOfWeek>) -> Unit,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
)
```

Update the `OnboardingTopChrome` call:

```kotlin
OnboardingTopChrome(
    currentStep = currentStep,
    totalSteps = state.totalSteps,
    onSkip = onSkip,
    modifier = Modifier.fillMaxWidth()
)
```

Add the notification permission step in the `when` block. Replace `2 ->` (First Habit) with the notification step and move First Habit to the dynamic index. The `when` block should become (shown for the animated branch — apply the same to the `reduceMotion` branch):

```kotlin
when (step) {
    0 -> {
        PhilosophyStep(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reduceMotion = reduceMotion
        )
        PhilosophyStepCta(
            onAdvance = { onStepChange(step + 1) },
            modifier = Modifier.fillMaxWidth(),
            reduceMotion = reduceMotion
        )
    }

    1 -> {
        StrictnessStep(
            selectedPreset = state.selectedPreset,
            onPresetSelected = onPresetSelected,
            reduceMotion = reduceMotion,
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
        StrictnessStepCta(
            state = state,
            onContinue = onContinueFromStrictness,
            modifier = Modifier.fillMaxWidth(),
            reduceMotion = reduceMotion
        )
    }

    state.notificationStepIndex -> if (state.showNotificationStep) {
        NotificationPermissionStep(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reduceMotion = reduceMotion
        )
        NotificationPermissionStepCta(
            onEnableNotifications = onEnableNotifications,
            onMaybeLater = onContinueFromNotificationPermission,
            modifier = Modifier.fillMaxWidth(),
            reduceMotion = reduceMotion
        )
    }

    state.firstHabitStepIndex -> {
        FirstHabitStep(
            habitName = state.habitName,
            habitType = state.habitType,
            targetValue = state.targetValue,
            unit = state.unit,
            selectedDays = state.selectedDays,
            onHabitNameChange = onHabitNameChange,
            onHabitTypeChange = onHabitTypeChange,
            onTargetValueChange = onTargetValueChange,
            onUnitChange = onUnitChange,
            onSelectedDaysChange = onSelectedDaysChange,
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
        FirstHabitStepCta(
            state = state,
            onCreateHabit = onCreateHabit,
            onSkip = onSkipFirstHabit,
            modifier = Modifier.fillMaxWidth(),
            reduceMotion = reduceMotion
        )
    }
}
```

Apply the exact same `when` block structure to both the `reduceMotion` branch (Crossfade) and the animated branch (AnimatedContent).

- [ ] **Step 3: Build to verify it compiles**

Run: `./gradlew :composeApp:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL (will have call-site errors in OnboardingRoute — fixed in next task)

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingWizard.kt \
      composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingTopChrome.kt
git commit -m "feat(onboarding): wire notification permission step into wizard"
```

---

### Task 7: Update OnboardingRoute to Thread Permission Requester

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingRoute.kt`

- [ ] **Step 1: Wire the permission requester and new callbacks**

Replace the full content of `OnboardingRoute.kt`:

```kotlin
package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.rite.presentation.ui.isReduceMotionEnabled
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.first_habit_error_empty_name
import rite.composeapp.generated.resources.first_habit_error_invalid_target_value
import rite.composeapp.generated.resources.first_habit_error_missing_target_value

@Composable
fun OnboardingRoute(
    viewModel: OnboardingViewModel,
    snackbarHostState: SnackbarHostState,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val reduceMotion = isReduceMotionEnabled()
    val permissionState = rememberNotificationPermissionState()

    LaunchedEffect(permissionState.shouldShow) {
        viewModel.setShowNotificationStep(permissionState.shouldShow)
    }

    val messageEmptyName = stringResource(Res.string.first_habit_error_empty_name)
    val messageMissingTarget = stringResource(Res.string.first_habit_error_missing_target_value)
    val messageInvalidTarget = stringResource(Res.string.first_habit_error_invalid_target_value)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.NavigateToToday -> onFinished()

                OnboardingEvent.EmptyHabitName -> snackbarHostState.showSnackbar(messageEmptyName)

                OnboardingEvent.MissingTargetValue ->
                    snackbarHostState.showSnackbar(messageMissingTarget)

                OnboardingEvent.InvalidTargetValue ->
                    snackbarHostState.showSnackbar(messageInvalidTarget)
            }
        }
    }

    OnboardingWizard(
        state = state,
        currentStep = state.currentStep,
        snackbarHostState = snackbarHostState,
        onStepChange = viewModel::setCurrentStep,
        onSkip = viewModel::skipToToday,
        reduceMotion = reduceMotion,
        onContinueFromStrictness = viewModel::continueFromStrictness,
        onContinueFromNotificationPermission = viewModel::continueFromNotificationPermission,
        onEnableNotifications = {
            permissionState.requestPermission { _ ->
                viewModel.continueFromNotificationPermission()
            }
        },
        onCreateHabit = viewModel::createFirstHabit,
        onSkipFirstHabit = viewModel::skipFirstHabit,
        onPresetSelected = viewModel::selectPreset,
        onHabitNameChange = viewModel::updateHabitName,
        onHabitTypeChange = viewModel::updateHabitType,
        onTargetValueChange = viewModel::updateTargetValue,
        onUnitChange = viewModel::updateUnit,
        onSelectedDaysChange = viewModel::updateSelectedDays,
        modifier = modifier
    )
}
```

- [ ] **Step 2: Build to verify everything compiles**

Run: `./gradlew :composeApp:compileKotlinDesktop :composeApp:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingRoute.kt
git commit -m "feat(onboarding): wire permission requester into OnboardingRoute"
```

---

### Task 8: Remove Cold Permission Request from MainActivity

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/MainActivity.kt`

- [ ] **Step 1: Remove the permission request code**

Replace the full content of `MainActivity.kt`:

```kotlin
package com.ricardocosteira.rite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ricardocosteira.rite.presentation.ui.startup.StartupState
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val appComponent = application.riteApplication.appComponent

        installSplashScreen().setKeepOnScreenCondition {
            appComponent.startupViewModel.state.value is StartupState.Loading
        }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(appComponent = appComponent)
        }
    }
}

actual fun generateUuid(): String = UUID.randomUUID().toString()
```

- [ ] **Step 2: Build and run to verify**

Run: `./gradlew :composeApp:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/MainActivity.kt
git commit -m "refactor: remove cold notification permission request from MainActivity"
```

---

### Task 9: Add Screenshot Tests

**Files:**
- Create: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionStepScreenshotTest.kt`

- [ ] **Step 1: Create the screenshot test**

```kotlin
package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w360dp-h800dp-420dpi", application = android.app.Application::class)
class NotificationPermissionStepScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun notificationPermissionStep_lightTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                NotificationPermissionStep(reduceMotion = true)
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun notificationPermissionStep_darkTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = true) {
                NotificationPermissionStep(reduceMotion = true)
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
```

- [ ] **Step 2: Record the golden screenshots**

Run: `./gradlew :composeApp:recordRoborazziDebug`
Expected: BUILD SUCCESSFUL, two new PNG files in `composeApp/src/androidUnitTest/snapshots/images/`

- [ ] **Step 3: Verify the screenshots pass**

Run: `./gradlew :composeApp:verifyRoborazziDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/NotificationPermissionStepScreenshotTest.kt \
      composeApp/src/androidUnitTest/snapshots/images/
git commit -m "test(onboarding): add screenshot tests for notification permission step"
```

---

### Task 10: Add ViewModel Unit Tests

**Files:**
- Modify or create: `composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingViewModelTest.kt`

- [ ] **Step 1: Check if the test file exists and read it**

Run: `find composeApp/src/commonTest -name "OnboardingViewModelTest.kt" 2>/dev/null`

If it doesn't exist, create it. If it does, add tests to it.

- [ ] **Step 2: Write tests for dynamic step navigation**

The following tests should be added (adapt to existing test infrastructure if the file exists):

```kotlin
package com.ricardocosteira.rite.presentation.ui.onboarding

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OnboardingViewModelTest {
    // Use your existing test setup/fakes for dependencies

    @Test
    fun `given showNotificationStep is false, totalSteps returns 3`() {
        // Given
        val state = OnboardingState(showNotificationStep = false)

        // Then
        assertEquals(3, state.totalSteps)
    }

    @Test
    fun `given showNotificationStep is true, totalSteps returns 4`() {
        // Given
        val state = OnboardingState(showNotificationStep = true)

        // Then
        assertEquals(4, state.totalSteps)
    }

    @Test
    fun `given showNotificationStep is false, firstHabitStepIndex returns 2`() {
        // Given
        val state = OnboardingState(showNotificationStep = false)

        // Then
        assertEquals(2, state.firstHabitStepIndex)
    }

    @Test
    fun `given showNotificationStep is true, firstHabitStepIndex returns 3`() {
        // Given
        val state = OnboardingState(showNotificationStep = true)

        // Then
        assertEquals(3, state.firstHabitStepIndex)
    }

    @Test
    fun `given showNotificationStep is true, notificationStepIndex returns 2`() {
        // Given
        val state = OnboardingState(showNotificationStep = true)

        // Then
        assertEquals(2, state.notificationStepIndex)
    }
}
```

- [ ] **Step 3: Run the tests**

Run: `./gradlew :composeApp:desktopTest --tests "*OnboardingViewModelTest*"`
Expected: All tests PASS

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonTest/
git commit -m "test(onboarding): add unit tests for dynamic step indexing"
```

---

### Task 11: Update FOLLOWUP.md

**Files:**
- Modify: `FOLLOWUP.md`

- [ ] **Step 1: Mark notification permission onboarding as done**

Change:
```
- [ ] Notification permission onboarding screen
```
to:
```
- [x] Notification permission onboarding screen
```

- [ ] **Step 2: Commit**

```bash
git add FOLLOWUP.md
git commit -m "docs: mark notification permission onboarding as done"
```
