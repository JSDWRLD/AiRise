package com.teamnotfound.airise.generativeAi

import com.teamnotfound.airise.data.serializable.DailyProgressData
import com.teamnotfound.airise.data.serializable.HealthData
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.delay

/**
 * Per-screen/session helper: caches prompt->reply and overview,
 * normalizes inputs, debounces, truncates, and logs hit/miss.
 */
class GeminiSessionManager(
    private val api: GeminiApi,
    private val debounceMs: Long = 500,
    private val maxChars: Int = 4000
) {
    private val mutex = Mutex()
    private val promptCache = LinkedHashMap<String, String>()      // normalized prompt -> reply
    private var overviewCache: String? = null
    private var lastPromptAt: Long = 0L

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

        // prompt cache
        promptCache[norm]?.let {
            println("[Gemini] cache HIT prompt=\"$norm\"")
            return it
        }
        println("[Gemini] cache MISS prompt=\"$norm\"")

        // debounce consecutive sends
        val now = System.currentTimeMillis()
        if (now - lastPromptAt < debounceMs) {
            val wait = debounceMs - (now - lastPromptAt)
            if (wait > 0) delay(wait)
        }
        lastPromptAt = System.currentTimeMillis()

        // truncate (char-safe)
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
        // If your API returns String already, keep as-is:
        val text = api.generateTodaysOverview(healthData, progress)
        overviewCache = text
        return text
    }

    private fun normalize(s: String): String =
        s.trim().lowercase().replace(Regex("\\s+"), " ")

    private fun truncate(s: String): String =
        if (s.length <= maxChars) s else s.take(maxChars) + "…"
}
