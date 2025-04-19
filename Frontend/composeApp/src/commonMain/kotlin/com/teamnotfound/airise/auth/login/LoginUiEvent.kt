package com.teamnotfound.airise.auth.login
// Login UI Event
sealed class LoginUiEvent {
    data class EmailChanged(val email: String) : LoginUiEvent()
    data class PasswordChanged(val password: String) : LoginUiEvent()
    data class GoogleSignInSuccess( val token: String) : LoginUiEvent()
    data object Login : LoginUiEvent()
}