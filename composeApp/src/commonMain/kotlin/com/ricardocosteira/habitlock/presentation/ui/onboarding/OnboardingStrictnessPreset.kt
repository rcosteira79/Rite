package com.ricardocosteira.habitlock.presentation.ui.onboarding

enum class OnboardingStrictnessPreset(
    val label: String,
    val description: String,
    val rules: List<String>,
    val isRecommended: Boolean = false
) {
    FLEXIBLE(
        label = "Flexible",
        description = "Gentle support, maximum forgiveness.",
        rules = listOf(
            "Unlimited undo",
            "Unlimited snoozes",
            "Skips allowed without limits",
            "Missed habits tracked lightly"
        )
    ),
    BALANCED(
        label = "Balanced",
        description = "Structure with room for real life.",
        rules = listOf(
            "Undo allowed for today only",
            "Snoozes are limited",
            "Skips are limited",
            "Missed habits fail at end of day"
        ),
        isRecommended = true
    ),
    LOCKED(
        label = "Locked",
        description = "No excuses. Full accountability.",
        rules = listOf(
            "No undo",
            "Snoozes are capped",
            "Skips are capped",
            "Missed habits always fail"
        )
    )
}
