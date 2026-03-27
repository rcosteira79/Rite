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
│  M  T  W  T  F  S  S             │  ← SchedulePicker (weekly only)
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
- Secondary CTA: **Discard Draft** — calls `viewModel.discardDraft()`, navigates back, no habit saved
- No delete icon in top bar
- No `BackHandler` override — OS back behaves identically to "Discard Draft"

### Edit mode

- Title: **Edit Habit**
- No subtitle
- Outlined trash icon (`Icons.Outlined.Delete`, 22dp, tinted `MaterialTheme.colorScheme.error`) in top-right of top bar — triggers the **Delete confirmation dialog** (see below)
- Primary CTA: **Save Changes**
- Secondary CTA: **Discard Changes** — calls `viewModel.discardChanges()`, resets all fields to the values loaded at screen entry, navigates back
- `BackHandler` intercepts the OS back gesture and calls `viewModel.discardChanges()` — same behaviour as the secondary CTA

### Loading state (edit mode only)

While `HabitFormState.isLoading == true`, render a full-screen `Box` with `CircularProgressIndicator` centred (same as the current implementation). The form content is not rendered until loading completes.

### Delete confirmation dialog

Shown when the user taps the trash icon in edit mode. Rendered as a `BasicAlertDialog` / `AlertDialog`.

- **Title:** "Delete habit?"
- **Body:** "This will permanently remove the habit and all its history. This action cannot be undone."
- **Confirm button:** "Delete" — destructive, tinted `error`; calls `viewModel.deleteHabit()`
- **Cancel button:** "Cancel" — dismisses dialog, no action
- **Dismiss on back gesture / outside tap:** dismisses dialog, no action

Dialog open/closed state is local to the composable (`remember { mutableStateOf(false) }`), not in `HabitFormState`.

---

## Components

### New: `TypeToggle`

Location: `ui/components/TypeToggle.kt`

Two full-width pills side by side. Selected pill: `primaryContainer` background, `onPrimaryContainer` text, `SemiBold`. Unselected: `surfaceContainerLow` background, `onSurfaceVariant` text. In dark mode, unselected pills add a `1dp outlineVariant` border. In light mode there is no border on unselected pills. The same border rule applies to the inline schedule toggle pills.

```
TypeToggle(
    selected: HabitType,
    onSelectionChange: (HabitType) -> Unit,
    modifier: Modifier
)
```

### New: `QuantityStepper`

Location: `ui/components/QuantityStepper.kt`

Row: circular `−` button — bold number — circular `+` button — label string. Minimum value 1; no maximum. Circular buttons use `surfaceContainerLow` background (`outlineVariant` 1dp border in dark mode). The `−` button is disabled (greyed out, not clickable) when the current value is already 1.

**There is exactly one `QuantityStepper` on screen at a time.** It controls different `HabitFormState` fields depending on the selected type:

- `HabitType.BINARY`: stepper controls `quota` (how many completions per cadence window)
- `HabitType.QUANTITATIVE`: stepper controls `targetValue` (the measurement goal); `quota` is implicitly 1 and not exposed

The **label** is derived by the caller using this rule:

- If `HabitType.QUANTITATIVE` and `unit` is non-blank: `"${unit} / ${cadence}"` (e.g. *"km / day"*)
- Otherwise: `"time(s) / ${cadence}"` where `cadence` is `"day"` for `DAILY` and `"week"` for `WEEKLY`

When `HabitType.QUANTITATIVE` is selected, a unit `TextField` (same underline style as the habit name field) appears below the stepper via `AnimatedVisibility` (`expandVertically + fadeIn` / `shrinkVertically + fadeOut`).

```
QuantityStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier
)
```

### New: `FormListRow`

Location: `ui/components/FormListRow.kt`

Horizontal row: 36dp circular icon container (`surfaceContainerLow` background, `outlineVariant` 1dp border in dark) — title + subtitle column — optional trailing composable slot. A `1dp outlineVariant` divider sits above each row by default; callers opt out by passing `showTopDivider = false`.

```
FormListRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?,
    trailingContent: (@Composable () -> Unit)?,
    showTopDivider: Boolean = true,
    modifier: Modifier
)
```

**Reminder row** — trailing slot is a `Switch`. Subtitle shows the formatted reminder time when `hasReminder == true` (e.g. *"09:00 AM"*); subtitle shows *"Off"* when `hasReminder == false`. The default reminder time when first enabling is `LocalTime(9, 0)`. The row is not tappable (no `onClick`). Time picker interaction is out of scope for this revamp (deferred); the time display is present as a placeholder.

**Add Note row** — no trailing slot (`trailingContent = null`). Tapping anywhere on the row toggles expansion. While collapsed: title = *"Add Note"*, subtitle = *"Tap to add a description"*, icon tint = `onSurfaceVariant`. While expanded: title = *"Note"*, subtitle is hidden, icon tint = `onSurface`. Below the row, an inline multi-line `TextField` appears via `AnimatedVisibility`. There is no explicit collapse button; tapping the row header again collapses it.

### Reused: `SchedulePicker`

`ui/components/SchedulePicker.kt` — unchanged. **Only rendered when `scheduleType == ScheduleType.WEEKLY`**. When the user switches to Daily the `SchedulePicker` disappears via `AnimatedVisibility`; `selectedDays` in state is preserved (not reset).

### Inline: Schedule type toggle

Not a separate component. A small two-pill row (`Daily` | `Weekly`) rendered inline in `HabitFormScreen`, right-aligned beside the `SCHEDULE` label. Uses the same pill shape and colour logic as `TypeToggle` but at `labelSmall` size with `12dp` horizontal padding.

---

## State & ViewModel changes

### `HabitFormState`

Add one field:

```kotlin
val selectedDays: Set<DayOfWeek> = DayOfWeek.entries.toSet()
```

Update `isValid` to also validate `selectedDays` when schedule is weekly:

```kotlin
val isValid: Boolean get() {
    val nameValid = name.isNotBlank()
    val typeValid = type == HabitType.BINARY || targetValue.toIntOrNull()?.let { it > 0 } == true
    val quotaValid = quota.toIntOrNull()?.let { it > 0 } == true
    val daysValid = scheduleType == ScheduleType.DAILY || selectedDays.isNotEmpty()
    return nameValid && typeValid && quotaValid && daysValid
}
```

### `HabitFormViewModel`

#### New field

```kotlin
private var originalState: HabitFormState? = null
```

Assigned once, immediately after the successful `_state.update` inside `loadHabit`. Not set in create mode.

#### Updated `loadHabit`

Must also fetch `HabitSchedule` to populate `scheduleType` and `selectedDays`. The repository call sequence:

1. `habitRepository.getHabitById(habitId)` → `Habit`
2. `habitRepository.getRemindersForHabit(habitId)` → `List<HabitReminder>`
3. `habitRepository.getScheduleForHabit(habitId)` → `HabitSchedule?` (existing repository method)

Populate state with `schedule?.scheduleType` (fallback `DAILY`) and `schedule?.specificDays ?: DayOfWeek.entries.toSet()`.

After `_state.update` succeeds, assign: `originalState = _state.value`.

#### New `updateSelectedDays`

```kotlin
fun updateSelectedDays(days: Set<DayOfWeek>) {
    _state.update { it.copy(selectedDays = days) }
}
```

#### `updateScheduleType`

The existing `updateScheduleType` function must **not** reset `selectedDays`. It only updates `scheduleType`. `selectedDays` is preserved so that switching Daily → Weekly → Daily and back retains the user's previous day selection.

#### Updated `updateType`

The existing side-effect that auto-switches `reminderType` is preserved unchanged. `ReminderType` is not user-facing in this revamp.

#### Updated `createNewHabit`

Pass `selectedDays` as `HabitSchedule.specificDays` using this rule:
- `scheduleType == DAILY` → `specificDays = null` (`null` means "all days" per `HabitSchedule` KDoc)
- `scheduleType == WEEKLY` → `specificDays = selectedDays`

#### Updated `updateExistingHabit`

Currently does not update `HabitSchedule` at all — **this is a bug fix required as part of this revamp**. Fetch the existing schedule and update `scheduleType` and `specificDays`. If no schedule exists, create one. Apply the same null rule as `createNewHabit`: DAILY → `null`, WEEKLY → `selectedDays`.

#### New `discardDraft` (create mode)

```kotlin
fun discardDraft() {
    viewModelScope.launch { _events.emit(HabitFormEvent.NavigateBack) }
}
```

#### New `discardChanges` (edit mode)

```kotlin
fun discardChanges() {
    originalState?.let { _state.value = it }
    // If originalState is null (loadHabit failed before completing), skip the reset
    // and navigate back immediately — there is no valid original state to restore.
    viewModelScope.launch { _events.emit(HabitFormEvent.NavigateBack) }
}
```

### `HabitFormScreen` Route

- Wire `onDiscardDraftClick` → `viewModel::discardDraft`
- Wire `onDiscardChangesClick` → `viewModel::discardChanges`
- Wire `onSelectedDaysChange` → `viewModel::updateSelectedDays`
- In edit mode, wrap content with `BackHandler(enabled = state.isEditing) { viewModel.discardChanges() }`

### Error surface

`HabitFormState.error` continues to surface as a Snackbar via the existing `ShowError` event — no change. No inline error rendering in this revamp.

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
Pattern: same as `FirstHabitStepScreenshotTest` — Roborazzi + Robolectric, `@Config(sdk=[33], qualifiers="w360dp-h800dp-420dpi")`, `application = android.app.Application::class`.

Tests target the stateless private `HabitFormScreen(state, ...)` overload directly.

| Test | State | Theme |
|---|---|---|
| `habitForm_create_binary_lightTheme` | binary, all days, no reminder, no note | light |
| `habitForm_create_binary_darkTheme` | same | dark |
| `habitForm_create_quantitative_lightTheme` | quantitative, quota=5, unit=km, all days (unit TextField visible) | light |
| `habitForm_create_quantitative_darkTheme` | same | dark |
| `habitForm_create_weeklySchedule_lightTheme` | binary, weekly, Mon/Wed/Fri selected (SchedulePicker visible) | light |
| `habitForm_create_weeklySchedule_darkTheme` | same | dark |
| `habitForm_create_noteExpanded_lightTheme` | binary, description = "Typical intention" (note row expanded) | light |
| `habitForm_create_noteExpanded_darkTheme` | same | dark |
| `habitForm_create_reminderOn_lightTheme` | binary, hasReminder=true, reminderTime=09:00 | light |
| `habitForm_create_reminderOn_darkTheme` | same | dark |
| `habitForm_edit_lightTheme` | isEditing=true, name="Deep Work", binary, all days, no reminder | light |
| `habitForm_edit_darkTheme` | same | dark |
| `habitForm_edit_weeklySchedule_lightTheme` | isEditing=true, name="Run", binary, weekly, Mon/Wed/Fri (SchedulePicker visible) | light |
| `habitForm_edit_weeklySchedule_darkTheme` | same | dark |
