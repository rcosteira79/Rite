# Custom Increment Values for Quantitative Habits

## Summary

Expose the existing `defaultIncrement` field in the habit create/edit form so users can configure the quick-increment amount for quantitative habits. The domain model, database schema, and Today screen already support custom increment values — this feature surfaces the configuration in the UI.

## Motivation

Quantitative habits benefit from different increment sizes. A user tracking water intake in mL wants "+500" per tap, not "+1". Today the quick-increment button always adds 1, forcing users to use the "Custom" dialog every time. Letting users set their preferred increment at habit creation removes that friction.

## Scope

**In scope:**
- New `defaultIncrement` text field in the habit form (create and edit)
- Field only visible for quantitative habits
- Wiring the field value through to `CreateHabitParams` and `UpdateHabit`
- Pre-filled with "1", defaults to 1 if left blank or invalid
- Unit tests for the new form logic

**Out of scope:**
- Decimal increment support (users should use smaller units instead, e.g., 500 mL instead of 0.5 L)
- Changes to the Today screen increment logic (already reads `habit.defaultIncrement`)
- Changes to the domain layer, database schema, or notification tracking (already support custom values)

## Design

### Data Layer — No Changes

The full pipeline already exists:
- `Habit.defaultIncrement: Int = 1` (domain model)
- `Habit.defaultIncrement INTEGER NOT NULL DEFAULT 1` (SQLDelight schema)
- `CreateHabitParams.defaultIncrement: Int = 1` (use case params)
- `HabitCompletionEvent.deltaValue` carries the increment amount
- `TodayHabitUiModel.defaultIncrement` exposes it to the Today screen
- `TodayViewModel.incrementHabitProgress()` reads `habit.defaultIncrement`

No migrations, no model changes required.

### Form State

Add one field to `HabitFormState`:

```kotlin
val defaultIncrement: String = "1"
```

String type to match how `targetValue` and `quota` are already handled as text field inputs.

### Form ViewModel

Four changes to `HabitFormViewModel`:

1. **New update method**: `updateDefaultIncrement(value: String)` — follows the same pattern as `updateTargetValue` and `updateUnit`
2. **Create path**: In `createNewHabit()`, pass `state.defaultIncrement.toIntOrNull() ?: 1` to `CreateHabitParams.defaultIncrement`
3. **Update path**: In `updateExistingHabit()`, set `defaultIncrement` on the updated habit from the same parsed value
4. **Edit loading**: When loading an existing habit for editing, populate `defaultIncrement` from `habit.defaultIncrement.toString()`

### Validation

No save-blocking validation. If the field is blank or unparseable, it silently defaults to 1. This matches the expected behavior — 1 is the natural default, and the field is optional configuration.

Minimum value: 1 (no zero or negative). No maximum cap.

### Form UI

A single `UnderlineTextField` added below the Unit field, inside the existing `AnimatedVisibility(visible = state.type == HabitType.QUANTITATIVE)` block.

**Field details:**
- Label: "Increment by"
- Placeholder/hint: "1"
- Keyboard type: numeric
- Style: underline-only (same `UnderlineTextField` component as the Unit field — no filled grey background)
- Positioned directly below the Unit field, before the Schedule section

### Testing

- **ViewModel unit test**: Verify `defaultIncrement` flows from form state to `CreateHabitParams` correctly, including blank-defaults-to-1 behavior
- **ViewModel unit test**: Verify edit mode populates `defaultIncrement` from existing habit
- **Form UI test**: Verify the increment field appears only when type is QUANTITATIVE and hides for BINARY

No changes needed for Today screen tests — the increment path already works with whatever `defaultIncrement` value is stored.
