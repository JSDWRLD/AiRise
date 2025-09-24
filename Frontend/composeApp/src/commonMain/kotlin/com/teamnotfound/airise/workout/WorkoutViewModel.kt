package com.teamnotfound.airise.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.repository.UserRepository
import com.teamnotfound.airise.data.serializable.ProgramType
import com.teamnotfound.airise.data.serializable.UserExerciseEntry
import com.teamnotfound.airise.data.serializable.UserExerciseWeight
import com.teamnotfound.airise.data.serializable.UserProgram
import com.teamnotfound.airise.data.serializable.UserProgramDay
import com.teamnotfound.airise.data.serializable.UserProgramDoc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Loading)
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = WorkoutUiState.Loading
            try {
                val result = userRepository.getUserProgram()
                when (result) {
                    is com.teamnotfound.airise.data.network.Result.Success -> {
                        _uiState.value = WorkoutUiState.Success(result.data)
                    }
                    is com.teamnotfound.airise.data.network.Result.Error -> {
                        _uiState.value = WorkoutUiState.Error(
                            Exception("Failed to load program: ${result.error}")
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = WorkoutUiState.Error(e)
            }
        }
    }

    fun changeSet(dayIndex: Int, exerciseName: String, reps: Int?, weight: Double?) {
        val state = _uiState.value as? WorkoutUiState.Success ?: return
        val programDoc = state.programDoc

        val updatedSchedule = programDoc.program.schedule.map { day ->
            if (day.dayIndex == dayIndex) {
                day.copy(
                    exercises = day.exercises.map { ex ->
                        if (ex.name == exerciseName) {
                            val newReps = reps ?: ex.repsCompleted
                            val newWeight = weight ?: ex.weight.value
                            ex.copy(
                                repsCompleted = newReps,
                                weight = ex.weight.copy(value = newWeight.toInt())
                            )
                        } else ex
                    }
                )
            } else day
        }

        val updatedDoc = programDoc.copy(program = programDoc.program.copy(schedule = updatedSchedule))
        _uiState.value = WorkoutUiState.Success(updatedDoc)
    }

    fun logAll() {
        val state = _uiState.value as? WorkoutUiState.Success ?: return
        val programDoc = state.programDoc

        viewModelScope.launch {
            try {
                val result = userRepository.updateUserProgram(programDoc.program)
                when (result) {
                    is com.teamnotfound.airise.data.network.Result.Success -> {
                        println("Program saved successfully")
                        // Optionally show success message to user
                    }
                    is com.teamnotfound.airise.data.network.Result.Error -> {
                        println("Failed to save program: ${result.error}")
                        // Handle error - maybe show error message to user
                    }
                }
            } catch (e: Exception) {
                println("Error saving program: ${e.message}")
            }
        }
    }

}
