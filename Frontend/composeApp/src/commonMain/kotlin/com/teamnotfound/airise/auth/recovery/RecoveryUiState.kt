package com.teamnotfound.airise.auth.recovery

import com.teamnotfound.airise.data.serializable.User

data class RecoveryUiState (
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)