package com.teamnotfound.airise.workout

import com.teamnotfound.airise.data.serializable.UserProgramDoc
import com.teamnotfound.airise.util.NetworkError
import kotlinx.serialization.Serializable

sealed interface WorkoutUiState {
    data object Loading : WorkoutUiState
    data class Error(val error: NetworkError) : WorkoutUiState
    data class Success(val programDoc: UserProgramDoc) : WorkoutUiState
}
