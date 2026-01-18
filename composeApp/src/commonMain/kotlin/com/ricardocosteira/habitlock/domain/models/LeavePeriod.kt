package com.ricardocosteira.habitlock.domain.models

import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/**
 * Represents a temporary suspension period for a habit.
 *
 * During a leave period, the habit is suspended and will not generate
 * instances, send notifications, or affect streaks. This is useful for
 * planned breaks like vacations, illness, or other life circumstances.
 *
 * @property id Unique identifier for the leave period
 * @property habitId ID of the habit being suspended
 * @property startDate First day of suspension (inclusive)
 * @property endDate Last day of suspension (inclusive, null = indefinite)
 * @property reason Optional explanation for the suspension
 * @property createdAt Timestamp when the leave period was created
 */
data class LeavePeriod(
    val id: String,
    val habitId: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val reason: String?,
    val createdAt: Instant
) {
    init {
        if (endDate != null) {
            require(endDate >= startDate) { "End date must be after or equal to start date" }
        }
    }

    /**
     * Checks if this leave period is active on a given date.
     *
     * @param date The date to check
     * @return true if the date falls within the leave period
     */
    fun isActiveOn(date: LocalDate): Boolean {
        if (date < startDate) return false
        if (endDate != null && date > endDate) return false
        return true
    }

    /**
     * Checks if this leave period has ended.
     *
     * @param currentDate The current date to check against
     * @return true if the leave period has a defined end date and it has passed
     */
    fun hasEnded(currentDate: LocalDate): Boolean {
        return endDate != null && currentDate > endDate
    }

    /**
     * Checks if this leave period is currently active.
     *
     * @param currentDate The current date to check against
     * @return true if the leave period is active on the current date
     */
    fun isCurrentlyActive(currentDate: LocalDate): Boolean {
        return isActiveOn(currentDate)
    }

    /**
     * Checks if this is an indefinite leave period (no end date).
     */
    val isIndefinite: Boolean
        get() = endDate == null

    /**
     * Calculates the total duration of the leave period in days.
     * Returns null if the leave period is indefinite.
     */
    val durationInDays: Int?
        get() {
            val end = endDate ?: return null
            return (end.toEpochDays() - startDate.toEpochDays() + 1).toInt()
        }

    /**
     * Creates a new LeavePeriod with an updated end date.
     * Useful for ending or extending a leave period.
     */
    fun withEndDate(newEndDate: LocalDate?): LeavePeriod {
        return copy(endDate = newEndDate)
    }
}
