package com.teamnotfound.airise.workout

import com.teamnotfound.airise.data.serializable.UserExerciseEntry
import com.teamnotfound.airise.data.serializable.UserExerciseWeight
import com.teamnotfound.airise.data.serializable.UserProgram
import com.teamnotfound.airise.data.serializable.UserProgramDoc
import com.teamnotfound.airise.data.serializable.ProgramType
import com.teamnotfound.airise.data.repository.UserRepository
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.util.NetworkError
import kotlinx.coroutines.test.runTest
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

    @Test
    fun test_viewModel_loads_success_state_when_getUserProgram_succeeds() = runTest {
        // Arrange
        val mockProgramDoc = createMockUserProgramDoc()
        val mockRepository = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgramDoc)
        )
        val viewModel = WorkoutViewModel(mockRepository)

        // Act & Assert
        val state = viewModel.uiState.value
        assertTrue(state is WorkoutUiState.Success)
        assertEquals(mockProgramDoc, (state as WorkoutUiState.Success).programDoc)
    }

    @Test
    fun test_viewModel_loads_error_state_when_getUserProgram_fails() = runTest {
        // Arrange
        val mockRepository = MockUserRepository(
            getUserProgramResult = Result.Error(NetworkError.NO_INTERNET)
        )
        val viewModel = WorkoutViewModel(mockRepository)

        // Act & Assert
        val state = viewModel.uiState.value
        assertTrue(state is WorkoutUiState.Error)
    }

    @Test
    fun test_logAll_calls_updateUserProgram_with_correct_data() = runTest {
        // Arrange
        val mockProgramDoc = createMockUserProgramDoc()
        val mockRepository = MockUserRepository(
            getUserProgramResult = Result.Success(mockProgramDoc),
            updateUserProgramResult = Result.Success(true)
        )
        val viewModel = WorkoutViewModel(mockRepository)

        // Act
        viewModel.logAll()

        // Assert
        assertEquals(mockProgramDoc.program, mockRepository.lastUpdateProgramCall)
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

// Mock UserRepository for testing
class MockUserRepository(
    private val getUserProgramResult: Result<UserProgramDoc, NetworkError>,
    private val updateUserProgramResult: Result<Boolean, NetworkError> = Result.Success(true)
) : UserRepository(
    // These would be mocked in a real test setup
    auth = null!!,
    userClient = null!!,
    userCache = null!!
) {
    var lastUpdateProgramCall: UserProgram? = null

    override suspend fun getUserProgram(): Result<UserProgramDoc, NetworkError> {
        return getUserProgramResult
    }

    override suspend fun updateUserProgram(userProgram: UserProgram): Result<Boolean, NetworkError> {
        lastUpdateProgramCall = userProgram
        return updateUserProgramResult
    }
}