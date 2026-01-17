# HabitLock – Notification Copy & Action Table (MVP)

This table defines **all HabitLock notifications**, including titles, bodies, and action buttons for daily, weekly, quantitative, and leave-mode habits. Designed to be **clear, unambiguous, and actionable**.

---

## 1. Daily Habits (Quota = 1)

| Habit Type       | Notification Timing       | Title        | Body                     | Action Buttons                |
|-----------------|--------------------------|-------------|--------------------------|-------------------------------|
| Daily Single    | User-defined daily time  | Habit name  | Due today                | ✅ Complete / ⏰ Snooze / ❌ Skip |

**Notes:**
- `Skip` only skips this instance.
- Completion increments habit instance to `COMPLETED`.

---

## 2. Daily Quantitative Habits (Quota > 1)

| Habit Type             | Notification Timing      | Title        | Body                     | Action Buttons                       |
|-----------------------|-------------------------|-------------|--------------------------|--------------------------------------|
| Daily Quantitative    | User-defined daily time | Habit name  | `<completed> / <quota> today` | ➕ +1 (optional: +2) / ⏰ Snooze / ❌ Skip |

**Notes:**
- Each +1 increments progress toward daily quota.
- Optional +2 allows quick batch logging.
- Habit marked complete automatically when quota reached.

**Examples:**
- `Drink water` → `3 / 8 today`
- `Take meds` → `1 / 2 today`

---

## 3. Weekly / Non-Daily Habits

| Habit Type        | Notification Timing                 | Title        | Body                                | Action Buttons                        |
|------------------|------------------------------------|-------------|------------------------------------|---------------------------------------|
| Weekly Habit      | User-selected days/times in week   | Habit name  | `<completed> / <quota> this week` | ➕ +1 (optional: +2) / ⏰ Snooze / ❌ Skip today |

**Optional Body Enhancements:**
- “Week ends Sunday”
- “X remaining” (for last few reminders in the week)

**Behavior Rules:**
- `Skip today` only skips this notification, does not affect quota
- Progress resets only at end of cadence window if quota not met
- Over-completion is allowed; app prompts user at end of week to update quota

**Examples:**
- `Gym` → `1 / 3 this week` (Tue reminder)
- `Cycling` → `0 / 1 this week` (Thu reminder)

---

## 4. Grace-Period Notifications (Informational Only)

| Habit Type        | Notification Timing                  | Title                | Body                                         | Action Buttons |
|------------------|-------------------------------------|--------------------|---------------------------------------------|----------------|
| Daily / Weekly   | After cadence window ends (midnight / Sunday 23:59) | Uncompleted habits | Complete them or they will be marked as failed | None |

**Notes:**
- Daily: triggered after midnight for previous day’s PENDING habits
- Weekly: triggered after Sunday 23:59 for weekly habits that did not meet quota
- No direct action; user must open app to resolve

---

## 5. Leave / Suspension Mode

| Habit Type        | Notification Timing                  | Title        | Body                                  | Action Buttons |
|------------------|-------------------------------------|-------------|--------------------------------------|----------------|
| Suspended Habit  | N/A                                 | Habit name  | `SUSPENDED until <end date>`         | None           |

**Notes:**
- Suspended habits do not generate notifications
- Do not affect streaks or failure calculations

---

## 6. Special Cases / Examples

### 6.1 Daily Quantitative Partial Completion

- **Title**: Drink water
- **Body**: 5 / 8 today
- **Action Buttons**: +1 / +2 / Snooze / Skip

### 6.2 Weekly Habit Mid-Week

- **Title**: Gym
- **Body**: 1 / 3 this week
- **Action Buttons**: +1 / Snooze / Skip today

### 6.3 Weekly Habit Last Reminder

- **Title**: Gym
- **Body**: 2 / 3 this week
- **Sub-body**: Last day
- **Action Buttons**: +1 / Snooze / Skip today

### 6.4 Over-Completion Prompt (End-of-Window)

- **Title**: Gym
- **Body**: You completed 4 sessions this week. Update quota for future weeks?
- **Action Buttons**: Yes / No


---

## 7. General UX Rules for Developers

1. **Numbers > Words:** Always show progress numerically (`completed / quota`).
2. **Cadence clarity:** Use “today” for daily habits, “this week” for weekly/non-daily.
3. **Buttons consistent:** +1 always increments progress; Skip only skips notification; Snooze delays reminder.
4. **Over-completion:** Must trigger end-of-window quota prompt.
5. **Leave Mode:** No notifications generated; visible in Today screen only.
6. **Notifications are per-habit:** Do not combine multiple habits in one notification.

---

This table and examples should allow you to **implement notifications directly** for Android, respecting cadence, quantitative habits, leave mode, and over-completion, without ambiguity.
