package com.teamnotfound.airise.generativeAi

import com.teamnotfound.airise.BuildKonfig
import com.teamnotfound.airise.data.serializable.DailyProgressData
import com.teamnotfound.airise.data.serializable.HealthData
import dev.shreyaspatil.ai.client.generativeai.Chat
import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.Content
import dev.shreyaspatil.ai.client.generativeai.type.GenerateContentResponse
import dev.shreyaspatil.ai.client.generativeai.type.PlatformImage
import dev.shreyaspatil.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

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

    suspend fun generateContentOnce(
        prompt: String,
        imageData: ByteArray
    ): String {
        val c = content {
            image(PlatformImage(imageData))
            text(prompt)
        }
        val resp = generativeVisionModel.generateContent(c)
        return resp.text.orEmpty()
    }



    fun generateChat(prompt: List<AiMessage>): Chat {
        val history = mutableListOf<Content>()
        prompt.forEach { p ->
            val role = if (p.aiModel.equals("user", ignoreCase = true)) "user" else "model"
            history.add(content(role) { text(p.message) })
        }
        return generativeModel.startChat(history)
    }

    /*
    More personalized functions, just in case we need it, later, we can revert back to more simplified usage.
     */


    private fun healthSnapshot(h: HealthData?): String? {
        if (h == null) return null
        val parts = buildList {
            if (h.steps > 0) add("steps=${h.steps}")
            if (h.workout > 0) add("workout_min=${h.workout}")
            if (h.caloriesBurned > 0) add("kcal_burned=${h.caloriesBurned}")
            if (h.sleep > 0f) add("sleep_h=${h.sleep}")
            if (h.avgHeartRate > 0) add("avg_hr=${h.avgHeartRate}")
            if (h.hydration > 0f) add("water_l=${h.hydration}")
        }
        return parts.takeIf { it.isNotEmpty() }?.joinToString("; ")
    }

    private fun progressSnapshot(p: DailyProgressData?): String? {
        if (p == null) return null

        fun pct(x: Float) = x.coerceIn(0f, 100f).roundToInt()

        val total = pct(p.totalProgress)
        val workout = pct(p.workoutProgress)
        val sleep   = pct(p.sleepProgress)
        val hydra   = pct(p.hydrationProgress)

        return "progress_total=$total; workout=$workout; sleep=$sleep; hydration=$hydra"
    }

    private fun buildProfileBlock(
        workoutGoal: String?,
        fitnessLevel: String?,
        workoutLength: Int?,
        activityLevel: String?,
        dietaryGoal: String?,
        workoutRestrictions: String?
    ): String? {
        val lines = buildList {
            workoutGoal?.takeIf { it.isNotBlank() }?.let { add("goal=$it") }
            fitnessLevel?.takeIf { it.isNotBlank() }?.let { add("fitness_level=$it") }
            workoutLength?.takeIf { it > 0 }?.let { add("session_length_min=$it") }
            activityLevel?.takeIf { it.isNotBlank() }?.let { add("activity_level=$it") }
            dietaryGoal?.takeIf { it.isNotBlank() }?.let { add("diet_goal=$it") }
            workoutRestrictions?.takeIf { it.isNotBlank() }?.let { add("restrictions=$it") }
        }
        return lines.takeIf { it.isNotEmpty() }?.joinToString("\n")
    }

    suspend fun chatReplyWithContext(
        userMsg: String,
        priorTurns: List<AiMessage> = emptyList(),
        workoutGoal: String? = null,
        dietaryGoal: String? = null,
        activityLevel: String? = null,
        fitnessLevel: String? = null,
        workoutLength: Int? = null,
        workoutRestrictions: String? = null,
        healthData: HealthData? = null,
        dailyProgressData: DailyProgressData? = null
    ): String {
        // Build compact context blocks
        val profileBlock = buildProfileBlock(
            workoutGoal = workoutGoal,
            fitnessLevel = fitnessLevel,
            workoutLength = workoutLength,
            activityLevel = activityLevel,
            dietaryGoal = dietaryGoal,
            workoutRestrictions = workoutRestrictions
        )
        val snapshotBlock = healthSnapshot(healthData)
        val progressBlock = progressSnapshot(dailyProgressData)

        // Compose preamble
        val preambleText = buildString {
            appendLine("You are Coach Rise, a concise, supportive, evidence-based fitness coach.")
            appendLine("Use PROFILE for long-term tailoring and SNAPSHOT_TODAY for day-to-day adjustments.")
            appendLine("Output a short title, 2–4 specific bullets (with units), then exactly one clarifying question.")
            appendLine("If data is missing, do not invent numbers—omit them.")

            profileBlock?.let {
                appendLine()
                appendLine("[PROFILE]")
                appendLine(profileBlock)
            }

            snapshotBlock?.let {
                appendLine()
                appendLine("[SNAPSHOT_TODAY]")
                appendLine(it)
            }
            progressBlock?.let {
                appendLine()
                appendLine("[PROGRESS]")
                appendLine(it)
            }
        }.trim()

        val preamble = AiMessage(aiModel = "user", message = preambleText)

        // Keep history small for latency
        val trimmed = if (priorTurns.size > 24) priorTurns.takeLast(24) else priorTurns

        // Call Gemini chat
        val chat = generateChat(buildList { add(preamble); addAll(trimmed) })
        val resp = chat.sendMessage(userMsg)

        // Fallback for unclear/empty responses
        return resp.text.orEmpty().ifBlank {
            "I didn’t quite catch that. Are you asking about workouts, nutrition, recovery, or motivation?"
        }
    }

    suspend fun visionReplyWithContext(
        userMsg: String,
        imageData: ByteArray,
        workoutGoal: String? = null,
        dietaryGoal: String? = null,
        activityLevel: String? = null,
        fitnessLevel: String? = null,
        workoutLength: Int? = null,
        workoutRestrictions: String? = null,
        healthData: HealthData? = null,
        dailyProgressData: DailyProgressData? = null
    ): String {
        // Reuse your existing builders
        val profileBlock = buildProfileBlock(
            workoutGoal, fitnessLevel, workoutLength, activityLevel, dietaryGoal, workoutRestrictions
        )
        val snapshotBlock = healthSnapshot(healthData)
        val progressBlock = progressSnapshot(dailyProgressData)

        val prompt = buildString {
            appendLine("You are Coach Rise, a concise, supportive, evidence-based fitness coach.")
            appendLine("Use PROFILE for long-term tailoring and SNAPSHOT_TODAY for day-to-day adjustments.")
            appendLine("Output a short title, 2–4 specific bullets (with units), then exactly one clarifying question.")
            appendLine("If data is missing, do not invent numbers—omit them.")

            profileBlock?.let {
                appendLine()
                appendLine("[PROFILE]")
                appendLine(it)
            }
            snapshotBlock?.let {
                appendLine()
                appendLine("[SNAPSHOT_TODAY]")
                appendLine(it)
            }
            progressBlock?.let {
                appendLine()
                appendLine("[PROGRESS]")
                appendLine(it)
            }

            appendLine()
            appendLine("[USER_IMAGE_PROMPT]")
            appendLine(userMsg.ifBlank { "Analyze this image and tailor advice to my profile and today’s snapshot." })
        }.trim()

        return try {
            val text = generateContentOnce(prompt, imageData)
            if (text.isNotBlank()) text
            else "I couldn’t read that image. Try another photo or add a brief note about what you want me to analyze."
        } catch (e: Exception) {
            // (optional) println("vision error: ${e.message}")
            "Sorry, I couldn’t analyze that image right now."
        }

    }
}