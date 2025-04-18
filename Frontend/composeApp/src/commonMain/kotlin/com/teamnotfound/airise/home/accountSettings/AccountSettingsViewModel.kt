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
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.serializable.UserSettingsData
import dev.gitlive.firebase.auth.FirebaseUser

class AccountSettingsViewModel(private val authService: AuthService,private val userClient: UserClient) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountSetttingsUiState())
    val uiState: StateFlow<AccountSetttingsUiState> = _uiState

    fun signout() {
        viewModelScope.launch {
            authService.signOut()  // Suspend function executed here
            _uiState.value = _uiState.value.copy(isSignedOut = true)
        }
    }

    fun getUserSettings(firebaseUser: FirebaseUser) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = userClient.getUserSettings(firebaseUser.uid)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        userSettings = result.data,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = mapError(result.error)
                    )
                }
            }
        }
    }

    fun updateUserSettings(userSettings: UserSettingsData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = userClient.upsertUserSettings(userSettings)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        userSettings = result.data,
                        isLoading = false,
                        errorMessage = null,
                        isSuccess = true
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = mapError(result.error),
                        isSuccess = false                    )
                }
            }
        }
    }
    //We might need these to avoid sending all the info for an update.
    fun updateNotificationSettings(
        challengeEnabled: Boolean? = null,
        friendReqEnabled: Boolean? = null,
        streakEnabled: Boolean? = null,
        mealEnabled: Boolean? = null
    ) {
        val currentSettings = _uiState.value.userSettings ?: return

        val updatedSettings = currentSettings.copy(
            challengeNotifsEnabled = challengeEnabled ?: currentSettings.challengeNotifsEnabled,
            friendReqNotifsEnabled = friendReqEnabled ?: currentSettings.friendReqNotifsEnabled,
            streakNotifsEnabled = streakEnabled ?: currentSettings.streakNotifsEnabled,
            mealNotifsEnabled = mealEnabled ?: currentSettings.mealNotifsEnabled
        )

        updateUserSettings(updatedSettings)
    }

    fun updateAiPersonality(personality: String) {
        val currentSettings = _uiState.value.userSettings ?: return
        val updatedSettings = currentSettings.copy(aiPersonality = personality)
        updateUserSettings(updatedSettings)
    }

    fun updateProfilePicture(pictureUrl: String) {
        val currentSettings = _uiState.value.userSettings ?: return
        val updatedSettings = currentSettings.copy(profilePictureUrl = pictureUrl)
        updateUserSettings(updatedSettings)
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
        }
    }
}