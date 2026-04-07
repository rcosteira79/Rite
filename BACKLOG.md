# Follow-up Work

## Post-MVP Polish

- [ ] Day-detail calendar view (tap a day to see that day's habits)
- [x] Habit detail screen (streaks, score, heatmap, actions)
- [ ] Leave/suspension mode UI (domain logic complete, needs UI entry point)
- [x] Notification permission onboarding screen
- [x] Habit form IME handling (keyboard occludes bottom buttons)
- [ ] Fix weekly habits changing daily target to times/week instead of keeping it times/day
- [ ] Animated collapsing toolbar transition on Today screen — ring + percentage move to collapsed position, ring arc unfurls into horizontal line that slides off-screen right, "Done" fades in beneath percentage
- [ ] Animate title in new/edit habit screens
- [ ] Check spec_notifications for missing notification features
- [ ] Discard confirmation on create habit screen; on edit screen, only prompt if there are unsaved changes
- [ ] Persist daily target value when switching between binary and quantitative habit types
- [ ] Animate complete button and metrics in habit detail screen
- [ ] Rethink Habit Score — should reach 100% after completing daily for the recommended habit-forming period (~66 days?). Currently overshoots well past 100%. Research and redesign the formula
- [ ] Wrap HabitDetailViewModel repository dependencies behind use cases returning Result

## Future Features

- [ ] Weekly Reflection / insights card (exploring on-device Gemma 4 E2B for natural language summaries)
- [ ] Periodic reminder scheduling (interval-based within a time window)
- [ ] Create/edit habit UI for custom increment values
- [ ] Active vs silent tracking notification toggle in Settings
- [ ] iOS activation
- [ ] Comprehensive unit test coverage
- [ ] Ascension mode — visual treatment for quantitative habits that go beyond 100% (ring colour shift, celebratory UI)
- [ ] Block distracting apps when there are pending habits (needs brainstorming)
- [ ] Bag-of-words matching for habit creation — auto-suggest icon based on habit name
- [ ] Update app icon
- [ ] Fix flaky TodayViewModelSwipeTest — inject test dispatchers into repositories instead of using real Dispatchers.IO
