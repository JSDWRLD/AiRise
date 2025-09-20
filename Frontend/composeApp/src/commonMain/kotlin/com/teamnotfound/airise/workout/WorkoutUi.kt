package com.teamnotfound.airise.workout

import kotlinx.coroutines.flow.StateFlow

data class WorkoutRow(
    val id: String,
    val name: String,
    val sets: List<WorkoutSet> = listOf(WorkoutSet(), WorkoutSet(), WorkoutSet())
)

data class WorkoutSet(
    val reps: Int = 0,
    val weightLbs: Int = 0
)

data class WorkoutUiState(
    val isLoading: Boolean = false,
    val items: List<WorkoutRow> = emptyList(),
    val error: String? = null
)

interface WorkoutViewModelContract {
    val uiState: StateFlow<WorkoutUiState>
    fun refresh()
    fun changeSet(workoutId: String, index: Int, reps: Int?, weight: Int?)
    fun logAll()
}
