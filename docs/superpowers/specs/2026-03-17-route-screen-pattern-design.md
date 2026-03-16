# Route/Screen Pattern — Design Spec

**Date:** 2026-03-17
**Branch:** `feature/nav-route-pattern`
**Status:** Approved

## Overview

Refactor `HabitLockNavigation` to apply the Route/Screen pattern: each screen file owns
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

A new `CompositionLocal` is defined in `di/` (alongside `HabitLockAppComponent.kt`):

```kotlin
// di/LocalAppComponent.kt
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

The public outer Route overload takes only the parameters the screen needs from outside:
- Navigation callbacks (for events that cause route changes)
- `snackbarHostState: SnackbarHostState` (for events that show messages)

Non-navigation ViewModel calls (e.g. `onCompleteClick`, `onSkipClick`) are wired directly to VM
methods inside the Route and passed as lambdas to the private inner Screen.

Example — `TodayScreen`:

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

    TodayScreen(
        state = state,
        onCalendarClick = onCalendarClick,
        onSettingsClick = onSettingsClick,
        onHabitClick = viewModel::navigateToHabitDetail,
        onCompleteClick = viewModel::completeHabit,
        onSkipClick = viewModel::skipHabit,
        onUndoClick = viewModel::undoHabit,
        onEditClick = onEditHabit,
        onArchiveClick = viewModel::archiveHabit,
        onAddHabitClick = viewModel::navigateToCreateHabit,
        onDismissTimezoneWarning = viewModel::dismissTimezoneWarning,
        snackbarHostState = snackbarHostState
    )

    // QuantitativeInputBottomSheet also moves into the Route overload:
    state.showQuantitativeInputFor?.let { instanceId ->
        val habit = state.habits.find { it.instanceId == instanceId }
        if (habit != null) {
            QuantitativeInputBottomSheet(
                habit = habit,
                onConfirm = { value -> viewModel.completeQuantitativeHabit(instanceId, value) },
                onDismiss = viewModel::dismissQuantitativeInput
            )
        }
    }
}

// Private inner — Screen layer (stateless, layout only)
@Composable
private fun TodayScreen(state: TodayState, ...) { /* unchanged */ }
```

## `HabitLockNavigation` After Refactor

Contains only: `savedStateConfig`, `backStack`, `snackbarHostState`, and `entryProvider` entries
that call Routes with navigation lambdas. No VM imports. No `LaunchedEffect` collectors.

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

## Special Cases

### Onboarding (3 screens, 1 ViewModel)

All three Route overloads independently call `LocalAppComponent.current.onboardingViewModel`.
Since it is `@AppScope`, they all receive the same instance.

Navigation events are forwarded via lambdas passed in from `HabitLockNavigation`. The
`NavigateToToday` event requires a `backStack.clear()` followed by `backStack.add(Today)` — this
cannot live inside a Route composable, since Routes don't own the back stack. `HabitLockNavigation`
provides an `onNavigateToToday: () -> Unit` lambda that performs the clear+add, and each onboarding
Route forwards the event to that lambda.

`PhilosophyScreen` is the simplest case: it has no state to collect (the ViewModel exposes no
`StateFlow` that `PhilosophyScreen` uses). Its Route overload is event-forwarding only:

```kotlin
@Composable
fun PhilosophyScreen(
    onNavigateToStrictness: () -> Unit,
    onNavigateToToday: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = LocalAppComponent.current.onboardingViewModel

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.NavigateToStrictness -> onNavigateToStrictness()
                OnboardingEvent.NavigateToFirstHabit -> Unit  // not reachable from PhilosophyScreen
                OnboardingEvent.NavigateToToday -> onNavigateToToday()
                is OnboardingEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    PhilosophyScreen(
        onContinue = viewModel::continueFromPhilosophy,
        onSkip = viewModel::skipToToday
    )
}
```

### CalendarScreen (no events)

`CalendarViewModel` emits no events — it exposes only `StateFlow<CalendarState>`. The
`CalendarScreen` Route overload needs no `LaunchedEffect`. It only collects state and wires
ViewModel methods as callbacks:

```kotlin
@Composable
fun CalendarScreen(onBackClick: () -> Unit) {
    val viewModel = LocalAppComponent.current.calendarViewModel
    val state by viewModel.state.collectAsStateWithLifecycle()

    CalendarScreen(
        state = state,
        onBackClick = onBackClick,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onDayClick = { viewModel.selectDay(it.date) }
    )
}
```

### HabitForm (unscoped VM with factory)

`HabitFormScreen` Route accepts `habitIdToEdit: String?` and creates the VM via:

```kotlin
val factory = LocalAppComponent.current.habitFormViewModelFactory
val viewModel = remember { factory.create(habitIdToEdit) }
```

No key is needed for `remember` — each `CreateHabit` and `EditHabit` back-stack entry is a
separate composable scope, so the VM is always fresh when the entry is first composed. The VM's
lifetime matches the composable's lifetime in the composition (i.e. it is destroyed when the
entry leaves the back stack), which is correct for a per-entry, unscoped ViewModel.

Both `CreateHabit` and `EditHabit` nav entries call the same Route overload with different
`habitIdToEdit` values (`null` for create, non-null for edit).

### HabitForm → Today reload (known pain point)

`onNavigateBack` in the `HabitFormScreen` entry calls both `backStack.removeLastOrNull()` and
`todayViewModel.loadTodayHabits()`. This couples `HabitLockNavigation` to `TodayViewModel`'s
internals. Deferred fix: a shared `onHabitChanged` event that `TodayViewModel` subscribes to
independently. See `CLAUDE.md`.

## Snackbar

`SnackbarHostState` is owned by `HabitLockNavigation` and passed as a parameter into each Route.
Several inner Screens render a `Scaffold` with `SnackbarHost(snackbarHostState)` — passing the
same instance into each is correct and intentional; they share the single snackbar. Simultaneous
snackbar calls queue (Material3 default behaviour). Hoisting the `Scaffold` to nav level is left
as a future improvement (see Out of Scope).

## Files Changed

| File | Change |
|------|--------|
| `di/LocalAppComponent.kt` | New — defines `LocalAppComponent` CompositionLocal |
| `App.kt` | Add `CompositionLocalProvider`; remove VM args from `HabitLockNavigation` call |
| `presentation/navigation/HabitLockNavigation.kt` | Remove VM params + `LaunchedEffect` collectors; entries call Routes with nav lambdas; delete `HabitFormEntry` helper |
| `presentation/ui/today/TodayScreen.kt` | Add public Route overload (including `QuantitativeInputBottomSheet`); mark existing composable `private` |
| `presentation/ui/calendar/CalendarScreen.kt` | Add public Route overload (no `LaunchedEffect`); mark existing composable `private` |
| `presentation/ui/settings/SettingsScreen.kt` | Add public Route overload; mark existing composable `private` |
| `presentation/ui/archived/ArchivedHabitsScreen.kt` | Add public Route overload; mark existing composable `private` |
| `presentation/ui/habit/HabitFormScreen.kt` | Add public Route overload; mark existing composable `private` |
| `presentation/ui/onboarding/PhilosophyScreen.kt` | Add public Route overload (event-forwarding only, no state); mark existing composable `private` |
| `presentation/ui/onboarding/StrictnessScreen.kt` | Add public Route overload; mark existing composable `private` |
| `presentation/ui/onboarding/FirstHabitScreen.kt` | Add public Route overload; mark existing composable `private` |

## Out of Scope

- Hoisting `Scaffold` to nav level (would allow lambda-only snackbar callbacks and remove
  `SnackbarHostState` from Route signatures, but requires restructuring all screen Scaffolds)
- Composable-scoped ViewModels via `rememberViewModelStoreOwner` (requires lifecycle 2.11.0-alpha02)
- `HabitDetail` screen implementation (still a TODO stub)
