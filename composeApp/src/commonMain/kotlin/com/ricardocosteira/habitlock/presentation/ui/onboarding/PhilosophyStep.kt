package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.philosophy_body
import habitlock.composeapp.generated.resources.philosophy_heading
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhilosophyStep(
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    val headlineAlpha = remember { Animatable(0f) }
    val headlineTranslateY = remember { Animatable(12f) }
    val accentWidth = remember { Animatable(0f) }
    val bodyAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (reduceMotion) {
            headlineAlpha.snapTo(1f)
            headlineTranslateY.snapTo(0f)
            accentWidth.snapTo(1f)
            bodyAlpha.snapTo(1f)
            return@LaunchedEffect
        }
        // Headline: fade + translate up — 200ms, 0ms delay
        launch { headlineAlpha.animateTo(1f, tween(200)) }
        launch { headlineTranslateY.animateTo(0f, tween(200)) }
        // Accent line: width draws in — 250ms, 80ms delay
        delay(80)
        launch { accentWidth.animateTo(1f, tween(250)) }
        // Body: fade in — 200ms, 140ms delay
        delay(60) // 80 + 60 = 140ms total
        launch { bodyAlpha.animateTo(1f, tween(200)) }
    }

    Box(modifier = modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        // Architectural background ring — decorative, non-interactive
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
                text = stringResource(Res.string.philosophy_heading),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .alpha(headlineAlpha.value)
                    .graphicsLayer { translationY = headlineTranslateY.value.dp.toPx() }
                    .semantics { heading() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Animated accent line
            Box(
                modifier = Modifier
                    .width((36 * accentWidth.value).dp)
                    .height(3.dp)
                    .background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.philosophy_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(bodyAlpha.value)
            )
        }
    }
}
