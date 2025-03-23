package com.teamnotfound.airise.auth.login

import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.BaseViewModel
import com.teamnotfound.airise.data.auth.AuthResult
import com.teamnotfound.airise.data.auth.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Removing HTTP client and login function until it can be accepted.
// This class is ready to handle login and signup screen
// class LoginViewModel(private val httpClient: HttpClient) : ViewModel() {
class LoginViewModel(
    private val authService: AuthService
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    init {
        launchWithCatchingException {
            authService.currentUser.collect {
                _uiState.value = _uiState.value.copy(currentUser = it)
            }
        }
    }

    fun onEvent(uiEvent: LoginUiEvent) {
        when (uiEvent) {
            is LoginUiEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(email = uiEvent.email, errorMessage = null)
            }

            is LoginUiEvent.PasswordChanged -> {
                _uiState.value =
                    _uiState.value.copy(password = uiEvent.password, errorMessage = null)
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
                _uiState.value =
                    _uiState.value.copy(errorMessage = "Email or password cannot be empty")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val authResult = authService.authenticate(_uiState.value.email, _uiState.value.password)

            _uiState.value = _uiState.value.copy(isLoading = false)

            when (authResult) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoggedIn = true)
                }

                is AuthResult.Failure -> {
                    _uiState.value = _uiState.value.copy(errorMessage = authResult.errorMessage)
                }
            }
        }
    }
}