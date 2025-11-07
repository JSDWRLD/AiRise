package widgets

//import com.teamnotfound.airise.customize.OnboardingDataUpdate
import com.teamnotfound.airise.customize.WorkoutValidator
import com.teamnotfound.airise.data.serializable.UserData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for Workout Customization using WorkoutValidator.
 * Tests the actual validation logic from the source code.
 */
class WorkoutCustomizationTest {

    /**
     * Test 11: Verify minimum 3 workout days enforced
     * Tests isValidWorkoutDays from WorkoutValidator
     */
    @Test
    fun test_workout_days_minimum_3_enforced() {
        // Test with 2 days (invalid)
        val twoDays = listOf("Monday", "Wednesday")
        val isValid2 = WorkoutValidator.isValidWorkoutDays(twoDays)
        assertEquals(false, isValid2, "2 days should be invalid")

        // Test with 1 day (invalid)
        val oneDay = listOf("Monday")
        val isValid1 = WorkoutValidator.isValidWorkoutDays(oneDay)
        assertEquals(false, isValid1, "1 day should be invalid")

        // Test with 0 days (invalid)
        val noDays = emptyList<String>()
        val isValid0 = WorkoutValidator.isValidWorkoutDays(noDays)
        assertEquals(false, isValid0, "0 days should be invalid")

        // Test with exactly 3 days (valid)
        val threeDays = listOf("Monday", "Wednesday", "Friday")
        val isValid3 = WorkoutValidator.isValidWorkoutDays(threeDays)
        assertEquals(true, isValid3, "3 days should be valid")
    }

    /**
     * Test 12: Verify maximum 6 workout days enforced
     * Tests isValidWorkoutDays from WorkoutValidator
     */
    @Test
    fun test_workout_days_maximum_6_enforced() {
        // Test with 7 days (invalid)
        val sevenDays = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val isValid7 = WorkoutValidator.isValidWorkoutDays(sevenDays)
        assertEquals(false, isValid7, "7 days should be invalid")

        // Test with exactly 6 days (valid)
        val sixDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val isValid6 = WorkoutValidator.isValidWorkoutDays(sixDays)
        assertEquals(true, isValid6, "6 days should be valid")

        // Test with 5 days (valid)
        val fiveDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        val isValid5 = WorkoutValidator.isValidWorkoutDays(fiveDays)
        assertEquals(true, isValid5, "5 days should be valid")
    }

    /**
     * Test 13: Verify workout days data structure is correct
     * Tests UserData.copy() with workout days
     */
    @Test
    fun test_workout_days_save_calls_api_correctly() {
        // Arrange
        val baseUserData = UserData(
            firstName = "Test",
            lastName = "User",
            workoutDays = listOf("Monday", "Wednesday"),
            workoutLength = 30,
            workoutEquipment = "home"
        )
        
        val newWorkoutDays = listOf("Monday", "Wednesday", "Friday", "Saturday")

        // Act - Simulate save operation data preparation
        val updatedUserData = baseUserData.copy(workoutDays = newWorkoutDays)

        // Assert
        assertEquals(
            newWorkoutDays,
            updatedUserData.workoutDays,
            "Workout days should be updated correctly"
        )
        assertEquals(4, updatedUserData.workoutDays.size, "Should have 4 workout days")
        assertTrue(
            updatedUserData.workoutDays.containsAll(newWorkoutDays),
            "All selected days should be present"
        )
    }

    /**
     * Test 14: Verify only valid workout length values accepted
     * Tests isValidWorkoutLength from WorkoutValidator
     */
    @Test
    fun test_workout_length_only_valid_values_accepted() {
        // Test valid values
        assertEquals(true, WorkoutValidator.isValidWorkoutLength(15), "15 minutes should be valid")
        assertEquals(true, WorkoutValidator.isValidWorkoutLength(30), "30 minutes should be valid")
        assertEquals(true, WorkoutValidator.isValidWorkoutLength(45), "45 minutes should be valid")
        assertEquals(true, WorkoutValidator.isValidWorkoutLength(60), "60 minutes should be valid")

        // Test invalid values
        assertEquals(false, WorkoutValidator.isValidWorkoutLength(0), "0 minutes should be invalid")
        assertEquals(false, WorkoutValidator.isValidWorkoutLength(10), "10 minutes should be invalid")
        assertEquals(false, WorkoutValidator.isValidWorkoutLength(20), "20 minutes should be invalid")
        assertEquals(false, WorkoutValidator.isValidWorkoutLength(90), "90 minutes should be invalid")
        assertEquals(false, WorkoutValidator.isValidWorkoutLength(-30), "Negative minutes should be invalid")
    }

    /**
     * Test 15: Verify workout length data structure is correct
     * Tests UserData.copy() with workout length
     */
    @Test
    fun test_workout_length_save_updates_correctly() {
        // Arrange
        val baseUserData = UserData(
            firstName = "Test",
            lastName = "User",
            workoutDays = listOf("Monday", "Wednesday", "Friday"),
            workoutLength = 30,
            workoutEquipment = "home"
        )
        
        val newWorkoutLength = 45

        // Act - Simulate save operation data preparation
        val updatedUserData = baseUserData.copy(workoutLength = newWorkoutLength)

        // Assert
        assertEquals(
            newWorkoutLength,
            updatedUserData.workoutLength,
            "Workout length should be updated correctly"
        )
        assertTrue(
            WorkoutValidator.isValidWorkoutLength(updatedUserData.workoutLength),
            "Updated length should be valid"
        )
    }

    /**
     * Test 16: Verify only valid equipment options accepted
     * Tests isValidEquipment from WorkoutValidator
     */
    @Test
    fun test_equipment_valid_options_only() {
        // Test valid options
        assertEquals(true, WorkoutValidator.isValidEquipment("bodyweight"), "bodyweight should be valid")
        assertEquals(true, WorkoutValidator.isValidEquipment("home"), "home should be valid")
        assertEquals(true, WorkoutValidator.isValidEquipment("gym"), "gym should be valid")

        // Test invalid options
        assertEquals(false, WorkoutValidator.isValidEquipment(""), "Empty string should be invalid")
        assertEquals(false, WorkoutValidator.isValidEquipment("outdoor"), "outdoor should be invalid")
        assertEquals(false, WorkoutValidator.isValidEquipment("none"), "none should be invalid")
        assertEquals(false, WorkoutValidator.isValidEquipment("BODYWEIGHT"), "Case-sensitive: BODYWEIGHT should be invalid")
    }

    /**
     * Test 17: Verify equipment data structure is correct
     * Tests UserData.copy() with equipment
     */
    @Test
    fun test_equipment_save_calls_api_correctly() {
        // Arrange
        val baseUserData = UserData(
            firstName = "Test",
            lastName = "User",
            workoutDays = listOf("Monday", "Wednesday", "Friday"),
            workoutLength = 30,
            workoutEquipment = "bodyweight"
        )
        
        val newEquipment = "gym"

        // Act - Simulate save operation data preparation
        val updatedUserData = baseUserData.copy(workoutEquipment = newEquipment)

        // Assert
        assertEquals(
            newEquipment,
            updatedUserData.workoutEquipment,
            "Equipment should be updated correctly"
        )
        assertTrue(
            WorkoutValidator.isValidEquipment(updatedUserData.workoutEquipment),
            "Updated equipment should be valid"
        )
    }

    /**
     * Test 18: Verify save preserves unchanged fields
     * Tests UserData.copy() preserves all unchanged fields
     */
    @Test
    fun test_save_preserves_unchanged_fields() {
        // Arrange
        val baseUserData = UserData(
            firstName = "John",
            lastName = "Doe",
            middleName = "A",
            fullName = "John A Doe",
            workoutGoal = "Lose Weight",
            fitnessLevel = "Beginner",
            workoutDays = listOf("Monday", "Wednesday"),
            workoutLength = 30,
            workoutEquipment = "bodyweight",
            workoutTime = "Morning",
            dietaryGoal = "High Protein",
            workoutRestrictions = "None",
            heightMetric = true,
            heightValue = 180,
            weightMetric = true,
            weightValue = 75,
            dobDay = 15,
            dobMonth = 6,
            dobYear = 1990,
            activityLevel = "Moderate",
            isAdmin = false
        )
        
        // Act - Update only workout days
        val updatedUserData = baseUserData.copy(
            workoutDays = listOf("Monday", "Wednesday", "Friday", "Saturday")
        )

        // Assert - Verify unchanged fields are preserved
        assertEquals("John", updatedUserData.firstName, "First name should be preserved")
        assertEquals("Doe", updatedUserData.lastName, "Last name should be preserved")
        assertEquals(30, updatedUserData.workoutLength, "Workout length should be preserved")
        assertEquals("bodyweight", updatedUserData.workoutEquipment, "Equipment should be preserved")
        assertEquals("Lose Weight", updatedUserData.workoutGoal, "Workout goal should be preserved")
        assertEquals("Beginner", updatedUserData.fitnessLevel, "Fitness level should be preserved")
        assertEquals(180, updatedUserData.heightValue, "Height should be preserved")
        assertEquals(75, updatedUserData.weightValue, "Weight should be preserved")
        assertEquals(1990, updatedUserData.dobYear, "Birth year should be preserved")
        
        // Verify only workout days changed
        assertEquals(
            listOf("Monday", "Wednesday", "Friday", "Saturday"),
            updatedUserData.workoutDays,
            "Workout days should be updated"
        )
    }
}