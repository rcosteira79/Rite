package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.material3.Text
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
class RiteSnackbarScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun completed_light() = render(RiteSnackbarVariant.Completed, dark = false)

    @Test fun completed_dark() = render(RiteSnackbarVariant.Completed, dark = true)

    @Test fun skipped_light() = render(RiteSnackbarVariant.Skipped, dark = false)

    @Test fun skipped_dark() = render(RiteSnackbarVariant.Skipped, dark = true)

    @Test fun failed_light() = render(RiteSnackbarVariant.Failed, dark = false)

    @Test fun failed_dark() = render(RiteSnackbarVariant.Failed, dark = true)

    @Test fun suspended_light() = render(RiteSnackbarVariant.Suspended, dark = false)

    @Test fun suspended_dark() = render(RiteSnackbarVariant.Suspended, dark = true)

    private fun render(variant: RiteSnackbarVariant, dark: Boolean) {
        val content = when (variant) {
            RiteSnackbarVariant.Completed -> RiteSnackbarContent(
                prefix = "Completed ",
                emphasized = "Morning sit",
                suffix = ". Streak → 15 days.",
                action = { Text("UNDO") }
            )

            RiteSnackbarVariant.Skipped -> RiteSnackbarContent(
                prefix = "Skipped ",
                emphasized = "Strength work",
                suffix = ". 1 skip remains this week.",
                action = { Text("UNDO") }
            )

            RiteSnackbarVariant.Failed -> RiteSnackbarContent(
                prefix = "Missed ",
                emphasized = "Morning sit",
                suffix = ". The 14-day streak resets at midnight.",
                subtext = "Tomorrow is a new page."
            )

            RiteSnackbarVariant.Suspended -> RiteSnackbarContent(
                prefix = "On leave until Monday. ",
                emphasized = "7 rituals",
                suffix = " paused.",
                action = { Text("END LEAVE") }
            )
        }
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                RiteSnackbar(variant = variant, content = content)
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
