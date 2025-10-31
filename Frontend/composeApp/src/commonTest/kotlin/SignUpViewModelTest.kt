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
}
