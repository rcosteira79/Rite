package com.ricardocosteira.rite.presentation.ui.onboarding

enum class OnboardingStrictnessPreset(
    val label: String,
    val description: String,
    val rules: List<String>,
    val isRecommended: Boolean = false
) {
    FLEXIBLE(
        label = "Flexible",
        description = "For habits still finding their shape. Generous with undos and skips — the point is to build the daily motion first.",
        rules = listOf(
            "Undo: unlimited, across all history",
            "Snoozes: unlimited · 60-min duration",
            "Skips: unlimited",
            "Consecutive skips: no cap"
        )
    ),
    BALANCED(
        label = "Balanced",
        description = "The default for most adults. Small safety net for life, firm enough to build consistency.",
        rules = listOf(
            "Undo: today only",
            "Snoozes: 3 per habit per day · 30-min duration",
            "Skips: allowed",
            "Consecutive skips: max 2"
        ),
        isRecommended = true
    ),
    UNWAVERING(
        label = "Unwavering",
        description = "For habits you've already decided are non-negotiable. No soft landings, no retroactive edits.",
        rules = listOf(
            "Undo: disabled",
            "Snoozes: 1 per habit per day · 15-min duration",
            "Skips: not permitted",
            "Consecutive skips: zero"
        )
    )
}
