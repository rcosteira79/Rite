# 🔒 HabitLock

**HabitLock** is a habit tracking app that enforces commitments through user-defined strictness levels. Unlike typical habit trackers focused on motivation and reminders, HabitLock helps users keep promises to themselves by providing accountability mechanisms with configurable enforcement rules.

## About the Project

HabitLock is built on the principle that meaningful behavior change requires more than gentle nudges—it requires commitment and accountability. Users consciously choose their level of strictness, from flexible to fully locked, and the app enforces those rules consistently.

### Core Features

- **Customizable Strictness Levels**: Choose between Flexible, Balanced, or Locked enforcement modes
- **Daily Habit Tracking**: Binary (yes/no) and quantitative habit types
- **Smart Notifications**: Actionable notifications with snooze/skip/complete options
- **Streak Tracking**: Per-habit streaks and "perfect days" when all habits are completed
- **Undo Policies**: Configurable undo permissions (none, today only, or full history)
- **Skip Management**: Limited skips to prevent streak loss with progressive consequences
- **Timezone Awareness**: Intelligent handling of timezone changes with user notifications
- **Calendar History**: Visual historical accuracy of habit completion

### What Makes HabitLock Different

- **Enforcement-based design**: The app actively prevents you from breaking your own rules
- **Conscious control**: You choose the strictness level that works for you
- **No gamification clutter**: Focuses on actual habit completion rather than XP/achievements (MVP)
- **Privacy-first**: Local-only storage, no backend sync (MVP)

### MVP Scope

This initial version includes:
- Core habit creation and tracking
- Daily habit instances with status management
- Notification system with inline actions
- Streak calculation and display
- Basic undo/skip/snooze mechanics
- Onboarding flow with strictness selection

Not included in MVP:
- Backend/cloud sync
- Social features
- XP, leveling, or achievement systems
- Advanced notification escalation

## Technical Stack

This is a **Kotlin Multiplatform** project targeting Android, iOS, and Desktop (JVM).

### Project Structure

* [/composeApp](./composeApp/src) contains code shared across all Compose Multiplatform applications:
  - [commonMain](./composeApp/src/commonMain/kotlin) – Code common for all targets
  - Platform-specific folders ([iosMain](./composeApp/src/iosMain/kotlin), [jvmMain](./composeApp/src/jvmMain/kotlin)) – Platform-specific implementations

* [/iosApp](./iosApp/iosApp) contains the iOS application entry point and SwiftUI code

### Architecture

- **Clean Architecture** with vertical feature packages (domain/data/presentation)
- **MVVM/MVI** pattern with single state classes for UI state management
- **Repository Pattern** for data persistence
- **Dependency Injection** using kotlin-inject (compile-time, KSP-generated)
- **Material 3** design system
- **Jetpack Compose** for UI (including Compose Multiplatform)

### Key Technologies

- Kotlin Multiplatform
- Jetpack Compose / Compose Multiplatform
- SQLDelight 2.x (local database — type-safe, KMP-native)
- Kotlin Coroutines & StateFlow
- kotlin-inject (compile-time dependency injection)
- Manual navigation (`Route` sealed interface — Navigation Component 3 deferred)
- Material 3

## Getting Started

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE's toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

## Documentation

- [SPEC.md](./SPEC.md) – Complete business logic and domain specification
- [onboarding_spec.md](./onboarding_spec.md) – Onboarding flow specification and copy

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
