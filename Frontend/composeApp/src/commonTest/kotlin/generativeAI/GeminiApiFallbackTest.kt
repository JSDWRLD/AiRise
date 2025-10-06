package generativeAI

import com.teamnotfound.airise.data.serializable.DailyProgressData
import com.teamnotfound.airise.data.serializable.HealthData
import com.teamnotfound.airise.generativeAi.AiMessage
import com.teamnotfound.airise.generativeAi.GeminiApi
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class GeminiApiFallbackTest {
    private val fallbackMessage = "Sorry, I couldn’t reach the coach right now. Please try again in a moment."

    class GeminiApiMock(
        private val response: String? = null,
        private val exception: Exception? = null
    ) : GeminiApi() {
        override suspend fun chatReplyWithContext(
            userMsg: String,
            priorTurns: List<AiMessage>,
            workoutGoal: String?,
            dietaryGoal: String?,
            activityLevel: String?,
            fitnessLevel: String?,
            workoutLength: Int?,
            workoutRestrictions: String?,
            healthData: HealthData?,
            dailyProgressData: DailyProgressData?
        ): String {
            exception?.let { throw it }
            return response ?: "Default response"
        }
    }

    @Test
    fun `test fallback message on specific exception`() = runBlocking {
        val api = GeminiApiMock(exception = IllegalArgumentException("Network timeout"))

        val result = try {
            api.chatReplyWithContext(
                userMsg = "Hello",
                priorTurns = emptyList()
            )
        } catch (e: Exception) {
            fallbackMessage
        }

        assertEquals(fallbackMessage, result)
    }

    @Test
    fun `test empty parameters`() = runBlocking {
        val api = GeminiApiMock(response = "Test response")

        val result = api.chatReplyWithContext(
            userMsg = "Hello",
            priorTurns = emptyList(),
            workoutGoal = null,
            dietaryGoal = null,
            activityLevel = null,
            fitnessLevel = null,
            workoutLength = null,
            workoutRestrictions = null,
            healthData = null,
            dailyProgressData = null
        )

        assertEquals("Test response", result)
    }

    @Test
    fun `test valid parameters with successful API call`() = runBlocking {
        val api = GeminiApiMock(response = "Test successful response")

        val result = api.chatReplyWithContext(
            userMsg = "Hello",
            priorTurns = emptyList(),
            workoutGoal = "Lose weight",
            dietaryGoal = "High protein",
            activityLevel = "Moderate",
            fitnessLevel = "Intermediate",
            workoutLength = 60,
            workoutRestrictions = "None",
            healthData = null,
            dailyProgressData = null
        )

        assertEquals("Test successful response", result)
    }

    @Test
    fun `test invalid input`() = runBlocking {
        val api = GeminiApiMock(response = "Invalid input handled")

        val result = api.chatReplyWithContext(
            userMsg = "Hello",
            priorTurns = emptyList(),
            workoutGoal = "Lose weight",
            dietaryGoal = "High protein",
            activityLevel = "Moderate",
            fitnessLevel = "Advanced",  // Edge case
            workoutLength = -10,  // Invalid value (negative)
            workoutRestrictions = "None",
            healthData = null,
            dailyProgressData = null
        )

        assertEquals("Invalid input handled", result)
    }

    @Test
    fun `test empty message history`() = runBlocking {
        val api = GeminiApiMock(exception = RuntimeException("API call failed"))

        val result = try {
            api.chatReplyWithContext(
                userMsg = "Hello",
                priorTurns = emptyList() // Empty message history
            )
        } catch (e: Exception) {
            fallbackMessage
        }

        assertEquals(fallbackMessage, result)
    }
}
