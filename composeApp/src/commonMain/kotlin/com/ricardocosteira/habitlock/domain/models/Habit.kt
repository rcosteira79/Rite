package com.ricardocosteira.habitlock.domain.models

import kotlin.time.Instant

/**
 * Habit definition/template representing a repeating behavior to track.
 */
data class Habit(
    val id: String,
    val name: String,
    val description: String?,
    val type: HabitType,
    val targetValue: Int?,
    val unit: String?,
    val isActive: Boolean,
    val isArchived: Boolean,
    val currentStreak: Int,
    val longestStreak: Int,
    val createdAt: Instant,
    val archivedAt: Instant?
)

