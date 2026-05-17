package com.ricardocosteira.rite.presentation.ui.onboarding

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
@Config(
    sdk = [33],
    qualifiers = "w360dp-h800dp-420dpi",
    application = android.app.Application::class
)
class OnboardingStepIndicatorScreenshotTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun step1of4_lightTheme() = renderAndCapture(step = 1, total = 4, dark = false)

    @Test fun step2of4_lightTheme() = renderAndCapture(step = 2, total = 4, dark = false)

    @Test fun step4of4_darkTheme() = renderAndCapture(step = 4, total = 4, dark = true)

    @Test fun step1of3_lightTheme() = renderAndCapture(step = 1, total = 3, dark = false)

    private fun renderAndCapture(step: Int, total: Int, dark: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                OnboardingStepIndicator(
                    step = step,
                    totalSteps = total,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
