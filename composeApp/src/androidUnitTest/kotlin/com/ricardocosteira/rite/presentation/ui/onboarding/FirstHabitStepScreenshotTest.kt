package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
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
@Config(
    sdk = [33],
    qualifiers = "w360dp-h800dp-420dpi",
    application = android.app.Application::class
)
class FirstHabitStepScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    // --- Light theme ---

    @Test
    fun firstHabitStep_empty_lightTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                FirstHabitStep(
                    habitName = "",
                    habitType = HabitType.BINARY,
                    targetValue = "",
                    unit = "",
                    scheduleKind = OnboardingScheduleKind.DAILY,
                    onHabitNameChange = {},
                    onHabitTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleKindChange = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun firstHabitStep_filledBinary_lightTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                FirstHabitStep(
                    habitName = "Meditate",
                    habitType = HabitType.BINARY,
                    targetValue = "",
                    unit = "",
                    scheduleKind = OnboardingScheduleKind.WEEKLY,
                    onHabitNameChange = {},
                    onHabitTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleKindChange = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun firstHabitStep_filledQuantitative_lightTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                FirstHabitStep(
                    habitName = "Run",
                    habitType = HabitType.QUANTITATIVE,
                    targetValue = "5",
                    unit = "km",
                    scheduleKind = OnboardingScheduleKind.DAILY,
                    onHabitNameChange = {},
                    onHabitTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleKindChange = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Dark theme ---

    @Test
    fun firstHabitStep_empty_darkTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = true) {
                FirstHabitStep(
                    habitName = "",
                    habitType = HabitType.BINARY,
                    targetValue = "",
                    unit = "",
                    scheduleKind = OnboardingScheduleKind.DAILY,
                    onHabitNameChange = {},
                    onHabitTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleKindChange = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun firstHabitStep_filledBinary_darkTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = true) {
                FirstHabitStep(
                    habitName = "Meditate",
                    habitType = HabitType.BINARY,
                    targetValue = "",
                    unit = "",
                    scheduleKind = OnboardingScheduleKind.WEEKLY,
                    onHabitNameChange = {},
                    onHabitTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleKindChange = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun firstHabitStep_filledQuantitative_darkTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = true) {
                FirstHabitStep(
                    habitName = "Run",
                    habitType = HabitType.QUANTITATIVE,
                    targetValue = "5",
                    unit = "km",
                    scheduleKind = OnboardingScheduleKind.DAILY,
                    onHabitNameChange = {},
                    onHabitTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleKindChange = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
