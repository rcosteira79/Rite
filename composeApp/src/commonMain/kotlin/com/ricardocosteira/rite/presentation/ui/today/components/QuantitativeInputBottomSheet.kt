package com.ricardocosteira.rite.presentation.ui.today.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.components.RiteButton
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.quantitative_input_cd_decrement
import rite.composeapp.generated.resources.quantitative_input_cd_increment
import rite.composeapp.generated.resources.quantitative_input_quick_add_label
import rite.composeapp.generated.resources.quantitative_input_save
import rite.composeapp.generated.resources.quantitative_input_subtitle_no_unit
import rite.composeapp.generated.resources.quantitative_input_subtitle_with_unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantitativeInputBottomSheet(
    name: String,
    completedValue: Int?,
    targetValue: Int?,
    unit: String?,
    defaultIncrement: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val initial = defaultIncrement.coerceAtLeast(1)
    var value by remember { mutableStateOf(initial) }
    val chipValues = rememberQuickAddValues(defaultIncrement)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = RiteAppTheme.colors.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = RiteAppTheme.spacing.gap6)
                .padding(top = RiteAppTheme.spacing.gap3, bottom = RiteAppTheme.spacing.gap6)
        ) {
            SheetHandle()
            Spacer(Modifier.height(RiteAppTheme.spacing.gap4))
            Text(
                text = name,
                style = RiteAppTheme.typography.titleLarge,
                color = RiteAppTheme.colors.onSurface
            )
            Text(
                text = subtitleFor(unit, targetValue),
                style = RiteAppTheme.typography.mono,
                color = RiteAppTheme.colors.onSurfaceSubtle,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(Modifier.height(RiteAppTheme.spacing.gap5))

            Row(
                horizontalArrangement = Arrangement.spacedBy(RiteAppTheme.spacing.gap6),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                StepperButton(
                    imageVector = Icons.Default.Remove,
                    contentDescription = stringResource(Res.string.quantitative_input_cd_decrement),
                    onClick = { value = (value - chipValues.step).coerceAtLeast(1) }
                )
                Text(
                    text = "$value",
                    style = RiteAppTheme.typography.displayLarge,
                    color = RiteAppTheme.colors.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                StepperButton(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.quantitative_input_cd_increment),
                    onClick = { value += chipValues.step }
                )
            }

            Spacer(Modifier.height(RiteAppTheme.spacing.gap4))

            Row(
                horizontalArrangement = Arrangement.spacedBy(RiteAppTheme.spacing.gap2),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                chipValues.chips.forEach { n ->
                    QuickAddChip(
                        label = stringResource(Res.string.quantitative_input_quick_add_label, n),
                        onClick = { value = n }
                    )
                }
            }

            Spacer(Modifier.height(RiteAppTheme.spacing.gap6))

            RiteButton(
                onClick = { if (value > 0) onConfirm(value) },
                enabled = value > 0
            ) {
                Text(stringResource(Res.string.quantitative_input_save))
            }
        }
    }
}

@Composable
private fun subtitleFor(unit: String?, target: Int?): String = when {
    unit != null && target != null ->
        stringResource(Res.string.quantitative_input_subtitle_with_unit, unit, target)

    target != null ->
        stringResource(Res.string.quantitative_input_subtitle_no_unit, target)

    else -> ""
}

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(width = 36.dp, height = 3.dp),
            shape = RiteAppTheme.shapes.pill,
            color = RiteAppTheme.colors.outline
        ) {}
    }
}

@Composable
private fun StepperButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Transparent,
        contentColor = RiteAppTheme.colors.onSurface,
        border = BorderStroke(1.dp, RiteAppTheme.colors.outline),
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun QuickAddChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RiteAppTheme.shapes.pill,
        color = Color.Transparent,
        contentColor = RiteAppTheme.colors.onSurface,
        border = BorderStroke(1.dp, RiteAppTheme.colors.outline)
    ) {
        Text(
            text = label,
            style = RiteAppTheme.typography.mono,
            modifier = Modifier.padding(
                horizontal = RiteAppTheme.spacing.gap3,
                vertical = RiteAppTheme.spacing.gap2
            )
        )
    }
}

private data class QuickAddValues(val step: Int, val chips: List<Int>)

@Composable
private fun rememberQuickAddValues(defaultIncrement: Int): QuickAddValues =
    remember(defaultIncrement) {
        val step = defaultIncrement.coerceAtLeast(1)
        // Chip preset: 1x, 2x, 5x, 10x the default step. Dedup + order ascending.
        val raw = listOf(step, step * 2, step * 5, step * 10).distinct().sorted()
        QuickAddValues(step = step, chips = raw)
    }
