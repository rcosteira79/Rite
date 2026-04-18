package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

enum class RiteSnackbarVariant { Completed, Skipped, Failed, Suspended }

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
            Box(
                modifier = Modifier.size(22.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glyph square placeholder — concrete icon chosen by the caller via content.action slot is
                // intentionally flat for now; full icon wiring arrives in Slice 2 when the Today screen uses it.
                Surface(
                    modifier = Modifier.size(22.dp),
                    shape = RiteAppTheme.shapes.xs,
                    color = fg.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, fg.copy(alpha = 0.2f))
                ) {}
            }

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
