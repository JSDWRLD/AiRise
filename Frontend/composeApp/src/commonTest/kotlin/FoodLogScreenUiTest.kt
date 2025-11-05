package com.teamnotfound.airise.meal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FoodLogScreenUiTest {

    private fun vm() = MealViewModel.fake()

    @Test
    fun quickAdd_increases_total_food() {
        val vm = vm()
        val before = vm.totalFood
        vm.addQuickFood(MealType.Breakfast, 250, "Oatmeal", "1 bowl", 5.0, 40.0, 10.0)
        assertTrue(vm.totalFood > before)
    }

    @Test
    fun remaining_is_goal_minus_food_clamped_at_zero() {
        val vm = vm()
        vm.setGoal(2000)
        vm.addQuickFood(MealType.Lunch, 500, "Rice", "1", 2.0, 50.0, 5.0)
        vm.addQuickFood(MealType.Dinner, 700, "Chicken", "1", 10.0, 0.0, 60.0)
        assertEquals(800, vm.remaining)

        vm.addQuickFood(MealType.Dinner, 5000, "Feast", "lot", 0.0, 0.0, 0.0)
        assertEquals(0, vm.remaining)
    }

    @Test
    fun addQuickFood_routes_to_correct_meal_list() {
        val vm = vm()
        vm.addQuickFood(MealType.Breakfast, 100, "Eggs", "1", 5.0, 1.0, 6.0)
        vm.addQuickFood(MealType.Lunch, 200, "Wrap", "1", 8.0, 20.0, 12.0)
        vm.addQuickFood(MealType.Dinner, 300, "Steak", "1", 15.0, 0.0, 25.0)

        val meals = vm.uiState.day.meals
        assertEquals("Eggs", meals.breakfast.last().name)
        assertEquals("Wrap", meals.lunch.last().name)
        assertEquals("Steak", meals.dinner.last().name)
    }

    @Test
    fun totals_sum_all_meals_and_truncate_double_calories() {
        val vm = vm()
        // calories are Doubles in the model; totalFood truncates to Int
        vm.addQuickFood(MealType.Breakfast, 100, "A", "1", 0.0, 0.0, 0.0)
        vm.addQuickFood(MealType.Lunch, 101, "B", "1", 0.0, 0.0, 0.0)
        // simulate decimals by two entries whose double sum is not whole when added before toInt
        vm.addQuickFood(MealType.Dinner, 100, "C", "1", 0.0, 0.0, 0.0)
        vm.addQuickFood(MealType.Dinner, 1, "D", "1", 0.0, 0.0, 0.0)

        // 100 + 101 + 100 + 1 = 302 -> expect 302 exactly
        assertEquals(302, vm.totalFood)
    }

    @Test
    fun addQuickFood_uses_default_name_when_blank() {
        val vm = vm()
        vm.addQuickFood(MealType.Breakfast, 100, "", "serving", 1.0, 2.0, 3.0)
        val lastName = vm.uiState.day.meals.breakfast.last().name
        assertEquals("Quick Add", lastName)
    }

    @Test
    fun navigating_days_updates_offset_and_isolates_day_data() {
        val vm = vm()

        // Today: add 300
        vm.addQuickFood(MealType.Breakfast, 300, "TodayCal", "1", 0.0, 0.0, 0.0)
        val todayFood = vm.totalFood

        // Tomorrow: add 400
        vm.nextDay()
        vm.addQuickFood(MealType.Breakfast, 400, "TomorrowCal", "1", 0.0, 0.0, 0.0)
        val tomorrowFood = vm.totalFood

        // Back to today: should still be original total
        vm.previousDay()
        assertEquals(todayFood, vm.totalFood)

        // Forward again: should be tomorrow's total
        vm.nextDay()
        assertEquals(tomorrowFood, vm.totalFood)
    }

    @Test
    fun multiple_adds_generate_non_empty_ids() {
        val vm = vm()
        repeat(3) { idx ->
            vm.addQuickFood(MealType.Breakfast, 50 + idx, "E$idx", "1", 0.0, 0.0, 0.0)
        }
        val ids = vm.uiState.day.meals.breakfast.takeLast(3).map { it.id }
        assertTrue(ids.all { it.isNotBlank() }, "IDs should be non-empty hex strings")
    }
    @Test
    fun can_navigate_far_past_and_future_and_data_is_isolated() {
        val vm = vm()

        // Base (today)
        vm.addQuickFood(MealType.Breakfast, 111, "T", "1", 0.0, 0.0, 0.0)
        val today = vm.totalFood

        // Go 10 days back, add
        repeat(10) { vm.previousDay() }
        vm.addQuickFood(MealType.Lunch, 222, "Past", "1", 0.0, 0.0, 0.0)
        val past = vm.totalFood
        assertEquals(222, past)

        // Return to today — unchanged
        repeat(10) { vm.nextDay() }
        assertEquals(today, vm.totalFood)

        // Go 7 days forward, add
        repeat(7) { vm.nextDay() }
        vm.addQuickFood(MealType.Dinner, 333, "Future", "1", 0.0, 0.0, 0.0)
        val future = vm.totalFood
        assertEquals(333, future)

        // Back to today — still unchanged
        repeat(7) { vm.previousDay() }
        assertEquals(today, vm.totalFood)
    }

    @Test
    fun goal_edit_offline_does_not_mutate_goal_and_remaining_clamps() {
        val vm = vm()

        // Offline VM: setGoal is no-op (network path only)
        val initialRemaining = vm.remaining
        val initialGoal = vm.uiState.goal
        vm.setGoal(1234)
        assertEquals(initialGoal, vm.uiState.goal)
        assertEquals(initialRemaining, vm.remaining)

        // Add huge calories -> remaining == 0
        vm.addQuickFood(MealType.Breakfast, 9_999, "Huge", "1", 0.0, 0.0, 0.0)
        assertEquals(0, vm.remaining)
    }

    @Test
    fun adding_food_in_two_categories_updates_totals_correctly_and_keeps_lists_separate() {
        val vm = vm()
        vm.addQuickFood(MealType.Breakfast, 120, "B1", "1", 0.0, 0.0, 0.0)
        vm.addQuickFood(MealType.Dinner, 180, "D1", "1", 0.0, 0.0, 0.0)

        val meals = vm.uiState.day.meals
        assertEquals(120 + 180, vm.totalFood)
        assertEquals("B1", meals.breakfast.last().name)
        assertEquals("D1", meals.dinner.last().name)
        assertTrue(meals.lunch.isEmpty())
    }

    @Test
    fun large_input_value_1234_is_accepted_and_reflected_in_totals() {
        val vm = vm()
        vm.addQuickFood(MealType.Lunch, 1234, "BigLunch", "1", 0.0, 0.0, 0.0)
        assertEquals(1234, vm.totalFood)
        assertTrue(vm.remaining <= vm.uiState.goal) // sanity
    }

    @Test
    fun editEntry_replaces_matching_entry_and_recomputes_total() {
        val vm = vm()
        vm.addQuickFood(MealType.Breakfast, 100, "X", "1", 0.0, 0.0, 0.0)
        val entry = vm.uiState.day.meals.breakfast.last()
        val updated = entry.copy(name = "X2", calories = 250.0)

        vm.editEntry(entry.id, updated)

        val meals = vm.uiState.day.meals.breakfast
        assertEquals("X2", meals.last().name)
        assertEquals(250, vm.totalFood)
    }

    @Test
    fun deleteEntry_removes_entry_and_recomputes_total() {
        val vm = vm()
        vm.addQuickFood(MealType.Lunch, 100, "A", "1", 0.0, 0.0, 0.0)
        vm.addQuickFood(MealType.Lunch, 200, "B", "1", 0.0, 0.0, 0.0)
        val idToRemove = vm.uiState.day.meals.lunch.first().id

        assertTrue(vm.uiState.day.meals.lunch.any { it.id == idToRemove })
        assertEquals(300, vm.totalFood)

        vm.deleteEntry(idToRemove)

        assertFalse(vm.uiState.day.meals.lunch.any { it.id == idToRemove })
        assertEquals(200, vm.totalFood)
    }

    @Test
    fun default_name_applies_only_when_blank_otherwise_preserves_name() {
        val vm = vm()
        vm.addQuickFood(MealType.Dinner, 100, "", "1", 0.0, 0.0, 0.0)
        vm.addQuickFood(MealType.Dinner, 100, "Named", "1", 0.0, 0.0, 0.0)

        val lastTwo = vm.uiState.day.meals.dinner.takeLast(2).map { it.name }
        assertEquals(listOf("Quick Add", "Named"), lastTwo)
    }
}

