package com.teamnotfound.airise.workout

import kotlinx.coroutines.flow.StateFlow

data class WorkoutUiState(
    val isLoading: Boolean = false,
    val items: List<WorkoutRow> = emptyList(),
    val error: String? = null
)

interface WorkoutViewModelContract {
    val uiState: StateFlow<WorkoutUiState>
    fun refresh()
    fun changeSet(workoutId: String, index: Int, reps: Int?, weight: Double?)
    fun changeExerciseNotes(workoutId: String, notes: String)
    fun logAll()
}
