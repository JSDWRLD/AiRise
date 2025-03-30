package com.teamnotfound.airise.home.accountSettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.auth.signup.SignUpUiState
import com.teamnotfound.airise.data.DTOs.RegisterUserDTO
import com.teamnotfound.airise.data.auth.AuthResult
import com.teamnotfound.airise.data.auth.AuthService
import com.teamnotfound.airise.util.NetworkError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class AccountSettingsViewModel(private val authService: AuthService,): ViewModel() {

    private val _uiState = MutableStateFlow(AccountSetttingsUiState())
    val uiState: MutableStateFlow<AccountSetttingsUiState> = _uiState

    fun signout() {
        viewModelScope.launch {
            authService.signOut()  // Suspend function executed here
            _uiState.value = _uiState.value.copy(isSignedOut = true)
        }
    }

    private fun mapError(error: NetworkError): String {
        return when (error) {
            NetworkError.NO_INTERNET -> "No internet connection."
            NetworkError.SERIALIZATION -> "Data error. Please try again."
            NetworkError.UNAUTHORIZED -> "Unauthorized access."
            NetworkError.CONFLICT -> "User already exists."
            NetworkError.UNKNOWN -> "Unknown error occurred."
            NetworkError.REQUEST_TIMEOUT -> TODO()
            NetworkError.TOO_MANY_REQUESTS -> TODO()
            NetworkError.PAYLOAD_TOO_LARGE -> TODO()
            NetworkError.SERVER_ERROR -> TODO()
            NetworkError.BAD_REQUEST -> TODO()
        }
    }
}