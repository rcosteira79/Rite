package com.ricardocosteira.rite.presentation.ui.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import kotlin.math.pow

// Splash background and seal colors are intentionally NOT theme tokens:
// they must match the launcher icon and native splash drawable exactly so
// the seal looks identical across launcher → native splash → Compose. The
// theme's sage primary (#5E7F6C) is a different value than the icon's
// bespoke moss (#4F6E5B) by design — only text colors come from the theme.
private val LinenBg = Color(0xFFF4EFE7)
private val LinenSealTrack = Color(0xFFDCE3D8)
private val LinenSealStroke = Color(0xFF4F6E5B)

private val DarkBg = Color(0xFF1A201D)
private val DarkSealTrack = Color(0x29DCE3D8)
private val DarkSealStroke = Color(0xFFC8D4C2)

private const val SPLASH_DURATION_MS = 1500L
private const val ARC_FRACTION = 0.62f

// Seal is centered to match the native splash icon position (no jump on
// hand-off). Wordmark TOP sits at ring_bottom + 32dp gap = ring_radius + 32.
private val SealCanvasSize = 200.dp
private val SealRingRadius = 80.dp
private val WordmarkTopFromCenter = SealRingRadius + 32.dp

@Composable
fun RiteSplashScreen(modifier: Modifier = Modifier, onAnimationComplete: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) DarkBg else LinenBg
    val track = if (isDark) DarkSealTrack else LinenSealTrack
    val stroke = if (isDark) DarkSealStroke else LinenSealStroke

    val colors = RiteAppTheme.colors
    val typography = RiteAppTheme.typography

    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        val durationNs = SPLASH_DURATION_MS * 1_000_000L
        val start = withFrameNanos { it }
        while (progress < 1f) {
            val now = withFrameNanos { it }
            progress = ((now - start).toFloat() / durationNs).coerceAtMost(1f)
        }
        onAnimationComplete()
    }

    val dashAnim = easeOut(progress.segment(0.00f, 0.55f))
    val wordAlpha = easeOut(progress.segment(0.25f, 0.65f))
    val tagAlpha = easeOut(progress.segment(0.30f, 0.75f))

    val wordmarkStyle = typography.displayItalic.copy(
        fontSize = 38.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = (-0.5f).sp,
        color = colors.onSurface
    )
    val taglineStyle = typography.mono.copy(
        fontSize = 9.5f.sp,
        letterSpacing = 0.32f.em,
        color = colors.onSurfaceMuted
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
    ) {
        val halfHeight = maxHeight / 2

        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .size(SealCanvasSize)
        ) {
            val ringRadius = SealRingRadius.toPx()
            val cx = size.width / 2f
            val cy = size.height / 2f
            // Match the launcher / native-splash drawable proportions: stroke 4.6/30
            // and tick/dot 1.5/30 of ring radius. This makes the seal identical
            // across launcher → native splash → Compose splash.
            val ringStroke = (SealRingRadius * (4.6f / 30f)).toPx()
            val markRadius = (SealRingRadius * (1.5f / 30f)).toPx()

            drawCircle(
                color = track,
                radius = ringRadius,
                center = Offset(cx, cy),
                style = Stroke(width = ringStroke)
            )

            drawArc(
                color = stroke,
                startAngle = -90f,
                sweepAngle = 360f * ARC_FRACTION,
                useCenter = false,
                topLeft = Offset(cx - ringRadius, cy - ringRadius),
                size = Size(ringRadius * 2f, ringRadius * 2f),
                style = Stroke(width = ringStroke, cap = StrokeCap.Butt)
            )

            // Dashed ritual ring — fades in while spiraling inward from the inner
            // edge of the main ring to its settled position at 0.86 of ring radius
            // (matches the today screen ProgressRing). 48 dashes, design's 9/22
            // dash-to-cycle ratio.
            if (dashAnim > 0.01f) {
                val dashStartR = ringRadius - ringStroke / 2f
                val dashEndR = ringRadius * 0.86f
                val dashR = dashStartR + (dashEndR - dashStartR) * dashAnim
                val cycleArc = 2f * kotlin.math.PI.toFloat() * dashR / 48f
                rotate(degrees = (1f - dashAnim) * 90f, pivot = Offset(cx, cy)) {
                    drawCircle(
                        color = stroke.copy(alpha = 0.55f * dashAnim),
                        radius = dashR,
                        center = Offset(cx, cy),
                        style = Stroke(
                            width = (SealRingRadius * (0.8f / 30f)).toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                intervals = floatArrayOf(
                                    cycleArc * (9f / 22f),
                                    cycleArc * (13f / 22f)
                                ),
                                phase = 0f
                            )
                        )
                    )
                }
            }

            drawCircle(
                color = stroke,
                radius = markRadius,
                center = Offset(cx, cy - ringRadius)
            )

            drawCircle(
                color = stroke,
                radius = markRadius,
                center = Offset(cx, cy)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = halfHeight + WordmarkTopFromCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "rite",
                style = wordmarkStyle,
                modifier = Modifier
                    .alpha(wordAlpha)
                    .offset(y = ((1f - wordAlpha) * 8f).dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "show up · for yourself",
                style = taglineStyle,
                modifier = Modifier.alpha(tagAlpha)
            )
        }
    }
}

private fun Float.segment(start: Float, end: Float): Float =
    ((this - start) / (end - start)).coerceIn(0f, 1f)

private fun easeOut(u: Float): Float = 1f - (1f - u).pow(3)
