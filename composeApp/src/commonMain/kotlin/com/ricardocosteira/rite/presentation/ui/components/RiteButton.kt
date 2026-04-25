package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

enum class RiteButtonVariant { Primary, Secondary, Ghost }

@Composable
fun RiteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: RiteButtonVariant = RiteButtonVariant.Primary,
    content: @Composable RowScope.() -> Unit
) {
    val colors = RiteAppTheme.colors
    val shape = RiteAppTheme.shapes.sm
    val rowModifier = modifier.fillMaxWidth().heightIn(min = 48.dp)

    when (variant) {
        RiteButtonVariant.Primary -> Button(
            onClick = onClick,
            modifier = rowModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.onSurface, // ink background (inverse of surface)
                contentColor = colors.surface, // surface-coloured text
                disabledContainerColor = colors.onSurfaceSubtle,
                disabledContentColor = colors.surface
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            content = content
        )

        RiteButtonVariant.Secondary -> OutlinedButton(
            onClick = onClick,
            modifier = rowModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.onSurface,
                disabledContentColor = colors.onSurfaceSubtle
            ),
            border = BorderStroke(1.dp, colors.outline),
            content = content
        )

        RiteButtonVariant.Ghost -> TextButton(
            onClick = onClick,
            modifier = rowModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.textButtonColors(
                contentColor = colors.onSurface,
                disabledContentColor = colors.onSurfaceSubtle
            ),
            content = content
        )
    }
}
