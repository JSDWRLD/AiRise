package com.teamnotfound.airise.community.challenges

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.teamnotfound.airise.data.serializable.Challenge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

//data for single challenge
@Serializable
data class ChallengeUI (
    var id: String = "",
    var name: MutableState<String> = mutableStateOf(""),
    var description: MutableState<String> = mutableStateOf(""),
    var imageUrl: MutableState<String> = mutableStateOf(""))
{
    fun toData() : Challenge = Challenge(
        id = id,
        name = name.value,
        description = description.value,
        url = imageUrl.value
    )
}


data class ChallengesUiState(
    val isLoading: Boolean = false,
    val items: List<ChallengeUI> = emptyList(),
    val error: String? = null,
    val progress: UserChallengeProgressUI = UserChallengeProgressUI(),
    var showAdminPasswordPrompt: Boolean = false
)

//ex challenge view model implementation
interface ChallengesViewModel {
    val uiState: StateFlow<ChallengesUiState>
    val events: Flow<ChallengesEvent>
    fun refresh()
    fun onChallengeClick(id: String)
}

data class UserChallengeProgressUI(
    val activeChallengeId: String? = null,
    val lastCompletionEpochDay: Long? = null,
    // NEW: truth from GET /completed-today/{uid}
    val completedToday: Boolean = false
) {
    fun completedOn(epochDay: Long): Boolean =
        lastCompletionEpochDay != null && lastCompletionEpochDay == epochDay
}

data class ChallengeEditorUIState(
    val isEditing: Boolean = false,
    val challengeUI: ChallengeUI = ChallengeUI(),
    val isLoading: Boolean = false,
    val authFailed: Boolean = false,
    val error: String? = null
)