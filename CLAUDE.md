# HabitLock — Project Notes for Claude

## Known Pain Points

### `HabitFormScreen` → `TodayViewModel` coupling in nav host

When navigating back from `HabitFormScreen` (create/edit habit), `HabitLockNavigation` calls
`todayViewModel.loadTodayHabits()` alongside `backStack.removeLastOrNull()`. This couples the
nav host to `TodayViewModel`'s internals.

The clean fix would be a shared event bus or a `onHabitChanged` callback that `TodayViewModel`
subscribes to — so `HabitFormViewModel` just emits "habit saved" and `TodayViewModel` reacts
independently, without `HabitLockNavigation` knowing about either.

Deferred until the pattern is otherwise stable.
