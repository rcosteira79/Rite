package com.ricardocosteira.habitlock.domain.repositories

import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitReminder
import com.ricardocosteira.habitlock.domain.models.HabitSchedule
import kotlinx.coroutines.flow.Flow

/**
 * Repository for habit definitions and related data.
 */
interface HabitRepository {
    
    /**
     * Observe all active, non-archived habits.
     */
    fun observeActiveHabits(): Flow<List<Habit>>
    
    /**
     * Observe archived habits.
     */
    fun observeArchivedHabits(): Flow<List<Habit>>
    
    /**
     * Get all active, non-archived habits.
     */
    suspend fun getActiveHabits(): List<Habit>
    
    /**
     * Get a habit by ID.
     */
    suspend fun getHabitById(habitId: String): Habit?
    
    /**
     * Create a new habit with schedule and optional reminder.
     */
    suspend fun createHabit(
        habit: Habit,
        schedule: HabitSchedule,
        reminder: HabitReminder?
    )
    
    /**
     * Update an existing habit.
     */
    suspend fun updateHabit(habit: Habit)
    
    /**
     * Update habit streak values.
     */
    suspend fun updateHabitStreak(habitId: String, currentStreak: Int, longestStreak: Int)
    
    /**
     * Update habit score values.
     */
    suspend fun updateHabitScore(habitId: String, totalCompletions: Int, expectedCompletions: Int)
    
    /**
     * Increment habit total completions.
     */
    suspend fun incrementHabitTotalCompletions(habitId: String, amount: Int = 1)
    
    /**
     * Decrement habit total completions.
     */
    suspend fun decrementHabitTotalCompletions(habitId: String, amount: Int = 1)
    
    /**
     * Increment habit expected completions.
     */
    suspend fun incrementHabitExpectedCompletions(habitId: String, amount: Int = 1)
    
    /**
     * Archive a habit. It will no longer generate daily instances.
     */
    suspend fun archiveHabit(habitId: String)
    
    /**
     * Unarchive a habit. It will start generating daily instances again.
     */
    suspend fun unarchiveHabit(habitId: String)
    
    /**
     * Delete a habit and all related data.
     */
    suspend fun deleteHabit(habitId: String)
    
    /**
     * Get the schedule for a habit.
     */
    suspend fun getScheduleForHabit(habitId: String): HabitSchedule?
    
    /**
     * Get reminders for a habit.
     */
    suspend fun getRemindersForHabit(habitId: String): List<HabitReminder>
    
    /**
     * Update or create a reminder for a habit.
     */
    suspend fun updateReminder(reminder: HabitReminder)
    
    /**
     * Delete a reminder.
     */
    suspend fun deleteReminder(reminderId: String)
}

