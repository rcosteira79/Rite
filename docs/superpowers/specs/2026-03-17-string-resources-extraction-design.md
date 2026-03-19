# String Resources Extraction Design

**Date:** 2026-03-17
**Status:** Approved

## Goal

Extract all hardcoded UI strings into Compose Multiplatform string resources to establish a single source of truth and prepare the app for future localization (i18n). Desktop platform is out of scope; Android and iOS are the targets.

## Resource System

Use Compose Multiplatform's built-in resource system (`org.jetbrains.compose.resources`). The dependency `compose.components.resources` is already present in `composeApp/build.gradle.kts`.

String files live in `commonMain/composeResources/values/` and are accessed at runtime via `stringResource(Res.string.<key>)` or `stringResource(Res.string.<key>, arg1, arg2, ...)` for parameterized strings. Plural strings use `pluralStringResource(Res.plurals.<key>, quantity, args...)`.

The existing `androidMain/res/values/strings.xml` (which contains only `app_name` for the Android manifest) is left untouched.

## File Structure

One XML file per screen, all in `composeApp/src/commonMain/composeResources/values/`. Strings shared across multiple screens go in a dedicated common file:

```
strings_common.xml                   ← shared strings (Back, Skip, Continue, Edit, etc.)
strings_today.xml
strings_settings.xml
strings_habit_form.xml
strings_calendar.xml
strings_archived.xml
strings_quantitative_input.xml
strings_onboarding_philosophy.xml
strings_onboarding_first_habit.xml
strings_onboarding_strictness.xml
```

## Naming Convention

Keys follow the pattern `<screen>_<element>` with a descriptive suffix. Shared strings use the `common_` prefix.

Content descriptions use a `cd_` segment immediately after the screen prefix: `<screen>_cd_<element>`.

```xml
<!-- Shared -->
<string name="common_cd_back">Back</string>
<string name="common_continue">Continue</string>
<string name="common_skip">Skip</string>
<string name="common_edit">Edit</string>
<string name="common_cancel">Cancel</string>

<!-- Screen-specific static -->
<string name="today_title">Today</string>
<string name="today_section_daily_habits">DAILY HABITS</string>
<string name="today_empty_state_heading">No habits for today</string>
<string name="today_cd_add_habit">Add Habit</string>

<!-- Parameterized — single argument -->
<string name="today_streak">🔥 %d day streak</string>
<string name="today_score">📊 Score: %s</string>

<!-- Parameterized — multiple arguments must use positional specifiers -->
<string name="today_timezone_changed_message">Your timezone has changed from %1$s. Past data remains unchanged.</string>
```

**Rule:** Strings with more than one format argument must use positional specifiers (`%1$s`, `%2$d`, etc.) to ensure correct ordering on both platforms.

## Scope

**Extracted:**
- All visible UI text: titles, labels, placeholders, section headers, button text, empty state messages, snackbar messages, warning banners
- Content descriptions
- Parameterized strings with runtime values
- Strings that mix emoji with translatable text — the full string including the emoji is placed in the resource file; the emoji is treated as fixed UI decoration
- Singular/plural variants — use `Res.plurals` and `pluralStringResource()`

**Not extracted:**
- Emoji-only strings (e.g. `"🟢"`, `"🟡"`, `"🔴"`) — not translatable text
- Format-only runtime values with no surrounding text

## Special Cases

### Optional inline segments (`QuantitativeInputBottomSheet`)

The "Current:" string has an optional unit suffix with a leading space:
```kotlin
"Current: ${habit.completedValue ?: 0}/${habit.targetValue}${habit.unit?.let { " $it" } ?: ""}"
```

Extract as two resource keys and select at the call site:
```xml
<string name="quantitative_input_current_with_unit">Current: %1$d/%2$d %3$s</string>
<string name="quantitative_input_current_no_unit">Current: %1$d/%2$d</string>
```

### List-of-strings call sites (`StrictnessScreen`)

`PresetCard` receives `rules: List<String>`. Once extracted, `stringResource` calls must be resolved before constructing the list, which requires a `@Composable` context. Since `StrictnessScreen` is already `@Composable` this is not a blocker — resolve each string at the call site before passing the list.

### Plural strings (`TodayScreen`)

The subtitle `"${state.pendingCount} habit/habits to go"` uses an inline Kotlin conditional. Extract as a plural resource:

```xml
<plurals name="today_subtitle_habits_to_go">
    <item quantity="one">%d habit to go</item>
    <item quantity="other">%d habits to go</item>
</plurals>
```

Used as:
```kotlin
pluralStringResource(Res.plurals.today_subtitle_habits_to_go, state.pendingCount, state.pendingCount)
```

### XML escaping

Apostrophes in XML string values must be escaped as `\'`. None of the current ~95 strings contain apostrophes, but this applies to any string added in the future.

## Execution Order

Screens are extracted one at a time to keep changes reviewable:

1. `strings_common.xml` — shared strings first, so later screens can reference them
2. `TodayScreen` — most strings, establishes the pattern
3. `HabitFormScreen`
4. `SettingsScreen`
5. `CalendarScreen`
6. `ArchivedHabitsScreen`
7. `QuantitativeInputBottomSheet`
8. `PhilosophyScreen`
9. `FirstHabitScreen`
10. `StrictnessScreen`

## Impact

- ~95 hardcoded strings extracted across 9 screens
- No behaviour changes — purely a refactor
- Enables future localization by adding language-specific `values-<locale>/` directories
