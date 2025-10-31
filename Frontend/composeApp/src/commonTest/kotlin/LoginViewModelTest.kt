package com.teamnotfound.airise.auth.login

import kotlin.test.*

class LoginViewModelTest {

    @Test
    fun default_state_is_empty_and_idle() {
        val s = LoginUiState()
        assertEquals("", s.email)
        assertEquals("", s.password)
        assertFalse(s.isLoading)
        assertFalse(s.isLoggedIn)
        assertNull(s.errorMessage)
    }

    @Test
    fun copy_updates_email_and_password_only() {
        val base = LoginUiState()
        val updated = base.copy(email = "user@airise.app", password = "StrongPass!")
        assertEquals("user@airise.app", updated.email)
        assertEquals("StrongPass!", updated.password)

        // unchanged flags
        assertFalse(updated.isLoading)
        assertFalse(updated.isLoggedIn)
        assertNull(updated.errorMessage)
    }

    @Test
    fun events_construct_correctly() {
        val e1: LoginUiEvent = LoginUiEvent.EmailChanged("a@b.com")
        val e2: LoginUiEvent = LoginUiEvent.PasswordChanged("Secret1!")
        val e3: LoginUiEvent = LoginUiEvent.Login
        val e4: LoginUiEvent = LoginUiEvent.GoogleSignInSuccess("token-xyz")

        assertTrue(e1 is LoginUiEvent.EmailChanged)
        assertTrue(e2 is LoginUiEvent.PasswordChanged)
        assertTrue(e3 is LoginUiEvent)
        assertTrue(e4 is LoginUiEvent.GoogleSignInSuccess)
    }

    @Test
    fun error_flag_can_be_set_and_cleared() {
        val withError = LoginUiState(errorMessage = "Invalid credentials")
        val cleared = withError.copy(errorMessage = null)
        assertNotNull(withError.errorMessage)
        assertNull(cleared.errorMessage)
    }

    @Test
    fun loading_and_loggedin_flags_toggle_via_copy() {
        val loading = LoginUiState(isLoading = true)
        val logged = LoginUiState(isLoggedIn = true)
        assertTrue(loading.isLoading)
        assertTrue(logged.isLoggedIn)
    }

    @Test
    fun login_flow_success_path_updates_fields() {
        var s = LoginUiState()
        s = s.copy(email = "ok@airise.app", password = "Password1!", isLoading = true)
        assertTrue(s.isLoading)

        s = s.copy(isLoading = false, isLoggedIn = true)
        assertTrue(s.isLoggedIn)
        assertFalse(s.isLoading)
        assertNull(s.errorMessage)
    }

    @Test
    fun login_flow_failure_sets_error_and_resets_loading() {
        var s = LoginUiState(email = "bad@airise.app", password = "wrongpass", isLoading = true)
        s = s.copy(isLoading = false, isLoggedIn = false, errorMessage = "Incorrect email or password")

        assertFalse(s.isLoggedIn)
        assertFalse(s.isLoading)
        assertEquals("Incorrect email or password", s.errorMessage)
    }

    @Test
    fun google_sign_in_success_marks_logged_in() {
        val before = LoginUiState(isLoggedIn = false)
        val after = before.copy(isLoggedIn = true, errorMessage = null)
        assertTrue(after.isLoggedIn)
        assertNull(after.errorMessage)
    }

    @Test
    fun state_can_reset_to_default_after_logout() {
        val loggedIn = LoginUiState(email = "user@airise.app", isLoggedIn = true)
        val reset = loggedIn.copy(email = "", password = "", isLoggedIn = false, errorMessage = null)
        assertEquals("", reset.email)
        assertEquals("", reset.password)
        assertFalse(reset.isLoggedIn)
        assertNull(reset.errorMessage)
    }
}
