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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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
import rite.composeapp.generated.resources.notifications_body
import rite.composeapp.generated.resources.notifications_card_reminders_subtitle
import rite.composeapp.generated.resources.notifications_card_reminders_title
import rite.composeapp.generated.resources.notifications_card_tracking_subtitle
import rite.composeapp.generated.resources.notifications_card_tracking_title
import rite.composeapp.generated.resources.notifications_card_warnings_subtitle
import rite.composeapp.generated.resources.notifications_card_warnings_title
import rite.composeapp.generated.resources.notifications_heading_accent
import rite.composeapp.generated.resources.notifications_heading_first
import rite.composeapp.generated.resources.notifications_strap_label

@Composable
fun NotificationPermissionStep(modifier: Modifier = Modifier, reduceMotion: Boolean = false) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        OnboardingStepStrap(
            step = 4,
            totalSteps = 4,
            stepName = stringResource(Res.string.notifications_strap_label)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = headingAnnotated(),
            style = RiteAppTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Normal),
            color = RiteAppTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(Res.string.notifications_body),
            style = RiteAppTheme.typography.bodySmall,
            color = RiteAppTheme.colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        NotificationFeatureRow(
            icon = Icons.Outlined.NotificationsActive,
            title = stringResource(Res.string.notifications_card_reminders_title),
            subtitle = stringResource(Res.string.notifications_card_reminders_subtitle)
        )
        Spacer(modifier = Modifier.height(10.dp))
        NotificationFeatureRow(
            icon = Icons.Outlined.Warning,
            title = stringResource(Res.string.notifications_card_warnings_title),
            subtitle = stringResource(Res.string.notifications_card_warnings_subtitle)
        )
        Spacer(modifier = Modifier.height(10.dp))
        NotificationFeatureRow(
            icon = Icons.AutoMirrored.Outlined.ShowChart,
            title = stringResource(Res.string.notifications_card_tracking_title),
            subtitle = stringResource(Res.string.notifications_card_tracking_subtitle)
        )
    }
}

@Composable
private fun headingAnnotated(): AnnotatedString = buildAnnotatedString {
    append(stringResource(Res.string.notifications_heading_first))
    append(" ")
    withStyle(
        SpanStyle(fontStyle = FontStyle.Italic, color = RiteAppTheme.colors.onSurfaceVariant)
    ) {
        append(stringResource(Res.string.notifications_heading_accent))
    }
}

@Composable
private fun NotificationFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = 1.dp,
                color = RiteAppTheme.colors.outlineVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .background(RiteAppTheme.colors.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(3.dp))
                .border(
                    width = 1.dp,
                    color = RiteAppTheme.colors.outline,
                    shape = RoundedCornerShape(3.dp)
                )
                .background(RiteAppTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = RiteAppTheme.colors.onSurface
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column {
            Text(
                text = title,
                style = RiteAppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = RiteAppTheme.colors.onSurface
            )
            Text(
                text = subtitle,
                style = RiteAppTheme.typography.bodySmall,
                color = RiteAppTheme.colors.onSurfaceVariant
            )
        }
    }
}
