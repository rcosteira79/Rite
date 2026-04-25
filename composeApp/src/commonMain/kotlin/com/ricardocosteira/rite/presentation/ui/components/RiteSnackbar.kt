package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PauseCircleOutline
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

enum class RiteSnackbarVariant(val icon: ImageVector) {
    Completed(Icons.Outlined.CheckCircle),
    Skipped(Icons.Outlined.SkipNext),
    Failed(Icons.Outlined.ErrorOutline),
    Suspended(Icons.Outlined.PauseCircleOutline)
}

data class RiteSnackbarContent(
    val prefix: String,
    val emphasized: String, // rendered in displayItalic + accent tint
    val suffix: String,
    val subtext: String? = null,
    val action: (@Composable () -> Unit)? = null
)

@Composable
fun RiteSnackbar(
    variant: RiteSnackbarVariant,
    content: RiteSnackbarContent,
    modifier: Modifier = Modifier
) {
    val colors = RiteAppTheme.colors
    val bg: Color
    val fg: Color
    val accent: Color
    when (variant) {
        RiteSnackbarVariant.Completed -> {
            bg = colors.onSurface
            fg = colors.surface
            accent =
                colors.primary
        }

        RiteSnackbarVariant.Skipped -> {
            bg = colors.onSurface
            fg = colors.surface
            accent =
                colors.onSurfaceMuted
        }

        RiteSnackbarVariant.Failed -> {
            bg = colors.error
            fg = colors.onError
            accent =
                colors.onError
        }

        RiteSnackbarVariant.Suspended -> {
            bg = colors.suspend
            fg = colors.onSuspend
            accent =
                colors.onSuspend
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth().widthIn(max = 420.dp),
        shape = RiteAppTheme.shapes.sm,
        color = bg,
        contentColor = fg
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = variant.icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        append(content.prefix)
                        withStyle(
                            SpanStyle(
                                color = accent,
                                fontFamily = RiteAppTheme.typography.displayItalic.fontFamily,
                                fontStyle = RiteAppTheme.typography.displayItalic.fontStyle,
                                fontWeight = RiteAppTheme.typography.displayItalic.fontWeight
                            )
                        ) { append(content.emphasized) }
                        append(content.suffix)
                    },
                    style = RiteAppTheme.typography.bodySmall
                )
                if (content.subtext != null) {
                    Text(
                        text = AnnotatedString(content.subtext),
                        style = RiteAppTheme.typography.labelSmall,
                        color = fg.copy(alpha = 0.7f)
                    )
                }
            }

            content.action?.invoke()
        }
    }
}
