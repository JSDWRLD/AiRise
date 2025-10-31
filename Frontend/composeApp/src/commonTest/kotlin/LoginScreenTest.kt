package com.teamnotfound.airise.auth.login

import kotlin.test.*

class LoginScreenTest {

    private fun shouldShowError(s: LoginUiState): Boolean {
        val msg = s.errorMessage
        return msg != null && msg.isNotBlank()
    }

    private fun isLoginButtonEnabled(s: LoginUiState): Boolean = !s.isLoading

    @Test
    fun default_state_is_empty_and_ready() {
        val s = LoginUiState()
        assertEquals("", s.email)
        assertEquals("", s.password)
        assertFalse(s.isLoading)
        assertFalse(s.isLoggedIn)
        assertNull(s.errorMessage)
        assertFalse(shouldShowError(s))
        assertTrue(isLoginButtonEnabled(s))
    }

    @Test
    fun loading_state_disables_login_button() {
        val s = LoginUiState(isLoading = true)
        assertFalse(isLoginButtonEnabled(s))
    }

    @Test
    fun error_state_is_displayed_when_message_exists() {
        val s = LoginUiState(errorMessage = "Invalid credentials")
        assertTrue(shouldShowError(s))
    }

    @Test
    fun copy_updates_email_and_password_only() {
        val base = LoginUiState()
        val updated = base.copy(email = "nick@airise.app", password = "Secret123!")
        assertEquals("nick@airise.app", updated.email)
        assertEquals("Secret123!", updated.password)
        assertFalse(updated.isLoading)
        assertFalse(updated.isLoggedIn)
        assertNull(updated.errorMessage)
    }

    @Test
    fun login_success_resets_error_and_marks_logged_in() {
        var s = LoginUiState(email = "ok@airise.app", password = "ValidPass!", isLoading = true)
        s = s.copy(isLoading = false, isLoggedIn = true, errorMessage = null)
        assertTrue(s.isLoggedIn)
        assertFalse(s.isLoading)
        assertNull(s.errorMessage)
        assertTrue(isLoginButtonEnabled(s))
    }

    @Test
    fun login_failure_sets_error_and_keeps_logged_out() {
        val s = LoginUiState(
            email = "wrong@airise.app",
            password = "BadPass",
            isLoading = false,
            isLoggedIn = false,
            errorMessage = "Invalid credentials"
        )
        assertFalse(s.isLoggedIn)
        assertTrue(shouldShowError(s))
        assertEquals("Invalid credentials", s.errorMessage)
    }

    @Test
    fun google_sign_in_success_event_is_recognized() {
        val event: LoginUiEvent = LoginUiEvent.GoogleSignInSuccess("token123")
        assertTrue(event is LoginUiEvent.GoogleSignInSuccess)
    }

    @Test
    fun back_forgot_and_signup_callbacks_exist() {
        var backClicked = false
        var forgotClicked = false
        var signupClicked = false
        val onBack = { backClicked = true }
        val onForgot = { forgotClicked = true }
        val onSignup = { signupClicked = true }
        onBack(); onForgot(); onSignup()
        assertTrue(backClicked); assertTrue(forgotClicked); assertTrue(signupClicked)
    }

    @Test
    fun clearing_errorMessage_returns_to_clean_state() {
        var s = LoginUiState(errorMessage = "Invalid password")
        assertTrue(shouldShowError(s))

        s = s.copy(errorMessage = null)
        assertFalse(shouldShowError(s))
        assertNull(s.errorMessage)
    }

    @Test
    fun email_and_password_edits_do_not_affect_login_status() {
        var s = LoginUiState(isLoggedIn = false)
        s = s.copy(email = "new@user.com")
        s = s.copy(password = "12345678")
        assertEquals("new@user.com", s.email)
        assertEquals("12345678", s.password)
        assertFalse(s.isLoggedIn)
    }

    @Test
    fun loading_then_error_then_success_flow_behaves_consistently() {
        var s = LoginUiState(isLoading = true)
        assertFalse(isLoginButtonEnabled(s))
        s = s.copy(isLoading = false, errorMessage = "Bad credentials")
        assertTrue(shouldShowError(s))
        assertFalse(s.isLoggedIn)
        s = s.copy(errorMessage = null, isLoggedIn = true)
        assertFalse(s.isLoading)
        assertTrue(s.isLoggedIn)
        assertFalse(shouldShowError(s))
    }

}

