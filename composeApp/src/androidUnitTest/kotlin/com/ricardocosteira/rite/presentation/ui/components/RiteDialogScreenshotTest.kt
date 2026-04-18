package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
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
class RiteDialogScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dialog_destructive_light() = render(dark = false, destructive = true)

    @Test
    fun dialog_destructive_dark() = render(dark = true, destructive = true)

    private fun render(dark: Boolean, destructive: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                RiteDialog(
                    title = "Delete habit?",
                    message = "Archived habits can be restored, but deleted habits cannot.",
                    confirmLabel = "Delete",
                    dismissLabel = "Cancel",
                    onConfirm = {},
                    onDismiss = {},
                    destructive = destructive
                )
            }
        }
        composeRule.onNode(isDialog()).captureRoboImage()
    }
}
