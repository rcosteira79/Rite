# Create / Edit Habit Screen Revamp — Design Spec

**Date:** 2026-03-25
**Status:** Approved

---

## Overview

Full rewrite of `HabitFormScreen` to match the app's established design system (Forest Discipline / Stoic Night). The current screen uses Material3 defaults (radio buttons, `OutlinedTextField`, `TopAppBar`). The revamped screen adopts the same visual language as the onboarding flow: large bold headings, pill controls, list-item rows, and the existing `SchedulePicker` component.

---

## Screen Layout

### Shared structure (create + edit)

```
┌─────────────────────────────────┐
│  HabitLock          [🗑 edit only]│  ← simple top bar, no back arrow
├─────────────────────────────────┤
│  New Habit / Edit Habit          │  ← headlineLarge ExtraBold
│  ▬▬▬▬                           │  ← 36×3dp accent bar (primary colour)
│  Subtitle (create only)          │  ← bodyLarge onSurfaceVariant
├─────────────────────────────────┤
│  HABIT NAME                      │  ← section label (labelSmall, caps)
│  [ text field — underline style ]│
│                                  │
│  TYPE                            │
│  [ Binary ] [ Quantitative ]     │  ← TypeToggle
│                                  │
│  DAILY TARGET                    │
│  ─  1  +  time(s) / day          │  ← QuantityStepper
│  [ UNIT field — quant only ]     │  ← AnimatedVisibility
│                                  │
│  SCHEDULE          Daily Weekly  │  ← inline tab toggle
│  M  T  W  T  F  S  S             │  ← SchedulePicker (reused)
│                                  │
│  ──────────────────────────────  │
│  🔔 Reminder       09:00 AM  ◉  │  ← FormListRow
│  ──────────────────────────────  │
│  ✏  Add Note / Note              │  ← FormListRow (expands)
│  [ inline text field ]           │  ← AnimatedVisibility
│  ──────────────────────────────  │
│                                  │
│  [ Establish Habit / Save ]      │  ← primary button
│    Discard Draft / Changes       │  ← secondary text button
└─────────────────────────────────┘
```

### Create mode

- Title: **New Habit**
- Subtitle present: e.g. *"Define your path to architectural discipline."*
- Primary CTA: **Establish Habit**
- Secondary CTA: **Discard Draft** — navigates back, no habit saved
- No delete icon in top bar

### Edit mode

- Title: **Edit Habit**
- No subtitle
- Outlined trash icon (`Icons.Outlined.Delete`) in top-right of top bar, tinted `error` colour — triggers a confirmation dialog before deleting
- Primary CTA: **Save Changes**
- Secondary CTA: **Discard Changes** — restores all fields to the values loaded at screen entry, navigates back
- Back gesture also discards changes and navigates back

---

## Components

### New: `TypeToggle`

Location: `ui/components/TypeToggle.kt`

Two full-width pills side by side. Selected pill: `primaryContainer` background, `onPrimaryContainer` text, `SemiBold`. Unselected: `surfaceContainerLow` background, `onSurfaceVariant` text; in dark mode adds a `1dp outlineVariant` border.

```
TypeToggle(
    selected: HabitType,
    onSelectionChange: (HabitType) -> Unit,
    modifier: Modifier
)
```

### New: `QuantityStepper`

Location: `ui/components/QuantityStepper.kt`

Row: circular `−` button — bold number — circular `+` button — label string. Minimum value 1. Circular buttons use `surfaceContainerLow` background (`outlineVariant` border in dark). The label is passed in by the caller so it can read *"time(s) / day"*, *"time(s) / week"*, or *"km / day"*.

```
QuantityStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier
)
```

When `HabitType.QUANTITATIVE` is selected, a unit `TextField` appears below the stepper via `AnimatedVisibility` (same enter/exit as `HabitTypeCard` in onboarding: `expandVertically + fadeIn` / `shrinkVertically + fadeOut`).

### New: `FormListRow`

Location: `ui/components/FormListRow.kt`

Horizontal row: 36dp circular icon container (`surfaceContainerLow` bg, `outlineVariant` border in dark) — title + subtitle column — optional trailing slot. A `1dp outlineVariant` divider sits above each row; callers opt out by omitting it.

```
FormListRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailingContent: (@Composable () -> Unit)?,
    modifier: Modifier
)
```

**Reminder row** — trailing slot is a `Switch`. Subtitle shows the reminder time when on ("09:00 AM"), "Off" when off.

**Add Note row** — no trailing slot. Tapping toggles expansion of an inline `TextField` via `AnimatedVisibility`. While expanded, the title changes from "Add Note" to "Note" and the icon tint changes to `onSurface` (active) instead of `onSurfaceVariant` (inactive).

### Reused: `SchedulePicker`

`ui/components/SchedulePicker.kt` — unchanged. Dropped in below the Daily / Weekly inline toggle.

### Inline: Schedule type toggle

Not a separate component. A small two-pill row (`Daily` | `Weekly`) rendered inline in `HabitFormScreen`, right-aligned beside the `SCHEDULE` label. Uses the same pill shape as `TypeToggle` but at `labelSmall` size with `12dp` horizontal padding.

---

## State & ViewModel changes

### `HabitFormState`

Add one field:

```kotlin
val selectedDays: Set<DayOfWeek> = DayOfWeek.entries.toSet()
```

### `HabitFormViewModel`

- Add `private val originalState: HabitFormState` — captured once when the habit finishes loading (edit mode only). Used by `discardChanges()` to restore the state.
- Add `fun updateSelectedDays(days: Set<DayOfWeek>)`.
- `loadHabit`: populate `selectedDays` from `HabitSchedule.specificDays` (falls back to all days if null).
- `createNewHabit` / `updateExistingHabit`: pass `selectedDays` as `HabitSchedule.specificDays`.
- Add `fun discardChanges()` — resets `_state` to `originalState` and emits `NavigateBack`.

### `HabitFormScreen` (Route)

Add `onDiscardClick` callback wired to `viewModel::discardChanges`. In create mode both "Discard Draft" and the OS back gesture call `onNavigateBack` directly (no unsaved state to restore).

---

## Colours reference

| Token | Light | Dark |
|---|---|---|
| `primary` | `#163829` | `#A9CFBA` |
| `primaryContainer` | `#2D4F3F` | `#2D4F3F` |
| `onPrimaryContainer` | `#FFFFFF` | `#E5E2DF` |
| `surfaceContainerLow` | `#F7F3F0` | `#1C1B1B` |
| `onSurfaceVariant` | `#334155` | `#BBC5BC` |
| `outlineVariant` | `#C1C8C2` | `#414844` |
| `error` | `#BA1A1A` | `#CF6679` |

---

## Screenshot tests

File: `androidUnitTest/.../HabitFormScreenshotTest.kt`
Pattern: same as `FirstHabitStepScreenshotTest` — Roborazzi + Robolectric, `@Config(sdk=[33], qualifiers="w360dp-h800dp-420dpi")`.

| Test | State | Theme |
|---|---|---|
| `habitForm_create_binary_lightTheme` | binary, all days, no reminder, no note | light |
| `habitForm_create_binary_darkTheme` | same | dark |
| `habitForm_create_quantitative_lightTheme` | quantitative, value=5, unit=km, all days | light |
| `habitForm_create_quantitative_darkTheme` | same | dark |
| `habitForm_create_weeklySchedule_lightTheme` | binary, weekly, Mon/Wed/Fri selected | light |
| `habitForm_create_weeklySchedule_darkTheme` | same | dark |
| `habitForm_create_noteExpanded_lightTheme` | binary, description filled | light |
| `habitForm_create_noteExpanded_darkTheme` | same | dark |
| `habitForm_create_reminderOn_lightTheme` | binary, reminder on, time 09:00 | light |
| `habitForm_create_reminderOn_darkTheme` | same | dark |
| `habitForm_edit_lightTheme` | isEditing=true, pre-filled values | light |
| `habitForm_edit_darkTheme` | same | dark |
