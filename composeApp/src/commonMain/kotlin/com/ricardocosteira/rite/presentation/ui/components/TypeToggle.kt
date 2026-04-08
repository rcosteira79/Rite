package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.labelRes
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource

private val ContainerShape = RoundedCornerShape(22.dp)
private val IndicatorShape = RoundedCornerShape(18.dp)
private val IndicatorPadding = 4.dp
private const val ANIMATION_DURATION_MS = 250

@Composable
fun TypeToggle(
    selected: HabitType,
    onSelectionChange: (HabitType) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme: Boolean = isSystemInDarkTheme()
    val selectedIndex: Int = HabitType.entries.indexOf(selected)
    val itemCount: Int = HabitType.entries.size

    val targetFraction: Float = selectedIndex.toFloat() / itemCount
    val animatedFraction: Float by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = ANIMATION_DURATION_MS),
        label = "indicatorFraction"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(ContainerShape)
            .background(RiteAppTheme.colorScheme.surfaceContainerLow)
            .then(
                if (isDarkTheme) {
                    Modifier.border(1.dp, RiteAppTheme.colorScheme.outlineVariant, ContainerShape)
                } else {
                    Modifier
                }
            )
    ) {
        val containerWidth = maxWidth
        val indicatorWidth = (containerWidth - IndicatorPadding * 2) / itemCount
        val indicatorOffset = IndicatorPadding + indicatorWidth * animatedFraction * itemCount

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .padding(vertical = IndicatorPadding)
                .width(indicatorWidth)
                .fillMaxHeight()
                .clip(IndicatorShape)
                .background(RiteAppTheme.colorScheme.primaryContainer)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            HabitType.entries.forEach { type ->
                val isSelected: Boolean = selected == type
                val contentColor = if (isSelected) {
                    RiteAppTheme.colorScheme.onPrimaryContainer
                } else {
                    RiteAppTheme.colorScheme.onSurfaceVariant
                }
                val label: String = stringResource(type.labelRes)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectionChange(type) }
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = RiteAppTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = contentColor
                    )
                }
            }
        }
    }
}
