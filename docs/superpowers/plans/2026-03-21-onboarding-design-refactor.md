# Onboarding Design Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the three onboarding wizard steps to match new designs — changing the Philosophy CTA text and rebuilding the First Habit screen with type cards, expandable quantitative fields, and a schedule section.

**Architecture:** Changes flow from data outward: string resources → state → viewmodel → composables. `OnboardingState` gains two new fields (`scheduleOption`, `customDays`); `OnboardingViewModel` gains two new methods and passes `specificDays` to `CreateHabit`; `FirstHabitStep` is rebuilt with cards and a schedule section; `OnboardingWizard` threads the new params through.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, kotlinx-datetime (`DayOfWeek`), MockK + kotlinx-coroutines-test for ViewModel tests, CMP string resources (`composeResources/values/*.xml`).

**Spec:** `docs/superpowers/specs/2026-03-21-onboarding-design-refactor.md`

---

## File Map

```
composeApp/src/commonMain/composeResources/values/
├── strings_onboarding_philosophy.xml   MODIFY — add philosophy_cta_accept
├── strings_onboarding_strictness.xml   MODIFY — add strictness_cta_continue
└── strings_onboarding_first_habit.xml  MODIFY — update binary label, add descriptions + schedule strings

composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/
├── OnboardingState.kt      MODIFY — add ScheduleOption enum + two new fields
├── OnboardingViewModel.kt  MODIFY — add two methods, update createFirstHabit()
├── OnboardingCta.kt        MODIFY — extract hardcoded strings, update PhilosophyStepCta, update isEnabled
├── FirstHabitStep.kt       MODIFY — rebuild: type cards, expandable quantitative, schedule section
├── OnboardingWizard.kt     MODIFY — add schedule params, update both FirstHabitStep call sites
└── OnboardingRoute.kt      MODIFY — pass two new schedule callbacks to OnboardingWizard

composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/
└── OnboardingViewModelScheduleTest.kt  CREATE — tests for schedule mapping in createFirstHabit()
```

**Not touched:** `OnboardingTopChrome.kt`, `OnboardingStrictnessPreset.kt`, `PhilosophyStep.kt`, `StrictnessStep.kt`, domain layer, navigation.

---

## Task 1: String Resources

**Files:**
- Modify: `composeApp/src/commonMain/composeResources/values/strings_onboarding_philosophy.xml`
- Modify: `composeApp/src/commonMain/composeResources/values/strings_onboarding_strictness.xml`
- Modify: `composeApp/src/commonMain/composeResources/values/strings_onboarding_first_habit.xml`

- [ ] **Step 1: Add the Philosophy CTA string**

In `strings_onboarding_philosophy.xml`, append before `</resources>`:

```xml
    <string name="philosophy_cta_accept">Accept the Commitment</string>
```

- [ ] **Step 2: Add the Strictness CTA string**

In `strings_onboarding_strictness.xml`, append before `</resources>`:

```xml
    <string name="strictness_cta_continue">Continue</string>
```

- [ ] **Step 3: Update and extend strings_onboarding_first_habit.xml**

Update the existing `first_habit_type_binary` value (line 7, currently `Yes/No`):

```xml
    <string name="first_habit_type_binary">Binary</string>
```

Append new keys before `</resources>`:

```xml
    <!-- Type card descriptions -->
    <string name="first_habit_type_binary_description">Simple "Yes" or "No" completion. Perfect for streaks.</string>
    <string name="first_habit_type_quantitative">Quantitative</string>
    <string name="first_habit_type_quantitative_description">Track units like "Minutes" or "Pages". Focus on volume.</string>

    <!-- Schedule section -->
    <string name="first_habit_schedule_label">Set a schedule</string>
    <string name="first_habit_schedule_every_day">Every Day</string>
    <string name="first_habit_schedule_weekdays">Weekdays</string>
    <string name="first_habit_schedule_weekends">Weekends</string>
    <string name="first_habit_schedule_custom">Custom</string>

    <!-- Day labels for custom picker -->
    <string name="first_habit_schedule_day_mon">Mon</string>
    <string name="first_habit_schedule_day_tue">Tue</string>
    <string name="first_habit_schedule_day_wed">Wed</string>
    <string name="first_habit_schedule_day_thu">Thu</string>
    <string name="first_habit_schedule_day_fri">Fri</string>
    <string name="first_habit_schedule_day_sat">Sat</string>
    <string name="first_habit_schedule_day_sun">Sun</string>
```

- [ ] **Step 4: Verify compilation**

```bash
./gradlew :composeApp:generateComposeResClass
```

Expected: BUILD SUCCESSFUL. No `Unresolved reference` errors. The generated `Res` class will contain all new keys.

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/composeResources/values/strings_onboarding_philosophy.xml \
        composeApp/src/commonMain/composeResources/values/strings_onboarding_strictness.xml \
        composeApp/src/commonMain/composeResources/values/strings_onboarding_first_habit.xml
git commit -m "feat(resources): add onboarding CTA and first habit schedule strings"
```

---

## Task 2: OnboardingState

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingState.kt`

`OnboardingState.kt` currently contains the `OnboardingState` data class and `OnboardingEvent` sealed interface. Add `ScheduleOption` enum and two new fields to `OnboardingState`.

- [ ] **Step 1: Add ScheduleOption enum and new state fields**

Add the import for `DayOfWeek` and add `ScheduleOption` enum above `OnboardingState`. Then add two fields to `OnboardingState`.

Full updated file:

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.onboarding

import com.ricardocosteira.habitlock.domain.models.HabitType
import kotlinx.datetime.DayOfWeek

enum class ScheduleOption { EVERY_DAY, WEEKDAYS, WEEKENDS, CUSTOM }

/**
 * State for the onboarding flow.
 */
data class OnboardingState(
    val selectedPreset: OnboardingStrictnessPreset = OnboardingStrictnessPreset.BALANCED,
    val habitName: String = "",
    val habitType: HabitType = HabitType.BINARY,
    val targetValue: String = "",
    val unit: String = "",
    val scheduleOption: ScheduleOption = ScheduleOption.EVERY_DAY,
    val customDays: Set<DayOfWeek> = emptySet(),
    val isCreatingHabit: Boolean = false,
    val isApplyingPreset: Boolean = false,
    val error: String? = null
)

/**
 * Events from the onboarding flow.
 */
sealed interface OnboardingEvent {
    data object NavigateToFirstHabit : OnboardingEvent
    data object NavigateToToday : OnboardingEvent
    data object EmptyHabitName : OnboardingEvent
    data object MissingTargetValue : OnboardingEvent
    data object InvalidTargetValue : OnboardingEvent
}
```

- [ ] **Step 2: Verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingState.kt
git commit -m "feat(onboarding): add ScheduleOption enum and schedule state fields"
```

---

## Task 3: OnboardingViewModel

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingViewModel.kt`
- Create: `composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingViewModelScheduleTest.kt`

The test verifies the schedule → `specificDays` mapping in `createFirstHabit()`. Tests use MockK and `runTest`. Pattern matches the rest of the test suite in `composeApp/src/commonTest/`.

- [ ] **Step 1: Write the failing tests**

Create `composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingViewModelScheduleTest.kt`:

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.onboarding

import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.StrictnessPreset
import com.ricardocosteira.habitlock.domain.models.UndoPolicy
import com.ricardocosteira.habitlock.domain.models.User
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import com.ricardocosteira.habitlock.domain.usecases.ApplyStrictnessPreset
import com.ricardocosteira.habitlock.domain.usecases.CreateHabit
import com.ricardocosteira.habitlock.domain.usecases.GenerateDailyHabits
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone

class OnboardingViewModelScheduleTest {

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockApplyStrictnessPreset = mockk<ApplyStrictnessPreset>(relaxed = true)
    private val mockCreateHabit = mockk<CreateHabit>()
    private val mockGenerateDailyHabits = mockk<GenerateDailyHabits>(relaxed = true)

    private val mockUser = User(
        id = "user-1",
        timezone = TimeZone.UTC,
        previousTimezone = null,
        undoPolicy = UndoPolicy.TODAY_ONLY,
        maxSnoozeDurationMinutes = 30,
        maxSnoozesPerHabitPerDay = 3,
        maxConsecutiveSkips = 2,
        isOnboardingCompleted = false,
        dailySummaryTime = null,
        createdAt = Clock.System.now()
    )

    private val mockHabit = mockk<Habit>(relaxed = true)

    private fun buildViewModel(): OnboardingViewModel = OnboardingViewModel(
        userRepository = mockUserRepository,
        applyStrictnessPreset = mockApplyStrictnessPreset,
        createHabit = mockCreateHabit,
        generateDailyHabits = mockGenerateDailyHabits
    )

    private fun givenCreateHabitSucceeds() {
        coEvery { mockUserRepository.getUser() } returns mockUser
        coEvery { mockCreateHabit.execute(any(), any()) } returns Result.success(mockHabit)
    }

    @Test
    fun `given every day schedule when creating habit then specificDays is null`() = runTest {
        // Given
        givenCreateHabitSucceeds()
        val viewModel = buildViewModel()
        viewModel.updateHabitName("Run")
        viewModel.updateScheduleOption(ScheduleOption.EVERY_DAY)

        // When
        viewModel.createFirstHabit()

        // Then
        val actualParamsSlot = slot<CreateHabit.CreateHabitParams>()
        coVerify { mockCreateHabit.execute(capture(actualParamsSlot), any()) }
        assertNull(actualParamsSlot.captured.specificDays)
    }

    @Test
    fun `given weekdays schedule when creating habit then specificDays is mon to fri`() = runTest {
        // Given
        givenCreateHabitSucceeds()
        val viewModel = buildViewModel()
        viewModel.updateHabitName("Run")
        viewModel.updateScheduleOption(ScheduleOption.WEEKDAYS)

        // When
        viewModel.createFirstHabit()

        // Then
        val actualParamsSlot = slot<CreateHabit.CreateHabitParams>()
        coVerify { mockCreateHabit.execute(capture(actualParamsSlot), any()) }
        val expectedDays = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
        assertEquals(expectedDays, actualParamsSlot.captured.specificDays)
    }

    @Test
    fun `given weekends schedule when creating habit then specificDays is sat and sun`() = runTest {
        // Given
        givenCreateHabitSucceeds()
        val viewModel = buildViewModel()
        viewModel.updateHabitName("Rest")
        viewModel.updateScheduleOption(ScheduleOption.WEEKENDS)

        // When
        viewModel.createFirstHabit()

        // Then
        val actualParamsSlot = slot<CreateHabit.CreateHabitParams>()
        coVerify { mockCreateHabit.execute(capture(actualParamsSlot), any()) }
        val expectedDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        assertEquals(expectedDays, actualParamsSlot.captured.specificDays)
    }

    @Test
    fun `given custom schedule when creating habit then specificDays matches selected days`() = runTest {
        // Given
        givenCreateHabitSucceeds()
        val viewModel = buildViewModel()
        viewModel.updateHabitName("Yoga")
        viewModel.updateScheduleOption(ScheduleOption.CUSTOM)
        val inputCustomDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        viewModel.updateCustomDays(inputCustomDays)

        // When
        viewModel.createFirstHabit()

        // Then
        val actualParamsSlot = slot<CreateHabit.CreateHabitParams>()
        coVerify { mockCreateHabit.execute(capture(actualParamsSlot), any()) }
        assertEquals(inputCustomDays, actualParamsSlot.captured.specificDays)
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

```bash
./gradlew :composeApp:testDebugUnitTest --tests "*.OnboardingViewModelScheduleTest" 2>&1 | tail -20
```

Expected: FAILED — `updateScheduleOption` and `updateCustomDays` are unresolved.

- [ ] **Step 3: Update OnboardingViewModel**

In `OnboardingViewModel.kt`:

1. Add `kotlinx.datetime.DayOfWeek` import.

2. Add two new public methods after `updateUnit()`:

```kotlin
fun updateScheduleOption(option: ScheduleOption) {
    _state.update { it.copy(scheduleOption = option) }
}

fun updateCustomDays(days: Set<DayOfWeek>) {
    _state.update { it.copy(customDays = days) }
}
```

3. In `createFirstHabit()`, add the `specificDays` computation **before** the `viewModelScope.launch` block that calls `createHabit.execute()`, and pass it into `CreateHabitParams`. Find the existing `val result = createHabit.execute(...)` call and add `specificDays`:

```kotlin
val specificDays: Set<DayOfWeek>? = when (_state.value.scheduleOption) {
    ScheduleOption.EVERY_DAY -> null
    ScheduleOption.WEEKDAYS -> setOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
    )
    ScheduleOption.WEEKENDS -> setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    ScheduleOption.CUSTOM -> _state.value.customDays
}

val result = createHabit.execute(
    params = CreateHabit.CreateHabitParams(
        name = habitName,
        description = null,
        type = habitType,
        targetValue = targetValue,
        unit = unit,
        specificDays = specificDays,   // new
        reminder = null
    ),
    startDate = today
)
```

`scheduleType` is intentionally omitted — it defaults to `ScheduleType.DAILY`, which is correct for all four schedule options.

- [ ] **Step 4: Run tests — verify they pass**

```bash
./gradlew :composeApp:testDebugUnitTest --tests "*.OnboardingViewModelScheduleTest" 2>&1 | tail -20
```

Expected: 4 tests, all PASSED.

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingViewModel.kt \
        composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingViewModelScheduleTest.kt
git commit -m "feat(onboarding): add schedule option state and mapping in createFirstHabit"
```

---

## Task 4: OnboardingCta — Extract Strings and Update Philosophy CTA

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingCta.kt`

`OnboardingCta.kt` has four hardcoded strings and an `isEnabled` expression that needs a third clause. The string resources for all four already exist after Task 1.

- [ ] **Step 1: Add string resource imports and replace hardcoded strings**

Add these imports to `OnboardingCta.kt` (with the existing imports):

```kotlin
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.first_habit_button_create
import habitlock.composeapp.generated.resources.first_habit_button_skip
import habitlock.composeapp.generated.resources.philosophy_cta_accept
import habitlock.composeapp.generated.resources.strictness_cta_continue
import org.jetbrains.compose.resources.stringResource
```

In `PhilosophyStepCta`, replace:
```kotlin
Text("Continue")
```
with:
```kotlin
Text(stringResource(Res.string.philosophy_cta_accept))
```

In `StrictnessStepCta`, replace:
```kotlin
Text("Continue")
```
with:
```kotlin
Text(stringResource(Res.string.strictness_cta_continue))
```

In `FirstHabitStepCta`, replace:
```kotlin
Text("Create habit")
```
with:
```kotlin
Text(stringResource(Res.string.first_habit_button_create))
```

And replace:
```kotlin
Text(
    text = "Skip for now",
    style = MaterialTheme.typography.labelLarge,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
```
with:
```kotlin
Text(
    text = stringResource(Res.string.first_habit_button_skip),
    style = MaterialTheme.typography.labelLarge,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
```

- [ ] **Step 2: Update the isEnabled condition in FirstHabitStepCta**

`FirstHabitStepCta` reads `state: OnboardingState`, so `scheduleOption` and `customDays` are accessible directly. Replace the current `isEnabled` expression:

```kotlin
// Before
val isEnabled = state.habitName.isNotBlank() &&
        (state.habitType == HabitType.BINARY || state.targetValue.isNotBlank())

// After
val isEnabled = state.habitName.isNotBlank() &&
        (state.habitType == HabitType.BINARY || state.targetValue.isNotBlank()) &&
        (state.scheduleOption != ScheduleOption.CUSTOM || state.customDays.isNotEmpty())
```

- [ ] **Step 3: Verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingCta.kt
git commit -m "feat(onboarding): extract CTA strings to resources, update philosophy CTA text and isEnabled"
```

---

## Task 5: FirstHabitStep — Type Cards and Schedule Section

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/FirstHabitStep.kt`

This is the largest change. The existing `FirstHabitStep` uses hardcoded strings and `FilterChip`s. Replace it entirely with the new layout: string resources throughout, type cards with expandable quantitative fields, and a schedule section with an optional day-picker.

The existing composable signature had: `habitName`, `habitType`, `targetValue`, `unit`, four `on*` callbacks. Add: `scheduleOption: ScheduleOption`, `customDays: Set<DayOfWeek>`, `onScheduleOptionChange: (ScheduleOption) -> Unit`, `onCustomDaysChange: (Set<DayOfWeek>) -> Unit`.

- [ ] **Step 1: Write the full updated FirstHabitStep.kt**

```kotlin
package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.common_placeholder_habit_name
import habitlock.composeapp.generated.resources.first_habit_heading
import habitlock.composeapp.generated.resources.first_habit_label_name
import habitlock.composeapp.generated.resources.first_habit_label_target_value
import habitlock.composeapp.generated.resources.first_habit_label_unit
import habitlock.composeapp.generated.resources.first_habit_placeholder_unit
import habitlock.composeapp.generated.resources.first_habit_schedule_custom
import habitlock.composeapp.generated.resources.first_habit_schedule_day_fri
import habitlock.composeapp.generated.resources.first_habit_schedule_day_mon
import habitlock.composeapp.generated.resources.first_habit_schedule_day_sat
import habitlock.composeapp.generated.resources.first_habit_schedule_day_sun
import habitlock.composeapp.generated.resources.first_habit_schedule_day_thu
import habitlock.composeapp.generated.resources.first_habit_schedule_day_tue
import habitlock.composeapp.generated.resources.first_habit_schedule_day_wed
import habitlock.composeapp.generated.resources.first_habit_schedule_every_day
import habitlock.composeapp.generated.resources.first_habit_schedule_label
import habitlock.composeapp.generated.resources.first_habit_schedule_weekdays
import habitlock.composeapp.generated.resources.first_habit_schedule_weekends
import habitlock.composeapp.generated.resources.first_habit_subtext
import habitlock.composeapp.generated.resources.first_habit_type_binary
import habitlock.composeapp.generated.resources.first_habit_type_binary_description
import habitlock.composeapp.generated.resources.first_habit_type_quantitative
import habitlock.composeapp.generated.resources.first_habit_type_quantitative_description
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FirstHabitStep(
    habitName: String,
    habitType: HabitType,
    targetValue: String,
    unit: String,
    scheduleOption: ScheduleOption,
    customDays: Set<DayOfWeek>,
    onHabitNameChange: (String) -> Unit,
    onHabitTypeChange: (HabitType) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onScheduleOptionChange: (ScheduleOption) -> Unit,
    onCustomDaysChange: (Set<DayOfWeek>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.first_habit_heading),
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
            text = stringResource(Res.string.first_habit_subtext),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = habitName,
            onValueChange = onHabitNameChange,
            label = { Text(stringResource(Res.string.first_habit_label_name)) },
            placeholder = { Text(stringResource(Res.string.common_placeholder_habit_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Type cards
        HabitTypeCard(
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

        Spacer(modifier = Modifier.height(8.dp))

        HabitTypeCard(
            label = stringResource(Res.string.first_habit_type_quantitative),
            description = stringResource(Res.string.first_habit_type_quantitative_description),
            isSelected = habitType == HabitType.QUANTITATIVE,
            onClick = { onHabitTypeChange(HabitType.QUANTITATIVE) },
            expandedContent = {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    OutlinedTextField(
                        value = targetValue,
                        onValueChange = onTargetValueChange,
                        label = { Text(stringResource(Res.string.first_habit_label_target_value)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = unit,
                        onValueChange = onUnitChange,
                        label = { Text(stringResource(Res.string.first_habit_label_unit)) },
                        placeholder = { Text(stringResource(Res.string.first_habit_placeholder_unit)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Schedule section
        Text(
            text = stringResource(Res.string.first_habit_schedule_label).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = scheduleOption == ScheduleOption.EVERY_DAY,
                onClick = { onScheduleOptionChange(ScheduleOption.EVERY_DAY) },
                label = { Text(stringResource(Res.string.first_habit_schedule_every_day)) }
            )
            FilterChip(
                selected = scheduleOption == ScheduleOption.WEEKDAYS,
                onClick = { onScheduleOptionChange(ScheduleOption.WEEKDAYS) },
                label = { Text(stringResource(Res.string.first_habit_schedule_weekdays)) }
            )
            FilterChip(
                selected = scheduleOption == ScheduleOption.WEEKENDS,
                onClick = { onScheduleOptionChange(ScheduleOption.WEEKENDS) },
                label = { Text(stringResource(Res.string.first_habit_schedule_weekends)) }
            )
            FilterChip(
                selected = scheduleOption == ScheduleOption.CUSTOM,
                onClick = { onScheduleOptionChange(ScheduleOption.CUSTOM) },
                label = { Text(stringResource(Res.string.first_habit_schedule_custom)) }
            )
        }

        AnimatedVisibility(
            visible = scheduleOption == ScheduleOption.CUSTOM,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DayOfWeek.entries.forEach { day ->
                    val label = stringResource(dayLabel(day))
                    FilterChip(
                        selected = day in customDays,
                        onClick = {
                            val updatedDays = if (day in customDays) {
                                customDays - day
                            } else {
                                customDays + day
                            }
                            onCustomDaysChange(updatedDays)
                        },
                        label = { Text(label) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HabitTypeCard(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    expandedContent: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
    }
}

private fun dayLabel(day: DayOfWeek) = when (day) {
    DayOfWeek.MONDAY -> Res.string.first_habit_schedule_day_mon
    DayOfWeek.TUESDAY -> Res.string.first_habit_schedule_day_tue
    DayOfWeek.WEDNESDAY -> Res.string.first_habit_schedule_day_wed
    DayOfWeek.THURSDAY -> Res.string.first_habit_schedule_day_thu
    DayOfWeek.FRIDAY -> Res.string.first_habit_schedule_day_fri
    DayOfWeek.SATURDAY -> Res.string.first_habit_schedule_day_sat
    DayOfWeek.SUNDAY -> Res.string.first_habit_schedule_day_sun
    else -> Res.string.first_habit_schedule_day_mon
}
```

- [ ] **Step 2: Verify compilation**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

Expected: BUILD SUCCESSFUL. If `common_placeholder_habit_name` is unresolved, check the key name in `strings_common.xml` and correct the import.

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/FirstHabitStep.kt
git commit -m "feat(onboarding): rebuild first habit step with type cards and schedule section"
```

---

## Task 6: OnboardingWizard — Wire Schedule Through

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingWizard.kt`

`OnboardingWizard` must accept the two new callbacks and pass all four new params (`scheduleOption`, `customDays`, `onScheduleOptionChange`, `onCustomDaysChange`) to `FirstHabitStep` at both call sites (Crossfade branch, line ~82, and AnimatedContent branch, line ~122).

- [ ] **Step 1: Add kotlinx.datetime import and new parameters to OnboardingWizard**

Add import:
```kotlin
import kotlinx.datetime.DayOfWeek
```

Add two parameters to the `OnboardingWizard` function signature (after `onUnitChange`):
```kotlin
onScheduleOptionChange: (ScheduleOption) -> Unit,
onCustomDaysChange: (Set<DayOfWeek>) -> Unit,
```

- [ ] **Step 2: Update both FirstHabitStep call sites**

Both the Crossfade branch (`step == 2`) and the AnimatedContent branch (`step == 2`) currently call `FirstHabitStep` with four content params and four callbacks. Add the four new params to both:

```kotlin
2 -> FirstHabitStep(
    habitName = state.habitName,
    habitType = state.habitType,
    targetValue = state.targetValue,
    unit = state.unit,
    scheduleOption = state.scheduleOption,       // new
    customDays = state.customDays,               // new
    onHabitNameChange = onHabitNameChange,
    onHabitTypeChange = onHabitTypeChange,
    onTargetValueChange = onTargetValueChange,
    onUnitChange = onUnitChange,
    onScheduleOptionChange = onScheduleOptionChange,   // new
    onCustomDaysChange = onCustomDaysChange,            // new
    modifier = Modifier.fillMaxSize()
)
```

Apply this change to both the Crossfade branch and the AnimatedContent branch.

- [ ] **Step 3: Update OnboardingRoute.kt to pass the new callbacks**

`OnboardingRoute.kt` instantiates `OnboardingWizard`. It must now pass the two new callbacks. Add these two lines inside the `OnboardingWizard(...)` call:

```kotlin
onScheduleOptionChange = viewModel::updateScheduleOption,
onCustomDaysChange = viewModel::updateCustomDays,
```

- [ ] **Step 4: Verify full compilation and tests**

```bash
./gradlew :composeApp:compileKotlinAndroid && ./gradlew :composeApp:testDebugUnitTest 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL, all tests PASS.

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingWizard.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/OnboardingRoute.kt
git commit -m "feat(onboarding): wire schedule params through OnboardingWizard and OnboardingRoute"
```

---

## Manual Verification

After all tasks are complete, run the app on Android and walk through the full onboarding flow:

1. **Philosophy screen** — CTA reads "Accept the Commitment" (not "Continue")
2. **Strictness screen** — unchanged; "Continue" still appears
3. **First Habit screen:**
   - Binary and Quantitative appear as stacked cards (not chips)
   - Selecting Quantitative expands the card to reveal Target value + Unit fields
   - Selecting Binary collapses the quantitative fields
   - Schedule chips appear: Every Day (selected by default), Weekdays, Weekends, Custom
   - Selecting Custom reveals 7 day-picker chips (Mon–Sun)
   - "Create Habit" CTA is disabled when Custom is selected with no days chosen
   - CTA re-enables when at least one custom day is selected
4. Skip the full flow and verify it still lands on Today screen
