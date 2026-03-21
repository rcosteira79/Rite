# Onboarding Design Refactor

**Date:** 2026-03-21
**Branch:** to be created from master
**Scope:** UI-only refactor of the three onboarding wizard steps to align with new designs. No changes to navigation, routing, or end-of-onboarding behaviour.

---

## Summary of Decisions

All copy and layout decisions were made screen by screen against the new design files in `design system/`.

---

## Screen 1 — Philosophy

### Copy
No copy changes. All existing strings in `strings_onboarding_philosophy.xml` are kept as-is.

### Layout
No layout changes.

### Behaviour
**CTA text change only:** `PhilosophyStepCta` changes from `"Continue"` to `"Accept the Commitment"`.

The string is currently hardcoded in `OnboardingCta.kt`. As part of this work, extract it (and all other hardcoded CTA strings in that file) to `strings_onboarding_philosophy.xml` / `strings_onboarding_first_habit.xml`.

**Dropped from design:**
- Feature blocks ("Uncompromising Focus", "Intentional Design", "Proof of Effort") — marketing copy for non-existent features
- "Review our Manifesto" secondary link — no manifesto content exists

---

## Screen 2 — Strictness

No changes. All copy and layout remain exactly as currently implemented.

---

## Screen 3 — First Habit

This is the only screen with structural changes.

### Copy
No copy changes to existing strings. New strings added only for new UI elements (see String Resources section).

### Layout — Type Selection

**Before:** Two `FilterChip`s ("Yes / No", "Quantitative") — no descriptions.

**After:** Two selectable cards, stacked vertically.

| Card | Label | Description |
|---|---|---|
| Binary | Binary | "Simple 'Yes' or 'No' completion. Perfect for streaks." |
| Quantitative | Quantitative | "Track units like 'Minutes' or 'Pages'. Focus on volume." |

When **Quantitative** is selected, the card expands (same animated pattern as `StrictnessStep` rule expansion) to reveal:
1. **Target value** — number input, required. Validation already exists in `OnboardingViewModel`.
2. **Unit** — text input, optional (e.g. "pages", "minutes").

When Binary is selected (or re-selected), these fields collapse and are cleared.

### Layout — Schedule Section

New section added below type selection, before the CTA.

**Section label:** "SET A SCHEDULE"

**Options** (chip row, single-select, default = Every Day):

| Chip | Maps to |
|---|---|
| Every Day | `specificDays = null` (all days) |
| Weekdays | `specificDays = {MON, TUE, WED, THU, FRI}` |
| Weekends | `specificDays = {SAT, SUN}` |
| Custom | Shows inline day-picker |

**Custom day-picker:** When "Custom" is selected, a row of 7 toggle chips appears below (Mon · Tue · Wed · Thu · Fri · Sat · Sun), each individually toggleable. At least one day must be selected for the CTA to be enabled.

All schedule options map to `ScheduleType.DAILY` with `specificDays` set accordingly. No weekly schedule type is introduced in onboarding.

---

## State Changes

### New: `ScheduleOption` enum (in `OnboardingState.kt`)

```kotlin
enum class ScheduleOption { EVERY_DAY, WEEKDAYS, WEEKENDS, CUSTOM }
```

### Updated: `OnboardingState`

Two new fields:

```kotlin
val scheduleOption: ScheduleOption = ScheduleOption.EVERY_DAY
val customDays: Set<DayOfWeek> = emptySet()
```

### CTA enabled condition (updated in `OnboardingCta.kt` / `FirstHabitStepCta`)

```kotlin
val isEnabled = state.habitName.isNotBlank() &&
    (state.habitType == HabitType.BINARY || state.targetValue.isNotBlank()) &&
    (state.scheduleOption != ScheduleOption.CUSTOM || state.customDays.isNotEmpty())
```

---

## ViewModel Changes (`OnboardingViewModel`)

Two new public methods:

```kotlin
fun updateScheduleOption(option: ScheduleOption)
fun updateCustomDays(days: Set<DayOfWeek>)
```

Updated `createFirstHabit()` — build `specificDays` from schedule state before calling `CreateHabit.execute()`:

```kotlin
val specificDays: Set<DayOfWeek>? = when (scheduleOption) {
    ScheduleOption.EVERY_DAY -> null
    ScheduleOption.WEEKDAYS -> setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
    ScheduleOption.WEEKENDS -> setOf(SATURDAY, SUNDAY)
    ScheduleOption.CUSTOM -> state.customDays
}
```

Pass `specificDays` into `CreateHabitParams`. `CreateHabit` already supports this field. Leave `scheduleType` at its default (`ScheduleType.DAILY`) — all four schedule options map to DAILY, so no explicit value is needed.

---

## String Resources

### `strings_onboarding_philosophy.xml` — additions

```xml
<string name="philosophy_cta_accept">Accept the Commitment</string>
```

### `strings_onboarding_first_habit.xml` — updates and additions

Update existing key (label changes from "Yes/No" to "Binary" to match new card design):

```xml
<!-- Update existing — was "Yes/No" -->
<string name="first_habit_type_binary">Binary</string>
```

Add new keys:

```xml
<!-- Type card descriptions -->
<string name="first_habit_type_binary_description">Simple "Yes" or "No" completion. Perfect for streaks.</string>
<string name="first_habit_type_quantitative">Quantitative</string>
<string name="first_habit_type_quantitative_description">Track units like "Minutes" or "Pages". Focus on volume.</string>

<!-- Schedule section -->
<string name="first_habit_schedule_label">Set a schedule</string>
<string name="first_habit_schedule_every_day">Every Day</string>
<string name="first_habit_schedule_weekdays">Weekdays</string>
<string name="first_habit_schedule_weekends">Weekends</string>
<string name="first_habit_schedule_custom">Custom</string>
```

The existing keys `first_habit_button_create` ("Create habit") and `first_habit_button_skip` ("Skip for now") are already correct — use them in `OnboardingCta.kt` instead of adding new keys.

### Existing hardcoded strings in `OnboardingCta.kt` to extract

All four hardcoded values must be replaced with resource lookups:

| Current hardcoded value | Location in file | Target resource file | Key |
|---|---|---|---|
| `"Continue"` | `PhilosophyStepCta` | `strings_onboarding_philosophy.xml` | `philosophy_cta_accept` (new value: "Accept the Commitment") |
| `"Continue"` | `StrictnessStepCta` | `strings_onboarding_strictness.xml` | `strictness_cta_continue` |
| `"Create habit"` | `FirstHabitStepCta` | `strings_onboarding_first_habit.xml` | `first_habit_button_create` (already exists) |
| `"Skip for now"` | `FirstHabitStepCta` | `strings_onboarding_first_habit.xml` | `first_habit_button_skip` (already exists) |

---

## Files Touched

| File | Change |
|---|---|
| `OnboardingCta.kt` | `PhilosophyStepCta` CTA text; extract all hardcoded strings to resources |
| `OnboardingState.kt` | Add `ScheduleOption` enum; add `scheduleOption` + `customDays` fields |
| `OnboardingViewModel.kt` | Add `updateScheduleOption()`, `updateCustomDays()`; update `createFirstHabit()` |
| `FirstHabitStep.kt` | Replace chips with type cards; add expandable quantitative fields; add schedule section; add `scheduleOption`, `customDays`, `onScheduleOptionChange`, `onCustomDaysChange` parameters |
| `OnboardingWizard.kt` | Add `onScheduleOptionChange` + `onCustomDaysChange` to its own parameter list; pass `scheduleOption`, `customDays`, and both callbacks to `FirstHabitStep` at **both** call sites (Crossfade branch and AnimatedContent branch) |
| `strings_onboarding_philosophy.xml` | Add `philosophy_cta_accept` |
| `strings_onboarding_first_habit.xml` | Add type descriptions, schedule strings, extract CTA strings |
| `strings_onboarding_strictness.xml` | Extract `strictness_cta_continue` |

**Not touched:** `OnboardingRoute.kt`, `OnboardingTopChrome.kt`, `OnboardingStrictnessPreset.kt`, `PhilosophyStep.kt`, `StrictnessStep.kt`, domain layer, navigation.

---

## Out of Scope

- Schedule editing after onboarding (habit detail screen)
- WEEKLY schedule type — not introduced in onboarding
- Reminder setup during onboarding — remains `null` (no change)
- Any changes to Strictness or Philosophy layouts
