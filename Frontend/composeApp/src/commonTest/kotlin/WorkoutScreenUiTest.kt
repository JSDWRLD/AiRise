package com.teamnotfound.airise.workout

import com.teamnotfound.airise.data.serializable.UserExerciseEntry
import com.teamnotfound.airise.data.serializable.UserExerciseWeight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WorkoutScreenUiTest {

    private fun parseReps(input: String, maxLen: Int = 4): Int {
        val s = input.filter(Char::isDigit).take(maxLen)
        return s.toIntOrNull() ?: 0
    }

    private fun parseWeight(input: String): Double? {
        var s = input.filter { it.isDigit() || it == '.' }
        val firstDot = s.indexOf('.')
        if (firstDot != -1) {
            s = s.substring(0, firstDot + 1) + s.substring(firstDot + 1).replace(".", "")
        }
        return s.toDoubleOrNull()
    }

    @Test
    fun numberField_onValue_sends_parsed_int() {
        var captured: Int? = null
        val onValue: (Int) -> Unit = { captured = it }

        onValue(parseReps("a1b2c3"))
        assertEquals(123, captured)

        onValue(parseReps("999999"))
        assertEquals(9999, captured)

        onValue(parseReps(""))
        assertEquals(0, captured)
    }

    @Test
    fun decimalField_onValue_sends_parsed_double_or_null() {
        var captured: Double? = null
        val onValue: (Double?) -> Unit = { captured = it }

        onValue(parseWeight("145.5"))
        assertEquals(145.5, captured)

        onValue(parseWeight("a145..5b"))
        assertEquals(145.5, captured)

        onValue(parseWeight(""))
        assertNull(captured)
    }

    @Test
    fun daySection_onChange_forwards_exercise_name_and_values() {
        val exercise = UserExerciseEntry(
            name = "Bench Press",
            sets = 3,
            targetReps = "8-10",
            repsCompleted = 0,
            weight = UserExerciseWeight(value = 135, unit = "lbs")
        )

        var capturedName: String? = null
        var capturedReps: Int? = null
        var capturedWeight: Double? = null

        val onChange: (exerciseName: String, reps: Int?, weight: Double?) -> Unit = { name, reps, wt ->
            capturedName = name
            capturedReps = reps
            capturedWeight = wt
        }

        onChange(exercise.name, 10, null)
        assertEquals("Bench Press", capturedName)
        assertEquals(10, capturedReps)
        assertEquals(null, capturedWeight)

        onChange(exercise.name, null, 145.0)
        assertEquals("Bench Press", capturedName)
        assertEquals(null, capturedReps)
        assertEquals(145.0, capturedWeight)
    }

    @Test
    fun planned_line_matches_expected_format() {
        val e = UserExerciseEntry(
            name = "Squat",
            sets = 4,
            targetReps = "5",
            repsCompleted = 0,
            weight = UserExerciseWeight(value = 225, unit = "lbs")
        )

        val planned = "${e.sets} sets • ${e.targetReps} reps @ ${e.weight.value} ${e.weight.unit}"

        assertEquals("4 sets • 5 reps @ 225 lbs", planned)
    }
}
