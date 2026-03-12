# Navigation 3 Migration Design

**Date:** 2026-03-12
**Status:** Approved

## Overview

Migrate `HabitLockNavHost` from a single `mutableStateOf<Route>` variable to a proper back stack using `SnapshotStateList<Route>`, mirroring the Navigation 3 mental model (`rememberNavBackStack` + `NavDisplay`). Remove the navigation drawer in favour of icon buttons on Today's top bar. No new library dependency — the pattern is implemented in `commonMain` for full KMP compatibility (Android + iOS + JVM).

## Architecture

The back stack is a `SnapshotStateList<Route>` owned in `HabitLockNavHost`:

```kotlin
val backStack = remember {
    mutableStateListOf(if (isOnboardingCompleted) Route.Today else Route.OnboardingPhilosophy)
}
```

- **Current screen** = `backStack.last()`
- **Navigate forward** = `backStack.add(route)`
- **Navigate back** = `backStack.removeLastOrNull()`
- **Replace root** = `backStack.clear(); backStack.add(route)` (used after onboarding completes)

The `when (backStack.last())` block acts as the `NavDisplay` equivalent.

All VM event collectors remain at the top level of `HabitLockNavHost` (no structural change). They now call `backStack.add(route)` instead of `currentRoute = route`.

## Route Changes

- Remove `Route.Onboarding` — unused duplicate of `Route.OnboardingPhilosophy`.
- All other routes unchanged.

### Back stack behaviour per destination

| Destination | Behaviour |
|---|---|
| `OnboardingPhilosophy` | Initial root when onboarding not completed |
| `OnboardingStrictness`, `OnboardingFirstHabit` | Pushed linearly |
| Onboarding → Today | `backStack.clear()` + `backStack.add(Route.Today)` so back cannot return to onboarding |
| `Today` | Root when onboarding completed; never popped |
| `Calendar`, `Settings`, `CreateHabit`, `EditHabit`, `ArchivedHabits` | Pushed on top; popped by back |

## Navigation Structure

The navigation drawer is removed. Calendar and Settings are reached via icon buttons in `TodayScreen`'s top bar.

- `TodayScreen` top bar: replace hamburger menu icon with **Calendar icon** (`onCalendarClick`) and **Settings icon** (`onSettingsClick`)
- `CalendarScreen` and `SettingsScreen`: no changes — back arrow already calls `onBackClick`, which pops the stack

## Files Changed

| File | Change |
|---|---|
| `navigation/Route.kt` | Remove `Route.Onboarding` |
| `navigation/HabitLockNavHost.kt` | Replace `currentRoute` with `backStack`; remove drawer state/scope; remove `Route.Onboarding` branch |
| `ui/components/AppNavigationDrawer.kt` | **Delete** |
| `ui/today/TodayScreen.kt` | Replace `onMenuClick` with `onCalendarClick` + `onSettingsClick`; update top bar icons |

## What Does Not Change

- `HabitLockNavHost` signature (all ViewModels passed as parameters from `App.kt`)
- `HabitFormViewModel` factory pattern (`createHabitFormViewModel: (String?) -> HabitFormViewModel`)
- `CalendarScreen`, `SettingsScreen`, `ArchivedHabitsScreen`, `HabitFormScreen` — no changes
- `rememberCoroutineScope` retained for `SnackbarHostState.showSnackbar`
- No changes to `libs.versions.toml` or `build.gradle.kts` — no new dependency

## Migration Path to Official Navigation 3

When JetBrains ships a KMP fork of `androidx.navigation3`:

1. Replace `mutableStateListOf(...)` with `rememberNavBackStack(...)`
2. Wrap the `when` block with `NavDisplay` + `entryProvider { entry<Route> { ... } }`
3. Optionally add `rememberViewModelStoreNavEntryDecorator` if scoping VMs per entry is desired

The route types (`sealed interface Route`) require no changes.
