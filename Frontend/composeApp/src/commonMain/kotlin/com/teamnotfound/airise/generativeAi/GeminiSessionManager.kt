package com.teamnotfound.airise.generativeAi

import com.teamnotfound.airise.data.serializable.DailyProgressData
import com.teamnotfound.airise.data.serializable.HealthData
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import dev.shreyaspatil.ai.client.generativeai.type.GenerateContentResponse
import dev.shreyaspatil.ai.client.generativeai.type.TextPart

class GeminiSessionManager(
    private val api: GeminiApi,
    private val debounceMs: Long = 500,
    private val maxChars: Int = 4000
) {
    private val mutex = Mutex()
    private val promptCache = LinkedHashMap<String, String>() // normalized prompt -> reply
    private var overviewCache: String? = null
    private var lastPromptAtMs: Long = 0L

    suspend fun sendPrompt(
        prompt: String,
        priorTurns: List<AiMessage>,
        workoutGoal: String? = null,
        dietaryGoal: String? = null,
        activityLevel: String? = null,
        fitnessLevel: String? = null,
        workoutLength: Int? = null,
        workoutRestrictions: String? = null,
        healthData: HealthData? = null,
        dailyProgressData: DailyProgressData? = null
    ): String = mutex.withLock {
        val norm = normalize(prompt)
        if (norm.isEmpty()) return ""

        // cache
        promptCache[norm]?.let {
            println("[Gemini] cache HIT prompt=\"$norm\"")
            return it
        }
        println("[Gemini] cache MISS prompt=\"$norm\"")

        // debounce
        val now = nowMillis()
        val elapsed = now - lastPromptAtMs
        if (elapsed in 0 until debounceMs) {
            delay(debounceMs - elapsed)
        }
        lastPromptAtMs = nowMillis()

        // truncate
        val safePrompt = truncate(prompt)

        val reply = api.chatReplyWithContext(
            userMsg = safePrompt,
            priorTurns = priorTurns,
            workoutGoal = workoutGoal,
            dietaryGoal = dietaryGoal,
            activityLevel = activityLevel,
            fitnessLevel = fitnessLevel,
            workoutLength = workoutLength,
            workoutRestrictions = workoutRestrictions,
            healthData = healthData,
            dailyProgressData = dailyProgressData
        )
        promptCache[norm] = reply
        reply
    }

    suspend fun getOverview(
        healthData: HealthData,
        progress: DailyProgressData? = null,
        forceRefresh: Boolean = false
    ): String = mutex.withLock {
        if (!forceRefresh) {
            overviewCache?.let {
                println("[Gemini] overview cache HIT")
                return it
            }
        }
        println("[Gemini] overview cache MISS/REFRESH")

        val res: GenerateContentResponse = api.generateTodaysOverview(healthData, progress)
        val text = flatten(res)

        overviewCache = text
        return text
    }

    private fun flatten(res: GenerateContentResponse): String {
        val cand = res.candidates?.firstOrNull() ?: return ""
        val parts = cand.content?.parts ?: return ""
        return buildString {
            for (part in parts) {
                when (part) {
                    is TextPart -> append(part.text)

                    else -> Unit
                }
            }
        }
    }


    private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

    private fun normalize(s: String): String =
        s.trim().lowercase().replace(Regex("\\s+"), " ")

    private fun truncate(s: String): String =
        if (s.length <= maxChars) s else s.take(maxChars) + "…"
}
