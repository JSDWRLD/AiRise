package com.teamnotfound.airise.login

class SignupUiState {
    data class SignUpUiState(
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isLoading: Boolean = false,
        val isSignUpSuccessful: Boolean = false,
        val errorMessage: String? = null
    )
}