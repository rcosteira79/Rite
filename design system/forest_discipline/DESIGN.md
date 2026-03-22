# Design System Document: Tactical Serenity

## 1. Overview & Creative North Star: "The Architectural Sanctuary"
This design system is built upon the principle of **The Architectural Sanctuary**. Unlike typical habit-tracking apps that rely on dopamine-fueled gamification and neon "streaks," this system embraces discipline through calmness. It is an editorial space that feels more like a physical wellness retreat or a high-end stationery set than a digital interface.

To break the "template" look, we move away from the rigid, centered grid. We utilize **intentional asymmetry**—aligning heavy typography to the left while allowing breathing room to the right—and **layered surfaces** that mimic the stacking of fine, handmade paper. The goal is a premium, tactile experience where every interaction feels weighted and significant.

---

## 2. Colors: Tonal Depth & The "No-Line" Rule
The palette is grounded in the earth. It avoids synthetic vibrance in favor of sophisticated, desaturated tones.

### The Palette
*   **Primary (#163829):** Our anchor. Used for the highest level of brand authority.
*   **Primary-Container (#2D4F3F):** Our "Forest Success" color. Reserved for completed habits and states of achievement. It is quiet, not loud.
*   **Secondary (#545F72):** A muted slate for secondary actions and utility, providing a cool contrast to the warm neutrals.
*   **Surface & Background (#FDF9F6):** A warm, off-white "linen" base that prevents screen fatigue.

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders for sectioning content. Boundaries must be defined solely through background color shifts.
*   *Example:* A `surface-container-low` (#F7F3F0) card sitting on a `surface` (#FDF9F6) background.
*   Traditional dividers are replaced by 1.4rem (`spacing.4`) of white space or a subtle shift to `surface-variant` (#E5E2DF).

### Signature Textures & Glass
To provide "visual soul," use subtle linear gradients (e.g., `primary` to `primary-container` at a 145-degree angle) for hero components. For floating navigation or modals, employ **Glassmorphism**: use `surface` with 80% opacity and a 20px backdrop-blur to allow underlying content to bleed through softly.

---

## 3. Typography: Architectural Discipline
We pair two sans-serifs to create a hierarchy that feels both modern and permanent.

*   **Display & Headlines (Manrope):** Chosen for its geometric, architectural structure.
    *   *Display-LG (3.5rem):* Used sparingly for emotional milestones.
    *   *Headline-MD (1.75rem):* The standard for screen titles.
*   **Title & Body (Inter):** Chosen for its legendary legibility and "workhorse" discipline.
    *   *Title-LG (1.375rem):* Used for habit names in list views.
    *   *Body-MD (0.875rem):* The default for all instructional text.
*   **Hierarchy Note:** Use high-contrast scales. A `display-sm` headline should often be paired with a `label-md` subheader to create an editorial, "high-fashion" layout feel.

---

## 4. Elevation & Depth: Tonal Layering
We reject the heavy drop-shadows of the 2010s. Depth is achieved through the **Layering Principle**.

*   **Surface Stacking:** To lift a component, move it one step up the container ladder. Place a `surface-container-highest` (#E5E2DF) element inside a `surface-container-low` (#F7F3F0) area to create natural focus.
*   **Ambient Shadows:** If a floating element (like a FAB or Popover) requires a shadow, it must be the "Ambient" style: `Y: 8px, Blur: 24px, Spread: -2px`. The color should be a tinted version of `on-surface` at 6% opacity.
*   **The "Ghost Border" Fallback:** If a container lacks sufficient contrast on certain displays, use a 1px border with `outline-variant` (#C1C8C2) at **15% opacity**. It should be felt, not seen.

---

## 5. Components

### Buttons
*   **Primary:** `primary-container` (#2D4F3F) background with `on-primary` text. **Roundedness: 12px (md)**. No border.
*   **Secondary:** `secondary-container` (#D5E0F7) background. Use for "Edit" or "Skip" actions.
*   **Tertiary:** Ghost style. No background, `on-surface-variant` text.

### Cards & Lists
*   **Habit Cards:** Use `surface-container-low` (#F7F3F0). Use **1.5rem (xl)** corner radius for a "soft pebble" feel.
*   **Forbidden:** Do not use horizontal lines between list items. Use `spacing.3` (1rem) of vertical padding to define the rhythm.

### Input Fields
*   **Text Inputs:** Background should be `surface-container-highest`. Use `title-sm` (Inter) for input text to maintain the "Architectural" feel. The cursor/caret should always be the `primary` forest green.

### Progress Indicators
*   **The Tactile Ring:** Use a thick stroke (4px+) for progress circles. The background track should be `surface-variant`, and the progress fill should be a gradient of `primary` to `primary-container`.

---

## 6. Do’s and Don’ts

### Do:
*   **DO** use asymmetric padding. For example, give a header more padding on the left (2.75rem / `spacing.8`) than on the right (1.4rem / `spacing.4`) to create an editorial look.
*   **DO** use the `primary-fixed-dim` (#A9CFBA) for "Success" states that shouldn't overwhelm the eye.
*   **DO** embrace white space. If a screen feels "empty," it is working.

### Don't:
*   **DON'T** use pure black (#000000) for text. Always use `on-surface` (#1C1B1A) to maintain the "Soft Earthy" feel.
*   **DON'T** use 100% opaque borders. They interrupt the flow of the "Sanctuary."
*   **DON'T** use "Spring" or "Neon" greens. If the green looks like it belongs on a high-vis vest, it is wrong for this system.