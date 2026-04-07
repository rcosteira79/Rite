# Flexible Weekly Habits

## Problem

Rite currently supports two scheduling modes: DAILY (every day) and WEEKLY (specific days of the week). There's no way to create a habit like "Exercise 3x/week" where the user can complete it on any day — they must pick which days up front.

## Solution

Add a new `FLEXIBLE_WEEKLY` schedule type that represents weekly habits without assigned days. The user sets a quota (e.g., 3 times per week) and can complete it on any day within the week.

## Domain Model

### ScheduleType

Add `FLEXIBLE_WEEKLY` to the existing enum:

```kotlin
enum class ScheduleType {
    DAILY,
    WEEKLY,
    FLEXIBLE_WEEKLY
}
```

### HabitSchedule

- `FLEXIBLE_WEEKLY` sets `specificDays = null` (no days to store)
- `isActiveOn()` returns `true` every day for `FLEXIBLE_WEEKLY` (active all week)
- `weekStartDay` and `quota` are used the same as `WEEKLY`

### HabitInstance

No changes. `FLEXIBLE_WEEKLY` uses the same instance model as `WEEKLY`:
- One instance per week, `date = weekStart`
- `targetValue = quota`
- `completedValue` accumulates across the week

### Database

No migration needed. `HabitSchedule.scheduleType` is a TEXT column — `'FLEXIBLE_WEEKLY'` is just a new string value. `EntityMappers` already uses `ScheduleType.valueOf()`.

## Instance Generation (GenerateDailyHabits)

`FLEXIBLE_WEEKLY` follows the same code path as `WEEKLY`:

1. Calculate week start from today + `schedule.weekStartDay`
2. Check for existing instance at `(habitId, weekStart)`
3. If none exists, create one with `date = weekStart`, `targetValue = quota`
4. Increment `expectedCompletions` by `quota`

The difference is in `isActiveOn()`: `WEEKLY` checks `specificDays` against today's day-of-week, while `FLEXIBLE_WEEKLY` always returns true. This means `GenerateDailyHabits` will always create/surface the instance regardless of the day.

Created mid-week: the instance is created with `date = weekStart` (earlier in the week) and the full quota. The first week is a short week — accepted behavior, same as existing `WEEKLY`.

## Today Screen

### Section restructure

Current layout:
- **Today's Focus**: daily habits only
- **Weekly Goals**: all weekly habits

New layout:
- **Today's Focus**: daily habits + fixed weekly habits scheduled for today (WEEKLY)
- **Weekly Goals** (labeled "This week"): flexible weekly habits only (FLEXIBLE_WEEKLY)

### Resolved habits

Follow the same split:
- Fixed weekly completions go to resolved daily section
- Flexible weekly completions go to resolved weekly section

### TodayViewModel changes

- `loadTodayHabits()` mapping: when `scheduleType == WEEKLY`, treat the instance like a daily habit for display grouping purposes
- `TodayHabitUiModel`: add a way to distinguish flexible weekly (e.g., `isFlexibleWeekly` property) for the "This week" label

## Habit Form UI

When the user selects "Weekly", a new toggle appears before the day picker:

- **"Specific days"** pill → shows current `SchedulePicker` with day chips and presets, sets `scheduleType = WEEKLY`
- **"Any day"** pill → hides day picker, shows only quota field, sets `scheduleType = FLEXIBLE_WEEKLY`

### Validation

- `WEEKLY`: requires `selectedDays.isNotEmpty()` (unchanged)
- `FLEXIBLE_WEEKLY`: no day selection required, only quota

### Editing

- Existing `FLEXIBLE_WEEKLY` habit: "Any day" pill pre-selected
- Existing `WEEKLY` habit: "Specific days" pill pre-selected
- Switching between the two clears/restores day selection as appropriate

## ProcessEndOfDay

### DAILY (unchanged)

Mark yesterday's pending instances as FAILED.

### WEEKLY (fixed days) — updated

Currently evaluates at week boundary (`today.dayOfWeek == weekStartDay`). This delays failure detection unnecessarily — if a habit is Mon/Wed/Fri with quota 3 and the user only completed 2 by end of Friday, it shouldn't wait until Monday.

New behavior: evaluate the day after the last `specificDay` in the week. For Mon/Wed/Fri with weekStartDay=MONDAY, the last specific day is Friday, so check on Saturday. If the last specific day is the day before `weekStartDay` (i.e., Sunday when weekStartDay=MONDAY), the check happens at the week boundary as before. If `completedValue < quota`, mark as FAILED. If `completedValue >= quota`, mark as COMPLETED.

### FLEXIBLE_WEEKLY

Evaluate at week boundary (`today.dayOfWeek == weekStartDay`). Since any day counts, the full week is the window. Same check: `completedValue < quota` → FAILED, otherwise → COMPLETED.

## Streaks

Week-over-week evaluation, same as existing `WEEKLY` logic. Did the user meet quota this week? Streak continues. Completions beyond quota don't extend the streak (overachieving — will count toward score in a future iteration).
