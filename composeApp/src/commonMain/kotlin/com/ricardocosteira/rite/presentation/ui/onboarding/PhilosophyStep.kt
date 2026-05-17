package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.philosophy_body_close
import rite.composeapp.generated.resources.philosophy_body_intro
import rite.composeapp.generated.resources.philosophy_heading_accent
import rite.composeapp.generated.resources.philosophy_heading_first
import rite.composeapp.generated.resources.philosophy_promise_local_subtitle
import rite.composeapp.generated.resources.philosophy_promise_local_title
import rite.composeapp.generated.resources.philosophy_promise_open_subtitle
import rite.composeapp.generated.resources.philosophy_promise_open_title
import rite.composeapp.generated.resources.philosophy_promise_undistracted_subtitle
import rite.composeapp.generated.resources.philosophy_promise_undistracted_title
import rite.composeapp.generated.resources.philosophy_strap_label

@Composable
fun PhilosophyStep(
    totalSteps: Int = 4,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        OnboardingStepStrap(
            step = 1,
            totalSteps = totalSteps,
            stepName = stringResource(Res.string.philosophy_strap_label)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = headingAnnotated(),
            style = RiteAppTheme.typography.displayMedium.copy(fontWeight = FontWeight.Normal),
            color = RiteAppTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = stringResource(Res.string.philosophy_body_intro),
            style = RiteAppTheme.typography.bodyLarge,
            color = RiteAppTheme.colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(Res.string.philosophy_body_close),
            style = RiteAppTheme.typography.bodyLarge,
            color = RiteAppTheme.colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(34.dp))

        PromiseRow(
            title = stringResource(Res.string.philosophy_promise_local_title),
            subtitle = stringResource(Res.string.philosophy_promise_local_subtitle)
        )
        Spacer(modifier = Modifier.height(12.dp))
        PromiseRow(
            title = stringResource(Res.string.philosophy_promise_undistracted_title),
            subtitle = stringResource(Res.string.philosophy_promise_undistracted_subtitle)
        )
        Spacer(modifier = Modifier.height(12.dp))
        PromiseRow(
            title = stringResource(Res.string.philosophy_promise_open_title),
            subtitle = stringResource(Res.string.philosophy_promise_open_subtitle)
        )
    }
}

@Composable
private fun headingAnnotated(): AnnotatedString = buildAnnotatedString {
    append(stringResource(Res.string.philosophy_heading_first))
    append(" ")
    withStyle(
        SpanStyle(fontStyle = FontStyle.Italic, color = RiteAppTheme.colors.onSurfaceVariant)
    ) {
        append(stringResource(Res.string.philosophy_heading_accent))
    }
}

@Composable
private fun PromiseRow(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(2.dp))
                .border(
                    width = 1.dp,
                    color = RiteAppTheme.colors.outline,
                    shape = RoundedCornerShape(2.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(RiteAppTheme.colors.onSurface)
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.padding(top = 1.dp)) {
            Text(
                text = title,
                style = RiteAppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = RiteAppTheme.colors.onSurface
            )
            Text(
                text = subtitle,
                style = RiteAppTheme.typography.bodyMedium,
                color = RiteAppTheme.colors.onSurfaceVariant
            )
        }
    }
}
