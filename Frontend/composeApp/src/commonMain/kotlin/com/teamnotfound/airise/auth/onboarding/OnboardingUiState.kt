package com.teamnotfound.airise.auth.onboarding

import com.teamnotfound.airise.data.serializable.UserDataUiState

data class OnboardingUiState(
    val userDataUiState: UserDataUiState = UserDataUiState(),
    val isLoading: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null
)
