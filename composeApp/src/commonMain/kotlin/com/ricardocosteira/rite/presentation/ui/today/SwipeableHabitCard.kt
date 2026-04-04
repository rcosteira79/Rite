package com.ricardocosteira.rite.presentation.ui.today

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.haptics.HapticController
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private const val MAX_DRAG_FRACTION = 0.55f
private const val ARM_THRESHOLD_FRACTION = 0.25f
private const val UNARMED_FULL_OPACITY_FRACTION = 0.15f
private const val UNARMED_MAX_ALPHA = 0.5f
private const val ARMED_ALPHA = 1.0f
private const val SNAP_BACK_DURATION_MS = 250
internal val CORNER_RADIUS = 16.dp

@Composable
internal fun SwipeBackground(
    zone: SwipeAction,
    isArmed: Boolean,
    revealFraction: Float,
    modifier: Modifier = Modifier
) {
    val baseAlpha: Float = (revealFraction / UNARMED_FULL_OPACITY_FRACTION).coerceIn(0f, 1f)
    val alpha: Float = if (isArmed) ARMED_ALPHA else baseAlpha * UNARMED_MAX_ALPHA

    val backgroundColor: Color = zone.backgroundColor()
    val iconTint: Color = zone.iconTint()
    val icon: ImageVector? = if (isArmed) zone.armedIcon else zone.unarmedIcon

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CORNER_RADIUS))
            .background(backgroundColor.copy(alpha = alpha))
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint.copy(alpha = alpha),
                modifier = Modifier
                    .align(zone.alignment)
                    .padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun SwipeableHabitCard(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    hapticController: HapticController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var cardWidth: Float by remember { mutableStateOf(0f) }
    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val currentOnEdit by rememberUpdatedState(onEdit)
    val currentOnDelete by rememberUpdatedState(onDelete)
    val currentHaptic by rememberUpdatedState(hapticController)

    val currentOffset: Float = offsetX.value

    val currentZone: SwipeAction by remember {
        derivedStateOf {
            when {
                offsetX.value > 0f -> SwipeAction.DELETE
                offsetX.value < 0f -> SwipeAction.EDIT
                else -> SwipeAction.REST
            }
        }
    }

    val revealFraction: Float by remember {
        derivedStateOf {
            val width: Float = cardWidth
            if (width > 0f) abs(offsetX.value) / width else 0f
        }
    }

    val isArmed: Boolean by remember {
        derivedStateOf {
            val width: Float = cardWidth
            if (width <= 0f) return@derivedStateOf false
            abs(offsetX.value) / width >= ARM_THRESHOLD_FRACTION
        }
    }

    LaunchedEffect(isArmed) {
        if (isArmed) {
            currentHaptic.heavyClick()
        } else if (currentZone != SwipeAction.REST) {
            currentHaptic.click()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { size -> cardWidth = size.width.toFloat() }
    ) {
        SwipeBackground(
            zone = currentZone,
            isArmed = isArmed,
            revealFraction = revealFraction,
            modifier = Modifier.matchParentSize()
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(x = currentOffset.roundToInt(), y = 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val maxOffset: Float = cardWidth * MAX_DRAG_FRACTION
                            val newOffset: Float = (offsetX.value + dragAmount)
                                .coerceIn(-maxOffset, maxOffset)
                            coroutineScope.launch { offsetX.snapTo(newOffset) }
                        },
                        onDragEnd = {
                            val armed: Boolean = cardWidth > 0f &&
                                abs(offsetX.value) / cardWidth >= ARM_THRESHOLD_FRACTION
                            val zone: SwipeAction = when {
                                offsetX.value > 0f -> SwipeAction.DELETE
                                offsetX.value < 0f -> SwipeAction.EDIT
                                else -> SwipeAction.REST
                            }

                            if (armed && zone == SwipeAction.DELETE) {
                                coroutineScope.launch {
                                    offsetX.animateTo(
                                        targetValue = cardWidth,
                                        animationSpec = tween(
                                            durationMillis = SNAP_BACK_DURATION_MS
                                        )
                                    )
                                    currentOnDelete()
                                }
                            } else if (armed && zone == SwipeAction.EDIT) {
                                currentOnEdit()
                                coroutineScope.launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(
                                            durationMillis = SNAP_BACK_DURATION_MS
                                        )
                                    )
                                }
                            } else {
                                coroutineScope.launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(
                                            durationMillis = SNAP_BACK_DURATION_MS
                                        )
                                    )
                                }
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = SNAP_BACK_DURATION_MS)
                                )
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}
