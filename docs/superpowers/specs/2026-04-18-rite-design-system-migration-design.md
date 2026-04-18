# Rite Design System Migration

## Problem

Rite's current UI is built on the "Forest Discipline / Stoic Night" palette with Manrope + Inter fonts, defined inline in `Theme.kt` and `Type.kt`. A new design system ("Rite v2 — Sage & Linen") has been mocked up in HTML/CSS/JS and handed off for implementation. It defines a full editorial visual language — new palette, new typography, new shape and spacing scales, motion tokens, and a complete component library — that the existing theme and components must be replaced with.

## Solution

Full replacement. The old design system is retired in favour of Rite v2. Work lands under a long-lived parent branch, split into eight slice PRs (each a coherent chunk of the app end-to-end), and merges into `develop` as a single integration PR at the end.

## Scope

### In scope

- Replace all color, typography, shape, spacing, and motion tokens with the Rite v2 system.
- Rebuild or re-skin every existing UI component and screen so visuals match the new system.
- Ship new components the system requires: progress ring, strictness pill (pulsing dot), heatmap, stat tile, category eyebrow, day-of-week picker, reminder configurator, track toggle, progress-step indicator, timezone banner, strictness accordion, four snackbar variants, calendar day cell (7 states).
- Reorder onboarding steps to match the design: Philosophy → Strictness → **First Habit** → **Notification Permission** (last two currently swapped).
- Delete the Forest Discipline palette and the Manrope font.
- Add Fraunces, Inter Tight, and JetBrains Mono as Compose font resources.

### Out of scope / deferred

- **Other palettes** (Ink & Stone, Moss & Slate). Ship Sage only. Token structure leaves room for a future `Palette` enum.
- **Runtime density selector.** `density-scale` from the CSS collapses to a fixed 1.0 for now.
- **Suspension-creation UI.** The design system defines a `suspended` card status variant and a `suspend` color token — we ship those. The flow for entering leave mode is out of scope per the brief (habit-suspension logic isn't implemented yet).
- **Navigation / information architecture changes** beyond the onboarding step reorder.
- **Copy changes** beyond what the design system mandates (e.g., the `"🎉"` emoji removal in the completion snackbar — the design system specifies its own snackbar copy/structure, and we adopt it verbatim).
- **`HabitFormScreen → TodayViewModel` nav-host coupling** (known pain point in `CLAUDE.md`) — stays deferred.
- **The untracked `COMPOSE-AUDIT-REPORT.md`** — superseded by this migration since every component is changing anyway.

## Goals

- Every screen in the app renders with Rite v2 tokens, in light and dark.
- Material 3 primitives (`Button`, `Card`, `TextField`, `Switch`, `RadioButton`) keep working via re-skinning wherever possible. The snackbar is a deliberate exception — the design system's four snackbar variants differ enough in structure that they're built as a custom component.
- One canonical theme access point: `RiteAppTheme.colors`, `RiteAppTheme.typography`, `RiteAppTheme.shapes`, `RiteAppTheme.spacing`, `RiteAppTheme.motion`.
- Every component exposes a Compose preview; every screen has a screenshot test at 360×800dp in both modes.

## Non-goals

- Adding features. This is a visual rebuild.
- Refactoring domain logic. Tests such as `ProcessEndOfDayTest` should not change.
- Supporting multiple palettes at runtime.
- Runtime theme editing / tweaks panel (present in the design prototype, not needed in the app).

## Theme architecture

Follows the Compose [anatomy of a theme](https://developer.android.com/develop/ui/compose/designsystems/anatomy) and [custom design systems](https://developer.android.com/develop/ui/compose/designsystems/custom) guidance: wrapper `@Immutable` data classes, `staticCompositionLocalOf` providers, a theme object with `@Composable @ReadOnlyComposable` getters, and `MaterialTheme` wrapped (not replaced) inside `RiteTheme` so Material components keep working.

### Package layout

```
presentation/ui/theme/
  RiteTheme.kt             — entry point, CompositionLocalProviders, expect fun RiteTheme()
  RiteColorScheme.kt       — data class, LightSageColors / DarkSageColors
  RiteTypography.kt        — data class, M3 slots + eyebrow + displayItalic + mono
  RiteShapes.kt            — data class, xs/sm/md/lg/xl/xxl/pill
  RiteSpacing.kt           — data class, gap1..gap8
  RiteMotion.kt            — data class, durations + easings
  RiteDimensions.kt        — icon sizes
```

### `RiteColorScheme`

Single flat namespace. Material 3 slots at the top, Rite-specific extensions below. `.toMaterialColorScheme()` produces an M3 `ColorScheme` for `MaterialTheme` to consume internally; app code never reads `MaterialTheme.colorScheme` directly.

```kotlin
@Immutable
data class RiteColorScheme(
    // — Material 3 core —
    val primary: Color, val onPrimary: Color,
    val primaryContainer: Color, val onPrimaryContainer: Color,
    val secondary: Color, val onSecondary: Color,
    val secondaryContainer: Color, val onSecondaryContainer: Color,
    val tertiary: Color, val onTertiary: Color,
    val tertiaryContainer: Color, val onTertiaryContainer: Color,
    val error: Color, val onError: Color,
    val errorContainer: Color, val onErrorContainer: Color,
    val background: Color, val onBackground: Color,
    val surface: Color, val onSurface: Color,
    val surfaceVariant: Color, val onSurfaceVariant: Color,
    val surfaceDim: Color, val surfaceBright: Color,
    val surfaceContainerLowest: Color, val surfaceContainerLow: Color,
    val surfaceContainer: Color, val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val outline: Color, val outlineVariant: Color,
    val inverseSurface: Color, val inverseOnSurface: Color, val inversePrimary: Color,
    val scrim: Color,

    // — Rite extensions —
    val onSurfaceMuted: Color,     // ink-3 — secondary body text
    val onSurfaceSubtle: Color,    // ink-4 — placeholders, disabled
    val primaryPressed: Color,     // accent-sunk — pressed state
    val warn: Color, val onWarn: Color,
    val warnContainer: Color, val onWarnContainer: Color,
    val suspend: Color, val onSuspend: Color,
    val suspendContainer: Color, val onSuspendContainer: Color,

    // — Day classification (calendar + heatmap) —
    val dayPerfect: Color,
    val dayBestEffort: Color,
    val dayPartial: Color,
    val dayRoughDay: Color,
    val dayFailed: Color,
    val daySkipped: Color,
    val dayFuture: Color,
    val dayNone: Color,
) {
    internal fun toMaterialColorScheme(): ColorScheme = /* ... */
}
```

### Sage palette — exact mapping

Source: `rite/project/styles/tokens.css`.

| `RiteColorScheme` field | Sage light | Sage dark | Source token |
|---|---|---|---|
| `primary` | `#5E7F6C` | `#9FBDA9` | `c-accent` |
| `onPrimary` | `#FBF7F1` | `#141413` | inverse of surface |
| `primaryContainer` | `#CFDDD1` | `#2E4438` | `c-accent-soft` |
| `onPrimaryContainer` | `#2D4F3F` | `#CBE1D1` | `c-accent-ink` |
| `primaryPressed` | `#B6C9BB` | `#1F2E26` | `c-accent-sunk` |
| `secondary` | `#545F72` | `#8A94A6` | `c-secondary` |
| `onSecondary` | `#FBF7F1` | `#141413` | inverse of surface |
| `secondaryContainer` | `#D4D9E0` | `#2B303A` | `c-secondary-soft` |
| `onSecondaryContainer` | `#1F1E1B` | `#EDE6D9` | `c-ink` |
| `tertiary` | `#5E7F6C` | `#9FBDA9` | aliased to `primary` |
| `onTertiary` | `#FBF7F1` | `#141413` | aliased to `onPrimary` |
| `tertiaryContainer` | `#CFDDD1` | `#2E4438` | aliased to `primaryContainer` |
| `onTertiaryContainer` | `#2D4F3F` | `#CBE1D1` | aliased to `onPrimaryContainer` |
| `error` | `#8B4B3E` | `#C78878` | `c-fail` |
| `onError` | `#FBF7F1` | `#141413` | inverse of surface |
| `errorContainer` | `#E4C9BF` | `#3A231C` | `c-fail-soft` |
| `onErrorContainer` | `#1F1E1B` | `#EDE6D9` | `c-ink` |
| `warn` | `#A67A3A` | `#D0A262` | `c-warn` |
| `onWarn` | `#FBF7F1` | `#141413` | inverse of surface |
| `warnContainer` | `#E8D7B8` | `#3C2F1A` | `c-warn-soft` |
| `onWarnContainer` | `#1F1E1B` | `#EDE6D9` | `c-ink` |
| `suspend` | `#7A6E85` | `#A396AE` | `c-suspend` |
| `onSuspend` | `#FBF7F1` | `#141413` | inverse of surface |
| `suspendContainer` | `#DDD6E1` | `#2C2633` | `c-suspend-soft` |
| `onSuspendContainer` | `#1F1E1B` | `#EDE6D9` | `c-ink` |
| `background` | `#F6F1EA` | `#141413` | `c-bg` |
| `onBackground` | `#1F1E1B` | `#EDE6D9` | `c-ink` |
| `surface` | `#FBF7F1` | `#1C1B19` | `c-surface` |
| `onSurface` | `#1F1E1B` | `#EDE6D9` | `c-ink` |
| `surfaceVariant` | `#EDE4D6` | `#121110` | `c-surface-inset` |
| `onSurfaceVariant` | `#3A362F` | `#CFC7B6` | `c-ink-2` |
| `onSurfaceMuted` | `#6B6459` | `#9A9283` | `c-ink-3` |
| `onSurfaceSubtle` | `#9A9185` | `#6B6558` | `c-ink-4` |
| `surfaceDim` | `#EFE8DE` | `#0E0E0D` | `c-bg-sunk` |
| `surfaceBright` | `#FFFDF8` | `#242320` | `c-surface-raised` |
| `surfaceContainerLowest` | `#EDE4D6` | `#121110` | `c-surface-inset` |
| `surfaceContainerLow` | `#EFE8DE` | `#1C1B19` | `c-bg-sunk` / surface |
| `surfaceContainer` | `#FBF7F1` | `#1C1B19` | `c-surface` |
| `surfaceContainerHigh` | `#FFFDF8` | `#242320` | `c-surface-raised` |
| `surfaceContainerHighest` | `#FFFDF8` | `#2F2D29` | lightened raised |
| `outline` | `#D1C5B0` | `#3B3832` | `c-rule-strong` |
| `outlineVariant` | `#E4DBCB` | `#2A2824` | `c-rule` |
| `inverseSurface` | `#1F1E1B` | `#EDE6D9` | swap of `surface` |
| `inverseOnSurface` | `#FBF7F1` | `#141413` | swap of `onSurface` |
| `inversePrimary` | `#9FBDA9` | `#5E7F6C` | swap of `primary` |
| `scrim` | `#000000` | `#000000` | black overlay |

### Day classification mapping

Source: `rite/project/src/screens-b.jsx` legend definition.

| Field | Sage light | Sage dark | Derived from |
|---|---|---|---|
| `dayPerfect` | `#5E7F6C` | `#9FBDA9` | `primary` |
| `dayBestEffort` | `#CFDDD1` | `#2E4438` | `primaryContainer` |
| `dayPartial` | `#A67A3A` | `#D0A262` | `warn` |
| `dayRoughDay` | `#E4C9BF` | `#3A231C` | `errorContainer` |
| `dayFailed` | `#8B4B3E` | `#C78878` | `error` |
| `daySkipped` | `#DDD6E1` | `#2C2633` | `suspendContainer` |
| `dayFuture` | border `#D1C5B0` dashed, no fill | border `#3B3832` dashed | `outline` |
| `dayNone` | `Color.Transparent` | `Color.Transparent` | — |

### `RiteTypography`

Three font families — **Fraunces** (display, for numeric values and headlines; supports italic variant), **Inter Tight** (body, titles, buttons, inputs), **JetBrains Mono** (uppercase letter-spaced labels: section headers, category eyebrows, stat tile labels, small UI chrome). Values drawn from `tokens.css` `--fs-*` scale.

| Slot | Family | Weight | Size | Line height | Letter spacing | Notes |
|---|---|---|---|---|---|---|
| `displayLarge` | Fraunces | 300 | 64sp | 72sp | -0.02em | "Show up for yourself." |
| `displayMedium` | Fraunces | 400 | 44sp | 52sp | -0.01em | "Quiet discipline." |
| `displaySmall` | Fraunces | 500 | 32sp | 40sp | -0.01em | Section hero |
| `headlineLarge` | Fraunces | 500 | 32sp | 40sp | -0.01em | — |
| `headlineMedium` | Fraunces | 500 | 28sp | 36sp | -0.01em | — |
| `headlineSmall` | Fraunces | 500 | 24sp | 32sp | 0 | — |
| `titleLarge` | Fraunces | 400 italic | 22sp | 28sp | -0.01em | Habit names in snackbars |
| `titleMedium` | Inter Tight | 600 | 18sp | 24sp | 0 | CTAs, stat values in short form |
| `titleSmall` | Inter Tight | 600 | 16sp | 22sp | 0.1sp | — |
| `bodyLarge` | Inter Tight | 400 | 16sp | 24sp | 0 | Body copy |
| `bodyMedium` | Inter Tight | 400 | 14sp | 20sp | 0.1sp | Secondary body |
| `bodySmall` | Inter Tight | 400 | 12sp | 16sp | 0.2sp | — |
| `labelLarge` | Inter Tight | 500 | 14sp | 20sp | 0.1sp | Buttons |
| `labelMedium` | Inter Tight | 500 | 12sp | 16sp | 0.1sp | Chips |
| `labelSmall` | Inter Tight | 500 | 11sp | 16sp | 0.2sp | — |
| `eyebrow` (ext) | JetBrains Mono | 500 | 11sp | 16sp | 0.18em (letter-spaced) | `BINARY RITUAL`, `ENFORCEMENT LIMITS`, `TODAY'S FOCUS` |
| `displayItalic` (ext) | Fraunces italic | 400 | 22sp | 28sp | -0.01em | Habit names in snackbars; some salutes |
| `mono` (ext) | JetBrains Mono | 500 | 10.5sp | 16sp | 0.2em | Section headers, stat tile labels |

Numerics (stat values, streak counts, ring percentages, "11 days") use Fraunces — this is what the screens render even though the spec chat flagged JetBrains Mono. Screens are authoritative.

### `RiteShapes`

| Field | Value |
|---|---|
| `xs` | `RoundedCornerShape(2.dp)` |
| `sm` | `RoundedCornerShape(4.dp)` |
| `md` | `RoundedCornerShape(8.dp)` |
| `lg` | `RoundedCornerShape(12.dp)` |
| `xl` | `RoundedCornerShape(16.dp)` |
| `xxl` | `RoundedCornerShape(20.dp)` |
| `pill` | `RoundedCornerShape(50)` (percentage; equivalent to fully rounded) |

### `RiteSpacing`

| Field | Value |
|---|---|
| `gap1` | `4.dp` |
| `gap2` | `8.dp` |
| `gap3` | `12.dp` |
| `gap4` | `16.dp` |
| `gap5` | `20.dp` |
| `gap6` | `24.dp` |
| `gap7` | `28.dp` |
| `gap8` | `32.dp` |

### `RiteMotion`

| Field | Value |
|---|---|
| `quick` | `160.milliseconds` |
| `standard` | `280.milliseconds` |
| `deliberate` | `480.milliseconds` |
| `easeQuiet` | `CubicBezierEasing(0.2f, 0.6f, 0.2f, 1f)` |
| `easeWeighted` | `CubicBezierEasing(0.4f, 0f, 0.2f, 1f)` |

### `RiteAppTheme` access object

```kotlin
object RiteAppTheme {
    val colors: RiteColorScheme
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
}
```

`RiteTheme` composable wires all five `CompositionLocal`s and the M3 `MaterialTheme` using `RiteColorScheme.toMaterialColorScheme()` and a `Typography` derived from the `RiteTypography` M3 slots.

Call-site migration: existing `RiteAppTheme.colorScheme` → `RiteAppTheme.colors`. Mechanical rename, no behavioral change.

## Component inventory & slice breakdown

Eight slices. Each is a short-lived branch off `feature/design-system-v2` with a PR into the parent. Items marked **★** are net-new; others are rebuilds or re-skins of existing files.

### Slice 1 — Foundation

Tokens + primitives. The rest of the app recolors automatically from this slice.

- Replace `theme/Theme.kt` and `theme/Type.kt` with six files per the package layout above.
- Delete `Forest*` / `ForestDark*` constants, `habitLockTypography`, Manrope font resource.
- Add Fraunces, Inter Tight, JetBrains Mono as Compose font resources (replacing Manrope; Inter replaced by Inter Tight).
- Rename `RiteAppTheme.colorScheme` → `RiteAppTheme.colors` across call sites.
- Rebuild `PrimaryButton` with primary / secondary / ghost variants matching the design's ink-button treatment.
- ★ New: `RitePill`, `StrictnessPill` (pulsing dot), `RiteChip` (letter chips + shortcut chips), `RiteDivider`, `ProgressRing`.
- Re-skin: `RiteDialog` (destructive-confirm dialog scaffold) via M3 `AlertDialog` styling.
- ★ New: `RiteSnackbar` with four variants (`completed`, `skipped`, `failed`, `suspended`) — replaces current M3 snackbar usage end-to-end. Structure: tinted-bg, habit name set in `displayItalic`, tiny check/skip/x/moon glyph in soft-tinted square, uppercase letter-spaced action button.
- Rebuild `BottomNav` (three tabs: Today · History · Settings).
- Add a `ThemeTokensPreview` composable and screenshot test rendering every color, typography slot, shape, and spacing value for visual regression on future slices.

### Slice 2 — Today + HabitCard

- Rebuild `TodayHeader` — expanded ↔ collapsed state with salute (Fraunces), strictness pill, progress ring, `+` button.
- Rebuild `HabitCard` supporting all 15 combinations: 5 status variants (pending / completed / skipped / failed / suspended) × binary / quantitative × daily / flexible-weekly.
- Re-skin `SwipeableHabitCard` + `SwipeAction` (edit / archive behind swipe, undo snackbar after destructive swipe).
- Re-skin `SectionHeader` ("Today's Focus" / "Weekly Goals") using `eyebrow` typography.
- Rebuild `QuantitativeInputBottomSheet`.
- ★ New: `TimezoneBanner` (informational variant, dismissible).
- Rebuild Today empty state ("Structure your day." + CTA).
- Re-skin `TodayCounts`.

### Slice 3 — Habit Detail

Primary action area stays in its current structural position (between category eyebrow and stat tiles) and is re-skinned in place; the design system's detail screens don't finalize button placement.

- ★ New: `CategoryEyebrow` (`BINARY RITUAL` / `QUANTITATIVE PURSUIT`, `eyebrow` typography).
- Re-skin primary action area (Complete / Completed / Skip / Skipped / Goal Reached variants; quantitative also: Custom + Undo + Undo last).
- ★ New: `StatTile` × 3 (Current Streak · Longest Streak · Habit Score), with Fraunces value and `eyebrow` label.
- Re-skin `EnforcementLimits` section header + skip counter copy (`X skips remaining` / `Unlimited skips` / `No skips remaining`).
- ★ New: `Heatmap` (3-month grid, "Last 3 months" title, 5-state legend: Perfect · BestEffort · Partial · Failed · Skipped).

### Slice 4 — Habit Form

- Rebuild `TypeToggle` (Binary / Quantitative, with descriptions).
- Rebuild `SchedulePicker` (Daily / Weekly selector; Weekly → Specific days / Any day sub-selector; single-letter chips + shortcut chips `Every day / Weekdays / Weekend`; quota field for flexible-weekly).
- ★ New: `ReminderConfigurator` (on/off switch; Fixed / Periodic type toggle; Fixed → single time picker; Periodic → Every [interval] [min/hr] + From [startTime] + Until [endTime]; inline validation "Start time must be before end time").
- ★ New: `TrackToggle` with permission-denied inline warning ("Notifications are disabled. Tap to open Settings.") and combined-on hint.
- Re-skin `QuantityStepper` (default-increment control).
- Re-skin `DetailRow`, input fields.
- Re-skin collapsible Note section, delete dialog, CTA button.

### Slice 5 — Onboarding

Also reorders steps: Philosophy → Strictness → **FirstHabit** → **NotificationPermission** (last two currently swapped in `OnboardingWizard`).

- ★ New: `ProgressStepIndicator` (shows current step of total).
- Re-skin `PhilosophyStep` ("Show up for yourself.").
- Rebuild `StrictnessStep` with `StrictnessAccordion` ★ (three expandable preset cards) + RECOMMENDED badge on Balanced + four-bullet rules per preset.
- Rebuild `FirstHabitStep` (mini form with name / type cards / target value / unit / Create habit / Skip for now; error states for empty name and missing/invalid target).
- Re-skin `NotificationPermissionStep` ("With you every step." + three info cards + two CTAs).
- Re-skin `OnboardingCta`, `OnboardingTopChrome`, `OnboardingWizard` (step order change lands here).

### Slice 6 — Calendar

- Rebuild month grid + prev/next month navigation.
- ★ New: `CalendarDayCell` rendering all 7 `DayClassification` states visually distinct (Perfect / BestEffort / Partial / RoughDay / Failed / Future / None).
- ★ New: `CalendarLegend` (5-state: Perfect / BestEffort / Partial / RoughDay / Future — Failed and None not in legend).
- Re-skin top stats (Perfect Days + Days Tracked).
- Re-skin day-of-week header (three-letter labels Mon · Tue · Wed … — distinct from the single-letter habit-form chips).

### Slice 7 — Settings + Archived + remaining sheets/dialogs

- Re-skin Settings sections (Notifications / Undo Policy / Snooze / Skip / Info / Archived Habits entry).
- Wire Slice-1 snackbar primitives into Settings save feedback ("Settings saved" / "Daily summary time updated").
- Rebuild Archived Habits list (tombstones + "Best streak: X days" + restore + delete-permanently icons).
- Re-skin Archived empty state ("No archived habits" / "Archived habits will appear here").
- Sweep any remaining bottom sheets / dialogs not already covered in slices 2–4.

### Slice 8 — Polish

- Wire motion tokens into existing animations: Today header collapse/expand (`easeWeighted`, `standard`), progress ring fill (`easeQuiet`, `deliberate`), strictness pill pulsing dot (update existing animation), snackbar lifecycle.
- Dark mode sweep: render every screen in dark mode, fix any contrast regressions, validate `onSurface*` hierarchy reads correctly.
- Screenshot test coverage: every screen × {light, dark} at 360×800dp. Bring any gaps to full coverage.
- Contrast audit against WCAG AA; dynamic type smoke test; touch target verification (≥ 44dp).
- Respect `ReduceMotion` setting (`presentation/ui/ReduceMotion.kt`) across all newly-motioned surfaces.

## Testing strategy

- Screenshot tests continue to run at 360×800dp (JVM target). Every screen × {light, dark} covered.
- Component previews: every `HabitCard` status variant, every `Snackbar` variant, every `CalendarDayCell` state, every `StatTile` variant, `StrictnessAccordion` states, `ProgressStepIndicator` states.
- Each slice PR must include screenshot-test updates for the components/screens it touches. Motion-only changes that can't be screenshot-tested are documented explicitly in the PR.
- Slice 1 adds a `ThemeTokensPreview` / screenshot test — renders every color, typography slot, shape, and spacing value on one canvas. Detects token regressions without relying on downstream consumers.
- Domain-layer tests (`ProcessEndOfDayTest`, etc.) are untouched.

## Branch strategy

- Parent branch `feature/design-system-v2` cut from `develop`.
- Each slice: a short-lived branch off the parent (e.g. `feature/design-system-v2/01-foundation`), PR into parent. Merge with individual commits preserved (no squash to parent) for readable history.
- At the start of each new slice, merge `develop` into the parent to minimize drift and surface conflicts early.
- Final step: one PR from `feature/design-system-v2` → `develop`. That PR is the readable changelog of the migration.
- CI runs on every slice PR against the parent.

## Retirement of old assets

Removed at slice 1 and never referenced again:

- `Theme.kt` `Forest*` and `ForestDark*` constants, `LightColorScheme`, `DarkColorScheme`, `LightRiteColorScheme`, `DarkRiteColorScheme` (current values).
- `Type.kt` `habitLockTypography()`, `manropeFontFamily()`, `interFontFamily()` (replaced by `RiteTypography`).
- `composeResources/font/manrope.ttf`, `composeResources/font/inter.ttf`.
- The `RiteAppTheme.colorScheme` property (renamed to `.colors`).

## Risk & rollback

- **Highest-risk slice: slice 1.** Recolors the whole app in one commit. Regressions are immediately visible; fix lives in the tokens, not scattered.
- **Font loading:** three new font files means cold-start flash-of-unstyled-text risk. Validate on Android + JVM; the `Font()` loader defaults handle this but worth smoke-testing.
- **M3 primitive regressions:** `TextField`, `Switch`, `RadioButton`, `Snackbar`, `Button` should be eyeball-tested on Android + JVM + iOS at least once per slice that introduces them.
- **Rollback**: the parent branch is disposable. If the migration is abandoned, `feature/design-system-v2` is deleted without touching `develop`. If one slice misbehaves after partial landing, revert the slice PR (possible because slice PRs preserve individual commits).

## Open decisions for implementation phase

- **M3 `OutlinedTextField` styling:** attempt to skin first; if it won't bend to the design's thin rule-divider input treatment, build from scratch in slice 4.
- **Pulsing strictness-pill dot:** update the existing animation (already present in the app) to match the design system's tempo and color treatment. Implementation detail for slice 2.

## References

- Design system bundle: `/tmp/rite-design/rite/project/` (tokens.css, rite.css, card-reimagined.css, src/*.jsx)
- Android: [Anatomy of a theme in Compose](https://developer.android.com/develop/ui/compose/designsystems/anatomy)
- Android: [Custom design systems in Compose](https://developer.android.com/develop/ui/compose/designsystems/custom)
- Product brief: `/tmp/rite-design/rite/chats/chat1.md` (Rite V2 — Product Brief)
