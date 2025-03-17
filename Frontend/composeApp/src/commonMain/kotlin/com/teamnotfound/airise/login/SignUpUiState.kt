package com.teamnotfound.airise.login

import com.teamnotfound.airise.serializable.UserAuthData

data class SignUpUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val registeredUser: UserAuthData? = null
)