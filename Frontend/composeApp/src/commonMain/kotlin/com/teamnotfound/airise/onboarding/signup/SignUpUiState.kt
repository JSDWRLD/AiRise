package com.teamnotfound.airise.onboarding.signup

import com.teamnotfound.airise.data.serializable.UserModel

data class SignUpUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val registeredUser: UserModel? = null
)