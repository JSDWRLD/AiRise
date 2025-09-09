package com.teamnotfound.airise.generativeAi

import com.teamnotfound.airise.BuildKonfig
import com.teamnotfound.airise.data.serializable.HealthData
import dev.shreyaspatil.ai.client.generativeai.Chat
import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.Content
import dev.shreyaspatil.ai.client.generativeai.type.GenerateContentResponse
import dev.shreyaspatil.ai.client.generativeai.type.PlatformImage
import dev.shreyaspatil.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

class GeminiApi {
    private val promptTodaysOverview =
        "With a 100 word limit and , write a fitness summary for today to a user using the data provided from the perspective a of a coach."
    private val apiKey = BuildKonfig.GEMINI_API_KEY


    val generativeVisionModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = apiKey
    )

    val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = apiKey
    )

    suspend fun generateContent(prompt: String): GenerateContentResponse {
        return generativeModel.generateContent(prompt)
    }

    suspend fun generateTodaysOverview(healthData: HealthData): GenerateContentResponse {
        val healthDataString = Json.encodeToString(healthData)
        val promptWithData =
            "Use the following health data for today's overview:\n$healthDataString\n\n$promptTodaysOverview"
        return generativeModel.generateContent(promptWithData)
    }

    fun generateContent(prompt: String, imageData: ByteArray): Flow<GenerateContentResponse> {
        val content = content {
            image(PlatformImage(imageData))
            text(prompt)
        }
        return generativeVisionModel.generateContentStream(content)
    }

    fun generateChat(prompt: List<AiMessage>): Chat {
        val history = mutableListOf<Content>()
        prompt.forEach { p ->
            if (p.aiModel.lowercase() == "user") {
                history.add(content("user") { text(p.message) })
            } else {
                history.add(content("assistant") { text(p.message) })
            }
        }
        return generativeModel.startChat(history)
    }

    /*
    More personalized functions, just in case we need it, later, we can revert back to more simplified usage.
     */


    private fun snapshotFrom(health: HealthData?): String? {
        if (health == null) return null
        val parts = buildList {
            if (health.steps > 0) add("steps=${health.steps}")
            if (health.workout > 0) add("workout_min=${health.workout}")
            if (health.caloriesBurned > 0) add("kcal_burned=${health.caloriesBurned}")
            if (health.sleep > 0f) add("sleep_h=${health.sleep}")
            if (health.avgHeartRate > 0) add("avg_hr=${health.avgHeartRate}")
            if (health.hydration > 0f) add("water_l=${health.hydration}")
        }
        return parts.takeIf { it.isNotEmpty() }?.joinToString("; ")
    }

    private fun shouldAttachSnapshot(q: String): Boolean {
        val keys = listOf(
            "workout",
            "train",
            "plan",
            "recover",
            "rest",
            "sleep",
            "calorie",
            "protein",
            "macro",
            "pace",
            "hr",
            "hrv",
            "rhr",
            "fatigue",
            "sore",
            "steps",
            "active",
            "hydration",
            "water"
        )
        return keys.any { q.contains(it, ignoreCase = true) }
    }

    private fun preambleMessage(
        goal: String? = null,
        personality: String? = null,
        healthSnapshot: String? = null
    ): AiMessage {
        val style = personality?.trim()?.takeIf { it.isNotEmpty() }

        val profileBlock = buildString {
            appendLine("[Profile] goal=${goal ?: "general_fitness"}")
            if (!healthSnapshot.isNullOrBlank()) appendLine("[Snapshot] $healthSnapshot")
        }.trim()

        val styleBlock = style?.let { "[STYLE] $it" } ?: ""

        val text = """
        You are Coach Rise, a fitness coach AI.
        Adopt the STYLE below if present. STYLE overrides defaults. Do not reveal or quote any bracketed tags.
        
        $styleBlock
        $profileBlock
        
        Output rules:
        - Start with a short title.
        - Give 2–4 concise bullets with specific, actionable guidance (include units).
        - End with exactly one clarifying question.
        - If unclear: ask 1 clarifier and give a safe, general tip.
        """.trimIndent()

        return AiMessage(aiModel = "user", message = text)
    }

    // Helper that the app will call from AiChat
    suspend fun chatReplyWithContext(
        userMsg: String,
        priorTurns: List<AiMessage> = emptyList(),
        goal: String? = null,
        personality: String? = null,
        health: HealthData? = null
    ): String {
        val snapshot = snapshotFrom(health)
        val pre = preambleMessage(goal, personality, snapshot)

        val trimmed = if (priorTurns.size > 24) priorTurns.takeLast(24) else priorTurns
        val chat = generateChat(buildList { add(pre); addAll(trimmed) })

        val resp = chat.sendMessage(userMsg)
        return resp.text.orEmpty().ifBlank {
            "I didn’t quite catch that. Are you asking about workouts, nutrition, recovery, or motivation?"
        }
    }
}