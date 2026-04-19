package com.ricardocosteira.rite.presentation.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateInt
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.ricardocosteira.rite.di.LocalAppComponent
import com.ricardocosteira.rite.presentation.ui.archived.ArchivedHabitsScreen
import com.ricardocosteira.rite.presentation.ui.calendar.CalendarScreen
import com.ricardocosteira.rite.presentation.ui.components.RiteSnackbar
import com.ricardocosteira.rite.presentation.ui.components.RiteSnackbarContent
import com.ricardocosteira.rite.presentation.ui.components.RiteSnackbarVariant
import com.ricardocosteira.rite.presentation.ui.components.RiteSnackbarVisuals
import com.ricardocosteira.rite.presentation.ui.habit.HabitFormScreen
import com.ricardocosteira.rite.presentation.ui.habitdetail.HabitDetailRoute
import com.ricardocosteira.rite.presentation.ui.onboarding.OnboardingRoute
import com.ricardocosteira.rite.presentation.ui.settings.SettingsScreen
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import com.ricardocosteira.rite.presentation.ui.today.TodayFeedbackEvent
import com.ricardocosteira.rite.presentation.ui.today.TodayScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.getString
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.snackbar_deleted_prefix
import rite.composeapp.generated.resources.snackbar_deleted_suffix
import rite.composeapp.generated.resources.snackbar_failed_prefix
import rite.composeapp.generated.resources.snackbar_failed_subtext_limit
import rite.composeapp.generated.resources.snackbar_failed_suffix
import rite.composeapp.generated.resources.snackbar_generic_error
import rite.composeapp.generated.resources.swipe_undo

private val savedStateConfig: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Onboarding::class)
            subclass(Today::class)
            subclass(HabitDetail::class)
            subclass(CreateHabit::class)
            subclass(EditHabit::class)
            subclass(Calendar::class)
            subclass(ArchivedHabits::class)
            subclass(Settings::class)
        }
    }
}

private val topLevelRoutes: Set<Route> = setOf(Today, Calendar, Settings)

/**
 * Provides the [SharedTransitionScope] from the app's [SharedTransitionLayout] to any
 * screen nested inside [NavDisplay], so that route-level shared-element transitions
 * (e.g. the Add Habit FAB → HabitFormScreen container transform) can resolve their
 * scope without having to thread it through every composable.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope> {
    error("LocalSharedTransitionScope not provided")
}

/** Shared key used by the Today FAB and HabitFormScreen root for the container transform. */
const val AddHabitSharedKey: String = "add-habit-container"

/**
 * Shared key for the small icon that travels (and rotates 0°↔45°) between the
 * FAB's + and the HabitFormScreen's close-button X, on top of the container
 * morph.
 */
const val AddHabitIconKey: String = "add-habit-icon"

const val AddHabitTransitionMs: Int = 500

/**
 * Bounds animation for the container morph. Direction-aware: on forward nav
 * (expansion) the bounds are delayed until the icon rotation completes; on
 * back nav (contraction) the bounds animate first and rotation finishes last.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val AddHabitBoundsTransform: BoundsTransform = BoundsTransform { initial, target ->
    val isExpanding = initial.width < target.width
    if (isExpanding) {
        // Forward: hold at FAB bounds while the icon rotates, then expand.
        tween(
            durationMillis = AddHabitTransitionMs * 6 / 10,
            delayMillis = AddHabitTransitionMs * 4 / 10,
            easing = FastOutSlowInEasing,
        )
    } else {
        // Back: contract first, then let the icon rotate in the remaining window.
        tween(
            durationMillis = AddHabitTransitionMs * 6 / 10,
            easing = FastOutSlowInEasing,
        )
    }
}

/**
 * Per-side container crossfade specs. Colour swap is strictly confined to the
 * container-morph phase so the FAB stays solid-dark while the icon is
 * rotating, and only starts crossfading to the form's light surface once the
 * expansion actually begins (and mirrors on back nav).
 *
 *   • FAB.exit   (forward nav) — holds solid for rotation, fades during
 *     expansion (40–100%).
 *   • Form.enter (forward nav) — invisible during rotation, fades in during
 *     expansion (40–100%).
 *   • Form.exit  (back nav) — fades out during contraction (0–60%), gone by
 *     the time the icon rotates back.
 *   • FAB.enter  (back nav) — fades in during contraction (0–60%), solid for
 *     the rotation phase.
 */
val AddHabitFabEnter: EnterTransition =
    fadeIn(animationSpec = tween(durationMillis = AddHabitTransitionMs * 6 / 10))

val AddHabitFabExit: ExitTransition =
    fadeOut(
        animationSpec = tween(
            durationMillis = AddHabitTransitionMs * 6 / 10,
            delayMillis = AddHabitTransitionMs * 4 / 10,
        )
    )

val AddHabitFormEnter: EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = AddHabitTransitionMs * 6 / 10,
            delayMillis = AddHabitTransitionMs * 4 / 10,
        )
    )

val AddHabitFormExit: ExitTransition =
    fadeOut(animationSpec = tween(durationMillis = AddHabitTransitionMs * 6 / 10))

/**
 * Shape used by the source side of the Add-Habit container transform (the FAB).
 * Morphs between a 50% rounded shape (circle at FAB's square bounds) and a 0%
 * rounded shape (rectangle). The morph is confined to the expansion/contraction
 * phase so corners don't round-off before the container actually starts
 * growing — which would have looked like a snap.
 */
@Composable
fun AnimatedVisibilityScope.animatedAddHabitSourceShape(): Shape {
    val cornerPercent: Int by transition.animateInt(
        transitionSpec = {
            if (targetState == EnterExitState.Visible) {
                // Back nav: corner rounds back up during contraction (0–60%).
                tween(
                    durationMillis = AddHabitTransitionMs * 6 / 10,
                    easing = FastOutSlowInEasing,
                )
            } else {
                // Forward nav: corner straightens during expansion (40–100%).
                tween(
                    durationMillis = AddHabitTransitionMs * 6 / 10,
                    delayMillis = AddHabitTransitionMs * 4 / 10,
                    easing = FastOutSlowInEasing,
                )
            }
        },
        label = "add-habit-source-corner",
    ) { state ->
        if (state == EnterExitState.Visible) 50 else 0
    }
    return RoundedCornerShape(percent = cornerPercent.coerceIn(0, 50))
}

/**
 * Alpha for the form's *inner* content (everything below the top app bar) so
 * it only fades in near the end of the container expansion — and fades out
 * immediately on back navigation so the shrinking container isn't crowded with
 * form fields. Apply via `Modifier.graphicsLayer { alpha = … }`.
 */
@Composable
fun AnimatedVisibilityScope.animatedAddHabitFormContentAlpha(): Float {
    val alpha: Float by transition.animateFloat(
        transitionSpec = {
            // Enter (PreEnter → Visible): delay until the last 40% of the morph.
            // Exit (Visible → PostExit): fade out quickly at the start.
            if (targetState == EnterExitState.Visible) {
                // Enter: hold until icon rotation finishes (0–60% of transition),
                // then fade in during the last 40%.
                tween(
                    durationMillis = AddHabitTransitionMs * 4 / 10,
                    delayMillis = AddHabitTransitionMs * 6 / 10,
                )
            } else {
                // Exit: fade out during the first 40% so the shrinking container
                // isn't crowded with form fields while the icon rotates back.
                tween(durationMillis = AddHabitTransitionMs * 4 / 10)
            }
        },
        label = "add-habit-form-content-alpha",
    ) { state ->
        if (state == EnterExitState.Visible) 1f else 0f
    }
    return alpha
}

/** Destination-side counterpart to [animatedAddHabitSourceShape]. */
@Composable
fun AnimatedVisibilityScope.animatedAddHabitDestinationShape(): Shape {
    val cornerPercent: Int by transition.animateInt(
        transitionSpec = {
            if (targetState == EnterExitState.Visible) {
                // Forward nav: corner straightens during expansion (40–100%).
                tween(
                    durationMillis = AddHabitTransitionMs * 6 / 10,
                    delayMillis = AddHabitTransitionMs * 4 / 10,
                    easing = FastOutSlowInEasing,
                )
            } else {
                // Back nav: corner rounds back up during contraction (0–60%).
                tween(
                    durationMillis = AddHabitTransitionMs * 6 / 10,
                    easing = FastOutSlowInEasing,
                )
            }
        },
        label = "add-habit-destination-corner",
    ) { state ->
        if (state == EnterExitState.Visible) 0 else 50
    }
    return RoundedCornerShape(percent = cornerPercent.coerceIn(0, 50))
}

@Composable
fun RiteNavigation(isOnboardingCompleted: Boolean) {
    val initialRoute: Route = if (isOnboardingCompleted) Today else Onboarding
    val backStack = rememberNavBackStack(savedStateConfig, initialRoute)
    val snackbarHostState = remember { SnackbarHostState() }
    val appComponent = LocalAppComponent.current

    // Feedback collector lives here — not inside TodayScreen — so snackbars survive
    // navigation. Preempts the current snackbar whenever a new feedback event
    // arrives, so only the latest is visible.
    LaunchedEffect(Unit) {
        val todayViewModel = appComponent.todayViewModel
        var showJob: Job? = null
        todayViewModel.feedbackEvents.collect { event ->
            showJob?.cancel()
            snackbarHostState.currentSnackbarData?.dismiss()
            showJob = when (event) {
                is TodayFeedbackEvent.SkipLimitReached -> launch {
                    snackbarHostState.showSnackbar(
                        RiteSnackbarVisuals(
                            variant = RiteSnackbarVariant.Failed,
                            content = RiteSnackbarContent(
                                prefix = getString(Res.string.snackbar_failed_prefix),
                                emphasized = event.habitName,
                                suffix = getString(Res.string.snackbar_failed_suffix),
                                subtext = getString(Res.string.snackbar_failed_subtext_limit)
                            )
                        )
                    )
                }

                is TodayFeedbackEvent.HabitDeleted -> launch {
                    val result = snackbarHostState.showSnackbar(
                        RiteSnackbarVisuals(
                            variant = RiteSnackbarVariant.Skipped,
                            content = RiteSnackbarContent(
                                prefix = getString(Res.string.snackbar_deleted_prefix),
                                emphasized = event.habitName,
                                suffix = getString(Res.string.snackbar_deleted_suffix)
                            ),
                            actionLabel = getString(Res.string.swipe_undo),
                            duration = SnackbarDuration.Long
                        )
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        todayViewModel.undoDelete()
                    }
                }

                is TodayFeedbackEvent.ShowError -> launch {
                    snackbarHostState.showSnackbar(
                        RiteSnackbarVisuals(
                            variant = RiteSnackbarVariant.Failed,
                            content = RiteSnackbarContent(
                                prefix = "",
                                emphasized = event.message
                                    ?: getString(Res.string.snackbar_generic_error),
                                suffix = ""
                            )
                        )
                    )
                }

                is TodayFeedbackEvent.ShowSnackbar -> launch {
                    snackbarHostState.showSnackbar(event.visuals)
                }

                TodayFeedbackEvent.UndoCompleted -> null
            }
        }
    }

    val currentTab: BottomNavTab by remember {
        derivedStateOf {
            // Find the deepest top-level route in the stack to keep
            // the correct tab highlighted on non-tab screens
            val topLevelEntry: NavKey? = backStack.lastOrNull { it as? Route in topLevelRoutes }
            when (topLevelEntry) {
                is Calendar -> BottomNavTab.HISTORY
                is Settings -> BottomNavTab.SETTINGS
                else -> BottomNavTab.TODAY
            }
        }
    }

    val showBottomNav: Boolean by remember {
        derivedStateOf { backStack.lastOrNull() !is Onboarding }
    }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                RiteBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        handleTabSelection(tab, backStack)
                    }
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(
                    horizontal = RiteAppTheme.spacing.gap4,
                    vertical = RiteAppTheme.spacing.gap3
                )
            ) { data ->
                val visuals = data.visuals
                if (visuals is RiteSnackbarVisuals) {
                    RiteSnackbar(
                        variant = visuals.variant,
                        content = visuals.content.copy(
                            action = visuals.actionLabel?.let { label ->
                                @Composable {
                                    androidx.compose.material3.TextButton(
                                        onClick = { data.performAction() }
                                    ) {
                                        androidx.compose.material3.Text(
                                            text = label.uppercase(),
                                            style = RiteAppTheme.typography.eyebrow
                                        )
                                    }
                                }
                            }
                        )
                    )
                } else {
                    // Plain-string fallback from screens not yet migrated.
                    RiteSnackbar(
                        variant = RiteSnackbarVariant.Completed,
                        content = RiteSnackbarContent(
                            prefix = visuals.message,
                            emphasized = "",
                            suffix = ""
                        )
                    )
                }
            }
        }
    ) { scaffoldPadding ->
        SharedTransitionLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = scaffoldPadding.calculateBottomPadding())
                .consumeWindowInsets(
                    PaddingValues(bottom = scaffoldPadding.calculateBottomPadding())
                )
        ) {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxSize(),
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<Onboarding> {
                            OnboardingRoute(
                                viewModel = appComponent.onboardingViewModel,
                                snackbarHostState = snackbarHostState,
                                onFinished = {
                                    backStack.clear()
                                    backStack.add(Today)
                                    appComponent.todayViewModel.loadTodayHabits()
                                }
                            )
                        }

                        entry<Today> {
                            TodayScreen(
                                onNavigateToHabitDetail = { backStack.add(HabitDetail(it)) },
                                onNavigateToCreateHabit = { backStack.add(CreateHabit) },
                                onEditHabit = { backStack.add(EditHabit(it)) }
                            )
                        }

                        entry<Calendar> {
                            CalendarScreen(onBackClick = backStack::removeLastOrNull)
                        }

                        entry<Settings> {
                            SettingsScreen(
                                onBackClick = backStack::removeLastOrNull,
                                onArchivedHabitsClick = { backStack.add(ArchivedHabits) },
                                snackbarHostState = snackbarHostState
                            )
                        }

                        entry<ArchivedHabits> {
                            ArchivedHabitsScreen(
                                onBackClick = backStack::removeLastOrNull,
                                snackbarHostState = snackbarHostState
                            )
                        }

                        entry<CreateHabit>(
                            // Plain fade gives the entry transition a real 500ms window
                            // so transition-driven child animations (icon rotation,
                            // form-content alpha) have time to interpolate.
                            metadata = NavDisplay.transitionSpec {
                                fadeIn(tween(AddHabitTransitionMs)) togetherWith
                                    fadeOut(tween(AddHabitTransitionMs))
                            } + NavDisplay.popTransitionSpec {
                                fadeIn(tween(AddHabitTransitionMs)) togetherWith
                                    fadeOut(tween(AddHabitTransitionMs))
                            } + NavDisplay.predictivePopTransitionSpec { _ ->
                                fadeIn(tween(AddHabitTransitionMs)) togetherWith
                                    fadeOut(tween(AddHabitTransitionMs))
                            }
                        ) {
                            // Capture todayViewModel inside the @Composable lambda — required because
                            // LocalAppComponent.current cannot be called from a non-composable callback.
                            val todayViewModel = LocalAppComponent.current.todayViewModel
                            HabitFormScreen(
                                habitIdToEdit = null,
                                onNavigateBack = {
                                    backStack.removeLastOrNull()
                                    todayViewModel.loadTodayHabits()
                                },
                                snackbarHostState = snackbarHostState,
                                useAddHabitTransition = true,
                            )
                        }

                        entry<EditHabit> { route ->
                            val todayViewModel = LocalAppComponent.current.todayViewModel
                            HabitFormScreen(
                                habitIdToEdit = route.habitId,
                                onNavigateBack = {
                                    backStack.removeLastOrNull()
                                    todayViewModel.loadTodayHabits()
                                },
                                snackbarHostState = snackbarHostState
                            )
                        }

                        entry<HabitDetail> { route ->
                            val todayViewModel = LocalAppComponent.current.todayViewModel
                            HabitDetailRoute(
                                instanceId = route.instanceId,
                                onNavigateBack = {
                                    backStack.removeLastOrNull()
                                    todayViewModel.loadTodayHabits()
                                },
                                onEditHabit = { habitId ->
                                    backStack.add(EditHabit(habitId))
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}

private fun handleTabSelection(tab: BottomNavTab, backStack: MutableList<NavKey>) {
    when (tab) {
        BottomNavTab.TODAY -> {
            backStack.clear()
            backStack.add(Today)
        }

        BottomNavTab.HISTORY -> {
            backStack.clear()
            backStack.add(Today)
            backStack.add(Calendar)
        }

        BottomNavTab.SETTINGS -> {
            backStack.clear()
            backStack.add(Today)
            backStack.add(Settings)
        }
    }
}
