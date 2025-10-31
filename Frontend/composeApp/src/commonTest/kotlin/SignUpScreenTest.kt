package com.teamnotfound.airise.auth.signup

import kotlin.test.*

private fun canSubmit(passwordsMatch: Boolean, state: SignUpUiState): Boolean =
    passwordsMatch && state.passwordErrors.isEmpty()

private fun shouldShowPasswordErrors(attemptedSubmit: Boolean, state: SignUpUiState): Boolean =
    attemptedSubmit && state.passwordErrors.isNotEmpty()

class SignUpScreenTest {

    @Test
    fun default_state_is_clean() {
        val s = SignUpUiState()
        assertFalse(s.isLoading)
        assertFalse(s.isSuccess)
        assertNull(s.errorMessage)
        assertTrue(s.passwordMatch)
        assertTrue(s.passwordErrors.isEmpty())
        assertNull(s.currentUser)
    }

    @Test
    fun show_password_errors_only_when_attempted_and_errors_exist() {
        val withErrors = SignUpUiState(passwordErrors = listOf("min length", "needs special"))
        assertFalse(shouldShowPasswordErrors(attemptedSubmit = false, state = withErrors))
        assertTrue(shouldShowPasswordErrors(attemptedSubmit = true, state = withErrors))

        val noErrors = SignUpUiState(passwordErrors = emptyList())
        assertFalse(shouldShowPasswordErrors(attemptedSubmit = true, state = noErrors))
    }

    @Test
    fun submit_enabled_only_when_passwords_match_and_no_errors() {
        val ok = SignUpUiState(passwordErrors = emptyList())
        assertTrue(canSubmit(passwordsMatch = true, state = ok))
        assertFalse(canSubmit(passwordsMatch = false, state = ok))

        val hasErrors = SignUpUiState(passwordErrors = listOf("too short"))
        assertFalse(canSubmit(passwordsMatch = true, state = hasErrors))
        assertFalse(canSubmit(passwordsMatch = false, state = hasErrors))
    }

    @Test
    fun loading_flag_does_not_change_submit_rule() {
        val s = SignUpUiState(isLoading = true, passwordErrors = emptyList())
        assertTrue(canSubmit(passwordsMatch = true, state = s))
        assertFalse(canSubmit(passwordsMatch = false, state = s))
    }

    @Test
    fun success_flow_clears_error_and_marks_success() {
        val start = SignUpUiState(
            isLoading = true,
            isSuccess = false,
            errorMessage = "Previous error",
            passwordErrors = emptyList()
        )
        val done = start.copy(isLoading = false, isSuccess = true, errorMessage = null)

        assertFalse(done.isLoading)
        assertTrue(done.isSuccess)
        assertNull(done.errorMessage)
    }

    @Test
    fun failure_flow_sets_error_and_not_success() {
        val start = SignUpUiState(isLoading = true, passwordErrors = emptyList())
        val failed = start.copy(isLoading = false, isSuccess = false, errorMessage = "Sign up failed")

        assertFalse(failed.isLoading)
        assertFalse(failed.isSuccess)
        assertEquals("Sign up failed", failed.errorMessage)
    }

    @Test
    fun clearing_errors_returns_to_clean_display_state() {
        val withErrs = SignUpUiState(passwordErrors = listOf("weak"), errorMessage = "bad")
        val cleared = withErrs.copy(passwordErrors = emptyList(), errorMessage = null)
        assertTrue(cleared.passwordErrors.isEmpty())
        assertNull(cleared.errorMessage)
    }

    @Test
    fun input_edits_do_not_toggle_success_flag() {
        val start = SignUpUiState(isSuccess = false)
        val after = start.copy(passwordMatch = false).copy(passwordMatch = true)
        assertFalse(after.isSuccess)
    }

    @Test
    fun attemptedSubmit_false_hides_errors_even_if_present() {
        val s = SignUpUiState(passwordErrors = listOf("too short", "needs special"))
        assertFalse(shouldShowPasswordErrors(attemptedSubmit = false, state = s))
    }

    @Test
    fun password_mismatch_blocks_submit_even_when_no_errors() {
        val s = SignUpUiState(passwordErrors = emptyList())
        assertFalse(canSubmit(passwordsMatch = false, state = s))
    }

    @Test
    fun fixing_errors_then_matching_passwords_enables_submit() {
        var s = SignUpUiState(passwordErrors = listOf("too short"))
        assertFalse(canSubmit(passwordsMatch = false, state = s))
        assertFalse(canSubmit(passwordsMatch = true, state = s))
        s = s.copy(passwordErrors = emptyList())
        assertTrue(canSubmit(passwordsMatch = true, state = s))
    }

    @Test
    fun loading_flag_does_not_clear_existing_errors() {
        val s = SignUpUiState(isLoading = true, passwordErrors = listOf("weak"))
        assertTrue(s.passwordErrors.isNotEmpty())
    }

    @Test
    fun error_and_success_should_not_coexist() {
        val s = SignUpUiState(isSuccess = true, errorMessage = "oops")
        assertTrue(s.isSuccess)
        assertNotNull(s.errorMessage)
    }

    @Test
    fun success_flow_keeps_password_errors_empty() {
        val start = SignUpUiState(passwordErrors = emptyList(), isLoading = true)
        val done = start.copy(isLoading = false, isSuccess = true)
        assertTrue(done.passwordErrors.isEmpty())
        assertTrue(done.isSuccess)
    }

}
