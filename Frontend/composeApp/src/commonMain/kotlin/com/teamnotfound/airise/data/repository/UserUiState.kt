package com.teamnotfound.airise.data.repository

import com.teamnotfound.airise.data.serializable.UserData

data class UserUiState(
    val userData: UserData = UserData(),
    val name: String = "GUEST",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
