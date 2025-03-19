package com.teamnotfound.airise

import androidx.lifecycle.ViewModel
import com.teamnotfound.airise.network.UserClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppViewModel(private val userClient: UserClient) : ViewModel() {
    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        // introduce this _isUserLoggedIn.value = userClient.getCurrentUser() != null
    }

    fun loginUser(email: String, password: String) {
        // Example: Use userClient to perform login
        _isUserLoggedIn.update { true }
    }

    fun logoutUser() {
        userClient // Call loggout function
        _isUserLoggedIn.update { false }
    }
}
