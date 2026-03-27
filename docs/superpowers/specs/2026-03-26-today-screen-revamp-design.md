# Today Screen Revamp — Design Spec

**Date:** 2026-03-26
**Branch:** feature/today-screen-revamp (from feature/habit-form-revamp)
**Design source:** `design system/today_dark_mode_expanded/`, `today_dark_mode_collapsed/`, `today_unified_completed_states_expanded/`, `today_unified_completed_states_collapsed/`

## Overview

Full reimplementation of the Today screen, replacing the current TopAppBar + accent-bar card layout with a motivational collapsing header, redesigned habit cards with expand/collapse interaction, and a standard M3 bottom navigation bar. The existing ViewModel and state management are extended, not rewritten.

## Out of Scope

- Weekly Reflection / insights card (separate feature)
- Animated collapsing toolbar transition (follow-up PR — crossfade only for now)
- Swipe-to-edit/delete on habit cards (follow-up PR)
- Create/edit habit UI for setting `defaultIncrement` (separate brainstorming session)
- Animate title in new/edit habit screens (follow-up PR)

---

## 1. Data & State Changes

### 1.1 Completion Timestamp

Add `completedAt: Instant?` to `HabitInstance`. Set when a habit is completed or skipped, cleared on undo. Persisted in the database.

Surface in `TodayHabitUiModel` as:
- `completedAt: Instant?`
- `completedAtText: String?` — formatted as "07:45 AM"

### 1.1b Default Increment (Quantitative Habits)

Add `defaultIncrement: Int` to `Habit` (quantitative only, defaults to 1). The data model and DB migration are included in this PR so the Today screen can use the value. The create/edit habit UI for setting this field requires its own design iteration and will be handled in a separate brainstorming session. Until then, all habits default to 1.

Surface in `TodayHabitUiModel` as:
- `defaultIncrement: Int` — used for the quick-add button label ("+{defaultIncrement} {unit}")

### 1.2 Motivational Titles

A curated pool of motivational titles displayed in the header, rotating once per day. Implemented as a pure function:

```
fun motivationalTitleForDate(date: LocalDate): String
```

Deterministic (seeded by date) so the same title shows all day. No database or network dependency.

Example titles: "Quiet discipline", "Small steps, big change", "Trust the process", "One day at a time".

### 1.3 TodayState Extensions

New fields:
- `motivationalTitle: String` — from `motivationalTitleForDate()`
- `strictnessPreset: StrictnessPreset` — user's chosen preset (FLEXIBLE / BALANCED / LOCKED)

Retained fields:
- `dailyResolved: Int`, `dailyTotal: Int` — used for the progress ring (daily only)
- `pendingCount: Int` — used for subtitle

Removed fields:
- `weeklyResolved`, `weeklyTotal` — no longer displayed (ring is daily-only)

### 1.4 Status Badge Logic

| Habit state | Badge text | Undo available? |
|---|---|---|
| Binary, pending | PENDING | No |
| Quantitative, completedValue == 0 | PENDING | No |
| Quantitative, completedValue > 0 & not complete | IN PROGRESS | Yes (reverts last increment) |
| Completed | (resolved row) | Yes |
| Skipped | (resolved row) | Yes |
| Failed | (resolved row, muted) | No |
| Suspended | Not shown | N/A |

---

## 2. Collapsing Header

Replaces the current `TopAppBar`. Uses a user-provided `DynamicCollapsingToolbar` composable with two content slots.

### 2.1 Expanded State (scroll at top)

Layout: left column + right progress ring.

Left column (top to bottom):
- **Motivational title** — Manrope extrabold, ~28sp, primary color, multi-line
- **Pending count subtitle** — "X habits remaining today", on-surface-variant
- **Strictness preset pill** — rounded pill with pulsing dot + preset name (e.g., "Balanced"), primary-container background

Right side:
- **Daily progress ring** — 88dp, Canvas arc, stroke width 5dp, rounded caps. Shows `dailyResolved / dailyTotal` as percentage. Label: "Day" below the percentage.

### 2.2 Collapsed State (scrolled)

Single row, space-between:
- Left: single-line motivational title (Manrope extrabold, ~18sp) + strictness pill inline
- Right: percentage text (extrabold) + "Done" label below — no ring

### 2.3 Transition

**This PR:** crossfade between expanded and collapsed.

**Follow-up PR animation:** Ring + percentage move to collapsed position faster than other elements. While title/pill settle, the ring arc unfurls into a horizontal line that slides off-screen right. After the line exits, "Done" fades in beneath the percentage.

---

## 3. Habit Cards

Cards start **collapsed**. Tap to expand (accordion — one card expanded at a time). Card background: `surface-container-low`, corner radius: 24dp. Habit names: uppercase, Manrope bold.

### 3.1 Collapsed Pending Card

Compact single row with quick actions on the right.

**Binary:**
- Left: habit name (uppercase) + description below (small, on-surface-variant)
- Right: checkmark button (primary-container bg) + "SKIP" text

**Quantitative:**
- Left: progress counter on top (e.g., "10 / 30 PAGES"), habit name below
- Right: +N button (primary-container bg) + "SKIP" text. Shows "+{defaultIncrement} {unit}" (e.g., "+1 PAGE", "+250 ML"). Increment comes from `Habit.defaultIncrement`.
- Below: thin progress bar (3dp height)

### 3.2 Expanded Pending Card

Full card with prominent action buttons.

**Binary:**
- Header: description (small, uppercase, on-surface-variant) + habit name (large, uppercase) + status badge (top-right)
- Action row: COMPLETE button (flex, primary bg) + SKIP button
- No undo button (nothing to undo)

**Quantitative:**
- Header: progress counter (primary color, bold) + "/ target UNIT" + habit name + status badge
- Progress bar (8dp height)
- Action row: +N UNIT button (flex, primary bg, uses `defaultIncrement`) + CUSTOM button (opens bottom sheet for custom amount) + SKIP button
- Undo button: only shown when `completedValue > 0` (reverts last increment)
- Badge: "IN PROGRESS" when `completedValue > 0`, "PENDING" otherwise

### 3.3 Resolved Habit Row

Distinct compact row for completed/skipped habits. Visually separated from pending cards by a thin divider.

- Background: `primary-container` at 80% opacity, corner radius 16dp
- Layout: check icon circle (40dp, primary-container tinted) + name (uppercase, bold) + subtitle + undo button
- Subtitle varies by status and type:
  - Completed binary: "COMPLETED AT 07:45 AM" (from `completedAtText`)
  - Completed quantitative (weekly with quota): "GOAL REACHED: 3 / 3 TIMES"
  - Skipped: "SKIPPED AT 07:45 AM"
  - Failed: "FAILED" (no timestamp)
- Undo button: present for completed and skipped, absent for failed

### 3.4 Failed / Suspended

- Failed: same compact row as resolved, but muted colors. Status label only, no undo.
- Suspended: not shown on the Today screen.

---

## 4. List Sections

Content displayed in a `LazyColumn` with two sections:

### 4.1 "Today's Focus" Section

- Header: "Today's Focus" (Manrope bold, primary) + date label right-aligned (e.g., "May 24", uppercase, on-surface-variant)
- Content order: pending daily habits (collapsed cards) → thin separator → resolved daily habits (compact rows)

### 4.2 "Weekly Goals" Section

- Header: "Weekly Goals" (Manrope bold, primary) + "This Week" right-aligned
- Content order: pending weekly habits (collapsed cards) → thin separator → resolved weekly habits (compact rows)

### 4.3 Empty State

Centered message + CTA button to create first habit. Reuses existing `EmptyHabitsMessage` pattern.

---

## 5. Bottom Navigation & FAB

### 5.1 NavigationBar

Standard M3 `NavigationBar` with three tabs:
- **Today** → `Today` route
- **History** → `Calendar` route
- **Settings** → `Settings` route

Active tab uses M3 indicator pill with filled icon. Icons sourced from composables.com/icons.

Lives in `HabitLockNavigation`'s `Scaffold`, wrapping all routes. Replaces the current TopAppBar calendar/settings icon buttons.

### 5.2 Scroll-Aware Hide/Show

The NavigationBar hides (slides down) when the user scrolls down and reappears when:
- The user scrolls up, OR
- The user reaches the bottom of the LazyColumn

Implemented via `NestedScrollConnection` tracking scroll direction.

### 5.3 FAB

Standard M3 `FloatingActionButton`, positioned above the NavigationBar (right-aligned). Navigates to `CreateHabit` route. Stays visible at all times (does not hide with the nav bar).

`Scaffold` handles the padding between FAB and NavigationBar automatically.

---

## 6. Theming & Typography

### Fonts
- **Headlines / titles:** Manrope (extrabold for header title, bold for section headers and habit names)
- **Body / labels:** Inter (medium for subtitles, bold for badges and labels)

### Dark Theme Colors (from design system)
- Primary: `#a9cfba`
- Primary container: `#2d4f3f`
- Surface: `#141312`
- Surface container low: `#1c1b1a`
- Surface container highest: `#363433`
- On-surface: `#e6e1e0`
- On-surface-variant: `#c1c8c2` (used as `#8b938d` in designs — check actual M3 theme values)
- Outline variant: `#3f4943`

### Light Theme Colors (from design system)
- Primary: `#163829`
- Primary container: `#2d4f3f`
- Surface: `#fdf9f6`
- Surface container low: `#f7f3f0`
- On-surface: `#1c1b1a`
- On-primary-container (for resolved rows): `#9ac0ab` (text), white (icons)

Note: Colors should be consumed via `MaterialTheme.colorScheme` tokens, not hardcoded. The design system HTML files provide reference values for validating the theme.

---

## 7. Approach

**Incremental refactor** — evolve the existing screen in place, one logical change per commit. Review by navigating commits.

Commit sequence (approximate):
1. Add `completedAt` to domain model, DB, and repository
2. Add `defaultIncrement` to Habit model and DB (defaults to 1, no UI yet)
3. Add motivational titles pool + pure function
4. Add strictness preset to TodayState + ViewModel
5. Rewrite header (collapsing toolbar with crossfade)
6. Rewrite habit cards (collapsed + expanded pending states)
7. Add resolved habit rows (completed/skipped/failed)
8. Restructure LazyColumn sections ("Today's Focus" / "Weekly Goals")
9. Add NavigationBar + FAB to HabitLockNavigation scaffold
10. Add scroll-aware nav bar hide/show
11. Update quantitative input: inline +N on collapsed, Custom button on expanded → bottom sheet
12. Clean up removed code (old TopAppBar, old HabitCard, old ProgressRingRow)
