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



// This class is ready to handle login and signup screen
// class LoginViewModel(private val httpClient: HttpClient) : ViewModel() {
class LoginViewModel(
    private val authService: AuthService,
    private val userCache: UserCache
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

// PlaceHolder function to simulate authentication
    fun authenticateWithGoogle(idToken: String) {
        // Set loading state
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {

                // dummy user with "id" being the raw token.

                val email = "hibahran@gmail.com"

                // Create a User object
                val user = User(
                    id = idToken, // Generate a temporary ID
                    email = email
                )

                // Cache the user data
                userCache.cacheUserData(user)

                // Update UI state to reflect successful login
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = true,
                    isLoading = false,
                    email = email,
                    errorMessage = null
                )

            } catch (e: Exception) {
                // Handle unexpected exceptions
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google Sign-In failed: ${e.message}"
                )
            }
        }
    }


    /* REAL AUTHENTICATION FUNCTION
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
                        // cache the user data similarly to email/password authentication
                        userCache.cacheUserData(authResult.data)

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
*/





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
                    userCache.cacheUserData(authResult.data)
                    _uiState.value = _uiState.value.copy(isLoggedIn = true)
                }

                is AuthResult.Failure -> {
                    _uiState.value = _uiState.value.copy(errorMessage = authResult.errorMessage)
                }
            }
        }
    }
}