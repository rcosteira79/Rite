# Rite Design System — Slice 3 (Habit Detail) Design

## Problem

The Habit Detail screen is already on Rite v2 tokens from Slice 1, but its composition is still pre-v2:
habit name is `.uppercase() + ExtraBold` (Material-ish) instead of Fraunces 34sp regular-weight proper-case;
the category label uses `labelSmall`, not the `eyebrow` type slot; stat cards use rounded surfaces
instead of the design bundle's editorial bordered grid; the progress ring is wrapped in a decorative
card that doesn't match the Today-screen ring treatment; the enforcement-limits area is a one-line
card that shows less information than the design bundle's 5-row table; and the heatmap renders a plain
"Last 3 months" title instead of the compound `LAST 3 MONTHS · TAPESTRY` + week-range header. The
habit description is present in the data model but unrendered.

Slice 3 rebuilds this screen on v2 primitives so it matches the design bundle editorially and
surfaces every piece of information the design calls for.

## Solution

Follow the design bundle (`/tmp/rite-design/rite/project/`) across the board. Where the parent
migration spec narrows the design (notably the enforcement-limits section), expand to the full
design and wire the data the design implies. No M3-style deviations.

The existing `HabitDetailScreen.kt` is rewritten to compose five new/polished primitives under
`ui/habitdetail/components/`. The ViewModel is expanded to surface strictness, undo policy, snooze
usage, weekly skip count, and the data the enforcement table needs. `HabitDetailRoute.kt` is
deleted — the route-like wrapper becomes a public `HabitDetailScreen` overload alongside the pure
internal one, matching the `TodayScreen` convention.

## Scope

### In scope

- **CategoryEyebrow** primitive (★ new) — `BINARY RITUAL` / `QUANTITATIVE PURSUIT` in `eyebrow`
  typography (JetBrains Mono 10.5sp, 0.22em tracking, `onSurfaceMuted`).
- **Render habit description** (habit notes) under the name — `Habit.description`, Fraunces
  italic 13.5sp, `onSurfaceVariant`, max-width 34ch, shown only when non-null. New rendering; the
  field is already on `HabitDetailUiModel`.
- **StatTileRow** primitive (★ new) — 3-column bordered grid. Top + bottom `outline` rules,
  1px `outline` column dividers, no fill. Each cell: mono eyebrow label (Current streak /
  Longest / Habit score) + Fraunces 30sp value + mono unit ("Days" / "/100"). Private `StatTile`
  composable inside the file.
- **Tapestry** primitive — renames and polishes `HeatmapGrid.kt`. Compound mono header:
  `"Last 3 months · Tapestry"` on the left + ISO-week range on the right (e.g. `W17 — W04`,
  computed from the date window). Grid, M/W/F/S row labels, 5-state legend (Perfect / Best effort
  / Partial / Failed / Skipped) kept from the current impl.
- **EnforcementLimitsTable** primitive (★ new) — 5-row bordered table inside `surface`-filled
  rounded container (1px `outline` border, 4dp corner, 1px internal row dividers). Rows:
  - `Strictness` — `StrictnessPreset.fromSettings(...)` value (Flexible / Balanced / Unwavering)
    or "Custom" when the user customised their settings.
  - `Undo` — `user.undoPolicy` mapped to "All history" / "Today only" / "Disabled".
  - `Snoozes` — `snoozeCount / maxSnoozesPerHabitPerDay used today`, or "Unlimited" if
    `maxSnoozesPerHabitPerDay == null`.
  - `Skips` — `N this week` (informational, no denominator). Counts `HabitInstance`s with
    `status == SKIPPED` whose `date` falls in the current ISO week in the user's timezone.
  - `Consecutive` — `currentConsecutiveSkips / max maxConsecutiveSkips`, or "Unlimited" if
    `maxConsecutiveSkips == null`. Row value rendered in `suspend` colour when locked
    (`currentConsecutiveSkips >= maxConsecutiveSkips`).
- **HabitDetailAction** primitive (★ new, extracted from inline) — action area that branches on
  status + type. Variants:
  - Binary: Complete (pending) / Completed (resolved → Undo) / Skip / Skipped (→ Undo) / Goal
    reached (n/a binary, covered by Completed).
  - Quantitative: Stepper row (pending) / Custom (opens sheet) / Undo + Undo last
    (resolved/in-progress) / Goal reached (when `isQuantitativeComplete`).
  - Skip Today button below primary action (both types); disabled when `isSkipLocked`.
- **HabitDetailScreen re-assembly** — layout order:
  topbar → eyebrow + name + note → ring → action → stat grid → tapestry → enforcement table.
  Ring reuses the Slice 1 `ProgressRing` primitive with `capLabel = null`, no surrounding card,
  matching the Today-header style.
- **Route-as-Screen-overload** — delete `HabitDetailRoute.kt`. Add a public `HabitDetailScreen`
  overload in `HabitDetailScreen.kt` that takes `instanceId`, `onNavigateBack`, `onEditHabit`,
  wires the ViewModel from `LocalAppComponent`, hosts the `QuantitativeInputBottomSheet` and
  delete-confirm dialog, and delegates to the internal pure overload. Mirrors `TodayScreen`.

### Out of scope / deferred

- **Decorative status cards** the JSX shows under the action area for completed / failed /
  skip-locked states ("Completed at 07:14 / Streak → 15 days" accent card, etc.). Not in the
  current app; revisit in a later polish pass if desired.
- **Weekly skip *limit***. The `Skips` row is informational only — the app does not enforce a
  weekly skip cap, only a consecutive one. Adding a cap requires new user settings wiring and
  strictness-preset changes outside Slice 3.
- **Animations / motion polish.** Slice 8 handles motion tokens across the app, including the
  action area's Skip ↔ Undo morph and ring-fill animation. Slice 3 ships static goldens.
- **Suspension status**. The `suspended` state is handled at the card level (Slice 2). No
  detail-screen changes needed for it in Slice 3.

## Goals

- Habit Detail renders in the Rite v2 editorial voice in both light and dark modes.
- Every primitive has a Compose preview and a roborazzi golden; the screen has goldens for the
  main status variants in both modes.
- No behavioural regressions — domain-layer tests untouched.

## Non-goals

- Adding new app features (weekly-skip limits, decorative status cards, etc.).
- Refactoring unrelated code in the habit-detail package.
- Touching `HabitFormScreen` / `TodayViewModel` coupling (deferred, per `CLAUDE.md`).

## Package layout

```
presentation/ui/habitdetail/
├── HabitDetailScreen.kt        — public overload (route-like) + internal overload (pure)
├── HabitDetailState.kt         — top-level (alongside VM + Screen)
├── HabitDetailViewModel.kt     — loadDetail expanded for new data
├── HabitDetailEvent.kt         — unchanged (NavigateBack)
├── models/
│   ├── HabitDetailUiModel.kt   — expanded with strictness / undo / snoozes / skipsThisWeek
│   └── HeatmapDay.kt           — moved from HabitDetailState.kt (it was co-resident)
└── components/
    ├── CategoryEyebrow.kt
    ├── StatTileRow.kt
    ├── Tapestry.kt             — renamed from HeatmapGrid.kt
    ├── EnforcementLimitsTable.kt
    └── HabitDetailAction.kt
```

Deleted: `HabitDetailRoute.kt`.

## `HabitDetailUiModel` expansion

Five new fields; existing fields unchanged.

```kotlin
data class HabitDetailUiModel(
    // … existing fields …
    val strictnessPreset: StrictnessPreset?,   // null when user customised their settings
    val undoPolicy: UndoPolicy,
    val snoozesUsedToday: Int,
    val maxSnoozesPerDay: Int?,                // null = unlimited
    val skipsThisWeek: Int,                    // informational count
)
```

Data sources in `HabitDetailViewModel.loadDetail`:

| Field | Source |
|---|---|
| `strictnessPreset` | `StrictnessPreset.fromSettings(UserStrictnessSettings(user.undoPolicy, user.maxSnoozesPerHabitPerDay, user.maxConsecutiveSkips, user.maxSnoozeDurationMinutes))` |
| `undoPolicy` | `user.undoPolicy` |
| `snoozesUsedToday` | `snoozeRepository.getSnoozeState(instanceId)?.snoozeCount ?: 0` |
| `maxSnoozesPerDay` | `user.maxSnoozesPerHabitPerDay` |
| `skipsThisWeek` | count of `allInstances` where `status == SKIPPED && date ∈ currentIsoWeek(user.timezone)` |

New VM dependency: `SnoozeRepository` injected via kotlin-inject. `HabitDetailViewModel.Factory`
signature unchanged (still `create(instanceId: String)`); the factory gets the new repo from DI.

Week calculation: ISO-8601 week (Monday 00:00 → Sunday 23:59 local time in `user.timezone`).
Counting stops at today — future dates are zero by construction. Pure helper function, unit-tested
separately so the VM stays thin.

## Primitive specs

### `CategoryEyebrow`

```kotlin
@Composable
fun CategoryEyebrow(
    type: HabitType,
    modifier: Modifier = Modifier,
)
```

Renders `stringResource(Res.string.habit_detail_category_binary)` or `..._quantitative` in
`RiteAppTheme.typography.eyebrow`, colour `onSurfaceMuted`. Single Text composable.

### `StatTileRow`

```kotlin
@Composable
fun StatTileRow(
    currentStreak: Int,
    longestStreak: Int,
    habitScore: Int,
    modifier: Modifier = Modifier,
)
```

Row with three equal-width private `StatTile(label, value, unit)` cells. Top + bottom `outline`
rule via `.drawBehind`. Internal column dividers drawn the same way. Label in `eyebrow`, value in
Fraunces 30sp regular, unit suffix in mono 11sp. Labels come from existing string resources
(`habit_detail_stat_current_streak`, `..._longest_streak`, `..._habit_score`, `..._stat_days`).

### `Tapestry`

```kotlin
@Composable
fun Tapestry(
    data: ImmutableList<HeatmapDay>,
    weekRangeLabel: String,
    modifier: Modifier = Modifier,
)
```

Compound header: `LAST 3 MONTHS · TAPESTRY` left + `weekRangeLabel` (e.g., `W17 — W04`) right,
both `eyebrow` mono. Grid, labels, legend kept from current `HeatmapGrid`. Data-to-colour mapping
unchanged (`dayPerfect` / `dayBestEffort` / `dayPartial` / `dayFailed` / `daySkipped`).

Caller (`HabitDetailScreen`) computes `weekRangeLabel` from the 3-month window using ISO week
numbers — pure helper, unit-tested.

New string resource: `habit_detail_heatmap_title_compound` (`"Last 3 months · Tapestry"`).

### `EnforcementLimitsTable`

```kotlin
@Composable
fun EnforcementLimitsTable(
    strictnessPreset: StrictnessPreset?,
    undoPolicy: UndoPolicy,
    snoozesUsedToday: Int,
    maxSnoozesPerDay: Int?,
    skipsThisWeek: Int,
    currentConsecutiveSkips: Int,
    maxConsecutiveSkips: Int?,
    modifier: Modifier = Modifier,
)
```

Minimal params per existing "minimal component params" memory — no `HabitDetailUiModel` passed
wholesale. Rows built internally via a private sealed hierarchy or list-of-pairs. `suspend`
colour on Consecutive row value when locked.

New string resources under `strings_habit_detail.xml`:

- `habit_detail_enf_row_strictness` / `..._undo` / `..._snoozes` / `..._skips` / `..._consecutive`
- `habit_detail_enf_strictness_flexible` / `..._balanced` / `..._unwavering` / `..._custom`
- `habit_detail_enf_undo_all_history` / `..._today_only` / `..._disabled`
- `habit_detail_enf_snoozes_used_today` = `"%1$d / %2$d used today"`
- `habit_detail_enf_snoozes_unlimited` = `"Unlimited"`
- `habit_detail_enf_skips_this_week` = `"%1$d this week"`
- `habit_detail_enf_consecutive_of_max` = `"%1$d / max %2$d"`
- `habit_detail_enf_consecutive_unlimited` = `"Unlimited"`

### `HabitDetailAction`

```kotlin
@Composable
fun HabitDetailAction(
    state: HabitDetailUiModel,
    onComplete: () -> Unit,
    onIncrement: () -> Unit,
    onCustomProgress: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    onUndoIncrement: () -> Unit,
    modifier: Modifier = Modifier,
)
```

Two internal branches: `BinaryBlock` (Complete ↔ Undo + Skip) and `QuantitativeBlock` (Stepper /
Goal Reached / Undo + Custom + Skip). Visual fidelity to the design bundle's `.r-action` /
`.r-stepper` CSS, ink-on-surface primary button, tinted secondary.

No animation wiring in this slice — Slice 8 handles motion. Goldens are static.

## Screen layout order

```
┌───────────────────────────────────────┐
│  TopBar: back · edit · archive · delete│
├───────────────────────────────────────┤
│  CategoryEyebrow                      │
│  Habit name (Fraunces 34sp regular)   │
│  Habit note (Fraunces italic 13.5sp)  │  ← if description != null
├───────────────────────────────────────┤
│  ProgressRing (100dp, capLabel=null)  │
├───────────────────────────────────────┤
│  HabitDetailAction                    │
├───────────────────────────────────────┤
│  StatTileRow (bordered)               │
├───────────────────────────────────────┤
│  Tapestry (compound header + grid +   │
│    5-state legend)                    │
├───────────────────────────────────────┤
│  EnforcementLimitsTable (5 rows)      │
└───────────────────────────────────────┘
```

The action area's "current structural position (between category eyebrow and stat tiles)" from the
parent migration spec is preserved.

## Testing strategy

### Roborazzi goldens (per-primitive)

- `CategoryEyebrowPreviewTest` — binary + quantitative × light + dark (4 goldens).
- `StatTileRowPreviewTest` — typical values × light + dark (2 goldens).
- `TapestryPreviewTest` — fully populated 3-month sample × light + dark + empty-data edge case
  (3 goldens).
- `EnforcementLimitsTablePreviewTest` — FLEXIBLE / BALANCED / UNWAVERING presets × light + dark,
  plus one `suspend`-coloured locked case (7 goldens).
- `HabitDetailActionPreviewTest` — Complete / Completed / Skip / Skipped (binary) + Stepper /
  Goal reached / Undo / Custom (quantitative) × light (≈10 goldens).

### Screen-level goldens

- `HabitDetailScreenScreenshotTest` — binary pending, binary completed, binary failed,
  quantitative in-progress, quantitative goal-reached, quantitative skip-locked, each in
  light + dark (≈12 goldens).

### Unit tests

- `HabitDetailUiModelTest` — existing tests still pass; new tests for
  `isSkipLocked`, `skipsRemaining`, enforcement-related derived fields.
- `HabitDetailViewModelTest` — new behaviour: snooze count pickup, skips-this-week count from
  instance filter, strictness derivation, undo-policy copy, edge cases (null user, empty
  instance list, missing snooze state).
- `IsoWeekTest` — pure helper for `currentIsoWeek(timezone)` and `formatWeekRange(from, to)`.

## Commit strategy

Small, readable commits — merge-commit preserved into the parent branch (no squash):

1. `feat(habit-detail): add CategoryEyebrow primitive` + goldens
2. `feat(habit-detail): add StatTileRow primitive` + goldens
3. `feat(habit-detail): rename HeatmapGrid → Tapestry, compound header + week range` + goldens
4. `feat(habit-detail): add EnforcementLimitsTable primitive` + goldens
5. `feat(habit-detail): add HabitDetailAction primitive` + goldens
6. `refactor(habit-detail): expand UiModel + VM for strictness/snoozes/skipsWeek` + VM tests
7. `refactor(habit-detail): top-level State, move models/, delete HabitDetailRoute`
8. `feat(habit-detail): re-assemble HabitDetailScreen on v2 primitives` + screen goldens

## Branch strategy

- Slice branch: `feature/design-system-v2-03-habit-detail` (cut off
  `origin/feature/design-system-v2-02-today-habitcard`).
- PR base: `feature/design-system-v2-02-today-habitcard` — GitHub auto-retargets to
  `feature/design-system-v2` when Slice 2 merges up.
- Merge with individual commits preserved (no squash).

## References

- Parent migration spec: `docs/superpowers/specs/2026-04-18-rite-design-system-migration-design.md`
- Design bundle: `/tmp/rite-design/rite/project/`
  - Habit Detail JSX: `src/screens-a.jsx:437-614`
  - Stat grid CSS: `styles/rite.css:692-725`
  - Action buttons / stepper / icon buttons CSS: `styles/rite.css:884-1027`
  - Heatmap CSS: `styles/rite.css:499-516`
- Slice 2 precedent: git history on `feature/design-system-v2-02-today-habitcard`
  (`git log --oneline feature/design-system-v2-01-foundation..HEAD`) — especially
  `ui/today/habitcard/` package structure.
- TodayScreen route-overload pattern: `ui/today/TodayScreen.kt`.
