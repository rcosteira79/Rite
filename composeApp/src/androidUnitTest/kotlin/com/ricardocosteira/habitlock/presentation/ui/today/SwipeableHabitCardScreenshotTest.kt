package com.ricardocosteira.habitlock.presentation.ui.today

import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.habitlock.presentation.ui.theme.HabitLockThemeFallback
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
class SwipeableHabitCardScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    // --- EDIT zone ---

    @Test
    fun swipeBackground_edit_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                SwipeBackground(
                    isArmed = true,
                    revealFraction = 1f,
                    zone = SwipeAction.EDIT,
                    modifier = Modifier.height(72.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun swipeBackground_edit_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                SwipeBackground(
                    isArmed = true,
                    revealFraction = 1f,
                    zone = SwipeAction.EDIT,
                    modifier = Modifier.height(72.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- DELETE zone ---

    @Test
    fun swipeBackground_delete_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                SwipeBackground(
                    isArmed = true,
                    revealFraction = 1f,
                    zone = SwipeAction.DELETE,
                    modifier = Modifier.height(72.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun swipeBackground_delete_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                SwipeBackground(
                    isArmed = true,
                    revealFraction = 1f,
                    zone = SwipeAction.DELETE,
                    modifier = Modifier.height(72.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
