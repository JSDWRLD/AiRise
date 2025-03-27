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
    private val promptTodaysOverview = "With a 100 word limit and , write a fitness summary for today to a user using the data provided from the perspective a of a coach."
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
        val promptWithData = "Use the following health data for today's overview:\n$healthDataString\n\n$promptTodaysOverview"
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
}