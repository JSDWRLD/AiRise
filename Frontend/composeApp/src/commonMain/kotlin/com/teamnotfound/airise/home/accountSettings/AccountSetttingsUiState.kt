package com.teamnotfound.airise.home.accountSettings

import com.teamnotfound.airise.data.serializable.UserSettingsData


data class AccountSetttingsUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isSignedOut: Boolean = false,
    val userSettings: UserSettingsData? = null,
    val errorMessage: String? = null,
)
