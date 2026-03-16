# Today Screen UI Refresh — Design Spec

**Date:** 2026-03-16
**Branch:** feature/ui-ux-improvements
**Files changed:** `TodayScreen.kt`, `TodayState.kt`, `TodayViewModel.kt`
**Scope:** UI layer only — no domain, data layer, or navigation changes.

---

## Problem

The current Today screen has several UX shortcomings:

- No date shown — users can't confirm at a glance which day they're looking at
- The primary action (complete a habit) is a small icon button with low visual weight
- No sense of daily progress — unclear how many habits remain to action
- Completed habits stay at full visual weight, cluttering the list
- Section headers are plain coloured text with no count

---

## Design

### 1. TopAppBar

Keep the existing `TopAppBar`. Replace the `title` slot with a two-line custom composable:

- **Line 1:** "Today" in `titleLarge` style
- **Line 2 — subtitle:** Shown only when `!state.isLoading && (state.dailyTotal > 0 || state.weeklyTotal > 0)`:
  - `"${state.pendingCount} ${if (state.pendingCount == 1) "habit" else "habits"} to go"` in `MaterialTheme.colorScheme.primary` when `state.pendingCount > 0`
  - `"All done for today 🎉"` in `MaterialTheme.colorScheme.tertiary` when `state.pendingCount == 0`

When `state.habits.isNotEmpty()` but `state.dailyTotal == 0 && state.weeklyTotal == 0` (all habits suspended), the subtitle is hidden — the TopAppBar shows only "Today".

The `CalendarMonth` and `Settings` icon buttons remain in the `actions` slot, unchanged.

### 2. Screen structure and progress ring row placement

The existing outer `Column` inside `Scaffold` contains:

```
TimezoneWarningBanner  ← outside the when block, unchanged
when {
    isLoading  → CircularProgressIndicator
    habits.isEmpty()  → EmptyHabitsMessage
    else  → LazyColumn
}
```

The ring row is added **inside the `else` branch**, directly above the `LazyColumn`. The `TimezoneWarningBanner` remains outside the `when` block, unchanged. Updated `else` branch:

```
else {
    ProgressRingRow(state)   ← new, above the list
    LazyColumn { ... }
}
```

`ProgressRingRow` is guarded at the call site in `TodayScreen` with `if (state.dailyTotal > 0 || state.weeklyTotal > 0)` — the composable itself has no internal guard.

**Ring row `Row` modifier:** `Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)` with `horizontalArrangement = Arrangement.spacedBy(10.dp)` (default `Alignment.Start`, so a lone chip is left-aligned).

It contains up to two pill-shaped chips. A chip is rendered only when its `total > 0`. When both chips are present, each uses `Modifier.weight(1f)`. When only one chip is present, it carries no weight modifier.

**Chip structure:** each chip is a `Surface` with `shape = RoundedCornerShape(50)` (fully rounded pill) and `color = MaterialTheme.colorScheme.surfaceVariant`. Inside: a `Row(verticalAlignment = Alignment.CenterVertically)` with `padding(horizontal = 10.dp, vertical = 6.dp)` and `horizontalArrangement = Arrangement.spacedBy(8.dp)` containing:

1. A `28.dp × 28.dp` square `Canvas` arc ring (see geometry below)
2. A `Column` with two stacked `Text` elements:
   - `"X / Y"` in `labelLarge` (e.g. `"2 / 3"`) — uses spaces around slash for readability, consistent with section headers
   - Cadence label ("Daily" / "Weekly") in `labelSmall`

**Arc ring geometry:**
- `Canvas` size: `28.dp × 28.dp`, square
- Stroke width: `5.dp`
- Ring radius: `(min(size.width, size.height) - strokeWidthPx) / 2`, centred in the canvas
- Background track: full `360°` arc in `surfaceContainerHighest` colour
- Filled arc: starts at `−90°` (12 o'clock), sweeps clockwise, `StrokeCap.Round`

| Chip | Ring colour — incomplete | Ring colour — complete |
|---|---|---|
| Daily | `primary` | `tertiary` |
| Weekly | `secondary` | `tertiary` |

"Complete" means `completed == total` (definitions in §3). `tertiary` appears in three places — the "All done" subtitle, a completed ring, and the Suspended section header — intentionally: all three convey "resolved / passive".

### 3. TodayState — new fields

Add the following fields to `TodayState`:

```kotlin
val pendingCount: Int = 0
val dailyCompleted: Int = 0
val dailyTotal: Int = 0
val weeklyCompleted: Int = 0
val weeklyTotal: Int = 0
```

**Definitions** (`ScheduleType` has exactly two values — `DAILY` and `WEEKLY`):

```
pendingCount    = habits.count { !it.isSuspended && it.status == PENDING }

dailyTotal      = habits.count { it.isDaily && !it.isSuspended }
dailyCompleted  = habits.count { it.isDaily && !it.isSuspended
                                 && it.status in {COMPLETED, SKIPPED, FAILED} }

weeklyTotal     = habits.count { it.isWeekly && !it.isSuspended }
weeklyCompleted = habits.count { it.isWeekly && !it.isSuspended
                                 && it.status in {COMPLETED, SKIPPED, FAILED} }
```

`isSuspended` on `TodayHabitUiModel` maps to `status == HabitStatus.SUSPENDED`. A suspended habit cannot simultaneously have any other status.

`FAILED` habits count toward `dailyCompleted`/`weeklyCompleted` but not toward `pendingCount`.

All five fields default to `0`. The subtitle and ring row are gated on `!isLoading && (dailyTotal > 0 || weeklyTotal > 0)`, and `isLoading` defaults to `true`, so the zero defaults are never shown.

**ViewModel:** Compute the five values from `habits` immediately after the `mapNotNull` block in `loadTodayHabits()`, then include them in the **final** `_state.update` call that sets `habits` and `isLoading = false`. The intermediate timezone `_state.update` runs before `getInstancesForDate` is called and is unaffected.

### 4. HabitCard — left accent border

Add a 3.dp leading accent bar as the **first child** of the existing card `Box` — inserted before the `Column` so it renders behind all other card content. Use `Modifier.matchParentSize().width(3.dp).align(Alignment.TopStart)`. `matchParentSize()` is a `BoxScope`-only modifier that sizes the bar to the `Box`'s measured dimensions (driven by the `Column`), avoiding the runtime crash that `fillMaxHeight()` would cause inside an unbounded `LazyColumn` item.

Because `Card` clips its content to its corner shape by default (`RoundedCornerShape(12.dp)` in MD3), the bar's top-left and bottom-left corners are automatically rounded to match — no additional clipping needed.

Change the content `Column`'s existing uniform `padding(16.dp)` to `padding(start = 19.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)` to prevent content overlapping the bar.

Colour by cadence and status:

| Cadence | Status | Accent colour |
|---|---|---|
| Daily | PENDING | `primary` |
| Daily | COMPLETED / SKIPPED / FAILED | `primary.copy(alpha = 0.35f)` |
| Weekly | PENDING | `secondary` |
| Weekly | COMPLETED / SKIPPED / FAILED | `secondary.copy(alpha = 0.35f)` |
| Any | SUSPENDED | `surfaceVariant` |

### 5. HabitCard — pending action buttons

Replace `IconButton(Icons.Default.Check)` with:

```kotlin
OutlinedButton(
    onClick = onCompleteClick,
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
) {
    Icon(Icons.Default.Check, contentDescription = "Complete")
}
```

Reduced `contentPadding` keeps the button compact. The `TextButton("Skip")` to its left is unchanged.

### 6. HabitCard — card colour and alpha for resolved states

Remove the `.copy(alpha = ...)` modifiers from the existing `cardColor` expressions. Apply `Modifier.alpha(0.65f)` to the entire `Card` composable for `COMPLETED`, `SKIPPED`, and `FAILED` statuses. For `PENDING` and `SUSPENDED`, no alpha modifier is applied.

Base `containerColor` values (full opacity) remain:

| Status | containerColor |
|---|---|
| COMPLETED | `primaryContainer` |
| SKIPPED | `surfaceVariant` |
| FAILED | `errorContainer` |
| SUSPENDED | `secondaryContainer` |
| PENDING | `surface` |

The following card content is unchanged: strikethrough on habit name, undo `IconButton` for COMPLETED/SKIPPED, `"Failed"` text label for FAILED, `"Suspended"` text label for SUSPENDED.

### 7. Section headers

Replace the existing `Text` (which uses `titleMedium` style) with a `Row(modifier = Modifier.fillMaxWidth())`:

- **Leading:** section label in `labelSmall` style, `fontWeight = FontWeight.Bold`, `letterSpacing = 0.8.sp`, uppercase, coloured by cadence (`primary` / `secondary` / `tertiary`) — matching existing colours
- **Trailing:** `"X / Y"` count (with spaces around slash) in `labelSmall` style, `onSurfaceVariant` colour, sourced from `TodayState`:
  - Daily: `"${state.dailyCompleted} / ${state.dailyTotal}"` (section only renders when `dailyHabits.isNotEmpty()`, guaranteeing `dailyTotal > 0`)
  - Weekly: `"${state.weeklyCompleted} / ${state.weeklyTotal}"` (section only renders when `weeklyHabits.isNotEmpty()`, guaranteeing `weeklyTotal > 0`)
  - Suspended: no trailing count

---

## State transitions summary

| Subtitle | Condition |
|---|---|
| `"N habit(s) to go"` (primary) | `!isLoading && (dailyTotal > 0 \|\| weeklyTotal > 0) && pendingCount > 0` |
| `"All done for today 🎉"` (tertiary) | `!isLoading && (dailyTotal > 0 \|\| weeklyTotal > 0) && pendingCount == 0` |
| Hidden | `isLoading`, or `dailyTotal == 0 && weeklyTotal == 0` |

| Ring row | Condition |
|---|---|
| Visible | `!isLoading && (dailyTotal > 0 \|\| weeklyTotal > 0)` |
| Hidden | `isLoading`, or both totals are 0 |

| Ring stroke | Condition |
|---|---|
| Cadence colour (`primary` / `secondary`) | `completed < total` |
| `tertiary` | `completed == total` |
| Chip hidden | `total == 0` |

---

## Out of scope

- HabitDetail screen (stub remains)
- `QuantitativeInputBottomSheet`
- Timezone warning banner
- FAB
- Any other screen (Calendar, Settings, HabitForm, Archived)
- Domain layer, data layer, navigation

---

## Files changed

| File | Change |
|---|---|
| `TodayScreen.kt` | Two-line TopAppBar title, `ProgressRingRow` composable, section headers, `HabitCard` accent bar + `OutlinedButton` + alpha |
| `TodayState.kt` | Add 5 count fields with default `0` |
| `TodayViewModel.kt` | Compute and populate the 5 new fields in the final `_state.update` of `loadTodayHabits()` |
