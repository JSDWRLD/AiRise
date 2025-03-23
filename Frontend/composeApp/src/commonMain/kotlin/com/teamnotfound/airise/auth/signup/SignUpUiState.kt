package com.teamnotfound.airise.auth.signup

import com.teamnotfound.airise.data.serializable.User

data class SignUpUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val currentUser: User? = null,
    val passwordMatch: Boolean = true
)