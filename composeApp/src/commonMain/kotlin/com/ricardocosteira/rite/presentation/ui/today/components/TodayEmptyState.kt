package com.ricardocosteira.rite.presentation.ui.today.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.components.PrimaryButton
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.today_empty_state_cta
import rite.composeapp.generated.resources.today_empty_state_heading
import rite.composeapp.generated.resources.today_empty_state_subtext

@Composable
fun TodayEmptyState(onAddFirstHabit: () -> Unit, modifier: Modifier = Modifier,) {
    val colors = RiteAppTheme.colors
    val typo = RiteAppTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = RiteAppTheme.spacing.gap6),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "\u25C6", // BLACK DIAMOND
            style = typo.displayMedium,
            color = colors.onSurfaceSubtle,
        )
        Text(
            text = heading(),
            style = typo.displaySmall,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = RiteAppTheme.spacing.gap4),
        )
        Text(
            text = stringResource(Res.string.today_empty_state_subtext),
            style = typo.bodyMedium,
            color = colors.onSurfaceMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = RiteAppTheme.spacing.gap2)
                .widthIn(max = 320.dp),
        )
        PrimaryButton(
            onClick = onAddFirstHabit,
            modifier = Modifier
                .padding(top = RiteAppTheme.spacing.gap6)
                .widthIn(max = 280.dp),
        ) {
            Text(stringResource(Res.string.today_empty_state_cta))
        }
    }
}

@Composable
private fun heading() = buildAnnotatedString {
    val raw = stringResource(Res.string.today_empty_state_heading)
    // Render the last word in italic display per the design's 'Structure your <em>day</em>.'
    val lastSpace = raw.trimEnd('.').lastIndexOf(' ')
    if (lastSpace <= 0) {
        append(raw)
        return@buildAnnotatedString
    }
    append(raw.substring(0, lastSpace + 1))
    val accent = raw.substring(lastSpace + 1)
    withStyle(
        SpanStyle(
            fontFamily = RiteAppTheme.typography.displayItalic.fontFamily,
            fontStyle = RiteAppTheme.typography.displayItalic.fontStyle,
            fontWeight = RiteAppTheme.typography.displayItalic.fontWeight,
        ),
    ) {
        append(accent)
    }
}
