package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

private val StepperButtonSize = 48.dp
private val CardCorner = 16.dp
private val ButtonCorner = 12.dp
private const val MIN_VALUE = 1
private const val DISABLED_CONTENT_ALPHA = 0.38f

@Composable
fun QuantityStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    step: Int = 1
) {
    val effectiveStep: Int = step.coerceAtLeast(1)
    val canDecrement: Boolean = value - effectiveStep >= MIN_VALUE

    Surface(
        shape = RoundedCornerShape(CardCorner),
        color = RiteAppTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StepperButton(
                text = "−",
                onClick = { onValueChange((value - effectiveStep).coerceAtLeast(MIN_VALUE)) },
                enabled = canDecrement
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value.toString(),
                    style = RiteAppTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = RiteAppTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = RiteAppTheme.typography.labelSmall,
                    color = RiteAppTheme.colorScheme.onSurfaceVariant
                )
            }

            StepperButton(
                text = "+",
                onClick = { onValueChange(value + effectiveStep) }
            )
        }
    }
}

@Composable
private fun StepperButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(ButtonCorner),
        color = RiteAppTheme.colorScheme.surface,
        enabled = enabled,
        modifier = modifier.size(StepperButtonSize)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = text,
                style = RiteAppTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = if (enabled) {
                    RiteAppTheme.colorScheme.onSurface
                } else {
                    RiteAppTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
                }
            )
        }
    }
}
