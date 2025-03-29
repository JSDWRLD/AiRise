package com.teamnotfound.airise.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.DTOs.RegisterUserDTO
import com.teamnotfound.airise.data.auth.AuthResult
import com.teamnotfound.airise.data.auth.AuthService
import com.teamnotfound.airise.util.NetworkError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.teamnotfound.airise.data.cache.UserCache

class SignUpViewModel(private val authService: AuthService, private val userCache: UserCache): ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun register(registerUserDTO: RegisterUserDTO) {
        if(!_uiState.value.passwordMatch) return //stops registration if password does not match
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val authResult = authService.createUser(registerUserDTO.email, registerUserDTO.password)

            _uiState.value = _uiState.value.copy(isLoading = false)

            when (authResult) {
                is AuthResult.Success -> {
                    userCache.cacheUserData(authResult.data)
                    _uiState.value = _uiState.value.copy(isSuccess = true)
                }
                is AuthResult.Failure -> {
                    _uiState.value = _uiState.value.copy(errorMessage = authResult.errorMessage)
                }
            }
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