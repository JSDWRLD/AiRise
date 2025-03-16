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

// Removing HTTP client and login function until it can be accepted.
// This class is ready to handle login and signup screen
// class LoginViewModel(private val httpClient: HttpClient) : ViewModel() {
class LoginViewModel() : ViewModel() {
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
                simulateLogin()
            }
            is LoginUiEvent.Signup -> {
                simulateLogin()
            }
        }
    }
    /*
    // Login method
    fun login(username: String, password: String) {
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
    // Registration method
    fun register(username: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            // Validate input
            val validationResult = validateRegistrationInput(username, email, password, confirmPassword)
            if (!validationResult.isValid) {
                _uiState.value = _uiState.value.copy(errorMessage = validationResult.errorMessage)
                return@launch
            }

            // Set loading state
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Make a POST request
                val response = httpClient.post("https://api-endpoint/signup") {
                    contentType(ContentType.Application.Json)
                    setBody(RegisterRequest(username, email, password))
                }
                // Request successful
                if (response.status == HttpStatusCode.Created) {
                    // Handle successful registration
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegistered = true,
                        successMessage = "Registration successful! Please verify your email."
                    )
                    // Handle unsucessful requests
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Registration failed: ${response.status.description}"
                    )
                }
            } catch (e: Exception) {
                // Handle network or other errors
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Registration failed: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
     */
    private fun simulateLogin() {
        viewModelScope.launch {
            // Checking for invalid credentials
            if (_uiState.value.email.isEmpty() || _uiState.value.password.isEmpty()) {
                _uiState.value =
                    _uiState.value.copy(errorMessage = "Email or password cannot be empty")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Validating
            if (_uiState.value.email.contains("@") && _uiState.value.password.length >= 6) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true
                )
            } else {
                // Else invalid
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Invalid email or password"
                )
            }
        }
    }
}