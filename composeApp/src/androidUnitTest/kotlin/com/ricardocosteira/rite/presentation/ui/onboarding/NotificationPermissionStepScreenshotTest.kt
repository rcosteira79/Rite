package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
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
class NotificationPermissionStepScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun notificationPermissionStep_lightTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                NotificationPermissionStep(reduceMotion = true)
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun notificationPermissionStep_darkTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = true) {
                NotificationPermissionStep(reduceMotion = true)
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
