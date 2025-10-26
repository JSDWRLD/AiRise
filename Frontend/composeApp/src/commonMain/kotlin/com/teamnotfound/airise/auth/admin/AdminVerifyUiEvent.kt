package com.teamnotfound.airise.auth.admin

import com.teamnotfound.airise.auth.login.LoginUiEvent

sealed class AdminVerifyUiEvent {
    data object Verify: AdminVerifyUiEvent()
    data class PasswordChanged(val password: String): AdminVerifyUiEvent()
}