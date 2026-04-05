# Periodic Reminder Scheduling

## Summary

Add support for interval-based periodic reminders within a configurable time window. A habit can have either a fixed-time reminder OR a periodic reminder, never both. When a periodic reminder is configured (e.g., "every 2 hours from 8:00 AM to 10:00 PM"), the system schedules individual exact alarms for each fire time within the window. Remaining alarms are cancelled when the habit is completed for the day.

Also fixes a pre-existing bug where snooze duration is hardcoded to 15 minutes instead of using the user's configured `maxSnoozeDurationMinutes` from their strictness preset.

## Data Model

No schema changes required. The existing `HabitReminder` table and domain model already have the necessary fields:

- `reminderType: ReminderType` — `FIXED` or `PERIODIC` (enum already exists)
- `time: LocalTime?` — used for FIXED reminders
- `intervalMinutes: Int?` — interval between periodic reminders
- `startTime: LocalTime?` — periodic window start
- `endTime: LocalTime?` — periodic window end

**Validation rules:**
- When `PERIODIC`: `intervalMinutes`, `startTime`, and `endTime` must all be non-null. `startTime < endTime`. `intervalMinutes > 0`.
- When `FIXED`: only `time` is non-null.

## Scheduling Engine

**Approach: Expand-at-schedule-time.** When `DailyHabitGenerationWorker` runs at midnight (or after boot/timezone change), it computes all fire times for periodic reminders and schedules each as an individual exact alarm via `NotificationScheduler`.

### Algorithm

Given `startTime`, `endTime`, and `intervalMinutes`:

1. Compute fire times: start at `startTime`, add `intervalMinutes` repeatedly until exceeding `endTime`. Include `startTime` itself as the first fire time.
2. Skip any times that have already passed (relevant for mid-day rescheduling after boot or timezone change).
3. Schedule each remaining time as an exact alarm using `AlarmManager.setExactAndAllowWhileIdle()`, with notification type `HABIT_REMINDER`.

**Example:** start=8:00, end=22:00, interval=120 min → fire times: [8:00, 10:00, 12:00, 14:00, 16:00, 18:00, 20:00, 22:00].

### Request Code Scheme

Currently, FIXED reminders use `habitInstanceId.hashCode()` as the alarm request code. For PERIODIC, use `(habitInstanceId + timeSlotIndex).hashCode()` (where index is 0, 1, 2...) to make each alarm uniquely addressable while avoiding collision with other habits' request codes.

### Cancellation

When a habit is completed, skipped, or archived, `cancelNotificationsForInstance()` must cancel all pending intents for periodic reminders. Derive the slot count from the reminder config (`startTime`, `endTime`, `intervalMinutes`) rather than storing it — compute the total number of fire times and iterate through request codes `(habitInstanceId + index).hashCode()` for each index to cancel.

### Lifecycle Handlers

- **BootReceiver**: No changes needed. Already reschedules all reminders via the same scheduling path.
- **TimezoneChangeReceiver**: No changes needed. Already cancels all and reschedules via the same path.
- **Edit habit**: Cancel existing alarms and reschedule with new config (same cancel-and-reschedule pattern as FIXED).

### NotificationReceiver

No changes. It receives alarms and shows reminders — it doesn't know or care whether the alarm came from a FIXED or PERIODIC source.

## UI: Habit Form

### State Changes (`HabitFormUiState`)

New fields:
- `reminderType: ReminderType` (default `FIXED`)
- `intervalMinutes: Int?` (default `null`)
- `periodicStartTime: LocalTime?` (default `null`)
- `periodicEndTime: LocalTime?` (default `null`)

`periodicStartTime` and `periodicEndTime` are populated with defaults (8:00 AM / 10:00 PM) only when the user selects the Periodic tab, and cleared back to `null` when they switch to Fixed.

### New UI Actions (`HabitFormUiAction`)

- `ReminderTypeChanged(type: ReminderType)`
- `IntervalChanged(minutes: Int)`
- `PeriodicStartTimeChanged(time: LocalTime)`
- `PeriodicEndTimeChanged(time: LocalTime)`

### Layout

Inside the existing reminder card, below the toggle switch:

1. **When toggle is ON**: Animate in (`expandVertically` + `fadeIn`) a Material 3 `SingleChoiceSegmentedButtonRow` with two segments: "Fixed time" and "Periodic".
2. **Below the segmented row**, crossfade between:
   - **Fixed tab**: Tappable time display (existing behavior, opens `TimePicker` dialog).
   - **Periodic tab**: Three rows:
     - **"Every"** — tappable chip that opens a dialog/bottom sheet with a number field + unit dropdown (minutes/hours).
     - **"From"** — tappable chip that opens a `TimePicker` dialog.
     - **"Until"** — tappable chip that opens a `TimePicker` dialog.

### Interval Picker

Free-form number input with a unit selector (minutes/hours). Validates that the value is > 0.

### Validation

- `startTime < endTime` — show inline error on the "Until" chip if violated.
- `intervalMinutes > 0` — enforce in the input.

### Design Reference

Option A from brainstorming: toggle switch stays, segmented control appears below when ON. See Stitch mockup `create-edit-habit-screen-for-rite-a-habi-2026-04-05T14-47-27-245Z` in `.stitch/screens/`.

## Bug Fix: Hardcoded Snooze Duration

**Problem:** `NotificationActionReceiver.handleSnooze()` passes `durationMinutes = 15` hardcoded to `SnoozeHabit.execute()`. The use case caps it with `minOf(durationMinutes, user.maxSnoozeDurationMinutes)`, but since 15 is already the lowest preset value, the user's configured max snooze duration has no effect.

**Fix:** Read the user's `maxSnoozeDurationMinutes` from the repository and pass it instead of hardcoding 15. The use case's `minOf` cap remains as a safety net.

**Files affected:** `NotificationActionReceiver.kt` — `handleSnooze()` method.

## Out of Scope

- iOS/JVM actual implementations (remain no-op stubs)
- Snooze behavior changes for periodic reminders (existing one-off snooze alarm works as-is)
- Custom notification content for periodic vs fixed (same notification, same actions)
- Settings for global default interval or window
