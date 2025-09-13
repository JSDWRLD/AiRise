package com.teamnotfound.airise.auth.recovery

data class RecoveryUiState (
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)