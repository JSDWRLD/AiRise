import kotlin.test.Test
import kotlin.test.assertEquals


class WaterUiTest {

    @Test
    fun quick_add_buttons_should_add_water_correctly() {
        // Arrange
        val hydration = 100.0
        val expectedValue = 108.0
        val expectedValue2 = 116.0

        // Act
        val newHydration = hydration + 8.0
        val newHydration2 = hydration + 16.0

        // Assert
        assertEquals(expectedValue, newHydration)
        assertEquals(expectedValue2, newHydration2)
    }

    @Test
    fun reset_button_should_enable_on_hydration_greater_than_zero() {
        // Arrange
        val hydration = 100.0
        var addEnable = false

        // Act
        val onChange: (Boolean) -> Unit = { addEnable = it}
        if(hydration > 0.0) {
            onChange(true)
        }

        // Assert
        assertEquals(addEnable, true)
    }

    @Test
    fun custom_add_button_should_only_enable_on_valid_input() {
        // Arrange
        val hydration = 100.0
        var addEnable = false

        // Act
        val onChange: (Boolean) -> Unit = { addEnable = it}
        if(isValidHydrationInput(hydration.toString())) {
            onChange(true)
        }

        // Assert
        assertEquals(addEnable, true)
    }

    @Test
    fun isHydrationInputValid_should_validate_input_range_correctly() {
        val maxHydration = 200.0

        // Valid inputs
        assertEquals(isHydrationInputValid(50.0, maxHydration), true)
        assertEquals(isHydrationInputValid(135.2, maxHydration), true)

        // Invalid inputs
        assertEquals(isHydrationInputValid(-1.0, maxHydration), false)
        assertEquals(isHydrationInputValid(0.0, maxHydration), false)
        assertEquals(isHydrationInputValid(300.1, maxHydration), false)
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
        val expectedValue = 16

        // Act
        val displayedValue = hydration.toInt()

        // Assert
        assertEquals(expectedValue, displayedValue)
    }

    private fun isHydrationInputValid(input: Double, maxHydration: Double): Boolean {
        return input > 0 && input <= maxHydration
    }

    private fun isValidHydrationInput(input: String): Boolean {
        return input.matches("^\\d*\\.?\\d*\$".toRegex())
    }
}