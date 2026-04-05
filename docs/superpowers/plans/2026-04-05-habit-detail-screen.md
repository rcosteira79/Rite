# Habit Detail Screen Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a habit detail screen showing today's progress, streaks, Habit Score, skip limits, a 3-month completion heatmap, and action buttons (complete/skip/custom).

**Architecture:** New `HabitDetailScreen` composable + `HabitDetailViewModel` following the existing factory pattern (like `HabitFormViewModel`). The ViewModel loads the HabitInstance by ID, then fetches the parent Habit for aggregate stats and historical instances for the heatmap. The Today screen's expand-on-tap behavior is replaced with navigation to this screen.

**Tech Stack:** Kotlin, Compose Multiplatform (Material 3), SQLDelight, kotlin-inject, Roborazzi

---

## File Structure

| Action | File | Responsibility |
|--------|------|----------------|
| Create | `.../composeResources/values/strings_habit_detail.xml` | String resources |
| Create | `.../ui/habitdetail/HabitDetailState.kt` | State data classes (`HabitDetailState`, `HeatmapDay`) |
| Create | `.../ui/habitdetail/HabitDetailViewModel.kt` | ViewModel with Factory pattern |
| Create | `.../ui/habitdetail/HabitDetailScreen.kt` | Screen composable (header, stats, heatmap, actions) |
| Create | `.../ui/habitdetail/HabitDetailRoute.kt` | Route composable (wires ViewModel) |
| Create | `.../ui/habitdetail/HeatmapGrid.kt` | Heatmap composable |
| Modify | `.../di/RiteAppComponent.kt` | Add HabitDetailViewModel.Factory provision |
| Modify | `.../navigation/RiteNavigation.kt` | Replace auto-pop with HabitDetailRoute |
| Modify | `.../ui/today/TodayScreen.kt` | Replace expand with navigate-to-detail |
| Modify | `.../ui/today/HabitCard.kt` | Remove expand state, change tap to navigate |
| Create | `.../androidUnitTest/.../HabitDetailScreenScreenshotTest.kt` | Screenshot tests |
| Create | `.../commonTest/.../HabitDetailViewModelTest.kt` | ViewModel unit tests |
| Create | `.../commonTest/.../HeatmapDataTest.kt` | Heatmap data mapping tests |

**Note:** All `.../` paths expand to `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/` unless otherwise specified.

---

### Task 1: Add String Resources

**Files:**
- Create: `composeApp/src/commonMain/composeResources/values/strings_habit_detail.xml`

- [ ] **Step 1: Create the string resource file**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="habit_detail_category_binary">BINARY RITUAL</string>
    <string name="habit_detail_category_quantitative">QUANTITATIVE PURSUIT</string>
    <string name="habit_detail_stat_current_streak">Current Streak</string>
    <string name="habit_detail_stat_longest_streak">Longest Streak</string>
    <string name="habit_detail_stat_habit_score">Habit Score</string>
    <string name="habit_detail_stat_days">Days</string>
    <string name="habit_detail_skips_remaining">%1$d skips remaining</string>
    <string name="habit_detail_skips_unlimited">Unlimited skips</string>
    <string name="habit_detail_skips_none">No skips remaining</string>
    <string name="habit_detail_heatmap_title">Last 3 months</string>
    <string name="habit_detail_action_complete">Complete</string>
    <string name="habit_detail_action_completed">Completed</string>
    <string name="habit_detail_action_skip">Skip</string>
    <string name="habit_detail_action_skipped">Skipped</string>
    <string name="habit_detail_action_custom">Custom</string>
    <string name="habit_detail_action_goal_reached">Goal reached</string>
    <string name="habit_detail_progress">%1$d of %2$d %3$s</string>
</resources>
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileDebugSources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/composeResources/values/strings_habit_detail.xml
git commit -m "feat(habit-detail): add string resources"
```

---

### Task 2: Create State and Data Classes

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailState.kt`

- [ ] **Step 1: Create the state file**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail

import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitStatus
import kotlinx.datetime.LocalDate

data class HabitDetailState(
    val habit: Habit? = null,
    val instance: HabitInstance? = null,
    val maxConsecutiveSkips: Int? = null,
    val heatmapData: List<HeatmapDay> = emptyList(),
    val isLoading: Boolean = true
) {
    val habitScore: Int
        get() = habit?.calculateScore()?.percentage ?: 0

    val skipsRemaining: Int?
        get() {
            val max: Int = maxConsecutiveSkips ?: return null
            val used: Int = instance?.consecutiveSkipsAtCreation ?: 0
            return (max - used).coerceAtLeast(0)
        }

    val isSkipLocked: Boolean
        get() = instance?.isSkipLocked(maxConsecutiveSkips) ?: false

    val isCompleted: Boolean
        get() {
            val inst: HabitInstance = instance ?: return false
            return inst.status == HabitStatus.COMPLETED
        }

    val isSkipped: Boolean
        get() {
            val inst: HabitInstance = instance ?: return false
            return inst.status == HabitStatus.SKIPPED
        }

    val isFailed: Boolean
        get() {
            val inst: HabitInstance = instance ?: return false
            return inst.status == HabitStatus.FAILED
        }

    val isResolved: Boolean
        get() = isCompleted || isSkipped || isFailed

    val isQuantitativeComplete: Boolean
        get() = instance?.isQuantitativeComplete() ?: false
}

data class HeatmapDay(
    val date: LocalDate,
    val completionPercentage: Float,
    val status: HabitStatus
)
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileDebugSources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailState.kt
git commit -m "feat(habit-detail): add state and data classes"
```

---

### Task 3: Create HabitDetailViewModel

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailViewModel.kt`

- [ ] **Step 1: Create the ViewModel**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricardocosteira.rite.domain.models.CompletionSource
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.rite.domain.repositories.HabitRepository
import com.ricardocosteira.rite.domain.repositories.UserRepository
import com.ricardocosteira.rite.domain.usecases.CompleteHabit
import com.ricardocosteira.rite.domain.usecases.SkipHabit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

private const val HEATMAP_DAYS = 90

class HabitDetailViewModel(
    private val habitRepository: HabitRepository,
    private val habitInstanceRepository: HabitInstanceRepository,
    private val userRepository: UserRepository,
    private val completeHabit: CompleteHabit,
    private val skipHabit: SkipHabit,
    private val instanceId: String
) : ViewModel() {

    private val _state = MutableStateFlow(HabitDetailState())
    val state: StateFlow<HabitDetailState> = _state.asStateFlow()

    init {
        loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val instance: HabitInstance = habitInstanceRepository.getInstanceById(instanceId)
                ?: run {
                    _state.update { it.copy(isLoading = false) }
                    return@launch
                }

            val habit = habitRepository.getHabitById(instance.habitId)
            val user = userRepository.getUser()
            val maxSkips: Int? = user?.maxConsecutiveSkips

            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val startDate = today.minus(HEATMAP_DAYS, DateTimeUnit.DAY)
            val allInstances: List<HabitInstance> =
                habitInstanceRepository.getInstancesForHabit(instance.habitId)

            val heatmapData: List<HeatmapDay> = allInstances
                .filter { it.date >= startDate && it.date <= today }
                .map { inst ->
                    HeatmapDay(
                        date = inst.date,
                        completionPercentage = inst.progressPercentage(),
                        status = inst.status
                    )
                }

            _state.update {
                it.copy(
                    habit = habit,
                    instance = instance,
                    maxConsecutiveSkips = maxSkips,
                    heatmapData = heatmapData,
                    isLoading = false
                )
            }
        }
    }

    fun completeBinary() {
        val instanceId: String = _state.value.instance?.id ?: return
        viewModelScope.launch {
            completeHabit.executeBinary(instanceId, CompletionSource.APP)
            loadDetail()
        }
    }

    fun incrementProgress() {
        val instanceId: String = _state.value.instance?.id ?: return
        val increment: Int = _state.value.habit?.defaultIncrement ?: 1
        viewModelScope.launch {
            completeHabit.executeQuantitative(instanceId, increment, CompletionSource.APP)
            loadDetail()
        }
    }

    fun addCustomProgress(amount: Int) {
        val instanceId: String = _state.value.instance?.id ?: return
        viewModelScope.launch {
            completeHabit.executeQuantitative(instanceId, amount, CompletionSource.APP)
            loadDetail()
        }
    }

    fun skip() {
        val instanceId: String = _state.value.instance?.id ?: return
        viewModelScope.launch {
            skipHabit.execute(instanceId)
            loadDetail()
        }
    }

    interface Factory {
        fun create(instanceId: String): HabitDetailViewModel
    }
}
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileDebugSources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailViewModel.kt
git commit -m "feat(habit-detail): add HabitDetailViewModel with Factory"
```

---

### Task 4: Register ViewModel Factory in DI

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/RiteAppComponent.kt`

- [ ] **Step 1: Add the factory provision and accessor**

Add import:
```kotlin
import com.ricardocosteira.rite.presentation.ui.habitdetail.HabitDetailViewModel
```

Add factory method after `provideHabitFormViewModelFactory` (around line 115):
```kotlin
    @AppScope
    @Provides
    fun provideHabitDetailViewModelFactory(
        habitRepository: HabitRepository,
        habitInstanceRepository: HabitInstanceRepository,
        userRepository: UserRepository,
        completeHabit: CompleteHabit,
        skipHabit: SkipHabit
    ): HabitDetailViewModel.Factory = object : HabitDetailViewModel.Factory {
        override fun create(instanceId: String): HabitDetailViewModel = HabitDetailViewModel(
            habitRepository,
            habitInstanceRepository,
            userRepository,
            completeHabit,
            skipHabit,
            instanceId
        )
    }
```

Add accessor alongside the other ViewModel accessors (around line 128):
```kotlin
    abstract val habitDetailViewModelFactory: HabitDetailViewModel.Factory
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileDebugSources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/RiteAppComponent.kt
git commit -m "feat(habit-detail): register HabitDetailViewModel.Factory in DI"
```

---

### Task 5: Create HeatmapGrid Composable

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HeatmapGrid.kt`

- [ ] **Step 1: Create the heatmap composable**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.domain.models.HabitStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

private val CELL_SIZE = 12.dp
private val CELL_GAP = 2.dp
private val CELL_CORNER = 2.dp
private val DAY_LABEL_WIDTH = 20.dp

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
fun HeatmapGrid(
    heatmapData: List<HeatmapDay>,
    modifier: Modifier = Modifier
) {
    val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val startDate: LocalDate = today.minus(90, DateTimeUnit.DAY)

    val dataByDate: Map<LocalDate, HeatmapDay> = heatmapData.associateBy { it.date }

    // Build weeks (columns). Each week is Mon-Sun.
    val weeks: List<List<LocalDate?>> = buildWeeks(startDate, today)

    Row(modifier = modifier) {
        // Day labels column
        Column(
            verticalArrangement = Arrangement.spacedBy(CELL_GAP)
        ) {
            DAY_LABELS.forEach { (_, label) ->
                Box(
                    modifier = Modifier.size(CELL_SIZE),
                    contentAlignment = Alignment.Center
                ) {
                    if (label.isNotEmpty()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Week columns
        Row(
            horizontalArrangement = Arrangement.spacedBy(CELL_GAP)
        ) {
            weeks.forEach { week ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(CELL_GAP)
                ) {
                    week.forEach { date ->
                        if (date == null) {
                            // Empty cell (before start or after today)
                            Box(modifier = Modifier.size(CELL_SIZE))
                        } else {
                            val day: HeatmapDay? = dataByDate[date]
                            HeatmapCell(day = day)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapCell(day: HeatmapDay?, modifier: Modifier = Modifier) {
    val color = when {
        day == null -> MaterialTheme.colorScheme.surfaceContainerLow
        day.status == HabitStatus.SKIPPED -> MaterialTheme.colorScheme.outlineVariant
        day.status == HabitStatus.SUSPENDED -> MaterialTheme.colorScheme.surfaceContainerLow
        day.completionPercentage >= 1.0f -> MaterialTheme.colorScheme.primary
        day.completionPercentage >= 0.5f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        day.completionPercentage > 0f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Box(
        modifier = modifier
            .size(CELL_SIZE)
            .background(color = color, shape = RoundedCornerShape(CELL_CORNER))
    )
}

private fun buildWeeks(startDate: LocalDate, endDate: LocalDate): List<List<LocalDate?>> {
    val weeks: MutableList<List<LocalDate?>> = mutableListOf()
    var current: LocalDate = startDate

    // Align to start of week (Monday)
    while (current.dayOfWeek != DayOfWeek.MONDAY) {
        current = current.minus(1, DateTimeUnit.DAY)
    }

    while (current <= endDate) {
        val week: MutableList<LocalDate?> = mutableListOf()
        repeat(7) { dayIndex ->
            val date: LocalDate = current.plus(dayIndex, DateTimeUnit.DAY)
            week.add(
                if (date in startDate..endDate) date else null
            )
        }
        weeks.add(week)
        current = current.plus(7, DateTimeUnit.DAY)
    }

    return weeks
}

private fun LocalDate.plus(value: Int, unit: DateTimeUnit.DateBased): LocalDate {
    return kotlinx.datetime.DateTimeUnit.DAY.let { dayUnit ->
        var result = this
        repeat(value) { result = result.plus(1, dayUnit) }
        result
    }
}
```

Wait — `kotlinx.datetime.LocalDate` already has `plus` with `DatePeriod`. Let me simplify using the proper API:

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.domain.models.HabitStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

private val CELL_SIZE = 12.dp
private val CELL_GAP = 2.dp
private val CELL_CORNER = 2.dp

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
fun HeatmapGrid(
    heatmapData: List<HeatmapDay>,
    modifier: Modifier = Modifier
) {
    val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val startDate: LocalDate = today.minus(DatePeriod(days = 90))

    val dataByDate: Map<LocalDate, HeatmapDay> = heatmapData.associateBy { it.date }

    val weeks: List<List<LocalDate?>> = buildWeeks(startDate, today)

    Row(modifier = modifier) {
        // Day labels column
        Column(verticalArrangement = Arrangement.spacedBy(CELL_GAP)) {
            DAY_LABELS.forEach { (_, label) ->
                Box(
                    modifier = Modifier.size(CELL_SIZE),
                    contentAlignment = Alignment.Center
                ) {
                    if (label.isNotEmpty()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Week columns
        Row(horizontalArrangement = Arrangement.spacedBy(CELL_GAP)) {
            weeks.forEach { week ->
                Column(verticalArrangement = Arrangement.spacedBy(CELL_GAP)) {
                    week.forEach { date ->
                        if (date == null) {
                            Box(modifier = Modifier.size(CELL_SIZE))
                        } else {
                            HeatmapCell(day = dataByDate[date])
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapCell(day: HeatmapDay?, modifier: Modifier = Modifier) {
    val color = when {
        day == null -> MaterialTheme.colorScheme.surfaceContainerLow
        day.status == HabitStatus.SKIPPED -> MaterialTheme.colorScheme.outlineVariant
        day.status == HabitStatus.SUSPENDED -> MaterialTheme.colorScheme.surfaceContainerLow
        day.completionPercentage >= 1.0f -> MaterialTheme.colorScheme.primary
        day.completionPercentage >= 0.5f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        day.completionPercentage > 0f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Box(
        modifier = modifier
            .size(CELL_SIZE)
            .background(color = color, shape = RoundedCornerShape(CELL_CORNER))
    )
}

private fun buildWeeks(startDate: LocalDate, endDate: LocalDate): List<List<LocalDate?>> {
    val weeks: MutableList<List<LocalDate?>> = mutableListOf()

    // Align to start of week (Monday)
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
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileDebugSources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HeatmapGrid.kt
git commit -m "feat(habit-detail): add HeatmapGrid composable"
```

---

### Task 6: Create HabitDetailScreen Composable

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailScreen.kt`

- [ ] **Step 1: Create the screen composable**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.components.PrimaryButton
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.common_cd_back
import rite.composeapp.generated.resources.habit_detail_action_complete
import rite.composeapp.generated.resources.habit_detail_action_completed
import rite.composeapp.generated.resources.habit_detail_action_custom
import rite.composeapp.generated.resources.habit_detail_action_goal_reached
import rite.composeapp.generated.resources.habit_detail_action_skip
import rite.composeapp.generated.resources.habit_detail_action_skipped
import rite.composeapp.generated.resources.habit_detail_category_binary
import rite.composeapp.generated.resources.habit_detail_category_quantitative
import rite.composeapp.generated.resources.habit_detail_heatmap_title
import rite.composeapp.generated.resources.habit_detail_progress
import rite.composeapp.generated.resources.habit_detail_skips_none
import rite.composeapp.generated.resources.habit_detail_skips_remaining
import rite.composeapp.generated.resources.habit_detail_skips_unlimited
import rite.composeapp.generated.resources.habit_detail_stat_current_streak
import rite.composeapp.generated.resources.habit_detail_stat_days
import rite.composeapp.generated.resources.habit_detail_stat_habit_score
import rite.composeapp.generated.resources.habit_detail_stat_longest_streak

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    state: HabitDetailState,
    onBackClick: () -> Unit,
    onComplete: () -> Unit,
    onIncrementProgress: () -> Unit,
    onCustomProgress: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_cd_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading || state.habit == null || state.instance == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                // Category label
                Text(
                    text = if (state.habit.type == HabitType.BINARY) {
                        stringResource(Res.string.habit_detail_category_binary)
                    } else {
                        stringResource(Res.string.habit_detail_category_quantitative)
                    },
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Habit name
                Text(
                    text = state.habit.name.uppercase(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Progress indicator
                if (state.habit.type == HabitType.QUANTITATIVE) {
                    QuantitativeProgress(state = state)
                } else {
                    BinaryProgress(state = state)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stats row
                StatsRow(state = state)

                Spacer(modifier = Modifier.height(16.dp))

                // Accountability limits
                SkipLimitsRow(state = state)

                Spacer(modifier = Modifier.height(24.dp))

                // Heatmap
                Text(
                    text = stringResource(Res.string.habit_detail_heatmap_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                HeatmapGrid(
                    heatmapData = state.heatmapData,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                ActionButtons(
                    state = state,
                    onComplete = onComplete,
                    onIncrementProgress = onIncrementProgress,
                    onCustomProgress = onCustomProgress,
                    onSkip = onSkip
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun QuantitativeProgress(state: HabitDetailState, modifier: Modifier = Modifier) {
    val instance = state.instance ?: return
    val habit = state.habit ?: return

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress ring placeholder — reuse Canvas ring from Today screen pattern
        Box(
            modifier = Modifier.size(88.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { instance.progressPercentage().coerceIn(0f, 1f) },
                modifier = Modifier.size(88.dp),
                strokeWidth = 5.dp,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${instance.currentProgress}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(
                        Res.string.habit_detail_progress,
                        instance.currentProgress,
                        instance.targetValue ?: 0,
                        habit.unit?.uppercase() ?: ""
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BinaryProgress(state: HabitDetailState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val isCompleted: Boolean = state.isCompleted
        val iconTint = if (isCompleted) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

        Box(
            modifier = Modifier
                .size(88.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Outlined.Check else Icons.Outlined.Block,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = iconTint
            )
        }
    }
}

@Composable
private fun StatsRow(state: HabitDetailState, modifier: Modifier = Modifier) {
    val habit = state.habit ?: return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            value = "${habit.currentStreak}",
            label = stringResource(Res.string.habit_detail_stat_current_streak)
        )
        StatItem(
            value = "${habit.longestStreak}",
            label = stringResource(Res.string.habit_detail_stat_longest_streak)
        )
        StatItem(
            value = "${state.habitScore}",
            label = stringResource(Res.string.habit_detail_stat_habit_score)
        )
    }
}

@Composable
private fun StatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SkipLimitsRow(state: HabitDetailState, modifier: Modifier = Modifier) {
    val text: String = when {
        state.maxConsecutiveSkips == null -> stringResource(Res.string.habit_detail_skips_unlimited)
        state.isSkipLocked -> stringResource(Res.string.habit_detail_skips_none)
        else -> stringResource(Res.string.habit_detail_skips_remaining, state.skipsRemaining ?: 0)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.SkipNext,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionButtons(
    state: HabitDetailState,
    onComplete: () -> Unit,
    onIncrementProgress: () -> Unit,
    onCustomProgress: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = state.habit ?: return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (habit.type == HabitType.BINARY) {
            PrimaryButton(
                onClick = onComplete,
                enabled = !state.isResolved
            ) {
                Text(
                    text = if (state.isCompleted) {
                        stringResource(Res.string.habit_detail_action_completed)
                    } else {
                        stringResource(Res.string.habit_detail_action_complete)
                    }
                )
            }
        } else {
            // Quantitative: +N button
            PrimaryButton(
                onClick = onIncrementProgress,
                enabled = !state.isResolved || !state.isQuantitativeComplete
            ) {
                val increment: Int = habit.defaultIncrement
                val unit: String = habit.unit?.uppercase() ?: ""
                Text(
                    text = if (state.isQuantitativeComplete) {
                        stringResource(Res.string.habit_detail_action_goal_reached)
                    } else {
                        "+$increment $unit"
                    }
                )
            }

            // Custom button
            OutlinedButton(
                onClick = onCustomProgress,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSkipped && !state.isFailed
            ) {
                Text(stringResource(Res.string.habit_detail_action_custom))
            }
        }

        // Skip button (both types)
        TextButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isResolved && !state.isSkipLocked
        ) {
            Text(
                text = if (state.isSkipped) {
                    stringResource(Res.string.habit_detail_action_skipped)
                } else {
                    stringResource(Res.string.habit_detail_action_skip)
                },
                style = MaterialTheme.typography.labelLarge,
                color = if (!state.isResolved && !state.isSkipLocked) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                }
            )
        }
    }
}
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileDebugSources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailScreen.kt
git commit -m "feat(habit-detail): add HabitDetailScreen composable"
```

---

### Task 7: Create HabitDetailRoute

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailRoute.kt`

- [ ] **Step 1: Create the route composable**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.rite.di.LocalAppComponent

@Composable
fun HabitDetailRoute(
    instanceId: String,
    onNavigateBack: () -> Unit,
    onCustomProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val factory = LocalAppComponent.current.habitDetailViewModelFactory
    val viewModel = remember { factory.create(instanceId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    HabitDetailScreen(
        state = state,
        onBackClick = onNavigateBack,
        onComplete = viewModel::completeBinary,
        onIncrementProgress = viewModel::incrementProgress,
        onCustomProgress = onCustomProgress,
        onSkip = viewModel::skip,
        modifier = modifier
    )
}
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileDebugSources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailRoute.kt
git commit -m "feat(habit-detail): add HabitDetailRoute"
```

---

### Task 8: Wire into Navigation and Replace Today Screen Expand

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/navigation/RiteNavigation.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/today/TodayScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/today/HabitCard.kt`

- [ ] **Step 1: Replace HabitDetail auto-pop with HabitDetailRoute in RiteNavigation.kt**

Find the `entry<HabitDetail>` block (currently has a TODO that auto-pops) and replace it:

```kotlin
entry<HabitDetail> { route ->
    val todayViewModel = LocalAppComponent.current.todayViewModel
    HabitDetailRoute(
        instanceId = route.instanceId,
        onNavigateBack = {
            backStack.removeLastOrNull()
            todayViewModel.loadTodayHabits()
        },
        onCustomProgress = {
            // TODO: open custom progress bottom sheet
        }
    )
}
```

Add the import:
```kotlin
import com.ricardocosteira.rite.presentation.ui.habitdetail.HabitDetailRoute
```

- [ ] **Step 2: Replace expand with navigate in TodayScreen.kt**

In `TodayScreen.kt`, remove the `expandedCardIds` state variable (line ~178):
```kotlin
// DELETE: var expandedCardIds: Set<String> by rememberSaveable { mutableStateOf(emptySet()) }
```

Replace all `HabitCard` calls. Change the `isExpanded` and `onToggleExpand` parameters. In both the "Today's Focus" section and the "Weekly Goals" section, update the `HabitCard` calls:

Replace:
```kotlin
isExpanded = habit.instanceId in expandedCardIds,
onToggleExpand = {
    expandedCardIds = if (habit.instanceId in expandedCardIds) {
        expandedCardIds - habit.instanceId
    } else {
        expandedCardIds + habit.instanceId
    }
},
```

With:
```kotlin
isExpanded = false,
onToggleExpand = {
    viewModel.navigateToHabitDetail(habit.instanceId)
},
```

Apply this to BOTH HabitCard call sites (daily habits and weekly habits).

Also remove the `mutableStateOf` import if it's no longer used, and `rememberSaveable` if only used for `expandedCardIds`.

- [ ] **Step 3: Build to verify**

Run: `./gradlew :composeApp:compileDebugSources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/navigation/RiteNavigation.kt \
      composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/today/TodayScreen.kt
git commit -m "feat(habit-detail): wire into navigation, replace card expand with navigate"
```

---

### Task 9: Add Heatmap Data Unit Tests

**Files:**
- Create: `composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HeatmapDataTest.kt`

- [ ] **Step 1: Create the test file**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail

import com.ricardocosteira.rite.domain.models.HabitStatus
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class HeatmapDataTest {

    @Test
    fun `given completed instance, completionPercentage is 1`() {
        val day = HeatmapDay(
            date = LocalDate(2026, 4, 1),
            completionPercentage = 1.0f,
            status = HabitStatus.COMPLETED
        )

        assertEquals(1.0f, day.completionPercentage)
        assertEquals(HabitStatus.COMPLETED, day.status)
    }

    @Test
    fun `given partial progress instance, completionPercentage reflects progress`() {
        val day = HeatmapDay(
            date = LocalDate(2026, 4, 1),
            completionPercentage = 0.5f,
            status = HabitStatus.PENDING
        )

        assertEquals(0.5f, day.completionPercentage)
    }

    @Test
    fun `given skipped instance, status is SKIPPED`() {
        val day = HeatmapDay(
            date = LocalDate(2026, 4, 1),
            completionPercentage = 0f,
            status = HabitStatus.SKIPPED
        )

        assertEquals(HabitStatus.SKIPPED, day.status)
    }
}
```

- [ ] **Step 2: Run the tests**

Run: `./gradlew :composeApp:jvmTest --tests "*HeatmapDataTest*"`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HeatmapDataTest.kt
git commit -m "test(habit-detail): add heatmap data unit tests"
```

---

### Task 10: Add HabitDetailState Unit Tests

**Files:**
- Create: `composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailStateTest.kt`

- [ ] **Step 1: Create the test file**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail

import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HabitDetailStateTest {

    private val baseHabit = Habit(
        id = "h1",
        name = "Read",
        description = null,
        type = HabitType.BINARY,
        targetValue = null,
        unit = null,
        defaultIncrement = 1,
        isTrackingEnabled = false,
        isActive = true,
        isArchived = false,
        currentStreak = 5,
        longestStreak = 10,
        totalCompletions = 20,
        expectedCompletions = 25,
        createdAt = Clock.System.now(),
        archivedAt = null
    )

    private val baseInstance = HabitInstance(
        id = "i1",
        habitId = "h1",
        date = LocalDate(2026, 4, 5),
        status = HabitStatus.PENDING,
        completedValue = null,
        targetValue = null,
        consecutiveSkipsAtCreation = 0,
        createdAt = Clock.System.now(),
        completedAt = null
    )

    @Test
    fun `given unlimited skips, skipsRemaining returns null`() {
        val state = HabitDetailState(
            habit = baseHabit,
            instance = baseInstance,
            maxConsecutiveSkips = null
        )

        assertNull(state.skipsRemaining)
    }

    @Test
    fun `given max 2 skips and 0 used, skipsRemaining returns 2`() {
        val state = HabitDetailState(
            habit = baseHabit,
            instance = baseInstance.copy(consecutiveSkipsAtCreation = 0),
            maxConsecutiveSkips = 2
        )

        assertEquals(2, state.skipsRemaining)
    }

    @Test
    fun `given max 2 skips and 2 used, skipsRemaining returns 0`() {
        val state = HabitDetailState(
            habit = baseHabit,
            instance = baseInstance.copy(consecutiveSkipsAtCreation = 2),
            maxConsecutiveSkips = 2
        )

        assertEquals(0, state.skipsRemaining)
    }

    @Test
    fun `given completed instance, isCompleted returns true`() {
        val state = HabitDetailState(
            instance = baseInstance.copy(status = HabitStatus.COMPLETED)
        )

        assertTrue(state.isCompleted)
        assertTrue(state.isResolved)
    }

    @Test
    fun `given pending instance, isResolved returns false`() {
        val state = HabitDetailState(
            instance = baseInstance.copy(status = HabitStatus.PENDING)
        )

        assertFalse(state.isResolved)
    }

    @Test
    fun `given skipped instance, isSkipped returns true`() {
        val state = HabitDetailState(
            instance = baseInstance.copy(status = HabitStatus.SKIPPED)
        )

        assertTrue(state.isSkipped)
        assertTrue(state.isResolved)
    }

    @Test
    fun `given failed instance, isFailed returns true`() {
        val state = HabitDetailState(
            instance = baseInstance.copy(status = HabitStatus.FAILED)
        )

        assertTrue(state.isFailed)
        assertTrue(state.isResolved)
    }
}
```

- [ ] **Step 2: Run the tests**

Run: `./gradlew :composeApp:jvmTest --tests "*HabitDetailStateTest*"`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailStateTest.kt
git commit -m "test(habit-detail): add HabitDetailState unit tests"
```

---

### Task 11: Add Screenshot Tests

**Files:**
- Create: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailScreenScreenshotTest.kt`

- [ ] **Step 1: Create the screenshot test**

```kotlin
package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w360dp-h800dp-420dpi", application = android.app.Application::class)
class HabitDetailScreenScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val binaryHabit = Habit(
        id = "h1",
        name = "Meditate",
        description = "Daily meditation",
        type = HabitType.BINARY,
        targetValue = null,
        unit = null,
        defaultIncrement = 1,
        isTrackingEnabled = false,
        isActive = true,
        isArchived = false,
        currentStreak = 14,
        longestStreak = 42,
        totalCompletions = 80,
        expectedCompletions = 90,
        createdAt = Clock.System.now(),
        archivedAt = null
    )

    private val quantitativeHabit = binaryHabit.copy(
        id = "h2",
        name = "Read",
        description = "Read 30 pages daily",
        type = HabitType.QUANTITATIVE,
        targetValue = 30,
        unit = "pages"
    )

    private val pendingBinaryInstance = HabitInstance(
        id = "i1",
        habitId = "h1",
        date = LocalDate(2026, 4, 5),
        status = HabitStatus.PENDING,
        completedValue = null,
        targetValue = null,
        consecutiveSkipsAtCreation = 0,
        createdAt = Clock.System.now(),
        completedAt = null
    )

    private val completedBinaryInstance = pendingBinaryInstance.copy(
        status = HabitStatus.COMPLETED,
        completedAt = Clock.System.now()
    )

    private val inProgressQuantInstance = pendingBinaryInstance.copy(
        id = "i2",
        habitId = "h2",
        completedValue = 22,
        targetValue = 30
    )

    private val completedQuantInstance = inProgressQuantInstance.copy(
        status = HabitStatus.COMPLETED,
        completedValue = 30,
        completedAt = Clock.System.now()
    )

    private val sampleHeatmap: List<HeatmapDay> = (0..89).map { daysAgo ->
        val date = LocalDate(2026, 4, 5).minus(kotlinx.datetime.DatePeriod(days = daysAgo))
        HeatmapDay(
            date = date,
            completionPercentage = if (daysAgo % 3 == 0) 1.0f else if (daysAgo % 2 == 0) 0.5f else 0f,
            status = if (daysAgo % 3 == 0) HabitStatus.COMPLETED else HabitStatus.PENDING
        )
    }

    @Test
    fun habitDetail_binaryPending_darkTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = true) {
                HabitDetailScreen(
                    state = HabitDetailState(
                        habit = binaryHabit,
                        instance = pendingBinaryInstance,
                        maxConsecutiveSkips = 2,
                        heatmapData = sampleHeatmap,
                        isLoading = false
                    ),
                    onBackClick = {},
                    onComplete = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onSkip = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitDetail_binaryPending_lightTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                HabitDetailScreen(
                    state = HabitDetailState(
                        habit = binaryHabit,
                        instance = pendingBinaryInstance,
                        maxConsecutiveSkips = 2,
                        heatmapData = sampleHeatmap,
                        isLoading = false
                    ),
                    onBackClick = {},
                    onComplete = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onSkip = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitDetail_quantitativeInProgress_darkTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = true) {
                HabitDetailScreen(
                    state = HabitDetailState(
                        habit = quantitativeHabit,
                        instance = inProgressQuantInstance,
                        maxConsecutiveSkips = 2,
                        heatmapData = sampleHeatmap,
                        isLoading = false
                    ),
                    onBackClick = {},
                    onComplete = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onSkip = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitDetail_quantitativeInProgress_lightTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                HabitDetailScreen(
                    state = HabitDetailState(
                        habit = quantitativeHabit,
                        instance = inProgressQuantInstance,
                        maxConsecutiveSkips = 2,
                        heatmapData = sampleHeatmap,
                        isLoading = false
                    ),
                    onBackClick = {},
                    onComplete = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onSkip = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
```

- [ ] **Step 2: Record golden screenshots**

Run: `./gradlew :composeApp:recordRoborazziDebug`
Expected: BUILD SUCCESSFUL, 4 new PNG files

- [ ] **Step 3: Verify screenshots pass**

Run: `./gradlew :composeApp:verifyRoborazziDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/rite/presentation/ui/habitdetail/HabitDetailScreenScreenshotTest.kt \
      composeApp/src/androidUnitTest/snapshots/images/
git commit -m "test(habit-detail): add screenshot tests"
```

---

### Task 12: Update BACKLOG.md

**Files:**
- Modify: `BACKLOG.md`

- [ ] **Step 1: Mark habit detail screen as done**

Change:
```
- [ ] Habit detail screen (streaks, score history, completion stats)
```
to:
```
- [x] Habit detail screen (streaks, score history, completion stats)
```

- [ ] **Step 2: Commit**

```bash
git add BACKLOG.md
git commit -m "docs: mark habit detail screen as done"
```
