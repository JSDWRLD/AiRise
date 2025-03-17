package com.teamnotfound.airise.onboarding.signup

// SignUp UI Event
sealed class SignUpUiEvent {
    data class EmailChanged(val email: String) : SignUpUiEvent()
    data class UsernameChanged(val username: String) : SignUpUiEvent()
    data class PasswordChanged(val password: String) : SignUpUiEvent()
    data object Register : SignUpUiEvent()
    data object NavigateToLogin : SignUpUiEvent()
}