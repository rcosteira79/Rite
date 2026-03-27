# CI Pipeline Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Set up a GitHub Actions CI pipeline that runs compile+tests on every push and PR, gates an iOS build on PRs, and produces versioned Android artifacts on pushes to `develop` and `master`.

**Architecture:** A single `.github/workflows/ci.yml` with three jobs (`test`, `ios-build`, `android-artifact`) gated by trigger conditions, all depending on `test` passing. Supporting changes: `roborazzi.test.compare=true` in `gradle.properties` enforces screenshot comparison on every test run, and a new `rc` Android build type produces release-like artifacts from `develop` without requiring a keystore.

**Tech Stack:** GitHub Actions, Gradle 8.14.3, AGP 8.13.2, Kotlin 2.3.0, Roborazzi, `actions/checkout@v4`, `actions/setup-java@v4`, `gradle/actions/setup-gradle@v4`, `actions/upload-artifact@v4`

---

## File Map

| File | Change |
|---|---|
| `gradle.properties` | Add `roborazzi.test.compare=true` |
| `composeApp/build.gradle.kts` | Add `rc` build type inside `buildTypes` block (lines 117–121) |
| `.github/workflows/ci.yml` | Create — full workflow definition |

---

### Task 1: Create `develop` branch

**Files:** Git operations only — no file changes.

- [ ] **Step 1: Create and push `develop` from `master`**

```bash
git checkout master
git pull
git checkout -b develop
git push -u origin develop
git checkout master
```

Expected: `develop` branch now exists on the remote.

- [ ] **Step 2: Create a feature branch for this work**

```bash
git checkout -b feature/ci-pipeline
```

---

### Task 2: Enable Roborazzi comparison mode

**Files:**
- Modify: `gradle.properties`

- [ ] **Step 1: Add comparison flag**

Add this block to the end of `gradle.properties`:

```properties
#Roborazzi
roborazzi.test.compare=true
```

Full file after the edit:

```properties
#Kotlin
kotlin.code.style=official
kotlin.daemon.jvmargs=-Xmx3072M

#Gradle
org.gradle.jvmargs=-Xmx4096M -Dfile.encoding=UTF-8
org.gradle.configuration-cache=true
org.gradle.caching=true

#Android
android.nonTransitiveRClass=true
android.useAndroidX=true

#Roborazzi
roborazzi.test.compare=true
```

- [ ] **Step 2: Run tests to verify comparison mode works**

```bash
./gradlew :composeApp:testDebugUnitTest --no-build-cache
```

Expected: `BUILD SUCCESSFUL`. All screenshot tests pass against the committed baselines. If any test fails with a screenshot mismatch, the baselines are out of date — re-record them first with `./gradlew :composeApp:recordRoborazziDebug --no-build-cache`, commit the updated PNGs, and re-run.

- [ ] **Step 3: Commit**

```bash
git add gradle.properties
git commit -m "test: enable roborazzi screenshot comparison on test runs"
```

---

### Task 3: Add `rc` build type

**Files:**
- Modify: `composeApp/build.gradle.kts` (lines 117–121)

- [ ] **Step 1: Replace the `buildTypes` block**

Find this block in `composeApp/build.gradle.kts` (currently lines 117–121):

```kotlin
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
```

Replace it with:

```kotlin
    buildTypes {
        create("rc") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }
```

`rc` inherits all `release` settings (R8, no debug flags) but signs with the local debug key so it can be installed without a keystore. `isMinifyEnabled` stays `false` on both types for now — enable it when signing is configured and shrinking is validated.

- [ ] **Step 2: Verify `assembleRc` builds successfully**

```bash
./gradlew :composeApp:assembleRc
```

Expected: `BUILD SUCCESSFUL`. APK produced under `composeApp/build/outputs/apk/rc/`.

- [ ] **Step 3: Commit**

```bash
git add composeApp/build.gradle.kts
git commit -m "build: add rc build type for release-candidate artifacts"
```

---

### Task 4: Create GitHub Actions workflow

**Files:**
- Create: `.github/workflows/ci.yml`

- [ ] **Step 1: Create the workflow file**

Create `.github/workflows/ci.yml` with this exact content:

```yaml
name: CI

on:
  push:
    branches: [master, develop]
  pull_request:
    branches: [master, develop]

jobs:
  test:
    name: Compile & Test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - uses: gradle/actions/setup-gradle@v4
      - name: Run tests
        run: ./gradlew :composeApp:testDebugUnitTest --no-build-cache

  ios-build:
    name: iOS Build
    runs-on: macos-latest
    needs: test
    if: github.event_name == 'pull_request'
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - uses: gradle/actions/setup-gradle@v4
      - name: Link Kotlin framework
        run: ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
      - name: Build Xcode project
        run: |
          xcodebuild \
            -project iosApp/iosApp.xcodeproj \
            -scheme iosApp \
            -destination 'generic/platform=iOS Simulator' \
            CODE_SIGNING_ALLOWED=NO \
            build

  android-artifact:
    name: Android Artifact
    runs-on: ubuntu-latest
    needs: test
    if: github.event_name == 'push'
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - uses: gradle/actions/setup-gradle@v4
      - name: Get short SHA
        id: slug
        run: echo "sha=$(echo $GITHUB_SHA | head -c 7)" >> $GITHUB_OUTPUT
      - name: Build RC APK
        if: github.ref == 'refs/heads/develop'
        run: ./gradlew :composeApp:assembleRc
      - name: Build Release APK
        if: github.ref == 'refs/heads/master'
        run: ./gradlew :composeApp:assembleRelease
      - name: Upload RC artifact
        if: github.ref == 'refs/heads/develop'
        uses: actions/upload-artifact@v4
        with:
          name: habitlock-rc-${{ steps.slug.outputs.sha }}
          path: composeApp/build/outputs/apk/rc/**/*.apk
      - name: Upload Release artifact
        if: github.ref == 'refs/heads/master'
        uses: actions/upload-artifact@v4
        with:
          name: habitlock-release-${{ steps.slug.outputs.sha }}
          path: composeApp/build/outputs/apk/release/**/*.apk
```

- [ ] **Step 2: Commit the workflow**

```bash
git add .github/workflows/ci.yml
git commit -m "ci: add GitHub Actions CI pipeline"
```

- [ ] **Step 3: Push and open a PR targeting `master`**

```bash
git push -u origin feature/ci-pipeline
gh pr create \
  --title "ci: add GitHub Actions CI pipeline" \
  --body "$(cat <<'EOF'
## Summary
- Adds \`.github/workflows/ci.yml\` with \`test\`, \`ios-build\`, and \`android-artifact\` jobs
- Enables Roborazzi screenshot comparison on every test run via \`gradle.properties\`
- Adds \`rc\` Android build type for release-candidate artifacts from \`develop\`

## Test Plan
- [ ] \`test\` job runs on this PR and passes
- [ ] \`ios-build\` job runs on this PR (after \`test\` passes) and passes
- [ ] \`android-artifact\` job does **not** appear on this PR (push-only)
- [ ] After merging, push a commit to \`develop\` — confirm \`android-artifact\` produces a \`habitlock-rc-<sha>\` artifact
- [ ] After merging, push a commit to \`master\` — confirm \`android-artifact\` produces a \`habitlock-release-<sha>\` artifact
EOF
)"
```

Expected: PR is created. GitHub Actions immediately queues `test` and (after it passes) `ios-build`.

- [ ] **Step 4: Verify CI passes on the PR**

Open the PR on GitHub and confirm:
- `test` job: green
- `ios-build` job: green (runs after `test`)
- `android-artifact` job: not present

Once both are green, merge the PR.
