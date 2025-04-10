package com.teamnotfound.airise.home.accountSettings


data class AccountSetttingsUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isSignedOut: Boolean = false,
    val errorMessage: String? = null,
)