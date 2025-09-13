package com.teamnotfound.airise.community.challenges

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

//data for single challenge
data class ChallengeUI(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String
)

data class ChallengesUiState(
    val isLoading: Boolean = false,
    val items: List<ChallengeUI> = emptyList(),
    val error: String? = null,
    val progress: UserChallengeProgressUI = UserChallengeProgressUI()
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