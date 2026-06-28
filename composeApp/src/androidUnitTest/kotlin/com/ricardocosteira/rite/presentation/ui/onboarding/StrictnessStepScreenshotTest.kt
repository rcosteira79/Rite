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
class StrictnessStepScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun balanced_open_lightTheme() =
        render(preset = OnboardingStrictnessPreset.BALANCED, dark = false)

    @Test
    fun balanced_open_darkTheme() =
        render(preset = OnboardingStrictnessPreset.BALANCED, dark = true)

    @Test
    fun flexible_open_lightTheme() =
        render(preset = OnboardingStrictnessPreset.FLEXIBLE, dark = false)

    @Test
    fun flexible_open_darkTheme() =
        render(preset = OnboardingStrictnessPreset.FLEXIBLE, dark = true)

    @Test
    fun unwavering_open_lightTheme() =
        render(preset = OnboardingStrictnessPreset.UNWAVERING, dark = false)

    @Test
    fun unwavering_open_darkTheme() =
        render(preset = OnboardingStrictnessPreset.UNWAVERING, dark = true)

    private fun render(preset: OnboardingStrictnessPreset, dark: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                StrictnessStep(
                    selectedPreset = preset,
                    onPresetSelected = {},
                    reduceMotion = true,
                    modifier = Modifier.padding(0.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
