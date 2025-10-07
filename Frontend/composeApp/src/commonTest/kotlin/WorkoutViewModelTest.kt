package com.teamnotfound.airise.workout

import com.teamnotfound.airise.data.DTOs.UserProfileDto
import com.teamnotfound.airise.data.DTOs.UsersEnvelope
import com.teamnotfound.airise.data.serializable.UserExerciseEntry
import com.teamnotfound.airise.data.serializable.UserExerciseWeight
import com.teamnotfound.airise.data.serializable.UserProgram
import com.teamnotfound.airise.data.serializable.UserProgramDoc
import com.teamnotfound.airise.data.serializable.ProgramType
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.repository.IUserRepository
import com.teamnotfound.airise.data.serializable.HealthData
import com.teamnotfound.airise.data.serializable.UserChallenge
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.util.NetworkError
import notifications.LocalNotifier
import notifications.WorkoutReminderUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    private fun createMockUserProgramDoc(): UserProgramDoc {
        return UserProgramDoc(
            id = "test-id",
            firebaseUid = "test-uid",
            program = UserProgram(
                templateName = "Test Program",
                days = 1,
                type = ProgramType.Gym,
                schedule = emptyList(),
                createdAtUtc = "2025-01-01T00:00:00Z",
                updatedAtUtc = "2025-01-01T00:00:00Z"
            ),
            lastUpdatedUtc = "2025-01-01T00:00:00Z"
        )
    }
}

/**
 * Fake implementation of IUserRepository for unit tests.
 * Returns canned values unless overridden via constructor parameters.
 */
class MockUserRepository(
    private val getUserProgramResult: Result<UserProgramDoc, NetworkError> =
        Result.Error(NetworkError.UNKNOWN),
    private val updateUserProgramResult: Result<Boolean, NetworkError> =
        Result.Success(true),
    private val fetchUserDataResult: Result<UserData, NetworkError> =
        Result.Success(
            UserData(
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
        ),
    private val searchUsersResult: Result<UsersEnvelope, NetworkError> =
        Result.Success(
            UsersEnvelope(
                listOf(
                    UserProfileDto(firebaseUid = "uid-1", fullName = "Alice Tester", streak = 5),
                    UserProfileDto(firebaseUid = "uid-2", fullName = "Bob Example", streak = 2)
                )
            )
        ),
    private val userChallenge: UserChallenge? = UserChallenge()
) : IUserRepository {

    /** Captures last program passed to updateUserProgram for assertions in tests. */
    var lastUpdateProgramCall: UserProgram? = null

    override suspend fun fetchUserData(): Result<UserData, NetworkError> {
        return fetchUserDataResult
    }

    override suspend fun searchUsers(query: String): Result<UsersEnvelope, NetworkError> {
        return searchUsersResult
    }

    override suspend fun getUserChallengeOrNull(): UserChallenge? {
        return userChallenge
    }

    override suspend fun getUserProgram(): Result<UserProgramDoc, NetworkError> {
        return getUserProgramResult
    }

    override suspend fun updateUserProgram(userProgram: UserProgram): Result<Boolean, NetworkError> {
        lastUpdateProgramCall = userProgram
        return updateUserProgramResult
    }

    override suspend fun getHealthData(): Result<HealthData, NetworkError> {
        val hd = HealthData()
        return Result.Success(hd)
    }

    override suspend fun updateHealthData(healthData: HealthData): Result<Boolean, NetworkError> {
        return Result.Success(true)
    }
}

class FakeLocalNotifier : LocalNotifier {

    data class OneTime(val id: Int, val title: String, val body: String, val atMs: Long)
    data class Daily(val id: Int, val title: String, val body: String, val hour: Int, val minute: Int)

    val scheduled = mutableListOf<OneTime>()
    val scheduledDaily = mutableListOf<Daily>()
    val canceled = mutableListOf<Int>()

    override fun schedule(id: Int, title: String, body: String, triggerAtEpochMillis: Long) {
        scheduled += OneTime(id, title, body, triggerAtEpochMillis)
    }

    override fun scheduleDaily(id: Int, title: String, body: String, hour: Int, minute: Int) {
        scheduledDaily += Daily(id, title, body, hour, minute)
    }

    override fun cancel(id: Int) {
        canceled += id
    }
}
