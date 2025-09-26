package com.teamnotfound.airise.generativeAi

import kotlin.test.*

class AiMessageTest {

    @Test
    fun `AiMessage should be created with user model`() {
        val message = AiMessage(
            aiModel = "user",
            message = "Hello, coach!"
        )

        assertEquals("user", message.aiModel)
        assertEquals("Hello, coach!", message.message)
    }

    @Test
    fun `AiMessage should be created with model model`() {
        val message = AiMessage(
            aiModel = "model",
            message = "Hello! How can I help you?"
        )

        assertEquals("model", message.aiModel)
        assertEquals("Hello! How can I help you?", message.message)
    }

    @Test
    fun `AiMessage should handle empty message`() {
        val message = AiMessage(
            aiModel = "user",
            message = ""
        )

        assertEquals("", message.message)
        assertTrue(message.message.isEmpty())
    }

    @Test
    fun `AiMessage should handle long message text`() {
        val longText = "This is a very long message ".repeat(100)
        val message = AiMessage(
            aiModel = "model",
            message = longText
        )

        assertEquals(longText, message.message)
        assertTrue(message.message.length > 1000)
    }

    @Test
    fun `AiMessage should handle special characters and emojis`() {
        val specialMessage = "Test with emojis 🏋️💪 and special characters: é, ñ, ü!"
        val message = AiMessage(
            aiModel = "user",
            message = specialMessage
        )

        assertEquals(specialMessage, message.message)
        assertTrue(message.message.contains("🏋️"))
        assertTrue(message.message.contains("💪"))
    }

    @Test
    fun `AiMessage equality should work correctly`() {
        val message1 = AiMessage(aiModel = "user", message = "Hello")
        val message2 = AiMessage(aiModel = "user", message = "Hello")
        val message3 = AiMessage(aiModel = "model", message = "Hello")
        val message4 = AiMessage(aiModel = "user", message = "Hi")

        assertEquals(message1, message2)
        assertNotEquals(message1, message3)
        assertNotEquals(message1, message4)
    }

    @Test
    fun `AiMessage hashCode should be consistent`() {
        val message1 = AiMessage(aiModel = "user", message = "Test")
        val message2 = AiMessage(aiModel = "user", message = "Test")

        assertEquals(message1.hashCode(), message2.hashCode())
    }

    @Test
    fun `AiMessage should be immutable using copy`() {
        val original = AiMessage(aiModel = "user", message = "Original")
        val modified = original.copy(message = "Modified")

        assertEquals("Original", original.message)
        assertEquals("Modified", modified.message)
        assertNotEquals(original, modified)
    }

    @Test
    fun `AiMessage copy with only aiModel changed`() {
        val original = AiMessage(aiModel = "user", message = "Same message")
        val copied = original.copy(aiModel = "model")

        assertEquals("Same message", copied.message)
        assertEquals("model", copied.aiModel)
        assertEquals(original.message, copied.message)
    }

    @Test
    fun `AiMessage should handle multiline text`() {
        val multilineText = """
            Line 1: Introduction
            Line 2: Details
            Line 3: Conclusion
        """.trimIndent()

        val message = AiMessage(
            aiModel = "model",
            message = multilineText
        )

        assertTrue(message.message.contains("\n"))
        assertEquals(3, message.message.lines().size)
        assertTrue(message.message.contains("Introduction"))
    }

    @Test
    fun `AiMessage case sensitivity for aiModel`() {
        val lowerCase = AiMessage(aiModel = "user", message = "Test")
        val upperCase = AiMessage(aiModel = "USER", message = "Test")

        assertNotEquals(lowerCase.aiModel, upperCase.aiModel)
        assertNotEquals(lowerCase, upperCase)
    }

    @Test
    fun `AiMessage list operations work correctly`() {
        val messages = listOf(
            AiMessage("user", "First message"),
            AiMessage("model", "First response"),
            AiMessage("user", "Second message"),
            AiMessage("model", "Second response")
        )

        assertEquals(4, messages.size)
        assertEquals("user", messages.first().aiModel)
        assertEquals("model", messages.last().aiModel)

        val userMessages = messages.filter { it.aiModel == "user" }
        assertEquals(2, userMessages.size)
    }

    @Test
    fun `AiMessage toString should contain field values`() {
        val message = AiMessage(
            aiModel = "user",
            message = "Test message"
        )

        val stringRepresentation = message.toString()

        assertTrue(stringRepresentation.contains("user"))
        assertTrue(stringRepresentation.contains("Test message"))
    }

    @Test
    fun `AiMessage handles whitespace-only message`() {
        val whitespaceMessage = AiMessage(aiModel = "user", message = "   ")

        assertEquals("   ", whitespaceMessage.message)
        assertTrue(whitespaceMessage.message.isNotEmpty())
        assertTrue(whitespaceMessage.message.isBlank())
    }

    @Test
    fun `AiMessage conversation pattern validation`() {
        val conversation = listOf(
            AiMessage("user", "Hello"),
            AiMessage("model", "Hi there!"),
            AiMessage("user", "How are you?"),
            AiMessage("model", "I'm doing great!")
        )

        // Verify alternating pattern
        for (i in conversation.indices) {
            val expectedModel = if (i % 2 == 0) "user" else "model"
            assertEquals(expectedModel, conversation[i].aiModel)
        }
    }

    @Test
    fun `AiMessage with newlines and tabs`() {
        val complexMessage = "Line 1\n\tIndented line\nLine 3"
        val message = AiMessage("model", complexMessage)

        assertTrue(message.message.contains("\n"))
        assertTrue(message.message.contains("\t"))
        assertEquals(complexMessage, message.message)
    }

    @Test
    fun `AiMessage filtering by model type`() {
        val messages = listOf(
            AiMessage("user", "Q1"),
            AiMessage("model", "A1"),
            AiMessage("user", "Q2"),
            AiMessage("model", "A2"),
            AiMessage("user", "Q3")
        )

        val userCount = messages.count { it.aiModel == "user" }
        val modelCount = messages.count { it.aiModel == "model" }

        assertEquals(3, userCount)
        assertEquals(2, modelCount)
    }

    @Test
    fun `AiMessage data class component functions`() {
        val message = AiMessage("user", "Test")
        val (aiModel, messageText) = message

        assertEquals("user", aiModel)
        assertEquals("Test", messageText)
    }
}
