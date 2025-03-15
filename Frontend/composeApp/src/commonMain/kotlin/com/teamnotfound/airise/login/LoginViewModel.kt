package com.teamnotfound.airise.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class LoginViewModel(private val httpClient: HttpClient) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEvent(uiEvent: LoginUiEvent) {
        when(uiEvent) {
            is LoginUiEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(email = uiEvent.email, errorMessage = null)
            }
            is LoginUiEvent.PasswordChanged -> {
                _uiState.value = _uiState.value.copy(password = uiEvent.password, errorMessage = null)
            }
            is LoginUiEvent.Login -> {
                login()
            }
        }
    }

    private fun login() {
        viewModelScope.launch {
            // Validate inputs before making the request
            if (_uiState.value.email.isEmpty() || _uiState.value.password.isEmpty()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Email or password cannot be empty")
                return@launch
            }

            // Set loading state
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Making a POST request with email and password
                val response = httpClient.post("https://api-endpoint/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(_uiState.value.email, _uiState.value.password))
                }

                // Check if login is successful
                if (response.status == HttpStatusCode.OK) {
                    // Update state to indicate successful login
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )

                } else {
                    // Handle error (incorrect credentials)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Login failed: ${response.status.description}"
                    )
                }
            } catch (e: Exception) {
                // Handle network or other errors
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Login failed: ${e ?: "Unknown error"}"
                )
            }
        }
    }
}