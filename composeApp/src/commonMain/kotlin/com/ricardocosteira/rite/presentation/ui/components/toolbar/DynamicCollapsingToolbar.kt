@file:OptIn(ExperimentalMaterial3Api::class)

package com.ricardocosteira.rite.presentation.ui.components.toolbar

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isFinite
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.util.fastFirst
import kotlin.math.abs
import kotlin.math.roundToInt

const val COLLAPSING_TOOLBAR_TEST_TAG = "collapsing_toolbar"
private const val UNBOUNDED_SIZE = Int.MAX_VALUE

/** Matches M3 TopAppBar internal horizontal padding (TopAppBarHorizontalPadding). */
private val TOOLBAR_HORIZONTAL_PADDING = 4.dp

/** Matches M3 TopAppBar vertical centering: (64dp container - 48dp icon) / 2 = 8dp. */
private val TOOLBAR_VERTICAL_PADDING = 8.dp

/**
 * @param stackVertically When true, the navigation icon is placed above the content in expanded
 * state, and the content gets full width. When collapsed, the nav icon stays top-left and the
 * content collapses into the nav icon row. When false (default), nav icon and content are
 * placed side by side.
 */
@Composable
fun DynamicCollapsingToolbar(
    toolbarSpec: CollapsingToolbarSpec,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    centerContent: Boolean = true,
    stackVertically: Boolean = false,
    collapsedElevation: Dp = 2.dp,
    navigationIcon: @Composable () -> Unit = { },
    navigationIconVerticalArrangement: Arrangement.Vertical = Arrangement.Top,
    bottomHorizontalDivider: @Composable () -> Unit = { },
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    backgroundContent: @Composable (scrollProgress: Float) -> Unit = { },
    content: @Composable BoxScope.(scrollProgress: Float) -> Unit
) {
    val collapsedHeight = toolbarSpec.collapsedHeight

    require(collapsedHeight.isSpecified && collapsedHeight.isFinite) {
        "The collapsedHeight is expected to be specified and finite"
    }

    val appBarDragModifier = if (!toolbarSpec.scrollIsPinned) {
        Modifier.draggable(
            orientation = Orientation.Vertical,
            state = rememberDraggableState { delta -> toolbarSpec.state.heightOffset += delta },
            onDragStopped = { velocity ->
                settleAppBar(
                    toolbarSpec.state,
                    velocity,
                    toolbarSpec.flingAnimationSpec,
                    toolbarSpec.snapAnimationSpec
                )
            }
        )
    } else {
        Modifier
    }

    val maxElevationPx = with(LocalDensity.current) { collapsedElevation.toPx() }

    Surface(
        modifier = modifier
            .then(appBarDragModifier)
            .testTag(COLLAPSING_TOOLBAR_TEST_TAG)
            .graphicsLayer {
                shadowElevation = maxElevationPx * toolbarSpec.state.collapsedFraction
            },
        color = backgroundColor
    ) {
        CollapsingToolbarLayout(
            modifier = Modifier
                .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Horizontal))
                .clipToBounds()
                .heightIn(min = collapsedHeight),
            windowInsets = windowInsets.only(WindowInsetsSides.Top),
            scrolledOffset = { toolbarSpec.state.heightOffset },
            scrollProgress = { toolbarSpec.state.collapsedFraction },
            onScrollOffsetLimitUpdate = { toolbarSpec.heightOffsetLimit = it },
            navigationIcon = navigationIcon,
            navigationIconVerticalArrangement = navigationIconVerticalArrangement,
            centerContent = centerContent,
            stackVertically = stackVertically,
            bottomHorizontalDivider = bottomHorizontalDivider,
            backgroundContent = backgroundContent,
            content = { scrollProgress ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    content(scrollProgress)
                }
            }
        )
    }
}

@Composable
private fun CollapsingToolbarLayout(
    modifier: Modifier,
    windowInsets: WindowInsets,
    scrolledOffset: () -> Float,
    scrollProgress: () -> Float,
    onScrollOffsetLimitUpdate: (Float) -> Unit,
    navigationIcon: @Composable () -> Unit,
    navigationIconVerticalArrangement: Arrangement.Vertical,
    centerContent: Boolean,
    stackVertically: Boolean,
    bottomHorizontalDivider: @Composable () -> Unit,
    backgroundContent: @Composable (scrollProgress: Float) -> Unit,
    content: @Composable (scrollProgress: Float) -> Unit
) {
    Layout(
        content = {
            Box(Modifier.layoutId("background")) {
                backgroundContent(scrollProgress())
            }
            Box(
                Modifier
                    .windowInsetsPadding(windowInsets)
                    .layoutId("navigationIcon")
            ) {
                navigationIcon()
            }
            Box(
                Modifier
                    .windowInsetsPadding(windowInsets)
                    .layoutId("content")
            ) {
                content(scrollProgress())
            }
            Box(Modifier.layoutId("divider")) {
                bottomHorizontalDivider()
            }
        },
        modifier = modifier
    ) { measurables, constraints ->
        val horizontalPaddingPx: Int = TOOLBAR_HORIZONTAL_PADDING.roundToPx()
        val verticalPaddingPx: Int = TOOLBAR_VERTICAL_PADDING.roundToPx()

        val navigationIconPlaceable = measurables
            .fastFirst { it.layoutId == "navigationIcon" }
            .measure(constraints.copy(minWidth = 0))

        val maxContentWidth = if (stackVertically || constraints.maxWidth == UNBOUNDED_SIZE) {
            constraints.maxWidth
        } else {
            (constraints.maxWidth - navigationIconPlaceable.width).coerceAtLeast(0)
        }

        val contentPlaceable = measurables
            .fastFirst { it.layoutId == "content" }
            .measure(constraints.copy(minWidth = 0, maxWidth = maxContentWidth))

        val navIconHeightWithPadding: Int = navigationIconPlaceable.height + verticalPaddingPx * 2

        val expandedHeight: Int = if (stackVertically) {
            navIconHeightWithPadding + contentPlaceable.height
        } else {
            contentPlaceable.height
        }

        // Only update offset limit when content is at max height (expanded state)
        val currentScrollProgress = scrollProgress()
        if (currentScrollProgress == 0f) {
            onScrollOffsetLimitUpdate((constraints.minHeight - expandedHeight).toFloat())
        }

        // ScrolledOffset is expected to be equal or smaller than zero.
        val scrolledOffsetValue = scrolledOffset()
        val heightOffset = if (scrolledOffsetValue.isNaN()) 0 else scrolledOffsetValue.roundToInt()

        val layoutHeight = if (expandedHeight == UNBOUNDED_SIZE) {
            expandedHeight
        } else {
            expandedHeight + heightOffset
        }

        val backgroundContentPlaceable = measurables
            .fastFirst { it.layoutId == "background" }
            .measure(
                constraints.copy(
                    minWidth = 0,
                    minHeight = 0,
                    maxHeight = layoutHeight.coerceAtLeast(0)
                )
            )

        val dividerPlaceable = measurables
            .fastFirst { it.layoutId == "divider" }
            .measure(constraints.copy(minWidth = 0, minHeight = 0))

        layout(constraints.maxWidth, layoutHeight.coerceAtLeast(0)) {
            backgroundContentPlaceable.placeRelative(x = 0, y = 0)

            if (stackVertically) {
                // Nav icon at top-left with M3-matching padding
                navigationIconPlaceable.placeRelative(
                    x = horizontalPaddingPx,
                    y = verticalPaddingPx
                )

                // Content below nav icon (including its padding) in expanded
                contentPlaceable.placeRelative(
                    x = 0,
                    y = navIconHeightWithPadding
                )
            } else {
                navigationIconPlaceable.placeRelative(
                    x = horizontalPaddingPx,
                    y = when (navigationIconVerticalArrangement) {
                        Arrangement.Center -> (layoutHeight - navigationIconPlaceable.height) / 2
                        Arrangement.Bottom -> layoutHeight - navigationIconPlaceable.height
                        else -> 0
                    }
                )

                var baseX = (constraints.maxWidth - contentPlaceable.width) / 2
                if (!centerContent && baseX < navigationIconPlaceable.width + horizontalPaddingPx) {
                    baseX += (navigationIconPlaceable.width + horizontalPaddingPx - baseX)
                }

                contentPlaceable.placeRelative(
                    x = baseX,
                    y = (layoutHeight - contentPlaceable.height) / 2
                )
            }

            dividerPlaceable.placeRelative(
                x = 0,
                y = layoutHeight - dividerPlaceable.height
            )
        }
    }
}

/** Copied as-is from [androidx.compose.material3] v1.4.0 (TopAppBar.kt) */
@OptIn(ExperimentalMaterial3Api::class)
private suspend fun settleAppBar(
    state: TopAppBarState,
    velocity: Float,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
    snapAnimationSpec: AnimationSpec<Float>?
): Velocity {
    if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
        return Velocity.Zero
    }
    var remainingVelocity = velocity
    if (flingAnimationSpec != null && abs(velocity) > 1f) {
        var lastValue = 0f
        AnimationState(
            initialValue = 0f,
            initialVelocity = velocity
        )
            .animateDecay(flingAnimationSpec) {
                val delta = value - lastValue
                val initialHeightOffset = state.heightOffset
                state.heightOffset = initialHeightOffset + delta
                val consumed = abs(initialHeightOffset - state.heightOffset)
                lastValue = value
                remainingVelocity = this.velocity
                if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
            }
    }
    if (snapAnimationSpec != null) {
        if (state.heightOffset < 0 && state.heightOffset > state.heightOffsetLimit) {
            AnimationState(initialValue = state.heightOffset).animateTo(
                if (state.collapsedFraction < 0.5f) {
                    0f
                } else {
                    state.heightOffsetLimit
                },
                animationSpec = snapAnimationSpec
            ) {
                state.heightOffset = value
            }
        }
    }

    return Velocity(0f, remainingVelocity)
}
