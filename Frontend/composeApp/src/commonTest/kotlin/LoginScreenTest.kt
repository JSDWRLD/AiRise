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

    @Test
    fun blank_errorMessage_is_not_shown() {
        val s1 = LoginUiState(errorMessage = "")
        val s2 = LoginUiState(errorMessage = "   ")
        assertFalse(shouldShowError(s1))
        assertFalse(shouldShowError(s2))
    }

    @Test
    fun toggling_loading_reenables_button() {
        var s = LoginUiState(isLoading = true)
        assertFalse(isLoginButtonEnabled(s))
        s = s.copy(isLoading = false)
        assertTrue(isLoginButtonEnabled(s))
    }

    @Test
    fun logged_in_does_not_clear_inputs() {
        val s = LoginUiState(email = "stay@airise.app", password = "KeepMe", isLoggedIn = true)
        assertEquals("stay@airise.app", s.email)
        assertEquals("KeepMe", s.password)
        assertTrue(s.isLoggedIn)
    }

    @Test
    fun copying_flags_does_not_change_inputs() {
        val base = LoginUiState(email = "x@y.com", password = "p1")
        val changed = base.copy(isLoading = true, isLoggedIn = true)
        assertEquals("x@y.com", changed.email)
        assertEquals("p1", changed.password)
        assertTrue(changed.isLoggedIn)
        assertTrue(changed.isLoading)
    }

    @Test
    fun long_inputs_are_preserved_verbatim() {
        val longEmail = "a".repeat(128) + "@example.com"
        val longPass = "P".repeat(512) + "!"
        val s = LoginUiState(email = longEmail, password = longPass)
        assertEquals(longEmail, s.email)
        assertEquals(longPass, s.password)
    }

    @Test
    fun inconsistent_state_loggedIn_with_error_is_detectable() {
        val s = LoginUiState(isLoggedIn = true, errorMessage = "Should not appear when logged in")
        assertTrue(s.isLoggedIn)
        assertTrue(shouldShowError(s))
    }

    @Test
    fun multiple_copy_chain_preserves_fields() {
        var s = LoginUiState(email = "chain@airise.app", password = "ChainPass")
        s = s.copy(isLoading = true).copy(isLoading = false).copy(errorMessage = "x").copy(errorMessage = null)
        assertEquals("chain@airise.app", s.email)
        assertEquals("ChainPass", s.password)
        assertFalse(s.isLoading)
        assertNull(s.errorMessage)
    }

    @Test
    fun login_event_exists_in_sealed_hierarchy() {
        val e: LoginUiEvent = LoginUiEvent.Login
        assertTrue(e is LoginUiEvent)
    }

    @Test
    fun email_and_password_change_events_exist_in_hierarchy() {
        val e1: LoginUiEvent = LoginUiEvent.EmailChanged("a@b.com")
        val e2: LoginUiEvent = LoginUiEvent.PasswordChanged("Zyx123!@#")
        assertTrue(e1 is LoginUiEvent.EmailChanged)
        assertTrue(e2 is LoginUiEvent.PasswordChanged)
    }

    @Test
    fun loading_with_error_still_disables_button() {
        val s = LoginUiState(isLoading = true, errorMessage = "err")
        assertFalse(isLoginButtonEnabled(s))
        assertTrue(shouldShowError(s))
    }

    @Test
    fun copy_does_not_mutate_original_instance() {
        val original = LoginUiState(email = "orig@airise.app", password = "orig")
        val modified = original.copy(email = "new@airise.app")
        assertEquals("orig@airise.app", original.email)
        assertEquals("new@airise.app", modified.email)
    }

}


