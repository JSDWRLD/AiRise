package com.teamnotfound.airise.workout

import com.teamnotfound.airise.data.serializable.UserProgramDoc
import kotlinx.serialization.Serializable

sealed interface WorkoutUiState {
    data object Loading : WorkoutUiState
    data class Error(val error: Throwable) : WorkoutUiState
    data class Success(val programDoc: UserProgramDoc) : WorkoutUiState
}
