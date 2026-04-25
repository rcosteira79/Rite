package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
class ChipAndDividerScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun letterChips_light() = renderLetters(dark = false)

    @Test
    fun letterChips_dark() = renderLetters(dark = true)

    @Test
    fun shortcutChips_light() = renderShortcuts(dark = false)

    @Test
    fun shortcutChips_dark() = renderShortcuts(dark = true)

    @Test
    fun divider_light() = renderDivider(dark = false)

    @Test
    fun divider_dark() = renderDivider(dark = true)

    private fun renderLetters(dark: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(12.dp)
                ) {
                    listOf(
                        "M" to true,
                        "T" to false,
                        "W" to true,
                        "T" to false,
                        "F" to true,
                        "S" to false,
                        "S" to false
                    )
                        .forEach { (l, sel) -> RiteLetterChip(l, selected = sel, onClick = {}) }
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    private fun renderShortcuts(dark: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(12.dp)
                ) {
                    listOf("Every day" to false, "Weekdays" to true, "Weekend" to false)
                        .forEach { (t, sel) -> RiteShortcutChip(t, selected = sel, onClick = {}) }
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    private fun renderDivider(dark: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                Column(modifier = Modifier.padding(12.dp)) {
                    RiteDivider()
                    RiteDivider(strong = true, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
