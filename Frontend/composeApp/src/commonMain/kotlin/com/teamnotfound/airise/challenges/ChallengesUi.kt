package com.teamnotfound.airise.challenges

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
    val error: String? = null
)

//ex challenge view model implementation
interface ChallengesViewModel {
    val uiState: kotlinx.coroutines.flow.StateFlow<ChallengesUiState>
    fun refresh()
    fun onChallengeClick(id: String)
}
