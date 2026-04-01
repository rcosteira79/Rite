package com.ricardocosteira.habitlock.presentation.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.presentation.ui.haptics.HapticController
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.distinctUntilChanged

enum class SwipeAction {
    REST,
    DELETE,
    EDIT
}

// Anchors — the card settles here when released past the arm threshold
private const val DELETE_ANCHOR_FRACTION = 0.55f
private const val EDIT_ANCHOR_FRACTION = -0.55f

// Visual — background starts fading immediately, reaches full "unarmed" opacity here
private const val UNARMED_FULL_OPACITY_FRACTION = 0.15f

// Armed vs unarmed opacity
private const val UNARMED_MAX_ALPHA = 0.5f
private const val ARMED_ALPHA = 1.0f

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
    var pendingAction: SwipeAction? by remember { mutableStateOf(null) }

    val currentOnEdit by rememberUpdatedState(onEdit)
    val currentOnDelete by rememberUpdatedState(onDelete)
    val currentHaptic by rememberUpdatedState(hapticController)

    // Defer action to next frame — navigating from confirmValueChange breaks backstack
    LaunchedEffect(pendingAction) {
        when (pendingAction) {
            SwipeAction.DELETE -> currentOnDelete()
            SwipeAction.EDIT -> currentOnEdit()
            SwipeAction.REST, null -> { /* no-op */ }
        }
        pendingAction = null
    }

    val anchoredDraggableState: AnchoredDraggableState<SwipeAction> = remember {
        AnchoredDraggableState(
            initialValue = SwipeAction.REST,
            confirmValueChange = { newValue: SwipeAction ->
                when (newValue) {
                    SwipeAction.DELETE -> {
                        pendingAction = SwipeAction.DELETE
                        true
                    }

                    SwipeAction.EDIT -> {
                        pendingAction = SwipeAction.EDIT
                        false // snap back to REST
                    }

                    SwipeAction.REST -> true
                }
            }
        )
    }

    if (cardWidth > 0f) {
        val anchors: DraggableAnchors<SwipeAction> = DraggableAnchors {
            SwipeAction.REST at 0f
            SwipeAction.DELETE at cardWidth * DELETE_ANCHOR_FRACTION
            SwipeAction.EDIT at cardWidth * EDIT_ANCHOR_FRACTION
        }
        anchoredDraggableState.updateAnchors(anchors)
    }

    val currentOffset: Float = anchoredDraggableState.offset.takeIf { !it.isNaN() } ?: 0f

    val currentZone: SwipeAction by remember {
        derivedStateOf {
            val offset: Float = anchoredDraggableState.offset.takeIf { !it.isNaN() } ?: 0f
            when {
                offset > 0f -> SwipeAction.DELETE
                offset < 0f -> SwipeAction.EDIT
                else -> SwipeAction.REST
            }
        }
    }

    val revealFraction: Float by remember {
        derivedStateOf {
            val offset: Float = anchoredDraggableState.offset.takeIf { !it.isNaN() } ?: 0f
            val width: Float = cardWidth
            if (width > 0f) abs(offset) / width else 0f
        }
    }

    // Armed = dragged past half the anchor distance (matches AnchoredDraggable's default
    // positional threshold, so armed = "will settle at action anchor if released")
    val isArmed: Boolean by remember {
        derivedStateOf {
            val offset: Float = anchoredDraggableState.offset.takeIf { !it.isNaN() } ?: 0f
            val width: Float = cardWidth
            if (width <= 0f) return@derivedStateOf false
            val fraction: Float = abs(offset) / width
            fraction >= abs(DELETE_ANCHOR_FRACTION) * 0.5f
        }
    }

    // Haptic feedback when crossing the arm threshold in either direction
    LaunchedEffect(Unit) {
        snapshotFlow { isArmed }
            .distinctUntilChanged()
            .collect { armed: Boolean ->
                if (armed) {
                    currentHaptic.heavyClick()
                } else if (currentZone != SwipeAction.REST) {
                    // Dragged back below threshold while still swiping
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
                .anchoredDraggable(
                    state = anchoredDraggableState,
                    orientation = Orientation.Horizontal
                )
        ) {
            content()
        }
    }
}
