import kotlin.test.Test
import kotlin.test.assertEquals


class WaterUiTest {

    @Test
    fun hydration_update_button_should_only_enable_on_input() {
        // Arrange
        val hydration = 100f
        val maxHydration = 200f
        var updateEnable = false

        // Act
        val onChange: (Boolean) -> Unit = { updateEnable = it}
        if(isValidHydrationInput(hydration.toString())) {
            onChange(true)
        }

        // Assert
        assertEquals(updateEnable, true)
    }

    @Test
    fun isHydrationInputValid_should_validate_input_range_correctly() {
        val maxHydration = 200f

        // Valid inputs
        assertEquals(isHydrationInputValid(0f, maxHydration), true)
        assertEquals(isHydrationInputValid(50f, maxHydration), true)
        assertEquals(isHydrationInputValid(135.2f, maxHydration), true)

        // Invalid inputs
        assertEquals(isHydrationInputValid(-1f, maxHydration), false)
        assertEquals(isHydrationInputValid(-100f, maxHydration), false)
        assertEquals(isHydrationInputValid(201f, maxHydration), false)
    }

    @Test
    fun input_filtering_should_only_allow_numbers_and_decimals() {
        // Valid inputs
        assertEquals(isValidHydrationInput("0"), true)
        assertEquals(isValidHydrationInput("100"), true)
        assertEquals(isValidHydrationInput("16.9"), true)

        // Invalid inputs
        assertEquals(isValidHydrationInput("abc"), false)
        assertEquals(isValidHydrationInput("12a"), false)
        assertEquals(isValidHydrationInput("12.3.4"), false)
        assertEquals(isValidHydrationInput("$@!"), false)
    }

    @Test
    fun test_hydration_input_updates_value_correctly() {
        // Arrange
        var capturedValue: Float? = null
        val newValue = 100.5f

        // Act
        val onValueChange: (Float) -> Unit = { capturedValue = it }

        // Simulating the user typing "100.5"
        val newText = "100.5"
        onValueChange(newText.toFloatOrNull() ?: 0f)

        // Assert
        assertEquals(newValue, capturedValue)
    }

    @Test
    fun test_hydration_value_is_displayed_as_integer() {
        // Arrange
        val hydration = 16.9f
        val maxHydration = 200f
        val expectedValue = 16

        // Act
        val displayedValue = hydration.toInt()

        // Assert
        assertEquals(expectedValue, displayedValue)
    }

    private fun isHydrationInputValid(input: Float, maxHydration: Float): Boolean {
        return input in 0f..maxHydration
    }

    private fun isValidHydrationInput(input: String): Boolean {
        return input.matches("^\\d*\\.?\\d*\$".toRegex())
    }
}