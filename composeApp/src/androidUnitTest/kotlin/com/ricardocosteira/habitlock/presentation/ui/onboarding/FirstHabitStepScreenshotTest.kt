package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.presentation.ui.theme.HabitLockThemeFallback
import kotlinx.datetime.DayOfWeek
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], application = android.app.Application::class)
class FirstHabitStepScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    // --- Light theme ---

    @Test
    fun firstHabitStep_binary_everyDay_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                FirstHabitStep(
                    habitName = "",
                    habitType = HabitType.BINARY,
                    targetValue = "",
                    unit = "",
                    scheduleOption = ScheduleOption.EVERY_DAY,
                    customDays = emptySet(),
                    onHabitNameChange = {},
                    onHabitTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleOptionChange = {},
                    onCustomDaysChange = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun firstHabitStep_quantitative_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                FirstHabitStep(
                    habitName = "Run",
                    habitType = HabitType.QUANTITATIVE,
                    targetValue = "5",
                    unit = "km",
                    scheduleOption = ScheduleOption.WEEKDAYS,
                    customDays = emptySet(),
                    onHabitNameChange = {},
                    onHabitTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleOptionChange = {},
                    onCustomDaysChange = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun firstHabitStep_customSchedule_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                FirstHabitStep(
                    habitName = "Meditate",
                    habitType = HabitType.BINARY,
                    targetValue = "",
                    unit = "",
                    scheduleOption = ScheduleOption.CUSTOM,
                    customDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                    onHabitNameChange = {},
                    onHabitTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleOptionChange = {},
                    onCustomDaysChange = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Dark theme ---

    @Test
    fun firstHabitStep_binary_everyDay_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                FirstHabitStep(
                    habitName = "",
                    habitType = HabitType.BINARY,
                    targetValue = "",
                    unit = "",
                    scheduleOption = ScheduleOption.EVERY_DAY,
                    customDays = emptySet(),
                    onHabitNameChange = {},
                    onHabitTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleOptionChange = {},
                    onCustomDaysChange = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun firstHabitStep_quantitative_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                FirstHabitStep(
                    habitName = "Run",
                    habitType = HabitType.QUANTITATIVE,
                    targetValue = "5",
                    unit = "km",
                    scheduleOption = ScheduleOption.WEEKDAYS,
                    customDays = emptySet(),
                    onHabitNameChange = {},
                    onHabitTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleOptionChange = {},
                    onCustomDaysChange = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun firstHabitStep_customSchedule_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                FirstHabitStep(
                    habitName = "Meditate",
                    habitType = HabitType.BINARY,
                    targetValue = "",
                    unit = "",
                    scheduleOption = ScheduleOption.CUSTOM,
                    customDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                    onHabitNameChange = {},
                    onHabitTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleOptionChange = {},
                    onCustomDaysChange = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
