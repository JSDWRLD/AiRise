package com.teamnotfound.airise.generativeAi

import com.teamnotfound.airise.BuildKonfig
import dev.shreyaspatil.ai.client.generativeai.Chat
import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.Content
import dev.shreyaspatil.ai.client.generativeai.type.GenerateContentResponse
import dev.shreyaspatil.ai.client.generativeai.type.PlatformImage
import dev.shreyaspatil.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.Flow

class GeminiApi {
    companion object {
        const val PROMPT_TODAYS_OVERVIEW = "Wrtie a 5 sentence fitness summary for today to a user using fake data from the perspective a of a coach."
    }


    private val apiKey = BuildKonfig.GEMINI_API_KEY


    val generativeVisionModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = apiKey
    )

    val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = apiKey
    )

    fun generateContent(prompt: String): Flow<GenerateContentResponse> {
        return generativeModel.generateContentStream(prompt)
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