# Notification System: Reminder Fix + Tracking Notification

## Problem

The notification infrastructure (scheduling, display, channels, receivers) is fully implemented but
**never connected to the habit creation or daily generation flows**. Reminders are saved to the
database but never scheduled with AlarmManager, so they never fire. Additionally, `POST_NOTIFICATIONS`
is declared in the manifest but never requested at runtime, causing silent failures on Android 13+.

Alongside the fix, a new **"Track this habit"** feature adds a persistent grouped notification that
lets users monitor progress and take actions (increment, undo, complete) without opening the app.

## Scope

- Fix reminder scheduling by wiring it into habit create/edit, daily generation, and boot receiver
- Add persistent tracking notification (ongoing, grouped, per-habit actions)
- Add notification permission handling (runtime request + form UI degradation)
- Add `HabitNotification` expect/actual interface for KMP abstraction

## Out of Scope

- Event bus / domain event architecture (future work)
- iOS notification implementation (no-op stub only)
- App startup rescheduling (daily worker + BootReceiver is sufficient)
- PERIODIC reminder type UI (existing but untouched in this work)

---

## Architecture

### `HabitNotification` Expect/Actual Interface

A common interface in `commonMain` abstracts all notification operations. Android provides the real
implementation; iOS gets a no-op stub.

```
commonMain:
  HabitNotification (expect)
    scheduleReminder(habit, reminder, instance)
    cancelReminder(instanceId)
    cancelAllRemindersForHabit(habitId)
    showTrackingNotification(trackedHabits)
    updateTrackingNotification(trackedHabits)
    hideTrackingNotification()
    isNotificationPermissionGranted(): Boolean
    openNotificationSettings()

androidMain:
  AndroidHabitNotification (actual)
    wraps NotificationScheduler (alarm scheduling)
    wraps HabitNotificationManager (notification display)
    wraps new TrackingNotificationManager (persistent grouped notification)

iosMain:
  IosHabitNotification (actual, no-op stub)
```

### DI Integration

`HabitNotification` is provided via `AppComponent` and injected into:

- **`HabitFormViewModel`** -- schedules/cancels reminders on save
- **`TodayViewModel`** -- updates tracking notification on progress changes
- **`DailyHabitGenerationWorker`** -- schedules reminders and shows tracking notification for new instances
- **`BootReceiver`** -- reschedules all active reminders after reboot

---

## Reminder Fix

### Root Cause

`NotificationScheduler.scheduleHabitReminder()` exists and works correctly, but is never called
during the main flows:

1. Habit creation (`HabitFormViewModel.saveHabit()`)
2. Habit editing (reminder settings change)
3. Daily instance generation (`DailyHabitGenerationWorker`)
4. Device reboot (`BootReceiver` -- has a TODO placeholder)

### Fix Points

**`HabitFormViewModel.saveHabit()`:**
- After successful create: call `scheduleReminder()` if reminder is enabled
- After successful edit: cancel old reminder, schedule new one if reminder is still enabled
- After successful edit with reminder disabled: cancel reminder only

**`DailyHabitGenerationWorker.doWork()`:**
- After `generateDailyHabits.execute()`, fetch today's instances with active reminders
- Call `scheduleReminder()` for each

**`BootReceiver.onReceive()`:**
- Replace the existing TODO with: fetch all active instances with reminders, reschedule all

**Habit completion / skip / archive / delete:**
- Cancel all notifications for that habit via `cancelAllRemindersForHabit()`

---

## Tracking Notification

### Notification Channel

New channel: `CHANNEL_HABIT_TRACKING` ("Habit Tracking")
- `IMPORTANCE_LOW` -- silent, no vibration, no sound
- Separate from reminders so the user can independently control each in system settings

### Notification Structure

- Single ongoing notification group (`setOngoing(true)`)
- Each tracked habit is a child notification in the group
- A summary notification acts as the group parent

**Binary habit child:**
- Title: habit name
- Content: "Not completed"
- Actions: "Complete"

**Quantitative habit child:**
- Title: habit name
- Content: progress display (e.g., "3 / 10 glasses")
- Actions: "+N" (increment by `defaultIncrement`), "Undo" (reverts last increment exactly)

### Lifecycle

- **Shown** when `DailyHabitGenerationWorker` creates instances for habits with `isTrackingEnabled = true`
- **Updated** in real-time when `TodayViewModel` processes increment, undo, or complete actions
- **Child removed** when a habit is completed (via notification action or in-app)
- **Entire notification dismissed** when all tracked habits for the day are completed
- **Dismissed** at end of day

### Domain Model Addition

`Habit` gains a new field:

```kotlin
val isTrackingEnabled: Boolean = false
```

Persisted in the database (`habit` table) as `isTrackingEnabled INTEGER NOT NULL DEFAULT 0`.

---

## Form UI Changes

### New "Track this habit" Row

Added to the extras card (the Surface containing reminder + note), between reminder and note:

```
┌─────────────────────────────────┐
|  Reminder              [toggle] |
|    Off / 09:00 AM               |
|---------------------------------|
|  Track this habit      [toggle] |
|    Show a persistent            |
|    notification to track        |
|    progress and complete        |
|    from anywhere                |
|---------------------------------|
|  Add a note                     |
|    Motivation, tips...          |
└─────────────────────────────────┘
```

### Contextual Messages Above the Card

Messages appear above the extras card, not inside it.

**When notification permission is denied:**

```
  Notifications are disabled. Tap to open Settings.
┌─────────────────────────────────┐
|  Reminder              [toggle] |  <- greyed out
|  Track this habit      [toggle] |  <- greyed out
|  Add a note                     |
└─────────────────────────────────┘
```

- Both toggles are visually disabled
- The message is tappable and opens the app's notification settings
  (`Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)`)
- When user returns, permission state is re-evaluated; toggles become active if granted

**When both reminder and tracking are enabled:**

```
  You'll receive both a reminder and a persistent
  tracking notification for this habit.
┌─────────────────────────────────┐
|  Reminder              [toggle] |  on
|    09:00 AM                     |
|  Track this habit      [toggle] |  on
|    Show a persistent ...        |
|  Add a note                     |
└─────────────────────────────────┘
```

**When only one or neither is enabled:** no text above the card.

### New State and Actions

`HabitFormState` additions:
- `isTrackingEnabled: Boolean = false`
- `isNotificationPermissionGranted: Boolean = true` (read from `HabitNotification`)

`HabitFormUiAction` addition:
- `data class IsTrackingEnabledChanged(val isEnabled: Boolean)`

---

## Permission Handling

### `POST_NOTIFICATIONS` (Android 13+)

- **Runtime request:** Prompted on first app launch in `MainActivity`
- **Form UI degradation:** When denied, reminder and tracking toggles are greyed out with
  "Notifications are disabled. Tap to open Settings." message above the extras card
- **Settings deep-link:** Tapping the message opens `Settings.ACTION_APP_NOTIFICATION_SETTINGS`
  with the app's package
- **Re-evaluation:** Permission state is checked when the form is composed / resumed

### `SCHEDULE_EXACT_ALARM` (Android 12+)

- Already declared in manifest (`SCHEDULE_EXACT_ALARM` + `USE_EXACT_ALARM`)
- `NotificationScheduler` already has `canScheduleExactAlarms()` check with inexact fallback
- No changes needed

### `HabitNotification` Permission API

```kotlin
expect class HabitNotification {
    fun isNotificationPermissionGranted(): Boolean
    fun openNotificationSettings()
}
```

Android actual:
- `isNotificationPermissionGranted()` checks `NotificationManager.areNotificationsEnabled()`
- `openNotificationSettings()` launches `Settings.ACTION_APP_NOTIFICATION_SETTINGS` intent

iOS actual (no-op stub):
- `isNotificationPermissionGranted()` returns `true` (no degradation until real implementation)
- `openNotificationSettings()` is a no-op

---

## Existing Files to Modify

### Android Platform (`androidMain`)

| File | Change |
|------|--------|
| `NotificationChannels.kt` | Add `CHANNEL_HABIT_TRACKING` channel |
| `NotificationScheduler.kt` | No changes (already works, just needs to be called) |
| `HabitNotificationManager.kt` | Minor: ensure notification actions update tracking notification |
| `NotificationActionReceiver.kt` | Add tracking notification update after actions |
| `BootReceiver.kt` | Replace TODO with reschedule-all implementation |
| `DailyHabitGenerationWorker.kt` | Schedule reminders + show tracking notification after generation |
| `HabitLockApplication.kt` | No changes (channels already created on startup) |
| `AndroidManifest.xml` | No changes (permissions already declared) |

### Common (`commonMain`)

| File | Change |
|------|--------|
| `Habit.kt` | Add `isTrackingEnabled: Boolean = false` |
| `HabitFormState.kt` | Add `isTrackingEnabled`, `isNotificationPermissionGranted` |
| `HabitFormUiAction.kt` | Add `IsTrackingEnabledChanged` |
| `HabitFormViewModel.kt` | Handle new action, schedule/cancel reminders on save |
| `HabitFormScreen.kt` | Add tracking row, contextual messages, permission-disabled state |
| `TodayViewModel.kt` | Update tracking notification on progress/complete/undo |
| `CreateHabit.kt` | Accept `isTrackingEnabled` parameter |
| `HabitLock.sq` | Add `isTrackingEnabled` column to `habit` table |

### New Files

| File | Purpose |
|------|---------|
| `commonMain/.../HabitNotification.kt` | Expect declaration |
| `androidMain/.../AndroidHabitNotification.kt` | Actual implementation wrapping existing classes |
| `androidMain/.../TrackingNotificationManager.kt` | Persistent grouped notification logic |
| `iosMain/.../IosHabitNotification.kt` | No-op stub |
