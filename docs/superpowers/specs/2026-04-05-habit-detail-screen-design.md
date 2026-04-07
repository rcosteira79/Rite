# Habit Detail Screen — Design Spec

**Date:** 2026-04-05
**Design source:** `design system/habit_detail_dark_mode/`, `design system/habit_detail_read_30_pages_corrected/`

## Overview

A detail screen for individual habits, accessible by tapping a habit card on the Today screen. Shows today's progress, streaks, Habit Score, accountability limits, a 3-month completion heatmap, and action buttons. Replaces the current expand-on-tap behavior for habit cards.

## Navigation

- **Route:** `HabitDetail(instanceId: String)` — already defined, currently auto-pops
- **Entry point:** Tap a habit card on the Today screen (replaces expand behavior)
- **Back:** Navigates back to Today screen. Today screen reloads habits on return (same pattern as HabitForm).

### Route Parameter

The route takes `instanceId`. The ViewModel loads the `HabitInstance` by ID, then fetches the parent `Habit` by `habitId` for streaks, score, type, and historical instances.

## Screen Layout

### Top Bar

Standard `TopAppBar` with back navigation icon only — no title text.

### Header

- **Category label:** "BINARY RITUAL" or "QUANTITATIVE PURSUIT" — `labelSmall`, uppercase, `onSurfaceVariant`, letter-spacing 0.1em
- **Habit name** — `headlineLarge`, `FontWeight.ExtraBold`, `onSurface`, uppercase

### Progress Ring (quantitative only)

- Circular progress ring showing today's completion: "{completedValue} OF {targetValue} {UNIT}"
- Same style as the Today screen header ring (Canvas arc, 88dp, stroke 5dp, rounded caps)
- For binary habits: show a large check icon (completed) or a pending icon (not completed) instead

### Stats Row

Three items in a row with equal weight:

| Stat | Source |
|---|---|
| Current Streak | `Habit.currentStreak` |
| Longest Streak | `Habit.longestStreak` |
| Habit Score | `Habit.calculateScore().percentage` |

Each stat: value in `titleLarge` bold, label in `labelSmall` `onSurfaceVariant` below.

### Accountability Limits

Single line showing remaining skips: "{N} skips remaining" with a skip icon. Derived from the User's `maxConsecutiveSkips` setting minus `HabitInstance.consecutiveSkipsAtCreation`.

If skips are unlimited (`maxConsecutiveSkips == null`), show "Unlimited skips".
If skip-locked (`isSkipLocked`), show "No skips remaining".

### Heatmap

- **Section header:** "Last 3 months" — `titleMedium`, `primary`
- **Grid:** GitHub-contribution-style heatmap covering the last ~13 weeks
  - Columns = weeks (most recent on the right)
  - Rows = days of the week (Mon–Sun)
  - Day labels on the left (abbreviated: M, W, F or similar)
- **Cell colors** based on completion intensity:
  - No data / no instance: `surfaceContainerLow` (empty)
  - 0% (failed/missed): `surfaceContainerHigh` (slightly visible, distinct from "no data")
  - 1-49% (partial): light `primary` (~30% alpha)
  - 50-99% (near complete): medium `primary` (~60% alpha)
  - 100%+ (complete/over-complete): full `primary`
  - Skipped: `outlineVariant`
- **Data source:** `HabitInstanceRepository.getInstancesForHabit(habitId)` filtered to last 90 days
- Cell size: ~12dp square with 2dp gap

### Action Buttons

Pinned to the bottom of the screen (same `CtaContainer`-style padding).

**Binary habits:**
- Primary button: "Complete" — calls `CompleteHabit.executeBinary()`
- Secondary text button: "Skip" — calls `SkipHabit`
- If already completed: primary button shows "Completed" (disabled)
- If skipped: skip button shows "Skipped" (disabled)

**Quantitative habits:**
- Primary button: "+{defaultIncrement} {UNIT}" — calls `CompleteHabit.executeQuantitative(defaultIncrement)`
- Secondary button: "Custom" — opens bottom sheet for custom amount input
- Tertiary text button: "Skip" — calls `SkipHabit`
- If already completed (target met): primary button shows "Goal reached" (disabled), Custom still available for over-completion
- If skipped: all action buttons disabled

## ViewModel

New `HabitDetailViewModel` scoped to the screen. Dependencies:
- `HabitRepository`
- `HabitInstanceRepository`
- `UserRepository` (for skip limits)
- `CompleteHabit`
- `SkipHabit`
- `UndoHabit`
- `UndoLastIncrement`

### State

```
HabitDetailState(
    habit: Habit?,
    instance: HabitInstance?,
    maxConsecutiveSkips: Int?,
    heatmapData: List<HeatmapDay>,
    isLoading: Boolean
)

HeatmapDay(
    date: LocalDate,
    completionPercentage: Float, // 0.0 to 1.0+
    status: HabitStatus
)
```

### Loading

On init, given `instanceId`:
1. Load `HabitInstance` by ID
2. Load `Habit` by `instance.habitId`
3. Load `User` for `maxConsecutiveSkips`
4. Load all instances for `habitId`, filter to last 90 days, map to `HeatmapDay`

## Changes to Today Screen

- **Remove expand/collapse behavior** on habit card tap
- Tap navigates to `HabitDetail(instanceId)` instead
- Swipe actions (edit, delete, archive) remain unchanged
- Collapsed card layout remains as-is (the card just becomes non-expandable)

## String Resources

New resource file: `strings_habit_detail.xml`

| Key | Value |
|---|---|
| `habit_detail_category_binary` | BINARY RITUAL |
| `habit_detail_category_quantitative` | QUANTITATIVE PURSUIT |
| `habit_detail_stat_current_streak` | Current Streak |
| `habit_detail_stat_longest_streak` | Longest Streak |
| `habit_detail_stat_habit_score` | Habit Score |
| `habit_detail_stat_days` | Days |
| `habit_detail_skips_remaining` | %1$d skips remaining |
| `habit_detail_skips_unlimited` | Unlimited skips |
| `habit_detail_skips_none` | No skips remaining |
| `habit_detail_heatmap_title` | Last 3 months |
| `habit_detail_action_complete` | Complete |
| `habit_detail_action_completed` | Completed |
| `habit_detail_action_skip` | Skip |
| `habit_detail_action_skipped` | Skipped |
| `habit_detail_action_custom` | Custom |
| `habit_detail_action_goal_reached` | Goal reached |
| `habit_detail_progress` | %1$d of %2$d %3$s |

## Testing

- Screenshot tests for:
  - Binary habit (pending) — light + dark
  - Binary habit (completed) — light + dark
  - Quantitative habit (in progress) — light + dark
  - Quantitative habit (goal reached) — light + dark
- Unit tests for:
  - HeatmapDay mapping from HabitInstance list
  - Skip remaining calculation
  - Action button state (enabled/disabled based on habit status)
  - ViewModel loading flow
