package com.teamnotfound.airise.community.challenges.challengeEditor

import com.teamnotfound.airise.community.challenges.ChallengeUI

sealed class ChallengeEditorUiEvent {
    data class NameChanged(val name: String): ChallengeEditorUiEvent()
    data class DescriptionChanged(val description: String) : ChallengeEditorUiEvent()
    data class ImageChanged(val imageBytes: ByteArray) : ChallengeEditorUiEvent()
    data class OpenEditor(val challengeUI: ChallengeUI): ChallengeEditorUiEvent()
    data object CloseEditor : ChallengeEditorUiEvent()
}