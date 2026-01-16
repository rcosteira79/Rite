package com.ricardocosteira.habitlock.domain.models

/**
 * Preset strictness levels chosen during onboarding.
 */
enum class StrictnessPreset {
    /** Gentle support with maximum forgiveness */
    FLEXIBLE,
    
    /** Structure with room for real life (default) */
    BALANCED,
    
    /** No excuses, full accountability */
    LOCKED
}

