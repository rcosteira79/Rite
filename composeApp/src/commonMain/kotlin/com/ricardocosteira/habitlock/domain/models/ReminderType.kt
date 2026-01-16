package com.ricardocosteira.habitlock.domain.models

/**
 * Type of reminder scheduling.
 */
enum class ReminderType {
    /** Fixed single time reminder (typical for binary habits) */
    FIXED,
    
    /** Periodic interval-based reminders (typical for quantitative habits) */
    PERIODIC
}

