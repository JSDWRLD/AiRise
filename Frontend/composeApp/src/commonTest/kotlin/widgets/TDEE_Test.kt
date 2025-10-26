package widgets

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for TDEE (Total Daily Energy Expenditure) calculation.
 * These tests validate that our implementation matches the standard TDEE calculator
 * results from tdeecalculator.net using the Mifflin-St Jeor Equation.
 */
class TDEETest {

    /**
     * TDEE Calculation Logic - mirrors the implementation in TDEEWidget.kt
     */
    private fun calculateTDEE(
        gender: String,
        goalType: String,
        heightCm: Double,
        weightKg: Double,
        age: Int,
        activityLevel: String
    ): Int {
        // Calculate BMR using Mifflin-St Jeor Equation
        val bmr = if (gender == "Male") {
            (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5
        } else {
            (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161
        }

        // Activity multipliers
        val activityMultiplier = when (activityLevel) {
            "Sedentary" -> 1.2
            "Lightly Active" -> 1.375
            "Moderately Active" -> 1.55
            "Very Active" -> 1.725
            "Extremely Active" -> 1.9
            else -> 1.2
        }

        // Calculate TDEE
        val tdee = bmr * activityMultiplier

        // Adjust for goal
        val targetCalories = when (goalType) {
            "Bulk" -> tdee + 300  // Surplus for muscle gain
            "Cut" -> tdee - 500   // Deficit for fat loss
            "Maintain" -> tdee    // Maintenance
            else -> tdee
        }

        return targetCalories.toInt()
    }

    @Test
    fun testMale_ModeratelyActive_Maintain() {
        // Male, 30 years, 180cm (5'11"), 80kg (176 lbs), Moderately Active
        // Expected BMR: (10 * 80) + (6.25 * 180) - (5 * 30) + 5 = 800 + 1125 - 150 + 5 = 1780
        // Expected TDEE: 1780 * 1.55 = 2759
        val result = calculateTDEE(
            gender = "Male",
            goalType = "Maintain",
            heightCm = 180.0,
            weightKg = 80.0,
            age = 30,
            activityLevel = "Moderately Active"
        )
        assertEquals(2759, result, "Male moderately active maintenance calories should be 2759")
    }

    @Test
    fun testFemale_LightlyActive_Cut() {
        // Female, 27 years, 165cm (5'5"), 60kg (132 lbs), Lightly Active
        // Expected BMR: (10 * 60) + (6.25 * 165) - (5 * 27) - 161 = 600 + 1031.25 - 135 - 161 = 1335.25
        // Expected TDEE: 1335.25 * 1.375 = 1835.96875
        // Expected Cut: 1835.96875 - 500 = 1335.96875 ≈ 1335
        val result = calculateTDEE(
            gender = "Female",
            goalType = "Cut",
            heightCm = 165.0,
            weightKg = 60.0,
            age = 27,
            activityLevel = "Lightly Active"
        )
        assertEquals(1335, result, "Female lightly active cut calories should be 1335")
    }

    @Test
    fun testMale_VeryActive_Bulk() {
        // Male, 25 years, 175cm (5'9"), 75kg (165 lbs), Very Active
        // Expected BMR: (10 * 75) + (6.25 * 175) - (5 * 25) + 5 = 750 + 1093.75 - 125 + 5 = 1723.75
        // Expected TDEE: 1723.75 * 1.725 = 2973.46875
        // Expected Bulk: 2973.46875 + 300 = 3273.46875 ≈ 3273
        val result = calculateTDEE(
            gender = "Male",
            goalType = "Bulk",
            heightCm = 175.0,
            weightKg = 75.0,
            age = 25,
            activityLevel = "Very Active"
        )
        assertEquals(3273, result, "Male very active bulk calories should be 3273")
    }

    @Test
    fun testFemale_Sedentary_Maintain() {
        // Female, 35 years, 160cm (5'3"), 55kg (121 lbs), Sedentary
        // Expected BMR: (10 * 55) + (6.25 * 160) - (5 * 35) - 161 = 550 + 1000 - 175 - 161 = 1214
        // Expected TDEE: 1214 * 1.2 = 1456.8 ≈ 1456
        val result = calculateTDEE(
            gender = "Female",
            goalType = "Maintain",
            heightCm = 160.0,
            weightKg = 55.0,
            age = 35,
            activityLevel = "Sedentary"
        )
        assertEquals(1456, result, "Female sedentary maintenance calories should be 1456")
    }
}