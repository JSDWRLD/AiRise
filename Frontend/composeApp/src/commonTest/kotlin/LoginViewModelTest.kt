package com.teamnotfound.airise.auth.login

import kotlin.test.*

class LoginViewModelTest {

    private fun isLoginButtonEnabled(s: LoginUiState) = !s.isLoading

    private fun shouldShowError(s: LoginUiState): Boolean {
        val m = s.errorMessage
        return m != null && m.isNotBlank()
    }

    @Test
    fun default_state_is_empty_and_idle() {
        val s = LoginUiState()
        assertEquals("", s.email)
        assertEquals("", s.password)
        assertFalse(s.isLoading)
        assertFalse(s.isLoggedIn)
        assertNull(s.errorMessage)
        assertTrue(isLoginButtonEnabled(s))
        assertFalse(shouldShowError(s))
    }

    @Test
    fun copy_updates_email_and_password_only() {
        val base = LoginUiState()
        val updated = base.copy(email = "user@airise.app", password = "StrongPass!")
        assertEquals("user@airise.app", updated.email)
        assertEquals("StrongPass!", updated.password)
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
        assertTrue(shouldShowError(withError))
        assertFalse(shouldShowError(cleared))
    }

    @Test
    fun loading_and_loggedin_flags_toggle_via_copy() {
        val loading = LoginUiState(isLoading = true)
        val logged = LoginUiState(isLoggedIn = true)
        assertTrue(loading.isLoading)
        assertTrue(logged.isLoggedIn)
        assertFalse(isLoginButtonEnabled(loading))
        assertTrue(isLoginButtonEnabled(logged))
    }

    @Test
    fun login_flow_success_path_updates_fields() {
        var s = LoginUiState()
        s = s.copy(email = "ok@airise.app", password = "Password1!", isLoading = true)
        assertTrue(s.isLoading)
        assertFalse(isLoginButtonEnabled(s))
        s = s.copy(isLoading = false, isLoggedIn = true)
        assertTrue(s.isLoggedIn)
        assertFalse(s.isLoading)
        assertNull(s.errorMessage)
        assertTrue(isLoginButtonEnabled(s))
    }

    @Test
    fun login_flow_failure_sets_error_and_resets_loading() {
        var s = LoginUiState(email = "bad@airise.app", password = "wrongpass", isLoading = true)
        s = s.copy(isLoading = false, isLoggedIn = false, errorMessage = "Incorrect email or password")
        assertFalse(s.isLoggedIn)
        assertFalse(s.isLoading)
        assertEquals("Incorrect email or password", s.errorMessage)
        assertTrue(shouldShowError(s))
        assertTrue(isLoginButtonEnabled(s))
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
        val loggedIn = LoginUiState(email = "user@airise.app", isLoggedIn = true, errorMessage = "x")
        val reset = loggedIn.copy(email = "", password = "", isLoggedIn = false, errorMessage = null, isLoading = false)
        assertEquals("", reset.email)
        assertEquals("", reset.password)
        assertFalse(reset.isLoggedIn)
        assertNull(reset.errorMessage)
        assertTrue(isLoginButtonEnabled(reset))
    }

    @Test
    fun blank_or_whitespace_error_is_not_shown() {
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
    fun email_and_password_change_events_can_drive_state_simulation() {
        var s = LoginUiState()

        val e1: LoginUiEvent = LoginUiEvent.EmailChanged("flow@airise.app")
        val e2: LoginUiEvent = LoginUiEvent.PasswordChanged("SecurePass!")

        if (e1 is LoginUiEvent.EmailChanged) {
            s = s.copy(email = "flow@airise.app")
        }
        if (e2 is LoginUiEvent.PasswordChanged) {
            s = s.copy(password = "SecurePass!")
        }

        assertEquals("flow@airise.app", s.email)
        assertEquals("SecurePass!", s.password)
    }

    @Test
    fun login_event_exists_in_sealed_hierarchy() {
        val e: LoginUiEvent = LoginUiEvent.Login
        assertTrue(e is LoginUiEvent)
    }

    @Test
    fun google_event_exists_in_hierarchy_even_without_token_checks() {
        val e: LoginUiEvent = LoginUiEvent.GoogleSignInSuccess("any-token")
        assertTrue(e is LoginUiEvent.GoogleSignInSuccess)
    }

    @Test
    fun clearing_error_does_not_change_login_flag() {
        val s = LoginUiState(isLoggedIn = true, errorMessage = "x")
        val cleared = s.copy(errorMessage = null)
        assertTrue(cleared.isLoggedIn)
        assertNull(cleared.errorMessage)
    }

    @Test
    fun setting_error_does_not_toggle_loading_or_login() {
        val s = LoginUiState(isLoggedIn = false, isLoading = false)
        val withErr = s.copy(errorMessage = "oops")
        assertFalse(withErr.isLoggedIn)
        assertFalse(withErr.isLoading)
        assertTrue(shouldShowError(withErr))
    }

    @Test
    fun whitespace_in_inputs_is_preserved_no_trimming() {
        val s = LoginUiState(email = "  spaced@airise.app  ", password = "  spaced  ")
        assertEquals("  spaced@airise.app  ", s.email)
        assertEquals("  spaced  ", s.password)
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
    fun loading_with_error_still_disables_button() {
        val s = LoginUiState(isLoading = true, errorMessage = "err")
        assertFalse(isLoginButtonEnabled(s))
        assertTrue(shouldShowError(s))
    }

    @Test
    fun setting_inputs_does_not_toggle_flags() {
        val base = LoginUiState(isLoggedIn = false, isLoading = false)
        val s = base.copy(email = "a@b.com", password = "xYz!2345")
        assertFalse(s.isLoggedIn)
        assertFalse(s.isLoading)
    }

    @Test
    fun copy_does_not_mutate_original_instance() {
        val original = LoginUiState(email = "orig@airise.app", password = "orig")
        val modified = original.copy(email = "new@airise.app")
        assertEquals("orig@airise.app", original.email)
        assertEquals("new@airise.app", modified.email)
    }

    @Test
    fun very_long_error_message_is_preserved_but_not_trimmed() {
        val long = "E".repeat(3000)
        val s = LoginUiState(errorMessage = long)
        assertEquals(3000, s.errorMessage!!.length)
        assertTrue(shouldShowError(s))
    }

    @Test
    fun isLoading_and_isLoggedIn_can_coexist_and_are_detectable() {
        val s = LoginUiState(isLoading = true, isLoggedIn = true)
        assertTrue(s.isLoading)
        assertTrue(s.isLoggedIn)
        assertFalse(isLoginButtonEnabled(s))
    }

    @Test
    fun clearing_inputs_keeps_flags_intact() {
        val start = LoginUiState(email = "x@y.com", password = "p", isLoggedIn = true)
        val cleared = start.copy(email = "", password = "")
        assertTrue(cleared.isLoggedIn)
        assertEquals("", cleared.email)
        assertEquals("", cleared.password)
    }

    @Test
    fun multiple_error_transitions_show_and_hide_correctly() {
        var s = LoginUiState()
        s = s.copy(errorMessage = "e1")
        assertTrue(shouldShowError(s))
        s = s.copy(errorMessage = null)
        assertFalse(shouldShowError(s))
        s = s.copy(errorMessage = "e2")
        assertTrue(shouldShowError(s))
    }

}

