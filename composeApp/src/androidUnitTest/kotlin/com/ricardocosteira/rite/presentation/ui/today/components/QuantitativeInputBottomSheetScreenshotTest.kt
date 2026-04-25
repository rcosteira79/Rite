package com.ricardocosteira.rite.presentation.ui.today.components

import androidx.compose.material3.ExperimentalMaterial3Api
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

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [33],
    qualifiers = "w360dp-h800dp-420dpi",
    application = android.app.Application::class
)
class QuantitativeInputBottomSheetScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun sheet_with_unit_and_target() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                QuantitativeInputBottomSheet(
                    name = "Read before sleep",
                    completedValue = 12,
                    targetValue = 30,
                    unit = "pg",
                    defaultIncrement = 5,
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.onNode(isDialog()).captureRoboImage()
    }

    @Test
    fun sheet_no_unit() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                QuantitativeInputBottomSheet(
                    name = "Pushups",
                    completedValue = 0,
                    targetValue = 100,
                    unit = null,
                    defaultIncrement = 10,
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.onNode(isDialog()).captureRoboImage()
    }

    @Test
    fun sheet_dark() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = true) {
                QuantitativeInputBottomSheet(
                    name = "Read before sleep",
                    completedValue = 12,
                    targetValue = 30,
                    unit = "pg",
                    defaultIncrement = 5,
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.onNode(isDialog()).captureRoboImage()
    }
}
