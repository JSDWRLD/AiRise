import kotlin.test.Test
import kotlin.test.assertEquals


class WaterUiTest {

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

    private fun isHydrationInputValid(input: Float, maxHydration: Float): Boolean {
        return input in 0f..maxHydration
    }

    private fun isValidHydrationInput(input: String): Boolean {
        return input.matches("^\\d*\\.?\\d*\$".toRegex())
    }
}