package widgets

import com.teamnotfound.airise.customize.TDEECalculator
import com.teamnotfound.airise.data.serializable.HealthData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for TDEE Widget using TDEECalculator.
 * Tests the actual calculation and validation logic from the source code.
 */
class TDEEWidgetTest {

    /**
     * Test 5: Verify that updating health data preserves existing fields
     * When setting calorie target, other health data fields should remain unchanged
     */
    @Test
    fun test_updateHealthData_success_preserves_existing_fields() {
        // Arrange
        val existingHealthData = HealthData(
            sleep = 7.5,
            steps = 10000,
            caloriesBurned = 2500,
            caloriesEaten = 2000,
            caloriesTarget = 2200,
            hydration = 64.0,
            hydrationTarget = 80.0
        )
        
        val newCalorieTarget = 2800

        // Act - Simulate the TDEE widget's data copy logic
        val updatedHealthData = existingHealthData.copy(
            caloriesTarget = newCalorieTarget
        )

        // Assert - Verify all existing fields are preserved
        assertEquals(7.5, updatedHealthData.sleep, "Sleep should be preserved")
        assertEquals(10000, updatedHealthData.steps, "Steps should be preserved")
        assertEquals(2500, updatedHealthData.caloriesBurned, "Calories burned should be preserved")
        assertEquals(2000, updatedHealthData.caloriesEaten, "Calories eaten should be preserved")
        assertEquals(64.0, updatedHealthData.hydration, "Hydration should be preserved")
        assertEquals(80.0, updatedHealthData.hydrationTarget, "Hydration target should be preserved")
        
        // Verify only calorie target was updated
        assertEquals(newCalorieTarget, updatedHealthData.caloriesTarget, "Calorie target should be updated")
    }

    /**
     * Test 6: Verify unit conversion functions work correctly
     * Tests feetInchesToCm and lbsToKg from TDEECalculator
     */
    @Test
    fun test_unit_conversions_are_accurate() {
        // Test feet/inches to cm
        val heightCm = TDEECalculator.feetInchesToCm(5, 10)
        assertEquals(177.8, heightCm, "5'10\" should equal 177.8 cm")
        
        val heightCm2 = TDEECalculator.feetInchesToCm(6, 0)
        assertEquals(182.88, heightCm2, "6'0\" should equal 182.88 cm")
        
        // Test lbs to kg
        val weightKg = TDEECalculator.lbsToKg(180.0)
        assertEquals(81.64656, weightKg, 0.001, "180 lbs should equal ~81.65 kg")
        
        val weightKg2 = TDEECalculator.lbsToKg(150.0)
        assertEquals(68.0388, weightKg2, 0.001, "150 lbs should equal ~68.04 kg")
    }

    /**
     * Test 7: Verify calculate button enable/disable logic
     * Tests isCalculateButtonEnabled from TDEECalculator
     */
    @Test
    fun test_calculate_button_disabled_when_required_fields_empty() {
        // Test with all fields empty
        val allEmpty = TDEECalculator.isCalculateButtonEnabled(
            gender = "",
            goalType = "",
            age = "",
            heightFeet = "",
            heightInches = "",
            weightLbs = "",
            activityLevel = ""
        )
        assertEquals(false, allEmpty, "Button should be disabled when all fields empty")

        // Test with some fields filled
        val someEmpty = TDEECalculator.isCalculateButtonEnabled(
            gender = "Male",
            goalType = "Bulk",
            age = "25",
            heightFeet = "",
            heightInches = "",
            weightLbs = "",
            activityLevel = ""
        )
        assertEquals(false, someEmpty, "Button should be disabled when some fields empty")

        // Test with all fields filled
        val allFilled = TDEECalculator.isCalculateButtonEnabled(
            gender = "Male",
            goalType = "Bulk",
            age = "25",
            heightFeet = "5",
            heightInches = "10",
            weightLbs = "180",
            activityLevel = "Moderately Active"
        )
        assertEquals(true, allFilled, "Button should be enabled when all fields filled")
    }

    /**
     * Test 8: Verify invalid height input is rejected
     * Tests isValidHeight from TDEECalculator
     */
    @Test
    fun test_invalid_height_input_rejected() {
        // Test feet > 8
        val invalidFeet = TDEECalculator.isValidHeight(feet = 9, inches = 0)
        assertEquals(false, invalidFeet, "Feet > 8 should be invalid")

        // Test inches > 11
        val invalidInches = TDEECalculator.isValidHeight(feet = 5, inches = 12)
        assertEquals(false, invalidInches, "Inches > 11 should be invalid")

        // Test negative feet
        val negativeFeet = TDEECalculator.isValidHeight(feet = -1, inches = 0)
        assertEquals(false, negativeFeet, "Negative feet should be invalid")

        // Test negative inches
        val negativeInches = TDEECalculator.isValidHeight(feet = 5, inches = -1)
        assertEquals(false, negativeInches, "Negative inches should be invalid")

        // Test valid height
        val validHeight = TDEECalculator.isValidHeight(feet = 5, inches = 10)
        assertEquals(true, validHeight, "Valid height should be accepted")

        // Test boundary values
        val maxFeet = TDEECalculator.isValidHeight(feet = 8, inches = 11)
        assertEquals(true, maxFeet, "Max valid height should be accepted")
    }

    /**
     * Test 9: Verify invalid weight input is rejected
     * Tests isValidWeight from TDEECalculator
     */
    @Test
    fun test_invalid_weight_input_rejected() {
        // Test zero weight
        val zeroWeight = TDEECalculator.isValidWeight(0.0)
        assertEquals(false, zeroWeight, "Zero weight should be invalid")

        // Test negative weight
        val negativeWeight = TDEECalculator.isValidWeight(-50.0)
        assertEquals(false, negativeWeight, "Negative weight should be invalid")

        // Test valid weight
        val validWeight = TDEECalculator.isValidWeight(150.5)
        assertEquals(true, validWeight, "Valid weight should be accepted")

        // Test very small positive weight
        val smallWeight = TDEECalculator.isValidWeight(0.1)
        assertEquals(true, smallWeight, "Small positive weight should be accepted")
    }

    /**
     * Test 10: Verify TDEE calculation produces correct calorie target
     * Tests calculateTDEE from TDEECalculator
     */
    @Test
    fun test_tdee_calculation_with_actual_calculator() {
        // Arrange - Input values
        val gender = "Male"
        val goalType = "Maintain"
        val heightCm = 180.0
        val weightKg = 80.0
        val age = 30
        val activityLevel = "Moderately Active"

        // Act - Calculate TDEE using actual TDEECalculator
        val calculatedCalories = TDEECalculator.calculateTDEE(
            gender = gender,
            goalType = goalType,
            heightCm = heightCm,
            weightKg = weightKg,
            age = age,
            activityLevel = activityLevel
        )

        // Prepare health data update
        val healthDataUpdate = HealthData(caloriesTarget = calculatedCalories)

        // Assert
        assertNotNull(healthDataUpdate, "Health data should be prepared")
        assertEquals(
            2759,
            healthDataUpdate.caloriesTarget,
            "Calorie target should match calculated TDEE"
        )
    }
}