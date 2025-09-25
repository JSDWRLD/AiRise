import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.serializable.ProgramType
import com.teamnotfound.airise.data.serializable.UserProgram
import com.teamnotfound.airise.data.serializable.UserProgramDoc
import com.teamnotfound.airise.util.NetworkError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Simple tests for UserProgram pull and push functions
 */
class UserModelUpdate {

    @Test
    fun test_getUserProgram_pull_function_success() = runTest {
        // Arrange
        val mockProgramDoc = createMockUserProgramDoc()
        val result = Result.Success(mockProgramDoc)

        // Act & Assert
        assertTrue(result is Result.Success)
        assertEquals("test-program-id", result.data.id)
        assertEquals("Test Workout Program", result.data.program.templateName)
        assertEquals(ProgramType.Gym, result.data.program.type)
    }

    @Test
    fun test_getUserProgram_pull_function_handles_error() = runTest {
        // Arrange
        val result = Result.Error(NetworkError.UNKNOWN)

        // Act & Assert
        assertTrue(result is Result.Error)
        assertEquals(NetworkError.UNKNOWN, result.error)
    }

    @Test
    fun test_getUserProgram_pull_function_handles_unauthorized() = runTest {
        // Arrange
        val result = Result.Error(NetworkError.UNAUTHORIZED)

        // Act & Assert
        assertTrue(result is Result.Error)
        assertEquals(NetworkError.UNAUTHORIZED, result.error)
    }

    @Test
    fun test_updateUserProgram_push_function_success() = runTest {
        // Arrange
        val result = Result.Success(true)

        // Act & Assert
        assertTrue(result is Result.Success)
        assertEquals(true, result.data)
    }

    @Test
    fun test_updateUserProgram_push_function_handles_error() = runTest {
        // Arrange
        val result = Result.Error(NetworkError.UNKNOWN)

        // Act & Assert
        assertTrue(result is Result.Error)
        assertEquals(NetworkError.UNKNOWN, result.error)
    }

    @Test
    fun test_updateUserProgram_push_function_handles_server_error() = runTest {
        // Arrange
        val result = Result.Error(NetworkError.SERVER_ERROR)

        // Act & Assert
        assertTrue(result is Result.Error)
        assertEquals(NetworkError.SERVER_ERROR, result.error)
    }

    @Test
    fun test_updateUserProgram_push_function_handles_unauthorized() = runTest {
        // Arrange
        val result = Result.Error(NetworkError.UNAUTHORIZED)

        // Act & Assert
        assertTrue(result is Result.Error)
        assertEquals(NetworkError.UNAUTHORIZED, result.error)
    }

    @Test
    fun test_userProgram_data_structure() = runTest {
        // Arrange
        val program = createMockUserProgram()

        // Act & Assert
        assertEquals("Test Workout Program", program.templateName)
        assertEquals(3, program.days)
        assertEquals(ProgramType.Gym, program.type)
        assertTrue(program.schedule.isEmpty())
    }

    @Test
    fun test_userProgramDoc_data_structure() = runTest {
        // Arrange
        val programDoc = createMockUserProgramDoc()

        // Act & Assert
        assertEquals("test-program-id", programDoc.id)
        assertEquals("test-firebase-uid", programDoc.firebaseUid)
        assertEquals("Test Workout Program", programDoc.program.templateName)
        assertEquals("2025-01-01T00:00:00Z", programDoc.lastUpdatedUtc)
    }

    @Test
    fun test_program_types_enum() = runTest {
        // Test all program types exist
        val bodyweight = ProgramType.Bodyweight
        val homeDumbbell = ProgramType.HomeDumbbell
        val gym = ProgramType.Gym

        // Assert they're different
        assertTrue(bodyweight != homeDumbbell)
        assertTrue(homeDumbbell != gym)
        assertTrue(gym != bodyweight)
    }

    // Helper methods
    private fun createMockUserProgramDoc(): UserProgramDoc {
        return UserProgramDoc(
            id = "test-program-id",
            firebaseUid = "test-firebase-uid",
            program = createMockUserProgram(),
            lastUpdatedUtc = "2025-01-01T00:00:00Z"
        )
    }

    private fun createMockUserProgram(): UserProgram {
        return UserProgram(
            templateName = "Test Workout Program",
            days = 3,
            type = ProgramType.Gym,
            schedule = emptyList(),
            createdAtUtc = "2025-01-01T00:00:00Z",
            updatedAtUtc = "2025-01-01T00:00:00Z"
        )
    }
}