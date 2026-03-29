package com.ricardocosteira.habitlock.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.presentation.ui.labelRes
import org.jetbrains.compose.resources.stringResource

private val PillShape = RoundedCornerShape(22.dp)

@Composable
fun TypeToggle(
    selected: HabitType,
    onSelectionChange: (HabitType) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HabitType.entries.forEach { type ->
            val isSelected = selected == type
            val backgroundColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
            val contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
            val label = stringResource(type.labelRes)

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .clip(PillShape)
                        .background(backgroundColor)
                        .then(
                            if (!isSelected && isDarkTheme) {
                                Modifier.border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    PillShape
                                )
                            } else {
                                Modifier
                            }
                        ).clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectionChange(type) }
                        ).padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = contentColor
                )
            }
        }
    }
}
