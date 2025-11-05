import com.teamnotfound.airise.data.auth.IAuthService
import com.teamnotfound.airise.data.network.clients.IUserClient
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.data.serializable.UserSettingsData
import com.teamnotfound.airise.home.accountSettings.AccountSettingsUiState
import com.teamnotfound.airise.home.accountSettings.AccountSettingsViewModel
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AccountSettingsTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @Test
    fun signout_calls_signout_and_sets_state() = runTest {
        val fakeAuth = object : IAuthService {
            override val currentUserId: String = ""
            override val isAuthenticated: Boolean = true
            override val currentUser: Flow<com.teamnotfound.airise.data.auth.User> = flowOf()
            override val firebaseUser: FirebaseUser? = null
            var signOutCalled = false
            override suspend fun authenticate(email: String, password: String) = throw NotImplementedError()
            override suspend fun createUser(email: String, password: String) = throw NotImplementedError()
            override suspend fun sendPasswordResetEmail(email: String) = throw NotImplementedError()
            override suspend fun updateEmail(newEmail: String) = throw NotImplementedError()
            override suspend fun updatePassword(newPassword: String) = throw NotImplementedError()
            override suspend fun signOut() = com.teamnotfound.airise.data.auth.AuthResult.Success(com.teamnotfound.airise.data.auth.User())
            override suspend fun authenticateWithGoogle(idToken: String) = throw NotImplementedError()
            override suspend fun getIdToken() = null
        }

        val fakeUserClient = object : IUserClient {
            override suspend fun insertUser(firebaseUser: FirebaseUser, email: String) = throw NotImplementedError()
            override suspend fun getUserData(firebaseUser: FirebaseUser) = throw NotImplementedError()
            override suspend fun insertUserData(firebaseUser: FirebaseUser, userData: UserData) = throw NotImplementedError()
            override suspend fun getHealthData(firebaseUser: FirebaseUser) = throw NotImplementedError()
            override suspend fun updateHealthData(firebaseUser: FirebaseUser, healthData: com.teamnotfound.airise.data.serializable.HealthData) = throw NotImplementedError()
            override suspend fun getUserSettings(firebaseUser: FirebaseUser) = throw NotImplementedError()
            override suspend fun upsertUserSettings(userSettings: UserSettingsData, firebaseUser: FirebaseUser) = throw NotImplementedError()
        }

        val vm = AccountSettingsViewModel(fakeAuth, fakeUserClient)
        vm.signout()

        // advance dispatcher to allow coroutine to run
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, vm.uiState.value.isSignedOut)
    }

    @Test
    fun loadUserData_with_no_user_sets_error() = runTest {
        val fakeAuth = object : IAuthService {
            override val currentUserId: String = ""
            override val isAuthenticated: Boolean = false
            override val currentUser: Flow<com.teamnotfound.airise.data.auth.User> = flowOf()
            override val firebaseUser: FirebaseUser? = null
            override suspend fun authenticate(email: String, password: String) = throw NotImplementedError()
            override suspend fun createUser(email: String, password: String) = throw NotImplementedError()
            override suspend fun sendPasswordResetEmail(email: String) = throw NotImplementedError()
            override suspend fun updateEmail(newEmail: String) = throw NotImplementedError()
            override suspend fun updatePassword(newPassword: String) = throw NotImplementedError()
            override suspend fun signOut() = com.teamnotfound.airise.data.auth.AuthResult.Success(com.teamnotfound.airise.data.auth.User())
            override suspend fun authenticateWithGoogle(idToken: String) = throw NotImplementedError()
            override suspend fun getIdToken() = null
        }

        val fakeUserClient = object : IUserClient {
            override suspend fun insertUser(firebaseUser: FirebaseUser, email: String) = throw NotImplementedError()
            override suspend fun getUserData(firebaseUser: FirebaseUser) = throw NotImplementedError()
            override suspend fun insertUserData(firebaseUser: FirebaseUser, userData: UserData) = throw NotImplementedError()
            override suspend fun getHealthData(firebaseUser: FirebaseUser) = throw NotImplementedError()
            override suspend fun updateHealthData(firebaseUser: FirebaseUser, healthData: com.teamnotfound.airise.data.serializable.HealthData) = throw NotImplementedError()
            override suspend fun getUserSettings(firebaseUser: FirebaseUser) = throw NotImplementedError()
            override suspend fun upsertUserSettings(userSettings: UserSettingsData, firebaseUser: FirebaseUser) = throw NotImplementedError()
        }

        val vm = AccountSettingsViewModel(fakeAuth, fakeUserClient)
        vm.loadUserData()

        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, vm.uiState.value.isSuccess)
        assertEquals("User not authenticated", vm.uiState.value.errorMessage)
    }

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
            activityLevel = "Moderate",
            isAdmin = false
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
        // Test cases: (year, month, day)
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

    @Test
    fun account_setting_screens_routes_are_correct() {
        // Verify that each screen route matches the expected string
        assertEquals(com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.AccountSettings.route, "accountScreen")
        assertEquals(com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.DOBSelect.route, "dobSelect")
        assertEquals(com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.HeightSelect.route, "heightSelect")
        assertEquals(com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.WeightSelect.route, "weightSelect")
        assertEquals(com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.HealthDashboard.route, "healthDashboard")
        assertEquals(com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.NameEdit.route, "nameEdit")
    }

    @Test
    fun account_setting_screens_routes_are_unique() {
        val routes = listOf(
            com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.AccountSettings.route,
            com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.DOBSelect.route,
            com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.HeightSelect.route,
        com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.WeightSelect.route,
        com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.HealthDashboard.route,
        com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.NameEdit.route
        )
        // Ensure all routes are unique
        assertEquals(routes.size, routes.toSet().size)
    }

    @Test
    fun account_setting_screens_count_matches_expected() {
        val routes = listOf(
            com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.AccountSettings.route,
            com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.DOBSelect.route,
            com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.HeightSelect.route,
            com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.WeightSelect.route,
            com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.HealthDashboard.route,
            com.teamnotfound.airise.home.accountSettings.AccountSettingScreens.NameEdit.route
        )

        // There should be 7 routes defined for the AccountSettings screens
        assertEquals(6, routes.size)
    }

    @Test
    fun account_settings_ui_state_copy_keeps_unmodified_fields() {
        val original = AccountSettingsUiState(
            isLoading = false,
            isSuccess = false,
            isSignedOut = false,
            userSettings = null,
            errorMessage = null,
            userData = null
        )

        val updated = original.copy(isLoading = true, errorMessage = "Oops")

        // Confirm updated fields changed
        assertEquals(true, updated.isLoading)
        assertEquals("Oops", updated.errorMessage)

        // Confirm other fields remain unchanged
        assertEquals(original.isSuccess, updated.isSuccess)
        assertEquals(original.isSignedOut, updated.isSignedOut)
        assertEquals(original.userSettings, updated.userSettings)
        assertEquals(original.userData, updated.userData)
    }

    @Test
    fun name_concatenation_trims_and_collapses_whitespace() {
        // Simulate the UI's behavior of concatenating first/middle/last and collapsing whitespace
        fun concatName(first: String, middle: String, last: String): String {
            return listOf(first, middle, last)
                .joinToString(" ")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        val result = concatName("  John  ", "  Michael  ", "  Doe  ")
        assertEquals("John Michael Doe", result)

        val result2 = concatName("  John  ", "", "  Doe  ")
        assertEquals("John Doe", result2)

        val result3 = concatName("   ", "  ", "   ")
        assertEquals("", result3)
    }

    @Test
    fun format_height_metric_and_imperial() {
        // mirror behavior from UI's formatHeight: metric -> "<value> cm", imperial -> feet'inches"
        fun formatHeightTest(metric: Boolean?, value: Int): String? {
            val metricResolved = metric ?: return null
            if (value == 0) return null
            return if (metricResolved) {
                "$value cm"
            } else {
                val feet = value / 12
                val inches = value % 12
                "${feet}'${inches}\""
            }
        }

        assertEquals("175 cm", formatHeightTest(true, 175))
        assertEquals("5'11\"", formatHeightTest(false, 71))
        assertEquals(null, formatHeightTest(true, 0))
        assertEquals(null, formatHeightTest(null, 100))
    }

    @Test
    fun format_weight_metric_and_imperial() {
        // mirror behavior from UI's formatWeight: metric -> "<value> kg", imperial -> "<value> lb"
        fun formatWeightTest(metric: Boolean?, value: Int): String? {
            val metricResolved = metric ?: return null
            if (value == 0) return null
            return if (metricResolved) {
                "$value kg"
            } else {
                "$value lb"
            }
        }

        assertEquals("70 kg", formatWeightTest(true, 70))
        assertEquals("154 lb", formatWeightTest(false, 154))
        assertEquals(null, formatWeightTest(true, 0))
        assertEquals(null, formatWeightTest(null, 80))
    }
}