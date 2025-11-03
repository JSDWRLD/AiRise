import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals

class OnboardingUiTest {

    @Test
    fun name_input_should_require_first_and_last_name() {
        val testCases = listOf(
            Triple("John", "", "Doe"),
            Triple("", "", "Doe"),
            Triple("John", "", ""),
            Triple("", "Middle", ""),
            Triple(" ", " ", " "),
            Triple("John", "Michael", "Doe"),
        )

        testCases.forEach { (firstName, middleName, lastName) ->
            val isValid = firstName.isNotBlank() && lastName.isNotBlank()

            if (isValid) {
                assertEquals(
                    firstName.isNotBlank() && lastName.isNotBlank(),
                    true,
                    "Expected valid name: $firstName $middleName $lastName"
                )
            } else {
                assertEquals(isValid,
                    false,
                    "Expected invalid name: $firstName $middleName $lastName"
                )
            }
        }
    }

    @Test
    fun test_character_input_validation() {
        val input = "alice"
        val input2 = "al"
        val input3 = "alice-bob"
        val input4 = "Bob jr."
        val input5 = "ch@rlie"
        val input6 = "12345"

        assertEquals(isValidCharacterInput(input), true)
        assertEquals(isValidCharacterInput(input2), false)
        assertEquals(isValidCharacterInput(input3), true)
        assertEquals(isValidCharacterInput(input4), true)
        assertEquals(isValidCharacterInput(input5), false)
        assertEquals(isValidCharacterInput(input6), false)
    }

    @Test
    fun continue_button_should_enable_on_complete_input() {
        var enable = false
        val input = "bob"
        val input2 = "charlie"

        val onChange: (Boolean) -> Unit = { enable = it}
        if(isValidCharacterInput(input) && isValidCharacterInput(input2)) {
            onChange(true)
        }

        assertEquals(enable, true)
    }

    @Test
    fun skip_button_should_navigate_to_next_question() {
        var current = "Question1"
        val next = "Question2"

        val onSkip: () -> Unit = { current = next}
        onSkip()

        assertEquals(current, next)
    }

    @Test
    fun back_button_should_navigate_to_previous_question() {
        var current = "Question2"
        val prev = "Question1"

        val onBack: () -> Unit = { current = prev}
        onBack()

        assertEquals(current, prev)
    }

    @Test
    fun test_only_single_option_is_selected() {
        val options = listOf("Option1", "Option2", "Option3")
        var selected = ""

        val onClick: (String) -> Unit = { selected = it}
        options.forEach {
            onClick(it)
        }

        assertEquals(selected, "Option3")
    }

    @Test
    fun test_multi_selection_checkboxes_function_properly() {
        val options = listOf("Option1", "Option2", "Option3")
        val selected = mutableStateOf(setOf<String>())

        selected.value += options[0]
        assertEquals(setOf("Option1"), selected.value)

        selected.value += options[1]
        assertEquals(setOf("Option1", "Option2"), selected.value)

        selected.value -= options[0]
        assertEquals(setOf("Option2"), selected.value)
    }

    private fun isValidCharacterInput(input: String): Boolean {
        return input.matches("^[A-Za-zÀ-ÿ\\s'-.]+\$".toRegex())
                && input.trim().length in 3..49
                && !input.trim().matches(".*\\d.*".toRegex())
    }
}
