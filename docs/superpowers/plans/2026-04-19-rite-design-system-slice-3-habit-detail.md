# Rite Design System — Slice 3 (Habit Detail) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> **Before starting any task, load these skills in addition to the required sub-skill:**
> - `android-skills:android-dev` — senior Android engineering knowledge
> - `android-skills:compose` — Compose/CMP idioms, state hoisting, previews
> - `android-skills:android-tdd` — fake-first strategy, coroutine testing, Compose UI testing
>
> Load via `Skill` tool at task start.

**Goal:** Rebuild Habit Detail on Rite v2 primitives — extract three net-new primitives (`CategoryEyebrow`, `StatTileRow`, `EnforcementLimitsTable`), rename + polish `HeatmapGrid` → `Tapestry`, extract `HabitDetailAction`, expand the UiModel + ViewModel for strictness / snooze / weekly-skip data, move `HabitDetailRoute` into `HabitDetailScreen` as a public overload, and re-assemble the screen.

**Architecture:** Screen-local primitives under `ui/habitdetail/components/`. UI models under `ui/habitdetail/models/`. `HabitDetailState.kt` at top-level, alongside `HabitDetailViewModel.kt` and `HabitDetailScreen.kt`. Public `HabitDetailScreen` overload replaces `HabitDetailRoute`, mirroring `TodayScreen`. Per-primitive roborazzi goldens; per-variant screen goldens. TDD for VM expansion and pure helpers.

**Tech Stack:** Kotlin Multiplatform 2.3.0, Compose Multiplatform 1.10.0, Material 3, Roborazzi 1.20.0, Robolectric, kotlin-inject, SQLDelight, kotlinx-datetime.

**Source of truth (spec):** `docs/superpowers/specs/2026-04-19-rite-design-system-slice-3-habit-detail-design.md`

**Worktree:** `.worktrees/design-system-habit-detail` (branch `feature/design-system-v2-03-habit-detail`, off `origin/feature/design-system-v2-02-today-habitcard`). All commands run from inside the worktree.

---

## Task 1: Add `CategoryEyebrow` primitive

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/CategoryEyebrow.kt`
- Create: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/CategoryEyebrowScreenshotTest.kt`

Existing string resources (`habit_detail_category_binary`, `habit_detail_category_quantitative`) already carry the correct copy. No new strings.

- [ ] **Step 1: Create the primitive**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.habit_detail_category_binary
import rite.composeapp.generated.resources.habit_detail_category_quantitative

@Composable
fun CategoryEyebrow(
    type: HabitType,
    modifier: Modifier = Modifier,
) {
    val text: String = when (type) {
        HabitType.BINARY -> stringResource(Res.string.habit_detail_category_binary)
        HabitType.QUANTITATIVE -> stringResource(Res.string.habit_detail_category_quantitative)
    }
    Text(
        text = text,
        style = RiteAppTheme.typography.eyebrow,
        color = RiteAppTheme.colors.onSurfaceMuted,
        modifier = modifier,
    )
}
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileDebugSources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Create the screenshot test**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.domain.models.HabitType
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
class CategoryEyebrowScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun binary_light() = render(HabitType.BINARY, darkTheme = false)
    @Test fun binary_dark() = render(HabitType.BINARY, darkTheme = true)
    @Test fun quantitative_light() = render(HabitType.QUANTITATIVE, darkTheme = false)
    @Test fun quantitative_dark() = render(HabitType.QUANTITATIVE, darkTheme = true)

    private fun render(type: HabitType, darkTheme: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = darkTheme) {
                CategoryEyebrow(type = type, modifier = Modifier.padding(16.dp))
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
```

- [ ] **Step 4: Record goldens**

Run: `./gradlew :composeApp:recordRoborazziDebug --tests "*CategoryEyebrowScreenshotTest*"`
Expected: BUILD SUCCESSFUL, 4 new PNGs under `composeApp/src/androidUnitTest/snapshots/images/`

- [ ] **Step 5: Verify goldens pass**

Run: `./gradlew :composeApp:verifyRoborazziDebug --tests "*CategoryEyebrowScreenshotTest*"`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/CategoryEyebrow.kt \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/CategoryEyebrowScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/com.ricardocosteira.rite.presentation.ui.habitdetail.components.CategoryEyebrowScreenshotTest*.png
git commit -m "feat(habit-detail): add CategoryEyebrow primitive"
```

---

## Task 2: Add `StatTileRow` primitive

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/StatTileRow.kt`
- Create: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/StatTileRowScreenshotTest.kt`

Uses existing strings: `habit_detail_stat_current_streak`, `habit_detail_stat_longest_streak`, `habit_detail_stat_habit_score`, `habit_detail_stat_days`. Plus a new unit suffix: `/100` (derived inline; no string resource).

- [ ] **Step 1: Create the primitive**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.habit_detail_stat_current_streak
import rite.composeapp.generated.resources.habit_detail_stat_days
import rite.composeapp.generated.resources.habit_detail_stat_habit_score
import rite.composeapp.generated.resources.habit_detail_stat_longest_streak

@Composable
fun StatTileRow(
    currentStreak: Int,
    longestStreak: Int,
    habitScore: Int,
    modifier: Modifier = Modifier,
) {
    val ruleColor = RiteAppTheme.colors.outline
    val ruleThicknessPx: Float = with(androidx.compose.ui.platform.LocalDensity.current) { 1.dp.toPx() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(color = ruleColor, topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(size.width, ruleThicknessPx))
                drawRect(color = ruleColor, topLeft = Offset(0f, size.height - ruleThicknessPx), size = androidx.compose.ui.geometry.Size(size.width, ruleThicknessPx))
            }
    ) {
        StatTile(
            label = stringResource(Res.string.habit_detail_stat_current_streak),
            value = currentStreak.toString(),
            unit = stringResource(Res.string.habit_detail_stat_days),
            modifier = Modifier
                .weight(1f)
                .drawBehind {
                    drawRect(color = ruleColor, topLeft = Offset(size.width - ruleThicknessPx, 0f), size = androidx.compose.ui.geometry.Size(ruleThicknessPx, size.height))
                },
        )
        StatTile(
            label = stringResource(Res.string.habit_detail_stat_longest_streak),
            value = longestStreak.toString(),
            unit = stringResource(Res.string.habit_detail_stat_days),
            modifier = Modifier
                .weight(1f)
                .drawBehind {
                    drawRect(color = ruleColor, topLeft = Offset(size.width - ruleThicknessPx, 0f), size = androidx.compose.ui.geometry.Size(ruleThicknessPx, size.height))
                },
        )
        StatTile(
            label = stringResource(Res.string.habit_detail_stat_habit_score),
            value = habitScore.toString(),
            unit = "/100",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
        Text(
            text = label.uppercase(),
            style = RiteAppTheme.typography.eyebrow,
            color = RiteAppTheme.colors.onSurfaceMuted,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = buildAnnotatedString {
                append(value)
                withStyle(
                    SpanStyle(
                        fontFamily = RiteAppTheme.typography.mono.fontFamily,
                        fontSize = 11.sp,
                        color = RiteAppTheme.colors.onSurfaceMuted,
                    )
                ) {
                    append(" ")
                    append(unit.uppercase())
                }
            },
            style = RiteAppTheme.typography.displaySmall.copy(
                fontSize = 30.sp,
                lineHeight = 30.sp,
                letterSpacing = (-0.6).sp,
            ),
            color = RiteAppTheme.colors.onSurface,
        )
    }
}
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileDebugSources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Create the screenshot test**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail.components

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
class StatTileRowScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun typical_light() = render(darkTheme = false)
    @Test fun typical_dark() = render(darkTheme = true)

    private fun render(darkTheme: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = darkTheme) {
                StatTileRow(
                    currentStreak = 14,
                    longestStreak = 42,
                    habitScore = 82,
                    modifier = Modifier.padding(horizontal = 22.dp),
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
```

- [ ] **Step 4: Record goldens**

Run: `./gradlew :composeApp:recordRoborazziDebug --tests "*StatTileRowScreenshotTest*"`
Expected: 2 new PNGs under `snapshots/images/`

- [ ] **Step 5: Verify goldens pass**

Run: `./gradlew :composeApp:verifyRoborazziDebug --tests "*StatTileRowScreenshotTest*"`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/StatTileRow.kt \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/StatTileRowScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/com.ricardocosteira.rite.presentation.ui.habitdetail.components.StatTileRowScreenshotTest*.png
git commit -m "feat(habit-detail): add StatTileRow primitive"
```

---

## Task 3: Rename `HeatmapGrid` → `Tapestry`, add compound header + week range helper

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/IsoWeek.kt`
- Create: `composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/IsoWeekTest.kt`
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/Tapestry.kt`
- Delete: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HeatmapGrid.kt` (after moving logic)
- Modify: `composeApp/src/commonMain/composeResources/values/strings_habit_detail.xml` (replace title string)

- [ ] **Step 1: Write failing test for `IsoWeek` helper**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class IsoWeekTest {
    @Test
    fun `given a Monday, isoWeekNumber returns its week`() {
        val mon = LocalDate(2026, 4, 13) // Monday, ISO week 16
        assertEquals(16, isoWeekNumber(mon))
    }

    @Test
    fun `given a Sunday, isoWeekNumber returns the same week as previous Monday`() {
        val sun = LocalDate(2026, 4, 19) // Sunday, still ISO week 16
        assertEquals(16, isoWeekNumber(sun))
    }

    @Test
    fun `given Jan 1 2026 (Thursday), isoWeekNumber returns 1`() {
        val jan1 = LocalDate(2026, 1, 1) // Thursday, ISO week 1 (since Thursday is in that week)
        assertEquals(1, isoWeekNumber(jan1))
    }

    @Test
    fun `given Jan 1 2023 (Sunday), isoWeekNumber returns 52 (prior year's last week)`() {
        val jan1 = LocalDate(2023, 1, 1) // Sunday, ISO week 52 of 2022
        assertEquals(52, isoWeekNumber(jan1))
    }

    @Test
    fun `formatWeekRange formats start to end`() {
        val from = LocalDate(2026, 1, 26) // ISO week 5
        val to = LocalDate(2026, 4, 19)   // ISO week 16
        assertEquals("W16 — W05", formatWeekRange(from = from, to = to))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :composeApp:jvmTest --tests "*IsoWeekTest*"`
Expected: FAIL — `isoWeekNumber` / `formatWeekRange` unresolved.

- [ ] **Step 3: Implement `IsoWeek` helper**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Returns the ISO-8601 week number (1..53) for the given date.
 * ISO week 1 is the week containing the first Thursday of the year.
 */
fun isoWeekNumber(date: LocalDate): Int {
    // Shift the date forward to the nearest Thursday (ISO definition).
    val daysToThursday: Int = (DayOfWeek.THURSDAY.ordinal - date.dayOfWeek.ordinal)
    val thursday: LocalDate = if (daysToThursday == 0) date
    else if (daysToThursday > 0) date.plus(DatePeriod(days = daysToThursday))
    else date.minus(DatePeriod(days = -daysToThursday))

    // The week number is the thursday's day-of-year divided into 7-day windows, starting
    // from the first Thursday of the year.
    val yearStart = LocalDate(thursday.year, 1, 1)
    val firstThursdayOffset: Int = ((DayOfWeek.THURSDAY.ordinal - yearStart.dayOfWeek.ordinal) + 7) % 7
    val firstThursday: LocalDate = yearStart.plus(DatePeriod(days = firstThursdayOffset))
    val daysBetween: Int = thursday.toEpochDays().toInt() - firstThursday.toEpochDays().toInt()
    return (daysBetween / 7) + 1
}

/**
 * Formats a date range as "W<end> — W<start>" (end first because the tapestry reads
 * right-to-newest).
 */
fun formatWeekRange(from: LocalDate, to: LocalDate): String {
    val startWeek: Int = isoWeekNumber(from)
    val endWeek: Int = isoWeekNumber(to)
    fun pad(n: Int): String = n.toString().padStart(2, '0')
    return "W${pad(endWeek)} — W${pad(startWeek)}"
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `./gradlew :composeApp:jvmTest --tests "*IsoWeekTest*"`
Expected: PASS

- [ ] **Step 5: Update strings_habit_detail.xml**

Replace the existing `habit_detail_heatmap_title` with:

```xml
<string name="habit_detail_heatmap_title">Last 3 months · Tapestry</string>
```

- [ ] **Step 6: Create `Tapestry.kt` (ported from HeatmapGrid)**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.presentation.ui.habitdetail.HeatmapDay
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import com.ricardocosteira.rite.util.todayIn
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.habit_detail_heatmap_best_effort
import rite.composeapp.generated.resources.habit_detail_heatmap_failed
import rite.composeapp.generated.resources.habit_detail_heatmap_partial
import rite.composeapp.generated.resources.habit_detail_heatmap_perfect
import rite.composeapp.generated.resources.habit_detail_heatmap_skipped
import rite.composeapp.generated.resources.habit_detail_heatmap_title

private val CELL_GAP = 3.dp
private val CELL_CORNER = 2.dp
private val DAY_LABEL_WIDTH = 16.dp
private val DAY_LABEL_SPACING = 4.dp
private val LEGEND_CELL_SIZE = 10.dp

private val DAY_LABELS: List<Pair<DayOfWeek, String>> = listOf(
    DayOfWeek.MONDAY to "M",
    DayOfWeek.TUESDAY to "",
    DayOfWeek.WEDNESDAY to "W",
    DayOfWeek.THURSDAY to "",
    DayOfWeek.FRIDAY to "F",
    DayOfWeek.SATURDAY to "",
    DayOfWeek.SUNDAY to "S"
)

@Composable
fun Tapestry(
    heatmapData: List<HeatmapDay>,
    weekRangeLabel: String,
    modifier: Modifier = Modifier,
) {
    val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val startDate: LocalDate = today.minus(DatePeriod(days = 90))
    val dataByDate: Map<String, HeatmapDay> = heatmapData.associateBy { it.date }
    val weeks: List<List<LocalDate?>> = buildWeeks(startDate, today)
    val weekCount: Int = weeks.size

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.size(width = Dp.Unspecified, height = 16.dp).also {}.let { Modifier },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.habit_detail_heatmap_title),
                style = RiteAppTheme.typography.eyebrow,
                color = RiteAppTheme.colors.onSurfaceMuted,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = weekRangeLabel,
                style = RiteAppTheme.typography.eyebrow.copy(
                    letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified.takeOrElse { 0.14.sp }
                ),
                color = RiteAppTheme.colors.onSurfaceSubtle,
            )
        }
        Spacer(Modifier.size(12.dp))

        BoxWithConstraints {
            val availableWidth: Dp = maxWidth
            val labelSpace: Dp = DAY_LABEL_WIDTH + DAY_LABEL_SPACING
            val totalGaps: Dp = CELL_GAP * (weekCount - 1)
            val cellSize: Dp = (availableWidth - labelSpace - totalGaps) / weekCount

            Row {
                Column(verticalArrangement = Arrangement.spacedBy(CELL_GAP)) {
                    DAY_LABELS.forEach { (_, label) ->
                        Box(
                            modifier = Modifier.size(width = DAY_LABEL_WIDTH, height = cellSize),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (label.isNotEmpty()) {
                                Text(
                                    text = label,
                                    style = RiteAppTheme.typography.labelSmall,
                                    color = RiteAppTheme.colors.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.width(DAY_LABEL_SPACING))

                Row(horizontalArrangement = Arrangement.spacedBy(CELL_GAP)) {
                    weeks.forEach { week ->
                        Column(verticalArrangement = Arrangement.spacedBy(CELL_GAP)) {
                            week.forEach { date ->
                                if (date == null) {
                                    Box(modifier = Modifier.size(cellSize))
                                } else {
                                    TapestryCell(
                                        day = dataByDate[date.toString()],
                                        size = cellSize,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.size(12.dp))
        TapestryLegend(cellSize = LEGEND_CELL_SIZE)
    }
}

@Composable
private fun TapestryCell(day: HeatmapDay?, size: Dp, modifier: Modifier = Modifier) {
    val colorScheme = RiteAppTheme.colors
    val color: Color = when {
        day == null -> colorScheme.dayNone
        day.status == HabitStatus.FAILED -> colorScheme.dayFailed
        day.status == HabitStatus.SKIPPED -> colorScheme.daySkipped
        day.status == HabitStatus.SUSPENDED -> colorScheme.dayNone
        day.completionPercentage >= 1.0f -> colorScheme.dayPerfect
        day.completionPercentage >= 0.5f -> colorScheme.dayBestEffort
        day.completionPercentage > 0f -> colorScheme.dayPartial
        else -> colorScheme.dayNone
    }
    Box(
        modifier = modifier
            .size(size)
            .background(color = color, shape = RoundedCornerShape(CELL_CORNER))
    )
}

@Composable
private fun TapestryLegend(cellSize: Dp, modifier: Modifier = Modifier) {
    val colorScheme = RiteAppTheme.colors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(colorScheme.dayPerfect, stringResource(Res.string.habit_detail_heatmap_perfect), cellSize)
        LegendItem(colorScheme.dayBestEffort, stringResource(Res.string.habit_detail_heatmap_best_effort), cellSize)
        LegendItem(colorScheme.dayPartial, stringResource(Res.string.habit_detail_heatmap_partial), cellSize)
        LegendItem(colorScheme.dayFailed, stringResource(Res.string.habit_detail_heatmap_failed), cellSize)
        LegendItem(colorScheme.daySkipped, stringResource(Res.string.habit_detail_heatmap_skipped), cellSize)
    }
}

@Composable
private fun LegendItem(color: Color, label: String, size: Dp, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(color = color, shape = RoundedCornerShape(CELL_CORNER))
        )
        Text(
            text = label,
            style = RiteAppTheme.typography.labelSmall,
            color = RiteAppTheme.colors.onSurfaceVariant,
        )
    }
}

private fun buildWeeks(startDate: LocalDate, endDate: LocalDate): List<List<LocalDate?>> {
    val weeks: MutableList<List<LocalDate?>> = mutableListOf()
    var weekStart: LocalDate = startDate
    while (weekStart.dayOfWeek != DayOfWeek.MONDAY) {
        weekStart = weekStart.minus(DatePeriod(days = 1))
    }
    while (weekStart <= endDate) {
        val week: MutableList<LocalDate?> = mutableListOf()
        for (dayOffset in 0..6) {
            val date: LocalDate = weekStart.plus(DatePeriod(days = dayOffset))
            week.add(if (date in startDate..endDate) date else null)
        }
        weeks.add(week)
        weekStart = weekStart.plus(DatePeriod(days = 7))
    }
    return weeks
}

private fun androidx.compose.ui.unit.TextUnit.takeOrElse(block: () -> androidx.compose.ui.unit.TextUnit): androidx.compose.ui.unit.TextUnit =
    if (this == androidx.compose.ui.unit.TextUnit.Unspecified) block() else this
```

- [ ] **Step 7: Delete the old `HeatmapGrid.kt`**

```bash
git rm composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HeatmapGrid.kt
```

- [ ] **Step 8: Temporarily update `HabitDetailScreen.kt` to compile**

In `HabitDetailScreen.kt`, find the `HeatmapGrid(...)` call (one occurrence) and replace it with:

```kotlin
Tapestry(
    heatmapData = state.heatmapData,
    weekRangeLabel = "", // filled in properly in task 8
    modifier = Modifier.fillMaxWidth()
)
```

Update the import: replace `import com.ricardocosteira.rite.presentation.ui.habitdetail.HeatmapGrid` (if present) with `import com.ricardocosteira.rite.presentation.ui.habitdetail.components.Tapestry`. Run `./gradlew :composeApp:compileDebugSources` — expected BUILD SUCCESSFUL.

- [ ] **Step 9: Create the `Tapestry` screenshot test**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.presentation.ui.habitdetail.HeatmapDay
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w360dp-h800dp-420dpi", application = android.app.Application::class)
class TapestryScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun populated_light() = render(populatedData(), darkTheme = false)
    @Test fun populated_dark() = render(populatedData(), darkTheme = true)
    @Test fun empty_light() = render(emptyList(), darkTheme = false)

    private fun render(data: List<HeatmapDay>, darkTheme: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = darkTheme) {
                Tapestry(
                    heatmapData = data,
                    weekRangeLabel = "W16 — W05",
                    modifier = Modifier.padding(horizontal = 22.dp),
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    private fun populatedData(): List<HeatmapDay> {
        val today = LocalDate(2026, 4, 19)
        return (0..89).map { offset ->
            val date = today.minus(DatePeriod(days = offset))
            val bucket = offset % 7
            val (pct, status) = when (bucket) {
                0, 1, 2 -> 1.0f to HabitStatus.COMPLETED
                3 -> 0.6f to HabitStatus.PENDING
                4 -> 0.3f to HabitStatus.PENDING
                5 -> 0.0f to HabitStatus.SKIPPED
                else -> 0.0f to HabitStatus.FAILED
            }
            HeatmapDay(date = date.toString(), completionPercentage = pct, status = status)
        }
    }
}
```

- [ ] **Step 10: Record and verify goldens**

Run: `./gradlew :composeApp:recordRoborazziDebug --tests "*TapestryScreenshotTest*"`
Expected: 3 new PNGs.

Run: `./gradlew :composeApp:verifyRoborazziDebug --tests "*TapestryScreenshotTest*"`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 11: Delete the old `HeatmapGridScreenshotTest` (if present)**

Check for a previous golden file under `snapshots/images/` for `HeatmapGridScreenshotTest` — delete both the test file (if it exists; the Slice 2 tip did not include one) and the stale snapshots.

- [ ] **Step 12: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/IsoWeek.kt \
        composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/IsoWeekTest.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/Tapestry.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailScreen.kt \
        composeApp/src/commonMain/composeResources/values/strings_habit_detail.xml \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/TapestryScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/com.ricardocosteira.rite.presentation.ui.habitdetail.components.TapestryScreenshotTest*.png
git commit -m "feat(habit-detail): rename HeatmapGrid → Tapestry, compound header + week range"
```

---

## Task 4: Add `EnforcementLimitsTable` primitive

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/EnforcementLimitsTable.kt`
- Create: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/EnforcementLimitsTableScreenshotTest.kt`
- Modify: `composeApp/src/commonMain/composeResources/values/strings_habit_detail.xml` (add rows/values)

- [ ] **Step 1: Add string resources**

Append inside `<resources>`:

```xml
<string name="habit_detail_enf_row_strictness">Strictness</string>
<string name="habit_detail_enf_row_undo">Undo</string>
<string name="habit_detail_enf_row_snoozes">Snoozes</string>
<string name="habit_detail_enf_row_skips">Skips</string>
<string name="habit_detail_enf_row_consecutive">Consecutive</string>
<string name="habit_detail_enf_strictness_flexible">Flexible</string>
<string name="habit_detail_enf_strictness_balanced">Balanced</string>
<string name="habit_detail_enf_strictness_unwavering">Unwavering</string>
<string name="habit_detail_enf_strictness_custom">Custom</string>
<string name="habit_detail_enf_undo_all_history">All history</string>
<string name="habit_detail_enf_undo_today_only">Today only</string>
<string name="habit_detail_enf_undo_disabled">Disabled</string>
<string name="habit_detail_enf_snoozes_used">%1$d / %2$d used today</string>
<string name="habit_detail_enf_snoozes_unlimited">Unlimited</string>
<string name="habit_detail_enf_skips_this_week">%1$d this week</string>
<string name="habit_detail_enf_consecutive_of_max">%1$d / max %2$d</string>
<string name="habit_detail_enf_consecutive_unlimited">Unlimited</string>
```

- [ ] **Step 2: Create the primitive**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.domain.models.StrictnessPreset
import com.ricardocosteira.rite.domain.models.UndoPolicy
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.habit_detail_enf_consecutive_of_max
import rite.composeapp.generated.resources.habit_detail_enf_consecutive_unlimited
import rite.composeapp.generated.resources.habit_detail_enf_row_consecutive
import rite.composeapp.generated.resources.habit_detail_enf_row_skips
import rite.composeapp.generated.resources.habit_detail_enf_row_snoozes
import rite.composeapp.generated.resources.habit_detail_enf_row_strictness
import rite.composeapp.generated.resources.habit_detail_enf_row_undo
import rite.composeapp.generated.resources.habit_detail_enf_skips_this_week
import rite.composeapp.generated.resources.habit_detail_enf_snoozes_unlimited
import rite.composeapp.generated.resources.habit_detail_enf_snoozes_used
import rite.composeapp.generated.resources.habit_detail_enf_strictness_balanced
import rite.composeapp.generated.resources.habit_detail_enf_strictness_custom
import rite.composeapp.generated.resources.habit_detail_enf_strictness_flexible
import rite.composeapp.generated.resources.habit_detail_enf_strictness_unwavering
import rite.composeapp.generated.resources.habit_detail_enf_undo_all_history
import rite.composeapp.generated.resources.habit_detail_enf_undo_disabled
import rite.composeapp.generated.resources.habit_detail_enf_undo_today_only

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
) {
    val colors = RiteAppTheme.colors
    val ruleColor: Color = colors.outline
    val isLocked: Boolean = maxConsecutiveSkips != null && currentConsecutiveSkips >= maxConsecutiveSkips

    val rows: List<EnforcementRow> = listOf(
        EnforcementRow(
            label = stringResource(Res.string.habit_detail_enf_row_strictness),
            value = when (strictnessPreset) {
                StrictnessPreset.FLEXIBLE -> stringResource(Res.string.habit_detail_enf_strictness_flexible)
                StrictnessPreset.BALANCED -> stringResource(Res.string.habit_detail_enf_strictness_balanced)
                StrictnessPreset.UNWAVERING -> stringResource(Res.string.habit_detail_enf_strictness_unwavering)
                null -> stringResource(Res.string.habit_detail_enf_strictness_custom)
            },
        ),
        EnforcementRow(
            label = stringResource(Res.string.habit_detail_enf_row_undo),
            value = when (undoPolicy) {
                UndoPolicy.ALL_HISTORY -> stringResource(Res.string.habit_detail_enf_undo_all_history)
                UndoPolicy.TODAY_ONLY -> stringResource(Res.string.habit_detail_enf_undo_today_only)
                UndoPolicy.NONE -> stringResource(Res.string.habit_detail_enf_undo_disabled)
            },
        ),
        EnforcementRow(
            label = stringResource(Res.string.habit_detail_enf_row_snoozes),
            value = when (val max: Int? = maxSnoozesPerDay) {
                null -> stringResource(Res.string.habit_detail_enf_snoozes_unlimited)
                else -> stringResource(Res.string.habit_detail_enf_snoozes_used, snoozesUsedToday, max)
            },
        ),
        EnforcementRow(
            label = stringResource(Res.string.habit_detail_enf_row_skips),
            value = stringResource(Res.string.habit_detail_enf_skips_this_week, skipsThisWeek),
        ),
        EnforcementRow(
            label = stringResource(Res.string.habit_detail_enf_row_consecutive),
            value = when (val max: Int? = maxConsecutiveSkips) {
                null -> stringResource(Res.string.habit_detail_enf_consecutive_unlimited)
                else -> stringResource(Res.string.habit_detail_enf_consecutive_of_max, currentConsecutiveSkips, max)
            },
            valueColor = if (isLocked) colors.suspend else colors.onSurface,
        ),
    )

    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = ruleColor, shape = RoundedCornerShape(4.dp))
            .background(color = colors.surface, shape = RoundedCornerShape(4.dp)),
    ) {
        rows.forEachIndexed { index, row ->
            EnforcementRowLine(row = row)
            if (index != rows.lastIndex) {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(ruleColor)
                )
            }
        }
    }
}

private data class EnforcementRow(
    val label: String,
    val value: String,
    val valueColor: Color? = null,
)

@Composable
private fun EnforcementRowLine(row: EnforcementRow, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.label,
            style = RiteAppTheme.typography.bodySmall,
            color = RiteAppTheme.colors.onSurfaceMuted,
        )
        Text(
            text = row.value,
            style = RiteAppTheme.typography.titleMedium.copy(fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp)),
            color = row.valueColor ?: RiteAppTheme.colors.onSurface,
        )
    }
}
```

- [ ] **Step 3: Build to verify**

Run: `./gradlew :composeApp:compileDebugSources`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Create the screenshot test**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.domain.models.StrictnessPreset
import com.ricardocosteira.rite.domain.models.UndoPolicy
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
class EnforcementLimitsTableScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun flexible_light() = render(StrictnessPreset.FLEXIBLE, UndoPolicy.ALL_HISTORY, null, null, 0, 0, null, darkTheme = false)
    @Test fun flexible_dark() = render(StrictnessPreset.FLEXIBLE, UndoPolicy.ALL_HISTORY, null, null, 0, 0, null, darkTheme = true)
    @Test fun balanced_light() = render(StrictnessPreset.BALANCED, UndoPolicy.TODAY_ONLY, 1, 3, 2, 0, 2, darkTheme = false)
    @Test fun balanced_dark() = render(StrictnessPreset.BALANCED, UndoPolicy.TODAY_ONLY, 1, 3, 2, 0, 2, darkTheme = true)
    @Test fun unwavering_light() = render(StrictnessPreset.UNWAVERING, UndoPolicy.NONE, 1, 1, 0, 0, 0, darkTheme = false)
    @Test fun unwavering_dark() = render(StrictnessPreset.UNWAVERING, UndoPolicy.NONE, 1, 1, 0, 0, 0, darkTheme = true)
    @Test fun locked_light() = render(StrictnessPreset.BALANCED, UndoPolicy.TODAY_ONLY, 3, 3, 1, 2, 2, darkTheme = false)

    private fun render(
        preset: StrictnessPreset?,
        undo: UndoPolicy,
        snoozesUsed: Int?,
        maxSnoozes: Int?,
        skipsWeek: Int,
        consecUsed: Int,
        consecMax: Int?,
        darkTheme: Boolean,
    ) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = darkTheme) {
                EnforcementLimitsTable(
                    strictnessPreset = preset,
                    undoPolicy = undo,
                    snoozesUsedToday = snoozesUsed ?: 0,
                    maxSnoozesPerDay = maxSnoozes,
                    skipsThisWeek = skipsWeek,
                    currentConsecutiveSkips = consecUsed,
                    maxConsecutiveSkips = consecMax,
                    modifier = Modifier.padding(horizontal = 22.dp),
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
```

- [ ] **Step 5: Record and verify goldens**

Run: `./gradlew :composeApp:recordRoborazziDebug --tests "*EnforcementLimitsTableScreenshotTest*"`
Expected: 7 new PNGs.

Run: `./gradlew :composeApp:verifyRoborazziDebug --tests "*EnforcementLimitsTableScreenshotTest*"`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/EnforcementLimitsTable.kt \
        composeApp/src/commonMain/composeResources/values/strings_habit_detail.xml \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/EnforcementLimitsTableScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/com.ricardocosteira.rite.presentation.ui.habitdetail.components.EnforcementLimitsTableScreenshotTest*.png
git commit -m "feat(habit-detail): add EnforcementLimitsTable primitive"
```

---

## Task 5: Add `HabitDetailAction` primitive

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/HabitDetailAction.kt`
- Create: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/HabitDetailActionScreenshotTest.kt`

No new strings — reuses existing `habit_detail_action_*` resources.

- [ ] **Step 1: Create the primitive**

Extract the existing `ActionButtons` / `BinaryActions` / `QuantitativeActions` logic from `HabitDetailScreen.kt` into this new file, with flat parameters (no `HabitDetailUiModel` dependency per the "minimal component params" convention):

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.components.PrimaryButton
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.habit_detail_action_complete
import rite.composeapp.generated.resources.habit_detail_action_custom
import rite.composeapp.generated.resources.habit_detail_action_skip
import rite.composeapp.generated.resources.habit_detail_action_undo
import rite.composeapp.generated.resources.habit_detail_action_undo_last

private val CARD_CORNER = 16.dp
private val STEPPER_BUTTON_SIZE = 48.dp

@Composable
fun HabitDetailAction(
    type: HabitType,
    status: HabitStatus,
    currentProgress: Int,
    unit: String?,
    isQuantitativeComplete: Boolean,
    isSkipLocked: Boolean,
    onComplete: () -> Unit,
    onIncrementProgress: () -> Unit,
    onCustomProgress: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    onUndoIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCompleted: Boolean = status == HabitStatus.COMPLETED
    val isSkipped: Boolean = status == HabitStatus.SKIPPED
    val isResolved: Boolean = isCompleted || isSkipped || status == HabitStatus.FAILED

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (type == HabitType.BINARY) {
            BinaryBlock(
                isResolved = isResolved,
                isCompletedOrSkipped = isCompleted || isSkipped,
                isSkipLocked = isSkipLocked,
                onComplete = onComplete,
                onUndo = onUndo,
                onSkip = onSkip,
            )
        } else {
            QuantitativeBlock(
                isResolved = isResolved,
                isCompletedOrSkipped = isCompleted || isSkipped,
                isSkipLocked = isSkipLocked,
                currentProgress = currentProgress,
                unit = unit,
                onIncrementProgress = onIncrementProgress,
                onUndoIncrement = onUndoIncrement,
                onCustomProgress = onCustomProgress,
                onUndo = onUndo,
                onSkip = onSkip,
            )
        }
    }
}

@Composable
private fun BinaryBlock(
    isResolved: Boolean,
    isCompletedOrSkipped: Boolean,
    isSkipLocked: Boolean,
    onComplete: () -> Unit,
    onUndo: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isResolved && isCompletedOrSkipped) {
            PrimaryButton(onClick = onUndo) { Text(stringResource(Res.string.habit_detail_action_undo)) }
        } else {
            PrimaryButton(onClick = onComplete, enabled = !isResolved) {
                Text(stringResource(Res.string.habit_detail_action_complete))
            }
        }
        if (!isResolved) {
            SkipRow(onSkip = onSkip, isSkipLocked = isSkipLocked)
        }
    }
}

@Composable
private fun QuantitativeBlock(
    isResolved: Boolean,
    isCompletedOrSkipped: Boolean,
    isSkipLocked: Boolean,
    currentProgress: Int,
    unit: String?,
    onIncrementProgress: () -> Unit,
    onUndoIncrement: () -> Unit,
    onCustomProgress: () -> Unit,
    onUndo: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasProgress: Boolean = currentProgress > 0
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isResolved && isCompletedOrSkipped) {
            PrimaryButton(onClick = onUndo) { Text(stringResource(Res.string.habit_detail_action_undo)) }
        } else {
            Surface(
                shape = RoundedCornerShape(CARD_CORNER),
                color = RiteAppTheme.colors.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    StepperIconButton(text = "−", onClick = onUndoIncrement, enabled = hasProgress)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$currentProgress",
                            style = RiteAppTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = RiteAppTheme.colors.onSurface,
                        )
                        Text(
                            text = unit?.uppercase() ?: "",
                            style = RiteAppTheme.typography.labelSmall,
                            color = RiteAppTheme.colors.onSurfaceVariant,
                        )
                    }
                    StepperIconButton(text = "+", onClick = onIncrementProgress)
                }
            }
        }
        if (!isResolved) {
            SkipRow(
                onSkip = onSkip,
                isSkipLocked = isSkipLocked,
                trailingButton = {
                    IconSurface(
                        onClick = onCustomProgress,
                        icon = Icons.Default.Edit,
                        contentDescription = stringResource(Res.string.habit_detail_action_custom),
                    )
                },
            )
        }
    }
}

@Composable
private fun StepperIconButton(text: String, onClick: () -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = RiteAppTheme.colors.surface,
        enabled = enabled,
        modifier = modifier.size(STEPPER_BUTTON_SIZE),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = text,
                style = RiteAppTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = if (enabled) RiteAppTheme.colors.onSurface else RiteAppTheme.colors.onSurface.copy(alpha = 0.38f),
            )
        }
    }
}

@Composable
private fun IconSurface(onClick: () -> Unit, icon: ImageVector, contentDescription: String, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = RiteAppTheme.colors.surfaceContainerHigh,
        modifier = modifier.size(STEPPER_BUTTON_SIZE),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp),
                tint = RiteAppTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SkipRow(
    onSkip: () -> Unit,
    isSkipLocked: Boolean,
    modifier: Modifier = Modifier,
    trailingButton: (@Composable () -> Unit)? = null,
) {
    val contentColor = if (!isSkipLocked) RiteAppTheme.colors.onSecondaryContainer
    else RiteAppTheme.colors.onSecondaryContainer.copy(alpha = 0.38f)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = onSkip,
            shape = RoundedCornerShape(CARD_CORNER),
            color = RiteAppTheme.colors.secondaryContainer.copy(alpha = 0.4f),
            enabled = !isSkipLocked,
            modifier = Modifier.weight(1f).height(STEPPER_BUTTON_SIZE),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = Icons.Outlined.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp), tint = contentColor)
                Spacer(Modifier.size(6.dp))
                Text(
                    text = stringResource(Res.string.habit_detail_action_skip),
                    style = RiteAppTheme.typography.labelLarge,
                    color = contentColor,
                )
            }
        }
        trailingButton?.invoke()
    }
}
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileDebugSources`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Create the screenshot test**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
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
class HabitDetailActionScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun binary_pending() = render(HabitType.BINARY, HabitStatus.PENDING)
    @Test fun binary_pending_skip_locked() = render(HabitType.BINARY, HabitStatus.PENDING, skipLocked = true)
    @Test fun binary_completed() = render(HabitType.BINARY, HabitStatus.COMPLETED)
    @Test fun binary_skipped() = render(HabitType.BINARY, HabitStatus.SKIPPED)
    @Test fun binary_failed() = render(HabitType.BINARY, HabitStatus.FAILED)
    @Test fun quant_pending_zero() = render(HabitType.QUANTITATIVE, HabitStatus.PENDING, currentProgress = 0)
    @Test fun quant_pending_mid() = render(HabitType.QUANTITATIVE, HabitStatus.PENDING, currentProgress = 1750)
    @Test fun quant_pending_goal_reached() = render(HabitType.QUANTITATIVE, HabitStatus.PENDING, currentProgress = 2000, isQuantComplete = true)
    @Test fun quant_completed() = render(HabitType.QUANTITATIVE, HabitStatus.COMPLETED, currentProgress = 2000, isQuantComplete = true)
    @Test fun quant_skipped() = render(HabitType.QUANTITATIVE, HabitStatus.SKIPPED)

    private fun render(
        type: HabitType,
        status: HabitStatus,
        currentProgress: Int = 0,
        isQuantComplete: Boolean = false,
        skipLocked: Boolean = false,
    ) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                HabitDetailAction(
                    type = type,
                    status = status,
                    currentProgress = currentProgress,
                    unit = if (type == HabitType.QUANTITATIVE) "ml" else null,
                    isQuantitativeComplete = isQuantComplete,
                    isSkipLocked = skipLocked,
                    onComplete = {}, onIncrementProgress = {}, onCustomProgress = {},
                    onSkip = {}, onUndo = {}, onUndoIncrement = {},
                    modifier = Modifier.padding(horizontal = 22.dp),
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
```

- [ ] **Step 4: Record and verify goldens**

Run: `./gradlew :composeApp:recordRoborazziDebug --tests "*HabitDetailActionScreenshotTest*"`
Expected: 10 new PNGs.

Run: `./gradlew :composeApp:verifyRoborazziDebug --tests "*HabitDetailActionScreenshotTest*"`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/HabitDetailAction.kt \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/components/HabitDetailActionScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/com.ricardocosteira.rite.presentation.ui.habitdetail.components.HabitDetailActionScreenshotTest*.png
git commit -m "feat(habit-detail): add HabitDetailAction primitive"
```

---

## Task 6: Expand `HabitDetailUiModel` and `HabitDetailViewModel` for new data

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailState.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailViewModel.kt`
- Modify: `composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailStateTest.kt` (if existing test asserts on UiModel constructor shape)

Note: `SnoozeRepository` is already registered in `RiteAppComponent` — kotlin-inject will auto-wire it via `@Inject` on the VM constructor. No DI-file changes needed.

- [ ] **Step 1: Write failing test for VM expansion**

Add new test methods to `HabitDetailStateTest.kt` (or create a new test if the file grows unwieldy):

```kotlin
@Test
fun `given balanced preset user, uiModel exposes strictnessPreset Balanced`() {
    val model = HabitDetailUiModel(
        // ... existing required fields ...
        strictnessPreset = StrictnessPreset.BALANCED,
        undoPolicy = UndoPolicy.TODAY_ONLY,
        snoozesUsedToday = 1,
        maxSnoozesPerDay = 3,
        skipsThisWeek = 2,
        // existing
        currentConsecutiveSkips = 0,
        maxConsecutiveSkips = 2,
        // ... rest of existing required fields ...
    )
    assertEquals(StrictnessPreset.BALANCED, model.strictnessPreset)
    assertEquals(2, model.skipsThisWeek)
}
```

(Engineer: fill in the existing required fields from the current `HabitDetailUiModel` constructor so the test compiles. Run it to watch it fail on the five missing fields.)

Run: `./gradlew :composeApp:jvmTest --tests "*HabitDetailStateTest*"`
Expected: FAIL — the new fields are not defined on `HabitDetailUiModel`.

- [ ] **Step 2: Expand `HabitDetailUiModel`**

In `HabitDetailState.kt`, add the five new fields at the end of `HabitDetailUiModel` (keep existing fields untouched):

```kotlin
val strictnessPreset: StrictnessPreset?,
val undoPolicy: UndoPolicy,
val snoozesUsedToday: Int,
val maxSnoozesPerDay: Int?,
val skipsThisWeek: Int,
```

Add imports at the top of the file:

```kotlin
import com.ricardocosteira.rite.domain.models.StrictnessPreset
import com.ricardocosteira.rite.domain.models.UndoPolicy
import com.ricardocosteira.rite.domain.models.UserStrictnessSettings
```

- [ ] **Step 3: Run the test to verify it compiles and passes**

Run: `./gradlew :composeApp:jvmTest --tests "*HabitDetailStateTest*"`
Expected: PASS.

- [ ] **Step 4: Write failing test for `HabitDetailViewModel.loadDetail` picking up new data**

Create `composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailViewModelTest.kt` with test that asserts:
- When the VM loads a habit instance for a Balanced user, `state.habit.strictnessPreset` is `BALANCED`, `undoPolicy` is `TODAY_ONLY`.
- `snoozesUsedToday` reflects `snoozeRepo.getSnoozeState(instanceId)?.snoozeCount`.
- `skipsThisWeek` is `2` when two instances with `SKIPPED` status fall in the current week (and zero outside).

Use fake repositories for `HabitRepository`, `HabitInstanceRepository`, `UserRepository`, `SnoozeRepository`. Each fake can be a simple in-memory class implementing the repo interface. Use `kotlinx.coroutines.test.runTest` and `UnconfinedTestDispatcher` for the VM's `viewModelScope`.

Minimal structure:

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail

import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.SnoozeState
import com.ricardocosteira.rite.domain.models.StrictnessPreset
import com.ricardocosteira.rite.domain.models.UndoPolicy
import com.ricardocosteira.rite.domain.models.User
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HabitDetailViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @AfterTest
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `given balanced user, strictnessPreset and undoPolicy are surfaced`() = runTest {
        val fakes = fakes(userPreset = StrictnessPreset.BALANCED)
        val vm = fakes.buildVm(instanceId = "i1")
        val model = vm.state.value.habit!!
        assertEquals(StrictnessPreset.BALANCED, model.strictnessPreset)
        assertEquals(UndoPolicy.TODAY_ONLY, model.undoPolicy)
    }

    @Test
    fun `given snooze state with count 2, snoozesUsedToday is 2`() = runTest {
        val fakes = fakes(snoozeCount = 2)
        val vm = fakes.buildVm(instanceId = "i1")
        assertEquals(2, vm.state.value.habit!!.snoozesUsedToday)
    }

    @Test
    fun `given 3 skipped instances in current ISO week, skipsThisWeek is 3`() = runTest {
        val today = LocalDate(2026, 4, 15) // Wednesday of ISO week 16
        val sameWeek = listOf(
            LocalDate(2026, 4, 13),
            LocalDate(2026, 4, 14),
            LocalDate(2026, 4, 15),
        ).map { HabitInstance(/* … */) /* status = SKIPPED */ }
        val outsideWeek = HabitInstance(/* … */) // status = SKIPPED, date before ISO week 16
        val fakes = fakes(instancesForHabit = sameWeek + outsideWeek, today = today)
        val vm = fakes.buildVm(instanceId = "i1")
        assertEquals(3, vm.state.value.habit!!.skipsThisWeek)
    }

    // … helper data class `fakes(...)` that builds the four fake repos, use-cases (stub),
    //    and a clock override. It exposes `buildVm(instanceId)` that constructs
    //    HabitDetailViewModel directly (no kotlin-inject) with the fakes.
}
```

(Engineer: see the existing `HabitFormViewModelTest` / `TodayViewModelTest` for the fake-repo structure used in this project, and mirror that.)

Run: `./gradlew :composeApp:jvmTest --tests "*HabitDetailViewModelTest*"`
Expected: FAIL (the VM does not yet wire `SnoozeRepository`, does not derive strictness, does not count skips this week).

- [ ] **Step 5: Expand `HabitDetailViewModel`**

Add constructor param (kotlin-inject will wire automatically, since `SnoozeRepository` is already bound in `RiteAppComponent`):

```kotlin
@Inject
class HabitDetailViewModel(
    private val habitRepository: HabitRepository,
    private val habitInstanceRepository: HabitInstanceRepository,
    private val userRepository: UserRepository,
    private val snoozeRepository: SnoozeRepository,                     // ← new
    private val completeHabit: CompleteHabit,
    private val skipHabit: SkipHabit,
    private val undoHabit: UndoHabit,
    private val undoLastIncrement: UndoLastIncrement,
    @Assisted private val instanceId: String
) : ViewModel() { ... }
```

Inside `loadDetail()`, after loading `user`, compute:

```kotlin
val strictnessSettings = UserStrictnessSettings(
    undoPolicy = user?.undoPolicy ?: UndoPolicy.TODAY_ONLY,
    maxSnoozesPerHabitPerDay = user?.maxSnoozesPerHabitPerDay,
    maxConsecutiveSkips = user?.maxConsecutiveSkips,
    maxSnoozeDurationMinutes = user?.maxSnoozeDurationMinutes ?: User.DEFAULT_MAX_SNOOZE_DURATION_MINUTES,
)
val strictnessPreset: StrictnessPreset? = StrictnessPreset.fromSettings(strictnessSettings)
val snoozeState: SnoozeState? = snoozeRepository.getSnoozeState(instanceId)
val snoozesUsedToday: Int = snoozeState?.snoozeCount ?: 0
val skipsThisWeek: Int = countSkipsInIsoWeek(allInstances, today, user?.timezone ?: TimeZone.currentSystemDefault())
```

And pass these five new values into the `HabitDetailUiModel(...)` copy.

Add a private helper:

```kotlin
private fun countSkipsInIsoWeek(
    instances: List<HabitInstance>,
    today: LocalDate,
    timezone: TimeZone,
): Int {
    // Monday of current ISO week
    val mondayOffset: Int = today.dayOfWeek.ordinal // MONDAY=0 in DayOfWeek.ordinal
    val monday: LocalDate = today.minus(DatePeriod(days = mondayOffset))
    val sunday: LocalDate = monday.plus(DatePeriod(days = 6))
    return instances.count { it.status == HabitStatus.SKIPPED && it.date in monday..sunday }
}
```

Add imports: `com.ricardocosteira.rite.domain.models.User`, `com.ricardocosteira.rite.domain.models.SnoozeState`, `com.ricardocosteira.rite.domain.models.UserStrictnessSettings`, `com.ricardocosteira.rite.domain.models.StrictnessPreset`, `com.ricardocosteira.rite.domain.repositories.SnoozeRepository`.

- [ ] **Step 6: Run the tests to verify they pass**

Run: `./gradlew :composeApp:jvmTest --tests "*HabitDetailViewModelTest*" --tests "*HabitDetailStateTest*"`
Expected: PASS (all tests).

- [ ] **Step 7: Rebuild to verify everything compiles**

Run: `./gradlew :composeApp:compileDebugSources`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailState.kt \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailViewModel.kt \
        composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailStateTest.kt \
        composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailViewModelTest.kt
git commit -m "refactor(habit-detail): expand UiModel + VM for strictness/snoozes/skipsWeek"
```

---

## Task 7: Move models/, delete Route, add public Screen overload

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/models/HabitDetailUiModel.kt`
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/models/HeatmapDay.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailState.kt`
- Delete: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailRoute.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/navigation/RiteNavigation.kt`

- [ ] **Step 1: Move `HabitDetailUiModel` to `models/HabitDetailUiModel.kt`**

Cut the `HabitDetailUiModel` data class (including its five new fields from Task 6) from `HabitDetailState.kt` and paste into a new file `models/HabitDetailUiModel.kt`. Package: `com.ricardocosteira.rite.presentation.ui.habitdetail.models`. Copy all necessary imports.

- [ ] **Step 2: Move `HeatmapDay` to `models/HeatmapDay.kt`**

Cut the `HeatmapDay` data class from `HabitDetailState.kt` and paste into a new file `models/HeatmapDay.kt`. Same package as above.

- [ ] **Step 3: Update `HabitDetailState.kt`**

Should now contain only the top-level `HabitDetailState` data class plus imports of `HabitDetailUiModel` and `HeatmapDay` from the new `models/` package.

- [ ] **Step 4: Update imports across files**

Any file that previously imported `com.ricardocosteira.rite.presentation.ui.habitdetail.HabitDetailUiModel` or `...HeatmapDay` updates to `...habitdetail.models.HabitDetailUiModel` / `.HeatmapDay`. Sweep: `HabitDetailScreen.kt`, `HabitDetailViewModel.kt`, `Tapestry.kt`, tests.

Run: `./gradlew :composeApp:compileDebugSources`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Add public `HabitDetailScreen` overload and delete `HabitDetailRoute.kt`**

Open `HabitDetailScreen.kt` and at the top (before the existing internal `HabitDetailScreen`), add:

```kotlin
@Composable
fun HabitDetailScreen(
    instanceId: String,
    onNavigateBack: () -> Unit,
    onEditHabit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val createViewModel = LocalAppComponent.current.createHabitDetailViewModel
    val viewModel: HabitDetailViewModel = viewModel { createViewModel(instanceId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isDeleteDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HabitDetailEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    HabitDetailScreen(
        state = state,
        onBackClick = onNavigateBack,
        onComplete = viewModel::completeBinary,
        onIncrementProgress = viewModel::incrementProgress,
        onCustomProgress = viewModel::showCustomInput,
        onSkip = viewModel::skip,
        onUndo = viewModel::undo,
        onUndoIncrement = viewModel::undoIncrement,
        onEditHabit = { state.habit?.habitId?.let(onEditHabit) },
        onArchiveHabit = viewModel::archiveHabit,
        onDeleteHabit = { isDeleteDialogVisible = true },
        modifier = modifier,
    )

    if (isDeleteDialogVisible) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogVisible = false },
            title = { Text(stringResource(Res.string.habit_form_delete_dialog_title)) },
            text = { Text(stringResource(Res.string.habit_form_delete_dialog_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleteDialogVisible = false
                        viewModel.deleteHabit()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = RiteAppTheme.colors.error),
                ) { Text(stringResource(Res.string.habit_form_delete_dialog_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { isDeleteDialogVisible = false }) {
                    Text(stringResource(Res.string.habit_form_delete_dialog_cancel))
                }
            },
        )
    }

    val customInputHabit = state.habit
    if (state.showCustomInput && customInputHabit != null) {
        QuantitativeInputBottomSheet(
            name = customInputHabit.name,
            completedValue = customInputHabit.completedValue,
            targetValue = customInputHabit.targetValue,
            unit = customInputHabit.unit,
            defaultIncrement = customInputHabit.defaultIncrement,
            onConfirm = { value ->
                viewModel.addCustomProgress(value)
                viewModel.dismissCustomInput()
            },
            onDismiss = viewModel::dismissCustomInput,
        )
    }
}
```

Add the imports this needs (most come from the deleted `HabitDetailRoute.kt`).

Change the existing internal composable's declaration from `fun HabitDetailScreen(...)` to `internal fun HabitDetailScreen(...)`.

- [ ] **Step 6: Delete `HabitDetailRoute.kt`**

```bash
git rm composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailRoute.kt
```

- [ ] **Step 7: Update navigation call site**

In `RiteNavigation.kt`:
- Replace the import `import com.ricardocosteira.rite.presentation.ui.habitdetail.HabitDetailRoute` with `import com.ricardocosteira.rite.presentation.ui.habitdetail.HabitDetailScreen`.
- Change the call inside `entry<HabitDetail>`: replace `HabitDetailRoute(` with `HabitDetailScreen(`. Argument list is identical.

Run: `./gradlew :composeApp:compileDebugSources`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Run all tests to verify no regressions**

Run: `./gradlew :composeApp:jvmTest`
Expected: PASS.

Run: `./gradlew :composeApp:verifyRoborazziDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/ \
        composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/navigation/RiteNavigation.kt
git commit -m "refactor(habit-detail): move models/ subpackage, delete Route, public Screen overload"
```

---

## Task 8: Re-assemble `HabitDetailScreen` on v2 primitives + full-screen goldens

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailScreen.kt` (internal overload body)
- Modify: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailScreenScreenshotTest.kt`

- [ ] **Step 1: Rewrite the internal `HabitDetailScreen` overload body**

The new body composes: `TopBar` (kept) → `CategoryEyebrow` + habit name + optional note → `ProgressRing` (100dp, `capLabel = null`, no surrounding card) → `HabitDetailAction` → `StatTileRow` → `Tapestry(weekRangeLabel = formatWeekRange(today.minus(DatePeriod(days = 90)), today))` → `EnforcementLimitsTable`.

Key changes:
- Remove `ProgressRingCard`, `StatsRow` (old inline StatCard trio), `SkipLimitsCard` (old one-line enforcement) inline functions — their code is now unused and deletable.
- Remove the inline `ActionButtons` / `BinaryActions` / `QuantitativeActions` / `StepperButton` / `IconSurface` / `SkipRow` private helpers — moved to `HabitDetailAction.kt`.
- Use `state.habit.description` to conditionally render the habit note:

```kotlin
state.habit.description?.let { note ->
    Spacer(Modifier.height(8.dp))
    Text(
        text = note,
        style = RiteAppTheme.typography.titleLarge.copy(fontStyle = FontStyle.Italic, fontSize = 13.5.sp, lineHeight = 20.sp),
        color = RiteAppTheme.colors.onSurfaceVariant,
        modifier = Modifier.widthIn(max = 300.dp),
    )
}
```

- Use `ProgressRing(progress = state.habit.progressPercentage.coerceIn(0f, 1f), size = 100.dp, capLabel = null)` instead of the inline canvas.
- Compute `weekRangeLabel` inline using the today / 90-day-ago dates — or expose a helper on the state.

Exact structural target is the final mockup at `.superpowers/brainstorm/58741-1776627505/content/design-04-enf-table.html`.

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileDebugSources`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Rewrite the screenshot test**

Replace existing scenarios with six per-variant scenarios × 2 themes (12 goldens):

```kotlin
@Test fun binary_pending_light() = render(binaryPending, darkTheme = false)
@Test fun binary_pending_dark() = render(binaryPending, darkTheme = true)
@Test fun binary_completed_light() = render(binaryCompleted, darkTheme = false)
@Test fun binary_completed_dark() = render(binaryCompleted, darkTheme = true)
@Test fun binary_failed_light() = render(binaryFailed, darkTheme = false)
@Test fun quant_in_progress_light() = render(quantInProgress, darkTheme = false)
@Test fun quant_in_progress_dark() = render(quantInProgress, darkTheme = true)
@Test fun quant_goal_reached_light() = render(quantGoalReached, darkTheme = false)
@Test fun quant_goal_reached_dark() = render(quantGoalReached, darkTheme = true)
@Test fun quant_skip_locked_light() = render(quantSkipLocked, darkTheme = false)
@Test fun quant_skip_locked_dark() = render(quantSkipLocked, darkTheme = true)
```

Each scenario constructs a `HabitDetailUiModel` (imported from `com.ricardocosteira.rite.presentation.ui.habitdetail.models`) with values that exercise the variant (e.g., `strictnessPreset = BALANCED`, `undoPolicy = TODAY_ONLY`, `snoozesUsedToday = 1`, `maxSnoozesPerDay = 3`, `skipsThisWeek = 2`, plus appropriate `status` and `currentProgress`). Populate `heatmapData` with a fixed sample produced by a helper so the golden is deterministic.

The `render(state, darkTheme)` helper calls `HabitDetailScreen(state = HabitDetailState(habit = model, heatmapData = sampleHeatmap, isLoading = false), ...)` — i.e. the **internal** overload with stub callbacks.

- [ ] **Step 4: Record goldens (remove stale first)**

Delete any stale pre-task goldens for this test:

```bash
rm -f composeApp/src/androidUnitTest/snapshots/images/com.ricardocosteira.rite.presentation.ui.habitdetail.HabitDetailScreenScreenshotTest_*.png
```

Then record:

Run: `./gradlew :composeApp:recordRoborazziDebug --tests "*HabitDetailScreenScreenshotTest*"`
Expected: 11 new PNGs (one per `@Test`).

- [ ] **Step 5: Verify goldens**

Run: `./gradlew :composeApp:verifyRoborazziDebug --tests "*HabitDetailScreenScreenshotTest*"`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Run the full test suite**

Run: `./gradlew :composeApp:jvmTest :composeApp:verifyRoborazziDebug`
Expected: BUILD SUCCESSFUL. No regressions elsewhere (`TodayScreenScreenshotTest`, etc.).

- [ ] **Step 7: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailScreen.kt \
        composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailScreenScreenshotTest.kt \
        composeApp/src/androidUnitTest/snapshots/images/com.ricardocosteira.rite.presentation.ui.habitdetail.HabitDetailScreenScreenshotTest*.png
git commit -m "feat(habit-detail): re-assemble HabitDetailScreen on v2 primitives"
```

---

## Final verification

- [ ] **Step A: Clean build**

Run: `./gradlew :composeApp:compileDebugSources`
Expected: BUILD SUCCESSFUL.

- [ ] **Step B: Full test suite**

Run: `./gradlew :composeApp:jvmTest :composeApp:verifyRoborazziDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step C: Review the commit log**

Run: `git log --oneline origin/feature/design-system-v2-02-today-habitcard..HEAD`
Expected: Eight `feat(habit-detail)` / `refactor(habit-detail)` commits in order (plus the initial `docs:` spec commit).
