# Navigation 3 Migration Design

**Date:** 2026-03-12
**Status:** Approved (revised to use official KMP library)

## Overview

Migrate `HabitLockNavHost` to use the official Jetpack Navigation 3 KMP library (`org.jetbrains.androidx.navigation3:navigation3-ui`). Remove the navigation drawer in favour of Calendar/Settings icon buttons in `TodayScreen`'s top bar. Works across Android, iOS, and JVM via Compose Multiplatform.

**Note:** Tasks 1 and 2 are already committed to `feature/navigation3-migration`:
- `Route.Onboarding` removed
- Drawer replaced with icon buttons; `AppNavigationDrawer` wrapper removed from `HabitLockNavHost`

## Architecture

The back stack is a `SnapshotStateList<NavKey>` provided by `rememberNavBackStack(navConfig, initialRoute)`. `NavDisplay` renders the current entry via `entryProvider`. Navigation forward calls `backStack.add(route)`, back calls `backStack.removeLastOrNull()`, and onboarding completion calls `backStack.clear()` + `backStack.add(Route.Today)`.

```kotlin
val backStack = rememberNavBackStack(
    navConfig,
    if (isOnboardingCompleted) Route.Today else Route.OnboardingPhilosophy
)

NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider = entryProvider {
        entry<Route.Today> { ... }
        entry<Route.Calendar> { ... }
        // etc.
    }
)
```

`rememberNavBackStack` saves and restores the back stack across process death (Android) via `SavedStateConfiguration` + polymorphic serialization.

**VM event collectors:** Top-level `LaunchedEffect(Unit)` collectors for all ViewModels remain outside `NavDisplay`, calling `backStack.add()`/`removeLastOrNull()`/`clear()` as before.

**`HabitFormViewModel` collectors:** Stay inside their respective `entry<Route.CreateHabit>` and `entry<Route.EditHabit>` lambdas, scoped to the entry's composition lifetime.

## Route Changes

`Route` sealed interface becomes `@Serializable` and implements `NavKey`. All subclasses get `@Serializable`. Required for the KMP polymorphic serialization that powers back stack state saving.

A `SavedStateConfiguration` with `subclassesOfSealed<Route>()` is defined as a top-level constant in `HabitLockNavHost.kt`:

```kotlin
private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<Route>()
        }
    }
}
```

### Back stack behaviour per destination

| Destination | Behaviour |
|---|---|
| `OnboardingPhilosophy` | Initial root when onboarding not completed |
| `OnboardingStrictness`, `OnboardingFirstHabit` | Pushed linearly (`backStack.add`) |
| Onboarding → Today | `backStack.clear()` + `backStack.add(Route.Today)` — cannot go back to onboarding |
| `Today` | Root when onboarding completed; never popped |
| `Calendar`, `Settings`, `CreateHabit`, `EditHabit`, `ArchivedHabits` | Pushed on top; popped by `backStack.removeLastOrNull()` |
| `HabitDetail` | Placeholder — immediately pops via `LaunchedEffect` |

## Navigation Structure

The drawer is removed (done in Task 2). Calendar and Settings are icon buttons in `TodayScreen`'s top bar. `onBack` in `NavDisplay` handles the system back button/gesture automatically.

## Dependencies

### New

| Artifact | Version | Scope |
|---|---|---|
| `org.jetbrains.androidx.navigation3:navigation3-ui` | `1.0.0-alpha05` | `commonMain` |
| `org.jetbrains.kotlin.plugin.serialization` plugin | same as Kotlin (`2.3.0`) | build |

`kotlinx-serialization-core` comes transitively from `navigation3-ui` — no explicit runtime dependency needed.

## Files Changed

| File | Change |
|---|---|
| `gradle/libs.versions.toml` | Add `navigation3` version, `navigation3-ui` library, `kotlinxSerialization` plugin |
| `composeApp/build.gradle.kts` | Apply `kotlinxSerialization` plugin; add `navigation3-ui` to `commonMain` |
| `navigation/Route.kt` | Add `@Serializable` to sealed interface + all subclasses; implement `NavKey` |
| `navigation/HabitLockNavHost.kt` | Add `navConfig`; replace `currentRoute` with `rememberNavBackStack`; replace `when` block with `NavDisplay + entryProvider`; remove `modifier` param |
| `ui/components/AppNavigationDrawer.kt` | **Delete** (already unused after Task 2) |

## What Does Not Change

- `HabitLockNavHost` composable parameters (all ViewModels from `App.kt`)
- `HabitFormViewModel` factory pattern
- `CalendarScreen`, `SettingsScreen`, `ArchivedHabitsScreen`, `HabitFormScreen` composables
- Top-level `LaunchedEffect` VM event collectors structure
- No changes to `App.kt`

## Migration Path (already complete)

This IS the Navigation 3 implementation. Future upgrades are version bumps only.
