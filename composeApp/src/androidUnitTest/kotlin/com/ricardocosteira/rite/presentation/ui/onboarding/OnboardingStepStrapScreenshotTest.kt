package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import kotlinx.collections.immutable.persistentListOf
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
class OnboardingStepStrapScreenshotTest {
    @get:Rule val composeRule = createComposeRule()

    private val allLabels =
        persistentListOf("Philosophy", "Strictness", "First habit", "Notifications")

    @Test fun philosophy_step_lightTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                OnboardingStepStrap(
                    step = 1,
                    totalSteps = 4,
                    stepName = "Philosophy",
                    allStepNames = allLabels,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test fun first_habit_step_darkTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = true) {
                OnboardingStepStrap(
                    step = 3,
                    totalSteps = 4,
                    stepName = "First habit",
                    allStepNames = allLabels,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
