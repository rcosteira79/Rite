# Periodic Reminder Scheduling Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable interval-based periodic reminders within a configurable time window, and fix hardcoded snooze duration.

**Architecture:** Expand-at-schedule-time — compute all fire times when daily worker runs, schedule each as an individual AlarmManager exact alarm. Reuses existing NotificationScheduler with new methods. UI adds segmented control to habit form's reminder card.

**Tech Stack:** Kotlin, Compose Multiplatform, AlarmManager, SQLDelight, kotlin-inject, kotlinx.datetime

---

## File Map

| File | Action | Responsibility |
|------|--------|----------------|
| `composeApp/src/commonMain/.../notifications/PeriodicReminderCalculator.kt` | Create | Pure function: compute fire times from interval + window |
| `composeApp/src/commonTest/.../notifications/PeriodicReminderCalculatorTest.kt` | Create | Tests for fire time computation |
| `composeApp/src/androidMain/.../notifications/NotificationScheduler.kt` | Modify | Add `schedulePeriodicReminders()` and `cancelPeriodicReminders()` |
| `composeApp/src/androidMain/.../notifications/HabitNotification.android.kt` | Modify | Wire PERIODIC branch to new scheduler methods |
| `composeApp/src/commonMain/.../presentation/ui/habit/HabitFormUiAction.kt` | Modify | Add new actions for periodic config |
| `composeApp/src/commonMain/.../presentation/ui/habit/HabitFormViewModel.kt` | Modify | Handle new actions, populate/clear periodic defaults |
| `composeApp/src/commonMain/.../presentation/ui/habit/HabitFormScreen.kt` | Modify | Add segmented control and periodic config UI |
| `composeApp/src/commonMain/composeResources/values/strings_habit_form.xml` | Modify | Add new string resources |
| `composeApp/src/androidMain/.../notifications/NotificationActionReceiver.kt` | Modify | Fix hardcoded snooze duration |

---

### Task 1: PeriodicReminderCalculator — Fire Time Computation

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/notifications/PeriodicReminderCalculator.kt`
- Create: `composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/notifications/PeriodicReminderCalculatorTest.kt`

- [ ] **Step 1: Write the failing tests**

Create the test file:

```kotlin
package com.ricardocosteira.rite.notifications

import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class PeriodicReminderCalculatorTest {

    @Test
    fun `given 2 hour interval from 8am to 10pm when computing fire times then returns all slots`() {
        // Given
        val inputStartTime = LocalTime(8, 0)
        val inputEndTime = LocalTime(22, 0)
        val inputIntervalMinutes = 120

        // When
        val actualFireTimes: List<LocalTime> = PeriodicReminderCalculator.computeFireTimes(
            startTime = inputStartTime,
            endTime = inputEndTime,
            intervalMinutes = inputIntervalMinutes
        )

        // Then
        val expectedFireTimes: List<LocalTime> = listOf(
            LocalTime(8, 0),
            LocalTime(10, 0),
            LocalTime(12, 0),
            LocalTime(14, 0),
            LocalTime(16, 0),
            LocalTime(18, 0),
            LocalTime(20, 0),
            LocalTime(22, 0)
        )
        assertEquals(expectedFireTimes, actualFireTimes)
    }

    @Test
    fun `given 90 minute interval from 9am to 12pm when computing fire times then returns correct slots`() {
        // Given
        val inputStartTime = LocalTime(9, 0)
        val inputEndTime = LocalTime(12, 0)
        val inputIntervalMinutes = 90

        // When
        val actualFireTimes: List<LocalTime> = PeriodicReminderCalculator.computeFireTimes(
            startTime = inputStartTime,
            endTime = inputEndTime,
            intervalMinutes = inputIntervalMinutes
        )

        // Then
        val expectedFireTimes: List<LocalTime> = listOf(
            LocalTime(9, 0),
            LocalTime(10, 30),
            LocalTime(12, 0)
        )
        assertEquals(expectedFireTimes, actualFireTimes)
    }

    @Test
    fun `given interval that does not evenly divide window when computing fire times then last slot before end is included`() {
        // Given
        val inputStartTime = LocalTime(8, 0)
        val inputEndTime = LocalTime(11, 0)
        val inputIntervalMinutes = 120

        // When
        val actualFireTimes: List<LocalTime> = PeriodicReminderCalculator.computeFireTimes(
            startTime = inputStartTime,
            endTime = inputEndTime,
            intervalMinutes = inputIntervalMinutes
        )

        // Then
        val expectedFireTimes: List<LocalTime> = listOf(
            LocalTime(8, 0),
            LocalTime(10, 0)
        )
        assertEquals(expectedFireTimes, actualFireTimes)
    }

    @Test
    fun `given start time equals end time when computing fire times then returns single slot`() {
        // Given
        val inputStartTime = LocalTime(9, 0)
        val inputEndTime = LocalTime(9, 0)
        val inputIntervalMinutes = 60

        // When
        val actualFireTimes: List<LocalTime> = PeriodicReminderCalculator.computeFireTimes(
            startTime = inputStartTime,
            endTime = inputEndTime,
            intervalMinutes = inputIntervalMinutes
        )

        // Then
        val expectedFireTimes: List<LocalTime> = listOf(LocalTime(9, 0))
        assertEquals(expectedFireTimes, actualFireTimes)
    }

    @Test
    fun `given current time within window when filtering past times then skips past slots`() {
        // Given
        val inputFireTimes: List<LocalTime> = listOf(
            LocalTime(8, 0),
            LocalTime(10, 0),
            LocalTime(12, 0),
            LocalTime(14, 0)
        )
        val inputCurrentTime = LocalTime(11, 0)

        // When
        val actualFutureTimes: List<LocalTime> = PeriodicReminderCalculator.filterFutureFireTimes(
            fireTimes = inputFireTimes,
            currentTime = inputCurrentTime
        )

        // Then
        val expectedFutureTimes: List<LocalTime> = listOf(
            LocalTime(12, 0),
            LocalTime(14, 0)
        )
        assertEquals(expectedFutureTimes, actualFutureTimes)
    }

    @Test
    fun `given current time before window when filtering past times then returns all slots`() {
        // Given
        val inputFireTimes: List<LocalTime> = listOf(
            LocalTime(8, 0),
            LocalTime(10, 0),
            LocalTime(12, 0)
        )
        val inputCurrentTime = LocalTime(7, 0)

        // When
        val actualFutureTimes: List<LocalTime> = PeriodicReminderCalculator.filterFutureFireTimes(
            fireTimes = inputFireTimes,
            currentTime = inputCurrentTime
        )

        // Then
        assertEquals(inputFireTimes, actualFutureTimes)
    }

    @Test
    fun `given fire times when computing slot count then returns total number of fire times`() {
        // Given
        val inputStartTime = LocalTime(8, 0)
        val inputEndTime = LocalTime(14, 0)
        val inputIntervalMinutes = 120

        // When
        val actualSlotCount: Int = PeriodicReminderCalculator.computeSlotCount(
            startTime = inputStartTime,
            endTime = inputEndTime,
            intervalMinutes = inputIntervalMinutes
        )

        // Then
        assertEquals(4, actualSlotCount)
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :composeApp:jvmTest --tests "com.ricardocosteira.rite.notifications.PeriodicReminderCalculatorTest" --no-daemon`
Expected: FAIL — class not found

- [ ] **Step 3: Write the implementation**

Create the calculator:

```kotlin
package com.ricardocosteira.rite.notifications

import kotlinx.datetime.LocalTime

/**
 * Pure computation of fire times for periodic reminders within a time window.
 * Shared across platforms (commonMain).
 */
object PeriodicReminderCalculator {

    /**
     * Computes all fire times for a periodic reminder.
     *
     * @param startTime Beginning of the reminder window
     * @param endTime End of the reminder window (inclusive if exactly on a slot boundary)
     * @param intervalMinutes Minutes between each reminder
     * @return Ordered list of fire times within the window
     */
    fun computeFireTimes(
        startTime: LocalTime,
        endTime: LocalTime,
        intervalMinutes: Int
    ): List<LocalTime> {
        val fireTimes: MutableList<LocalTime> = mutableListOf()
        var currentMinuteOfDay: Int = startTime.toSecondOfDay() / 60
        val endMinuteOfDay: Int = endTime.toSecondOfDay() / 60

        while (currentMinuteOfDay <= endMinuteOfDay) {
            fireTimes.add(LocalTime.fromSecondOfDay(currentMinuteOfDay * 60))
            currentMinuteOfDay += intervalMinutes
        }

        return fireTimes
    }

    /**
     * Filters out fire times that have already passed.
     *
     * @param fireTimes All computed fire times for the day
     * @param currentTime The current time of day
     * @return Only fire times that are strictly after currentTime
     */
    fun filterFutureFireTimes(
        fireTimes: List<LocalTime>,
        currentTime: LocalTime
    ): List<LocalTime> = fireTimes.filter { it > currentTime }

    /**
     * Computes the total number of fire time slots for cancellation purposes.
     * Equivalent to computeFireTimes(...).size but without allocating the list.
     *
     * @param startTime Beginning of the reminder window
     * @param endTime End of the reminder window
     * @param intervalMinutes Minutes between each reminder
     * @return Total number of slots
     */
    fun computeSlotCount(
        startTime: LocalTime,
        endTime: LocalTime,
        intervalMinutes: Int
    ): Int {
        val startMinute: Int = startTime.toSecondOfDay() / 60
        val endMinute: Int = endTime.toSecondOfDay() / 60
        val windowMinutes: Int = endMinute - startMinute
        return (windowMinutes / intervalMinutes) + 1
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :composeApp:jvmTest --tests "com.ricardocosteira.rite.notifications.PeriodicReminderCalculatorTest" --no-daemon`
Expected: All 7 tests PASS

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/notifications/PeriodicReminderCalculator.kt composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/notifications/PeriodicReminderCalculatorTest.kt
git commit -m "feat(notifications): add PeriodicReminderCalculator for fire time computation"
```

---

### Task 2: NotificationScheduler — Periodic Alarm Scheduling & Cancellation

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/notifications/NotificationScheduler.kt`

- [ ] **Step 1: Add `schedulePeriodicReminders()` method**

Add this method to `NotificationScheduler` after `scheduleHabitReminder()` (after line 63):

```kotlin
    /**
     * Schedules multiple alarms for a periodic reminder within a time window.
     * Each fire time gets its own exact alarm.
     *
     * @param instance The habit instance to send notifications for
     * @param habit The habit details
     * @param fireTimes The computed fire times to schedule (already filtered for future-only)
     */
    fun schedulePeriodicReminders(
        instance: HabitInstance,
        habit: Habit,
        fireTimes: List<LocalTime>
    ) {
        fireTimes.forEachIndexed { index: Int, fireTime: LocalTime ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, fireTime.hour)
                set(Calendar.MINUTE, fireTime.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Skip if time already passed (safety check)
            if (calendar.timeInMillis > System.currentTimeMillis()) {
                scheduleNotification(
                    notificationId = "${instance.id}_periodic_$index".hashCode(),
                    triggerTimeMillis = calendar.timeInMillis,
                    notificationType = NotificationType.HABIT_REMINDER,
                    instanceId = instance.id,
                    habitName = habit.name
                )
            }
        }
    }
```

- [ ] **Step 2: Add `cancelPeriodicReminders()` method**

Add this method after `cancelNotificationsForInstance()` (after line 129):

```kotlin
    /**
     * Cancels all periodic reminder alarms for a habit instance.
     *
     * @param instanceId The ID of the habit instance
     * @param slotCount Total number of periodic slots to cancel
     */
    fun cancelPeriodicReminders(instanceId: String, slotCount: Int) {
        for (index: Int in 0 until slotCount) {
            cancelNotification("${instanceId}_periodic_$index".hashCode())
        }
        // Also cancel grace period and snooze
        cancelNotification("${instanceId}_grace".hashCode())
        cancelNotification("${instanceId}_snooze".hashCode())
    }
```

- [ ] **Step 3: Verify the project compiles**

Run: `./gradlew :composeApp:compileKotlinAndroid --no-daemon`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/notifications/NotificationScheduler.kt
git commit -m "feat(notifications): add periodic alarm scheduling and cancellation to NotificationScheduler"
```

---

### Task 3: HabitNotification.android.kt — Wire PERIODIC Branch

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/notifications/HabitNotification.android.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/notifications/HabitNotification.kt`

- [ ] **Step 1: Update the expect interface to accept a reminder parameter for cancellation**

The current `cancelReminder(instanceId: String)` doesn't know the reminder type. For periodic cancellation, we need the reminder config to compute slot count. Update the expect interface in `HabitNotification.kt` to add an overload:

```kotlin
expect class HabitNotification {
    fun scheduleReminder(habit: Habit, reminder: HabitReminder, instance: HabitInstance)
    fun cancelReminder(instanceId: String)
    fun cancelReminder(instanceId: String, reminder: HabitReminder?)
    fun cancelAllForHabit(habitId: String, instanceIds: List<String>)
    fun updateTrackingNotification(trackedHabits: List<TrackedHabitInfo>)
    fun hideTrackingNotification()
    fun isNotificationPermissionGranted(): Boolean
    fun openNotificationSettings()
}
```

- [ ] **Step 2: Implement the PERIODIC branch in the Android actual**

Replace the `scheduleReminder()` and add the new `cancelReminder()` overload in `HabitNotification.android.kt`:

```kotlin
    actual fun scheduleReminder(habit: Habit, reminder: HabitReminder, instance: HabitInstance) {
        if (!isNotificationPermissionGranted()) return
        when (reminder.reminderType) {
            ReminderType.FIXED -> {
                val time: LocalTime = reminder.time ?: return
                scheduler.scheduleHabitReminder(instance, habit, time.hour, time.minute)
            }

            ReminderType.PERIODIC -> {
                val intervalMinutes: Int = reminder.intervalMinutes ?: return
                val startTime: LocalTime = reminder.startTime ?: return
                val endTime: LocalTime = reminder.endTime ?: return

                val allFireTimes: List<LocalTime> = PeriodicReminderCalculator.computeFireTimes(
                    startTime = startTime,
                    endTime = endTime,
                    intervalMinutes = intervalMinutes
                )
                val currentTime: LocalTime = kotlinx.datetime.Clock.System.now()
                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                    .time
                val futureFireTimes: List<LocalTime> =
                    PeriodicReminderCalculator.filterFutureFireTimes(allFireTimes, currentTime)

                scheduler.schedulePeriodicReminders(instance, habit, futureFireTimes)
            }
        }
    }

    actual fun cancelReminder(instanceId: String) {
        scheduler.cancelNotificationsForInstance(instanceId)
    }

    actual fun cancelReminder(instanceId: String, reminder: HabitReminder?) {
        if (reminder != null && reminder.reminderType == ReminderType.PERIODIC) {
            val slotCount: Int = PeriodicReminderCalculator.computeSlotCount(
                startTime = reminder.startTime ?: return,
                endTime = reminder.endTime ?: return,
                intervalMinutes = reminder.intervalMinutes ?: return
            )
            scheduler.cancelPeriodicReminders(instanceId, slotCount)
        } else {
            scheduler.cancelNotificationsForInstance(instanceId)
        }
    }
```

- [ ] **Step 3: Add the `cancelReminder` overload stub to iOS and JVM actuals**

In `composeApp/src/iosMain/kotlin/com/ricardocosteira/rite/notifications/HabitNotification.ios.kt` and `composeApp/src/jvmMain/kotlin/com/ricardocosteira/rite/notifications/HabitNotification.jvm.kt`, add:

```kotlin
    actual fun cancelReminder(instanceId: String, reminder: HabitReminder?) {
        // No-op on this platform
    }
```

Add the necessary import: `import com.ricardocosteira.rite.domain.models.HabitReminder`

- [ ] **Step 4: Verify the project compiles**

Run: `./gradlew :composeApp:compileKotlinAndroid --no-daemon`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/notifications/HabitNotification.kt composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/notifications/HabitNotification.android.kt composeApp/src/iosMain/kotlin/com/ricardocosteira/rite/notifications/HabitNotification.ios.kt composeApp/src/jvmMain/kotlin/com/ricardocosteira/rite/notifications/HabitNotification.jvm.kt
git commit -m "feat(notifications): wire PERIODIC branch in HabitNotification with reminder-aware cancellation"
```

---

### Task 4: Fix Hardcoded Snooze Duration

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/notifications/NotificationActionReceiver.kt`

- [ ] **Step 1: Replace hardcoded snooze duration with user setting**

In `NotificationActionReceiver.kt`, modify the `handleSnooze()` method (around line 85). Replace:

```kotlin
    private suspend fun handleSnooze(
        context: Context,
        appComponent: RiteAppComponent,
        instanceId: String
    ) {
        val result = appComponent.snoozeHabit.execute(instanceId, durationMinutes = 15)
```

With:

```kotlin
    private suspend fun handleSnooze(
        context: Context,
        appComponent: RiteAppComponent,
        instanceId: String
    ) {
        val user = appComponent.userRepository.getUser()
        val snoozeDurationMinutes: Int = user?.maxSnoozeDurationMinutes ?: 15
        val result = appComponent.snoozeHabit.execute(instanceId, durationMinutes = snoozeDurationMinutes)
```

Add import at top of file: `import com.ricardocosteira.rite.domain.models.User`

- [ ] **Step 2: Verify the project compiles**

Run: `./gradlew :composeApp:compileKotlinAndroid --no-daemon`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/notifications/NotificationActionReceiver.kt
git commit -m "fix(notifications): use user's maxSnoozeDurationMinutes instead of hardcoded 15"
```

---

### Task 5: UI Actions & ViewModel — Periodic Config Handling

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormUiAction.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormViewModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormScreen.kt` (action wiring only)

- [ ] **Step 1: Add new UI actions**

Add these actions to `HabitFormUiAction.kt` (after `ReminderTimeChanged` on line 26):

```kotlin
    data class ReminderTypeChanged(val reminderType: ReminderType) : HabitFormUiAction

    data class IntervalChanged(val interval: String) : HabitFormUiAction

    data class PeriodicStartTimeChanged(val hour: Int, val minute: Int) : HabitFormUiAction

    data class PeriodicEndTimeChanged(val hour: Int, val minute: Int) : HabitFormUiAction
```

Add import: `import com.ricardocosteira.rite.domain.models.ReminderType`

- [ ] **Step 2: Update ViewModel to populate/clear periodic defaults**

In `HabitFormViewModel.kt`, modify `updateReminderType()` (line 173) to populate defaults when switching to PERIODIC and clear when switching to FIXED:

```kotlin
    fun updateReminderType(reminderType: ReminderType) {
        _state.update {
            when (reminderType) {
                ReminderType.PERIODIC -> it.copy(
                    reminderType = reminderType,
                    startTime = it.startTime ?: DEFAULT_PERIODIC_START_TIME,
                    endTime = it.endTime ?: DEFAULT_PERIODIC_END_TIME
                )
                ReminderType.FIXED -> it.copy(
                    reminderType = reminderType,
                    startTime = null,
                    endTime = null
                )
            }
        }
    }
```

Also add the default constants in the companion object (line 46):

```kotlin
    private companion object {
        private val DEFAULT_REMINDER_TIME = HabitFormState.DEFAULT_REMINDER_TIME
        private val DEFAULT_PERIODIC_START_TIME = LocalTime(8, 0)
        private val DEFAULT_PERIODIC_END_TIME = LocalTime(22, 0)
    }
```

And add methods for the new time pickers:

```kotlin
    fun updatePeriodicStartTime(hour: Int, minute: Int) {
        _state.update { it.copy(startTime = LocalTime(hour, minute)) }
    }

    fun updatePeriodicEndTime(hour: Int, minute: Int) {
        _state.update { it.copy(endTime = LocalTime(hour, minute)) }
    }
```

- [ ] **Step 3: Wire new actions in HabitFormScreen**

In `HabitFormScreen.kt`, add the new action cases to the `when` block in the `onAction` lambda (around line 211, after the `ReminderTimeChanged` case):

```kotlin
                is HabitFormUiAction.ReminderTypeChanged ->
                    viewModel.updateReminderType(action.reminderType)

                is HabitFormUiAction.IntervalChanged ->
                    viewModel.updateIntervalMinutes(action.interval)

                is HabitFormUiAction.PeriodicStartTimeChanged ->
                    viewModel.updatePeriodicStartTime(action.hour, action.minute)

                is HabitFormUiAction.PeriodicEndTimeChanged ->
                    viewModel.updatePeriodicEndTime(action.hour, action.minute)
```

- [ ] **Step 4: Also update `updateType()` to no longer force reminderType**

In `HabitFormViewModel.kt`, the `updateType()` method (line 127) currently forces `reminderType = PERIODIC` when switching to QUANTITATIVE. Remove this coupling — the user should choose the reminder type independently:

```kotlin
    fun updateType(type: HabitType) {
        _state.update { it.copy(type = type) }
    }
```

- [ ] **Step 5: Verify the project compiles**

Run: `./gradlew :composeApp:compileKotlinAndroid --no-daemon`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormUiAction.kt composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormViewModel.kt composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormScreen.kt
git commit -m "feat(habit-form): add UI actions and ViewModel handling for periodic reminder config"
```

---

### Task 6: UI — Segmented Control & Periodic Config Fields

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormScreen.kt`
- Modify: `composeApp/src/commonMain/composeResources/values/strings_habit_form.xml`

- [ ] **Step 1: Add string resources**

Add the following strings to `strings_habit_form.xml`:

```xml
    <string name="habit_form_reminder_type_fixed">Fixed time</string>
    <string name="habit_form_reminder_type_periodic">Periodic</string>
    <string name="habit_form_periodic_every">Every</string>
    <string name="habit_form_periodic_from">From</string>
    <string name="habit_form_periodic_until">Until</string>
    <string name="habit_form_periodic_minutes">min</string>
    <string name="habit_form_periodic_hours">hr</string>
    <string name="habit_form_periodic_interval_hint">Interval</string>
    <string name="habit_form_periodic_invalid_window">Start time must be before end time</string>
```

- [ ] **Step 2: Update the reminder subtitle logic**

In `HabitFormScreen.kt`, replace the `reminderSubtitle` computation (lines 496-500):

```kotlin
            val reminderSubtitle: String = if (state.hasReminder) {
                when (state.reminderType) {
                    ReminderType.FIXED -> state.reminderTime?.formatAmPm().orEmpty()
                    ReminderType.PERIODIC -> {
                        val interval = state.intervalMinutes.toIntOrNull()
                        if (interval != null && state.startTime != null && state.endTime != null) {
                            val intervalText = if (interval >= 60 && interval % 60 == 0) {
                                "${interval / 60}${stringResource(Res.string.habit_form_periodic_hours)}"
                            } else {
                                "$interval${stringResource(Res.string.habit_form_periodic_minutes)}"
                            }
                            "${stringResource(Res.string.habit_form_periodic_every)} $intervalText"
                        } else {
                            stringResource(Res.string.habit_form_reminder_type_periodic)
                        }
                    }
                }
            } else {
                stringResource(Res.string.habit_form_reminder_off)
            }
```

Add import: `import com.ricardocosteira.rite.domain.models.ReminderType`

- [ ] **Step 3: Update the reminder DetailRow click handler**

Replace the `onClick` in the Reminder DetailRow (lines 540-544). When periodic is selected, clicking the row shouldn't open the time picker:

```kotlin
                        onClick = if (state.hasReminder && state.reminderType == ReminderType.FIXED) {
                            { isTimePickerVisible = true }
                        } else {
                            null
                        },
```

- [ ] **Step 4: Remove automatic time picker open on toggle**

In the Switch `onCheckedChange` (line 550-552), remove the automatic `isTimePickerVisible = true` when toggling on — the user should first pick a reminder type:

```kotlin
                            Switch(
                                checked = state.hasReminder,
                                onCheckedChange = { checked: Boolean ->
                                    onAction(HabitFormUiAction.HasReminderChanged(checked))
                                },
                                enabled = state.areNotificationTogglesEnabled
                            )
```

- [ ] **Step 5: Add the segmented control and periodic config below the Reminder DetailRow**

After the Reminder DetailRow closing parenthesis (after line 557), before the Tracking DetailRow (line 559), insert:

```kotlin
                    // Reminder type selector and config (animated)
                    AnimatedVisibility(
                        visible = state.hasReminder,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                            // Segmented button row
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                SegmentedButton(
                                    selected = state.reminderType == ReminderType.FIXED,
                                    onClick = {
                                        onAction(
                                            HabitFormUiAction.ReminderTypeChanged(ReminderType.FIXED)
                                        )
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = 0,
                                        count = 2
                                    )
                                ) {
                                    Text(
                                        text = stringResource(
                                            Res.string.habit_form_reminder_type_fixed
                                        )
                                    )
                                }
                                SegmentedButton(
                                    selected = state.reminderType == ReminderType.PERIODIC,
                                    onClick = {
                                        onAction(
                                            HabitFormUiAction.ReminderTypeChanged(
                                                ReminderType.PERIODIC
                                            )
                                        )
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = 1,
                                        count = 2
                                    )
                                ) {
                                    Text(
                                        text = stringResource(
                                            Res.string.habit_form_reminder_type_periodic
                                        )
                                    )
                                }
                            }

                            // Content crossfade between Fixed and Periodic
                            Crossfade(
                                targetState = state.reminderType,
                                label = "reminder_type_content"
                            ) { currentType: ReminderType ->
                                when (currentType) {
                                    ReminderType.FIXED -> {
                                        // Tappable time display
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { isTimePickerVisible = true }
                                                .padding(vertical = 12.dp),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = state.resolvedReminderTime.formatAmPm(),
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    ReminderType.PERIODIC -> {
                                        PeriodicReminderConfig(
                                            intervalMinutes = state.intervalMinutes,
                                            startTime = state.startTime,
                                            endTime = state.endTime,
                                            onIntervalChanged = { interval: String ->
                                                onAction(
                                                    HabitFormUiAction.IntervalChanged(interval)
                                                )
                                            },
                                            onStartTimeClick = {
                                                isStartTimePickerVisible = true
                                            },
                                            onEndTimeClick = {
                                                isEndTimePickerVisible = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
```

Add imports at top of file:

```kotlin
import androidx.compose.animation.Crossfade
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
```

- [ ] **Step 6: Add local state variables for the time pickers**

Near the existing `var isTimePickerVisible` declaration (search for it near the top of the composable function), add:

```kotlin
    var isStartTimePickerVisible by remember { mutableStateOf(false) }
    var isEndTimePickerVisible by remember { mutableStateOf(false) }
```

- [ ] **Step 7: Add the PeriodicReminderConfig composable**

Add this composable at the bottom of the file, before the `ReminderTimePickerDialog`:

```kotlin
@Composable
private fun PeriodicReminderConfig(
    intervalMinutes: String,
    startTime: LocalTime?,
    endTime: LocalTime?,
    onIntervalChanged: (String) -> Unit,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit
) {
    val isWindowInvalid: Boolean = startTime != null && endTime != null && startTime >= endTime

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Interval row with unit toggle
        var isHoursMode by remember {
            mutableStateOf(
                intervalMinutes.toIntOrNull()?.let { it >= 60 && it % 60 == 0 } ?: false
            )
        }
        val displayValue: String = if (isHoursMode) {
            val mins = intervalMinutes.toIntOrNull() ?: 0
            if (mins > 0) (mins / 60).toString() else ""
        } else {
            intervalMinutes
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.habit_form_periodic_every),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = displayValue,
                    onValueChange = { value: String ->
                        if (value.all { it.isDigit() }) {
                            val rawMinutes: Int = if (isHoursMode) {
                                (value.toIntOrNull() ?: 0) * 60
                            } else {
                                value.toIntOrNull() ?: 0
                            }
                            onIntervalChanged(rawMinutes.toString())
                        }
                    },
                    modifier = Modifier.width(72.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = !isHoursMode,
                        onClick = {
                            isHoursMode = false
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text(
                            text = stringResource(Res.string.habit_form_periodic_minutes),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    SegmentedButton(
                        selected = isHoursMode,
                        onClick = {
                            isHoursMode = true
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text(
                            text = stringResource(Res.string.habit_form_periodic_hours),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // From row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onStartTimeClick),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.habit_form_periodic_from),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = startTime?.formatAmPm() ?: "--:--",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Until row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEndTimeClick),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.habit_form_periodic_until),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isWindowInvalid) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = endTime?.formatAmPm() ?: "--:--",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isWindowInvalid) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier
                    .background(
                        color = if (isWindowInvalid) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Validation error
        if (isWindowInvalid) {
            Text(
                text = stringResource(Res.string.habit_form_periodic_invalid_window),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
```

Add imports:

```kotlin
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.OutlinedTextField
import kotlinx.datetime.LocalTime
import androidx.compose.runtime.mutableStateOf
```

- [ ] **Step 8: Add the start/end time picker dialogs**

After the existing `ReminderTimePickerDialog` usage (around line 800), add:

```kotlin
    // Start time picker for periodic reminders
    if (isStartTimePickerVisible) {
        ReminderTimePickerDialog(
            initialHour = state.startTime?.hour ?: 8,
            initialMinute = state.startTime?.minute ?: 0,
            onConfirm = { hour: Int, minute: Int ->
                onAction(HabitFormUiAction.PeriodicStartTimeChanged(hour, minute))
                isStartTimePickerVisible = false
            },
            onDismiss = { isStartTimePickerVisible = false }
        )
    }

    // End time picker for periodic reminders
    if (isEndTimePickerVisible) {
        ReminderTimePickerDialog(
            initialHour = state.endTime?.hour ?: 22,
            initialMinute = state.endTime?.minute ?: 0,
            onConfirm = { hour: Int, minute: Int ->
                onAction(HabitFormUiAction.PeriodicEndTimeChanged(hour, minute))
                isEndTimePickerVisible = false
            },
            onDismiss = { isEndTimePickerVisible = false }
        )
    }
```

- [ ] **Step 9: Verify the project compiles**

Run: `./gradlew :composeApp:compileKotlinAndroid --no-daemon`
Expected: BUILD SUCCESSFUL

- [ ] **Step 10: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormScreen.kt composeApp/src/commonMain/composeResources/values/strings_habit_form.xml
git commit -m "feat(habit-form): add segmented control and periodic reminder config UI"
```

---

### Task 7: Integration — Update Cancellation Call Sites

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormViewModel.kt`

- [ ] **Step 1: Update `updateExistingHabit()` to use reminder-aware cancellation**

In `HabitFormViewModel.kt`, in the `updateExistingHabit()` method (around line 356), replace the cancellation call to use the new overload that knows about the reminder type. Change:

```kotlin
            habitNotification.cancelReminder(todayInstance.id)
```

To:

```kotlin
            val existingReminder: HabitReminder? = existingReminders.firstOrNull()
            habitNotification.cancelReminder(todayInstance.id, existingReminder)
```

Note: `existingReminders` is already fetched on line 342 before deletion on line 343. Move the cancellation call to *before* the deletion so we still have access to the reminder config. Reorder lines 342-348 to:

```kotlin
        val existingReminders: List<HabitReminder> = habitRepository.getRemindersForHabit(habitId)
        val today: LocalDate =
            Clock.System.now().toLocalDate(TimeZone.currentSystemDefault())
        val todayInstance: HabitInstance? =
            habitInstanceRepository.getInstanceForHabitAndDate(habitId, today)

        // Cancel existing alarms before deleting reminders (need reminder config for periodic cancellation)
        if (todayInstance != null) {
            val existingReminder: HabitReminder? = existingReminders.firstOrNull()
            habitNotification.cancelReminder(todayInstance.id, existingReminder)
        }

        // Now safe to delete old reminders
        existingReminders.forEach { habitRepository.deleteReminder(it.id) }

        if (reminder != null) {
            habitRepository.createReminderForHabit(
                reminder.copy(habitId = habitId, id = uuidProvider.generate())
            )
        }

        // Reschedule with new config
        if (todayInstance != null && reminder != null) {
            val savedReminders: List<HabitReminder> =
                habitRepository.getRemindersForHabit(habitId)
            val savedReminder: HabitReminder? = savedReminders.firstOrNull()
            if (savedReminder != null) {
                habitNotification.scheduleReminder(updatedHabit, savedReminder, todayInstance)
            }
        }
```

- [ ] **Step 2: Verify the project compiles**

Run: `./gradlew :composeApp:compileKotlinAndroid --no-daemon`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Run existing tests to check for regressions**

Run: `./gradlew :composeApp:jvmTest --no-daemon`
Expected: All existing tests PASS

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormViewModel.kt
git commit -m "feat(habit-form): use reminder-aware cancellation in edit flow"
```

---

### Task 8: Screenshot Tests — Update Goldens

**Files:**
- Modify: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormScreenshotTest.kt`

- [ ] **Step 1: Check existing screenshot test**

Read the existing `HabitFormScreenshotTest.kt` to understand the pattern, then run the existing tests to see if they need re-recording due to the UI changes.

Run: `./gradlew :composeApp:testDebugUnitTest --tests "com.ricardocosteira.rite.presentation.ui.habit.HabitFormScreenshotTest" --no-daemon`

- [ ] **Step 2: Re-record goldens if they fail**

If goldens fail due to UI changes (expected — we changed the reminder section):

Run: `./gradlew :composeApp:recordRoborazziDebug --tests "com.ricardocosteira.rite.presentation.ui.habit.HabitFormScreenshotTest" --no-daemon`

- [ ] **Step 3: Add screenshot test for periodic reminder state**

Add a new test case in `HabitFormScreenshotTest.kt` that shows the form with periodic reminder enabled:

```kotlin
    @Test
    fun habitFormScreen_periodicReminderEnabled() {
        composeTestRule.setContent {
            RiteTheme {
                HabitFormScreen(
                    state = HabitFormState(
                        name = "Drink Water",
                        type = HabitType.QUANTITATIVE,
                        targetValue = "8",
                        unit = "glasses",
                        hasReminder = true,
                        reminderType = ReminderType.PERIODIC,
                        intervalMinutes = "120",
                        startTime = LocalTime(8, 0),
                        endTime = LocalTime(22, 0),
                        isNotificationPermissionGranted = true
                    ),
                    onAction = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
```

- [ ] **Step 4: Record the new golden**

Run: `./gradlew :composeApp:recordRoborazziDebug --tests "com.ricardocosteira.rite.presentation.ui.habit.HabitFormScreenshotTest.habitFormScreen_periodicReminderEnabled" --no-daemon`

- [ ] **Step 5: Verify all screenshot tests pass**

Run: `./gradlew :composeApp:testDebugUnitTest --tests "com.ricardocosteira.rite.presentation.ui.habit.HabitFormScreenshotTest" --no-daemon`
Expected: All PASS

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/habit/HabitFormScreenshotTest.kt composeApp/src/androidUnitTest/resources/
git commit -m "test: add screenshot test for periodic reminder config and re-record goldens"
```

---

### Task 9: Final Integration Test & Cleanup

- [ ] **Step 1: Run full test suite**

Run: `./gradlew :composeApp:jvmTest :composeApp:testDebugUnitTest --no-daemon`
Expected: All tests PASS

- [ ] **Step 2: Build release variant to verify no ProGuard/R8 issues**

Run: `./gradlew :composeApp:assembleRelease --no-daemon`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Update BACKLOG.md**

Mark the periodic reminder scheduling item as completed:

Change:
```markdown
- [ ] Periodic reminder scheduling (interval-based within a time window)
```

To:
```markdown
- [x] Periodic reminder scheduling (interval-based within a time window)
```

- [ ] **Step 4: Commit**

```bash
git add BACKLOG.md
git commit -m "docs: mark periodic reminder scheduling as completed in BACKLOG.md"
```
