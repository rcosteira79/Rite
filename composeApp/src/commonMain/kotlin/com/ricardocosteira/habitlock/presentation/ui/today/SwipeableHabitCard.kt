package com.ricardocosteira.habitlock.presentation.ui.today

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.presentation.ui.haptics.HapticController
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

enum class SwipeAction {
    REST,
    DELETE,
    EDIT
}

// How far the card can be dragged (fraction of card width)
private const val MAX_DRAG_FRACTION = 0.55f

// Fraction of card width where the action becomes armed
private const val ARM_THRESHOLD_FRACTION = 0.25f

// Background fade
private const val UNARMED_FULL_OPACITY_FRACTION = 0.15f
private const val UNARMED_MAX_ALPHA = 0.5f
private const val ARMED_ALPHA = 1.0f

// Snap-back animation
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

    val zoneColor: Color = when (zone) {
        SwipeAction.DELETE -> MaterialTheme.colorScheme.errorContainer
        SwipeAction.EDIT -> MaterialTheme.colorScheme.secondaryContainer
        SwipeAction.REST -> MaterialTheme.colorScheme.surface
    }

    val iconTint: Color = when (zone) {
        SwipeAction.DELETE -> MaterialTheme.colorScheme.onErrorContainer
        SwipeAction.EDIT -> MaterialTheme.colorScheme.onSecondaryContainer
        SwipeAction.REST -> MaterialTheme.colorScheme.onSurface
    }

    val icon: ImageVector? = when {
        zone == SwipeAction.DELETE && isArmed -> Icons.Filled.DeleteForever
        zone == SwipeAction.DELETE -> Icons.Outlined.Delete
        zone == SwipeAction.EDIT && isArmed -> Icons.Filled.Edit
        zone == SwipeAction.EDIT -> Icons.Outlined.Edit
        else -> null
    }

    val alignment: Alignment = when (zone) {
        SwipeAction.DELETE -> Alignment.CenterStart
        SwipeAction.EDIT -> Alignment.CenterEnd
        SwipeAction.REST -> Alignment.CenterEnd
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CORNER_RADIUS))
            .background(zoneColor.copy(alpha = alpha))
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint.copy(alpha = alpha),
                modifier = Modifier
                    .align(alignment)
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
    val offsetX: Animatable<Float, *> = remember { Animatable(0f) }
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

    // Haptic feedback when crossing the arm threshold
    LaunchedEffect(Unit) {
        snapshotFlow { isArmed }
            .distinctUntilChanged()
            .collect { armed: Boolean ->
                if (armed) {
                    currentHaptic.heavyClick()
                } else if (currentZone != SwipeAction.REST) {
                    currentHaptic.tick()
                }
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
                                // Slide off-screen to the right, then fire delete
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
