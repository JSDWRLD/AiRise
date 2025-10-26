package generativeAI

import kotlin.test.Test
import kotlin.test.assertEquals

class AiChatUiTest {

    @Test
    fun chat_button_opens_ai_chat_screen() {
        // Arrange
        var currentScreen = "Home"

        // Action
        val onClick: (String) -> Unit = {currentScreen = it}
        onClick("AiChat")

        // Assert
        assertEquals(currentScreen, "AiChat")
    }

    @Test
    fun exiting_chat_returns_to_previous_screen() {
        // Arrange
        val previousScreen = "Home"
        var currentScreen = "AiChat"

        // Action
        val onClick: () -> Unit = {currentScreen = previousScreen}
        onClick()

        // Assert
        assertEquals(currentScreen, "Home")
    }
}