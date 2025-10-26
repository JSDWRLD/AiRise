package com.teamnotfound.airise.auth.admin

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamnotfound.airise.data.BaseViewModel
import com.teamnotfound.airise.data.auth.AuthResult
import com.teamnotfound.airise.data.auth.AuthService
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.repository.IUserRepository
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.util.NetworkError
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminVerifyViewModel(
    private val authService: AuthService,
    private val userRepository: IUserRepository
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(AdminVerifyUiState())
    val uiState: StateFlow<AdminVerifyUiState> = _uiState

    init {
        checkIfAdmin()
    }

    fun onEvent(uiEvent: AdminVerifyUiEvent) {
        when (uiEvent) {
            is AdminVerifyUiEvent.Verify -> {
                return verifyPassword()
            }
            is AdminVerifyUiEvent.PasswordChanged -> {
                _uiState.value =
                    _uiState.value.copy(password = uiEvent.password, errorMessage = null)
            }
            is AdminVerifyUiEvent.ToggleAdminModeActive -> {
                val toggledBool = !_uiState.value.isAdminModeActive
                _uiState.value = _uiState.value.copy(isAdminModeActive = toggledBool)
            }
        }
    }


    private fun verifyPassword() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = Firebase.auth.currentUser
            val email = user?.email ?: return@launch

            val authResult = authService.authenticate(email, _uiState.value.password)

            _uiState.value = _uiState.value.copy(isLoading = false)

            when (authResult) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isVerified = true, errorMessage = null)
                }
                is AuthResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isVerified = false, errorMessage = authResult.errorMessage)
                }
            }
        }
    }

    fun resetVerification() {
        _uiState.value = _uiState.value.copy(isVerified = false)
    }

    private fun checkIfAdmin() {
        viewModelScope.launch {
            when (val result = userRepository.fetchUserData()) {
                is Result.Error<NetworkError> -> _uiState.value = _uiState.value.copy(isAdmin = false)
                is Result.Success<UserData> -> _uiState.value =
                    _uiState.value.copy(isAdmin = result.data.isAdmin)
            }
        }
    }
    fun dismissPasswordPrompt(){
        _uiState.update { currentState ->
            currentState.copy(showAdminPasswordPrompt = false, password = "")
        }
    }
    fun showPasswordPrompt(){
        _uiState.update { currentState ->
            currentState.copy(showAdminPasswordPrompt = true)
        }
    }
}