package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

private val SIZE = 48.dp
private const val DISABLED_ALPHA = 0.38f

/**
 * Square 48dp tappable stepper button used by quantity controls (e.g. − / + in the habit
 * form's [QuantityStepper] and the habit detail action area).
 *
 * Renders [text] centred over a surface with [RiteAppTheme.shapes.lg] corners. Animates
 * `shadowElevation` between 2.dp (resting) and 6.dp (pressed) so the button feels tactile.
 */
@Composable
fun StepperButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val isPressed: Boolean by interactionSource.collectIsPressedAsState()
    val elevation: Dp by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 2.dp,
        label = "stepperButtonElevation"
    )

    Surface(
        onClick = onClick,
        shape = RiteAppTheme.shapes.lg,
        color = RiteAppTheme.colors.surface,
        shadowElevation = elevation,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier.size(SIZE)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = text,
                style = RiteAppTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = if (enabled) {
                    RiteAppTheme.colors.onSurface
                } else {
                    RiteAppTheme.colors.onSurface.copy(alpha = DISABLED_ALPHA)
                }
            )
        }
    }
}
