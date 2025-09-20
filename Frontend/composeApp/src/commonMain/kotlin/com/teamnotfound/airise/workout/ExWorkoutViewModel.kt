package com.teamnotfound.airise.workout

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ExWorkoutViewModel : WorkoutViewModelContract {
    private val _ui = MutableStateFlow(
        WorkoutUiState(
            isLoading = false,
            items = listOf(
                Exercise(
                    exerciseTemplateId = "ex1",
                    name = "Bench Press",
                    setsPlanned = 3,
                    repsPlanned = "10",
                    weightValuePlanned = 135.0,
                    setLogs = listOf(
                        SetLog(135.0, 10, null),
                        SetLog(145.0, 8, null),
                        SetLog(155.0, 6, null)
                    )
                ),
                Exercise(
                    exerciseTemplateId = "ex2",
                    name = "Squats",
                    setsPlanned = 2,
                    repsPlanned = "12",
                    weightValuePlanned = 185.0,
                    setLogs = listOf(
                        SetLog(185.0, 12, null),
                        SetLog(205.0, 10, null)
                    )
                ),
                Exercise(
                    exerciseTemplateId = "ex3",
                    name = "Pull Ups",
                    setsPlanned = 2,
                    repsPlanned = "8",
                    weightValuePlanned = 0.0,
                    setLogs = listOf(
                        SetLog(0.0, 8, null),
                        SetLog(0.0, 6, null)
                    )
                )
            ).toWorkoutRows()
        )
    )
    override val uiState: StateFlow<WorkoutUiState> = _ui

    override fun refresh() {  }

    override fun changeSet(workoutId: String, index: Int, reps: Int?, weight: Double?) {
        _ui.update { state ->
            val newItems = state.items.map { row ->
                if (row.id != workoutId) return@map row
                val newSets = row.sets.map { s ->
                    if (s.index != index) s else s.copy(
                        repsCompleted = reps ?: s.repsCompleted,
                        weightUsedLbs = weight ?: s.weightUsedLbs
                    )
                }
                row.copy(sets = newSets)
            }
            state.copy(items = newItems)
        }
    }

    override fun changeExerciseNotes(workoutId: String, notes: String) {
        _ui.update { state ->
            val newItems = state.items.map { row ->
                if (row.id != workoutId) row else row.copy(exerciseNotes = notes)
            }
            state.copy(items = newItems)
        }
    }

    override fun logAll() {

    }
}
