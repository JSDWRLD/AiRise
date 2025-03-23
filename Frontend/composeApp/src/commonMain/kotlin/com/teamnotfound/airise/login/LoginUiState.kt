package com.teamnotfound.airise.login

import com.teamnotfound.airise.data.auth.User

//Login UI State
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val currentUser: User? = null,
)
