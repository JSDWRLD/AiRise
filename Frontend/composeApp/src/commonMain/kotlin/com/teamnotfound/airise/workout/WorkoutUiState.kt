package com.teamnotfound.airise.workout

import kotlinx.serialization.Serializable

sealed interface WorkoutUiState {
    data object Loading : WorkoutUiState
    data class Error(val error: Throwable) : WorkoutUiState
    @Serializable
    data class Success(val workoutPlan: UserWorkoutPlan) : WorkoutUiState
}

@Serializable
data class UserWorkoutPlan(
    val id: String,
    val userId: String,
    val templateId: String,
    val exercises: List<ExerciseUi>
)

@Serializable
data class ExerciseUi(
    val exerciseTemplateId: String,
    val name: String,
    val plannedSets: Int,
    val plannedReps: String,
    val plannedWeight: Double,
    val notes: String,
    val setLogs: List<SetLogUi>
)

@Serializable
data class SetLogUi(
    val repsCompleted: Int,
    val weightUsed: Double,
)