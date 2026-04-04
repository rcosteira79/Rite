package com.ricardocosteira.rite.domain.models

/**
 * Type of habit - determines how completion is tracked.
 */
enum class HabitType {
    /** Single action habit - either done or not done */
    BINARY,
    
    /** Accumulating habit - progress tracked with multiple completions */
    QUANTITATIVE
}

