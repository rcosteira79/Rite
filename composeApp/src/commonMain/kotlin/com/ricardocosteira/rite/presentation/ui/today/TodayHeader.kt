package com.ricardocosteira.rite.presentation.ui.today

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.domain.models.StrictnessPreset as DomainStrictnessPreset
import com.ricardocosteira.rite.presentation.ui.components.ProgressRing
import com.ricardocosteira.rite.presentation.ui.components.StrictnessPill
import com.ricardocosteira.rite.presentation.ui.components.StrictnessPreset as PillPreset
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.today_header_salute_all_done
import rite.composeapp.generated.resources.today_header_salute_empty
import rite.composeapp.generated.resources.today_header_subtitle_all_done
import rite.composeapp.generated.resources.today_header_subtitle_mixed_many
import rite.composeapp.generated.resources.today_header_subtitle_mixed_one
import rite.composeapp.generated.resources.today_header_subtitle_no_habits

@Composable
fun TodayHeader(
    saluteKey: StringResource?,
    pendingCount: Int,
    dailyTotal: Int,
    hasHabits: Boolean,
    dailyProgressFraction: Float,
    strictnessPreset: DomainStrictnessPreset?,
    isCollapsed: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = isCollapsed,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = modifier.fillMaxWidth(),
        label = "TodayHeaderCollapse"
    ) { collapsed ->
        if (collapsed) {
            TodayHeaderCollapsed(
                saluteKey = saluteKey,
                pendingCount = pendingCount,
                dailyTotal = dailyTotal,
                dailyProgressFraction = dailyProgressFraction,
                strictnessPreset = strictnessPreset
            )
        } else {
            TodayHeaderExpanded(
                saluteKey = saluteKey,
                pendingCount = pendingCount,
                dailyTotal = dailyTotal,
                hasHabits = hasHabits,
                dailyProgressFraction = dailyProgressFraction,
                strictnessPreset = strictnessPreset
            )
        }
    }
}

@Composable
internal fun TodayHeaderExpanded(
    saluteKey: StringResource?,
    pendingCount: Int,
    dailyTotal: Int,
    hasHabits: Boolean,
    dailyProgressFraction: Float,
    strictnessPreset: DomainStrictnessPreset?,
    modifier: Modifier = Modifier
) {
    val colors = RiteAppTheme.colors
    val typo = RiteAppTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start = RiteAppTheme.spacing.gap6,
                end = RiteAppTheme.spacing.gap6,
                top = RiteAppTheme.spacing.gap6,
                bottom = RiteAppTheme.spacing.gap5
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(RiteAppTheme.spacing.gap2)
        ) {
            Text(
                text = saluteAnnotated(saluteKey, hasHabits, pendingCount),
                style = typo.displayMedium,
                color = colors.onSurface
            )
            Text(
                text = subtitleText(hasHabits, pendingCount, dailyTotal),
                style = typo.bodyMedium,
                color = colors.onSurfaceMuted
            )
            if (strictnessPreset != null) {
                Spacer(Modifier.height(RiteAppTheme.spacing.gap1))
                StrictnessPill(preset = strictnessPreset.toPillPreset())
            }
        }

        Spacer(Modifier.width(RiteAppTheme.spacing.gap4))

        if (dailyTotal > 0) {
            ProgressRing(
                progress = dailyProgressFraction.coerceIn(0f, 1f)
            )
        }
    }
}

@Composable
private fun saluteAnnotated(saluteKey: StringResource?, hasHabits: Boolean, pendingCount: Int) =
    buildAnnotatedString {
        val raw: String = when {
            !hasHabits -> stringResource(Res.string.today_header_salute_empty)
            pendingCount == 0 -> stringResource(Res.string.today_header_salute_all_done)
            saluteKey != null -> stringResource(saluteKey)
            else -> stringResource(Res.string.today_header_salute_all_done)
        }
        val lastSpace = raw.trimEnd('.').lastIndexOf(' ')
        if (lastSpace <= 0) {
            append(raw)
            return@buildAnnotatedString
        }
        append(raw.substring(0, lastSpace + 1))
        val italic = raw.substring(lastSpace + 1)
        withStyle(
            SpanStyle(
                fontFamily = RiteAppTheme.typography.displayItalic.fontFamily,
                fontStyle = RiteAppTheme.typography.displayItalic.fontStyle,
                fontWeight = RiteAppTheme.typography.displayItalic.fontWeight,
                color = RiteAppTheme.colors.onSurfaceMuted
            )
        ) { append(italic) }
    }

@Composable
private fun subtitleText(hasHabits: Boolean, pendingCount: Int, dailyTotal: Int): String = when {
    !hasHabits -> stringResource(Res.string.today_header_subtitle_no_habits)

    pendingCount == 0 -> stringResource(Res.string.today_header_subtitle_all_done, dailyTotal)

    pendingCount == 1 -> stringResource(
        Res.string.today_header_subtitle_mixed_one,
        pendingCount,
        dailyTotal - pendingCount
    )

    else -> stringResource(
        Res.string.today_header_subtitle_mixed_many,
        pendingCount,
        dailyTotal - pendingCount
    )
}

internal fun DomainStrictnessPreset.toPillPreset(): PillPreset = when (this) {
    DomainStrictnessPreset.FLEXIBLE -> PillPreset.Flexible
    DomainStrictnessPreset.BALANCED -> PillPreset.Balanced
    DomainStrictnessPreset.UNWAVERING -> PillPreset.Unwavering
}

@Composable
internal fun TodayHeaderCollapsed(
    saluteKey: StringResource?,
    pendingCount: Int,
    dailyTotal: Int,
    dailyProgressFraction: Float,
    strictnessPreset: DomainStrictnessPreset?
) {
    val colors = RiteAppTheme.colors
    val typo = RiteAppTheme.typography
    val pct = (dailyProgressFraction.coerceIn(0f, 1f) * 100).toInt()
    val done = (dailyTotal - pendingCount).coerceAtLeast(0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = RiteAppTheme.spacing.gap6,
                vertical = RiteAppTheme.spacing.gap4
            ),
        horizontalArrangement = Arrangement.spacedBy(RiteAppTheme.spacing.gap3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = saluteShortLine(saluteKey),
                style = typo.titleMedium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$done / $dailyTotal · $pct%",
                style = typo.eyebrow,
                color = colors.onSurfaceMuted
            )
        }
        if (strictnessPreset != null) {
            StrictnessPill(
                preset = strictnessPreset.toPillPreset(),
                animated = false,
                showCap = false
            )
        }
    }
}

@Composable
private fun saluteShortLine(saluteKey: StringResource?): String =
    saluteKey?.let { stringResource(it) }
        ?: stringResource(Res.string.today_header_salute_all_done)
