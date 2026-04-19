package com.ricardocosteira.rite.presentation.ui.today.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.today_timezone_changed_body_with_previous
import rite.composeapp.generated.resources.today_timezone_changed_body_without_previous
import rite.composeapp.generated.resources.today_timezone_changed_dismiss
import rite.composeapp.generated.resources.today_timezone_changed_title

@Composable
fun TimezoneBanner(
    previousTimezone: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RiteAppTheme.colors
    val typo = RiteAppTheme.typography

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = RiteAppTheme.spacing.gap4, vertical = RiteAppTheme.spacing.gap2),
        shape = RiteAppTheme.shapes.sm,
        color = colors.surfaceContainer,
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(RiteAppTheme.spacing.gap3),
            modifier = Modifier.padding(
                horizontal = RiteAppTheme.spacing.gap4,
                vertical = RiteAppTheme.spacing.gap3
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = null,
                tint = colors.onSurfaceMuted,
                modifier = Modifier.size(18.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(Res.string.today_timezone_changed_title),
                    style = typo.labelLarge,
                    color = colors.onSurface
                )
                val body = if (previousTimezone != null) {
                    val template =
                        stringResource(Res.string.today_timezone_changed_body_with_previous)
                    val before = template.substringBefore("{from}")
                    val after = template.substringAfter("{from}")
                    buildAnnotatedString {
                        append(before)
                        withStyle(
                            SpanStyle(
                                fontFamily = typo.displayItalic.fontFamily,
                                fontStyle = typo.displayItalic.fontStyle,
                                fontWeight = typo.displayItalic.fontWeight,
                                color = colors.onSurface
                            )
                        ) { append(previousTimezone) }
                        append(after)
                    }
                } else {
                    buildAnnotatedString {
                        append(
                            stringResource(Res.string.today_timezone_changed_body_without_previous)
                        )
                    }
                }
                Text(
                    text = body,
                    style = typo.bodySmall,
                    color = colors.onSurfaceMuted
                )
            }
            TextButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(
                    horizontal = RiteAppTheme.spacing.gap2,
                    vertical = 4.dp
                )
            ) {
                Text(
                    text = stringResource(Res.string.today_timezone_changed_dismiss).uppercase(),
                    style = typo.eyebrow,
                    color = colors.onSurfaceMuted
                )
            }
        }
    }
}
