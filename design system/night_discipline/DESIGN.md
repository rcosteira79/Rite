# Design System: The Stoic Night

## 1. Overview & Creative North Star
The Creative North Star for this design system is **"The Stoic Night."** 

In a world of digital noise, this system acts as a sanctuary of absolute focus. It is designed to feel like an interface carved out of obsidian and stone—heavy, permanent, and intentional. We are moving away from the "generic dark mode" (which often feels like a simple color inversion) and toward a high-end editorial experience. 

To achieve this, we challenge the rigid, boxed-in grid. We use intentional asymmetry, expansive negative space, and a typography scale that favors dramatic contrast. The goal is to make the user feel like they are interacting with a physical object of discipline rather than a flickering screen.

---

## 2. Colors: Tonal Depth & The Obsidian Palette
The palette is rooted in deep earth and minerals. We utilize a monochromatic base to minimize cognitive load, punctuated by life-affirming greens that signify growth and completion.

### Core Tokens
*   **Background (`surface-dim` / `#131313`):** The foundation. A deep, non-pure black that prevents OLED smearing while maintaining a "void-like" focus.
*   **Primary (`primary` / `#A9CFBA`):** A muted Sage used for high-importance actions and secondary indicators. It feels sophisticated, not neon.
*   **Container (`primary-container` / `#2D4F3F`):** Our signature Forest Green, reserved strictly for success states and completed disciplines.
*   **Typography (`tertiary-fixed` / `#E5E2DF`):** An off-white that mimics stone under moonlight, reducing eye strain compared to pure `#FFFFFF`.

### The "No-Line" Rule
**Explicit Instruction:** Prohibit the use of 1px solid borders for sectioning. Boundaries must be defined solely through background color shifts or subtle tonal transitions. 
*   Place a `surface-container-low` card on a `surface` background to create a "ghost" edge. 
*   If a visual break is needed, use vertical whitespace (16px–24px) rather than a horizontal rule.

### Signature Textures & Glass
To provide visual "soul," use **Glassmorphism** for floating elements (like the Bottom Nav). Use `surface-container-highest` at 80% opacity with a `20px` backdrop-blur. This allows the "obsidian" background to bleed through, making the UI feel like a single integrated piece of hardware.

---

## 3. Typography: Architectural Precision
We pair the structural strength of **Manrope** with the high-precision utility of **Inter**.

*   **Display & Headlines (Manrope):** Use `display-lg` (3.5rem) for daily streaks and `headline-lg` (2rem) for habit titles. These should feel architectural—bold, wide, and immovable.
*   **Body & Utility (Inter):** Use `body-md` (0.875rem) for descriptions and `label-sm` (0.6875rem) for metadata. Inter provides the technical clarity needed for tracking data.
*   **The Editorial Edge:** Use dramatic scale shifts. A `display-lg` number sitting next to a `label-md` caption creates a high-end, magazine-style layout that screams "authority."

---

## 4. Elevation & Depth: Tonal Layering
Traditional drop shadows are forbidden. In "The Stoic Night," depth is a result of **Tonal Layering**.

*   **The Layering Principle:** Stacking determines importance. 
    *   Base Level: `surface` (#131313)
    *   Section Level: `surface-container-low` (#1C1B1B)
    *   Interactive Card Level: `surface-container` (#201F1F)
    *   Active/Pressed Level: `surface-container-high` (#2A2A2A)
*   **Ambient Shadows:** If an element must float (e.g., a Modal), use an ultra-diffused shadow: `box-shadow: 0 20px 40px rgba(0,0,0,0.4)`.
*   **The "Ghost Border" Fallback:** If accessibility requires a container edge, use `outline-variant` (#414844) at **15% opacity**. It should be felt, not seen.

---

## 5. Components: Carved Elements

### The Integrated Progress Toolbar
Transitioning from light mode, the toolbar loses its "tacked-on" appearance. 
*   **Surface:** Use `surface-container-lowest` (#0E0E0E) to create a "recessed" look, as if the progress bar is etched into the top of the screen.
*   **Progress Fill:** Use a gradient transition from `primary-container` (#2D4F3F) to `primary` (#A9CFBA) to show "life" returning to the dark interface.

### The Floating Bottom Nav
*   **Visual Style:** A pill-shaped container using `xl` (1.5rem) roundness. 
*   **Material:** Semi-transparent `surface-container-highest` with a heavy backdrop-blur. 
*   **Interaction:** Active icons use the `primary` (#A9CFBA) color with a subtle outer glow (4% opacity) to mimic a soft LED light in the dark.

### Cards & Habit Lists
*   **Spacing:** Use `spacing-6` (1.5rem) between cards to allow the "obsidion" background to breathe.
*   **Content:** No dividers. Separate the Habit Title (`title-lg`) from the frequency (`label-md`) using a `4px` vertical gap.
*   **Completion State:** Upon completion, the card background should smoothly transition from `surface-container` to `primary-container` (#2D4F3F).

### Buttons
*   **Primary:** Solid `primary` (#A9CFBA) with `on-primary` (#143728) text. Use `ROUND_SIXTEEN` for a tactile, pebble-like feel.
*   **Secondary:** Ghost style. No background, no border. Use `tertiary-fixed` text with an icon.

---

## 6. Do’s and Don’ts

### Do
*   **Do** use extreme whitespace to signify focus.
*   **Do** use "Sage" (#A9CFBA) sparingly as a surgical highlight.
*   **Do** ensure all typography meets WCAG AA contrast ratios against the `surface-container` tiers.
*   **Do** use asymmetrical layouts (e.g., left-aligned headers with right-aligned data points) to create a premium feel.

### Don’t
*   **Don’t** use pure black (#000000) or pure white (#FFFFFF). 
*   **Don’t** use 1px lines to separate list items; let the space do the work.
*   **Don’t** use "Pop" colors like bright red or electric blue. If an error occurs, use the muted `error` token (#FFB4AB).
*   **Don’t** use standard "Material Design" shadows. If it doesn't look like it's made of stone or glass, it doesn't belong.