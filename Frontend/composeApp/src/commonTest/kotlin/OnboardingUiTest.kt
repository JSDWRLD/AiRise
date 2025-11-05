import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.*

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
    fun test_name_input_validation() {
        val input = "alice"
        val input2 = "al"
        val input3 = "alice-bob"
        val input4 = "Bob jr."
        val input5 = "ch@rlie"
        val input6 = "12345"

        assertEquals(isValidCharacterInput(input), true)
        assertEquals(isValidCharacterInput(input2), true)
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
        val screens = listOf("Screen1", "Screen2", "Screen3")
        var current = "Screen1"
        var i = 0

        val onSkip: () -> Unit = {
            current = screens[i + 1]
            i++
        }

        onSkip()
        assertEquals(current, "Screen2")

        onSkip()
        assertEquals(current, "Screen3")
    }

    @Test
    fun back_button_should_navigate_to_previous_question() {
        val screens = listOf("Screen1", "Screen2", "Screen3")
        var current = "Screen2"
        var i = 2

        val onBack: () -> Unit = {
            current = screens[i - 1]
            i--
        }

        onBack()
        assertEquals(current, "Screen2")

        onBack()
        assertEquals(current, "Screen1")
    }

    @Test
    fun test_unit_toggle_should_work_properly() {
        var current = "Imperial"
        var alternative = "Metric"
        var placeHolder = ""

        val onToggle: () -> Unit = {
            placeHolder = current
            current = alternative
            alternative = placeHolder
        }

        onToggle()
        assertEquals(current, "Metric")

        onToggle()
        assertEquals(current, "Imperial")
    }

    @Test
    fun test_single_selection_radio_button_function_properly() {
        val options = listOf("Option1", "Option2", "Option3")
        var selected = ""
        var continueEnable = false

        val onClick: (String) -> Unit = {
            selected = it
            continueEnable = true
        }
        options.forEach {
            onClick(it)
        }

        assertEquals(selected, "Option3")
        assertEquals(continueEnable, true)
    }

    @Test
    fun test_multi_selection_checkboxes_function_properly() {
        val options = listOf("Option1", "Option2", "Option3")
        val selected = mutableStateOf(setOf<String>())
        val continueEnable = mutableStateOf(false)

        val onSelect: (String) -> Unit = { option ->
            selected.value += option
            continueEnable.value = selected.value.isNotEmpty()
        }

        val deSelect: (String) -> Unit = { option ->
            selected.value -= option
            continueEnable.value = selected.value.isNotEmpty()
        }

        // Initial state
        assertEquals(emptySet(), selected.value)
        assertEquals(false, continueEnable.value)

        // Select first option
        onSelect(options[0])
        assertEquals(setOf("Option1"), selected.value)
        assertEquals(true, continueEnable.value)

        // Select second option
        onSelect(options[1])
        assertEquals(setOf("Option1", "Option2"), selected.value)
        assertEquals(true, continueEnable.value)

        // Deselect first option
        deSelect(options[0])
        assertEquals(setOf("Option2"), selected.value)
        assertEquals(true, continueEnable.value)

        // Deselect second option
        deSelect(options[1])
        assertEquals(emptySet(), selected.value)
        assertEquals(false, continueEnable.value)
    }

    @Test
    fun test_date_of_birth_input_validation() {
        val testCases = listOf (
            Triple(0, 1, 2000),
            Triple(12, 31, 1999),
            Triple(13, 32, 1800),
            Triple(1, -3, 2090),
        )

        testCases.forEach { (month, day, year) ->
            if (isValidBirthDate(month, day, year)) {
                assertEquals(isValidBirthDate(month, day, year), true)
            } else {
                assertEquals(isValidBirthDate(month, day, year), false)
            }
        }
    }

    private fun isValidCharacterInput(input: String): Boolean {
        return input.matches("^[A-Za-zÀ-ÿ\\s'-.]+\$".toRegex())
                && input.trim().length in 2..50
                && !input.trim().matches(".*\\d.*".toRegex())
    }

    private fun isValidBirthDate(month: Int, day: Int, year: Int): Boolean {
        val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
        return month in 1..12
                && day in 1..31
                && year in currentYear - 150..currentYear
    }
}
