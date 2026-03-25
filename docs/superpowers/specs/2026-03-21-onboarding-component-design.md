# Onboarding Component Design Spec

## Overview

Apply the Forest Discipline visual language to all three onboarding screens. The color scheme and
typography are already implemented (`Theme.kt`, `Type.kt`). This spec covers the remaining visual
layer: button style, background decoration, strictness card redesign, and first-habit input/card
redesign.

---

## 1. CTA Buttons — `OnboardingCta.kt`

**Current:** Default M3 `Button` with pill shape and `primary` container color (`#163829`).

**Design:** Rounded-rectangle shape (12dp radius) and `primaryContainer` (`#2D4F3F`) background.

**Changes:**

All three `Button` calls inside `CtaContainer` (`PhilosophyStepCta`, `StrictnessStepCta`,
`FirstHabitStepCta`) receive the same `shape` and `colors` overrides:

```kotlin
Button(
    onClick = ...,
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
) { ... }
```

No other changes to `OnboardingCta.kt`.

---

## 2. Philosophy Screen — `PhilosophyStep.kt`

**Current:** 88dp square box (24dp rounded corners) in the bottom-right corner, containing a
filled `Lock` icon at `primaryContainer`/`onPrimaryContainer` colors, 45% opacity.

**Design:** Replace with an architectural background ring — a large hollow circle positioned
behind all content, conveying structure without distracting from the text.

**Specification:**

- **Size:** 420 × 420 dp
- **Shape:** Circle (50% radius) with a 40dp-wide stroke (border), no fill
- **Color:** `primaryContainer` at 5% opacity
- **Rotation:** 12° clockwise (`graphicsLayer { rotationZ = 12f }`)
- **Position:** `Box` overlay using `Alignment.TopEnd`, offset so the ring bleeds off the top-right
  edge — `offset(x = 120.dp, y = (-80).dp)`
- **Rendering:** Drawn with `Canvas` + `drawCircle` (style = `Stroke(width = 40.dp.toPx())`),
  or equivalently with `Box` + circular border (see implementation note)
- **Pointer events:** Do not add any `clickable`, `pointerInput`, or indication modifier to the ring `Box` — absence of gesture modifiers means it is inherently non-interactive and will not intercept touches
- **Accessibility:** `contentDescription = null`, no semantics role

**Implementation note:** The simplest approach is a `Box` with `Modifier.size(420.dp)`,
`border(40.dp, color.copy(alpha = 0.05f), CircleShape)`, and `graphicsLayer { rotationZ = 12f }`.
This avoids a custom `Canvas` draw while producing the correct hollow-ring appearance.

Remove the `lockAlpha` `Animatable` and the associated `Box`/`Icon` composable entirely. The
`lockAlpha` animation step in `LaunchedEffect` is also removed; adjust the total `delay` chain
accordingly (the remaining animations — headline, accent line, body — are unchanged).

The architectural ring does **not** animate in; it is rendered at full opacity (5%) from
composition.

**Layout structure after change:**

```
Box(fillMaxSize, horizontalPadding = 24.dp) {
    // Background ring (TopEnd, offset)
    Box(420.dp, border = 40.dp circle primaryContainer@5%, rotation = 12°)

    // Foreground content column (unchanged)
    Column(fillMaxSize) { headline, accent line, body }
}
```

---

## 3. Strictness Screen — `StrictnessStep.kt` and `OnboardingStrictnessPreset.kt`

### 3a. Data model — `OnboardingStrictnessPreset.kt`

The existing `rules: List<String>` (bullet points) is replaced by structured key-value pairs for
the expanded selected state, and a short summary string for the collapsed state.

**New fields on the enum:**

```kotlin
data class PresetRule(val key: String, val value: String)

enum class OnboardingStrictnessPreset(
    val label: String,
    val description: String,
    val collapsedSummary: String,     // shown under label when collapsed
    val rules: List<PresetRule>,      // shown in expanded selected card
    val isRecommended: Boolean = false
)
```

**Values:**

```
FLEXIBLE:
  description = "Gentle support, maximum forgiveness."
  collapsedSummary = "Undo: Unlimited · Snoozes: Unlimited"
  rules = [PresetRule("Undo", "Unlimited"), PresetRule("Snoozes", "Unlimited"), PresetRule("Skips", "Unlimited")]

BALANCED (description copy intentionally updated — approved during design review):
  description = "The middle path. Enough grace to fail, enough structure to win."
  collapsedSummary = "Undo: Within 5 min · Snoozes: 1/day"
  rules = [PresetRule("Undo", "Within 5 min"), PresetRule("Snoozes", "1 / day"), PresetRule("Skips", "2 / month")]

LOCKED:
  description = "No excuses. Full accountability."
  collapsedSummary = "No undo · Skips capped"
  rules = [PresetRule("Undo", "None"), PresetRule("Snoozes", "Capped"), PresetRule("Skips", "Capped")]
```

### 3b. Card visual redesign — `StrictnessStep.kt`

**Collapsed (unselected) card:**

- Background: `surfaceContainerLow` (`MaterialTheme.colorScheme.surfaceContainerLow`)
- Border: 1dp `outlineVariant` at 30% opacity (ghost border)
- Corner radius: 16dp
- Layout: `Row(verticalAlignment = CenterVertically)` →
  `[Icon 22dp] [Column(label + collapsedSummary)] [Spacer(weight=1)] [Icon expand_more 20dp]`
- Icon tints:
  - FLEXIBLE: `Icons.Outlined.EditNote`, tint = `onSurfaceVariant`
  - LOCKED: `Icons.Filled.Lock` (no Outlined variant exists), tint = `error`
- `collapsedSummary` text: `labelSmall`, `onSurfaceVariant`
- The `expand_more` chevron is purely decorative (no separate click target); the whole card is
  clickable as before

**Selected card (any preset):**

- Background: `primaryContainer`
- Corner radius: 24dp
- Drop shadow: `shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp))`
- No border
- Top badge for `isRecommended`: small pill absolutely positioned at `Alignment.TopCenter`,
  offset `y = (-12).dp`, background `primary`, text "RECOMMENDED" in `labelSmall`,
  color `onPrimary` (white in light, dark green in dark — guaranteed contrast), `letterSpacing = 0.12.sp`
- Layout: `Column` →
  - Icon (28dp, `onPrimary`) — see icon mapping below
  - Label: `titleMedium` bold, `onPrimary`
  - Description: `bodySmall`, `onPrimary` at 80% alpha
  - Divider: 1dp horizontal line, `onPrimary` at 10% alpha, `padding(top = 16.dp)`
  - Rules: for each `PresetRule` → `Row(SpaceBetween)` with key (`labelSmall`, `onPrimary` 60%)
    and value (`labelSmall` bold, `onPrimary`); separated by 1dp dividers at `onPrimary` 7% alpha
- Icon tints for selected state: use `onPrimary`
  - FLEXIBLE: `Icons.Outlined.EditNote`
  - BALANCED: `Icons.Filled.Balance`
  - LOCKED: `Icons.Filled.Lock`

**Remove:** `ColorDotFlexible`, `ColorDotBalanced` constants, `dotColor()` extension function, all
`CircleShape` dot `Box` composables, `SuggestionChip` "Recommended" chip (replaced by pill badge).

**Animation:** Keep existing `AnimatedVisibility` for expand/collapse. The card background color
change (unselected ↔ selected) uses `animateColorAsState` with a 200ms tween so the swap is
smooth rather than a hard cut.

---

## 4. First Habit Screen — `FirstHabitStep.kt`

### 4a. Habit name input: `OutlinedTextField` → filled `TextField`

**Current:** `OutlinedTextField` with default outlined style.

**Design:** Filled text field — `surfaceContainerHighest` background, no visible border line,
12dp top corners (M3 filled style).

```kotlin
TextField(
    value = habitName,
    onValueChange = onHabitNameChange,
    label = { Text(stringResource(Res.string.first_habit_label_name)) },
    placeholder = { Text(stringResource(Res.string.common_placeholder_habit_name)) },
    singleLine = true,
    modifier = Modifier.fillMaxWidth(),
    colors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent
    ),
    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
)
```

Apply the same filled style to the quantitative `targetValue` and `unit` text fields inside
`HabitTypeCard`'s `expandedContent`.

### 4b. Habit type cards: icons + updated shape/colors

**Current:** `Card` with 14dp radius, border only, no icon.

**Design:** 24dp radius, icon above label, selected vs unselected background.

**`HabitTypeCard` changes:**

Add `icon: ImageVector` parameter (passed at call site).

```kotlin
// Binary call site
HabitTypeCard(
    icon = Icons.Outlined.CheckCircle,
    ...
)
// Quantitative call site
HabitTypeCard(
    icon = Icons.Outlined.ShowChart,
    ...
)
```

Inside the card `Column`:

```kotlin
Icon(
    imageVector = icon,
    contentDescription = null,
    modifier = Modifier.size(26.dp),
    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
)
Spacer(modifier = Modifier.height(8.dp))
Text(label, ...)          // unchanged
Spacer(modifier = Modifier.height(4.dp))
Text(description, ...)    // unchanged
// expandedContent        // unchanged
```

**Card shape and colors:**

```kotlin
Card(
    shape = RoundedCornerShape(24.dp),
    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer)
             else BorderStroke(2.dp, Color.Transparent),
    colors = CardDefaults.cardColors(
        containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHighest
                         else MaterialTheme.colorScheme.surfaceContainerLow
    ),
    ...
)
```

---

## Files Changed

| File | Change |
|------|--------|
| `OnboardingCta.kt` | Button shape → 12dp, colors → primaryContainer/onPrimary |
| `PhilosophyStep.kt` | Replace lock watermark with architectural background ring |
| `OnboardingStrictnessPreset.kt` | Add `PresetRule` data class, `collapsedSummary`, replace `List<String>` rules |
| `StrictnessStep.kt` | Full card redesign — icons, ghost border, selected primaryContainer card, key-value rows |
| `FirstHabitStep.kt` | OutlinedTextField → filled TextField; type cards add icon + 24dp radius + new colors |

No changes to `OnboardingWizard.kt`, `OnboardingRoute.kt`, `OnboardingState.kt`,
`OnboardingViewModel.kt`, `Theme.kt`, or `Type.kt`.

---

## Out of Scope

- "Rules of the Sanctuary" section: dropped (references non-existent 48h cooling / biometric features)
- Schedule chip shape: `FilterChip` already renders as pill by default — no change needed
- Dark theme: the color tokens used (`primaryContainer`, `onPrimary`, `surfaceContainerHighest`, etc.) resolve correctly in both light and dark via `Theme.kt`. Badge text uses `onPrimary` (not `inversePrimary`) specifically to guarantee contrast in dark theme.
