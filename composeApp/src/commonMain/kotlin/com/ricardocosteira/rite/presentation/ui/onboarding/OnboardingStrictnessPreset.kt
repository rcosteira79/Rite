package com.ricardocosteira.rite.presentation.ui.onboarding

data class PresetRule(val key: String, val value: String)

enum class OnboardingStrictnessPreset(
    val label: String,
    val description: String,
    val collapsedSummary: String,
    val rules: List<PresetRule>,
    val isRecommended: Boolean = false
) {
    FLEXIBLE(
        label = "Flexible",
        description = "Gentle support, maximum forgiveness.",
        collapsedSummary = "Undo: Unlimited · Snoozes: Unlimited",
        rules = listOf(
            PresetRule("Undo", "Unlimited"),
            PresetRule("Snoozes", "Unlimited"),
            PresetRule("Skips", "Unlimited")
        )
    ),
    BALANCED(
        label = "Balanced",
        description = "The middle path. Enough grace to fail, enough structure to win.",
        collapsedSummary = "Undo: Within 5 min · Snoozes: 1/day",
        rules = listOf(
            PresetRule("Undo", "Within 5 min"),
            PresetRule("Snoozes", "1 / day"),
            PresetRule("Skips", "2 / month")
        ),
        isRecommended = true
    ),
    LOCKED(
        label = "Locked",
        description = "No excuses. Full accountability.",
        collapsedSummary = "No undo · Skips capped",
        rules = listOf(
            PresetRule("Undo", "None"),
            PresetRule("Snoozes", "Capped"),
            PresetRule("Skips", "Capped")
        )
    )
}
