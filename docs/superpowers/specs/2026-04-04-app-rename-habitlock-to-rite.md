# App Rename: HabitLock to Rite

## Summary

Rename the app from "HabitLock" to "Rite" across the entire codebase. This includes package names, class names, Android/iOS configuration, CI/CD, documentation, and a tone refresh for onboarding copy. The word "Rite" reframes habits as intentional daily rituals the user shows up for, rather than locks/enforcement.

## Decisions

- **ApplicationId changes** from `com.ricardocosteira.habitlock` to `com.ricardocosteira.rite`. No backward compatibility needed (no store releases yet).
- **GitHub repo renames** from `rcosteira79/HabitLock` to `rcosteira79/Rite` (manual post-merge step).
- **Signing keystore stays as-is.** Only the CI environment variable names change from `HABITLOCK_*` to `RITE_*`.
- **Strictness tier "Locked" renames to "Unwavering."**
- **Onboarding copy gets a tone refresh** — shifting from enforcement language to intentional self-improvement.
- **v1.0.0 release APK** on GitHub gets replaced with the renamed build.

---

## 1. Identity Mapping

| Old | New |
|-----|-----|
| App name: HabitLock | Rite |
| Package: `com.ricardocosteira.habitlock` | `com.ricardocosteira.rite` |
| ApplicationId: `com.ricardocosteira.habitlock` | `com.ricardocosteira.rite` |
| GitHub repo: `rcosteira79/HabitLock` | `rcosteira79/Rite` |
| Database class: `HabitLockDatabase` | `RiteDatabase` |
| SQLDelight file: `HabitLock.sq` | `Rite.sq` |
| DI component: `HabitLockAppComponent` | `RiteAppComponent` |
| Application class: `HabitLockApplication` | `RiteApplication` |
| Extension property: `habitLockApplication` | `riteApplication` |
| Navigation composable: `HabitLockNavigation` | `RiteNavigation` |
| Bottom nav composable: `HabitLockBottomNav` | `RiteBottomNav` |
| Strictness tier: Locked | Unwavering |
| Android theme: `Theme.HabitLock` | `Theme.Rite` |
| CI env vars: `HABITLOCK_STORE_FILE`, etc. | `RITE_STORE_FILE`, etc. |
| CI artifact names: `habitlock-rc-*`, `habitlock-release-*` | `rite-rc-*`, `rite-release-*` |
| iOS product name: HabitLock | Rite |
| iOS bundle ID: `com.ricardocosteira.habitlock.HabitLock` | `com.ricardocosteira.rite.Rite` |

---

## 2. Onboarding Copy Refresh

### Philosophy Screen

**Heading:**
- Old: "Enforce what you commit to."
- New: **"Show up for yourself."**

**Body:**
- Old: "Keep promises to yourself — even on hard days.\n\nYou choose the rules. HabitLock helps you stick to them."
- New: **"Turn your goals into daily rituals — even on the hard days.\n\nYou set the standard. Rite helps you hold it."**

**CTA:**
- Old: "Accept the Commitment"
- New: **"I'm Ready"**

### First Habit Screen

**Heading:**
- Old: "Lock in your first habit"
- New: **"Start your first rite"**

### Strictness Screen

**Tier rename only:**
- Old label: "Locked"
- New label: **"Unwavering"**
- Old description: "No excuses. Full accountability."
- New description: **"No excuses. Show up every time."**

Note: The strictness labels/descriptions live in two places:
1. `OnboardingStrictnessPreset.kt` — hardcoded enum values (this is what's actually rendered in the onboarding UI)
2. `strings_onboarding_strictness.xml` — XML string resources (used in the settings screen)

Both must be updated for the "Locked" → "Unwavering" rename.

All other strictness copy (Flexible, Balanced, their descriptions and rules) remains unchanged.

---

## 3. File Impact

### Configuration Files (5 files)

- `composeApp/build.gradle.kts` — namespace, applicationId, desktop packageName, SQLDelight database name and package, signing env var names
- `settings.gradle.kts` — rootProject.name
- `composeApp/src/androidMain/AndroidManifest.xml` — application class name, theme references
- `.github/workflows/ci.yml` — env var names (`HABITLOCK_*` to `RITE_*`), artifact names, APK filenames
- `iosApp/Configuration/Config.xcconfig` — PRODUCT_NAME, PRODUCT_BUNDLE_IDENTIFIER

### Android Resources (6 files)

- `composeApp/src/androidMain/res/values/strings.xml` — app_name, tracking_summary_title
- `composeApp/src/androidMain/res/values/themes.xml` — Theme.HabitLock references
- `composeApp/src/androidMain/res/values-night/themes.xml` — Theme.HabitLock references

### Onboarding String Resources (2 files)

- `composeApp/src/commonMain/composeResources/values/strings_onboarding_philosophy.xml` — heading, body, CTA
- `composeApp/src/commonMain/composeResources/values/strings_onboarding_first_habit.xml` — heading

### Strictness String Resources (1 file)

- `composeApp/src/commonMain/composeResources/values/strings_onboarding_strictness.xml` — Locked label and description

### Kotlin Source Files (~240 files)

All files under `com.ricardocosteira.habitlock` — directory structure renamed to `com.ricardocosteira.rite`, all package declarations and imports updated.

**Class renames:**
- `HabitLockDatabase` → `RiteDatabase`
- `HabitLockAppComponent` → `RiteAppComponent`
- `HabitLockApplication` → `RiteApplication`
- `HabitLockNavigation` → `RiteNavigation`
- `HabitLockBottomNav` → `RiteBottomNav`
- `habitLockApplication` (extension property) → `riteApplication`

### SQLDelight (1 file + directory)

- `composeApp/src/commonMain/sqldelight/com/ricardocosteira/habitlock/data/database/HabitLock.sq` → renamed to `Rite.sq` under `com/ricardocosteira/rite/data/database/`
- Migration files (`.sqm`) move to the new directory path

### iOS Project (1 file)

- `iosApp/iosApp.xcodeproj/project.pbxproj` — product name references

### Documentation (2 files)

- `README.md` — title, all HabitLock references, architecture paths
- `CLAUDE.md` — class name references

### Domain Model (1 file)

- `StrictnessPreset.kt` (or equivalent enum) — `LOCKED` → `UNWAVERING`

---

## 4. What Does NOT Change

- **Keystore file** — stays at its current path; only env var names change
- **Database schema and migrations** — SQLDelight table names are defined in `.sq` queries, not the file/class name. Existing `Habit`, `HabitInstance`, `User` tables are unaffected.
- **Domain models, use cases, repository interfaces** — only package paths change, no logic changes
- **Business logic** — zero behavioral changes
- **Notification icons** — already generic (journal-check), not HabitLock-branded

---

## 5. Testing Strategy

- **Compilation across all targets** (Android, JVM, iOS) is the primary gate — if it builds, the rename is correct.
- **Existing unit tests** run as-is with new package paths to verify nothing broke.
- **Screenshot golden images** will fail on onboarding and strictness screens due to copy changes. Re-record as the final step.
- **Manual smoke test** — install on device, verify "Rite" appears in launcher and notifications.

No new tests needed. This is a rename, not a behavior change.

---

## 6. Post-Merge Manual Steps

1. Rename GitHub repo: `rcosteira79/HabitLock` → `rcosteira79/Rite`
2. Update GitHub secrets: `HABITLOCK_*` → `RITE_*`
3. Update local git remotes to point to new repo URL
4. Replace v1.0.0 release APK with renamed build
5. Update any branch protection rulesets if needed after repo rename
