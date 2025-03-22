package com.teamnotfound.airise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.auth.AuthResult
import com.teamnotfound.airise.data.auth.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(private val authService: AuthService) : ViewModel() {
    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        _isUserLoggedIn.value = authService.isAuthenticated
    }

    // Not really required since loginviewmodel does this
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            val result = authService.authenticate(email, password)
            if (result is AuthResult.Success) {
                _isUserLoggedIn.update { true }
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            authService.signOut()
            _isUserLoggedIn.update { false }
        }
    }
}
