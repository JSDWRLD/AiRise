import kotlin.test.Test
import kotlin.test.assertEquals


class WaterUiTest {

    @Test
    fun hydration_update_button_should_only_enable_on_input() {
        // Arrange
        val hydration = 100.0
        val maxHydration = 200.0
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
        val maxHydration = 200.0

        // Valid inputs
        assertEquals(isHydrationInputValid(0.0, maxHydration), true)
        assertEquals(isHydrationInputValid(50.0, maxHydration), true)
        assertEquals(isHydrationInputValid(135.2, maxHydration), true)

        // Invalid inputs
        assertEquals(isHydrationInputValid(-1.0, maxHydration), false)
        assertEquals(isHydrationInputValid(-100.0, maxHydration), false)
        assertEquals(isHydrationInputValid(201.0, maxHydration), false)
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
        var capturedValue: Double? = null
        val newValue = 100.5

        // Act
        val onValueChange: (Double) -> Unit = { capturedValue = it }

        // Simulating the user typing "100.5"
        val newText = "100.5"
        onValueChange(newText.toDoubleOrNull() ?: 0.0)

        // Assert
        assertEquals(newValue, capturedValue)
    }

    @Test
    fun test_hydration_value_is_displayed_as_integer() {
        // Arrange
        val hydration = 16.9
        val maxHydration = 200.0
        val expectedValue = 16

        // Act
        val displayedValue = hydration.toInt()

        // Assert
        assertEquals(expectedValue, displayedValue)
    }

    private fun isHydrationInputValid(input: Double, maxHydration: Double): Boolean {
        return input in 0.0..maxHydration
    }

    private fun isValidHydrationInput(input: String): Boolean {
        return input.matches("^\\d*\\.?\\d*\$".toRegex())
    }
}