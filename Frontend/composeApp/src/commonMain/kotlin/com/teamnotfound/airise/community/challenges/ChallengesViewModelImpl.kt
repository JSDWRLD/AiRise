package com.teamnotfound.airise.community.challenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.network.clients.DataClient
import com.teamnotfound.airise.util.NetworkError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChallengesViewModelImpl(
    private val dataClient: DataClient
) : ViewModel(), ChallengesViewModel {

    private val _uiState = MutableStateFlow(ChallengesUiState(isLoading = true))
    override val uiState: StateFlow<ChallengesUiState> = _uiState.asStateFlow()

    // Optional: expose selection like your example VM
    private val _selected = MutableStateFlow<ChallengeUI?>(null)
    val selected: StateFlow<ChallengeUI?> = _selected.asStateFlow()

    init {
        // mirror your pattern: kick off initial load(s)
        refresh()
    }

    override fun refresh() {
        loadChallenges()
    }

    private fun loadChallenges() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = dataClient.getChallenges()) {
                is Result.Success -> {
                    val items = result.data.map { it.toUI() }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        items = items,
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.error.humanMessage()
                    )
                }
            }
        }
    }

    override fun onChallengeClick(id: String) {
        select(id)
    }

    fun select(id: String) {
        _selected.value = _uiState.value.items.firstOrNull { it.id == id }
    }

    fun clearSelection() {
        _selected.value = null
    }

    fun updateSelectedDescription(newDesc: String) {
        val current = _selected.value ?: return
        val updated = current.copy(description = newDesc)
        _selected.value = updated
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.map { if (it.id == current.id) updated else it }
        )
    }
}

// --- Mappers & helpers

private fun com.teamnotfound.airise.data.serializable.Challenge.toUI(): ChallengeUI =
    ChallengeUI(
        id = id ?: name,                 // fallback if id missing
        name = name,
        description = description,
        imageUrl = url                   // server field is 'url'
    )

private fun NetworkError.humanMessage(): String = when (this) {
    NetworkError.NO_INTERNET   -> "No internet connection."
    NetworkError.SERIALIZATION -> "Invalid response format."
    NetworkError.BAD_REQUEST   -> "Bad request."
    NetworkError.CONFLICT      -> "Conflict."
    NetworkError.SERVER_ERROR  -> "Server error."
    NetworkError.UNKNOWN       -> "Unknown error."
    else                       -> "Something went wrong."
}
