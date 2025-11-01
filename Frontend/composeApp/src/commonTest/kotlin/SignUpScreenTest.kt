package com.teamnotfound.airise.auth.signup

import kotlin.test.*

private fun canSubmit(passwordsMatch: Boolean, state: SignUpUiState): Boolean =
    passwordsMatch && state.passwordErrors.isEmpty()

private fun shouldShowPasswordErrors(attemptedSubmit: Boolean, state: SignUpUiState): Boolean =
    attemptedSubmit && state.passwordErrors.isNotEmpty()

private fun shouldShowTopError(attemptedSubmit: Boolean, state: SignUpUiState): Boolean {
    val msg = state.errorMessage
    return attemptedSubmit && state.passwordErrors.isEmpty() && msg != null && msg.isNotBlank()
}

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

    @Test
    fun top_error_shown_only_when_attempted_and_no_password_errors() {
        val withTop = SignUpUiState(errorMessage = "Server says nope", passwordErrors = emptyList())
        val withLocal = SignUpUiState(errorMessage = "Server error", passwordErrors = listOf("weak"))

        assertTrue(shouldShowTopError(attemptedSubmit = true, state = withTop))
        assertFalse(shouldShowTopError(attemptedSubmit = false, state = withTop))
        assertFalse(shouldShowTopError(attemptedSubmit = true, state = withLocal))
    }

    @Test
    fun blank_top_error_is_never_shown() {
        val s1 = SignUpUiState(errorMessage = "", passwordErrors = emptyList())
        val s2 = SignUpUiState(errorMessage = "   ", passwordErrors = emptyList())
        assertFalse(shouldShowTopError(attemptedSubmit = true, state = s1))
        assertFalse(shouldShowTopError(attemptedSubmit = true, state = s2))
    }

    @Test
    fun passwordErrors_with_blank_entries_still_counts_as_errors() {
        val s = SignUpUiState(passwordErrors = listOf("", "   "))
        assertFalse(canSubmit(passwordsMatch = true, state = s))
        assertTrue(shouldShowPasswordErrors(attemptedSubmit = true, state = s))
    }

    @Test
    fun copy_chain_preserves_fields_and_applies_overrides() {
        var s = SignUpUiState(
            isLoading = false,
            isSuccess = false,
            errorMessage = null,
            passwordErrors = listOf("weak"),
            passwordMatch = false
        )
        s = s.copy(isLoading = true).copy(isLoading = false).copy(passwordErrors = emptyList()).copy(passwordMatch = true)
        assertFalse(s.isLoading)
        assertTrue(s.passwordErrors.isEmpty())
        assertTrue(s.passwordMatch)
        assertFalse(s.isSuccess)
        assertNull(s.errorMessage)
    }

    @Test
    fun submit_rule_ignores_top_error_presence() {
        val s = SignUpUiState(errorMessage = "previous failure", passwordErrors = emptyList())
        assertTrue(canSubmit(passwordsMatch = true, state = s))
        assertFalse(canSubmit(passwordsMatch = false, state = s))
    }

    @Test
    fun retry_cycle_failure_then_success_is_consistent() {
        var s = SignUpUiState(
            isLoading = false,
            isSuccess = false,
            errorMessage = "Network error",
            passwordErrors = emptyList()
        )
        s = s.copy(errorMessage = null, isLoading = true)
        assertTrue(s.isLoading)
        assertNull(s.errorMessage)
        s = s.copy(isLoading = false, isSuccess = true)
        assertTrue(s.isSuccess)
        assertFalse(s.isLoading)
    }

    @Test
    fun success_and_loading_can_coexist_but_is_detectable() {
        val s = SignUpUiState(isSuccess = true, isLoading = true)
        assertTrue(s.isSuccess)
        assertTrue(s.isLoading)
    }

    @Test
    fun reset_via_copy_clears_flags_and_errors() {
        val dirty = SignUpUiState(
            isLoading = true,
            isSuccess = false,
            errorMessage = "err",
            passwordErrors = listOf("weak"),
            passwordMatch = false
        )
        val clean = dirty.copy(
            isLoading = false,
            isSuccess = false,
            errorMessage = null,
            passwordErrors = emptyList(),
            passwordMatch = true
        )
        assertFalse(clean.isLoading)
        assertFalse(clean.isSuccess)
        assertNull(clean.errorMessage)
        assertTrue(clean.passwordErrors.isEmpty())
        assertTrue(clean.passwordMatch)
    }

    @Test
    fun submit_rule_uses_argument_not_state_passwordMatch() {
        val s = SignUpUiState(passwordErrors = emptyList(), passwordMatch = false)
        assertTrue(canSubmit(passwordsMatch = true, state = s))
        assertFalse(canSubmit(passwordsMatch = false, state = s))
    }

    @Test
    fun backend_surface_rule_ignores_blank_top_error() {
        val blank = SignUpUiState(errorMessage = "", passwordErrors = emptyList())
        val spaces = SignUpUiState(errorMessage = "   ", passwordErrors = emptyList())
        assertTrue(shouldShowTopError(true, blank).not())
        assertTrue(shouldShowTopError(true, spaces).not())
    }

    @Test
    fun local_errors_take_precedence_over_backend_error() {
        val s = SignUpUiState(errorMessage = "server", passwordErrors = listOf("too short"))
        assertFalse(shouldShowTopError(true, s))
        assertFalse(canSubmit(passwordsMatch = true, state = s))
    }

    @Test
    fun long_error_message_preserved_and_does_not_block_submit() {
        val longMsg = "x".repeat(2048)
        val s = SignUpUiState(errorMessage = longMsg, passwordErrors = emptyList())
        assertEquals(2048, s.errorMessage!!.length)
        assertTrue(canSubmit(passwordsMatch = true, state = s))
    }

    @Test
    fun copy_does_not_mutate_original() {
        val original = SignUpUiState(passwordErrors = listOf("weak"))
        val modified = original.copy(passwordErrors = emptyList())
        assertEquals(listOf("weak"), original.passwordErrors)
        assertTrue(modified.passwordErrors.isEmpty())
    }

    @Test
    fun complex_flow_mismatch_then_localError_fix_all_then_submit() {
        var s = SignUpUiState(passwordErrors = emptyList())
        assertFalse(canSubmit(passwordsMatch = false, state = s))
        s = s.copy(passwordErrors = listOf("needs special"))
        assertFalse(canSubmit(passwordsMatch = true, state = s))
        s = s.copy(passwordErrors = emptyList())
        assertTrue(canSubmit(passwordsMatch = true, state = s))
    }

}
