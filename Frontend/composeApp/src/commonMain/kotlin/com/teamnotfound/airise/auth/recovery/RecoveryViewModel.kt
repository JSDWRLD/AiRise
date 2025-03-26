package com.teamnotfound.airise.auth.recovery

import airise.composeapp.generated.resources.Res
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.auth.signup.SignUpUiState
import com.teamnotfound.airise.data.DTOs.RegisterUserDTO
import com.teamnotfound.airise.data.auth.AuthResult
import com.teamnotfound.airise.data.auth.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecoveryViewModel(private val authService: AuthService): ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun sendEmail(email: String) {
        if(!_uiState.value.passwordMatch) return //stops registration if password does not match
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val authResult = authService.sendPasswordResetEmail(email)

            _uiState.value = _uiState.value.copy(isLoading = false)

            when (authResult) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSuccess = true)
                }
                is AuthResult.Failure -> {
                    _uiState.value = _uiState.value.copy(errorMessage = authResult.errorMessage)
                }
            }
        }
    }
}