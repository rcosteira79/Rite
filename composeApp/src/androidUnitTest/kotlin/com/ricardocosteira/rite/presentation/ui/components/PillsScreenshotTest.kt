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
class PillsScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun ritePill_light() = renderPill(dark = false)

    @Test
    fun ritePill_dark() = renderPill(dark = true)

    @Test
    fun strictnessPill_balanced_light() = renderStrictness(StrictnessPreset.Balanced, dark = false)

    @Test
    fun strictnessPill_balanced_dark() = renderStrictness(StrictnessPreset.Balanced, dark = true)

    @Test
    fun strictnessPill_unwavering_light() =
        renderStrictness(StrictnessPreset.Unwavering, dark = false)

    private fun renderPill(dark: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                RitePill(text = "DONE")
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    private fun renderStrictness(preset: StrictnessPreset, dark: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                // animated=false makes the golden deterministic
                StrictnessPill(preset = preset, animated = false)
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
