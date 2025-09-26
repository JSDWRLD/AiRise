package com.teamnotfound.airise.home

import kotlin.test.*

class MessageTest {

    @Test
    fun `Message should be created with user message`() {
        val message = Message(
            text = "Hello, how can I improve my workout?",
            ai = false
        )

        assertEquals("Hello, how can I improve my workout?", message.text)
        assertFalse(message.ai)
    }

    @Test
    fun `Message should be created with AI message`() {
        val message = Message(
            text = "Here are some tips for improving your workout...",
            ai = true
        )

        assertEquals("Here are some tips for improving your workout...", message.text)
        assertTrue(message.ai)
    }

    @Test
    fun `Message should handle empty text`() {
        val message = Message(text = "", ai = false)

        assertTrue(message.text.isEmpty())
        assertEquals("", message.text)
    }

    @Test
    fun `Message should handle special characters and emojis`() {
        val messageText = "Great job! 💪 Keep up the good work! 🏋️"
        val message = Message(text = messageText, ai = true)

        assertEquals(messageText, message.text)
        assertTrue(message.text.contains("💪"))
        assertTrue(message.text.contains("🏋️"))
    }

    @Test
    fun `Message should handle long text`() {
        val longText = "This is a detailed response ".repeat(50)
        val message = Message(text = longText, ai = true)

        assertTrue(message.text.length > 1000)
        assertEquals(longText, message.text)
    }

    @Test
    fun `Message should handle multiline text`() {
        val multilineText = """
            First line of advice
            Second line of advice
            Third line of advice
        """.trimIndent()

        val message = Message(text = multilineText, ai = true)

        assertTrue(message.text.contains("\n"))
        assertEquals(3, message.text.lines().size)
    }

    @Test
    fun `Message equality should work correctly`() {
        val message1 = Message(text = "Hello", ai = false)
        val message2 = Message(text = "Hello", ai = false)
        val message3 = Message(text = "Hello", ai = true)
        val message4 = Message(text = "Hi", ai = false)

        assertEquals(message1, message2)
        assertNotEquals(message1, message3)
        assertNotEquals(message1, message4)
    }

    @Test
    fun `Message hashCode should be consistent`() {
        val message1 = Message(text = "Test", ai = true)
        val message2 = Message(text = "Test", ai = true)

        assertEquals(message1.hashCode(), message2.hashCode())
    }

    @Test
    fun `Message should be immutable using copy`() {
        val original = Message(text = "Original", ai = false)
        val modified = original.copy(text = "Modified")

        assertEquals("Original", original.text)
        assertEquals("Modified", modified.text)
        assertNotEquals(original, modified)
    }

    @Test
    fun `Message copy with only ai flag changed`() {
        val original = Message(text = "Same text", ai = false)
        val copied = original.copy(ai = true)

        assertEquals("Same text", copied.text)
        assertTrue(copied.ai)
        assertNotEquals(original.ai, copied.ai)
    }

    @Test
    fun `Message list operations should work correctly`() {
        val messages = listOf(
            Message("Hello", ai = false),
            Message("Hi there!", ai = true),
            Message("How are you?", ai = false),
            Message("I'm doing great!", ai = true)
        )

        assertEquals(4, messages.size)

        val userMessages = messages.filter { !it.ai }
        val aiMessages = messages.filter { it.ai }

        assertEquals(2, userMessages.size)
        assertEquals(2, aiMessages.size)
    }

    @Test
    fun `Message toString should contain field values`() {
        val message = Message(text = "Test message", ai = true)
        val stringRep = message.toString()

        assertTrue(stringRep.contains("Test message"))
        assertTrue(stringRep.contains("true"))
    }

    @Test
    fun `Message should handle whitespace text`() {
        val whitespaceMessage = Message(text = "   ", ai = false)

        assertEquals("   ", whitespaceMessage.text)
        assertTrue(whitespaceMessage.text.isNotEmpty())
        assertTrue(whitespaceMessage.text.isBlank())
    }

    @Test
    fun `Message should differentiate AI and user messages`() {
        val userMsg = Message(text = "What should I eat?", ai = false)
        val aiMsg = Message(text = "Here's a meal plan", ai = true)

        assertFalse(userMsg.ai)
        assertTrue(aiMsg.ai)
        assertNotEquals(userMsg.ai, aiMsg.ai)
    }

    @Test
    fun `Message with image placeholder text`() {
        val imageMessage = Message(text = "📷 Image attached", ai = false)

        assertEquals("📷 Image attached", imageMessage.text)
        assertTrue(imageMessage.text.contains("Image"))
        assertTrue(imageMessage.text.contains("📷"))
    }

    @Test
    fun `Message alternating conversation pattern`() {
        val conversation = listOf(
            Message("Hello!", false),
            Message("Hi! How can I help?", true),
            Message("I need workout advice", false),
            Message("I'd be happy to help!", true)
        )

        // Verify alternating pattern
        for (i in conversation.indices) {
            if (i % 2 == 0) {
                assertFalse(conversation[i].ai, "Index $i should be user message")
            } else {
                assertTrue(conversation[i].ai, "Index $i should be AI message")
            }
        }
    }

    @Test
    fun `Message filtering by sender type`() {
        val messages = listOf(
            Message("Q1", false),
            Message("A1", true),
            Message("Q2", false),
            Message("A2", true),
            Message("Q3", false)
        )

        val userCount = messages.count { !it.ai }
        val aiCount = messages.count { it.ai }

        assertEquals(3, userCount)
        assertEquals(2, aiCount)
    }

    @Test
    fun `Message data class component functions`() {
        val message = Message("Test", true)
        val (text, ai) = message

        assertEquals("Test", text)
        assertTrue(ai)
    }

    @Test
    fun `Message with newlines and special formatting`() {
        val formattedText = "Step 1:\n\t- Warm up\nStep 2:\n\t- Exercise"
        val message = Message(formattedText, true)

        assertTrue(message.text.contains("\n"))
        assertTrue(message.text.contains("\t"))
        assertEquals(formattedText, message.text)
    }

    @Test
    fun `Message conversation history size`() {
        val history = mutableListOf<Message>()

        repeat(10) { i ->
            history.add(Message("User message $i", false))
            history.add(Message("AI response $i", true))
        }

        assertEquals(20, history.size)
        assertEquals(10, history.count { it.ai })
        assertEquals(10, history.count { !it.ai })
    }

    @Test
    fun `Message handles Coach Rise responses`() {
        val coachMessage = Message(
            text = "Great progress today! Keep up the excellent work with your fitness journey.",
            ai = true
        )

        assertTrue(coachMessage.ai)
        assertTrue(coachMessage.text.contains("progress"))
    }
}
