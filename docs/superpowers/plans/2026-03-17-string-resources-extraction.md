# String Resources Extraction Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extract all hardcoded UI strings from 9 Compose screens into Compose Multiplatform string resource files to enable future localization.

**Architecture:** One XML file per screen in `commonMain/composeResources/values/`, plus a shared `strings_common.xml` for strings that appear across multiple screens. Screens reference strings via `stringResource(Res.string.<key>)` and `pluralStringResource(Res.plurals.<key>, count, args...)`. No logic or behaviour changes.

**Tech Stack:** Compose Multiplatform resources (`org.jetbrains.compose.resources`), `compose.components.resources` (already in `composeApp/build.gradle.kts`)

**Worktree:** All implementation work happens in `.worktrees/feature/extract-resources`. Run all commands from there unless stated otherwise.

---

## File Map

**Created:**
- `composeApp/src/commonMain/composeResources/values/strings_common.xml`
- `composeApp/src/commonMain/composeResources/values/strings_today.xml`
- `composeApp/src/commonMain/composeResources/values/strings_habit_form.xml`
- `composeApp/src/commonMain/composeResources/values/strings_settings.xml`
- `composeApp/src/commonMain/composeResources/values/strings_calendar.xml`
- `composeApp/src/commonMain/composeResources/values/strings_archived.xml`
- `composeApp/src/commonMain/composeResources/values/strings_quantitative_input.xml`
- `composeApp/src/commonMain/composeResources/values/strings_onboarding_philosophy.xml`
- `composeApp/src/commonMain/composeResources/values/strings_onboarding_first_habit.xml`
- `composeApp/src/commonMain/composeResources/values/strings_onboarding_strictness.xml`

**Modified:**
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayScreen.kt`
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormScreen.kt`
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/settings/SettingsScreen.kt`
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/calendar/CalendarScreen.kt`
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/archived/ArchivedHabitsScreen.kt`
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/QuantitativeInputBottomSheet.kt`
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/PhilosophyScreen.kt`
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/FirstHabitScreen.kt`
- `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/StrictnessScreen.kt`

---

## Imports Pattern

Every modified screen needs these imports added (remove any that are unused). Do **not** use wildcard imports — import each resource key individually (your IDE will suggest them automatically):

```kotlin
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.today_title   // example — import each key used
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
```

---

## Task 1: strings_common.xml — shared strings

**Files:**
- Create: `composeApp/src/commonMain/composeResources/values/strings_common.xml`

- [ ] **Step 1: Create the file**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="common_cd_back">Back</string>
    <string name="common_skip">Skip</string>
    <string name="common_continue">Continue</string>
    <string name="common_edit">Edit</string>
    <string name="common_cancel">Cancel</string>
    <string name="common_daily">Daily</string>
    <string name="common_weekly">Weekly</string>
    <string name="common_quantitative">Quantitative</string>
    <string name="common_failed">Failed</string>
    <string name="common_placeholder_habit_name">E.g. Drink water</string>
    <string name="common_placeholder_target_value">E.g. 8</string>
</resources>
```

- [ ] **Step 2: Build to verify resource generation**

```bash
./gradlew :composeApp:generateResourceAccessorsForCommonMain --quiet
```

Expected: BUILD SUCCESSFUL. The `Res.string.common_cd_back` accessor and siblings are now available.

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/composeResources/values/strings_common.xml
git commit -m "feat(resources): add shared string resources"
```

---

## Task 2: TodayScreen

**Files:**
- Create: `composeApp/src/commonMain/composeResources/values/strings_today.xml`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayScreen.kt`

- [ ] **Step 1: Create strings_today.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="today_title">Today</string>
    <string name="today_subtitle_all_done">All done for today 🎉</string>
    <string name="today_cd_calendar">Calendar</string>
    <string name="today_cd_settings">Settings</string>
    <string name="today_cd_add_habit">Add Habit</string>
    <string name="today_section_daily_habits">DAILY HABITS</string>
    <string name="today_section_weekly_habits">WEEKLY HABITS</string>
    <string name="today_section_suspended_habits">SUSPENDED HABITS</string>
    <string name="today_timezone_changed_title">Timezone changed</string>
    <string name="today_timezone_changed_message">Your timezone has changed from %1$s. Past data remains unchanged.</string>
    <string name="today_timezone_changed_dismiss">Dismiss</string>
    <string name="today_empty_state_heading">No habits for today</string>
    <string name="today_empty_state_subtext">Create your first habit to get started</string>
    <string name="today_empty_state_add_habit">Add Habit</string>
    <string name="today_streak">🔥 %d day streak</string>
    <string name="today_score">📊 Score: %s</string>
    <string name="today_cd_complete">Complete</string>
    <string name="today_cd_undo">Undo</string>
    <string name="today_status_suspended">Suspended</string>
    <string name="today_action_archive">Archive</string>
    <plurals name="today_subtitle_habits_to_go">
        <item quantity="one">%d habit to go</item>
        <item quantity="other">%d habits to go</item>
    </plurals>
</resources>
```

- [ ] **Step 2: Update TodayScreen.kt**

Add the imports from the Imports Pattern section at the top of the file.

Apply the following replacements (line numbers are approximate — verify against the actual file):

| Location | Old | New |
|---|---|---|
| TopAppBar title (~L101) | `"Today"` | `stringResource(Res.string.today_title)` |
| Subtitle all-done (~L108) | `"All done for today 🎉"` | `stringResource(Res.string.today_subtitle_all_done)` |
| Subtitle pending (~L106) | `"${state.pendingCount} ${if (state.pendingCount == 1) "habit" else "habits"} to go"` | `pluralStringResource(Res.plurals.today_subtitle_habits_to_go, state.pendingCount, state.pendingCount)` |
| Calendar icon cd (~L127) | `"Calendar"` | `stringResource(Res.string.today_cd_calendar)` |
| Settings icon cd (~L133) | `"Settings"` | `stringResource(Res.string.today_cd_settings)` |
| FAB cd (~L141) | `"Add Habit"` | `stringResource(Res.string.today_cd_add_habit)` |
| Daily section header (~L201) | `"DAILY HABITS"` | `stringResource(Res.string.today_section_daily_habits)` |
| Weekly section header (~L240) | `"WEEKLY HABITS"` | `stringResource(Res.string.today_section_weekly_habits)` |
| Suspended section header (~L273) | `"SUSPENDED HABITS"` | `stringResource(Res.string.today_section_suspended_habits)` |
| Timezone banner title (~L325) | `"Timezone changed"` | `stringResource(Res.string.today_timezone_changed_title)` |
| Timezone banner message (~L330) | `"Your timezone has changed from $previousTimezone. Past data remains unchanged."` | `stringResource(Res.string.today_timezone_changed_message, previousTimezone)` |
| Timezone banner dismiss (~L336) | `"Dismiss"` | `stringResource(Res.string.today_timezone_changed_dismiss)` |
| Empty state heading (~L353) | `"No habits for today"` | `stringResource(Res.string.today_empty_state_heading)` |
| Empty state subtext (~L359) | `"Create your first habit to get started"` | `stringResource(Res.string.today_empty_state_subtext)` |
| Empty state button (~L367) | `"Add Habit"` | `stringResource(Res.string.today_empty_state_add_habit)` |
| Streak text (~L464) | `"🔥 ${habit.currentStreak} day streak"` | `stringResource(Res.string.today_streak, habit.currentStreak)` |
| Score text (~L471) | `"📊 Score: ${habit.scoreText}"` | `stringResource(Res.string.today_score, habit.scoreText)` |
| Skip button (~L490) | `"Skip"` | `stringResource(Res.string.common_skip)` |
| Complete cd (~L497) | `"Complete"` | `stringResource(Res.string.today_cd_complete)` |
| Undo cd (~L505) | `"Undo"` | `stringResource(Res.string.today_cd_undo)` |
| Suspended status (~L512) | `"Suspended"` | `stringResource(Res.string.today_status_suspended)` |
| Failed status (~L519) | `"Failed"` | `stringResource(Res.string.common_failed)` |
| Edit menu item (~L548) | `"Edit"` | `stringResource(Res.string.common_edit)` |
| Archive menu item (~L555) | `"Archive"` | `stringResource(Res.string.today_action_archive)` |
| Daily ring label (~L583) | `"Daily"` | `stringResource(Res.string.common_daily)` |
| Weekly ring label (~L592) | `"Weekly"` | `stringResource(Res.string.common_weekly)` |

- [ ] **Step 3: Build to verify**

```bash
./gradlew :composeApp:compileKotlinAndroid --quiet
```

Expected: BUILD SUCCESSFUL with no unresolved reference errors.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/composeResources/values/strings_today.xml \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayScreen.kt
git commit -m "feat(resources): extract TodayScreen strings"
```

---

## Task 3: HabitFormScreen

**Files:**
- Create: `composeApp/src/commonMain/composeResources/values/strings_habit_form.xml`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormScreen.kt`

- [ ] **Step 1: Create strings_habit_form.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="habit_form_title_create">Create Habit</string>
    <string name="habit_form_title_edit">Edit Habit</string>
    <string name="habit_form_cd_delete">Delete</string>
    <string name="habit_form_label_name">Habit name *</string>
    <string name="habit_form_label_description">Description (optional)</string>
    <string name="habit_form_section_type">Habit Type</string>
    <string name="habit_form_type_binary_label">Binary</string>
    <string name="habit_form_type_binary_description">Single action - done or not done</string>
    <string name="habit_form_type_quantitative_description">Track progress with a target</string>
    <string name="habit_form_section_target">Target</string>
    <string name="habit_form_label_target_value">Target value *</string>
    <string name="habit_form_label_unit">Unit</string>
    <string name="habit_form_placeholder_unit">E.g. glasses</string>
    <string name="habit_form_section_schedule">Schedule</string>
    <string name="habit_form_schedule_daily_description">Resets every day</string>
    <string name="habit_form_schedule_weekly_description">Resets every week</string>
    <string name="habit_form_label_quota">Quota *</string>
    <string name="habit_form_placeholder_quota">E.g. 1</string>
    <string name="habit_form_quota_supporting_daily">Number of completions required per day</string>
    <string name="habit_form_quota_supporting_weekly">Number of completions required per week</string>
    <string name="habit_form_section_reminder">Reminder</string>
    <string name="habit_form_reminder_info_periodic">Periodic reminders at intervals</string>
    <string name="habit_form_label_interval">Interval (minutes)</string>
    <string name="habit_form_reminder_info_time">Set a specific time for your reminder</string>
    <string name="habit_form_button_save">Save Changes</string>
    <string name="habit_form_button_create">Create Habit</string>
</resources>
```

- [ ] **Step 2: Update HabitFormScreen.kt**

Add the imports from the Imports Pattern section.

| Location | Old | New |
|---|---|---|
| Title (~L65) | `"Edit Habit"` (conditional) | `if (isEditing) stringResource(Res.string.habit_form_title_edit) else stringResource(Res.string.habit_form_title_create)` |
| Back cd (~L69) | `"Back"` | `stringResource(Res.string.common_cd_back)` |
| Delete cd (~L77) | `"Delete"` | `stringResource(Res.string.habit_form_cd_delete)` |
| Name label (~L106) | `"Habit name *"` | `stringResource(Res.string.habit_form_label_name)` |
| Name placeholder (~L107) | `"E.g. Drink water"` | `stringResource(Res.string.common_placeholder_habit_name)` |
| Description label (~L116) | `"Description (optional)"` | `stringResource(Res.string.habit_form_label_description)` |
| Type section title (~L125) | `"Habit Type"` | `stringResource(Res.string.habit_form_section_type)` |
| Binary label (~L148) | `"Binary"` | `stringResource(Res.string.habit_form_type_binary_label)` |
| Binary description (~L150) | `"Single action - done or not done"` | `stringResource(Res.string.habit_form_type_binary_description)` |
| Quantitative label (~L173) | `"Quantitative"` | `stringResource(Res.string.common_quantitative)` |
| Quantitative description (~L175) | `"Track progress with a target"` | `stringResource(Res.string.habit_form_type_quantitative_description)` |
| Target section title (~L190) | `"Target"` | `stringResource(Res.string.habit_form_section_target)` |
| Target value label (~L203) | `"Target value *"` | `stringResource(Res.string.habit_form_label_target_value)` |
| Target value placeholder (~L204) | `"E.g. 8"` | `stringResource(Res.string.common_placeholder_target_value)` |
| Unit label (~L212) | `"Unit"` | `stringResource(Res.string.habit_form_label_unit)` |
| Unit placeholder (~L213) | `"E.g. glasses"` | `stringResource(Res.string.habit_form_placeholder_unit)` |
| Schedule section title (~L226) | `"Schedule"` | `stringResource(Res.string.habit_form_section_schedule)` |
| Daily label (~L249) | `"Daily"` | `stringResource(Res.string.common_daily)` |
| Daily description (~L251) | `"Resets every day"` | `stringResource(Res.string.habit_form_schedule_daily_description)` |
| Weekly label (~L274) | `"Weekly"` | `stringResource(Res.string.common_weekly)` |
| Weekly description (~L276) | `"Resets every week"` | `stringResource(Res.string.habit_form_schedule_weekly_description)` |
| Quota label (~L289) | `"Quota *"` | `stringResource(Res.string.habit_form_label_quota)` |
| Quota placeholder (~L290) | `"E.g. 1"` | `stringResource(Res.string.habit_form_placeholder_quota)` |
| Quota supporting daily (~L293) | `"Number of completions required per day"` | `stringResource(Res.string.habit_form_quota_supporting_daily)` |
| Quota supporting weekly (~L296) | `"Number of completions required per week"` | `stringResource(Res.string.habit_form_quota_supporting_weekly)` |
| Reminder section title (~L315) | `"Reminder"` | `stringResource(Res.string.habit_form_section_reminder)` |
| Reminder periodic info (~L329) | `"Periodic reminders at intervals"` | `stringResource(Res.string.habit_form_reminder_info_periodic)` |
| Interval label (~L339) | `"Interval (minutes)"` | `stringResource(Res.string.habit_form_label_interval)` |
| Reminder time info (~L345) | `"Set a specific time for your reminder"` | `stringResource(Res.string.habit_form_reminder_info_time)` |
| Save button (~L366) | `"Save Changes"` / `"Create Habit"` | `if (isEditing) stringResource(Res.string.habit_form_button_save) else stringResource(Res.string.habit_form_button_create)` |

- [ ] **Step 3: Build to verify**

```bash
./gradlew :composeApp:compileKotlinAndroid --quiet
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/composeResources/values/strings_habit_form.xml \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormScreen.kt
git commit -m "feat(resources): extract HabitFormScreen strings"
```

---

## Task 4: SettingsScreen

**Files:**
- Create: `composeApp/src/commonMain/composeResources/values/strings_settings.xml`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/settings/SettingsScreen.kt`

- [ ] **Step 1: Create strings_settings.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="settings_title">Settings</string>
    <string name="settings_section_undo_policy">Undo Policy</string>
    <string name="settings_undo_disabled_label">Disabled</string>
    <string name="settings_undo_disabled_description">No undo allowed</string>
    <string name="settings_undo_today_label">Today Only</string>
    <string name="settings_undo_today_description">Can undo actions from today</string>
    <string name="settings_undo_all_label">All History</string>
    <string name="settings_undo_all_description">Can undo any past action</string>
    <string name="settings_section_snooze">Snooze Settings</string>
    <string name="settings_snooze_max_duration">Max snooze duration: %d minutes</string>
    <string name="settings_snooze_unlimited_label">Unlimited snoozes</string>
    <string name="settings_snooze_status_unlimited">Enabled</string>
    <string name="settings_snooze_status_limited">Limited to %d per habit/day</string>
    <string name="settings_section_skip">Skip Settings</string>
    <string name="settings_skip_unlimited_label">Unlimited skips</string>
    <string name="settings_skip_status_unlimited">Enabled</string>
    <string name="settings_skip_status_limited">Limited to %d consecutive days</string>
    <string name="settings_section_info">Info</string>
    <string name="settings_info_timezone_label">Current timezone</string>
    <string name="settings_archived_habits">Archived Habits</string>
</resources>
```

- [ ] **Step 2: Update SettingsScreen.kt**

Add the imports from the Imports Pattern section.

| Location | Old | New |
|---|---|---|
| Title (~L63) | `"Settings"` | `stringResource(Res.string.settings_title)` |
| Back cd (~L66) | `"Back"` | `stringResource(Res.string.common_cd_back)` |
| Undo section (~L90) | `"Undo Policy"` | `stringResource(Res.string.settings_section_undo_policy)` |
| Undo disabled label (~L93) | `"Disabled"` | `stringResource(Res.string.settings_undo_disabled_label)` |
| Undo disabled desc (~L94) | `"No undo allowed"` | `stringResource(Res.string.settings_undo_disabled_description)` |
| Undo today label (~L99) | `"Today Only"` | `stringResource(Res.string.settings_undo_today_label)` |
| Undo today desc (~L100) | `"Can undo actions from today"` | `stringResource(Res.string.settings_undo_today_description)` |
| Undo all label (~L105) | `"All History"` | `stringResource(Res.string.settings_undo_all_label)` |
| Undo all desc (~L106) | `"Can undo any past action"` | `stringResource(Res.string.settings_undo_all_description)` |
| Snooze section (~L116) | `"Snooze Settings"` | `stringResource(Res.string.settings_section_snooze)` |
| Snooze duration (~L119) | `"Max snooze duration: ${state.maxSnoozeDurationMinutes} minutes"` | `stringResource(Res.string.settings_snooze_max_duration, state.maxSnoozeDurationMinutes)` |
| Snooze unlimited label (~L144) | `"Unlimited snoozes"` | `stringResource(Res.string.settings_snooze_unlimited_label)` |
| Snooze status unlimited (~L148) | `"Enabled"` | `stringResource(Res.string.settings_snooze_status_unlimited)` |
| Snooze status limited (~L149) | `"Limited to ${state.maxSnoozesPerHabitPerDay} per habit/day"` | `stringResource(Res.string.settings_snooze_status_limited, state.maxSnoozesPerHabitPerDay)` |
| Skip section (~L167) | `"Skip Settings"` | `stringResource(Res.string.settings_section_skip)` |
| Skip unlimited label (~L175) | `"Unlimited skips"` | `stringResource(Res.string.settings_skip_unlimited_label)` |
| Skip status unlimited (~L179) | `"Enabled"` | `stringResource(Res.string.settings_skip_status_unlimited)` |
| Skip status limited (~L180) | `"Limited to ${state.maxConsecutiveSkips} consecutive days"` | `stringResource(Res.string.settings_skip_status_limited, state.maxConsecutiveSkips)` |
| Info section (~L197) | `"Info"` | `stringResource(Res.string.settings_section_info)` |
| Timezone label (~L200) | `"Current timezone"` | `stringResource(Res.string.settings_info_timezone_label)` |
| Archived habits row (~L223) | `"Archived Habits"` | `stringResource(Res.string.settings_archived_habits)` |

- [ ] **Step 3: Build to verify**

```bash
./gradlew :composeApp:compileKotlinAndroid --quiet
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/composeResources/values/strings_settings.xml \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/settings/SettingsScreen.kt
git commit -m "feat(resources): extract SettingsScreen strings"
```

---

## Task 5: CalendarScreen

**Files:**
- Create: `composeApp/src/commonMain/composeResources/values/strings_calendar.xml`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/calendar/CalendarScreen.kt`

- [ ] **Step 1: Create strings_calendar.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="calendar_title">Calendar</string>
    <string name="calendar_stats_perfect_days">Perfect Days</string>
    <string name="calendar_stats_days_tracked">Days Tracked</string>
    <string name="calendar_cd_previous_month">Previous month</string>
    <string name="calendar_cd_next_month">Next month</string>
    <string name="calendar_day_mon">Mon</string>
    <string name="calendar_day_tue">Tue</string>
    <string name="calendar_day_wed">Wed</string>
    <string name="calendar_day_thu">Thu</string>
    <string name="calendar_day_fri">Fri</string>
    <string name="calendar_day_sat">Sat</string>
    <string name="calendar_day_sun">Sun</string>
    <string name="calendar_legend_perfect">Perfect</string>
    <string name="calendar_legend_best_effort">Best Effort</string>
    <string name="calendar_legend_partial">Partial</string>
    <string name="calendar_legend_rough_day">Rough Day</string>
    <string name="calendar_legend_future">Future</string>
</resources>
```

- [ ] **Step 2: Update CalendarScreen.kt**

Add the imports from the Imports Pattern section.

| Location | Old | New |
|---|---|---|
| Title (~L57) | `"Calendar"` | `stringResource(Res.string.calendar_title)` |
| Back cd (~L60) | `"Back"` | `stringResource(Res.string.common_cd_back)` |
| Perfect Days label (~L92) | `"Perfect Days"` | `stringResource(Res.string.calendar_stats_perfect_days)` |
| Days Tracked label (~L104) | `"Days Tracked"` | `stringResource(Res.string.calendar_stats_days_tracked)` |
| Previous month cd (~L121) | `"Previous month"` | `stringResource(Res.string.calendar_cd_previous_month)` |
| Next month cd (~L130) | `"Next month"` | `stringResource(Res.string.calendar_cd_next_month)` |
| Day headers list (~L138) | `listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")` | Replace with: `listOf(stringResource(Res.string.calendar_day_mon), stringResource(Res.string.calendar_day_tue), stringResource(Res.string.calendar_day_wed), stringResource(Res.string.calendar_day_thu), stringResource(Res.string.calendar_day_fri), stringResource(Res.string.calendar_day_sat), stringResource(Res.string.calendar_day_sun))` |
| Perfect legend (~L192) | `"Perfect"` | `stringResource(Res.string.calendar_legend_perfect)` |
| Best Effort legend (~L193) | `"Best Effort"` | `stringResource(Res.string.calendar_legend_best_effort)` |
| Partial legend (~L194) | `"Partial"` | `stringResource(Res.string.calendar_legend_partial)` |
| Rough Day legend (~L200) | `"Rough Day"` | `stringResource(Res.string.calendar_legend_rough_day)` |
| Failed legend (~L201) | `"Failed"` | `stringResource(Res.string.common_failed)` |
| Future legend (~L202) | `"Future"` | `stringResource(Res.string.calendar_legend_future)` |

- [ ] **Step 3: Build to verify**

```bash
./gradlew :composeApp:compileKotlinAndroid --quiet
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/composeResources/values/strings_calendar.xml \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/calendar/CalendarScreen.kt
git commit -m "feat(resources): extract CalendarScreen strings"
```

---

## Task 6: ArchivedHabitsScreen

**Files:**
- Create: `composeApp/src/commonMain/composeResources/values/strings_archived.xml`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/archived/ArchivedHabitsScreen.kt`

- [ ] **Step 1: Create strings_archived.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="archived_title">Archived Habits</string>
    <string name="archived_empty_state_heading">No archived habits</string>
    <string name="archived_empty_state_subtext">Archived habits will appear here</string>
    <string name="archived_best_streak">Best streak: %d days</string>
    <string name="archived_cd_restore">Restore</string>
    <string name="archived_cd_delete">Delete permanently</string>
</resources>
```

- [ ] **Step 2: Update ArchivedHabitsScreen.kt**

Add the imports from the Imports Pattern section.

| Location | Old | New |
|---|---|---|
| Title (~L51) | `"Archived Habits"` | `stringResource(Res.string.archived_title)` |
| Back cd (~L54) | `"Back"` | `stringResource(Res.string.common_cd_back)` |
| Empty state heading (~L81) | `"No archived habits"` | `stringResource(Res.string.archived_empty_state_heading)` |
| Empty state subtext (~L87) | `"Archived habits will appear here"` | `stringResource(Res.string.archived_empty_state_subtext)` |
| Best streak (~L154) | `"Best streak: ${habit.longestStreak} days"` | `stringResource(Res.string.archived_best_streak, habit.longestStreak)` |
| Restore cd (~L164) | `"Restore"` | `stringResource(Res.string.archived_cd_restore)` |
| Delete cd (~L171) | `"Delete permanently"` | `stringResource(Res.string.archived_cd_delete)` |

- [ ] **Step 3: Build to verify**

```bash
./gradlew :composeApp:compileKotlinAndroid --quiet
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/composeResources/values/strings_archived.xml \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/archived/ArchivedHabitsScreen.kt
git commit -m "feat(resources): extract ArchivedHabitsScreen strings"
```

---

## Task 7: QuantitativeInputBottomSheet

**Files:**
- Create: `composeApp/src/commonMain/composeResources/values/strings_quantitative_input.xml`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/QuantitativeInputBottomSheet.kt`

- [ ] **Step 1: Create strings_quantitative_input.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="quantitative_input_title">Add progress</string>
    <string name="quantitative_input_current_with_unit">Current: %1$d/%2$d %3$s</string>
    <string name="quantitative_input_current_no_unit">Current: %1$d/%2$d</string>
    <string name="quantitative_input_label_amount">Amount</string>
    <string name="quantitative_input_quick_add">+%d</string>
    <string name="quantitative_input_button_add">Add</string>
</resources>
```

- [ ] **Step 2: Update QuantitativeInputBottomSheet.kt**

Add the imports from the Imports Pattern section.

| Location | Old | New |
|---|---|---|
| Title (~L52) | `"Add progress"` | `stringResource(Res.string.quantitative_input_title)` |
| Current progress (~L69) | `"Current: ${habit.completedValue ?: 0}/${habit.targetValue}${habit.unit?.let { " $it" } ?: ""}"` | `if (habit.unit != null) stringResource(Res.string.quantitative_input_current_with_unit, habit.completedValue ?: 0, habit.targetValue, habit.unit) else stringResource(Res.string.quantitative_input_current_no_unit, habit.completedValue ?: 0, habit.targetValue)` |
| Amount label (~L90) | `"Amount"` | `stringResource(Res.string.quantitative_input_label_amount)` |
| Quick add button (~L115) | `"+$amount"` | `stringResource(Res.string.quantitative_input_quick_add, amount)` — this replacement is inside the `forEach { amount -> }` lambda body: replace `Text("+$amount")` with `Text(stringResource(Res.string.quantitative_input_quick_add, amount))` |
| Cancel button (~L130) | `"Cancel"` | `stringResource(Res.string.common_cancel)` |
| Add button (~L143) | `"Add"` | `stringResource(Res.string.quantitative_input_button_add)` |

- [ ] **Step 3: Build to verify**

```bash
./gradlew :composeApp:compileKotlinAndroid --quiet
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/composeResources/values/strings_quantitative_input.xml \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/QuantitativeInputBottomSheet.kt
git commit -m "feat(resources): extract QuantitativeInputBottomSheet strings"
```

---

## Task 8: PhilosophyScreen

**Files:**
- Create: `composeApp/src/commonMain/composeResources/values/strings_onboarding_philosophy.xml`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/PhilosophyScreen.kt`

- [ ] **Step 1: Create strings_onboarding_philosophy.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="philosophy_heading">🔒 HabitLock enforces what you commit to</string>
    <string name="philosophy_body">HabitLock isn\'t about motivation or reminders alone.\nIt\'s about keeping promises to yourself — even on hard days.\n\nYou choose the rules.\nHabitLock helps you stick to them.</string>
</resources>
```

Note: apostrophes in XML must be escaped as `\'` — already applied above.

- [ ] **Step 2: Update PhilosophyScreen.kt**

Add the imports from the Imports Pattern section.

| Location | Old | New |
|---|---|---|
| Heading (~L34) | `"🔒 HabitLock enforces what you commit to"` | `stringResource(Res.string.philosophy_heading)` |
| Body text (~L43) | `"HabitLock isn't about motivation..."` (multi-line) | `stringResource(Res.string.philosophy_body)` |
| Continue button (~L58) | `"Continue"` | `stringResource(Res.string.common_continue)` |
| Skip button (~L64) | `"Skip"` | `stringResource(Res.string.common_skip)` |

- [ ] **Step 3: Build to verify**

```bash
./gradlew :composeApp:compileKotlinAndroid --quiet
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/composeResources/values/strings_onboarding_philosophy.xml \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/PhilosophyScreen.kt
git commit -m "feat(resources): extract PhilosophyScreen strings"
```

---

## Task 9: FirstHabitScreen

**Files:**
- Create: `composeApp/src/commonMain/composeResources/values/strings_onboarding_first_habit.xml`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/FirstHabitScreen.kt`

- [ ] **Step 1: Create strings_onboarding_first_habit.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="first_habit_heading">Lock in your first habit</string>
    <string name="first_habit_subtext">Start small. One habit is enough to begin.</string>
    <string name="first_habit_label_name">Habit name</string>
    <string name="first_habit_label_type">Habit type</string>
    <string name="first_habit_type_binary">Yes/No</string>
    <string name="first_habit_label_target_value">Target value</string>
    <string name="first_habit_label_unit">Unit (optional)</string>
    <string name="first_habit_placeholder_unit">E.g. glasses, pages, minutes</string>
    <string name="first_habit_button_create">Create habit</string>
    <string name="first_habit_button_skip">Skip for now</string>
</resources>
```

- [ ] **Step 2: Update FirstHabitScreen.kt**

Add the imports from the Imports Pattern section.

| Location | Old | New |
|---|---|---|
| Heading (~L50) | `"Lock in your first habit"` | `stringResource(Res.string.first_habit_heading)` |
| Subtext (~L59) | `"Start small. One habit is enough to begin."` | `stringResource(Res.string.first_habit_subtext)` |
| Name label (~L70) | `"Habit name"` | `stringResource(Res.string.first_habit_label_name)` |
| Name placeholder (~L71) | `"E.g. Drink water"` | `stringResource(Res.string.common_placeholder_habit_name)` |
| Type label (~L79) | `"Habit type"` | `stringResource(Res.string.first_habit_label_type)` |
| Yes/No chip (~L94) | `"Yes/No"` | `stringResource(Res.string.first_habit_type_binary)` |
| Quantitative chip (~L99) | `"Quantitative"` | `stringResource(Res.string.common_quantitative)` |
| Target value label (~L109) | `"Target value"` | `stringResource(Res.string.first_habit_label_target_value)` |
| Target value placeholder (~L110) | `"E.g. 8"` | `stringResource(Res.string.common_placeholder_target_value)` |
| Unit label (~L121) | `"Unit (optional)"` | `stringResource(Res.string.first_habit_label_unit)` |
| Unit placeholder (~L122) | `"E.g. glasses, pages, minutes"` | `stringResource(Res.string.first_habit_placeholder_unit)` |
| Create button (~L139) | `"Create habit"` | `stringResource(Res.string.first_habit_button_create)` |
| Skip button (~L145) | `"Skip for now"` | `stringResource(Res.string.first_habit_button_skip)` |

- [ ] **Step 3: Build to verify**

```bash
./gradlew :composeApp:compileKotlinAndroid --quiet
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/composeResources/values/strings_onboarding_first_habit.xml \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/FirstHabitScreen.kt
git commit -m "feat(resources): extract FirstHabitScreen strings"
```

---

## Task 10: StrictnessScreen

**Files:**
- Create: `composeApp/src/commonMain/composeResources/values/strings_onboarding_strictness.xml`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/StrictnessScreen.kt`

- [ ] **Step 1: Create strings_onboarding_strictness.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="strictness_heading">How strict should HabitLock be?</string>
    <string name="strictness_subtext">You\'re always in control. You can change this later.</string>
    <string name="strictness_flexible_label">Flexible</string>
    <string name="strictness_flexible_description">Gentle support with maximum forgiveness.</string>
    <string name="strictness_flexible_rule_1">• Unlimited undo</string>
    <string name="strictness_flexible_rule_2">• Unlimited snoozes</string>
    <string name="strictness_flexible_rule_3">• Skips allowed without limits</string>
    <string name="strictness_flexible_rule_4">• Missed habits are tracked, but lightly enforced</string>
    <string name="strictness_balanced_label">Balanced</string>
    <string name="strictness_balanced_description">Structure with room for real life.</string>
    <string name="strictness_balanced_rule_1">• Undo allowed for today only</string>
    <string name="strictness_balanced_rule_2">• Snoozes are limited</string>
    <string name="strictness_balanced_rule_3">• Skips are limited</string>
    <string name="strictness_balanced_rule_4">• Missed habits fail at the end of the day</string>
    <string name="strictness_locked_label">Locked</string>
    <string name="strictness_locked_description">No excuses. Full accountability.</string>
    <string name="strictness_locked_rule_1">• No undo</string>
    <string name="strictness_locked_rule_2">• Snoozes are capped</string>
    <string name="strictness_locked_rule_3">• Skips are capped</string>
    <string name="strictness_locked_rule_4">• Missed habits always fail</string>
    <string name="strictness_badge_recommended">(Recommended)</string>
</resources>
```

- [ ] **Step 2: Update StrictnessScreen.kt**

Add the imports from the Imports Pattern section.

`PresetCard` receives `rules: List<String>`. Resolve all `stringResource` calls before building each list — they must be called within a `@Composable` scope, which `StrictnessScreen` already provides. The bullet prefix (`• `) is embedded in each resource value, so remove any `"• $rule"` concatenation at the call site — the resource value already contains it.

| Location | Old | New |
|---|---|---|
| Heading (~L47) | `"How strict should HabitLock be?"` | `stringResource(Res.string.strictness_heading)` |
| Subtext (~L56) | `"You're always in control..."` | `stringResource(Res.string.strictness_subtext)` |
| Flexible label (~L70) | `"Flexible"` | `stringResource(Res.string.strictness_flexible_label)` |
| Flexible description (~L71) | `"Gentle support with maximum forgiveness."` | `stringResource(Res.string.strictness_flexible_description)` |
| Flexible rules list (~L73–76) | `listOf("Unlimited undo", ...)` | `listOf(stringResource(Res.string.strictness_flexible_rule_1), stringResource(Res.string.strictness_flexible_rule_2), stringResource(Res.string.strictness_flexible_rule_3), stringResource(Res.string.strictness_flexible_rule_4))` |
| Balanced label (~L84) | `"Balanced"` | `stringResource(Res.string.strictness_balanced_label)` |
| Balanced description (~L85) | `"Structure with room for real life."` | `stringResource(Res.string.strictness_balanced_description)` |
| Balanced rules list (~L87–90) | `listOf("Undo allowed for today only", ...)` | `listOf(stringResource(Res.string.strictness_balanced_rule_1), stringResource(Res.string.strictness_balanced_rule_2), stringResource(Res.string.strictness_balanced_rule_3), stringResource(Res.string.strictness_balanced_rule_4))` |
| Locked label (~L99) | `"Locked"` | `stringResource(Res.string.strictness_locked_label)` |
| Locked description (~L100) | `"No excuses. Full accountability."` | `stringResource(Res.string.strictness_locked_description)` |
| Locked rules list (~L102–105) | `listOf("No undo", ...)` | `listOf(stringResource(Res.string.strictness_locked_rule_1), stringResource(Res.string.strictness_locked_rule_2), stringResource(Res.string.strictness_locked_rule_3), stringResource(Res.string.strictness_locked_rule_4))` |
| Recommended badge (~L183) | `"(Recommended)"` | `stringResource(Res.string.strictness_badge_recommended)` |
| Continue button (~L121) | `"Continue"` | `stringResource(Res.string.common_continue)` |
| Skip button (~L127) | `"Skip"` | `stringResource(Res.string.common_skip)` |

- [ ] **Step 3: Build to verify**

```bash
./gradlew :composeApp:compileKotlinAndroid --quiet
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/composeResources/values/strings_onboarding_strictness.xml \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/onboarding/StrictnessScreen.kt
git commit -m "feat(resources): extract StrictnessScreen strings"
```

---

## Final Verification

- [ ] **Run unit tests**

```bash
./gradlew :composeApp:testDebugUnitTest --quiet
```

Expected: all tests pass (this is a pure refactor, no tests should change).
