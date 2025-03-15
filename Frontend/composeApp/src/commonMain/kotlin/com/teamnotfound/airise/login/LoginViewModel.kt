package com.teamnotfound.airise.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEvent(uiEvent: LoginUiEvent) {
        when(uiEvent) {
            is LoginUiEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(email = uiEvent.email)
            }
            is LoginUiEvent.PasswordChanged -> {
                _uiState.value = _uiState.value.copy(password = uiEvent.password)
            }
            else -> {

            }
        }
    }

}