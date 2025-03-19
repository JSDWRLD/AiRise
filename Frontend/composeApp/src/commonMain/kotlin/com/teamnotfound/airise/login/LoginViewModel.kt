package com.teamnotfound.airise.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.serializable.UserLogin
import com.teamnotfound.airise.network.UserClient
import com.teamnotfound.airise.util.NetworkError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.teamnotfound.airise.network.Result

// Removing HTTP client and login function until it can be accepted.
// This class is ready to handle login and signup screen
// class LoginViewModel(private val httpClient: HttpClient) : ViewModel() {
class LoginViewModel(private val client: UserClient) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEvent(uiEvent: LoginUiEvent) {
        when (uiEvent) {
            is LoginUiEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(email = uiEvent.email, errorMessage = null)
            }
            is LoginUiEvent.PasswordChanged -> {
                _uiState.value = _uiState.value.copy(password = uiEvent.password, errorMessage = null)
            }
            is LoginUiEvent.Login -> {
                loginUser()
            }
        }
    }

    private fun loginUser() {
        viewModelScope.launch {
            // Validate input
            if (_uiState.value.email.isEmpty() || _uiState.value.password.isEmpty()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Email or password cannot be empty")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Attempt login
            when (val result = client.login(UserLogin(
                email = _uiState.value.email,
                password = _uiState.value.password
            ))) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = when (result.error) {
                            NetworkError.UNAUTHORIZED -> "Invalid credentials."
                            NetworkError.NO_INTERNET -> "No internet connection."
                            NetworkError.SERIALIZATION -> "An error occurred. Try again."
                            else -> "Login failed. Please try again."
                        }
                    )
                }

                is Result.Error<*> -> TODO()
                is Result.Success<*> -> TODO()
            }
        }
    }
}