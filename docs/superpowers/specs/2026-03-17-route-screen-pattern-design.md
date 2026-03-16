# Route/Screen Pattern — Design Spec

**Date:** 2026-03-17
**Branch:** `feature/nav-route-pattern`
**Status:** Approved

## Overview

Refactor `HabitLockNavigation` to apply the `FooRoute`/`FooScreen` pattern: each screen file owns
its ViewModel retrieval, state collection, and event handling. `HabitLockNavigation` shrinks to
back-stack management and navigation lambdas only.

## Motivation

All ViewModels are currently injected into `HabitLockNavigation` and wired inline in `entryProvider`
entries. This is poor separation of concerns — screen-specific VM logic is co-located with routing
logic. The pattern mirrors what `hiltViewModel()` achieves on Hilt projects.

## Approach: Same-name overloads with `private` inner Screen

Each `XxxScreen.kt` file contains two overloads of the same composable function:

- **Public outer (Route):** Gets ViewModel from `LocalAppComponent`, collects state, handles events
  via `LaunchedEffect`, calls the private inner Screen.
- **Private inner (Screen):** Stateless — takes explicit state + lambda callbacks, renders layout.

The `private` modifier enforces the contract: callers outside the file can only reach the Route
overload. No separate naming (`FooRoute`/`FooScreen`) is needed.

## DI Layer

A new `CompositionLocal` is defined:

```kotlin
// presentation/navigation/LocalAppComponent.kt
val LocalAppComponent = staticCompositionLocalOf<HabitLockAppComponent> {
    error("No HabitLockAppComponent provided")
}
```

`App.kt` provides it wrapping `HabitLockNavigation`:

```kotlin
CompositionLocalProvider(LocalAppComponent provides appComponent) {
    HabitLockNavigation(isOnboardingCompleted = currentState.isOnboardingCompleted)
}
```

`HabitLockNavigation`'s signature shrinks from 6 VM parameters + factory to just:

```kotlin
fun HabitLockNavigation(isOnboardingCompleted: Boolean)
```

## Screen File Structure

```kotlin
// Public outer — Route layer
@Composable
fun TodayScreen(
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNavigateToHabitDetail: (String) -> Unit,
    onNavigateToCreateHabit: () -> Unit,
    onEditHabit: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = LocalAppComponent.current.todayViewModel
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is TodayEvent.NavigateToHabitDetail -> onNavigateToHabitDetail(event.instanceId)
                TodayEvent.NavigateToCreateHabit -> onNavigateToCreateHabit()
                is TodayEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is TodayEvent.ShowSuccess -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    TodayScreen(state = state, onCalendarClick = onCalendarClick, ...)
}

// Private inner — Screen layer (stateless)
@Composable
private fun TodayScreen(state: TodayState, ...) { /* layout — unchanged */ }
```

`snackbarHostState` is owned by `HabitLockNavigation` (shared across all screens) and passed as a
parameter into each Route. Routes call `snackbarHostState.showSnackbar(...)` directly inside their
`LaunchedEffect`.

## `HabitLockNavigation` After Refactor

Contains only: `savedStateConfig`, `backStack`, `snackbarHostState`, and `entryProvider` entries
that call Routes with navigation lambdas.

```kotlin
@Composable
fun HabitLockNavigation(isOnboardingCompleted: Boolean) {
    val backStack = rememberNavBackStack(savedStateConfig, ...)
    val snackbarHostState = remember { SnackbarHostState() }

    NavDisplay(...) {
        entry<Today> {
            TodayScreen(
                onCalendarClick = { backStack.add(Calendar) },
                onSettingsClick = { backStack.add(Settings) },
                onNavigateToHabitDetail = { backStack.add(HabitDetail(it)) },
                onNavigateToCreateHabit = { backStack.add(CreateHabit) },
                onEditHabit = { backStack.add(EditHabit(it)) },
                snackbarHostState = snackbarHostState
            )
        }
        // ... other entries
    }
}
```

No VM imports. No `LaunchedEffect` collectors.

## Special Cases

### Onboarding (3 screens, 1 ViewModel)

All three Route overloads independently call `LocalAppComponent.current.onboardingViewModel`.
Since it is `@AppScope`, they all receive the same instance. Each screen only collects the events
relevant to it — safe because `MutableSharedFlow` has no replay and only the currently composed
Route is active.

### HabitForm (unscoped VM with factory)

`HabitFormScreen` Route accepts `habitIdToEdit: String?` and creates the VM via:

```kotlin
val factory = LocalAppComponent.current.habitFormViewModelFactory
val viewModel = remember(habitIdToEdit) { factory.create(habitIdToEdit) }
```

`remember(habitIdToEdit)` ensures a fresh VM for create (`null`) vs edit (non-null ID).
Both `CreateHabit` and `EditHabit` nav entries call the same Route overload with different
`habitIdToEdit` values.

### HabitForm → Today reload (known pain point)

`onNavigateBack` in `HabitFormScreen`'s entry calls both `backStack.removeLastOrNull()` and
`todayViewModel.loadTodayHabits()`. This couples `HabitLockNavigation` to `TodayViewModel`'s
internals. Deferred fix: a shared `onHabitChanged` event that `TodayViewModel` subscribes to
independently. See `CLAUDE.md`.

## Files Changed

| File | Change |
|------|--------|
| `presentation/navigation/LocalAppComponent.kt` | New — defines `LocalAppComponent` |
| `App.kt` | Add `CompositionLocalProvider`; remove VM args from `HabitLockNavigation` call |
| `presentation/navigation/HabitLockNavigation.kt` | Remove VM params + `LaunchedEffect` collectors; entries call Routes |
| `presentation/ui/today/TodayScreen.kt` | Add public Route overload; mark existing composable `private` |
| `presentation/ui/calendar/CalendarScreen.kt` | Add public Route overload; mark existing composable `private` |
| `presentation/ui/settings/SettingsScreen.kt` | Add public Route overload; mark existing composable `private` |
| `presentation/ui/archived/ArchivedHabitsScreen.kt` | Add public Route overload; mark existing composable `private` |
| `presentation/ui/habit/HabitFormScreen.kt` | Add public Route overload; mark existing composable `private` |
| `presentation/ui/onboarding/PhilosophyScreen.kt` | Add public Route overload; mark existing composable `private` |
| `presentation/ui/onboarding/StrictnessScreen.kt` | Add public Route overload; mark existing composable `private` |
| `presentation/ui/onboarding/FirstHabitScreen.kt` | Add public Route overload; mark existing composable `private` |

`HabitFormEntry` private helper in `HabitLockNavigation.kt` is deleted (logic moves into
`HabitFormScreen` Route overload).

## Out of Scope

- Hoisting `Scaffold` to nav level (would clean up snackbar threading but is a larger change)
- Composable-scoped ViewModels via `rememberViewModelStoreOwner` (requires lifecycle 2.11.0-alpha02)
- `HabitDetail` screen implementation (still a TODO stub)
