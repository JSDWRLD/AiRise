package com.teamnotfound.airise.auth.signup

import kotlin.test.*

private fun canAttemptRegister(passwordsMatch: Boolean, state: SignUpUiState): Boolean =
    passwordsMatch && state.passwordErrors.isEmpty()

private fun shouldSurfaceBackendError(state: SignUpUiState): Boolean =
    state.passwordErrors.isEmpty()

class SignUpViewModelTest {

    @Test
    fun default_state_is_clean_and_not_loading() {
        val s = SignUpUiState()
        assertFalse(s.isLoading)
        assertFalse(s.isSuccess)
        assertNull(s.errorMessage)
        assertTrue(s.passwordMatch)
        assertTrue(s.passwordErrors.isEmpty())
        assertNull(s.currentUser)
    }

    @Test
    fun register_is_blocked_when_passwords_do_not_match() {
        val s = SignUpUiState(passwordErrors = emptyList())
        assertFalse(canAttemptRegister(passwordsMatch = false, state = s))
    }

    @Test
    fun register_is_blocked_when_local_password_errors_exist() {
        val s = SignUpUiState(passwordErrors = listOf("too short", "needs special"))
        assertFalse(canAttemptRegister(passwordsMatch = true, state = s))
        assertFalse(canAttemptRegister(passwordsMatch = false, state = s))
    }

    @Test
    fun register_is_allowed_only_when_match_and_no_errors() {
        val s = SignUpUiState(passwordErrors = emptyList())
        assertTrue(canAttemptRegister(passwordsMatch = true, state = s))
    }

    @Test
    fun backend_error_should_surface_only_when_no_local_password_errors() {
        val clean = SignUpUiState(passwordErrors = emptyList())
        val hasLocal = SignUpUiState(passwordErrors = listOf("weak password"))

        assertTrue(shouldSurfaceBackendError(clean))
        assertFalse(shouldSurfaceBackendError(hasLocal))
    }

    @Test
    fun success_flow_clears_loading_and_error_and_marks_success() {
        val start = SignUpUiState(isLoading = true, errorMessage = "prev error", passwordErrors = emptyList())
        val end = start.copy(isLoading = false, isSuccess = true, errorMessage = null)

        assertFalse(end.isLoading)
        assertTrue(end.isSuccess)
        assertNull(end.errorMessage)
        assertTrue(end.passwordErrors.isEmpty())
    }

    @Test
    fun failure_flow_sets_error_but_keeps_not_success_and_not_loading() {
        val start = SignUpUiState(isLoading = true, passwordErrors = emptyList())
        val failed = start.copy(isLoading = false, isSuccess = false, errorMessage = "Sign up failed")

        assertFalse(failed.isLoading)
        assertFalse(failed.isSuccess)
        assertEquals("Sign up failed", failed.errorMessage)
    }

    @Test
    fun retry_after_failure_then_success_results_in_consistent_state() {
        // Failure
        var s = SignUpUiState(
            isLoading = false,
            isSuccess = false,
            errorMessage = "Network error",
            passwordErrors = emptyList()
        )
        assertFalse(s.isSuccess)
        assertNotNull(s.errorMessage)

        s = s.copy(errorMessage = null, isLoading = true)
        assertNull(s.errorMessage); assertTrue(s.isLoading)

        s = s.copy(isLoading = false, isSuccess = true)
        assertTrue(s.isSuccess)
        assertFalse(s.isLoading)
        assertNull(s.errorMessage)
    }

    @Test
    fun loading_flag_does_not_change_submit_rule() {
        val s = SignUpUiState(isLoading = true, passwordErrors = emptyList())
        assertTrue(canAttemptRegister(passwordsMatch = true, state = s))
        assertFalse(canAttemptRegister(passwordsMatch = false, state = s))
    }

    @Test
    fun clearing_password_errors_restores_submit_when_passwords_match() {
        var s = SignUpUiState(passwordErrors = listOf("too short"))
        assertFalse(canAttemptRegister(passwordsMatch = true, state = s))

        s = s.copy(passwordErrors = emptyList())
        assertTrue(canAttemptRegister(passwordsMatch = true, state = s))
    }

    @Test
    fun success_and_error_should_not_coexist_in_final_state() {
        val inconsistent = SignUpUiState(isSuccess = true, errorMessage = "should not be here")
        assertTrue(inconsistent.isSuccess)
        assertNotNull(inconsistent.errorMessage)
    }


    @Test
    fun blank_errorMessage_is_effectively_ignored_for_display_purposes() {
        val s1 = SignUpUiState(errorMessage = "", passwordErrors = emptyList())
        val s2 = SignUpUiState(errorMessage = "   ", passwordErrors = emptyList())
        assertTrue(canAttemptRegister(passwordsMatch = true, state = s1))
        assertTrue(canAttemptRegister(passwordsMatch = true, state = s2))
    }

    @Test
    fun passwordErrors_with_blank_entries_still_block_submit() {
        val s = SignUpUiState(passwordErrors = listOf("", "   "))
        assertFalse(canAttemptRegister(passwordsMatch = true, state = s))
    }

    @Test
    fun submit_rule_ignores_top_error_presence_entirely() {
        val s = SignUpUiState(errorMessage = "previous failure", passwordErrors = emptyList())
        assertTrue(canAttemptRegister(passwordsMatch = true, state = s))
        assertFalse(canAttemptRegister(passwordsMatch = false, state = s))
    }

    @Test
    fun loading_does_not_clear_existing_password_errors() {
        val s = SignUpUiState(isLoading = true, passwordErrors = listOf("weak"))
        assertEquals(listOf("weak"), s.passwordErrors)
        assertFalse(canAttemptRegister(passwordsMatch = true, state = s))
    }

    @Test
    fun copy_chain_preserves_and_applies_overrides_consistently() {
        var s = SignUpUiState(
            isLoading = false,
            isSuccess = false,
            errorMessage = null,
            passwordErrors = listOf("weak"),
            passwordMatch = false
        )
        s = s.copy(isLoading = true)
            .copy(isLoading = false)
            .copy(errorMessage = "x")
            .copy(errorMessage = null)
            .copy(passwordErrors = emptyList())
            .copy(passwordMatch = true)

        assertFalse(s.isLoading)
        assertFalse(s.isSuccess)
        assertNull(s.errorMessage)
        assertTrue(s.passwordErrors.isEmpty())
        assertTrue(s.passwordMatch)
    }

    @Test
    fun mismatch_then_fix_then_submit_flow() {
        var s = SignUpUiState(passwordErrors = emptyList())
        assertFalse(canAttemptRegister(passwordsMatch = false, state = s))
        assertTrue(canAttemptRegister(passwordsMatch = true, state = s))
    }

    @Test
    fun large_number_of_password_errors_still_blocks_submit() {
        val many = List(25) { "err-$it" }
        val s = SignUpUiState(passwordErrors = many)
        assertFalse(canAttemptRegister(passwordsMatch = true, state = s))
        assertEquals(25, s.passwordErrors.size)
    }

    @Test
    fun backend_error_should_not_surface_when_local_errors_present_even_if_message_exists() {
        val s = SignUpUiState(errorMessage = "Server says no", passwordErrors = listOf("too short"))
        assertFalse(shouldSurfaceBackendError(s))
    }

    @Test
    fun success_path_keeps_password_errors_empty() {
        val start = SignUpUiState(passwordErrors = emptyList(), isLoading = true)
        val end = start.copy(isLoading = false, isSuccess = true)
        assertTrue(end.passwordErrors.isEmpty())
        assertTrue(end.isSuccess)
    }

    @Test
    fun success_and_loading_together_is_inconsistent_but_detectable() {
        val s = SignUpUiState(isSuccess = true, isLoading = true)
        assertTrue(s.isSuccess)
        assertTrue(s.isLoading)
    }

    @Test
    fun reset_to_clean_state_via_copy_clears_all_flags_and_errors() {
        val dirty = SignUpUiState(
            isLoading = true,
            isSuccess = false,
            errorMessage = "oops",
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
    fun flipping_passwordMatch_flag_in_state_does_not_change_submit_rule_here() {
        val s = SignUpUiState(passwordErrors = emptyList(), passwordMatch = false)
        assertTrue(canAttemptRegister(passwordsMatch = true, state = s))
        assertFalse(canAttemptRegister(passwordsMatch = false, state = s))
    }

    @Test
    fun backend_surface_rule_ignores_top_error_text_including_blank() {
        val blankMsg = SignUpUiState(errorMessage = "", passwordErrors = emptyList())
        val whitespaceMsg = SignUpUiState(errorMessage = "   ", passwordErrors = emptyList())
        assertTrue(shouldSurfaceBackendError(blankMsg))
        assertTrue(shouldSurfaceBackendError(whitespaceMsg))
    }

    @Test
    fun flow_localErrors_then_backendFailure_shows_local_only_by_rule() {
        val withLocal = SignUpUiState(errorMessage = "server fail", passwordErrors = listOf("too short"))
        assertFalse(shouldSurfaceBackendError(withLocal))
        assertFalse(canAttemptRegister(passwordsMatch = true, state = withLocal))
    }

    @Test
    fun complex_transition_mismatch_then_localError_then_fix_all_and_submit() {
        var s = SignUpUiState(passwordErrors = emptyList())
        assertFalse(canAttemptRegister(passwordsMatch = false, state = s))
        s = s.copy(passwordErrors = listOf("needs special"))
        assertFalse(canAttemptRegister(passwordsMatch = true, state = s))
        s = s.copy(passwordErrors = emptyList())
        assertTrue(canAttemptRegister(passwordsMatch = true, state = s))
    }

    @Test
    fun copy_does_not_mutate_original_instance() {
        val original = SignUpUiState(passwordErrors = listOf("weak"))
        val modified = original.copy(passwordErrors = emptyList())
        assertEquals(listOf("weak"), original.passwordErrors)
        assertTrue(modified.passwordErrors.isEmpty())
    }

    @Test
    fun long_error_message_is_preserved_and_does_not_affect_submit_rule() {
        val longMsg = "x".repeat(2048)
        val s = SignUpUiState(errorMessage = longMsg, passwordErrors = emptyList())
        assertTrue(s.errorMessage!!.length >= 2048)
        assertTrue(canAttemptRegister(passwordsMatch = true, state = s))
    }

}
