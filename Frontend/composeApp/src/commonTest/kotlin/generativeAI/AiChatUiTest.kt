package generativeAI

import androidx.compose.runtime.mutableStateOf
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

    @Test
    fun send_query_when_text_box_is_not_empty() {
        var sendButton = false

        val onSend: (String) -> Unit = {
            if (it.isNotEmpty()) sendButton = true
        }

        onSend("")
        assertEquals(sendButton, false)

        onSend("hello")
        assertEquals(sendButton, true)
    }

    @Test
    fun suggested_question_chip_should_not_activate_if_used() {
        var isSelected = false
        var usageCount = 0
        val maxUses = 1

        val onSelect: () -> Unit = {
            if (usageCount < maxUses) {
                isSelected = true
                usageCount++
            }
        }

        onSelect()
        assertEquals(isSelected, true)
        assertEquals(1, usageCount)

        onSelect()
        assertEquals(isSelected, true)
        assertEquals(1, usageCount)
    }

    @Test
    fun addition_icon_should_bring_up_photo_upload_options() {
        var showPhotoUploadOptions = false

        val onShowPhotos: () -> Unit = {
            showPhotoUploadOptions = true
        }

        val onClose: () -> Unit = {
            showPhotoUploadOptions = false
        }

        onShowPhotos()
        assertEquals(showPhotoUploadOptions, true)
    }
}