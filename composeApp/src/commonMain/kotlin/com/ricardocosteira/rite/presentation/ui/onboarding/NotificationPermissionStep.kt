package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.notifications_body
import rite.composeapp.generated.resources.notifications_card_reminders_subtitle
import rite.composeapp.generated.resources.notifications_card_reminders_title
import rite.composeapp.generated.resources.notifications_card_tracking_subtitle
import rite.composeapp.generated.resources.notifications_card_tracking_title
import rite.composeapp.generated.resources.notifications_card_warnings_subtitle
import rite.composeapp.generated.resources.notifications_card_warnings_title
import rite.composeapp.generated.resources.notifications_heading

@Composable
fun NotificationPermissionStep(modifier: Modifier = Modifier, reduceMotion: Boolean = false) {
    val headlineAlpha = remember { Animatable(0f) }
    val headlineTranslateY = remember { Animatable(12f) }
    val accentWidth = remember { Animatable(0f) }
    val bodyAlpha = remember { Animatable(0f) }
    val cardsAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (reduceMotion) {
            headlineAlpha.snapTo(1f)
            headlineTranslateY.snapTo(0f)
            accentWidth.snapTo(1f)
            bodyAlpha.snapTo(1f)
            cardsAlpha.snapTo(1f)
            return@LaunchedEffect
        }
        launch { headlineAlpha.animateTo(1f, tween(200)) }
        launch { headlineTranslateY.animateTo(0f, tween(200)) }
        delay(80)
        launch { accentWidth.animateTo(1f, tween(250)) }
        delay(60)
        launch { bodyAlpha.animateTo(1f, tween(200)) }
        delay(100)
        launch { cardsAlpha.animateTo(1f, tween(300)) }
    }

    Box(modifier = modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        // Decorative background ring — same as PhilosophyStep
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 120.dp, y = (-160).dp)
                .requiredSize(420.dp)
                .graphicsLayer { rotationZ = 12f }
                .border(
                    width = 40.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = CircleShape
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.notifications_heading),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .alpha(headlineAlpha.value)
                    .graphicsLayer { translationY = headlineTranslateY.value.dp.toPx() }
                    .semantics { heading() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .width((36 * accentWidth.value).dp)
                    .height(3.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.notifications_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(bodyAlpha.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.alpha(cardsAlpha.value)
            ) {
                NotificationPreviewCard(
                    icon = Icons.Outlined.NotificationsActive,
                    title = stringResource(Res.string.notifications_card_reminders_title),
                    subtitle = stringResource(Res.string.notifications_card_reminders_subtitle)
                )
                Spacer(modifier = Modifier.height(10.dp))
                NotificationPreviewCard(
                    icon = Icons.Outlined.Warning,
                    title = stringResource(Res.string.notifications_card_warnings_title),
                    subtitle = stringResource(Res.string.notifications_card_warnings_subtitle)
                )
                Spacer(modifier = Modifier.height(10.dp))
                NotificationPreviewCard(
                    icon = Icons.Outlined.ShowChart,
                    title = stringResource(Res.string.notifications_card_tracking_title),
                    subtitle = stringResource(Res.string.notifications_card_tracking_subtitle)
                )
            }
        }
    }
}

@Composable
private fun NotificationPreviewCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
