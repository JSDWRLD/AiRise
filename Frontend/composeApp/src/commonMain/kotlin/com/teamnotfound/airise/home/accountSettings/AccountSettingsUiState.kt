package com.teamnotfound.airise.home.accountSettings

import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.data.serializable.UserSettingsData


data class AccountSettingsUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isSignedOut: Boolean = false,
    val userSettings: UserSettingsData? = null,
    val errorMessage: String? = null,
    val userData: UserData? = null
)