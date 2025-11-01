package com.teamnotfound.airise.auth.login

import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.BaseViewModel
import com.teamnotfound.airise.data.auth.AuthResult
import com.teamnotfound.airise.data.auth.AuthService
import com.teamnotfound.airise.data.auth.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.teamnotfound.airise.data.cache.UserCache
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth


// This class is ready to handle login and signup screen
// class LoginViewModel(private val httpClient: HttpClient) : ViewModel() {
class LoginViewModel(
    private val authService: AuthService,
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

            is LoginUiEvent.GoogleSignInSuccess -> {
               authenticateWithGoogle(uiEvent.token)
            }
        }

    }


    // This function is to be uncommented & ran once Firebase keys are configured
    fun authenticateWithGoogle(idToken: String) {
        // Set loading state
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                // Call the AuthService to handle the Firebase authentication with Google
                val authResult = authService.authenticateWithGoogle(idToken)

                _uiState.value = _uiState.value.copy(isLoading = false)

                when (authResult) {
                    is AuthResult.Success -> {

                        // Update UI state to reflect successful login
                        _uiState.value = _uiState.value.copy(
                            isLoggedIn = true,
                            email = authResult.data.email ?: "",
                            errorMessage = null
                        )
                    }

                    is AuthResult.Failure -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Google Sign-In failed: ${authResult.errorMessage}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google Sign-In failed: ${e.message}"
                )
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
                    val firebaseUser = Firebase.auth.currentUser
                    firebaseUser?.reload()
                    if (firebaseUser?.isEmailVerified == true) {
                        _uiState.value = _uiState.value.copy(isLoggedIn = true)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isEmailNotVerified = true,
                            errorMessage = "Email not verified."
                        )
                    }
                }

                is AuthResult.Failure -> {
                    _uiState.value = _uiState.value.copy(errorMessage = authResult.errorMessage)
                }
            }
        }
    }
}