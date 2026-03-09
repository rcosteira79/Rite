# HabitLock – Complete MVP & Onboarding Specification

---

## 1. Purpose

HabitLock is a **habit enforcing app** that lets users consciously choose their level of strictness and provides **actionable reminders** and **accountability** for daily and weekly habits.

Goals:
- Encourage consistency without punishing partial completion
- Allow user-defined rules (strictness, undo, snooze, skips)
- Track both **short-term streaks** and **long-term habit scores**
- Include **quantitative habits**, **non-daily cadences**, and **leave/suspension mode**

---

## 2. Onboarding Flow

### Overview
Three screens, mandatory on first launch (skippable anytime):

1. Philosophy
2. Strictness Selection
3. First Habit Creation (optional but recommended)

**Maximum total time:** ~30 seconds

---

### Screen 1 – Philosophy
- **Title:** 🔒 HabitLock enforces what you commit to
- **Body:** HabitLock isn’t about motivation alone. It’s about keeping promises to yourself. You choose the rules. HabitLock helps you stick to them.
- **Primary Button:** Continue
- **Secondary Action:** Skip

---

### Screen 2 – Strictness Selection
- **Title:** How strict should HabitLock be?
- **Subtitle:** You’re always in control. You can change this later.

#### Presets
1. **Flexible** (🟢)
    - Gentle support with maximum forgiveness
    - Unlimited undo
    - Unlimited snoozes
    - Unlimited skips
    - Missed habits lightly enforced

2. **Balanced** (🟡) – Recommended
    - Structure with room for real life
    - Undo allowed for today only
    - Limited snoozes
    - Limited skips
    - Missed habits fail at end of day

3. **Locked** (🔴)
    - Full accountability
    - No undo
    - Snoozes capped
    - Skips capped
    - Missed habits always fail

- **Primary Button:** Continue
- **Secondary Action:** Skip (defaults to Balanced)

---

### Screen 3 – First Habit Creation
- **Title:** Lock in your first habit
- **Body:** Start small. One habit is enough to begin.
- **Inputs:** Habit name, optional reminder time
- **Primary Button:** Create habit
- **Secondary Action:** Skip for now

---

### Completion Behavior
- `user.onboardingCompleted = true`
- Selected strictness preset applied
- User taken to Today screen

---

## 3. Core Data Model

### Time Model & Timezone Handling
- The app is **timezone-aware**
- The user has a stored `timezone`
- System timezone is checked:
  - On app launch
  - Before daily habit generation
- If timezone changed:
  - Update `User.timezone`
  - Store `previousTimezone`
  - Display a **dismissible UI warning**
- Past habit data is **never modified** due to timezone changes
- Day is defined as local calendar day: **[00:00:00, 23:59:59]** inclusive
- Day rollover occurs at **local midnight**

### Habit
- Name (required)
- Description (optional)
- Units (optional, quantitative habits)
- Cadence: DAILY | WEEKLY
- Quota: completions per cadence window
- Notifications: preferred times
- Leave Mode: optional start/end dates

### HabitInstance
- Status: `PENDING`, `COMPLETED`, `SKIPPED`, `SUSPENDED`, `FAILED`
- Represents habit occurrence in a cadence window

### User Model
- `id`
- `timezone`
- `previousTimezone` (nullable)
- Strictness preset
- Undo policy: none / today / all history
- Snooze policy: limit per habit, max duration 60 min
- `maxConsecutiveSkips` (configurable, typically 2)
- Streaks: per habit and perfect-day
- Habit Score: cumulative progress

---

## 4. Habit Types & Cadence

| Type                     | Cadence | Quota | Notifications                   | Action Buttons                         |
|--------------------------|--------|-------|---------------------------------|---------------------------------------|
| Daily, single            | DAILY  | 1     | user-defined daily time         | ✅ Complete / ⏰ Snooze / ❌ Skip      |
| Daily, quantitative      | DAILY  | N     | user-defined daily time         | ➕ +1 (+2 optional) / ⏰ Snooze / ❌ Skip |
| Weekly / non-daily       | WEEKLY | N     | user-defined days/times         | ➕ +1 (+2 optional) / ⏰ Snooze / ❌ Skip today |
| Leave Mode               | Any    | -     | none                            | N/A (status: SUSPENDED)               |

**Notes:**
- Quota can be exceeded; over-completion is encouraged
- Notifications display progress (`completed / quota`)
- "Skip today" only affects current instance
- **Quantitative habits:** Progress accumulates via multiple completion events per day; auto-complete when quota reached; undo removes/negates events and recalculates progress

---

## 5. Notifications

### Daily / Quantitative
- **Title:** `<Habit name>`
- **Body:** `<completed> / <quota> today`
- **Action Buttons:** +1 (+2 optional) / Snooze / Skip
- Auto-complete when quota reached

### Weekly / Non-Daily
- **Title:** `<Habit name>`
- **Body:** `<completed> / <quota> this week`
- Optional: “Week ends Sunday” / “X remaining”
- **Action Buttons:** +1 (+2 optional) / Snooze / Skip today
- Over-completion allowed; prompt user at end of cadence window

### Grace-Period Notifications
- Triggered after cadence window ends
- **Grace period duration:** until daily job executes
- Title: `Uncompleted habits`
- Body: `Complete them before tomorrow's habits or they will be marked as failed`
- No buttons

### Leave Mode
- No notifications
- Shown in Today screen as `SUSPENDED until <end date>`

---

## 6. Completion Rules
- Completing increments current cadence progress
- Skipping affects only current instance
- Undo allowed based on user preference, recalculates streaks and HabitScore
- Over-completion: tracked and reflected in HabitScore; optional prompt at end of cadence window to update quota

---

## 7. Failure Rules
- Daily: PENDING at end of day → fail
- Weekly: completed < quota at end of week → fail
- Suspended habits excluded

---

## 8. Streaks & Habit Score

### Streaks
- Increment if all instances in cadence window completed
- Reset on failure
- Daily: days, Weekly: weeks
- Perfect-day streak: only daily habits; increments only if ALL daily habits are completed on that day; reset on any daily habit failure

### Habit Score
- Formula: `HabitScore = min(OverCompletionCap, (TotalCompletions / ExpectedCompletions) * 100)`
- `OverCompletionCap` = 150 (allows recognition of 50% over-completion)
- Tracks cumulative performance
- Never resets to zero
- Optional: weight recent windows for momentum

### Display
- Habit card top: Streak
- Below streak: Habit Score % (progress bar optional)
- Progress in cadence window shown (`1 / 3 this week`)

---

## 9. Daily Engine

### Daily Habit Generation
Triggered by:
- Daily background job at specific time (except at first launch)

Steps:
1. Check timezone and update User if needed
2. Determine "today" (LocalDate)
3. Generate HabitInstances for all:
   - Active
   - Non-archived
   - Scheduled habits
4. Cancel daily job if generation already occurred

### End-of-Day Processing (Failure)

When day changes:
- All PENDING HabitInstances from previous day → FAILED
- SKIPPED remains SKIPPED
- COMPLETED remains COMPLETED

This is triggered by:
- Daily job

Grace notification is sent before final failure marking (see section 5 for grace period details).

---

## 11. Calendar & History View

### Purpose
Provides visual historical accuracy of habit completion with color-coded days.

### Day Classification
Each day is classified as:
- **PERFECT** (green): All daily habits completed, no failures
- **BEST EFFORT** (lighter green or yellow/green mix): All daily completed, with suspended habits
- **PARTIAL** (yellow): Some completed/skipped, at least one incomplete
- **ROUGH DAY** (orange): Some completed/skipped, at least one failure
- **FAILED** (red): All habits failed
- **FUTURE** (no color): Dates not yet reached

### Rules
- Calendar is read-only
- Shows past performance only
- Archived habits are included in historical data
- Undo rules still apply globally (undoing can change calendar colors)

---

## 12. Settings Screen

### User-Configurable Settings
Users can modify their preferences after onboarding:

1. **Strictness Preset**
   - Switch between Flexible, Balanced, Locked
   - Changes apply to new habit instances

2. **Undo Policy**
   - None / Today only / All history

3. **Snooze Settings**
   - Max snoozes per habit per day
   - Max snooze duration (up to 60 minutes)

4. **Skip Settings**
   - Max consecutive skips (typically 2)

5. **Notification Preferences**
   - Enable/disable notifications per habit
   - Update reminder times

6. **Habit Display**
   - Header + Completion map + tap to show footer
   - Header + Footer + tap to show completion map

### Archive vs Delete
- **Archive**: Habit stops generating new instances but remains visible in history; data preserved
- **Delete**: Complete removal of habit and all associated data (should require confirmation)

---

## 13. Leave Mode
- Temporary suspension of selected habits
- Suspended habits:
    - Do not generate notifications
    - Excluded from streaks and score
    - Displayed as `SUSPENDED`

---

## 14. Undo / Snooze / Skip Rules
- Undo: configurable; recalculates streak and score
- Snooze: configurable, max 60 min
- Skip: limited by strictness preset; affects only current notification instance

---

## 15. Notifications UX Principles
- Show numeric progress (`completed / quota`)
- “Today” vs “This week” for cadence clarity
- Action buttons consistent: +1 = increment, Skip = skip notification, Snooze = delay reminder
- Over-completion reflected in HabitScore
- Leave mode suppresses notifications

---

## 16. Today + Weekly View Mockup (Header + Footer)

    ------------------------------------------------------------
    | Today (Mon, Jan 20)                                    🔔 |
    ------------------------------------------------------------
    
    DAILY HABITS
    ------------------------------------------------------------
    [ ] Drink Water                      3 / 8 today         ✏️
    Streak: 5 days      Score: 72%
    (+1)  (+2)  Snooze  Skip
    
    [x] Take Meds                        2 / 2 today         ✏️
    Streak: 5 days      Score: 100%
    Completed ✅
    
    [ ] Morning Stretch                   0 / 1 today        ✏️
    Streak: 2 days      Score: 60%
    (+1)  Snooze  Skip
    ------------------------------------------------------------
    
    WEEKLY HABITS
    ------------------------------------------------------------
    [ ] Gym                               1 / 3 this week    ✏️
    Streak: 2 weeks    Score: 83%
    (+1)  Snooze  Skip today
    
    [ ] Cycling                            0 / 1 this week   ✏️
    Streak: 1 week     Score: 50%
    (+1)  Snooze  Skip today
    ------------------------------------------------------------
    
    SUSPENDED HABITS // swipe for early unsuspend
    ------------------------------------------------------------
    [ ] Yoga                               SUSPENDED until Fri
    [ ] Morning Run                        SUSPENDED until Thu
    ------------------------------------------------------------
    
    FOOTER
    ------------------------------------------------------------
    Tip: Complete all habits to maintain your streaks!

### Design Notes

1. Cards per habit:
   - Each habit is a “card” with subtle shadows and rounded corners (Material You style).
   - Background color can reflect habit type or status:
       - Pending = light neutral
       - Completed = green overlay
       - Suspended = grey overlay

2. Progress & Metrics:
   - Show current progress (completed / quota) prominently
   - Display streak and habit score in a small, secondary line
   - Action Buttons:
       - For daily single/quantitative: +1, +2, Snooze, Skip
       - For weekly/non-daily: +1, Snooze, Skip today
       - Buttons should be touch-friendly with color cues (green for +1, yellow for Snooze, red for Skip)

3. Leave Mode / Suspended:
   - Gray background, show suspension end date
   - No action buttons
   - Material You styling suggestions:
       - Rounded corners (12–16dp)
       - Slight elevation (2–4dp) for cards
       - Bold habit name, smaller secondary text for streak/score
       - Iconography optional: checkmarks for completed, clock for snooze

---

## 17. MVP Scope Notes
- Daily & weekly habits, quantitative habits, user-defined notification times
- Leave / suspension mode
- Over-completion handling & quota adjustment
- Undo / snooze / skip rules applied to all habit types
- Streaks and HabitScore displayed together
- Grace-period notifications for uncompleted habits
- Timezone-aware with warning on timezone changes
- Calendar/history view with color-coded days
- Local-only persistence (no cloud sync)

### Non-Goals (Explicit)
The MVP does NOT include:
- Backend / cloud sync
- Social features
- XP, leveling, or achievements UI
- Advanced notification escalation
- AI suggestions

### MVP Acceptance Criteria
The MVP is complete when:
- User can define habits (daily/weekly, binary/quantitative)
- Habits generate daily/weekly instances
- Notifications fire and actions work
- Snooze/skip/undo behave correctly
- Streaks and HabitScore update accurately
- Calendar reflects history truthfully
- Timezone changes do not corrupt data
- Settings can be modified after onboarding

---

**End of Complete HabitLock MVP & Onboarding Specification**
