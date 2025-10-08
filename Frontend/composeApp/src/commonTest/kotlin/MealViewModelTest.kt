package com.teamnotfound.airise.meal

import kotlinx.coroutines.test.runTest
import kotlin.test.*

class MealViewModelTest {

    @Test
    fun `initial state has today as default offset`() {
        val vm = MealViewModel.fake()

        assertEquals(1, vm.uiState.dayOffset)
        assertEquals(1900, vm.uiState.goal)
        assertFalse(vm.uiState.isLoading)
    }

    @Test
    fun `adding food increases total calories`() = runTest {
        val vm = MealViewModel.fake()

        // Initial total should be 0
        assertEquals(0, vm.totalFood)

        // Add a breakfast item
        vm.addQuickFood(
            meal = MealType.Breakfast,
            calories = 300,
            name = "Oatmeal",
            serving = "1 bowl",
            fats = 5.0,
            carbs = 50.0,
            proteins = 10.0
        )

        // Total should now be 300
        assertEquals(300, vm.totalFood)
        assertEquals(1, vm.uiState.day.meals.breakfast.size)
    }

    @Test
    fun `remaining calories calculation is correct`() = runTest {
        val vm = MealViewModel.fake(startGoal = 2000)

        // Remaining = Goal - Food + Exercise
        // 2000 - 0 + 0 = 2000
        assertEquals(2000, vm.remaining)

        // Add food (300 calories)
        vm.addQuickFood(
            meal = MealType.Lunch,
            calories = 300,
            name = "Salad",
            serving = "1 plate",
            fats = 10.0,
            carbs = 20.0,
            proteins = 15.0
        )

        // Remaining = 2000 - 300 + 0 = 1700
        assertEquals(1700, vm.remaining)
    }

    @Test
    fun `setting goal updates state`() {
        val vm = MealViewModel.fake()

        vm.setGoal(2500)

        assertEquals(2500, vm.uiState.goal)
    }

    @Test
    fun `next day increments offset`() {
        val vm = MealViewModel.fake(startOffset = 1)

        vm.nextDay()

        assertEquals(2, vm.uiState.dayOffset)
    }

    @Test
    fun `previous day decrements offset`() {
        val vm = MealViewModel.fake(startOffset = 1)

        vm.previousDay()

        assertEquals(0, vm.uiState.dayOffset)
    }

    @Test
    fun `deleting food entry removes it from meals`() = runTest {
        val vm = MealViewModel.fake()

        // Add a food entry
        vm.addQuickFood(
            meal = MealType.Dinner,
            calories = 500,
            name = "Pizza",
            serving = "2 slices",
            fats = 20.0,
            carbs = 60.0,
            proteins = 25.0
        )

        assertEquals(1, vm.uiState.day.meals.dinner.size)
        assertEquals(500, vm.totalFood)

        // Delete the entry
        val entryId = vm.uiState.day.meals.dinner.first().id
        vm.deleteEntry(entryId)

        assertEquals(0, vm.uiState.day.meals.dinner.size)
        assertEquals(0, vm.totalFood)
    }

    @Test
    fun `editing food entry updates its values`() = runTest {
        val vm = MealViewModel.fake()

        // Add initial entry
        vm.addQuickFood(
            meal = MealType.Breakfast,
            calories = 200,
            name = "Toast",
            serving = "2 slices",
            fats = 5.0,
            carbs = 30.0,
            proteins = 8.0
        )

        val originalEntry = vm.uiState.day.meals.breakfast.first()
        assertEquals("Toast", originalEntry.name)
        assertEquals(200.0, originalEntry.calories)

        // Edit the entry
        val updatedEntry = originalEntry.copy(
            name = "Toast with Butter",
            calories = 250.0,
            fats = 10.0
        )
        vm.editEntry(originalEntry.id, updatedEntry)

        val editedEntry = vm.uiState.day.meals.breakfast.first()
        assertEquals("Toast with Butter", editedEntry.name)
        assertEquals(250.0, editedEntry.calories)
        assertEquals(10.0, editedEntry.fats)
    }

    @Test
    fun `adding to different meals keeps them separate`() = runTest {
        val vm = MealViewModel.fake()

        vm.addQuickFood(MealType.Breakfast, 300, "Eggs", "2", 15.0, 2.0, 20.0)
        vm.addQuickFood(MealType.Lunch, 400, "Sandwich", "1", 10.0, 40.0, 20.0)
        vm.addQuickFood(MealType.Dinner, 600, "Steak", "1", 25.0, 5.0, 50.0)

        assertEquals(1, vm.uiState.day.meals.breakfast.size)
        assertEquals(1, vm.uiState.day.meals.lunch.size)
        assertEquals(1, vm.uiState.day.meals.dinner.size)
        assertEquals(1300, vm.totalFood)
    }
}