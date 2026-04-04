package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.quantity_stepper_cd_decrease
import habitlock.composeapp.generated.resources.quantity_stepper_cd_increase
import org.jetbrains.compose.resources.stringResource

private val ButtonSize = 36.dp
private const val MIN_VALUE = 1
private const val DISABLED_CONTENT_ALPHA = 0.38f

@Composable
fun QuantityStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val buttonBackground = MaterialTheme.colorScheme.surfaceContainerLow
    val buttonBorder = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = { if (value > MIN_VALUE) onValueChange(value - 1) },
            enabled = value > MIN_VALUE,
            modifier = Modifier
                .size(ButtonSize)
                .clip(CircleShape)
                .background(buttonBackground)
                .then(
                    if (isDarkTheme) {
                        Modifier.border(1.dp, buttonBorder, CircleShape)
                    } else {
                        Modifier
                    }
                )
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = stringResource(Res.string.quantity_stepper_cd_decrease),
                tint = if (value > MIN_VALUE) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
                }
            )
        }

        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(
            onClick = { onValueChange(value + 1) },
            modifier = Modifier
                .size(ButtonSize)
                .clip(CircleShape)
                .background(buttonBackground)
                .then(
                    if (isDarkTheme) {
                        Modifier.border(1.dp, buttonBorder, CircleShape)
                    } else {
                        Modifier
                    }
                )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(Res.string.quantity_stepper_cd_increase),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
