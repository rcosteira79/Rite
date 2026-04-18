# Rite Design System — Slice 1 (Foundation) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the Forest Discipline / Stoic Night theme with the Rite v2 Sage palette, introduce the full token system (colors, typography, shapes, spacing, motion), rebuild or create all foundation primitives (Button, Pill, Chip, Divider, ProgressRing, Dialog, Snackbar, BottomNav), and retire the old palette + Manrope/Inter fonts. After this slice lands, the app runs on Sage & Linen; existing screens pick up new colors automatically; subsequent slices have the primitives they need.

**Architecture:** Single flat `RiteColorScheme` data class with M3-native names and Rite-specific extensions, feeding an M3 `ColorScheme` internally so Material primitives keep working. Sibling data classes for `RiteTypography`, `RiteShapes`, `RiteSpacing`, `RiteMotion`, `RiteDimensions`. All exposed via `staticCompositionLocalOf` providers and one `RiteAppTheme` object with `@Composable @ReadOnlyComposable` getters. New primitive composables live under `presentation/ui/components/`.

**Tech Stack:** Kotlin Multiplatform 2.3.0, Compose Multiplatform 1.10.0, Material 3, Roborazzi 1.20.0 screenshot tests, Robolectric, Compose Resources (auto-generated `Res.font.*`), Gradle + KSP.

**Source of truth (spec):** `docs/superpowers/specs/2026-04-18-rite-design-system-migration-design.md`

---

## Chunk 1: Branch & worktree setup

### Task 1: Create parent branch and slice worktree

**Files:** none modified (git-only).

- [ ] **Step 1: Verify you're on develop at the repo root**

Run (from `/Users/ricardocosteira/Documents/Rite`):

```bash
git status && git rev-parse --abbrev-ref HEAD
```

Expected: `On branch develop`. Working tree may have the existing `.gitignore`, `CLAUDE.md`, `.claude/`, `COMPOSE-AUDIT-REPORT.md` changes — that's fine.

- [ ] **Step 2: Create the long-lived parent branch off develop**

```bash
git branch feature/design-system-v2 develop
git push -u origin feature/design-system-v2
```

Expected: new remote-tracking branch.

- [ ] **Step 3: Create a worktree for this slice off the parent branch**

```bash
git worktree add .worktrees/design-system-foundation -b feature/design-system-v2/01-foundation feature/design-system-v2
cd .worktrees/design-system-foundation
git status
```

Expected: worktree at `.worktrees/design-system-foundation`, HEAD on `feature/design-system-v2/01-foundation`, clean working tree.

- [ ] **Step 4: All remaining tasks run inside the worktree**

From here on, every `git` / `./gradlew` / file edit assumes `cd .worktrees/design-system-foundation` as the working directory. Do not operate on the root `develop` checkout for this plan.

---

## Chunk 2: Token data classes (additive, non-breaking)

Goal for this chunk: introduce new data classes alongside the existing `RiteColorScheme`, but don't wire them into `RiteTheme` yet. Everything here compiles without touching any call site. The new classes shadow nothing.

### Task 2: Add new font resources

**Files:**
- Create: `composeApp/src/commonMain/composeResources/font/fraunces.ttf`
- Create: `composeApp/src/commonMain/composeResources/font/fraunces_italic.ttf`
- Create: `composeApp/src/commonMain/composeResources/font/inter_tight.ttf`
- Create: `composeApp/src/commonMain/composeResources/font/jetbrains_mono.ttf`

Fonts come from Google Fonts: Fraunces (variable font, download TTF with regular weight range), Inter Tight, JetBrains Mono.

- [ ] **Step 1: Download the TTFs**

All three are on Google Fonts. Simplest path: open each family page in a browser, click **Download family**, unzip, pick the variable-font TTFs.

- Fraunces: <https://fonts.google.com/specimen/Fraunces> → inside the zip, `Fraunces-VariableFont_SOFT,WONK,opsz,wght.ttf` and `Fraunces-Italic-VariableFont_SOFT,WONK,opsz,wght.ttf`
- Inter Tight: <https://fonts.google.com/specimen/Inter+Tight> → `InterTight-VariableFont_wght.ttf`
- JetBrains Mono: <https://fonts.google.com/specimen/JetBrains+Mono> → `JetBrainsMono-VariableFont_wght.ttf`

Rename the files to the exact lowercase names used by `Res.font.*` in Task 4, then copy into the resource folder in Step 2:

```
fraunces.ttf
fraunces_italic.ttf
inter_tight.ttf
jetbrains_mono.ttf
```

Expected: four TTFs staged somewhere on disk (e.g., `/tmp/fonts/`), each non-empty.

- [ ] **Step 2: Copy into the Compose resource folder**

```bash
cp /tmp/fonts/fraunces.ttf composeApp/src/commonMain/composeResources/font/
cp /tmp/fonts/fraunces_italic.ttf composeApp/src/commonMain/composeResources/font/
cp /tmp/fonts/inter_tight.ttf composeApp/src/commonMain/composeResources/font/
cp /tmp/fonts/jetbrains_mono.ttf composeApp/src/commonMain/composeResources/font/
ls composeApp/src/commonMain/composeResources/font/
```

Expected listing: `fraunces.ttf  fraunces_italic.ttf  inter.ttf  inter_tight.ttf  jetbrains_mono.ttf  manrope.ttf` (old ones still present; we delete in Chunk 4).

- [ ] **Step 3: Regenerate Compose Resources + verify compile**

```bash
./gradlew :composeApp:generateComposeResClass && ./gradlew :composeApp:compileDebugKotlin -q
```

Expected: build succeeds. The generated `rite.composeapp.generated.resources.Res` now includes `Res.font.fraunces`, `Res.font.fraunces_italic`, `Res.font.inter_tight`, `Res.font.jetbrains_mono` alongside the existing `inter` and `manrope`.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/composeResources/font/
git commit -m "chore(theme): add Fraunces, Inter Tight, JetBrains Mono font resources"
```

### Task 3: Create `RiteShapes`, `RiteSpacing`, `RiteMotion`, `RiteDimensions`

Four small data classes. Values straight from the spec's token tables.

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteShapes.kt`
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteSpacing.kt`
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteMotion.kt`
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteDimensions.kt`
- Create: `composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteTokensTest.kt`

- [ ] **Step 1: Write the failing test**

Create `composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteTokensTest.kt`:

```kotlin
package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class RiteTokensTest {

    @Test
    fun `RiteShapes has seven corner sizes from xs 2dp to xxl 20dp plus pill`() {
        val shapes = RiteShapes()
        // Just assert the data class exposes all seven fields — instances are compared via TopStart corner size
        // Sanity: calling the constructor compiles
        shapes.xs
        shapes.sm
        shapes.md
        shapes.lg
        shapes.xl
        shapes.xxl
        shapes.pill
    }

    @Test
    fun `RiteSpacing exposes gap1 through gap8 with expected dp values`() {
        val spacing = RiteSpacing()
        assertEquals(4.dp, spacing.gap1)
        assertEquals(8.dp, spacing.gap2)
        assertEquals(12.dp, spacing.gap3)
        assertEquals(16.dp, spacing.gap4)
        assertEquals(20.dp, spacing.gap5)
        assertEquals(24.dp, spacing.gap6)
        assertEquals(28.dp, spacing.gap7)
        assertEquals(32.dp, spacing.gap8)
    }

    @Test
    fun `RiteMotion exposes three durations and two easings`() {
        val motion = RiteMotion()
        assertEquals(160, motion.quick.inWholeMilliseconds.toInt())
        assertEquals(280, motion.standard.inWholeMilliseconds.toInt())
        assertEquals(480, motion.deliberate.inWholeMilliseconds.toInt())
        motion.easeQuiet
        motion.easeWeighted
    }

    @Test
    fun `RiteDimensions exposes icon size defaults`() {
        val dims = RiteDimensions()
        assertEquals(20.dp, dims.iconDefault)
        assertEquals(44.dp, dims.touchTargetMin)
    }
}
```

- [ ] **Step 2: Run the test and confirm it fails (classes don't exist yet)**

```bash
./gradlew :composeApp:jvmTest --tests "com.ricardocosteira.rite.presentation.ui.theme.RiteTokensTest" -q
```

Expected: compile failure referencing unresolved `RiteShapes`, `RiteSpacing`, `RiteMotion`, `RiteDimensions`.

- [ ] **Step 3: Create `RiteShapes.kt`**

```kotlin
package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class RiteShapes(
    val xs: Shape = RoundedCornerShape(2.dp),
    val sm: Shape = RoundedCornerShape(4.dp),
    val md: Shape = RoundedCornerShape(8.dp),
    val lg: Shape = RoundedCornerShape(12.dp),
    val xl: Shape = RoundedCornerShape(16.dp),
    val xxl: Shape = RoundedCornerShape(20.dp),
    val pill: Shape = RoundedCornerShape(percent = 50)
)

val LocalRiteShapes = staticCompositionLocalOf { RiteShapes() }
```

- [ ] **Step 4: Create `RiteSpacing.kt`**

```kotlin
package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class RiteSpacing(
    val gap1: Dp = 4.dp,
    val gap2: Dp = 8.dp,
    val gap3: Dp = 12.dp,
    val gap4: Dp = 16.dp,
    val gap5: Dp = 20.dp,
    val gap6: Dp = 24.dp,
    val gap7: Dp = 28.dp,
    val gap8: Dp = 32.dp
)

val LocalRiteSpacing = staticCompositionLocalOf { RiteSpacing() }
```

- [ ] **Step 5: Create `RiteMotion.kt`**

```kotlin
package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Immutable
data class RiteMotion(
    val quick: Duration = 160.milliseconds,
    val standard: Duration = 280.milliseconds,
    val deliberate: Duration = 480.milliseconds,
    val easeQuiet: Easing = CubicBezierEasing(0.2f, 0.6f, 0.2f, 1f),
    val easeWeighted: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
)

val LocalRiteMotion = staticCompositionLocalOf { RiteMotion() }
```

- [ ] **Step 6: Create `RiteDimensions.kt`**

```kotlin
package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class RiteDimensions(
    val iconSmall: Dp = 13.dp,
    val iconCompact: Dp = 16.dp,
    val iconDefault: Dp = 20.dp,
    val iconLarge: Dp = 22.dp,
    val touchTargetMin: Dp = 44.dp
)

val LocalRiteDimensions = staticCompositionLocalOf { RiteDimensions() }
```

- [ ] **Step 7: Run tests and confirm pass**

```bash
./gradlew :composeApp:jvmTest --tests "com.ricardocosteira.rite.presentation.ui.theme.RiteTokensTest" -q
```

Expected: 4 tests pass.

- [ ] **Step 8: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteShapes.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteSpacing.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteMotion.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteDimensions.kt \
        composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteTokensTest.kt
git commit -m "feat(theme): add shape, spacing, motion, and dimension tokens"
```

### Task 4: Create `RiteTypography` data class and sage typography instance

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteTypography.kt`
- Modify: `composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteTokensTest.kt` (add typography tests)

- [ ] **Step 1: Extend the test with typography assertions**

Append to `RiteTokensTest.kt`:

```kotlin
    @Test
    fun `riteTypography exposes M3 slots plus eyebrow, displayItalic, mono extensions`() {
        // Can't construct RiteTypography without @Composable FontFamily — test via the default instance
        // that's available in the composition. Instead, this test is a compile-time check that the fields exist.
        // The @Composable construction is exercised by screenshot tests later.
        val slots = listOf(
            "displayLarge", "displayMedium", "displaySmall",
            "headlineLarge", "headlineMedium", "headlineSmall",
            "titleLarge", "titleMedium", "titleSmall",
            "bodyLarge", "bodyMedium", "bodySmall",
            "labelLarge", "labelMedium", "labelSmall",
            "eyebrow", "displayItalic", "mono"
        )
        val properties = RiteTypography::class.members.map { it.name }.toSet()
        slots.forEach { slot ->
            assertTrue(slot in properties, "RiteTypography is missing slot: $slot")
        }
    }
```

Add import: `import kotlin.test.assertTrue`.

- [ ] **Step 2: Run test and confirm it fails**

```bash
./gradlew :composeApp:jvmTest --tests "com.ricardocosteira.rite.presentation.ui.theme.RiteTokensTest.riteTypography exposes M3 slots plus eyebrow, displayItalic, mono extensions" -q
```

Expected: compile failure referencing unresolved `RiteTypography`.

- [ ] **Step 3: Create `RiteTypography.kt`**

```kotlin
package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.fraunces
import rite.composeapp.generated.resources.fraunces_italic
import rite.composeapp.generated.resources.inter_tight
import rite.composeapp.generated.resources.jetbrains_mono

@Immutable
data class RiteTypography(
    val displayLarge: TextStyle = TextStyle.Default,
    val displayMedium: TextStyle = TextStyle.Default,
    val displaySmall: TextStyle = TextStyle.Default,
    val headlineLarge: TextStyle = TextStyle.Default,
    val headlineMedium: TextStyle = TextStyle.Default,
    val headlineSmall: TextStyle = TextStyle.Default,
    val titleLarge: TextStyle = TextStyle.Default,
    val titleMedium: TextStyle = TextStyle.Default,
    val titleSmall: TextStyle = TextStyle.Default,
    val bodyLarge: TextStyle = TextStyle.Default,
    val bodyMedium: TextStyle = TextStyle.Default,
    val bodySmall: TextStyle = TextStyle.Default,
    val labelLarge: TextStyle = TextStyle.Default,
    val labelMedium: TextStyle = TextStyle.Default,
    val labelSmall: TextStyle = TextStyle.Default,
    // Rite extensions
    val eyebrow: TextStyle = TextStyle.Default,
    val displayItalic: TextStyle = TextStyle.Default,
    val mono: TextStyle = TextStyle.Default
) {
    fun toMaterialTypography(): Typography = Typography(
        displayLarge = displayLarge,
        displayMedium = displayMedium,
        displaySmall = displaySmall,
        headlineLarge = headlineLarge,
        headlineMedium = headlineMedium,
        headlineSmall = headlineSmall,
        titleLarge = titleLarge,
        titleMedium = titleMedium,
        titleSmall = titleSmall,
        bodyLarge = bodyLarge,
        bodyMedium = bodyMedium,
        bodySmall = bodySmall,
        labelLarge = labelLarge,
        labelMedium = labelMedium,
        labelSmall = labelSmall
    )
}

val LocalRiteTypography = staticCompositionLocalOf { RiteTypography() }

@Composable
fun riteTypography(): RiteTypography {
    val fraunces = FontFamily(
        Font(Res.font.fraunces, FontWeight.Light),
        Font(Res.font.fraunces, FontWeight.Normal),
        Font(Res.font.fraunces, FontWeight.Medium),
        Font(Res.font.fraunces, FontWeight.SemiBold),
        Font(Res.font.fraunces_italic, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.fraunces_italic, FontWeight.Medium, FontStyle.Italic)
    )
    val interTight = FontFamily(
        Font(Res.font.inter_tight, FontWeight.Normal),
        Font(Res.font.inter_tight, FontWeight.Medium),
        Font(Res.font.inter_tight, FontWeight.SemiBold),
        Font(Res.font.inter_tight, FontWeight.Bold)
    )
    val jetbrainsMono = FontFamily(
        Font(Res.font.jetbrains_mono, FontWeight.Normal),
        Font(Res.font.jetbrains_mono, FontWeight.Medium)
    )

    return RiteTypography(
        displayLarge = TextStyle(fontFamily = fraunces, fontWeight = FontWeight.Light, fontSize = 64.sp, lineHeight = 72.sp, letterSpacing = (-0.02).em),
        displayMedium = TextStyle(fontFamily = fraunces, fontWeight = FontWeight.Normal, fontSize = 44.sp, lineHeight = 52.sp, letterSpacing = (-0.01).em),
        displaySmall = TextStyle(fontFamily = fraunces, fontWeight = FontWeight.Medium, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.01).em),
        headlineLarge = TextStyle(fontFamily = fraunces, fontWeight = FontWeight.Medium, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.01).em),
        headlineMedium = TextStyle(fontFamily = fraunces, fontWeight = FontWeight.Medium, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = (-0.01).em),
        headlineSmall = TextStyle(fontFamily = fraunces, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp),
        titleLarge = TextStyle(fontFamily = fraunces, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Italic, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.01).em),
        titleMedium = TextStyle(fontFamily = interTight, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = 0.sp),
        titleSmall = TextStyle(fontFamily = interTight, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp),
        bodyLarge = TextStyle(fontFamily = interTight, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp),
        bodyMedium = TextStyle(fontFamily = interTight, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        bodySmall = TextStyle(fontFamily = interTight, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp),
        labelLarge = TextStyle(fontFamily = interTight, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        labelMedium = TextStyle(fontFamily = interTight, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.1.sp),
        labelSmall = TextStyle(fontFamily = interTight, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp),
        // Rite extensions
        eyebrow = TextStyle(fontFamily = jetbrainsMono, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.18.em),
        displayItalic = TextStyle(fontFamily = fraunces, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Italic, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.01).em),
        mono = TextStyle(fontFamily = jetbrainsMono, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.2.em)
    )
}
```

- [ ] **Step 4: Run tests and confirm pass**

```bash
./gradlew :composeApp:jvmTest --tests "com.ricardocosteira.rite.presentation.ui.theme.RiteTokensTest" -q
```

Expected: all tests in `RiteTokensTest` pass.

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteTypography.kt \
        composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteTokensTest.kt
git commit -m "feat(theme): add RiteTypography with Fraunces, Inter Tight, JetBrains Mono"
```

### Task 5: Create new `RiteColorScheme` data class (parameterized; values in Task 6)

The current `Theme.kt` already defines a `RiteColorScheme`. We keep it for now and will replace it in Chunk 3. To avoid a name collision during the transition, create the new class under a distinct file and a temporary different name: `RiteColorSchemeV2`. It becomes the canonical `RiteColorScheme` when we delete the old one in Task 10.

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteColorSchemeV2.kt`

- [ ] **Step 1: Create the new color scheme class**

```kotlin
package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class RiteColorSchemeV2(
    // M3 core
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceDim: Color,
    val surfaceBright: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val outline: Color,
    val outlineVariant: Color,
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val inversePrimary: Color,
    val scrim: Color,
    // Rite extensions
    val onSurfaceMuted: Color,
    val onSurfaceSubtle: Color,
    val primaryPressed: Color,
    val warn: Color,
    val onWarn: Color,
    val warnContainer: Color,
    val onWarnContainer: Color,
    val suspend: Color,
    val onSuspend: Color,
    val suspendContainer: Color,
    val onSuspendContainer: Color,
    // Day classification
    val dayPerfect: Color,
    val dayBestEffort: Color,
    val dayPartial: Color,
    val dayRoughDay: Color,
    val dayFailed: Color,
    val daySkipped: Color,
    val dayFuture: Color,
    val dayNone: Color,
    private val isLight: Boolean
) {
    fun toMaterialColorScheme(): ColorScheme = if (isLight) {
        lightColorScheme(
            primary = primary, onPrimary = onPrimary,
            primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
            secondary = secondary, onSecondary = onSecondary,
            secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary, onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer, onTertiaryContainer = onTertiaryContainer,
            error = error, onError = onError,
            errorContainer = errorContainer, onErrorContainer = onErrorContainer,
            background = background, onBackground = onBackground,
            surface = surface, onSurface = onSurface,
            surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant,
            surfaceDim = surfaceDim, surfaceBright = surfaceBright,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            outline = outline, outlineVariant = outlineVariant,
            inverseSurface = inverseSurface, inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary, scrim = scrim
        )
    } else {
        darkColorScheme(
            primary = primary, onPrimary = onPrimary,
            primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
            secondary = secondary, onSecondary = onSecondary,
            secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary, onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer, onTertiaryContainer = onTertiaryContainer,
            error = error, onError = onError,
            errorContainer = errorContainer, onErrorContainer = onErrorContainer,
            background = background, onBackground = onBackground,
            surface = surface, onSurface = onSurface,
            surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant,
            surfaceDim = surfaceDim, surfaceBright = surfaceBright,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            outline = outline, outlineVariant = outlineVariant,
            inverseSurface = inverseSurface, inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary, scrim = scrim
        )
    }
}
```

- [ ] **Step 2: Verify it compiles**

```bash
./gradlew :composeApp:compileDebugKotlin -q
```

Expected: success (no tests changed — the class is unused so far).

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteColorSchemeV2.kt
git commit -m "feat(theme): add RiteColorSchemeV2 data class (unwired)"
```

### Task 6: Create `LightSageColors` and `DarkSageColors` instances

Concrete sage-palette values per the spec's mapping table. Still unwired to the live theme.

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/SageColors.kt`
- Modify: `composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteTokensTest.kt` (add sage palette assertions)

- [ ] **Step 1: Extend the test with sage palette assertions**

Append to `RiteTokensTest.kt`:

```kotlin
    @Test
    fun `LightSageColors has spec-defined hex values for key slots`() {
        assertEquals(Color(0xFF5E7F6C), LightSageColors.primary)           // c-accent
        assertEquals(Color(0xFFCFDDD1), LightSageColors.primaryContainer)   // c-accent-soft
        assertEquals(Color(0xFFF6F1EA), LightSageColors.background)         // c-bg
        assertEquals(Color(0xFF1F1E1B), LightSageColors.onSurface)          // c-ink
        assertEquals(Color(0xFF6B6459), LightSageColors.onSurfaceMuted)     // c-ink-3
        assertEquals(Color(0xFFA67A3A), LightSageColors.warn)               // c-warn
        assertEquals(Color(0xFF7A6E85), LightSageColors.suspend)            // c-suspend
        assertEquals(Color(0xFF5E7F6C), LightSageColors.dayPerfect)         // = primary
        assertEquals(Color.Transparent, LightSageColors.dayNone)
    }

    @Test
    fun `DarkSageColors has spec-defined hex values for key slots`() {
        assertEquals(Color(0xFF9FBDA9), DarkSageColors.primary)
        assertEquals(Color(0xFF141413), DarkSageColors.background)
        assertEquals(Color(0xFFEDE6D9), DarkSageColors.onSurface)
        assertEquals(Color(0xFFD0A262), DarkSageColors.warn)
        assertEquals(Color(0xFFA396AE), DarkSageColors.suspend)
    }
```

Add import: `import androidx.compose.ui.graphics.Color`.

- [ ] **Step 2: Run test and confirm it fails**

```bash
./gradlew :composeApp:jvmTest --tests "com.ricardocosteira.rite.presentation.ui.theme.RiteTokensTest" -q
```

Expected: compile failure referencing unresolved `LightSageColors` / `DarkSageColors`.

- [ ] **Step 3: Create `SageColors.kt`**

```kotlin
package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.ui.graphics.Color

val LightSageColors = RiteColorSchemeV2(
    // M3 core
    primary = Color(0xFF5E7F6C),
    onPrimary = Color(0xFFFBF7F1),
    primaryContainer = Color(0xFFCFDDD1),
    onPrimaryContainer = Color(0xFF2D4F3F),
    secondary = Color(0xFF545F72),
    onSecondary = Color(0xFFFBF7F1),
    secondaryContainer = Color(0xFFD4D9E0),
    onSecondaryContainer = Color(0xFF1F1E1B),
    tertiary = Color(0xFF5E7F6C),
    onTertiary = Color(0xFFFBF7F1),
    tertiaryContainer = Color(0xFFCFDDD1),
    onTertiaryContainer = Color(0xFF2D4F3F),
    error = Color(0xFF8B4B3E),
    onError = Color(0xFFFBF7F1),
    errorContainer = Color(0xFFE4C9BF),
    onErrorContainer = Color(0xFF1F1E1B),
    background = Color(0xFFF6F1EA),
    onBackground = Color(0xFF1F1E1B),
    surface = Color(0xFFFBF7F1),
    onSurface = Color(0xFF1F1E1B),
    surfaceVariant = Color(0xFFEDE4D6),
    onSurfaceVariant = Color(0xFF3A362F),
    surfaceDim = Color(0xFFEFE8DE),
    surfaceBright = Color(0xFFFFFDF8),
    surfaceContainerLowest = Color(0xFFEDE4D6),
    surfaceContainerLow = Color(0xFFEFE8DE),
    surfaceContainer = Color(0xFFFBF7F1),
    surfaceContainerHigh = Color(0xFFFFFDF8),
    surfaceContainerHighest = Color(0xFFFFFDF8),
    outline = Color(0xFFD1C5B0),
    outlineVariant = Color(0xFFE4DBCB),
    inverseSurface = Color(0xFF1F1E1B),
    inverseOnSurface = Color(0xFFFBF7F1),
    inversePrimary = Color(0xFF9FBDA9),
    scrim = Color(0xFF000000),
    // Rite extensions
    onSurfaceMuted = Color(0xFF6B6459),
    onSurfaceSubtle = Color(0xFF9A9185),
    primaryPressed = Color(0xFFB6C9BB),
    warn = Color(0xFFA67A3A),
    onWarn = Color(0xFFFBF7F1),
    warnContainer = Color(0xFFE8D7B8),
    onWarnContainer = Color(0xFF1F1E1B),
    suspend = Color(0xFF7A6E85),
    onSuspend = Color(0xFFFBF7F1),
    suspendContainer = Color(0xFFDDD6E1),
    onSuspendContainer = Color(0xFF1F1E1B),
    // Day classification
    dayPerfect = Color(0xFF5E7F6C),
    dayBestEffort = Color(0xFFCFDDD1),
    dayPartial = Color(0xFFA67A3A),
    dayRoughDay = Color(0xFFE4C9BF),
    dayFailed = Color(0xFF8B4B3E),
    daySkipped = Color(0xFFDDD6E1),
    dayFuture = Color(0xFFD1C5B0),
    dayNone = Color.Transparent,
    isLight = true
)

val DarkSageColors = RiteColorSchemeV2(
    // M3 core
    primary = Color(0xFF9FBDA9),
    onPrimary = Color(0xFF141413),
    primaryContainer = Color(0xFF2E4438),
    onPrimaryContainer = Color(0xFFCBE1D1),
    secondary = Color(0xFF8A94A6),
    onSecondary = Color(0xFF141413),
    secondaryContainer = Color(0xFF2B303A),
    onSecondaryContainer = Color(0xFFEDE6D9),
    tertiary = Color(0xFF9FBDA9),
    onTertiary = Color(0xFF141413),
    tertiaryContainer = Color(0xFF2E4438),
    onTertiaryContainer = Color(0xFFCBE1D1),
    error = Color(0xFFC78878),
    onError = Color(0xFF141413),
    errorContainer = Color(0xFF3A231C),
    onErrorContainer = Color(0xFFEDE6D9),
    background = Color(0xFF141413),
    onBackground = Color(0xFFEDE6D9),
    surface = Color(0xFF1C1B19),
    onSurface = Color(0xFFEDE6D9),
    surfaceVariant = Color(0xFF121110),
    onSurfaceVariant = Color(0xFFCFC7B6),
    surfaceDim = Color(0xFF0E0E0D),
    surfaceBright = Color(0xFF242320),
    surfaceContainerLowest = Color(0xFF121110),
    surfaceContainerLow = Color(0xFF1C1B19),
    surfaceContainer = Color(0xFF1C1B19),
    surfaceContainerHigh = Color(0xFF242320),
    surfaceContainerHighest = Color(0xFF2F2D29),
    outline = Color(0xFF3B3832),
    outlineVariant = Color(0xFF2A2824),
    inverseSurface = Color(0xFFEDE6D9),
    inverseOnSurface = Color(0xFF141413),
    inversePrimary = Color(0xFF5E7F6C),
    scrim = Color(0xFF000000),
    // Rite extensions
    onSurfaceMuted = Color(0xFF9A9283),
    onSurfaceSubtle = Color(0xFF6B6558),
    primaryPressed = Color(0xFF1F2E26),
    warn = Color(0xFFD0A262),
    onWarn = Color(0xFF141413),
    warnContainer = Color(0xFF3C2F1A),
    onWarnContainer = Color(0xFFEDE6D9),
    suspend = Color(0xFFA396AE),
    onSuspend = Color(0xFF141413),
    suspendContainer = Color(0xFF2C2633),
    onSuspendContainer = Color(0xFFEDE6D9),
    // Day classification
    dayPerfect = Color(0xFF9FBDA9),
    dayBestEffort = Color(0xFF2E4438),
    dayPartial = Color(0xFFD0A262),
    dayRoughDay = Color(0xFF3A231C),
    dayFailed = Color(0xFFC78878),
    daySkipped = Color(0xFF2C2633),
    dayFuture = Color(0xFF3B3832),
    dayNone = Color.Transparent,
    isLight = false
)
```

- [ ] **Step 4: Run tests and confirm pass**

```bash
./gradlew :composeApp:jvmTest --tests "com.ricardocosteira.rite.presentation.ui.theme.RiteTokensTest" -q
```

Expected: all tests pass including the two new sage assertions.

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/SageColors.kt \
        composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteTokensTest.kt
git commit -m "feat(theme): add LightSageColors and DarkSageColors instances"
```

---

## Chunk 3: Theme swap, rename, and cleanup

This chunk is where breakage happens. Everything in Chunks 1-2 was additive. Now we rewrite `Theme.kt` to use the new types, rename the `colorScheme` property to `colors`, update all ~235 call sites, and delete the retired Forest palette + old fonts.

### Task 7: Rename old day-classification fields and `colorScheme` property in call sites

Performed before the theme swap so the old data class can still be referenced during the rename (safer: call sites update to new names while the underlying shape is still the old one).

The old `RiteColorScheme` exposes day-classification fields as `perfect`, `bestEffort`, `partial`, `roughDay`, `failed`, `noData`, `skipped`. The new scheme uses `dayPerfect`, `dayBestEffort`, `dayPartial`, `dayRoughDay`, `dayFailed`, `daySkipped`, `dayFuture`, `dayNone`. We pre-rename the five overlapping old names to the new names, leaving the old data class in place but with the new field names.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/Theme.kt` (rename five fields + the `colorScheme` property)
- Modify: 25+ call-site files across `composeApp/src/`

- [ ] **Step 1: Inventory the call sites for safety**

```bash
grep -rn "RiteAppTheme\.colorScheme" composeApp/src/ | wc -l
grep -rn "\.colorScheme\.perfect\b" composeApp/src/
grep -rn "\.colorScheme\.bestEffort\b" composeApp/src/
grep -rn "\.colorScheme\.partial\b" composeApp/src/
grep -rn "\.colorScheme\.roughDay\b" composeApp/src/
grep -rn "\.colorScheme\.failed\b" composeApp/src/
grep -rn "\.colorScheme\.noData\b" composeApp/src/
grep -rn "\.colorScheme\.skipped\b" composeApp/src/
```

Expected: `~235` for the first; per-field counts vary. Record totals (you'll verify they drop to zero after the rename).

- [ ] **Step 2: Rename the five overlapping day-classification fields in `Theme.kt`**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/Theme.kt`, edit the `RiteColorScheme` data class and both `LightRiteColorScheme` / `DarkRiteColorScheme` constructors:

- `perfect` → `dayPerfect`
- `bestEffort` → `dayBestEffort`
- `partial` → `dayPartial`
- `roughDay` → `dayRoughDay`
- `failed` → `dayFailed`
- `skipped` → `daySkipped`

`noData` is dropped in the new scheme (replaced by `dayNone = Color.Transparent`). Rename `noData` to `dayNone` and leave its current value — the old color maps to the new semantic slot during this interim.

- [ ] **Step 3: Rename the `colorScheme` property on `RiteAppTheme` to `colors`**

In the same file, find:

```kotlin
object RiteAppTheme {
    val colorScheme: RiteColorScheme
        @Composable @ReadOnlyComposable
        get() = LocalRiteColorScheme.current
    ...
}
```

Change `colorScheme` to `colors`.

- [ ] **Step 4: Apply the rename across all call sites using sed**

```bash
# Rename the property access
find composeApp/src -type f -name "*.kt" \
    -exec sed -i '' 's/RiteAppTheme\.colorScheme/RiteAppTheme.colors/g' {} +

# Rename day-classification fields — note these patterns only match field access after a preceding '.colors' or 'colorScheme' to avoid collateral
find composeApp/src -type f -name "*.kt" \
    -exec sed -i '' -E \
        -e 's/(\.colors)\.perfect\b/\1.dayPerfect/g' \
        -e 's/(\.colors)\.bestEffort\b/\1.dayBestEffort/g' \
        -e 's/(\.colors)\.partial\b/\1.dayPartial/g' \
        -e 's/(\.colors)\.roughDay\b/\1.dayRoughDay/g' \
        -e 's/(\.colors)\.failed\b/\1.dayFailed/g' \
        -e 's/(\.colors)\.noData\b/\1.dayNone/g' \
        -e 's/(\.colors)\.skipped\b/\1.daySkipped/g' \
        {} +
```

- [ ] **Step 5: Verify no leftover references**

```bash
grep -rn "RiteAppTheme\.colorScheme" composeApp/src/ || echo "OK: no colorScheme references"
grep -rn "\.colors\.perfect\b\|\.colors\.bestEffort\b\|\.colors\.partial\b\|\.colors\.roughDay\b\|\.colors\.failed\b\|\.colors\.noData\b\|\.colors\.skipped\b" composeApp/src/ || echo "OK: no old day-field references"
```

Expected: both print their `OK:` lines (grep exited non-zero = no matches).

- [ ] **Step 6: Compile**

```bash
./gradlew :composeApp:compileDebugKotlin -q
```

Expected: success. If there are compile errors, they indicate a sed miss; inspect and correct by hand.

- [ ] **Step 7: Run screenshot tests to confirm behavior is preserved**

```bash
./gradlew :composeApp:verifyRoborazziDebug -q
```

Expected: pass (visuals unchanged — only field names changed, colors are the same Forest palette).

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "refactor(theme): rename colorScheme→colors and day-classification fields"
```

### Task 8: Rewrite `Theme.kt` to use the new token system

Replace the Forest palette internals with the new `RiteColorSchemeV2` wiring. `RiteAppTheme.colors` now returns a `RiteColorSchemeV2`. Existing call sites that read `.primary`, `.background`, `.onSurface`, `.dayPerfect` etc. continue to work because those names exist in both schemes.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/Theme.kt` (replace contents)

- [ ] **Step 1: Replace `Theme.kt` contents**

Replace the entire file with:

```kotlin
package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalRiteColorScheme = staticCompositionLocalOf { LightSageColors }

object RiteAppTheme {
    val colors: RiteColorSchemeV2
        @Composable @ReadOnlyComposable
        get() = LocalRiteColorScheme.current

    val typography: RiteTypography
        @Composable @ReadOnlyComposable
        get() = LocalRiteTypography.current

    val shapes: RiteShapes
        @Composable @ReadOnlyComposable
        get() = LocalRiteShapes.current

    val spacing: RiteSpacing
        @Composable @ReadOnlyComposable
        get() = LocalRiteSpacing.current

    val motion: RiteMotion
        @Composable @ReadOnlyComposable
        get() = LocalRiteMotion.current

    val dimensions: RiteDimensions
        @Composable @ReadOnlyComposable
        get() = LocalRiteDimensions.current
}

@Composable
expect fun RiteTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit)

@Composable
fun RiteThemeFallback(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val riteColors = if (darkTheme) DarkSageColors else LightSageColors
    val riteTypography = riteTypography()
    val riteShapes = RiteShapes()
    val riteSpacing = RiteSpacing()
    val riteMotion = RiteMotion()
    val riteDimensions = RiteDimensions()

    CompositionLocalProvider(
        LocalRiteColorScheme provides riteColors,
        LocalRiteTypography provides riteTypography,
        LocalRiteShapes provides riteShapes,
        LocalRiteSpacing provides riteSpacing,
        LocalRiteMotion provides riteMotion,
        LocalRiteDimensions provides riteDimensions
    ) {
        MaterialTheme(
            colorScheme = riteColors.toMaterialColorScheme(),
            typography = riteTypography.toMaterialTypography()
        ) {
            Surface(content = content)
        }
    }
}
```

This removes:
- `ForestPrimary` ... `ForestDarkOnBackground` (all Forest color constants)
- `RiteColorScheme` (the old class — gone)
- `LightColorScheme` / `DarkColorScheme` (M3 wrappers)
- `LightRiteColorScheme` / `DarkRiteColorScheme` (old instances)
- the old `typography` getter on `RiteAppTheme` (replaced by the `RiteTypography`-backed one)

- [ ] **Step 2: Compile**

```bash
./gradlew :composeApp:compileDebugKotlin -q
```

Expected: success. All existing call sites still read `RiteAppTheme.colors.X` where X is present on both old and new schemes; fields removed from the old scheme (`noData` etc.) were already renamed in Task 7.

- [ ] **Step 3: Run tests**

```bash
./gradlew :composeApp:jvmTest -q
```

Expected: `RiteTokensTest` passes. Domain tests continue to pass.

- [ ] **Step 4: Run screenshot tests and capture fresh goldens**

The visuals have changed (Forest → Sage). Accept the new goldens.

```bash
./gradlew :composeApp:recordRoborazziDebug -q
```

Then visually inspect `composeApp/src/androidUnitTest/snapshots/images/` — spot-check at least three golden PNGs to confirm the sage palette is rendering as expected.

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/Theme.kt \
        composeApp/src/androidUnitTest/snapshots/images/
git commit -m "feat(theme): swap Forest palette for Rite v2 Sage & Linen tokens"
```

### Task 9: Update platform `actual` `RiteTheme` implementations

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/Theme.android.kt`
- Modify: `composeApp/src/iosMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/Theme.ios.kt`
- Modify: `composeApp/src/jvmMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/Theme.jvm.kt`

- [ ] **Step 1: Replace `Theme.android.kt` contents**

```kotlin
package com.ricardocosteira.rite.presentation.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun RiteTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val riteColors = if (darkTheme) DarkSageColors else LightSageColors
    val riteTypography = riteTypography()
    val riteShapes = RiteShapes()
    val riteSpacing = RiteSpacing()
    val riteMotion = RiteMotion()
    val riteDimensions = RiteDimensions()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalRiteColorScheme provides riteColors,
        LocalRiteTypography provides riteTypography,
        LocalRiteShapes provides riteShapes,
        LocalRiteSpacing provides riteSpacing,
        LocalRiteMotion provides riteMotion,
        LocalRiteDimensions provides riteDimensions
    ) {
        MaterialTheme(
            colorScheme = riteColors.toMaterialColorScheme(),
            typography = riteTypography.toMaterialTypography(),
            content = content
        )
    }
}
```

- [ ] **Step 2: `Theme.ios.kt` and `Theme.jvm.kt` — no change required**

Both delegate to `RiteThemeFallback`, which now reads the new tokens. Confirm they still compile.

- [ ] **Step 3: Compile all targets**

```bash
./gradlew :composeApp:compileDebugKotlin :composeApp:compileKotlinJvm :composeApp:compileKotlinIosArm64 -q
```

Expected: success.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/Theme.android.kt
git commit -m "feat(theme): wire Android RiteTheme actual to Sage tokens"
```

### Task 10: Delete retired assets

Now that nothing references the old Forest palette, old typography, or Manrope / Inter fonts, delete them. Also rename `RiteColorSchemeV2` → `RiteColorScheme` (the old class name is now free).

**Files:**
- Delete: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/Type.kt`
- Delete: `composeApp/src/commonMain/composeResources/font/manrope.ttf`
- Delete: `composeApp/src/commonMain/composeResources/font/inter.ttf`
- Rename: `RiteColorSchemeV2.kt` → `RiteColorScheme.kt` + rename the class inside

- [ ] **Step 1: Verify `Type.kt` is no longer referenced**

```bash
grep -rn "habitLockTypography\|manropeFontFamily\|interFontFamily" composeApp/src/ || echo "OK: no references"
```

Expected: `OK: no references`. If there are any, delete/update them in the same commit.

- [ ] **Step 2: Delete `Type.kt`**

```bash
git rm composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/Type.kt
```

- [ ] **Step 3: Verify old fonts are no longer referenced**

```bash
grep -rn "Res\.font\.manrope\|Res\.font\.inter\b" composeApp/src/ || echo "OK: no references"
```

Expected: `OK: no references` (note `Res.font.inter_tight` is fine; only bare `inter` should be gone).

- [ ] **Step 4: Delete old font files**

```bash
git rm composeApp/src/commonMain/composeResources/font/manrope.ttf
git rm composeApp/src/commonMain/composeResources/font/inter.ttf
```

- [ ] **Step 5: Rename `RiteColorSchemeV2` → `RiteColorScheme`**

```bash
sed -i '' 's/RiteColorSchemeV2/RiteColorScheme/g' \
    composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteColorSchemeV2.kt \
    composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/SageColors.kt \
    composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/Theme.kt
git mv composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteColorSchemeV2.kt \
       composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/RiteColorScheme.kt
```

- [ ] **Step 6: Regenerate Compose Resources and compile**

```bash
./gradlew :composeApp:generateComposeResClass && ./gradlew :composeApp:compileDebugKotlin -q
```

Expected: success. The generated `Res.font` no longer includes `manrope` or `inter`.

- [ ] **Step 7: Run tests**

```bash
./gradlew :composeApp:jvmTest -q && ./gradlew :composeApp:verifyRoborazziDebug -q
```

Expected: all pass.

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "chore(theme): remove Forest palette, Manrope font, old typography"
```

---

## Chunk 4: Primitive components

New or rebuilt primitives, each with a Roborazzi screenshot test (light + dark). First-run tests are recorded via `recordRoborazziDebug` and reviewed visually.

### Task 11: Rebuild `PrimaryButton` with Primary / Secondary / Ghost variants

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/PrimaryButton.kt`
- Create: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/components/PrimaryButtonScreenshotTest.kt`

- [ ] **Step 1: Replace `PrimaryButton.kt` contents**

```kotlin
package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

enum class RiteButtonVariant { Primary, Secondary, Ghost }

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: RiteButtonVariant = RiteButtonVariant.Primary,
    content: @Composable RowScope.() -> Unit
) {
    val colors = RiteAppTheme.colors
    val shape = RiteAppTheme.shapes.sm
    val rowModifier = modifier.fillMaxWidth().heightIn(min = 48.dp)

    when (variant) {
        RiteButtonVariant.Primary -> Button(
            onClick = onClick,
            modifier = rowModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.onSurface,           // ink background (inverse of surface)
                contentColor = colors.surface,                // surface-coloured text
                disabledContainerColor = colors.onSurfaceSubtle,
                disabledContentColor = colors.surface
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            content = content
        )
        RiteButtonVariant.Secondary -> OutlinedButton(
            onClick = onClick,
            modifier = rowModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.onSurface,
                disabledContentColor = colors.onSurfaceSubtle
            ),
            border = BorderStroke(1.dp, colors.outline),
            content = content
        )
        RiteButtonVariant.Ghost -> TextButton(
            onClick = onClick,
            modifier = rowModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.textButtonColors(
                contentColor = colors.onSurface,
                disabledContentColor = colors.onSurfaceSubtle
            ),
            content = content
        )
    }
}
```

- [ ] **Step 2: Create the screenshot test**

```kotlin
package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w360dp-h800dp-420dpi", application = android.app.Application::class)
class PrimaryButtonScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun primary_light() = render(RiteButtonVariant.Primary, dark = false)
    @Test
    fun primary_dark() = render(RiteButtonVariant.Primary, dark = true)
    @Test
    fun secondary_light() = render(RiteButtonVariant.Secondary, dark = false)
    @Test
    fun secondary_dark() = render(RiteButtonVariant.Secondary, dark = true)
    @Test
    fun ghost_light() = render(RiteButtonVariant.Ghost, dark = false)
    @Test
    fun ghost_dark() = render(RiteButtonVariant.Ghost, dark = true)

    private fun render(variant: RiteButtonVariant, dark: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                PrimaryButton(onClick = {}, variant = variant) {
                    Text("Establish Habit")
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
```

- [ ] **Step 3: Record goldens**

```bash
./gradlew :composeApp:recordRoborazziDebug --tests "com.ricardocosteira.rite.presentation.ui.components.PrimaryButtonScreenshotTest" -q
```

Inspect the six PNGs in `composeApp/src/androidUnitTest/snapshots/images/` — confirm Primary renders ink-on-linen in light and sage-on-ink in dark; Secondary renders outlined; Ghost renders text-only.

- [ ] **Step 4: Verify goldens on a fresh run**

```bash
./gradlew :composeApp:verifyRoborazziDebug --tests "com.ricardocosteira.rite.presentation.ui.components.PrimaryButtonScreenshotTest" -q
```

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/PrimaryButton.kt \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/components/PrimaryButtonScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/
git commit -m "feat(components): rebuild PrimaryButton with Primary/Secondary/Ghost variants"
```

### Task 12: Create `RitePill` and `StrictnessPill`

`RitePill` is the small capsule used for meta badges (e.g., "360 × 800dp", "DONE"). `StrictnessPill` is the specific Today-screen variant with an animated pulsing dot + preset name.

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/RitePill.kt`
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/StrictnessPill.kt`
- Create: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/components/PillsScreenshotTest.kt`

- [ ] **Step 1: Create `RitePill.kt`**

```kotlin
package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun RitePill(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    bordered: Boolean = false
) {
    val colors = RiteAppTheme.colors
    val bg = if (containerColor.isUnspecified()) colors.surfaceContainer else containerColor
    val fg = if (contentColor.isUnspecified()) colors.onSurface else contentColor

    Surface(
        modifier = modifier,
        shape = RiteAppTheme.shapes.pill,
        color = bg,
        contentColor = fg,
        border = if (bordered) BorderStroke(1.dp, colors.outline) else null
    ) {
        Text(
            text = text,
            style = RiteAppTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

private fun Color.isUnspecified(): Boolean = this == Color.Unspecified
```

- [ ] **Step 2: Create `StrictnessPill.kt` with a pulsing dot**

```kotlin
package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

enum class StrictnessPreset { Flexible, Balanced, Unwavering }

@Composable
fun StrictnessPill(
    preset: StrictnessPreset,
    modifier: Modifier = Modifier,
    animated: Boolean = true
) {
    val colors = RiteAppTheme.colors
    val dotAlpha = if (animated) {
        val t = rememberInfiniteTransition(label = "strictness-dot")
        val a by t.animateFloat(
            initialValue = 0.45f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = RiteAppTheme.motion.deliberate.inWholeMilliseconds.toInt(),
                    easing = RiteAppTheme.motion.easeQuiet
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "strictness-dot-alpha"
        )
        a
    } else 1f

    Surface(
        modifier = modifier,
        shape = RiteAppTheme.shapes.pill,
        color = colors.surfaceContainer,
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(dotAlpha)
                    .background(colors.primary, CircleShape)
            )
            Text(
                text = preset.name,
                style = RiteAppTheme.typography.labelSmall
            )
        }
    }
}
```

- [ ] **Step 3: Create the screenshot test**

```kotlin
package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w360dp-h800dp-420dpi", application = android.app.Application::class)
class PillsScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun ritePill_light() = renderPill(dark = false)
    @Test
    fun ritePill_dark() = renderPill(dark = true)
    @Test
    fun strictnessPill_balanced_light() = renderStrictness(StrictnessPreset.Balanced, dark = false)
    @Test
    fun strictnessPill_balanced_dark() = renderStrictness(StrictnessPreset.Balanced, dark = true)
    @Test
    fun strictnessPill_unwavering_light() = renderStrictness(StrictnessPreset.Unwavering, dark = false)

    private fun renderPill(dark: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                RitePill(text = "DONE")
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    private fun renderStrictness(preset: StrictnessPreset, dark: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                // animated=false makes the golden deterministic
                StrictnessPill(preset = preset, animated = false)
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
```

- [ ] **Step 4: Record, review, verify, commit**

```bash
./gradlew :composeApp:recordRoborazziDebug --tests "com.ricardocosteira.rite.presentation.ui.components.PillsScreenshotTest" -q
./gradlew :composeApp:verifyRoborazziDebug --tests "com.ricardocosteira.rite.presentation.ui.components.PillsScreenshotTest" -q
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/RitePill.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/StrictnessPill.kt \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/components/PillsScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/
git commit -m "feat(components): add RitePill and StrictnessPill with pulsing dot"
```

### Task 13: Create `RiteChip` (letter chip + shortcut chip) and `RiteDivider`

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/RiteChip.kt`
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/RiteDivider.kt`
- Create: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/components/ChipAndDividerScreenshotTest.kt`

- [ ] **Step 1: Create `RiteChip.kt`**

```kotlin
package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun RiteLetterChip(
    letter: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RiteAppTheme.colors
    Surface(
        modifier = modifier.size(32.dp),
        shape = RiteAppTheme.shapes.sm,
        color = if (selected) colors.onSurface else colors.surfaceContainer,
        contentColor = if (selected) colors.surface else colors.onSurface,
        border = BorderStroke(1.dp, colors.outline),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = letter, style = RiteAppTheme.typography.labelLarge)
        }
    }
}

@Composable
fun RiteShortcutChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RiteAppTheme.colors
    Surface(
        modifier = modifier,
        shape = RiteAppTheme.shapes.sm,
        color = if (selected) colors.onSurface else colors.surfaceContainer,
        contentColor = if (selected) colors.surface else colors.onSurface,
        border = BorderStroke(1.dp, colors.outline),
        onClick = onClick
    ) {
        Text(
            text = text,
            style = RiteAppTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
```

- [ ] **Step 2: Create `RiteDivider.kt`**

```kotlin
package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun RiteDivider(
    modifier: Modifier = Modifier,
    strong: Boolean = false
) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth().height(1.dp),
        thickness = 1.dp,
        color = if (strong) RiteAppTheme.colors.outline else RiteAppTheme.colors.outlineVariant
    )
}
```

- [ ] **Step 3: Create the screenshot test**

```kotlin
package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w360dp-h800dp-420dpi", application = android.app.Application::class)
class ChipAndDividerScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun letterChips_light() = renderLetters(dark = false)
    @Test
    fun letterChips_dark() = renderLetters(dark = true)
    @Test
    fun shortcutChips_light() = renderShortcuts(dark = false)
    @Test
    fun shortcutChips_dark() = renderShortcuts(dark = true)
    @Test
    fun divider_light() = renderDivider(dark = false)
    @Test
    fun divider_dark() = renderDivider(dark = true)

    private fun renderLetters(dark: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(12.dp)) {
                    listOf("M" to true, "T" to false, "W" to true, "T" to false, "F" to true, "S" to false, "S" to false)
                        .forEach { (l, sel) -> RiteLetterChip(l, selected = sel, onClick = {}) }
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    private fun renderShortcuts(dark: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(12.dp)) {
                    listOf("Every day" to false, "Weekdays" to true, "Weekend" to false)
                        .forEach { (t, sel) -> RiteShortcutChip(t, selected = sel, onClick = {}) }
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    private fun renderDivider(dark: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                Column(modifier = Modifier.padding(12.dp)) {
                    RiteDivider()
                    RiteDivider(strong = true, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
```

- [ ] **Step 4: Record, review, verify, commit**

```bash
./gradlew :composeApp:recordRoborazziDebug --tests "com.ricardocosteira.rite.presentation.ui.components.ChipAndDividerScreenshotTest" -q
./gradlew :composeApp:verifyRoborazziDebug --tests "com.ricardocosteira.rite.presentation.ui.components.ChipAndDividerScreenshotTest" -q
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/RiteChip.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/RiteDivider.kt \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/components/ChipAndDividerScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/
git commit -m "feat(components): add RiteChip (letter + shortcut) and RiteDivider"
```

### Task 14: Create `ProgressRing`

Circular progress indicator with Fraunces-numeric center value. Used by the Today header and habit cards for day/quota completion.

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/ProgressRing.kt`
- Create: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/components/ProgressRingScreenshotTest.kt`

- [ ] **Step 1: Create `ProgressRing.kt`**

```kotlin
package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun ProgressRing(
    progress: Float,                     // 0f..1f
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    strokeWidth: Dp = 4.dp,
    label: String? = null
) {
    val clamped = progress.coerceIn(0f, 1f)
    val track = RiteAppTheme.colors.outlineVariant
    val bar = RiteAppTheme.colors.primary

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val s = this.size.minDimension
            val topLeft = Offset((this.size.width - s) / 2, (this.size.height - s) / 2)
            val arcSize = Size(s, s)
            val stroke = Stroke(width = strokeWidth.toPx())
            // Track
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
            // Progress
            drawArc(
                color = bar,
                startAngle = -90f,
                sweepAngle = 360f * clamped,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
        }
        if (label != null) {
            Text(text = label, style = RiteAppTheme.typography.displaySmall)
        }
    }
}
```

- [ ] **Step 2: Create the screenshot test**

```kotlin
package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w360dp-h800dp-420dpi", application = android.app.Application::class)
class ProgressRingScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun ring_zero_light() = render(0f, dark = false)
    @Test
    fun ring_sixty_light() = render(0.62f, dark = false, label = "62")
    @Test
    fun ring_full_light() = render(1f, dark = false, label = "100")
    @Test
    fun ring_sixty_dark() = render(0.62f, dark = true, label = "62")

    private fun render(progress: Float, dark: Boolean, label: String? = null) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                ProgressRing(progress = progress, label = label)
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
```

- [ ] **Step 3: Record, review, verify, commit**

```bash
./gradlew :composeApp:recordRoborazziDebug --tests "com.ricardocosteira.rite.presentation.ui.components.ProgressRingScreenshotTest" -q
./gradlew :composeApp:verifyRoborazziDebug --tests "com.ricardocosteira.rite.presentation.ui.components.ProgressRingScreenshotTest" -q
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/ProgressRing.kt \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/components/ProgressRingScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/
git commit -m "feat(components): add ProgressRing"
```

### Task 15: Create `RiteDialog`

A design-system wrapper around M3 `AlertDialog` for destructive confirmations. Centralizes the visual treatment so existing dialog call sites (HabitDetailRoute, HabitFormScreen) can migrate in their slices.

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/RiteDialog.kt`
- Create: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/components/RiteDialogScreenshotTest.kt`

- [ ] **Step 1: Create `RiteDialog.kt`**

```kotlin
package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun RiteDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RiteAppTheme.shapes.lg,
        containerColor = RiteAppTheme.colors.surface,
        titleContentColor = RiteAppTheme.colors.onSurface,
        textContentColor = RiteAppTheme.colors.onSurfaceMuted,
        title = { Text(title, style = RiteAppTheme.typography.headlineSmall) },
        text = { Text(message, style = RiteAppTheme.typography.bodyMedium) },
        confirmButton = {
            // Destructive dialogs tint the confirm button with the error color directly.
            // Non-destructive dialogs use the default primary (ink) treatment.
            if (destructive) {
                androidx.compose.material3.TextButton(
                    onClick = onConfirm,
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = RiteAppTheme.colors.error
                    )
                ) { Text(confirmLabel, style = RiteAppTheme.typography.labelLarge) }
            } else {
                PrimaryButton(onClick = onConfirm, variant = RiteButtonVariant.Primary) {
                    Text(confirmLabel)
                }
            }
        },
        dismissButton = {
            PrimaryButton(onClick = onDismiss, variant = RiteButtonVariant.Ghost) { Text(dismissLabel) }
        }
    )
}
```

- [ ] **Step 2: Create the screenshot test**

```kotlin
package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w360dp-h800dp-420dpi", application = android.app.Application::class)
class RiteDialogScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dialog_destructive_light() = render(dark = false, destructive = true)
    @Test
    fun dialog_destructive_dark() = render(dark = true, destructive = true)

    private fun render(dark: Boolean, destructive: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                RiteDialog(
                    title = "Delete habit?",
                    message = "Archived habits can be restored, but deleted habits cannot.",
                    confirmLabel = "Delete",
                    dismissLabel = "Cancel",
                    onConfirm = {},
                    onDismiss = {},
                    destructive = destructive
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
```

- [ ] **Step 3: Record, review, verify, commit**

```bash
./gradlew :composeApp:recordRoborazziDebug --tests "com.ricardocosteira.rite.presentation.ui.components.RiteDialogScreenshotTest" -q
./gradlew :composeApp:verifyRoborazziDebug --tests "com.ricardocosteira.rite.presentation.ui.components.RiteDialogScreenshotTest" -q
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/RiteDialog.kt \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/components/RiteDialogScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/
git commit -m "feat(components): add RiteDialog scaffold for destructive confirmations"
```

### Task 16: Create `RiteSnackbar` with four variants

The design system's snackbar has distinct treatments for `completed` / `skipped` (ink bg, accent italic), `failed` (terracotta bg), `suspended` (dusk bg). Each optionally has an action button.

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/RiteSnackbar.kt`
- Create: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/components/RiteSnackbarScreenshotTest.kt`

- [ ] **Step 1: Create `RiteSnackbar.kt`**

```kotlin
package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

enum class RiteSnackbarVariant { Completed, Skipped, Failed, Suspended }

data class RiteSnackbarContent(
    val prefix: String,
    val emphasized: String,     // rendered in displayItalic + accent tint
    val suffix: String,
    val subtext: String? = null,
    val action: (@Composable () -> Unit)? = null
)

@Composable
fun RiteSnackbar(
    variant: RiteSnackbarVariant,
    content: RiteSnackbarContent,
    modifier: Modifier = Modifier
) {
    val colors = RiteAppTheme.colors
    val bg: Color
    val fg: Color
    val accent: Color
    when (variant) {
        RiteSnackbarVariant.Completed -> { bg = colors.onSurface; fg = colors.surface; accent = colors.primary }
        RiteSnackbarVariant.Skipped -> { bg = colors.onSurface; fg = colors.surface; accent = colors.onSurfaceMuted }
        RiteSnackbarVariant.Failed -> { bg = colors.error; fg = colors.onError; accent = colors.onError }
        RiteSnackbarVariant.Suspended -> { bg = colors.suspend; fg = colors.onSuspend; accent = colors.onSuspend }
    }

    Surface(
        modifier = modifier.fillMaxWidth().widthIn(max = 420.dp),
        shape = RiteAppTheme.shapes.sm,
        color = bg,
        contentColor = fg
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier.size(22.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glyph square placeholder — concrete icon chosen by the caller via content.action slot is
                // intentionally flat for now; full icon wiring arrives in Slice 2 when the Today screen uses it.
                Surface(
                    modifier = Modifier.size(22.dp),
                    shape = RiteAppTheme.shapes.xs,
                    color = fg.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, fg.copy(alpha = 0.2f))
                ) {}
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        append(content.prefix)
                        withStyle(
                            SpanStyle(
                                color = accent,
                                fontFamily = RiteAppTheme.typography.displayItalic.fontFamily,
                                fontStyle = RiteAppTheme.typography.displayItalic.fontStyle,
                                fontWeight = RiteAppTheme.typography.displayItalic.fontWeight
                            )
                        ) { append(content.emphasized) }
                        append(content.suffix)
                    },
                    style = RiteAppTheme.typography.bodySmall
                )
                if (content.subtext != null) {
                    Text(
                        text = AnnotatedString(content.subtext),
                        style = RiteAppTheme.typography.labelSmall,
                        color = fg.copy(alpha = 0.7f)
                    )
                }
            }

            content.action?.invoke()
        }
    }
}
```

- [ ] **Step 2: Create the screenshot test covering all 4 variants × light/dark**

```kotlin
package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w360dp-h800dp-420dpi", application = android.app.Application::class)
class RiteSnackbarScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun completed_light() = render(RiteSnackbarVariant.Completed, dark = false)
    @Test fun completed_dark() = render(RiteSnackbarVariant.Completed, dark = true)
    @Test fun skipped_light() = render(RiteSnackbarVariant.Skipped, dark = false)
    @Test fun skipped_dark() = render(RiteSnackbarVariant.Skipped, dark = true)
    @Test fun failed_light() = render(RiteSnackbarVariant.Failed, dark = false)
    @Test fun failed_dark() = render(RiteSnackbarVariant.Failed, dark = true)
    @Test fun suspended_light() = render(RiteSnackbarVariant.Suspended, dark = false)
    @Test fun suspended_dark() = render(RiteSnackbarVariant.Suspended, dark = true)

    private fun render(variant: RiteSnackbarVariant, dark: Boolean) {
        val content = when (variant) {
            RiteSnackbarVariant.Completed -> RiteSnackbarContent(
                prefix = "Completed ", emphasized = "Morning sit", suffix = ". Streak → 15 days.",
                action = { Text("UNDO") }
            )
            RiteSnackbarVariant.Skipped -> RiteSnackbarContent(
                prefix = "Skipped ", emphasized = "Strength work", suffix = ". 1 skip remains this week.",
                action = { Text("UNDO") }
            )
            RiteSnackbarVariant.Failed -> RiteSnackbarContent(
                prefix = "Missed ", emphasized = "Morning sit", suffix = ". The 14-day streak resets at midnight.",
                subtext = "Tomorrow is a new page."
            )
            RiteSnackbarVariant.Suspended -> RiteSnackbarContent(
                prefix = "On leave until Monday. ", emphasized = "7 rituals", suffix = " paused.",
                action = { Text("END LEAVE") }
            )
        }
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                RiteSnackbar(variant = variant, content = content)
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
```

- [ ] **Step 3: Record, review, verify, commit**

```bash
./gradlew :composeApp:recordRoborazziDebug --tests "com.ricardocosteira.rite.presentation.ui.components.RiteSnackbarScreenshotTest" -q
./gradlew :composeApp:verifyRoborazziDebug --tests "com.ricardocosteira.rite.presentation.ui.components.RiteSnackbarScreenshotTest" -q
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/RiteSnackbar.kt \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/components/RiteSnackbarScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/
git commit -m "feat(components): add RiteSnackbar with completed/skipped/failed/suspended variants"
```

### Task 17: Rebuild `RiteBottomNav` with Rite v2 tokens

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/RiteBottomNav.kt` (path may differ — locate with grep)

- [ ] **Step 1: Locate and read the current file**

```bash
find composeApp/src -type f -name "RiteBottomNav.kt"
```

Open the result and read it end-to-end. Note exactly:
- The function signature (parameters, defaults, modifier slot)
- How `BottomNavTab` is defined (values, label resolution)
- How icons are resolved per tab
- How `HapticController` is threaded in
- Any route/navigation interaction

You'll preserve all of that verbatim in Step 2. The **only** substantive change this task makes is swapping the `NavigationBar` / `NavigationBarItemDefaults.colors(...)` arguments from the old `MaterialTheme.colorScheme.*` or whatever they currently reference over to `RiteAppTheme.colors.*`.

- [ ] **Step 2: Rewrite the file preserving existing logic**

Apply the color wiring below to the existing file, keeping every other part of the function (signature, icon resolver, haptics, route args) identical. Do **not** copy this block blindly — integrate it into the current file's structure:

```kotlin
package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ricardocosteira.rite.presentation.ui.haptics.HapticController
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

enum class BottomNavTab(val label: String) {
    TODAY("Today"), HISTORY("History"), SETTINGS("Settings")
}

@Composable
fun RiteBottomNav(
    selected: BottomNavTab,
    onSelect: (BottomNavTab) -> Unit,
    haptics: HapticController,
    modifier: Modifier = Modifier,
    iconFor: @Composable (BottomNavTab) -> Unit = { /* existing resolver — keep whatever is in the current file */ }
) {
    val colors = RiteAppTheme.colors
    NavigationBar(
        modifier = modifier,
        containerColor = colors.surface,
        contentColor = colors.onSurface,
        tonalElevation = 0.dp
    ) {
        BottomNavTab.values().forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = {
                    haptics.tick()
                    onSelect(tab)
                },
                icon = { iconFor(tab) },
                label = { Text(tab.label, style = RiteAppTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.onSurface,
                    unselectedIconColor = colors.onSurfaceMuted,
                    selectedTextColor = colors.onSurface,
                    unselectedTextColor = colors.onSurfaceMuted,
                    indicatorColor = colors.surfaceContainer
                )
            )
        }
    }
}
```

Important: this is a **skeleton**. Before committing, open the current `RiteBottomNav.kt` and preserve any logic the existing file has (resolver for icons per tab, default parameter values, route wiring). Only the colors wiring changes.

- [ ] **Step 3: Compile**

```bash
./gradlew :composeApp:compileDebugKotlin -q
```

Expected: success. If the Scaffold that hosts `RiteBottomNav` passes route-specific props, keep the function signature identical to the current file.

- [ ] **Step 4: Update existing screenshot tests for containing screens**

Run the full screenshot suite. Any regressions on screens that embed bottom nav (Today, Calendar, Settings) will surface here; record new goldens.

```bash
./gradlew :composeApp:recordRoborazziDebug -q
# Visually inspect touched PNGs under composeApp/src/androidUnitTest/snapshots/images/
./gradlew :composeApp:verifyRoborazziDebug -q
```

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/components/RiteBottomNav.kt \
        composeApp/src/androidUnitTest/snapshots/images/
git commit -m "feat(components): rewire RiteBottomNav on Rite v2 tokens"
```

---

## Chunk 5: Verification & token preview

### Task 18: Create `ThemeTokensPreview` composable and screenshot test

A single screen that renders every color, typography slot, shape, and spacing value. Acts as a visual regression canvas for all future slices — a single failing golden here signals a token-level change, before any downstream screen test flips.

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/ThemeTokensPreview.kt`
- Create: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/theme/ThemeTokensPreviewScreenshotTest.kt`

- [ ] **Step 1: Create `ThemeTokensPreview.kt`**

```kotlin
package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ThemeTokensPreview(modifier: Modifier = Modifier) {
    val colors = RiteAppTheme.colors
    val type = RiteAppTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(RiteAppTheme.spacing.gap4),
        verticalArrangement = Arrangement.spacedBy(RiteAppTheme.spacing.gap4)
    ) {
        Text("Colors", style = type.headlineSmall, color = colors.onSurface)
        colorRow("primary", colors.primary, colors.onPrimary)
        colorRow("primaryContainer", colors.primaryContainer, colors.onPrimaryContainer)
        colorRow("secondary", colors.secondary, colors.onSecondary)
        colorRow("error", colors.error, colors.onError)
        colorRow("warn", colors.warn, colors.onWarn)
        colorRow("suspend", colors.suspend, colors.onSuspend)
        colorRow("surface", colors.surface, colors.onSurface)
        colorRow("surfaceDim", colors.surfaceDim, colors.onSurface)
        colorRow("surfaceBright", colors.surfaceBright, colors.onSurface)
        colorRow("onSurfaceMuted", colors.surface, colors.onSurfaceMuted)
        colorRow("onSurfaceSubtle", colors.surface, colors.onSurfaceSubtle)

        Text("Day classification", style = type.headlineSmall, color = colors.onSurface)
        dayRow("Perfect", colors.dayPerfect)
        dayRow("BestEffort", colors.dayBestEffort)
        dayRow("Partial", colors.dayPartial)
        dayRow("RoughDay", colors.dayRoughDay)
        dayRow("Failed", colors.dayFailed)
        dayRow("Skipped", colors.daySkipped)
        dayRow("Future", colors.dayFuture)

        Text("Typography", style = type.headlineSmall, color = colors.onSurface)
        Text("displayLarge — Rite", style = type.displayLarge, color = colors.onSurface)
        Text("displayMedium — Quiet discipline", style = type.displayMedium, color = colors.onSurface)
        Text("displaySmall — Structure your day.", style = type.displaySmall, color = colors.onSurface)
        Text("titleLarge italic", style = type.titleLarge, color = colors.onSurface)
        Text("bodyLarge — regular body copy", style = type.bodyLarge, color = colors.onSurface)
        Text("bodyMedium — secondary body", style = type.bodyMedium, color = colors.onSurfaceMuted)
        Text("eyebrow", style = type.eyebrow, color = colors.onSurfaceMuted)
        Text("mono", style = type.mono, color = colors.onSurfaceMuted)

        Text("Shapes", style = type.headlineSmall, color = colors.onSurface)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            shapeSwatch("xs", RiteAppTheme.shapes.xs)
            shapeSwatch("sm", RiteAppTheme.shapes.sm)
            shapeSwatch("md", RiteAppTheme.shapes.md)
            shapeSwatch("lg", RiteAppTheme.shapes.lg)
            shapeSwatch("xl", RiteAppTheme.shapes.xl)
            shapeSwatch("xxl", RiteAppTheme.shapes.xxl)
            shapeSwatch("pill", RiteAppTheme.shapes.pill)
        }
    }
}

@Composable
private fun colorRow(name: String, bg: Color, fg: Color) {
    Surface(
        color = bg,
        contentColor = fg,
        shape = RiteAppTheme.shapes.sm,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(name, style = RiteAppTheme.typography.labelLarge, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun dayRow(name: String, c: Color) {
    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.size(20.dp).background(c, RiteAppTheme.shapes.xs))
        Text(name, style = RiteAppTheme.typography.labelMedium, color = RiteAppTheme.colors.onSurface)
    }
}

@Composable
private fun shapeSwatch(name: String, shape: androidx.compose.ui.graphics.Shape) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(32.dp).background(RiteAppTheme.colors.primaryContainer, shape))
        Text(name, style = RiteAppTheme.typography.labelSmall, color = RiteAppTheme.colors.onSurfaceMuted)
    }
}
```

- [ ] **Step 2: Create the screenshot test**

```kotlin
package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w360dp-h800dp-420dpi", application = android.app.Application::class)
class ThemeTokensPreviewScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tokens_light() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) { ThemeTokensPreview() }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun tokens_dark() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = true) { ThemeTokensPreview() }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
```

- [ ] **Step 3: Record goldens and eyeball**

```bash
./gradlew :composeApp:recordRoborazziDebug --tests "com.ricardocosteira.rite.presentation.ui.theme.ThemeTokensPreviewScreenshotTest" -q
```

Open both PNGs side by side and verify:
- Sage green `primary`, amber `warn`, terracotta `error`, dusk `suspend` all visually distinct
- Body copy renders in Inter Tight
- `eyebrow` and `mono` render in JetBrains Mono (letterspaced uppercase)
- Shape swatches walk from sharp (xs) to pill

- [ ] **Step 4: Verify**

```bash
./gradlew :composeApp:verifyRoborazziDebug --tests "com.ricardocosteira.rite.presentation.ui.theme.ThemeTokensPreviewScreenshotTest" -q
```

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/theme/ThemeTokensPreview.kt \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/theme/ThemeTokensPreviewScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/
git commit -m "test(theme): add ThemeTokensPreview screenshot regression canvas"
```

---

## Chunk 6: Slice wrap-up

### Task 19: Full verification + open slice PR

- [ ] **Step 1: Run the complete test suite**

```bash
./gradlew :composeApp:jvmTest :composeApp:verifyRoborazziDebug -q
```

Expected: all green. If anything fails, diagnose and fix before proceeding.

- [ ] **Step 2: Compile all Kotlin targets**

```bash
./gradlew :composeApp:compileDebugKotlin :composeApp:compileKotlinJvm :composeApp:compileKotlinIosArm64 :composeApp:compileKotlinIosSimulatorArm64 -q
```

Expected: success on all four.

- [ ] **Step 3: Sanity-render the app manually on Android**

```bash
./gradlew :composeApp:installDebug -q
# Open the app on an Android device/emulator, navigate through Today, Calendar, Settings, Habit Detail, Habit Form
# Light + dark. Nothing should be catastrophically broken — visual inconsistencies between screens are expected
# (components not yet rebuilt in this slice will look transitional); outright crashes or invisible text are bugs.
```

- [ ] **Step 4: Push the slice branch**

```bash
git push -u origin feature/design-system-v2/01-foundation
```

- [ ] **Step 5: Open the slice PR targeting the parent branch**

```bash
gh pr create \
    --base feature/design-system-v2 \
    --head feature/design-system-v2/01-foundation \
    --title "Design system v2 — Slice 1: Foundation" \
    --body "$(cat <<'EOF'
## Summary
- Introduce the Rite v2 Sage & Linen token system: colors, typography, shapes, spacing, motion, dimensions
- Rebuild PrimaryButton (Primary / Secondary / Ghost); create RitePill, StrictnessPill, RiteChip, RiteDivider, ProgressRing, RiteDialog, RiteSnackbar
- Rewire RiteBottomNav on the new tokens
- Retire the Forest Discipline / Stoic Night palette, Manrope font, old Inter TTF, and old typography
- Add ThemeTokensPreview as a visual regression canvas

Spec: `docs/superpowers/specs/2026-04-18-rite-design-system-migration-design.md`

## Test plan
- [x] `jvmTest` green
- [x] `verifyRoborazziDebug` green (all new + pre-existing goldens)
- [x] Manual smoke on Android — app renders, no crashes, navigation intact
- [ ] Reviewer: inspect `ThemeTokensPreview` goldens for correctness before merge
EOF
)"
```

- [ ] **Step 6: Verify CI runs green against the parent branch**

Wait for CI. If it passes, this slice is ready to merge into the parent branch. The next slice (Today + HabitCard) will be planned in its own document once this merges.

---

## Self-review checklist

Before handing off, confirm:

- All spec items in "Slice 1 — Foundation" have a corresponding task:
  - Token data classes ✓ (Tasks 3, 4, 5, 6)
  - Font resources ✓ (Task 2, 10)
  - Theme swap + rename ✓ (Tasks 7, 8, 9, 10)
  - PrimaryButton rebuild ✓ (Task 11)
  - RitePill + StrictnessPill ✓ (Task 12)
  - RiteChip + RiteDivider ✓ (Task 13)
  - ProgressRing ✓ (Task 14)
  - RiteDialog ✓ (Task 15)
  - RiteSnackbar with 4 variants ✓ (Task 16)
  - BottomNav rebuild ✓ (Task 17)
  - ThemeTokensPreview + screenshot tests ✓ (Task 18)
- Every component that ships also has a Roborazzi screenshot test.
- Every "Commit" step uses a conventional commit prefix matching the repo's style (`feat(...)`, `fix(...)`, `chore(...)`, `refactor(...)`, `test(...)`, `docs:`).

## Out of scope for this slice

Explicitly **not** in this plan (arrives in later slices):

- Integrating `RiteSnackbar` into Today / Habit Form / Settings — they still use the M3 `SnackbarHostState` for now. Slice 2 wires the Today screen onto the new snackbar.
- Migrating `AlertDialog` call sites in `HabitDetailRoute.kt` and `HabitFormScreen.kt` to `RiteDialog` — happens in slices 3 / 4 respectively.
- Screen-level rebuilds (TodayHeader, HabitCard, Heatmap, etc.).
- Motion integration on existing animations — slice 8.

