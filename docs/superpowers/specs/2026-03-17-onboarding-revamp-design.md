# Onboarding Revamp — Design Spec

**Date:** 2026-03-17
**Status:** Approved

---

## 1. Goal

Redesign the onboarding UI/UX from a functional prototype into a polished, native-feeling Android experience. The flow structure (3 screens) is preserved. The deliverable is a self-contained animated wizard that replaces the current three separate routes.

---

## 2. Flow Structure

Three steps, unchanged in content and order:

1. **Philosophy** — sets the app's mindset
2. **Strictness** — user picks a preset
3. **First Habit** — optional habit creation

### Single-route wizard

The entire onboarding is hosted on **one route** (`Route.Onboarding`) as an `OnboardingWizard` composable. It manages its own step state internally — no Nav3 backstack entries for individual steps.

```
Route.Onboarding
  └── OnboardingWizard
        ├── step 0 — PhilosophyStep
        ├── step 1 — StrictnessStep
        └── step 2 — FirstHabitStep
```

- Back press on step > 0 navigates to the previous step
- Back press on step 0 is a no-op (does not exit onboarding)
- `OnboardingRoute` wraps `OnboardingWizard`, collects VM events, and handles `NavigateToToday`

---

## 3. Visual Direction

**Style:** Editorial, left-aligned. Bold headlines, teal accent underline, generous whitespace. Clean and confident — not sterile, not aggressive.

**Theme:** Follows system (light/dark). Accessibility is a first-class concern — all text meets WCAG AA (4.5:1 minimum).

### Layout (all steps)

- **Top chrome:** expanding dot progress indicator (left) + Skip button (right) — rendered once outside `AnimatedContent`, does not re-animate on step transitions
- **Content area:** left-aligned, top-to-bottom — headline → accent line → body copy → step-specific content
- **Bottom:** `FilledButton` ("Continue" / "Create habit") — rendered once outside `AnimatedContent`; `TextButton` ("Skip for now") on First Habit step only

### Typography

Default M3 type scale (Roboto). No custom font bundling — avoids KMP asset complexity.

| Role | Style |
|---|---|
| Headline | `headlineLarge` — 800 weight |
| Body | `bodyLarge` |
| Skip / labels | `labelLarge` |

---

## 4. Color Scheme

Custom M3 `ColorScheme` seeded from `#006A6B` (deep teal), replacing the current `lightColorScheme()` / `darkColorScheme()` defaults in `Theme.kt`.

### Light

| Token | Value |
|---|---|
| `primary` | `#006A6B` |
| `primaryContainer` | `#C0EDED` |
| `onPrimaryContainer` | `#002020` |
| `surface` | `#FAFCFC` |
| `onSurface` | `#191C1C` |
| `onSurfaceVariant` | `#3F4948` |

### Dark

| Token | Value |
|---|---|
| `primary` | `#4CDADA` |
| `primaryContainer` | `#004F50` |
| `onPrimaryContainer` | `#9FF3F3` |
| `surface` | `#1B2030` |
| `onSurface` | `#DCE4E4` |
| `onSurfaceVariant` | `#BEC9C8` |

---

## 5. Progress Indicator

**Expanding dots** — left-aligned in top chrome.

- Active step: pill shape, `20dp × 6dp`, `primary` color
- Completed steps: circle `6dp × 6dp`, `primary` color at 45% opacity
- Inactive steps: circle `6dp × 6dp`, `surfaceVariant` color
- Active dot width animates via `animateDpAsState` with `Spring.DampingRatioMediumBouncy`
- Completed dot opacity animates via `animateFloatAsState`

---

## 6. Screen Designs

### 6.1 Philosophy (step 0)

- **Headline:** "Enforce what you commit to."
- **Accent line:** 36dp teal bar below headline
- **Body:** "Keep promises to yourself — even on hard days. You choose the rules. HabitLock helps you stick to them."
- **Lock watermark:** tonal container (`primaryContainer`, 45% opacity) bottom-right, lock SVG icon inside. Decorative — `contentDescription = null`.
- **CTA:** "Continue" `FilledButton`
- No secondary Skip visible on the Philosophy screen itself (Skip is in the top chrome)

### 6.2 Strictness (step 1)

- **Headline:** "How strict should it be?"
- **Accent line**
- **Sub-copy:** "You're always in control. Change this anytime."
- **Preset cards:** three `OutlinedCard`s — Flexible, Balanced (pre-selected), Locked
  - Unselected: `outline` border, `surface` background, compact (name + one-line description)
  - Selected: `primary` border, `primaryContainer` at 10% alpha background
    - Rules expand below description via `AnimatedVisibility(expandVertically + fadeIn)`
    - Previous card's rules collapse via `shrinkVertically + fadeOut`
  - Each card has a colored status dot (green / amber / red) next to the preset name
  - Balanced shows a small "Recommended" chip (`primaryContainer` background, `primary` text)
- No scrolling required — only one card is ever expanded at a time

### 6.3 First Habit (step 2)

- **Headline:** "Lock in your first habit."
- **Accent line**
- **Body:** "Start small. One habit is enough to begin."
- **Form:**
  - `OutlinedTextField` — habit name, placeholder "E.g. Drink water"
  - Type selector — two `FilterChip`s: "Yes / No" (default selected), "Quantitative"
  - Quantitative fields (conditionally shown via `AnimatedVisibility`): target value (`OutlinedTextField`, `KeyboardType.Number`) + unit (`OutlinedTextField`, optional)
- **CTA:** "Create habit" `FilledButton` — enabled only when name is non-blank (and target value non-blank if quantitative)
- **Secondary:** "Skip for now" `TextButton`

---

## 7. Animations

### Step transitions

`AnimatedContent` with slide + fade:

| Direction | Enter | Exit | Duration | Easing |
|---|---|---|---|---|
| Forward | Slide from right (24dp) + fade in | Slide to left (24dp) + fade out | 300ms enter / 200ms exit | `EmphasizedDecelerate` / `EmphasizedAccelerate` |
| Back | Slide from left (24dp) + fade in | Slide to right (24dp) + fade out | 300ms / 200ms | Same |

### Per-screen entry choreography (staggered, plays once on first composition)

| Element | Enter | Duration | Delay |
|---|---|---|---|
| Headline | Fade + translate up 12dp | 200ms | 0ms |
| Accent line | Width draws from 0 → 36dp | 250ms | 80ms |
| Body copy | Fade in | 200ms | 140ms |
| CTA button | Slide up 16dp + fade in | 200ms | 200ms |

### Strictness card expansion

- Expand: `AnimatedVisibility(expandVertically() + fadeIn())` — 250ms
- Collapse: `shrinkVertically() + fadeOut()` — 200ms

### Philosophy lock watermark

- Fades in at 45% opacity after 300ms delay on first composition

### Reduced motion

All animations check `LocalReducedMotion`. When enabled:
- Step transitions fall back to `Crossfade` (no translation)
- Per-screen stagger is skipped (content appears immediately)
- Card expansion/collapse uses `AnimatedVisibility(enter = EnterTransition.None, exit = ExitTransition.None)`

---

## 8. Accessibility

- All interactive elements ≥ 48dp touch target (`Modifier.minimumInteractiveComponentSize()` where needed)
- Skip button: 48dp minimum tap area
- Preset cards: `Modifier.semantics { role = Role.RadioButton }` + `stateDescription` for selected/unselected
- Lock watermark: `contentDescription = null` (decorative)
- Progress dots: `Modifier.semantics { contentDescription = "Step X of 3" }` on the dots container
- Form inputs: proper `KeyboardOptions` per field type
- All text in `sp` units (respects dynamic type)
- WCAG AA contrast verified for all color pairs in both light and dark themes

---

## 9. Architecture Summary

```kotlin
// OnboardingRoute.kt — owns event collection and navigation
@Composable
fun OnboardingRoute(viewModel: OnboardingViewModel, onFinished: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var currentStep by remember { mutableIntStateOf(0) }
    // collect events:
    //   NavigateToFirstHabit → currentStep = 2
    //   NavigateToToday      → onFinished()
    //   ShowError            → show snackbar
    OnboardingWizard(state, viewModel, currentStep, onStepChange = { currentStep = it })
}

// OnboardingWizard.kt — pure UI, no event collection
@Composable
fun OnboardingWizard(state: OnboardingState, viewModel: OnboardingViewModel, currentStep: Int, onStepChange: (Int) -> Unit) {
    BackHandler(enabled = currentStep > 0) { onStepChange(currentStep - 1) }

    Scaffold { padding ->
        Column {
            // Top chrome: dots + skip — outside AnimatedContent
            OnboardingTopChrome(currentStep, onSkip = { viewModel.skipToToday() })

            // Animated step content
            AnimatedContent(currentStep, ...) { step ->
                when (step) {
                    0 -> PhilosophyStep(...)
                    1 -> StrictnessStep(state, viewModel)
                    2 -> FirstHabitStep(state, viewModel)
                }
            }

            // CTA — outside AnimatedContent
            // Step 0: calls onStepChange(1) directly (no async work)
            // Step 1: calls viewModel.continueFromStrictness(); advance driven by NavigateToFirstHabit event
            // Step 2: calls viewModel.createFirstHabit(); advance driven by NavigateToToday event
            OnboardingCta(currentStep, state, viewModel, onAdvance = { onStepChange(currentStep + 1) })
        }
    }
}
```

- `currentStep` is `remember` — pure UI state, not in ViewModel
- Business state (preset, habit fields, loading) stays in `OnboardingViewModel` unchanged
- `Theme.kt` updated with custom `lightColorScheme` / `darkColorScheme` tokens

### Step advancement from Strictness (step 1)

`continueFromStrictness()` is async — it applies the preset and emits `NavigateToFirstHabit` on success, or sets `error` on failure. Step 1 → 2 advancement is **event-driven**, not immediate:

- The CTA on step 1 calls `viewModel.continueFromStrictness()` — it does **not** call `onAdvance` directly
- `OnboardingRoute` collects `NavigateToFirstHabit` and increments `currentStep` to 2
- While `isApplyingPreset = true`, the CTA shows a `CircularProgressIndicator` in place of the button label and is non-interactive (existing behaviour, preserved as-is)
- On error, the existing `ShowError` snackbar event is used — no new error UI needed

This means `OnboardingCta` must be aware of which step it's on to dispatch the right action.

### Obsolete ViewModel events

`NavigateToStrictness` and `NavigateToFirstHabit` were used by the old three-route model. In the new wizard:

- `NavigateToFirstHabit` is still emitted by `continueFromStrictness()` and is **collected** by `OnboardingRoute` to advance to step 2 (see above)
- `NavigateToStrictness` is emitted by `continueFromPhilosophy()` — this event becomes **unused**. `continueFromPhilosophy()` is replaced by a direct `onAdvance` call from the CTA on step 0 (no async work needed). The event and method may be removed from the VM or left as dead code — implementer's discretion, but removal is preferred.

### Route changes

`Route.Onboarding` does not yet exist. The plan must include:
1. Adding `Route.Onboarding` to `Route.kt`
2. Removing the three existing onboarding route objects (`Route.OnboardingPhilosophy`, `Route.OnboardingStrictness`, `Route.OnboardingFirstHabit` or equivalent)
3. Updating the nav host to use the single new route

---

## 10. Out of Scope

- Custom font (Lexend / Source Sans 3) — deferred, KMP asset complexity not warranted now
- Dynamic Color (Material You / Android 12+) — can be layered on top later
- Onboarding illustrations — not in this revamp
- Changes to `OnboardingViewModel` business logic
