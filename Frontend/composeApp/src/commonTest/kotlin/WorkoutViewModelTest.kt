package com.teamnotfound.airise.workout

import com.teamnotfound.airise.data.serializable.UserExerciseEntry
import com.teamnotfound.airise.data.serializable.UserExerciseWeight
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkoutViewModelTest {

    @Test
    fun test_number_field_updates_value_correctly() {
        // Arrange
        var capturedValue: Int? = null
        val initialValue = 5
        val newValue = 10

        // Act
        val onValueChange: (Int) -> Unit = { capturedValue = it }

        // Simulating the user typing "10"
        val newText = "10"
        onValueChange(newText.toIntOrNull() ?: 0)

        // Assert
        assertEquals(newValue, capturedValue, "The number field should capture the new integer value.")
    }

    @Test
    fun test_decimal_number_field_updates_value_correctly() {
        // Arrange
        var capturedValue: Double? = null
        val initialValue = 135.0
        val newValue = 145.5

        // Act
        val onValueChange: (Double?) -> Unit = { capturedValue = it }

        // Simulating the user typing "145.5"
        val newText = "145.5"
        onValueChange(newText.toDoubleOrNull())

        // Assert
        assertEquals(newValue, capturedValue, "The decimal number field should capture the new double value.")
    }

    @Test
    fun test_workout_card_onChange_is_called_with_correct_reps() {
        // Arrange
        var capturedReps: Int? = null
        var capturedWeight: Double? = null
        val repsValue = 15
        val initialExercise = UserExerciseEntry(
            name = "Test Exercise",
            sets = 3,
            targetReps = "10-12",
            repsCompleted = 0,
            weight = UserExerciseWeight(value = 100, unit = "kg")
        )

        // Act
        val onChange: (reps: Int?, weight: Double?) -> Unit = { reps, weight ->
            capturedReps = reps
            capturedWeight = weight
        }
        onChange(repsValue, null)

        // Assert
        assertEquals(repsValue, capturedReps, "onChange should be called with the new reps value.")
        assertEquals(null, capturedWeight, "Weight should not be changed when reps are updated.")
    }
}