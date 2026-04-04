# Notification Permission Onboarding — Design Spec

**Date:** 2026-04-05
**Reference mockup:** Dark theme Stitch screen (`notification-permission-onboarding-scree-2026-04-04T22-24-13-983Z-1-frame.html`)

## Overview

Add a notification permission step to the onboarding wizard. The screen explains what notifications Rite sends and requests the Android 13+ `POST_NOTIFICATIONS` runtime permission. Replaces the cold system dialog currently fired in `MainActivity.onCreate()`.

## Placement

New step in the onboarding wizard, inserted **after Strictness selection** (step 1) and **before First Habit** (currently step 2).

- **Conditionally shown**: only when `Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU` (API 33+)
- On older devices the wizard skips this step entirely — step indices adjust so the user sees no gap
- Zero-indexed: Philosophy (0) → Strictness (1) → Notification Permission (2, conditional) → First Habit (2 or 3)

## Screen Layout

Follows the existing onboarding step pattern: top-aligned content with 24dp horizontal padding, headline + accent line + body, bottom-pinned CTA area.

### Content Area

1. **Headline**: "With you\nevery step." — `headlineLarge`, `FontWeight.ExtraBold`, `onSurface`
2. **Spacer**: 14dp
3. **Accent line**: 36dp wide, 3dp tall, `primary`, 2dp corner radius
4. **Spacer**: 16dp
5. **Body text**: "Rite uses notifications to keep you on track — reminders when it's time, and warnings before a habit is marked as failed." — `bodyLarge`, `onSurfaceVariant`
6. **Spacer**: 24dp
7. **Notification preview cards** — three cards stacked vertically, 10dp gap between them:

| Icon | Title | Subtitle |
|---|---|---|
| `Icons.Outlined.NotificationsActive` | Habit reminders | A nudge when it's time to show up |
| `Icons.Outlined.Warning` | Deadline warnings | A last chance before it's marked as failed |
| `Icons.Outlined.ShowChart` | Progress tracking | Your day's progress from the notification shade |

**Card styling:**
- Background: `surfaceContainerLow`
- Corner radius: 16dp
- Internal padding: 16dp
- Layout: icon (left, in a subtle circular container) + column (title bold `titleSmall` + subtitle `bodySmall` `onSurfaceVariant`)

### Bottom CTA Area

Uses the existing `CtaContainer` pattern (24dp horizontal padding, 16dp vertical padding, fade+translate animation).

- **Primary button** (`PrimaryButton`): "Enable notifications" — triggers the system `POST_NOTIFICATIONS` permission dialog
- **Spacer**: 4dp
- **Text button** (`TextButton`): "Maybe later" — `labelLarge`, `onSurfaceVariant`

### Decorative Background

Reuse the same decorative ring from `PhilosophyStep` — large `CircleShape` border at `primary.copy(alpha = 0.12f)`, offset to top-end, rotated 12 degrees.

## Behavior

1. **"Enable notifications" tapped**: launches the system `POST_NOTIFICATIONS` permission request via `ActivityResultContracts.RequestPermission`. Regardless of grant or deny result, the wizard advances to the First Habit step.
2. **"Maybe later" tapped**: wizard advances to the First Habit step without requesting permission.
3. **Back navigation**: returns to Strictness step (handled by existing `BackHandler` in `OnboardingWizard`).

## Permission Request Mechanism

The `POST_NOTIFICATIONS` permission request requires an Android `Activity` context. Since the onboarding composables live in shared KMP code:

- Define an `expect fun requestNotificationPermission(onResult: (Boolean) -> Unit)` in commonMain, or pass a callback lambda from the Android-side composition
- The simplest approach: pass a `onRequestNotificationPermission: (() -> Unit) -> Unit` lambda from `MainActivity` through the composition, which registers and launches `ActivityResultContracts.RequestPermission`
- The exact bridging mechanism is an implementation detail — the spec defines the behavior, not the wiring

## Cleanup

- Remove `requestNotificationPermissionIfNeeded()` from `MainActivity.onCreate()` and the associated `requestPermissionLauncher` field — this screen fully replaces that cold request.

## Animations

Match the existing onboarding steps:
- Headline: fade + translate up (200ms)
- Accent line: width draws in (250ms, 80ms delay)
- Body text: fade in (200ms, 140ms delay)
- Cards: fade in sequentially (staggered ~100ms each, after body)
- CTA area: fade + translate up (200ms, 200ms delay) — handled by `CtaContainer`

## String Resources

New resource file: `strings_onboarding_notifications.xml`

| Key | Value |
|---|---|
| `notifications_heading` | With you\nevery step. |
| `notifications_body` | Rite uses notifications to keep you on track — reminders when it\'s time, and warnings before a habit is marked as failed. |
| `notifications_card_reminders_title` | Habit reminders |
| `notifications_card_reminders_subtitle` | A nudge when it\'s time to show up |
| `notifications_card_warnings_title` | Deadline warnings |
| `notifications_card_warnings_subtitle` | A last chance before it\'s marked as failed |
| `notifications_card_tracking_title` | Progress tracking |
| `notifications_card_tracking_subtitle` | Your day\'s progress from the notification shade |
| `notifications_cta_enable` | Enable notifications |
| `notifications_cta_later` | Maybe later |

## Testing

- Screenshot tests for dark and light themes (matching existing `PhilosophyStepScreenshotTest` pattern)
- Unit test: wizard step index adjusts correctly based on API level
- The permission request itself is not unit-testable (system dialog), but the state transitions (advance after grant/deny/skip) should be tested in the ViewModel
