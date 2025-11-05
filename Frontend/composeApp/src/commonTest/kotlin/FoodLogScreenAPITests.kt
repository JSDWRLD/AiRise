package com.teamnotfound.airise.meal

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

class FoodLogScreenAPITests {

    private fun liveEnabled(): Boolean = System.getenv("AIRISE_LIVE_TESTS") == "1"

    private fun fakeVm() = MealViewModel.fake(startOffset = 1)

    @Test
    fun setGoal_offline_does_not_change_goal() {
        if (liveEnabled()) {
            println("AIRISE_LIVE_TESTS=1, but live dependencies are not available. Running offline (fake VM) test.")
        }

        val vm = fakeVm()
        val initialGoal = vm.uiState.goal
        vm.setGoal(1800)

        assertEquals(initialGoal, vm.uiState.goal, "Offline setGoal should not change the goal")
    }

    @Test
    fun addQuickFood_adds_new_entry() {
        val vm = fakeVm()
        vm.addQuickFood(MealType.Breakfast, 250, "LiveEggs", "1", 5.0, 1.0, 6.0)
        val entry = vm.uiState.day.meals.breakfast.lastOrNull()

        assertNotNull(entry, "Breakfast entry should be added")
        assertEquals("LiveEggs", entry.name)
        assertEquals(250, vm.totalFood)
    }

    @Test
    fun editEntry_updates_existing_entry() {
        val vm = fakeVm()
        vm.addQuickFood(MealType.Breakfast, 250, "LiveEggs", "1", 5.0, 1.0, 6.0)
        val entry = vm.uiState.day.meals.breakfast.last()
        val edited = entry.copy(name = "LiveEggsEdited", calories = 300.0)

        vm.editEntry(entry.id, edited)
        val afterEdit = vm.uiState.day.meals.breakfast.last()

        assertEquals("LiveEggsEdited", afterEdit.name)
        assertEquals(300, vm.totalFood)
    }

    @Test
    fun deleteEntry_removes_entry() {
        val vm = fakeVm()
        vm.addQuickFood(MealType.Breakfast, 250, "LiveEggs", "1", 5.0, 1.0, 6.0)
        val entry = vm.uiState.day.meals.breakfast.last()

        vm.deleteEntry(entry.id)
        val stillThere = vm.uiState.day.meals.breakfast.any { it.id == entry.id }

        assertFalse(stillThere, "Entry should be deleted in offline mode")
    }
}
