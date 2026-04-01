# Swipe Actions on Habit Cards — Design Spec

## Overview

Add swipe gestures to all habit cards on the Today screen (both pending and resolved) with three actions across two directions: archive (right), edit (left), delete (far left). Each zone has a distinct color, icon, and haptic feedback intensity. Archive and delete show undo snackbars; edit navigates to the habit form.

## Swipe Zones

### Zone Model

Four anchors on a single `AnchoredDraggableState`:

| Anchor | Direction | Threshold | Background Token | Icon Tint Token | Icon | On Release |
|--------|-----------|-----------|-----------------|----------------|------|------------|
| Rest | — | 0% | — | — | — | — |
| Archive | Right | 30% card width | `surfaceContainerHighest` | `onSurface` | `Icons.Outlined.Inventory2` | Slide off right, snackbar with undo |
| Edit | Left | 30% card width | `secondaryContainer` | `onSecondaryContainer` | `Icons.Outlined.Edit` | Snap back to rest, navigate to edit screen |
| Delete | Far left | 60% card width | `errorContainer` | `onErrorContainer` | `Icons.Filled.DeleteForever` | Slide off left, snackbar with undo |

### Visual Behavior

- **Background layer**: Sits behind the card. Draws the zone color and icon based on the current drag offset. The icon is positioned near the revealed edge (leading for right-swipe, trailing for left-swipe).
- **Foreground layer**: The existing habit card content (`PendingHabitCard` or `ResolvedHabitRow`), horizontally offset by the drag amount.
- **Color transitions**: Background color changes as the card crosses from one zone threshold to the next. The transition is immediate at the threshold boundary (snap, not gradient).
- **Dismissal animation**: For archive and delete, the card slides off-screen in the swipe direction after releasing in the zone. For edit, the card snaps back to rest position.

### Gesture Details

- Built on Compose Foundation's `AnchoredDraggableState` with custom anchors.
- Velocity-based fling: a fast swipe past a minimum velocity threshold settles to the nearest zone in the fling direction, even if the positional threshold hasn't been crossed.
- Partial drags that don't reach a threshold snap back to rest.
- The swipe gesture coexists with the existing tap-to-expand interaction on pending cards.

## Haptic Feedback

### KMP Abstraction

`expect class HapticController` in `commonMain` with platform `actual` implementations:

```
expect class HapticController {
    fun tick()       // lightest — archive zone
    fun click()      // medium — edit zone
    fun heavyClick() // heaviest — delete zone
}
```

Haptic fires once when the drag crosses a zone threshold (entering the zone), not continuously during drag.

### Android — Layered Backward Compatibility

| Zone | API 30+ (Composition Primitives) | API 29+ (Predefined Effects) | API 26+ (OneShot Fallback) |
|------|----------------------------------|------------------------------|---------------------------|
| Archive | `PRIMITIVE_TICK` scale 0.4 | `EFFECT_TICK` | `createOneShot(20ms, 80)` |
| Edit | `PRIMITIVE_CLICK` scale 0.6 | `EFFECT_CLICK` | `createOneShot(30ms, 150)` |
| Delete | `PRIMITIVE_THUD` scale 1.0 | `EFFECT_HEAVY_CLICK` | `createOneShot(50ms, 255)` |

Check `vibrator.arePrimitivesSupported()` before using composition primitives on API 30+. Fall through tiers gracefully.

Do not use Compose's `LocalHapticFeedback` — it only exposes `LongPress` and `TextHandleMove`, which is too limited for distinct zone feedback.

### iOS

| Zone | UIKit API |
|------|-----------|
| Archive | `UIImpactFeedbackGenerator(style: .light)` |
| Edit | `UIImpactFeedbackGenerator(style: .medium)` |
| Delete | `UIImpactFeedbackGenerator(style: .heavy)` |

No backward compatibility concerns (iOS 10+).

## Undo Flow

### Delete

1. User releases card in delete zone.
2. Card slides off-screen (animation).
3. Habit is removed from UI state immediately (optimistic).
4. Snackbar appears: "Habit deleted" with UNDO action, 5-second duration.
5. If snackbar dismisses without undo → execute `habitRepository.deleteHabit(habitId)`. This is a hard delete with cascade (removes all instances, schedules, reminders, completion events, snooze states, leave periods).
6. If user taps UNDO → habit reappears in UI state, no database operation needed (delete was deferred).

### Archive

1. User releases card in archive zone.
2. Card slides off-screen (animation).
3. Habit is removed from UI state immediately (optimistic).
4. Snackbar appears: "Habit archived" with UNDO action, 5-second duration.
5. If snackbar dismisses without undo → execute `habitRepository.archiveHabit(habitId)`.
6. If user taps UNDO → habit reappears in UI state, no database operation needed (archive was deferred).

### Edit

No undo. Card snaps back to rest, then navigates to the edit habit screen with the habit ID.

## Architecture

### New Files

- `SwipeableHabitCard.kt` — composable wrapping habit cards with swipe gesture layer
- `HapticController.kt` (commonMain) — `expect` interface
- `HapticController.android.kt` — `actual` Android implementation with layered API strategy
- `HapticController.ios.kt` — `actual` iOS implementation with UIKit feedback generators

### Modified Files

- **`TodayScreen.kt`** — wrap `HabitCard` calls in `SwipeableHabitCard`, pass archive/edit/delete callbacks
- **`TodayViewModel.kt`** — add `deleteHabit()`, `undoDelete()`, `undoArchive()` methods; modify `archiveHabit()` to support deferred execution with undo
- **`TodayState.kt`** — add undo state fields for tracking deferred delete/archive operations

### Data Layer

No changes needed. `HabitRepository.deleteHabit()` and `archiveHabit()` / `unarchiveHabit()` already exist. Database cascade on delete already covers all related tables.

## Testing

### Integration Tests (ViewModel)

Test the delete/undo and archive/undo flows using real repository implementations backed by fake in-memory data sources. Only the data source layer is faked — repositories and use cases are real.

- Delete flow: verify habit removed from state, verify DB delete fires after snackbar timeout, verify undo cancels delete
- Archive flow: same pattern with archive/unarchive
- Edge cases: rapid delete-then-undo, deleting last habit in a section, deleting while snackbar from previous delete is still showing

### Screenshot Tests

- Each swipe zone state (archive, edit, delete) in both light and dark themes
- Rest state (existing, just verify no regression)

### UI Tests

- Swipe right past threshold → archive action fires
- Swipe left past first threshold → edit action fires
- Swipe left past second threshold → delete action fires
- Partial swipe below threshold → card snaps back, no action
- Tap still expands/collapses pending cards (no gesture conflict)

## Out of Scope

- Swipe actions on the Archived Habits screen (already has explicit delete/restore buttons)
- Custom swipe threshold configuration by the user
- Animated color gradient transitions between zones (snap transition only)
