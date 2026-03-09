# HabitLock – Onboarding Specification & Copy

## 1. Purpose
The onboarding flow introduces users to HabitLock’s core philosophy and allows them to **consciously choose their level of strictness** before using the app.

Onboarding is **mandatory on first launch** but:
- Can be skipped at any time
- Settings chosen during onboarding can be changed later

The goal is expectation-setting, not feature education.

---

## 2. Onboarding Flow Overview

The onboarding consists of **three screens**:
1. Philosophy
2. Strictness Selection
3. First Habit Creation (optional but recommended)

Maximum total time: ~30 seconds.

---

## 3. Screen 1 – Philosophy

### Purpose
Align the user’s mindset with HabitLock’s enforcement-based design.

### UI Elements
- Title
- Short body text
- Primary action button

### Copy

**Title:**  
🔒 HabitLock enforces what you commit to

**Body:**  
HabitLock isn’t about motivation or reminders alone.  
It’s about keeping promises to yourself — even on hard days.

You choose the rules.  
HabitLock helps you stick to them.

**Primary Button:**  
Continue

**Secondary Action:**  
Skip

---

## 4. Screen 2 – Strictness Selection

### Purpose
Allow the user to explicitly choose how strict the app should be.
This choice frames HabitLock as a **tool they control**, not a system that punishes them.

### UI Elements
- Title
- Subtitle
- Three selectable preset cards
- Confirmation button

### Copy

**Title:**  
How strict should HabitLock be?

**Subtitle:**  
You’re always in control. You can change this later.

---

### Preset 1 – Flexible

**Label:**  
🟢 Flexible

**Description:**  
Gentle support with maximum forgiveness.

**Rules Summary:**
- Unlimited undo
- Unlimited snoozes
- Skips allowed without limits
- Missed habits are tracked, but lightly enforced

---

### Preset 2 – Balanced (Recommended)

**Label:**  
🟡 Balanced

**Description:**  
Structure with room for real life.

**Rules Summary:**
- Undo allowed for today only
- Snoozes are limited
- Skips are limited
- Missed habits fail at the end of the day

---

### Preset 3 – Locked

**Label:**  
🔴 Locked

**Description:**  
No excuses. Full accountability.

**Rules Summary:**
- No undo
- Snoozes are capped
- Skips are capped
- Missed habits always fail

---

**Primary Button:**  
Continue

**Secondary Action:**  
Skip (defaults to Balanced)

---

## 5. Screen 3 – First Habit Creation

### Purpose
Reduce friction by getting the user to create at least one habit immediately.

This screen can be skipped.

### UI Elements
- Title
- Habit name input
- Optional reminder time selector
- Save button

### Copy

**Title:**  
Lock in your first habit

**Body:**  
Start small. One habit is enough to begin.

**Habit Name Placeholder:**  
E.g. Drink water

**Reminder Label:**  
Optional reminder

**Primary Button:**  
Create habit

**Secondary Action:**  
Skip for now

---

## 6. Completion Behavior

On completion or skip:
- `user.onboardingCompleted = true`
- Selected strictness preset is applied to user settings
- User is taken to the **Today** screen

---

## 7. Design & Tone Guidelines

- Calm, confident, and honest
- No hype or motivational clichés
- No animations required for MVP
- Copy should feel respectful and intentional

---

## 8. Non-Goals

The onboarding does NOT:
- Teach how to use every feature
- Explain streak mechanics in detail
- Lock the user into irreversible settings

---

**End of Onboarding Specification**