package com.teamnotfound.airise.home.accountSettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.auth.AuthService
import com.teamnotfound.airise.util.NetworkError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.serializable.UserDataUiState
import com.teamnotfound.airise.data.serializable.UserSettingsData
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

class AccountSettingsViewModel(private val authService: AuthService,private val userClient: UserClient) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountSettingsUiState())
    val uiState: StateFlow<AccountSettingsUiState> = _uiState

    fun signout() {
        viewModelScope.launch {
            authService.signOut()  // Suspend function executed here
            _uiState.value = _uiState.value.copy(isSignedOut = true)
        }
    }

    fun getUserSettings(firebaseUser: FirebaseUser) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = userClient.getUserSettings(firebaseUser)) {
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

    fun updateUserSettings(userSettings: UserSettingsData, firebaseUser: FirebaseUser) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = userClient.upsertUserSettings(
                userSettings, firebaseUser)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
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

    fun uploadProfilePicture(bytes: ByteArray, firebaseUser: FirebaseUser?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                if (firebaseUser == null) throw Exception("User not authenticated")

                val fileName = "${firebaseUser.uid}.jpg"
                val path = "profile_pictures/$fileName"

                Supabase.bucket.upload(
                    path,
                    bytes
                ) {
                    upsert = true
                }

                val publicUrl = Supabase.bucket.publicUrl(path)

                // *** IMPORTANT ***
                val currentTimestamp = Clock.System.now().toEpochMilliseconds()
                val cacheBustedUrl = "$publicUrl?t=$currentTimestamp"
                updateProfilePicture(cacheBustedUrl, firebaseUser)

                delay(1000)
                // refresh ui
                getUserSettings(firebaseUser)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Upload failed: ${e.message}"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }


    //We might need these to update only certain parts, may implement later, full get and update are a bigger concern
    fun updateNotificationSettings(
        challengeEnabled: Boolean? = null,
        friendReqEnabled: Boolean? = null,
        streakEnabled: Boolean? = null,
        mealEnabled: Boolean? = null,
        firebaseUser: FirebaseUser
    ) {
        val currentSettings = _uiState.value.userSettings ?: return

        val updatedSettings = currentSettings.copy(
            challengeNotifsEnabled = challengeEnabled ?: currentSettings.challengeNotifsEnabled,
            friendReqNotifsEnabled = friendReqEnabled ?: currentSettings.friendReqNotifsEnabled,
            streakNotifsEnabled = streakEnabled ?: currentSettings.streakNotifsEnabled,
            mealNotifsEnabled = mealEnabled ?: currentSettings.mealNotifsEnabled
        )

        updateUserSettings(updatedSettings, firebaseUser)
    }

    fun updateAiPersonality(personality: String, firebaseUser: FirebaseUser) {
        val currentSettings = _uiState.value.userSettings ?: return
        val updatedSettings = currentSettings.copy(aiPersonality = personality)
        updateUserSettings(updatedSettings, firebaseUser)
    }

    fun updateProfilePicture(pictureUrl: String, firebaseUser: FirebaseUser) {
        val currentSettings = _uiState.value.userSettings ?: return
        val updatedSettings = currentSettings.copy(profilePictureUrl = pictureUrl)
        updateUserSettings(updatedSettings, firebaseUser)
    }

    fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isSuccess = false
            )

            try {
                val firebaseUser = authService.firebaseUser
                if (firebaseUser != null) {
                    when (val result = userClient.getUserData(firebaseUser)) {
                        is Result.Success -> {
                            _uiState.value = _uiState.value.copy(
                                userData = result.data,
                                isLoading = false,
                                errorMessage = null,
                                isSuccess = true
                            )
                        }
                        is Result.Error -> {
                            _uiState.value = _uiState.value.copy(
                                userData = null,
                                isLoading = false,
                                errorMessage = mapError(result.error),
                                isSuccess = false
                            )
                        }
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "User not authenticated",
                        isSuccess = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to read user data: ${e.message}",
                    isSuccess = false
                )
            }
        }
    }

    fun saveUserData(userData: UserDataUiState) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val firebaseUser = authService.firebaseUser
                if (firebaseUser != null) {
                    when (val result = userClient.insertUserData(firebaseUser, userData.toData())) {
                        is Result.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false
                            )
                        }

                        is Result.Error -> {
                            _uiState.value = _uiState.value.copy(
                                errorMessage = mapError(result.error),
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun mapError(error: NetworkError): String {
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