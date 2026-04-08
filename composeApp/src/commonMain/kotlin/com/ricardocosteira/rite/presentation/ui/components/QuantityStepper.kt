package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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

private val MainButtonSize = 48.dp
private val FineButtonSize = 32.dp
private val CardCorner = 16.dp
private val MainButtonCorner = 12.dp
private val FineButtonCorner = 8.dp
private const val MIN_VALUE = 1
private const val DISABLED_CONTENT_ALPHA = 0.38f
private const val FINE_STEP = 1

@Composable
fun QuantityStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    step: Int = 1
) {
    val effectiveStep: Int = step.coerceAtLeast(1)
    val showFineControls: Boolean = effectiveStep > 1
    val canDecrement: Boolean = value - effectiveStep >= MIN_VALUE
    val canFineDecrement: Boolean = value > MIN_VALUE

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(
                    visible = showFineControls,
                    enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                    exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FineStepperButton(
                            text = "−1",
                            onClick = {
                                onValueChange((value - FINE_STEP).coerceAtLeast(MIN_VALUE))
                            },
                            enabled = canFineDecrement
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                StepperButton(
                    text = "−",
                    onClick = {
                        onValueChange((value - effectiveStep).coerceAtLeast(MIN_VALUE))
                    },
                    enabled = canDecrement
                )
            }

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

            Row(verticalAlignment = Alignment.CenterVertically) {
                StepperButton(
                    text = "+",
                    onClick = { onValueChange(value + effectiveStep) }
                )

                AnimatedVisibility(
                    visible = showFineControls,
                    enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
                    exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(4.dp))
                        FineStepperButton(
                            text = "+1",
                            onClick = { onValueChange(value + FINE_STEP) }
                        )
                    }
                }
            }
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
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val isPressed: Boolean by interactionSource.collectIsPressedAsState()
    val elevation: Dp by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 2.dp,
        label = "stepperButtonElevation"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(MainButtonCorner),
        color = RiteAppTheme.colorScheme.surface,
        shadowElevation = elevation,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier.size(MainButtonSize)
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

@Composable
private fun FineStepperButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(FineButtonCorner),
        color = RiteAppTheme.colorScheme.surfaceContainer,
        enabled = enabled,
        modifier = modifier.size(FineButtonSize)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = text,
                style = RiteAppTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (enabled) {
                    RiteAppTheme.colorScheme.onSurfaceVariant
                } else {
                    RiteAppTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
                }
            )
        }
    }
}
