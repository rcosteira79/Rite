package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
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
class ThemeTokensPreviewScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tokens_light() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) { ThemeTokensPreview() }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun tokens_dark() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = true) { ThemeTokensPreview() }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
