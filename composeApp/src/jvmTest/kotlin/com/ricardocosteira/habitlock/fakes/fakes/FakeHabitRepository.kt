package com.ricardocosteira.habitlock.fakes

import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitReminder
import com.ricardocosteira.habitlock.domain.models.HabitSchedule
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeHabitRepository : HabitRepository {

    private val habits: MutableMap<String, Habit> = mutableMapOf()
    private val schedules: MutableMap<String, HabitSchedule> = mutableMapOf()
    private val reminders: MutableMap<String, MutableList<HabitReminder>> = mutableMapOf()
    private val activeHabitsFlow: MutableStateFlow<List<Habit>> = MutableStateFlow(emptyList())

    fun addHabit(habit: Habit, schedule: HabitSchedule) {
        habits[habit.id] = habit
        schedules[habit.id] = schedule
        refreshActiveHabitsFlow()
    }

    fun getHabitOrNull(habitId: String): Habit? = habits[habitId]

    private fun refreshActiveHabitsFlow() {
        activeHabitsFlow.value = habits.values.filter { it.isActive && !it.isArchived }
    }

    override fun observeActiveHabits(): Flow<List<Habit>> = activeHabitsFlow

    override fun observeArchivedHabits(): Flow<List<Habit>> = activeHabitsFlow.map { list ->
        list.filter { it.isArchived }
    }

    override suspend fun getActiveHabits(): List<Habit> =
        habits.values.filter { it.isActive && !it.isArchived }

    override suspend fun getHabitById(habitId: String): Habit? = habits[habitId]

    override suspend fun createHabit(
        habit: Habit,
        schedule: HabitSchedule,
        reminder: HabitReminder?
    ) {
        habits[habit.id] = habit
        schedules[habit.id] = schedule
        reminder?.let {
            reminders.getOrPut(habit.id) { mutableListOf() }.add(it)
        }
        refreshActiveHabitsFlow()
    }

    override suspend fun updateHabit(habit: Habit) {
        habits[habit.id] = habit
        refreshActiveHabitsFlow()
    }

    override suspend fun updateHabitStreak(
        habitId: String,
        currentStreak: Int,
        longestStreak: Int
    ) {
        val habit: Habit = habits[habitId] ?: return
        habits[habitId] = habit.copy(currentStreak = currentStreak, longestStreak = longestStreak)
    }

    override suspend fun updateHabitScore(
        habitId: String,
        totalCompletions: Int,
        expectedCompletions: Int
    ) {
        val habit: Habit = habits[habitId] ?: return
        habits[habitId] = habit.copy(
            totalCompletions = totalCompletions,
            expectedCompletions = expectedCompletions
        )
    }

    override suspend fun incrementHabitTotalCompletions(habitId: String, amount: Int) {
        val habit: Habit = habits[habitId] ?: return
        habits[habitId] = habit.copy(totalCompletions = habit.totalCompletions + amount)
    }

    override suspend fun decrementHabitTotalCompletions(habitId: String, amount: Int) {
        val habit: Habit = habits[habitId] ?: return
        val newTotal: Int = (habit.totalCompletions - amount).coerceAtLeast(0)
        habits[habitId] = habit.copy(totalCompletions = newTotal)
    }

    override suspend fun incrementHabitExpectedCompletions(habitId: String, amount: Int) {
        val habit: Habit = habits[habitId] ?: return
        habits[habitId] = habit.copy(expectedCompletions = habit.expectedCompletions + amount)
    }

    override suspend fun archiveHabit(habitId: String) {
        val habit: Habit = habits[habitId] ?: return
        habits[habitId] = habit.copy(isArchived = true, isActive = false)
        refreshActiveHabitsFlow()
    }

    override suspend fun unarchiveHabit(habitId: String) {
        val habit: Habit = habits[habitId] ?: return
        habits[habitId] = habit.copy(isArchived = false, isActive = true)
        refreshActiveHabitsFlow()
    }

    override suspend fun deleteHabit(habitId: String) {
        habits.remove(habitId)
        schedules.remove(habitId)
        reminders.remove(habitId)
        refreshActiveHabitsFlow()
    }

    override suspend fun getScheduleForHabit(habitId: String): HabitSchedule? = schedules[habitId]

    override suspend fun updateSchedule(schedule: HabitSchedule) {
        schedules[schedule.habitId] = schedule
    }

    override suspend fun createScheduleForHabit(schedule: HabitSchedule) {
        schedules[schedule.habitId] = schedule
    }

    override suspend fun getRemindersForHabit(habitId: String): List<HabitReminder> =
        reminders[habitId] ?: emptyList()

    override suspend fun updateReminder(reminder: HabitReminder) {
        val habitReminders: MutableList<HabitReminder> = reminders[reminder.habitId] ?: return
        val index: Int = habitReminders.indexOfFirst { it.id == reminder.id }
        if (index >= 0) {
            habitReminders[index] = reminder
        }
    }

    override suspend fun deleteReminder(reminderId: String) {
        reminders.values.forEach { list -> list.removeAll { it.id == reminderId } }
    }

    override suspend fun createReminderForHabit(reminder: HabitReminder) {
        reminders.getOrPut(reminder.habitId) { mutableListOf() }.add(reminder)
    }
}
