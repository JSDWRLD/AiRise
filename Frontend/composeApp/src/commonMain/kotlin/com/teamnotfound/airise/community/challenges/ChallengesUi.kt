package com.teamnotfound.airise.community.challenges

import kotlinx.coroutines.flow.StateFlow

//data for single challenge
data class ChallengeUI(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val isStarted: Boolean = false,
    val startTime: Long? = null,
    val isCompleted: Boolean = false
)

data class ChallengesUiState(
    val isLoading: Boolean = false,
    val items: List<ChallengeUI> = emptyList(),
    val error: String? = null
)

//ex challenge view model implementation
interface ChallengesViewModel {
    val uiState: StateFlow<ChallengesUiState>
    fun refresh()
    fun onChallengeClick(id: String)
}
