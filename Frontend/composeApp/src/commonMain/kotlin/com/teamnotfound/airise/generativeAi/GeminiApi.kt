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
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

open class GeminiApi {
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

    suspend fun generateTodaysOverview(
        healthData: HealthData,
        dailyProgress: DailyProgressData? = null
    ): GenerateContentResponse {
        // Build compact context blocks
        val snapshot = healthSnapshot(healthData)
        val progress = progressSnapshot(dailyProgress)

        // If everything is zero/empty, still send a minimal prompt
        val contextBlock = buildString {
            snapshot?.let {
                appendLine("[SNAPSHOT_TODAY]")
                appendLine(it)
            }
            progress?.let {
                appendLine()
                appendLine("[PROGRESS]")
                appendLine(it)
            }
        }.ifBlank { "[SNAPSHOT_TODAY]\n(no metrics available)" }

        val prompt = buildString {
            appendLine("You are Coach Rise, a concise, supportive, evidence-based fitness coach.")
            appendLine("Write ONE short paragraph of 3–4 sentences (≤100 words).")
            appendLine("Use metrics below only as evidence—mention numbers/units and weave them into sentences.")
            appendLine("Start with quick praise tied to a metric, then give 1–2 specific next actions (time-bound or quantity-based).")
            appendLine("Prioritize the lowest areas in PROGRESS if present; otherwise suggest a small stretch goal for tomorrow.")
            appendLine("Do NOT list metrics, repeat the metrics block, or output headings/brackets/bullets. No 'Goal:' or 'Status:'.")
            appendLine("Do NOT invent numbers; copy exactly if you choose to mention them.")
            appendLine()
            appendLine("METRICS (context only):")
            appendLine(contextBlock) // your snapshot/progress text
        }.trim()


        return generativeModel.generateContent(prompt)
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
            if (h.caloriesBurned > 0) add("kcal_burned=${h.caloriesBurned}")
            if (h.caloriesEaten > 0) add("kcal_eaten = ${h.caloriesEaten}")
            if (h.caloriesTarget > 0) add("kcal_eaten_target = ${h.caloriesTarget}")
            if (h.sleep > 0f) add("sleep_h=${h.sleep}")
            if (h.hydration > 0f) add("water_oz=${h.hydration}")
            if (h.hydrationTarget > 0f) add("water_oz_target=${h.hydrationTarget}")
        }
        return parts.takeIf { it.isNotEmpty() }?.joinToString("; ")
    }

    private fun progressSnapshot(p: DailyProgressData?): String? {
        if (p == null) return null

        fun pct(x: Float) = x.coerceIn(0f, 100f).roundToInt()

        val total = pct(p.totalProgress)
        val workout = pct(p.caloriesProgress)
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

    open suspend fun chatReplyWithContext(
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
            text.ifBlank { "I couldn’t read that image. Try another photo or add a brief note about what you want me to analyze." }
        } catch (e: Exception) {
            // (optional) println("vision error: ${e.message}")
            "Sorry, I couldn’t analyze that image right now."
        }

    }
}