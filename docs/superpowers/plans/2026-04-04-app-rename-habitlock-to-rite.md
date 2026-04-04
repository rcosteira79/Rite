# App Rename: HabitLock → Rite — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rename the app from "HabitLock" to "Rite" across the entire codebase — packages, classes, configs, resources, copy, and documentation.

**Architecture:** This is a mechanical rename with no behavioral changes. The work is layered so each task produces a compilable state: Gradle config + directory structure first, then class renames, then resources, then copy/documentation. The onboarding tone is refreshed and the "Locked" strictness tier becomes "Unwavering."

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Gradle (Kotlin DSL), SQLDelight, Android resources, iOS Xcode project, GitHub Actions CI.

**Spec:** `docs/superpowers/specs/2026-04-04-app-rename-habitlock-to-rite.md`

---

## File Structure

**Modified files (by task):**

| Task | Files |
|------|-------|
| 1. Package structure & Gradle config | `settings.gradle.kts`, `composeApp/build.gradle.kts`, all 8 source set directories (move `habitlock/` → `rite/`), all ~240 `.kt` files (package declarations + imports), SQLDelight `.sq` and `.sqm` files |
| 2. Class renames | `HabitLockAppComponent.kt` → `RiteAppComponent.kt`, `HabitLockApplication.kt` → `RiteApplication.kt`, `HabitLockNavigation.kt` → `RiteNavigation.kt`, `HabitLockBottomNav.kt` → `RiteBottomNav.kt`, `LocalAppComponent.kt`, `AppComponentFactory.kt`, `MainViewController.kt`, `main.kt`, all files referencing these classes |
| 3. Android resources & manifest | `AndroidManifest.xml`, `res/values/strings.xml`, `res/values/themes.xml`, `res/values-night/themes.xml`, `res/drawable/habit_lock_splash.xml` → `rite_splash.xml` |
| 4. Onboarding copy & strictness rename | `strings_onboarding_philosophy.xml`, `strings_onboarding_first_habit.xml`, `strings_onboarding_strictness.xml`, `StrictnessPreset.kt`, `OnboardingStrictnessPreset.kt`, `StrictnessPresetTest.kt`, `OnboardingStrictnessPresetTest.kt` |
| 5. iOS & JVM config | `iosApp/Configuration/Config.xcconfig`, `iosApp/iosApp.xcodeproj/project.pbxproj`, `composeApp/src/jvmMain/.../main.kt` (window title) |
| 6. CI/CD | `.github/workflows/ci.yml` |
| 7. Documentation | `README.md`, `CLAUDE.md` |
| 8. Screenshot re-recording | Golden images in `composeApp/src/androidUnitTest/snapshots/images/` |

---

### Task 1: Package Structure & Gradle Config

This is the largest task — moving all source directories and updating every package declaration and import. It must be atomic (the project won't compile in an intermediate state).

**Files:**
- Modify: `settings.gradle.kts:1`
- Modify: `composeApp/build.gradle.kts:85,89,143,151-153`
- Move: 8 directory trees from `com/ricardocosteira/habitlock` → `com/ricardocosteira/rite`
- Move: SQLDelight directory `composeApp/src/commonMain/sqldelight/com/ricardocosteira/habitlock/` → `com/ricardocosteira/rite/`
- Rename: `HabitLock.sq` → `Rite.sq`
- Modify: All ~240 `.kt` files (package declarations and imports)

- [ ] **Step 1: Update `settings.gradle.kts`**

Change line 1:

```kotlin
rootProject.name = "Rite"
```

- [ ] **Step 2: Update `composeApp/build.gradle.kts`**

Change the android namespace and applicationId:

```kotlin
android {
    namespace = "com.ricardocosteira.rite"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.ricardocosteira.rite"
```

Change the desktop packageName:

```kotlin
compose.desktop {
    application {
        mainClass = "com.ricardocosteira.rite.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.ricardocosteira.rite"
            packageVersion = "1.0.0"
        }
    }
}
```

Change the SQLDelight config:

```kotlin
sqldelight {
    databases {
        create("RiteDatabase") {
            packageName.set("com.ricardocosteira.rite.data.database")
        }
    }
}
```

- [ ] **Step 3: Move all source directories**

Run each of these commands to move the package directories. For each source set, create the new parent directory and move the contents:

```bash
# Kotlin source sets
for srcset in androidMain androidUnitTest commonMain commonTest iosMain jvmMain jvmTest; do
    src="composeApp/src/$srcset/kotlin/com/ricardocosteira/habitlock"
    dst="composeApp/src/$srcset/kotlin/com/ricardocosteira/rite"
    if [ -d "$src" ]; then
        mkdir -p "$(dirname "$dst")"
        mv "$src" "$dst"
    fi
done

# SQLDelight source
src="composeApp/src/commonMain/sqldelight/com/ricardocosteira/habitlock"
dst="composeApp/src/commonMain/sqldelight/com/ricardocosteira/rite"
mkdir -p "$(dirname "$dst")"
mv "$src" "$dst"
```

- [ ] **Step 4: Rename the SQLDelight file**

```bash
mv composeApp/src/commonMain/sqldelight/com/ricardocosteira/rite/data/database/HabitLock.sq \
   composeApp/src/commonMain/sqldelight/com/ricardocosteira/rite/data/database/Rite.sq
```

- [ ] **Step 5: Update all package declarations and imports**

Replace all occurrences of the old package with the new one across every `.kt` file:

```bash
find composeApp/src -name "*.kt" -exec sed -i '' 's/com\.ricardocosteira\.habitlock/com.ricardocosteira.rite/g' {} +
```

- [ ] **Step 6: Update the KSP source dir path in `build.gradle.kts`**

The generated KSP metadata path doesn't contain the package name — it stays as-is. No change needed. Verify the line is:

```kotlin
kotlin.sourceSets.commonMain {
    kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
}
```

- [ ] **Step 7: Verify compilation**

```bash
./gradlew :composeApp:compileKotlinMetadata 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL (or errors that will be fixed in subsequent tasks — class renames like `HabitLockDatabase` will fail until Task 2).

Note: Full compilation may fail at this point because class names like `HabitLockDatabase` still reference the old name in generated code. That's expected — Task 2 fixes it.

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "refactor: rename package com.ricardocosteira.habitlock to com.ricardocosteira.rite

Move all source directories, update Gradle config (namespace, applicationId,
SQLDelight), and update all package declarations and imports."
```

---

### Task 2: Class Renames

Rename all classes and references that contain "HabitLock" in their name.

**Files:**
- Rename: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/HabitLockAppComponent.kt` → `RiteAppComponent.kt`
- Rename: `composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/HabitLockApplication.kt` → `RiteApplication.kt`
- Rename: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/navigation/HabitLockNavigation.kt` → `RiteNavigation.kt`
- Rename: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/navigation/HabitLockBottomNav.kt` → `RiteBottomNav.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/LocalAppComponent.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/AppComponentFactory.kt`
- Modify: `composeApp/src/iosMain/kotlin/com/ricardocosteira/rite/MainViewController.kt`
- Modify: `composeApp/src/jvmMain/kotlin/com/ricardocosteira/rite/main.kt`
- Modify: All files that reference these class names

- [ ] **Step 1: Rename the files**

```bash
cd composeApp/src

# HabitLockAppComponent → RiteAppComponent
mv commonMain/kotlin/com/ricardocosteira/rite/di/HabitLockAppComponent.kt \
   commonMain/kotlin/com/ricardocosteira/rite/di/RiteAppComponent.kt

# HabitLockApplication → RiteApplication
mv androidMain/kotlin/com/ricardocosteira/rite/HabitLockApplication.kt \
   androidMain/kotlin/com/ricardocosteira/rite/RiteApplication.kt

# HabitLockNavigation → RiteNavigation
mv commonMain/kotlin/com/ricardocosteira/rite/presentation/navigation/HabitLockNavigation.kt \
   commonMain/kotlin/com/ricardocosteira/rite/presentation/navigation/RiteNavigation.kt

# HabitLockBottomNav → RiteBottomNav
mv commonMain/kotlin/com/ricardocosteira/rite/presentation/navigation/HabitLockBottomNav.kt \
   commonMain/kotlin/com/ricardocosteira/rite/presentation/navigation/RiteBottomNav.kt

cd ../..
```

- [ ] **Step 2: Replace all class name references across the codebase**

```bash
# HabitLockDatabase → RiteDatabase
find composeApp/src -name "*.kt" -exec sed -i '' 's/HabitLockDatabase/RiteDatabase/g' {} +

# HabitLockAppComponent → RiteAppComponent (also catches InjectHabitLockAppComponent)
find composeApp/src -name "*.kt" -exec sed -i '' 's/HabitLockAppComponent/RiteAppComponent/g' {} +

# HabitLockApplication → RiteApplication
find composeApp/src -name "*.kt" -exec sed -i '' 's/HabitLockApplication/RiteApplication/g' {} +

# habitLockApplication (extension property) → riteApplication
find composeApp/src -name "*.kt" -exec sed -i '' 's/habitLockApplication/riteApplication/g' {} +

# HabitLockNavigation → RiteNavigation
find composeApp/src -name "*.kt" -exec sed -i '' 's/HabitLockNavigation/RiteNavigation/g' {} +

# HabitLockBottomNav → RiteBottomNav
find composeApp/src -name "*.kt" -exec sed -i '' 's/HabitLockBottomNav/RiteBottomNav/g' {} +
```

- [ ] **Step 3: Update `RiteAppComponent.kt` KDoc**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/RiteAppComponent.kt`, update the class-level KDoc if it references "HabitLock":

```kotlin
/**
 * ...existing doc with HabitLock references updated to Rite...
 */
@Component
@Singleton
abstract class RiteAppComponent(
```

- [ ] **Step 4: Update `RiteApplication.kt` KDoc**

In `composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/RiteApplication.kt`, update:

```kotlin
/**
 * Application class that holds the singleton [RiteAppComponent].
 *
 * Workers and BroadcastReceivers retrieve the shared component via the
 * [Context.riteApplication] extension, eliminating the per-invocation
 * AppModule pattern and ensuring a single SQLite connection per process.
 */
class RiteApplication : Application() {

    lateinit var appComponent: RiteAppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        val driverFactory = DatabaseDriverFactory(this)
        val habitNotification = HabitNotification(this)
        appComponent = createAppComponent(driverFactory, habitNotification)
        NotificationChannels.createChannels(this)
        WorkManagerInitializer.initialize(this)
    }
}

val Context.riteApplication: RiteApplication
    get() = applicationContext as RiteApplication
```

- [ ] **Step 5: Update `AppComponentFactory.kt` KDoc**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/AppComponentFactory.kt`, update references:

```kotlin
/**
 * Platform-specific factory for [RiteAppComponent].
 * Each platform instantiates the kotlin-inject generated [InjectRiteAppComponent].
 */
expect fun createAppComponent(
    driverFactory: DatabaseDriverFactory,
    habitNotification: HabitNotification
): RiteAppComponent
```

- [ ] **Step 6: Update `LocalAppComponent.kt`**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/LocalAppComponent.kt`:

```kotlin
val LocalAppComponent = staticCompositionLocalOf<RiteAppComponent> {
    error("No RiteAppComponent provided")
}
```

- [ ] **Step 7: Verify compilation across all targets**

```bash
./gradlew :composeApp:compileKotlinMetadata :composeApp:compileDebugKotlin :composeApp:compileKotlinJvm 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Run tests**

```bash
./gradlew :composeApp:testDebugUnitTest :composeApp:jvmTest 2>&1 | tail -20
```

Expected: Tests pass (screenshot tests may fail due to package name in test class paths — that's expected and will be addressed in Task 8).

- [ ] **Step 9: Commit**

```bash
git add -A
git commit -m "refactor: rename HabitLock classes to Rite

HabitLockDatabase → RiteDatabase, HabitLockAppComponent → RiteAppComponent,
HabitLockApplication → RiteApplication, HabitLockNavigation → RiteNavigation,
HabitLockBottomNav → RiteBottomNav, habitLockApplication → riteApplication."
```

---

### Task 3: Android Resources & Manifest

Update Android-specific resources: app name, themes, manifest, and splash drawable.

**Files:**
- Modify: `composeApp/src/androidMain/AndroidManifest.xml`
- Modify: `composeApp/src/androidMain/res/values/strings.xml`
- Modify: `composeApp/src/androidMain/res/values/themes.xml`
- Modify: `composeApp/src/androidMain/res/values-night/themes.xml`
- Rename: `composeApp/src/androidMain/res/drawable/habit_lock_splash.xml` → `rite_splash.xml`

- [ ] **Step 1: Update `AndroidManifest.xml`**

Change the application class name and theme references:

```xml
    <application
        android:name=".RiteApplication"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Rite">

        <!-- Required for createComposeRule() in Robolectric tests -->
        <activity
            android:name="androidx.activity.ComponentActivity"
            android:exported="false" />

        <!-- Main Activity -->
        <activity
            android:exported="true"
            android:name=".MainActivity"
            android:theme="@style/Theme.Rite.Launch">
```

- [ ] **Step 2: Update `res/values/strings.xml`**

```xml
<resources>
    <string name="app_name">Rite</string>

    <!-- Tracking notification -->
    <string name="tracking_child_not_completed">Not completed</string>
    <string name="tracking_child_progress">%1$d / %2$d%3$s</string>
    <string name="tracking_summary_title">Rite</string>
    <plurals name="tracking_summary_text">
        <item quantity="one">%d habit remaining</item>
        <item quantity="other">%d habits remaining</item>
    </plurals>
    <string name="tracking_action_complete">Complete</string>
    <string name="tracking_action_increment">+%d</string>
    <string name="tracking_action_undo">Undo</string>
</resources>
```

- [ ] **Step 3: Rename splash drawable**

```bash
mv composeApp/src/androidMain/res/drawable/habit_lock_splash.xml \
   composeApp/src/androidMain/res/drawable/rite_splash.xml
```

- [ ] **Step 4: Update `res/values/themes.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <style name="Theme.Rite" parent="android:Theme.Material.Light.NoActionBar" />

    <style name="Theme.Rite.Launch" parent="Theme.SplashScreen">
        <item name="windowSplashScreenBackground">@color/splash_background</item>
        <item name="windowSplashScreenAnimatedIcon">@drawable/rite_splash</item>
        <item name="windowSplashScreenIconBackgroundColor">@color/splash_icon_background</item>
        <item name="postSplashScreenTheme">@style/Theme.Rite</item>
    </style>

</resources>
```

- [ ] **Step 5: Update `res/values-night/themes.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <style name="Theme.Rite" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:windowLightStatusBar">false</item>
    </style>

    <style name="Theme.Rite.Launch" parent="Theme.SplashScreen">
        <item name="windowSplashScreenBackground">@color/splash_background</item>
        <item name="windowSplashScreenAnimatedIcon">@drawable/rite_splash</item>
        <item name="windowSplashScreenIconBackgroundColor">@color/splash_icon_background</item>
        <item name="postSplashScreenTheme">@style/Theme.Rite</item>
    </style>

</resources>
```

- [ ] **Step 6: Verify Android compilation**

```bash
./gradlew :composeApp:compileDebugKotlin 2>&1 | tail -10
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "refactor: update Android resources for Rite rename

App name, theme styles, manifest class reference, and splash drawable
all updated from HabitLock to Rite."
```

---

### Task 4: Onboarding Copy Refresh & Strictness Rename

Update onboarding text to match the new "Rite" tone, and rename the "Locked" strictness tier to "Unwavering."

**Files:**
- Modify: `composeApp/src/commonMain/composeResources/values/strings_onboarding_philosophy.xml`
- Modify: `composeApp/src/commonMain/composeResources/values/strings_onboarding_first_habit.xml`
- Modify: `composeApp/src/commonMain/composeResources/values/strings_onboarding_strictness.xml`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/domain/models/StrictnessPreset.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingStrictnessPreset.kt`
- Modify: All files referencing `StrictnessPreset.LOCKED` or `OnboardingStrictnessPreset.LOCKED`

- [ ] **Step 1: Update `strings_onboarding_philosophy.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="philosophy_heading">Show up\nfor yourself.</string>
    <string name="philosophy_body">Turn your goals into daily rituals — even on the hard days.\n\nYou set the standard. Rite helps you hold it.</string>
    <string name="philosophy_cta_accept">I\'m Ready</string>
</resources>
```

- [ ] **Step 2: Update `strings_onboarding_first_habit.xml`**

Change only the heading:

```xml
    <string name="first_habit_heading">Start your first rite</string>
```

All other strings in this file remain unchanged.

- [ ] **Step 3: Update `strings_onboarding_strictness.xml`**

Change the locked tier strings:

```xml
    <string name="strictness_locked_label">Unwavering</string>
    <string name="strictness_locked_description">No excuses. Show up every time.</string>
```

Note: The XML key names (`strictness_locked_*`) stay the same to avoid cascading changes in composables that reference these keys. Only the display values change.

All other strings in this file remain unchanged.

- [ ] **Step 4: Rename `LOCKED` → `UNWAVERING` in `StrictnessPreset.kt`**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/domain/models/StrictnessPreset.kt`:

```kotlin
enum class StrictnessPreset {
    /**
     * Gentle support with maximum forgiveness.
     * - Unlimited undo for all history
     * - Unlimited snoozes
     * - Unlimited skips
     * - Longer snooze duration (60 minutes)
     */
    FLEXIBLE,

    /**
     * Structure with room for real life (default/recommended).
     * - Undo allowed for today only
     * - Limited snoozes (3 per habit per day)
     * - Limited consecutive skips (2)
     * - Moderate snooze duration (30 minutes)
     */
    BALANCED,

    /**
     * No excuses. Show up every time.
     * - No undo allowed
     * - Very limited snoozes (1 per habit per day)
     * - No skips allowed (0)
     * - Short snooze duration (15 minutes)
     */
    UNWAVERING;

    /**
     * Converts this preset to user settings.
     */
    fun toUserSettings(): UserStrictnessSettings = when (this) {
        FLEXIBLE -> {
            UserStrictnessSettings(
                undoPolicy = UndoPolicy.ALL_HISTORY,
                maxSnoozesPerHabitPerDay = null,
                maxConsecutiveSkips = null,
                maxSnoozeDurationMinutes = 60
            )
        }

        BALANCED -> {
            UserStrictnessSettings(
                undoPolicy = UndoPolicy.TODAY_ONLY,
                maxSnoozesPerHabitPerDay = 3,
                maxConsecutiveSkips = 2,
                maxSnoozeDurationMinutes = 30
            )
        }

        UNWAVERING -> {
            UserStrictnessSettings(
                undoPolicy = UndoPolicy.NONE,
                maxSnoozesPerHabitPerDay = 1,
                maxConsecutiveSkips = 0,
                maxSnoozeDurationMinutes = 15
            )
        }
    }

    companion object {
        /**
         * Default preset recommended for most users.
         */
        val DEFAULT = BALANCED

        /**
         * Reverse-maps user settings back to a preset, or null if the settings
         * don't match any known preset (i.e. the user customised them).
         */
        fun fromSettings(settings: UserStrictnessSettings): StrictnessPreset? =
            entries.firstOrNull {
                it.toUserSettings() == settings
            }
    }
}
```

- [ ] **Step 5: Rename `LOCKED` → `UNWAVERING` in `OnboardingStrictnessPreset.kt`**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/onboarding/OnboardingStrictnessPreset.kt`:

```kotlin
enum class OnboardingStrictnessPreset(
    val label: String,
    val description: String,
    val collapsedSummary: String,
    val rules: List<PresetRule>,
    val isRecommended: Boolean = false
) {
    FLEXIBLE(
        label = "Flexible",
        description = "Gentle support, maximum forgiveness.",
        collapsedSummary = "Undo: Unlimited · Snoozes: Unlimited",
        rules = listOf(
            PresetRule("Undo", "Unlimited"),
            PresetRule("Snoozes", "Unlimited"),
            PresetRule("Skips", "Unlimited")
        )
    ),
    BALANCED(
        label = "Balanced",
        description = "The middle path. Enough grace to fail, enough structure to win.",
        collapsedSummary = "Undo: Within 5 min · Snoozes: 1/day",
        rules = listOf(
            PresetRule("Undo", "Within 5 min"),
            PresetRule("Snoozes", "1 / day"),
            PresetRule("Skips", "2 / month")
        ),
        isRecommended = true
    ),
    UNWAVERING(
        label = "Unwavering",
        description = "No excuses. Show up every time.",
        collapsedSummary = "No undo · Skips capped",
        rules = listOf(
            PresetRule("Undo", "None"),
            PresetRule("Snoozes", "Capped"),
            PresetRule("Skips", "Capped")
        )
    )
}
```

- [ ] **Step 6: Update all references to `LOCKED` across Kotlin files**

```bash
# Replace StrictnessPreset.LOCKED with StrictnessPreset.UNWAVERING
find composeApp/src -name "*.kt" -exec sed -i '' 's/StrictnessPreset\.LOCKED/StrictnessPreset.UNWAVERING/g' {} +

# Replace OnboardingStrictnessPreset.LOCKED with OnboardingStrictnessPreset.UNWAVERING
find composeApp/src -name "*.kt" -exec sed -i '' 's/OnboardingStrictnessPreset\.LOCKED/OnboardingStrictnessPreset.UNWAVERING/g' {} +
```

- [ ] **Step 7: Verify compilation and run tests**

```bash
./gradlew :composeApp:compileDebugKotlin :composeApp:testDebugUnitTest :composeApp:jvmTest 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL, tests pass (except screenshot goldens which will be re-recorded in Task 8).

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "refactor: refresh onboarding copy and rename Locked to Unwavering

Philosophy screen: 'Show up for yourself' tone. First habit: 'Start your
first rite'. Strictness tier Locked becomes Unwavering with updated copy."
```

---

### Task 5: iOS & JVM Platform Config

Update platform-specific configuration files for iOS and JVM desktop.

**Files:**
- Modify: `iosApp/Configuration/Config.xcconfig`
- Modify: `iosApp/iosApp.xcodeproj/project.pbxproj`
- Modify: `composeApp/src/jvmMain/kotlin/com/ricardocosteira/rite/main.kt` (window title)

- [ ] **Step 1: Update `Config.xcconfig`**

```
TEAM_ID=

PRODUCT_NAME=Rite
PRODUCT_BUNDLE_IDENTIFIER=com.ricardocosteira.rite.Rite$(TEAM_ID)

CURRENT_PROJECT_VERSION=1
MARKETING_VERSION=1.0
```

- [ ] **Step 2: Update `project.pbxproj`**

Replace all occurrences of `HabitLock.app` with `Rite.app`:

```bash
sed -i '' 's/HabitLock\.app/Rite.app/g' iosApp/iosApp.xcodeproj/project.pbxproj
```

- [ ] **Step 3: Update JVM window title**

In `composeApp/src/jvmMain/kotlin/com/ricardocosteira/rite/main.kt`, change the window title:

```kotlin
    Window(
        onCloseRequest = ::exitApplication,
        title = "Rite",
    ) {
        App(appComponent = appComponent)
    }
```

- [ ] **Step 4: Verify JVM compilation**

```bash
./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -10
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: update iOS and JVM config for Rite rename

iOS product name, bundle identifier, and Xcode project references.
JVM desktop window title."
```

---

### Task 6: CI/CD

Update GitHub Actions workflow environment variable names and artifact names.

**Files:**
- Modify: `.github/workflows/ci.yml`

- [ ] **Step 1: Update `.github/workflows/ci.yml`**

Replace all `HABITLOCK_*` environment variable references with `RITE_*`, and update artifact names and APK filenames.

In the `android-artifact` job, update the `release` build step env block:

```yaml
      - name: Build Release APK
        if: github.ref == 'refs/heads/master'
        env:
          RITE_STORE_FILE: ${{ runner.temp }}/rite-release.keystore
          RITE_KEY_ALIAS: ${{ secrets.RITE_KEY_ALIAS }}
          RITE_STORE_PASSWORD: ${{ secrets.RITE_STORE_PASSWORD }}
          RITE_KEY_PASSWORD: ${{ secrets.RITE_KEY_PASSWORD }}
        run: |
          echo "${{ secrets.RITE_KEYSTORE_BASE64 }}" | base64 --decode > "$RITE_STORE_FILE"
          ./gradlew :composeApp:assembleRelease --no-configuration-cache
```

Update artifact upload names:

```yaml
      - name: Upload RC artifact
        if: github.ref == 'refs/heads/develop'
        uses: actions/upload-artifact@v4
        with:
          name: rite-rc-${{ steps.slug.outputs.sha }}
          path: composeApp/build/outputs/apk/rc/**/*.apk
          if-no-files-found: error
      - name: Upload Release artifact
        if: github.ref == 'refs/heads/master'
        uses: actions/upload-artifact@v4
        with:
          name: rite-release-${{ steps.slug.outputs.sha }}
          path: composeApp/build/outputs/apk/release/**/*.apk
          if-no-files-found: error
```

In the `release` job, update all references:

```yaml
      - name: Decode keystore
        run: echo "${{ secrets.RITE_KEYSTORE_BASE64 }}" | base64 --decode > "${{ runner.temp }}/rite-release.keystore"
      - name: Build signed APK
        env:
          RITE_STORE_FILE: ${{ runner.temp }}/rite-release.keystore
          RITE_KEY_ALIAS: ${{ secrets.RITE_KEY_ALIAS }}
          RITE_STORE_PASSWORD: ${{ secrets.RITE_STORE_PASSWORD }}
          RITE_KEY_PASSWORD: ${{ secrets.RITE_KEY_PASSWORD }}
        run: ./gradlew :composeApp:assembleRelease --no-configuration-cache
```

Update the APK rename and upload:

```yaml
      - name: Attach APK to release
        env:
          GH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          mv composeApp/build/outputs/apk/release/composeApp-release.apk "rite-${{ steps.version.outputs.tag }}.apk"
          gh release upload "${{ steps.version.outputs.tag }}" "rite-${{ steps.version.outputs.tag }}.apk"
```

- [ ] **Step 2: Update `composeApp/build.gradle.kts` signing env vars**

In the signing config section, update environment variable names:

```kotlin
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("RITE_STORE_FILE") ?: "/dev/null")
            keyAlias = System.getenv("RITE_KEY_ALIAS") ?: ""
            storePassword = System.getenv("RITE_STORE_PASSWORD") ?: ""
            keyPassword = System.getenv("RITE_KEY_PASSWORD") ?: ""
        }
    }
```

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "build: update CI/CD and signing config for Rite rename

Environment variables HABITLOCK_* → RITE_*, artifact names and APK
filenames updated. GitHub secrets will need manual updating."
```

---

### Task 7: Documentation

Update README and CLAUDE.md with the new name and references.

**Files:**
- Modify: `README.md`
- Modify: `CLAUDE.md`

- [ ] **Step 1: Update `README.md`**

Replace all occurrences of "HabitLock" with "Rite" throughout the file:

```bash
sed -i '' 's/HabitLock/Rite/g' README.md
```

Then manually verify and fix the title and any context-specific phrasing. The title should be:

```markdown
# Rite

An open-source habit tracker that turns your goals into daily rituals through user-defined strictness levels. Unlike typical habit trackers focused on motivation and gentle reminders, Rite helps you show up for yourself through configurable accountability rules.
```

Also update the architecture section paths from `habitlock` to `rite`:

```bash
sed -i '' 's/habitlock/rite/g' README.md
```

Update the copyright line to keep it unchanged:

```markdown
[MIT License](./LICENSE) — © 2026 Ricardo Costeira
```

- [ ] **Step 2: Update `CLAUDE.md`**

Replace the title and all class name references:

```markdown
# Rite — Project Notes for Claude

## Known Pain Points

### `HabitFormScreen` → `TodayViewModel` coupling in nav host

When navigating back from `HabitFormScreen` (create/edit habit), `RiteNavigation` calls
`todayViewModel.loadTodayHabits()` alongside `backStack.removeLastOrNull()`. This couples the
nav host to `TodayViewModel`'s internals.

The clean fix would be a shared event bus or a `onHabitChanged` callback that `TodayViewModel`
subscribes to — so `HabitFormViewModel` just emits "habit saved" and `TodayViewModel` reacts
independently, without `RiteNavigation` knowing about either.

Deferred until the pattern is otherwise stable.
```

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "docs: update README and CLAUDE.md for Rite rename"
```

---

### Task 8: Re-record Screenshot Goldens

The onboarding copy changes and "Unwavering" rename will cause screenshot test failures. Re-record all goldens.

**Files:**
- Regenerate: `composeApp/src/androidUnitTest/snapshots/images/*.png`

- [ ] **Step 1: Delete existing golden images**

The old goldens have the old package name in their filenames (`com.ricardocosteira.habitlock.*`). Delete them all so they get regenerated with the new package name:

```bash
rm -f composeApp/src/androidUnitTest/snapshots/images/*.png
```

- [ ] **Step 2: Record new goldens**

```bash
./gradlew :composeApp:recordRoborazziDebug 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL with new golden images generated.

- [ ] **Step 3: Verify the new images exist**

```bash
ls composeApp/src/androidUnitTest/snapshots/images/ | head -20
```

Expected: PNG files with `com.ricardocosteira.rite.*` in their names.

- [ ] **Step 4: Run screenshot verification to confirm they match**

```bash
./gradlew :composeApp:testDebugUnitTest 2>&1 | tail -20
```

Expected: All tests pass, including screenshot comparisons.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "test: re-record screenshot goldens after Rite rename

All golden images regenerated with new package paths and updated
onboarding copy (philosophy, first habit, Unwavering tier)."
```

---

## Post-Implementation Manual Steps

After all tasks are merged:

1. **Rename GitHub repo:** `rcosteira79/HabitLock` → `rcosteira79/Rite` (Settings → General → Repository name)
2. **Update GitHub secrets:** Delete `HABITLOCK_*` secrets and create `RITE_*` equivalents with the same values. Also rename `HABITLOCK_KEYSTORE_BASE64` → `RITE_KEYSTORE_BASE64`
3. **Update local git remotes:** `git remote set-url origin git@github.com:rcosteira79/Rite.git`
4. **Replace v1.0.0 release APK:** Build new signed APK and replace the asset on the GitHub release
5. **Update branch protection rulesets** if needed after repo rename
