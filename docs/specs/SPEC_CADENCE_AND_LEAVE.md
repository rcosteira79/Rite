# HabitLock – MVP Specification (Updated with Cadence & Leave Mode)

## 1. Core Concepts

### Habit
- **Name**: required
- **Description**: optional
- **Units**: optional, for quantitative habits
- **Cadence**: DAILY | WEEKLY
- **Quota**: number of required completions per cadence window
- **Notifications**: preferred times per cadence window
- **Leave Mode**: optional; temporarily suspend selected habits for a date range

### HabitInstance
- Represents a single habit occurrence due in a specific cadence window
- Status: `PENDING`, `COMPLETED`, `SKIPPED`, `SUSPENDED`

### User Model
- **Strictness preset**: Flexible, Balanced, Locked
- **Undo policy**: none, today-only, all history
- **Snooze policy**: limit per habit, duration
- **Streaks**: per-habit (daily/weekly) and perfect-day (daily only)

---

## 2. Habit Types & Cadence

| Habit Type               | Cadence | Quota | Notifications                     | Action Buttons                             |
|---------------------------|--------|-------|-----------------------------------|-------------------------------------------|
| Daily, single             | DAILY  | 1     | user-defined daily time           | ✅ Complete / ⏰ Snooze / ❌ Skip          |
| Daily, quantitative       | DAILY  | N     | user-defined daily time           | ➕ +1 / ⏰ Snooze / ❌ Skip (optional +2)  |
| Non-daily / weekly        | WEEKLY | N     | user-defined days/times           | ➕ +1 / ⏰ Snooze / ❌ Skip today           |
| Leave Mode                | Any    | -     | none                              | N/A (status: SUSPENDED)                   |

**Notes:**
- Quota can be exceeded; over-completion is encouraged and updated at the end of the cadence window.
- Notifications for weekly/non-daily habits show current progress: `<completed> / <quota> this week`.
- “Skip today” only skips the current notification instance; it does not skip the habit itself.

---

## 3. Notifications

### 3.1 Daily habits
**Title:** `<Habit name>`  
**Body:** `<completed> / <quota> today`  
**Action Buttons:** Complete / Snooze / Skip

### 3.2 Weekly habits
**Title:** `<Habit name>`  
**Body:** `<completed> / <quota> this week`  
Optional secondary line: `Week ends Sunday` or `X remaining`  
**Action Buttons:** +1 (optionally +2), Snooze, Skip today

### 3.3 Grace-period notifications
- Triggered after cadence window ends (daily: after midnight; weekly: Sunday 23:59)
- **Title:** `Uncompleted habits`
- **Body:** `Complete them or they will be marked as failed`
- No action buttons (informational only)

### 3.4 Leave Mode
- Suspended habits do not generate notifications
- App shows suspended habits in the Today view with `SUSPENDED` status

---

## 4. Completion Rules
- Completing a habit increments progress in the current cadence window
- Skipping affects only the current instance (notification), subject to skip limits
- Undo allowed according to user settings; recalculates streaks and derived progress
- **Over-completion:**
    - Users can log more completions than the quota
    - At the end of the cadence window, prompt user:  
      `"You did X completions. Would you like to update this habit's quota?"`
    - Quota adjustment affects future cadence windows only

---

## 5. Failure Rules
- Daily habits: fail after the day ends if `PENDING`
- Weekly habits: fail at the end of the week if `completed < quota`
- Leave Mode: suspended habits are excluded from failure calculations

---

## 6. Streaks
- **Per-habit streaks:** incremented if all habit instances in the cadence window are completed; reset only on failure
- **Perfect-day streaks:** only counts daily habits; reset on any daily habit failure

---

## 7. Leave Mode
- Users can activate temporary suspension for selected habits
- Requires start and end dates
- Suspended habits:
    - Do not generate notifications
    - Do not count toward streaks or failures
    - Are marked `SUSPENDED` in history

---

## 8. Undo / Snooze / Skip Rules
- Undo: optional, per user settings
- Snooze: configurable per habit (max 60 minutes)
- Skip: limited per habit according to strictness rules; “Skip today” only affects that notification instance

---

## 9. Notifications UX Principles
- Numbers > words: show progress (`2 / 3 this week`)
- Cadence clarity: “today” for daily, “this week” for weekly/non-daily
- Action buttons consistent: +1 increments, Skip only skips notification, Snooze delays reminder
- No preemptive failure language: only grace-period notifications mention potential failure

---

## 10. MVP Scope Notes
- Quantitative habits with multiple units/day included
- Weekly/non-daily habits with user-selected reminder times included
- Leave / temporary suspension mode included
- Over-completion supported with end-of-window quota adjustment
- Undo / snooze / skip functionality extended to weekly and non-daily habits

---

## 11. Today + Weekly View Mockup (conceptual)

[ Today Screen ]

DAILY HABITS

[ ] Drink water 3 / 8 today (+1) Snooze Skip
[x] Take meds 2 / 2 today (Completed)

WEEKLY HABITS

[ ] Gym 1 / 3 this week (+1) Snooze Skip today
[ ] Cycling 0 / 1 this week (+1) Snooze Skip today

SUSPENDED HABITS

[ ] Yoga SUSPENDED until Fri
[ ] Morning run SUSPENDED until Thu


**Legend:**
- `[ ]` Pending, `[x]` Completed
- `SUSPENDED` = Leave Mode active
- `(+1)` = notification action button to increment progress
- `Snooze` / `Skip today` = notification-aligned actions

**Behavior Notes:**
- Users can interact with each habit card to update progress if outside notifications
- Weekly habits show progress clearly; last reminder indicates “X remaining” before end of week
- Daily quantitative habits allow multiple increments (+1, optional +2)

