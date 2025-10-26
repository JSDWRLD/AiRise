package com.teamnotfound.airise.auth.admin

import com.teamnotfound.airise.data.auth.User

data class AdminVerifyUiState (
    val password: String = "",
    val isAdmin: Boolean = false,
    val isVerified: Boolean = false,
    val showAdminPasswordPrompt: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentUser: User? = null
)