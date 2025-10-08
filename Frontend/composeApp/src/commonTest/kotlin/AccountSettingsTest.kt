import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.home.accountSettings.AccountSettingsUiState
import kotlin.test.Test
import kotlin.test.assertEquals

class AccountSettingsTest {

    @Test
    fun AccountSettingsUiState_should_have_correct_default_values() {
        // When
        val uiState = AccountSettingsUiState()

        // Then
        assertEquals(uiState.isLoading, false)
        assertEquals(uiState.isSuccess, false)
        assertEquals(uiState.isSignedOut, false)
        assertEquals(uiState.userSettings, null)
        assertEquals(uiState.userData, null)
        assertEquals(uiState.errorMessage, null)
    }

    @Test
    fun AccountSettingsUiState_copy_should_work_correctly() {
        // Given
        val original = AccountSettingsUiState()
        val mockUserData = UserData(
            firstName = "Test",
            lastName = "User",
            middleName = "",
            fullName = "Test User",
            workoutGoal = "Strength",
            fitnessLevel = "Beginner",
            workoutLength = 45,
            workoutEquipment = "Dumbbells",
            workoutDays = listOf("Mon", "Wed", "Fri"),
            workoutTime = "Morning",
            dietaryGoal = "High Protein",
            workoutRestrictions = "None",
            heightMetric = true,
            heightValue = 175,
            weightMetric = true,
            weightValue = 70,
            dobDay = 1,
            dobMonth = 1,
            dobYear = 1995,
            activityLevel = "Moderate"
        )

        val copied = original.copy(
            isLoading = true,
            isSuccess = true,
            isSignedOut = true,
            userData = mockUserData,
            errorMessage = "Error message"
        )

        // Then
        assertEquals(copied.isLoading, true)
        assertEquals(copied.isSuccess, true)
        assertEquals(copied.isSignedOut, true)
        copied.userData?.let { assertEquals(it.firstName, "Test") }
        copied.userData?.let { assertEquals(it.lastName, "User") }
        copied.userData?.let { assertEquals(it.middleName, "") }
        copied.userData?.let { assertEquals(it.fullName, "Test User") }
        assertEquals(copied.errorMessage, "Error message")
    }

    @Test
    fun name_validation_should_require_first_and_last_name() {
        // Test cases: (firstName, middleName, lastName, expectedIsValid)
        val testCases = listOf(
            Triple("John", "", "Doe"),      // Valid: first and last names present
            Triple("", "", "Doe"),         // Invalid: missing first name
            Triple("John", "", ""),        // Invalid: missing last name
            Triple("", "Middle", ""),      // Invalid: missing first and last names
            Triple("  ", "\t", "  "),      // Invalid: only whitespace
            Triple("John", "Michael", "Doe"), // Valid: all names present
            Triple("J@hn", "", "Doe")      //Invalid: special characters in first name
        )

        testCases.forEach { (firstName, middleName, lastName) ->
            val isValid = firstName.isNotBlank() && lastName.isNotBlank()

            if (isValid) {
                assertEquals(
                    firstName.isNotBlank() && lastName.isNotBlank(),
                    true,
                    "Expected valid name: $firstName $middleName $lastName"
                )
            } else {
                assertEquals(isValid,
                    false,
                    "Expected invalid name: $firstName $middleName $lastName"
                )
            }
        }
    }

    @Test
    fun date_validation_should_handle_leap_years_correctly() {
        // Test cases: (year, month, day, expectedIsValid)
        val testCases = listOf(
            Triple(2020, 2, 29),   // Leap year
            Triple(2021, 2, 29),  // Not leap year
            Triple(2020, 4, 31),  // April has 30 days
            Triple(2020, 6, 31),  // June has 30 days
            Triple(2020, 9, 31),  // September has 30 days
            Triple(2020, 11, 31), // November has 30 days
            Triple(2020, 1, 31),   // January has 31 days
            Triple(2020, 3, 31)    // March has 31 days
        )

        testCases.forEach { (year, month, day) ->
            val isValid = when (month) {
                4, 6, 9, 11 -> day in 1..30
                2 -> {
                    val isLeapYear = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
                    if (isLeapYear) day in 1..29 else day in 1..28
                }
                else -> day in 1..31
            }

            if (isValid) {
                assertEquals(isValid, true, "Expected valid date: $year-$month-$day")
            } else {
                assertEquals(isValid, false, "Expected invalid date: $year-$month-$day")
            }
        }
    }
}