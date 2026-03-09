# Habit App MVP – Full Business & Domain Specification

## 1. Purpose & Scope
This document defines the **complete business logic and domain rules** for the Habit App MVP. It is intended to be precise enough that a developer can implement the app without additional product clarification.

Out of scope:
- Backend / cloud sync
- Social features
- XP, leveling, achievements UI
- Advanced notification escalation

In scope:
- Habit definitions
- Daily habit execution
- Notifications with actions
- Snooze / skip / undo logic
- Streaks (per-habit and perfect days)
- Calendar-style historical accuracy

---

## 2. Core Concepts

### 2.1 Time Model
- The app is **timezone-aware**.
- The user has a stored `timezone`.
- The system timezone is checked:
  - On app launch
  - Before daily habit generation
- If changed:
  - Update `User.timezone`
  - Store previous timezone
  - Display a **dismissible UI warning**
- Past habit data is **never modified** due to timezone changes.

### 2.2 Day Definition ("Today")
- A day is defined as the **local calendar day** in the user’s timezone.
- Time interval: **[00:00:00, 23:59:59]** inclusive.
- Day rollover occurs at **local midnight**.

---

## 3. Domain Entities

### 3.1 User
- `id`
- `timezone`
- `previousTimezone` (nullable)
- `undoPolicy`:
  - NONE
  - TODAY_ONLY
  - ALL_HISTORY
- Snooze preferences:
  - `maxSnoozeDurationMinutes` (≤ 60)
  - `maxSnoozesPerHabitPerDay` (0…∞)

---

### 3.2 Habit (Definition)
Represents a habit template.

Fields:
- `id`
- `name`
- `description` (optional)
- `type`:
  - BINARY
  - QUANTITATIVE
- `targetValue` (nullable)
- `unit` (optional)
- `isActive`
- `isArchived`
- `createdAt`
- `archivedAt` (nullable)

Rules:
- Archived habits:
  - Do not generate new HabitInstances
  - Remain visible in history

---

### 3.3 HabitSchedule
Defines when a habit is expected.

MVP rules:
- Only DAILY schedules are supported

Fields:
- `habitId`
- `scheduleType = DAILY`
- `startDate`
- `endDate` (nullable)

---

### 3.4 HabitReminder
Defines notification behavior.

Fields:
- `id`
- `habitId`
- `time` (fixed reminder time)
- `isActive`

MVP constraints:
- One reminder per habit
- Reminder fires only if HabitInstance is still PENDING

---

### 3.5 HabitInstance
Represents one habit on one specific day.

Fields:
- `id`
- `habitId`
- `date` (LocalDate)
- `status`:
  - PENDING
  - COMPLETED
  - SKIPPED
  - FAILED
- `completedValue` (nullable)
- `consecutiveSkipsAtCreation`
- `skipLocked` (derived)
- `createdAt`

Rules:
- HabitInstances are immutable once the day passes
- Status transitions are strictly controlled

---

### 3.6 HabitCompletionEvent
Represents user actions.

Fields:
- `id`
- `habitInstanceId`
- `timestamp`
- `deltaValue`
- `source`:
  - IN_APP
  - NOTIFICATION

All derived state must be recomputable from events.

---

## 4. Daily Engine

### 4.1 Daily Habit Generation
Triggered by:
- Daily background job
- App launch (if job hasn’t run)

Steps:
1. Check timezone and update User if needed
2. Determine "today" (LocalDate)
3. Generate HabitInstances for all:
   - Active
   - Non-archived
   - Scheduled habits
4. Cancel daily job if generation already occurred

---

### 4.2 End-of-Day Processing (Failure)

When day changes:
- All PENDING HabitInstances from previous day → FAILED
- SKIPPED remains SKIPPED
- COMPLETED remains COMPLETED

This is triggered by:
- Daily job
- OR app launch (whichever happens first)

No user notification is shown.

---

## 5. User Actions & Business Rules

### 5.1 Complete Habit

Action allowed if:
- HabitInstance.status == PENDING

Effects:
- Create HabitCompletionEvent
- Update status → COMPLETED
- Reset habit’s consecutive skip counter
- Recalculate streaks

---

### 5.2 Skip Habit

Rules:
- A habit can be skipped **up to 2 consecutive days**
- On 3rd consecutive day:
  - Skip is disabled
  - User sees warning

Effects:
- Update status → SKIPPED
- Increment consecutive skip counter
- Does NOT increment streak

---

### 5.3 Snooze Habit

Rules:
- Snooze duration ≤ 60 minutes
- Snooze count limited per user settings
- Snooze survives app restarts

Effects:
- Reschedule next reminder
- Does not change HabitInstance status

---

### 5.4 Undo Action

Availability:
- Controlled by User.undoPolicy

Undo behavior:
- Reverts HabitCompletionEvents
- Recalculates:
  - HabitInstance status
  - Consecutive skips
  - Streaks

Undo never affects archived habits differently.

---

## 6. Streak System

### 6.1 Per-Habit Streaks (Primary)

Each habit tracks:
- `currentStreak`
- `longestStreak`

Rules:
- Incremented when HabitInstance is COMPLETED
- Reset when HabitInstance is FAILED
- SKIPPED does not increment or reset

---

### 6.2 Perfect Days (Secondary)

A day is considered "Perfect" if:
- No HabitInstances have FAILED
- Archived habits are ignored

Perfect days:
- Are tracked for statistics
- Feed calendar visualization
- Do NOT override per-habit streaks

---

## 7. Calendar & History View

Each day is classified as:
- PERFECT (no failures)
- PARTIAL (some completed/skipped)
- FAILED (≥1 failure)

Calendar is read-only.
Undo rules still apply globally.

---

## 8. Notifications

### 8.1 Daily Summary Notification

- Fired at user-defined time
- Lists today’s habits
- Opens Today screen

---

### 8.2 Habit Reminder Notification

- Fired at habit’s reminder time
- Contains actions:
  - Complete
  - Skip
  - Snooze

Notification actions invoke domain logic directly.

---

## 9. Persistence & Architecture

- Local-only persistence
- Event-based model
- IDs are UUIDs
- Domain logic lives in shared KMP module
- Platform layers handle:
  - Storage (Room)
  - Notifications
  - Background jobs

---

## 10. Non-Goals (Explicit)

The MVP does NOT include:
- XP or leveling
- Achievements UI
- Social sharing
- Cloud sync
- AI suggestions

---

## 11. MVP Acceptance Criteria

The MVP is complete when:
- User can define habits
- Habits generate daily
- Notifications fire and actions work
- Snooze/skip/undo behave correctly
- Streaks update accurately
- Calendar reflects history truthfully
- Timezone changes do not corrupt data

---

**End of Specification**

## Appendix A – Quantitative Habits & End-of-Day Failure Clarification (MVP)

This section clarifies and supersedes any earlier definitions related to quantitative habits, reminders, and failure timing.

---

### A.1 Quantitative Habits

Quantitative habits represent habits that are expected to be performed **multiple times per day**, with progress accumulated over the course of the day.

Examples:
- Drink water
- Walk
- Read
- Stretch

Quantitative habits are still evaluated **per day**, not per event.

---

#### A.1.1 Daily Accumulation Model

For each quantitative habit:

- Exactly **one HabitInstance** is created per day
- Progress is accumulated via **multiple HabitCompletionEvents**
- Each event contributes a `deltaValue` to the day’s total

The HabitInstance stores:
- `completedValue` – the sum of all delta values for that day
- `targetValue` – copied from the Habit definition at creation time

---

#### A.1.2 Completion Rules

A quantitative HabitInstance is automatically marked as `COMPLETED` when:

- `targetValue != null` **and** `completedValue >= targetValue`, or
- `targetValue == null` **and** at least one completion event exists

Users do **not** need to manually “mark complete”.

Once completed:
- Further completion events are ignored
- All reminders for that habit on that day are cancelled

---

#### A.1.3 Undo Behavior

Undoing quantitative progress:
- Removes or negates one or more completion events
- Recalculates `completedValue`
- If `completedValue` falls below the completion threshold:
    - HabitInstance reverts to `PENDING`

Undo always triggers a full recalculation of derived state.

---

### A.2 Periodic Reminders for Quantitative Habits

Quantitative habits typically use **PERIODIC reminders**.

Periodic reminders are defined by:
- `intervalMinutes`
- Optional `startTime`
- Optional `endTime`

Rules:
- Reminders repeat during the day at the defined interval
- Reminders stop immediately once the habit is `COMPLETED`
- Reminder actions may include:
    - Increment progress (e.g. +1)
    - Snooze
    - Skip

Reminder scheduling must survive:
- App restarts
- Device reboots

---

### A.3 End-of-Day Failure Processing (Grace Model)

Failure is **not immediate at midnight**.

Failure follows a two-step process.

---

#### A.3.1 Step 1 – Grace Notification

After local midnight:

- If there are HabitInstances from the previous day with status `PENDING`:
    - A notification is sent to the user:
      > “You have uncompleted habits from yesterday. They will be marked as failed if not completed.”
- No state change occurs at this stage
- Users may still complete the habits

---

#### A.3.2 Step 2 – Failure Resolution

When the daily job runs (or when the app is launched, whichever happens first):

- All remaining `PENDING` HabitInstances from the previous day are marked as `FAILED`
- `COMPLETED` and `SKIPPED` instances remain unchanged

This ensures:
- Users are warned before failure
- Failure is deterministic and enforced
- No failure occurs silently

---

### A.4 Impact on Streaks

- **Per-habit streaks**
    - Increment only on `COMPLETED`
    - Reset only on `FAILED`
    - `SKIPPED` neither increments nor resets

- **Perfect-day streaks**
    - A day is considered perfect if **no habits FAILED**
    - `SKIPPED` is allowed
    - Any `FAILED` habit breaks the streak

---

### A.5 Summary

- Quantitative habits accumulate progress across multiple events per day
- Completion is automatic and threshold-based
- Periodic reminders are the default for quantitative habits
- Failure is delayed, warned, and enforced by a daily job
- All derived values are recalculated on undo
