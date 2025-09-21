package com.teamnotfound.airise.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Loading)
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = WorkoutUiState.Loading

            try {
                // HARD CODED WORKOUT PLAN
                val hardcodedData = createHardcodedWorkoutPlan()
                _uiState.value = WorkoutUiState.Success(hardcodedData)
            } catch (e: Exception) {
                _uiState.value = WorkoutUiState.Error(e)
            }
        }
    }

    fun changeSet(workoutId: String, setIndex: Int, reps: Int?, weight: Double?) {
        val currentState = _uiState.value as? WorkoutUiState.Success ?: return

        val updatedPlan = currentState.workoutPlan.copy(
            exercises = currentState.workoutPlan.exercises.map { exercise ->
                if (exercise.exerciseTemplateId == workoutId) {
                    val updatedSetLogs = exercise.setLogs.toMutableList().apply {
                        val currentSet = get(setIndex)
                        set(setIndex, currentSet.copy(
                            repsCompleted = reps ?: currentSet.repsCompleted,
                            weightUsed = weight ?: currentSet.weightUsed
                        ))
                    }
                    exercise.copy(setLogs = updatedSetLogs)
                } else {
                    exercise
                }
            }
        )
        _uiState.value = WorkoutUiState.Success(updatedPlan)
    }

    fun changeExerciseNotes(workoutId: String, notes: String) {
        val currentState = _uiState.value as? WorkoutUiState.Success ?: return

        val updatedPlan = currentState.workoutPlan.copy(
            exercises = currentState.workoutPlan.exercises.map { exercise ->
                if (exercise.exerciseTemplateId == workoutId) {
                    exercise.copy(notes = notes)
                } else {
                    exercise
                }
            }
        )
        _uiState.value = WorkoutUiState.Success(updatedPlan)
    }

    fun logAll() {
        val currentState = _uiState.value as? WorkoutUiState.Success ?: return
        println("Logging the following workout data:\n${currentState.workoutPlan}")
    }

    private fun createHardcodedWorkoutPlan(): UserWorkoutPlan {
        return UserWorkoutPlan(
            id = "plan_123",
            userId = "user_abc",
            templateId = "template_xyz",
            exercises = listOf(
                ExerciseUi(
                    exerciseTemplateId = "ex_456",
                    name = "Dumbbell Bench Press",
                    plannedSets = 3,
                    plannedReps = "8-12",
                    plannedWeight = 50.0,
                    notes = "Warm up with light weight.",
                    setLogs = listOf(
                        SetLogUi(repsCompleted = 10, weightUsed = 45.0),
                        SetLogUi(repsCompleted = 8, weightUsed = 50.0),
                        SetLogUi(repsCompleted = 7, weightUsed = 50.0),
                    )
                ),
                ExerciseUi(
                    exerciseTemplateId = "ex_789",
                    name = "Barbell Squats",
                    plannedSets = 4,
                    plannedReps = "6-10",
                    plannedWeight = 135.0,
                    notes = "",
                    setLogs = listOf(
                        SetLogUi(repsCompleted = 6, weightUsed = 135.0),
                        SetLogUi(repsCompleted = 5, weightUsed = 135.0),
                        SetLogUi(repsCompleted = 5, weightUsed = 135.0),
                        SetLogUi(repsCompleted = 4, weightUsed = 135.0),
                    )
                ),
                ExerciseUi(
                    exerciseTemplateId = "ex_101",
                    name = "Pull-Ups",
                    plannedSets = 3,
                    plannedReps = "To Failure",
                    plannedWeight = 0.0,
                    notes = "Use resistance band if needed.",
                    setLogs = listOf(
                        SetLogUi(repsCompleted = 8, weightUsed = 0.0),
                        SetLogUi(repsCompleted = 6, weightUsed = 0.0),
                        SetLogUi(repsCompleted = 5, weightUsed = 0.0),
                    )
                )
            )
        )
    }
}