package com.teamnotfound.airise.workout

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExWorkoutViewModel : WorkoutViewModelContract {

    private val _ui = MutableStateFlow(
        WorkoutUiState(
            items = listOf(
                WorkoutRow("1", "Bench Press"),
                WorkoutRow("2", "Squat"),
                WorkoutRow("3", "Deadlift")
            )
        )
    )
    override val uiState: StateFlow<WorkoutUiState> = _ui

    override fun refresh() {  }

    override fun changeSet(workoutId: String, index: Int, reps: Int?, weight: Int?) {
        val list = _ui.value.items.toMutableList()
        val i = list.indexOfFirst { it.id == workoutId }
        if (i == -1) return

        val row = list[i]
        val sets = row.sets.toMutableList()
        if (index in sets.indices) {
            val s = sets[index]
            sets[index] = s.copy(
                reps = reps ?: s.reps,
                weightLbs = weight ?: s.weightLbs
            )
        }
        list[i] = row.copy(sets = sets)
        _ui.value = _ui.value.copy(items = list)
    }

    override fun logAll() {
        println("LOG (fake): ${_ui.value.items}")
    }
}
