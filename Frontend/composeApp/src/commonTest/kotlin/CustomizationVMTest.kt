package com.teamnotfound.airise.customize

import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.data.serializable.UserDataUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


private fun fullBase(): UserDataUiState = UserDataUiState().apply {
    firstName.value = "John"
    lastName.value = "Appleseed"
    middleName.value = ""
    fullName.value = "John Appleseed"

    workoutGoal.value = "Lose Weight"
    fitnessLevel.value = "Beginner"
    workoutLength.value = 30

    equipmentAccess.value = "home"
    workoutDays.value = listOf("Monday", "Thursday")
    workoutTime.value = "Evening"

    dietaryGoal.value = "High Protein"
    workoutRestrictions.value = ""

    heightMetric.value = false
    heightValue.value = 70
    weightMetric.value = false
    weightValue.value = 180

    dobDay.value = 1
    dobMonth.value = 1
    dobYear.value = 1990

    activityLevel.value = "Lightly Active"
}

private data class OnboardingDataUpdate(
    val workoutDays: List<String>,
    val workoutTime: String,
    val workoutEquipment: String
)

private fun applyUpdate(base: UserDataUiState, update: OnboardingDataUpdate): UserData {
    base.workoutDays.value = update.workoutDays
    base.workoutTime.value = update.workoutTime
    base.equipmentAccess.value = update.workoutEquipment
    return base.toData()
}


class CustomizationVMTest {

    @Test
    fun toData_maps_equipmentAccess_to_workoutEquipment() {
        val ui = fullBase()
        ui.equipmentAccess.value = "gym"
        val data = ui.toData()
        assertEquals("gym", data.workoutEquipment)
    }

    @Test
    fun applyUpdate_changes_only_the_three_fields_and_keeps_rest() {
        val base = fullBase()
        val updated = applyUpdate(
            base,
            OnboardingDataUpdate(
                workoutDays = listOf("Monday", "Wednesday", "Friday"),
                workoutTime = "Morning",
                workoutEquipment = "bodyweight"
            )
        )

        assertEquals(listOf("Monday", "Wednesday", "Friday"), updated.workoutDays)
        assertEquals("Morning", updated.workoutTime)
        assertEquals("bodyweight", updated.workoutEquipment)

        assertEquals("John", updated.firstName)
        assertEquals("Appleseed", updated.lastName)
        assertEquals(30, updated.workoutLength)
        assertTrue(updated.heightValue > 0)
        assertTrue(updated.weightValue > 0)
        assertEquals(1990, updated.dobYear)
    }

    @Test
    fun timesCsv_roundTrips_cleanly() {
        val csv = "Morning, Evening"
        val set = csv.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val roundTrip = set.joinToString(", ")
        val a = csv.split(',').map { it.trim() }.toSet()
        val b = roundTrip.split(',').map { it.trim() }.toSet()
        assertEquals(a, b)
    }

    @Test
    fun timesCsv_parsing_handles_extra_spaces_and_commas() {
        val messy = "  Morning  , ,  Evening ,  "
        val set = messy.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val clean = set.joinToString(", ")
        val expected = setOf("Morning", "Evening")
        assertEquals(expected, clean.split(',').map { it.trim() }.toSet())
    }

    @Test
    fun applyUpdate_allows_empty_times_and_empty_days() {
        val base = fullBase()
        val updated = applyUpdate(
            base,
            OnboardingDataUpdate(
                workoutDays = emptyList(),
                workoutTime = "",
                workoutEquipment = "home"
            )
        )
        assertTrue(updated.workoutDays.isEmpty())
        assertEquals("", updated.workoutTime)
        assertEquals("home", updated.workoutEquipment)
    }

    @Test
    fun workoutDays_order_is_preserved_in_toData() {
        val base = fullBase()
        val ordered = listOf("Sunday", "Tuesday", "Saturday")
        val updated = applyUpdate(
            base,
            OnboardingDataUpdate(
                workoutDays = ordered,
                workoutTime = "Evening",
                workoutEquipment = "gym"
            )
        )
        assertEquals(ordered, updated.workoutDays)
    }

    @Test
    fun applyUpdate_does_not_touch_height_weight_or_metrics() {
        val base = fullBase()
        val heightBefore = base.heightValue.value
        val weightBefore = base.weightValue.value
        val heightMetricBefore = base.heightMetric.value
        val weightMetricBefore = base.weightMetric.value

        val updated = applyUpdate(
            base,
            OnboardingDataUpdate(
                workoutDays = listOf("Mon"),
                workoutTime = "Morning",
                workoutEquipment = "bodyweight"
            )
        )

        assertEquals(heightBefore, updated.heightValue)
        assertEquals(weightBefore, updated.weightValue)
        assertEquals(heightMetricBefore, updated.heightMetric)
        assertEquals(weightMetricBefore, updated.weightMetric)
    }

    @Test
    fun minimal_toData_produces_UserData_with_defaults() {
        val ui = UserDataUiState()
        val data = ui.toData()
        assertEquals("", data.firstName)
        assertEquals("", data.workoutEquipment)
        assertEquals(0, data.workoutLength)
        assertEquals(0, data.dobYear)
        assertTrue(data.workoutDays.isEmpty())
    }
}
