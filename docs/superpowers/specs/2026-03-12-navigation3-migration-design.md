# Navigation 3 Migration Design

**Date:** 2026-03-12
**Status:** Draft

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
- **Replace root** (onboarding completion) = `backStack.clear(); backStack.add(Route.Today)`

The `when (backStack.last())` block acts as the `NavDisplay` equivalent.

**VM event collectors:** The top-level `LaunchedEffect(Unit)` collectors for `OnboardingViewModel`, `TodayViewModel`, `SettingsViewModel`, `ArchivedHabitsViewModel`, and `CalendarViewModel` remain at the top level of `HabitLockNavHost`. The `HabitFormViewModel` collectors are intentionally kept inside the `CreateHabit` and `EditHabit` branches (inside the `when` block), scoped to the lifetime of those route entries — this is safe because both branches create a fresh VM on each entry via `remember { createHabitFormViewModel(...) }`, ensuring no stale collector is left running after the entry is popped.

**`rememberCoroutineScope`:** The existing `scope` variable is only used for drawer open/close operations, which are removed. It must be deleted. `SnackbarHostState.showSnackbar` is called inside `LaunchedEffect` coroutines, which have their own scope and do not need an external `CoroutineScope`.

## Route Changes

- Remove `Route.Onboarding` — unused duplicate of `Route.OnboardingPhilosophy`.
- All other routes unchanged.

### Back stack behaviour per destination

| Destination | Behaviour |
|---|---|
| `OnboardingPhilosophy` | Initial root when onboarding not completed |
| `OnboardingStrictness`, `OnboardingFirstHabit` | Pushed linearly (`backStack.add`) |
| Onboarding → Today | `backStack.clear()` + `backStack.add(Route.Today)` so back cannot return to onboarding |
| `Today` | Root when onboarding completed; never popped |
| `Calendar`, `Settings`, `CreateHabit`, `EditHabit`, `ArchivedHabits` | Pushed on top; popped by `backStack.removeLastOrNull()` |
| `HabitDetail` | Currently redirects immediately back to `Today` via `LaunchedEffect`. Behaviour preserved as-is — placeholder until the screen is implemented. |

## Navigation Structure

The navigation drawer is removed. Calendar and Settings are reached via icon buttons in `TodayScreen`'s top bar.

- `TodayScreen` top bar: replace hamburger menu icon with **Calendar icon** (`onCalendarClick`) and **Settings icon** (`onSettingsClick`)
- `CalendarScreen`, `SettingsScreen`, `ArchivedHabitsScreen`: the `onBackClick` wire-up inside `HabitLockNavHost` must be changed from hardcoded route assignments (`currentRoute = Route.Today`, `currentRoute = Route.Settings`) to `backStack.removeLastOrNull()`. The screen composables themselves do not change.

## Files Changed

| File | Change |
|---|---|
| `navigation/Route.kt` | Remove `Route.Onboarding` |
| `navigation/HabitLockNavHost.kt` | Replace `currentRoute` with `backStack`; remove `drawerState`, `scope`, `selectedDrawerDestination`, `DrawerDestination` import, `AppNavigationDrawer` import and wrapper; update all `onBackClick` lambdas to `backStack.removeLastOrNull()`; remove `Route.Onboarding` branch |
| `ui/components/AppNavigationDrawer.kt` | **Delete** |
| `ui/today/TodayScreen.kt` | Replace `onMenuClick` with `onCalendarClick` + `onSettingsClick`; update top bar icons |

## What Does Not Change

- `HabitLockNavHost` composable signature (all ViewModels passed as parameters from `App.kt`)
- `HabitFormViewModel` factory pattern (`createHabitFormViewModel: (String?) -> HabitFormViewModel`); `remember(route.habitId)` in `EditHabit` branch ensures a fresh VM per entry. `CreateHabit` uses `remember` without a key — this is safe because `Route.CreateHabit` is a `data object`; navigating away always pops it off the back stack, so re-entering always starts a new composition scope with a fresh `remember` cell
- `CalendarScreen`, `SettingsScreen`, `ArchivedHabitsScreen`, `HabitFormScreen` composable implementations
- `SnackbarHostState` and top-level `LaunchedEffect` event collectors for all non-form ViewModels
- No changes to `libs.versions.toml` or `build.gradle.kts` — no new dependency

## Migration Path to Official Navigation 3

When JetBrains ships a KMP fork of `androidx.navigation3`:

1. Replace `mutableStateListOf(...)` with `rememberNavBackStack(...)`
2. Wrap the `when` block with `NavDisplay` + `entryProvider { entry<Route> { ... } }`
3. Optionally add `rememberViewModelStoreNavEntryDecorator` if scoping VMs per entry is desired

The route types (`sealed interface Route`) require no changes.
