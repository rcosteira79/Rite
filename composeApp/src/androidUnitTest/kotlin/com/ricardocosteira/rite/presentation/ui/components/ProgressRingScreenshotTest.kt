package com.ricardocosteira.rite.presentation.ui.components

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
class ProgressRingScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun ring_zero_light() = render(0f, dark = false)

    @Test
    fun ring_sixty_light() = render(0.62f, dark = false)

    @Test
    fun ring_full_light() = render(1f, dark = false)

    @Test
    fun ring_sixty_dark() = render(0.62f, dark = true)

    private fun render(progress: Float, dark: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                ProgressRing(progress = progress)
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
