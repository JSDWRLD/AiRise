package com.teamnotfound.airise.community.challenges.challengeEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.community.challenges.ChallengeEditorUIState
import com.teamnotfound.airise.community.challenges.ChallengeUI
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.network.clients.DataClient
import com.teamnotfound.airise.data.serializable.Challenge
import com.teamnotfound.airise.util.NetworkError
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random


class ChallengeEditorViewModel(
    private val dataClient: DataClient
): ViewModel(), IChallengeEditorViewModel{
    private val _uiState = MutableStateFlow(ChallengeEditorUIState())
    override val uiState = _uiState.asStateFlow()

    override fun onEvent(uiEvent: ChallengeEditorUiEvent) {
        when (uiEvent) {
            is ChallengeEditorUiEvent.DescriptionChanged -> _uiState.value.challengeUI.description.value = uiEvent.description
            is ChallengeEditorUiEvent.ImageChanged -> uploadPicture(uiEvent.imageBytes)
            is ChallengeEditorUiEvent.NameChanged -> _uiState.value.challengeUI.name.value = uiEvent.name
            is ChallengeEditorUiEvent.CloseEditor -> _uiState.value = _uiState.value.copy(isEditing = false)
            is ChallengeEditorUiEvent.OpenEditor -> populate(uiEvent.challengeUI)
        }
    }


    private fun populate(challengeUI: ChallengeUI) {
        _uiState.value = _uiState.value.copy(
            isEditing = true,
            challengeUI = challengeUI
        )
    }

    override fun upsert(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val challenge: Challenge = _uiState.value.challengeUI.toData()
            val user = Firebase.auth.currentUser
            if (user == null) {
                _uiState.update { it.copy(error = "Not signed in") }
                return@launch
            }
            when (val res = dataClient.upsertChallenge(user, challenge)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isEditing = false)
                    onSuccess()
                    return@launch
                }
                is Result.Error -> {
                    if(res.error == NetworkError.FORBIDDEN || res.error == NetworkError.UNAUTHORIZED) {
                        _uiState.value =
                            _uiState.value.copy(authFailed = true, error = "Access error. Please cancel and re-verify to try again.")
                        return@launch
                    }
                    _uiState.value = _uiState.value.copy(error = "Unable to upsert the challenge")
                }
            }
        }
    }

    override fun delete(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
//            val id = _uiState.value.challengeUI.id
            if (id.isBlank()) {
                _uiState.value = _uiState.value.copy(error = "Unable to delete. Make sure to select an existing challenge to delete it.")
            }

            val user = Firebase.auth.currentUser
            if (user == null) {
                _uiState.update { it.copy(error = "Not signed in") }
                return@launch
            }
            when (dataClient.deleteChallenge(user, id)) {
                is Result.Success -> {
                    onSuccess()
                    return@launch
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(error = "Unable to delete the challenge due to a server error")
            }
        }
    }

    private fun generateChallengePath(): String {
        val timestamp = Clock.System.now().toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
        val randomSuffix = Random.nextInt(1000, 9999) // short, readable randomness
        return "challenges/img_${timestamp}_$randomSuffix.jpg"
    }

    private fun uploadPicture(bytes: ByteArray) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {

                val path = generateChallengePath()

                Supabase.bucket.upload(
                    path,
                    bytes
                ) {
                    upsert = true
                }

                val publicUrl = Supabase.bucket.publicUrl(path)

                _uiState.value.challengeUI.imageUrl.value = publicUrl

                delay(1000)
                // refresh ui

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Upload failed: ${e.message}"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}