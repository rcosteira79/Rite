# CI Pipeline Design

## Goal

Set up a GitHub Actions CI pipeline that runs tests on every push and PR, gates iOS and artifact builds on passing tests, and produces versioned Android artifacts from `develop` and `master`.

## Architecture

A single workflow file (`.github/workflows/ci.yml`) with three jobs gated by branch/trigger conditions. All artifact and platform-specific jobs depend on the `test` job passing first. The project gains a new `rc` Android build type and `roborazzi.test.compare=true` is added to `gradle.properties` to enforce screenshot comparison on every test run.

## Triggers

```
on:
  push:
    branches: [master, develop]
  pull_request:
    branches: [master, develop]
```

## Jobs

### `test`

- **Runner**: `ubuntu-latest`
- **When**: Always (all pushes and PRs)
- **Depends on**: —
- **Command**: `./gradlew :composeApp:testDebugUnitTest --no-build-cache`
- `--no-build-cache` prevents Gradle from restoring Roborazzi's intermediate snapshot cache, which would bypass screenshot comparison and produce false passes.

### `ios-build`

- **Runner**: `macos-latest`
- **When**: PRs only (`github.event_name == 'pull_request'`)
- **Depends on**: `test`
- **Steps**:
  1. Link Kotlin framework: `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`
  2. Build Xcode project (no signing):
     ```
     xcodebuild -project iosApp/iosApp.xcodeproj \
       -scheme iosApp \
       -destination 'generic/platform=iOS Simulator' \
       CODE_SIGNING_ALLOWED=NO \
       build
     ```
- No artifact produced; this is a build gate only.
- Note: `macos-latest` minutes count at 10× on GitHub's private repo quota.

### `android-artifact`

- **Runner**: `ubuntu-latest`
- **When**: Push to `develop` or `master` only
- **Depends on**: `test`
- **`develop`**: `./gradlew :composeApp:assembleRc` → artifact named `habitlock-rc-<short-sha>`
- **`master`**: `./gradlew :composeApp:assembleRelease` → artifact named `habitlock-release-<short-sha>`
- Artifacts uploaded via `actions/upload-artifact@v4`.
- Release signing is not configured yet; `assembleRelease` produces an unsigned APK until a keystore is added.

## Android Build Types

A new `rc` build type is added to `composeApp/build.gradle.kts`:

```kotlin
buildTypes {
    create("rc") {
        initWith(getByName("release"))
        signingConfig = signingConfigs.getByName("debug")
    }
    getByName("release") {
        isMinifyEnabled = false  // stays false until signing + Play Console are configured
    }
}
```

The `rc` type inherits all release settings (R8, no debug flags) but signs with the debug key so it can be installed without a keystore. This makes `develop` artifacts representative of release behaviour without requiring signing infrastructure.

## `gradle.properties` change

```
roborazzi.test.compare=true
```

This enables screenshot comparison mode during normal `testDebugUnitTest` runs. Without it, Roborazzi only records or skips comparison. With it, any screenshot drift from the committed baselines fails the build — both locally and on CI.

## Infrastructure

- **JDK**: All jobs use `actions/setup-java` with `java-version: '17'` (matching `jvmToolchain(17)`).
- **Gradle caching**: `gradle/actions/setup-gradle@v4` is used instead of manual `actions/cache`. It handles Gradle wrapper and dependency caching automatically.

## Branch Setup

A `develop` branch is created from `master` before the workflow is pushed. Future feature branches target `develop`; `master` receives merges from `develop` at release time.

## Future Work

- Add release signing via GitHub Actions secrets (keystore + passwords) when a Play Console account is set up.
- Enable `isMinifyEnabled = true` on `release` and `rc` build types once signing is in place and shrinking is validated.
