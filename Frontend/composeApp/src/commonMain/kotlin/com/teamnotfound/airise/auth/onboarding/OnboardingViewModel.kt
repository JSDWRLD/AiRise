package com.teamnotfound.airise.auth.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.auth.AuthService
import com.teamnotfound.airise.data.cache.SummaryCache
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.serializable.UserDataUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.util.NetworkError

class OnboardingViewModel(
    private val authService: AuthService,
    private val summaryCache: SummaryCache,
    private val userClient: UserClient
): ViewModel(){
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState

    fun saveUserData(userData: UserDataUiState) {
        if(!_uiState.value.isComplete){
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                try {
                    val firebaseUser = authService.firebaseUser
                    if (firebaseUser != null) {
                        when(val result = userClient.insertUserData(firebaseUser, userData.toData())) {
                            is Result.Success -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    isComplete = true
                                )
                            }
                            is Result.Error -> {
                                _uiState.value = _uiState.value.copy(
                                    error = mapError(result.error),
                                    isLoading = false,
                                    isComplete = false
                                )
                            }
                        }
                    }
                    val summaryData = userData.toData()
                    summaryCache.cacheSummary(summaryData)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
                _uiState.value = _uiState.value.copy(isLoading = false)
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
            NetworkError.REQUEST_TIMEOUT -> "Request timed out."
            NetworkError.TOO_MANY_REQUESTS -> "Too many requests. Please try again later."
            NetworkError.PAYLOAD_TOO_LARGE -> "Data too large to process."
            NetworkError.SERVER_ERROR -> "Server error occurred."
            NetworkError.BAD_REQUEST -> "Invalid request. Please check your data."
            NetworkError.FORBIDDEN -> TODO()
        }
    }
}
