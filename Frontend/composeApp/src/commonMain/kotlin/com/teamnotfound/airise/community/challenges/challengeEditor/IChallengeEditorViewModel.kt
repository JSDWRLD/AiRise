package com.teamnotfound.airise.community.challenges.challengeEditor

import com.teamnotfound.airise.community.challenges.ChallengeEditorUIState
import com.teamnotfound.airise.community.challenges.ChallengeUI
import kotlinx.coroutines.flow.StateFlow

interface IChallengeEditorViewModel {
    val uiState: StateFlow<ChallengeEditorUIState>
    fun onEvent(uiEvent: ChallengeEditorUiEvent)
    fun delete(id: String)
    fun upsert()
}