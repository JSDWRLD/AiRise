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
import kotlinx.coroutines.flow.update
import notifications.WorkoutReminderUseCase

class WorkoutViewModel(
    private val userRepository: UserRepository,
    private val reminder: WorkoutReminderUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Loading)
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private val _activeDayIndex = MutableStateFlow<Int?>(null)
    val activeDayIndex: StateFlow<Int?> = _activeDayIndex.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = WorkoutUiState.Loading
            try {
                // TODO: Replace with repository call
                val hardcoded = createHardcodedProgramDoc()
                _uiState.value = WorkoutUiState.Success(hardcoded)

                // Pick a default active day (first in schedule) and schedule its reminder.
                val first = hardcoded.program.schedule.firstOrNull()
                if (first != null) {
                    _activeDayIndex.value = first.dayIndex
                    reminder.cancelActive()
                    reminder.scheduleActive(
                        title = "Workout: ${first.dayName}",
                        body  = first.focus
                    )
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
        val updatedProgramDoc = state.programDoc
        println("Logging data: ${state.programDoc}")
        reminder.cancelActive()
        // TODO: call function
    }


    fun setActiveDay(dayIndex: Int, dayTitle: String, dayFocus: String) {
        val prev = _activeDayIndex.value
        _activeDayIndex.value = dayIndex

        // Cancel previous one-shot notification and schedule a new one for the active day
        reminder.cancelActive()
        reminder.scheduleActive(
            title = "Workout: $dayTitle",
            body = dayFocus
        )
    }

    /** Call this after the user logs/completes the active workout. */
    fun onWorkoutLogged() {
        reminder.cancelActive()
    }


    private fun createHardcodedProgramDoc(): UserProgramDoc {
        return UserProgramDoc(
            id = "66f4a37e57c2d93d5b8f9a21",
            firebaseUid = "abc123",
            program = UserProgram(
                templateName = "3-Day Full Body Strength (Gym)",
                days = 3,
                type = ProgramType.Gym,
                schedule = listOf(
                    UserProgramDay(
                        dayIndex = 1,
                        dayName = "Monday",
                        focus = "Upper Push (Gym)",
                        exercises = listOf(
                            UserExerciseEntry(
                                name = "Barbell Bench Press",
                                sets = 4,
                                targetReps = "6-10",
                                repsCompleted = 0,
                                weight = UserExerciseWeight(value = 135, unit = "lbs")
                            ),
                            UserExerciseEntry(
                                name = "Overhead Press",
                                sets = 3,
                                targetReps = "8-10",
                                repsCompleted = 0,
                                weight = UserExerciseWeight(value = 95, unit = "lbs")
                            ),
                            UserExerciseEntry(
                                name = "Dumbbell Lateral Raises",
                                sets = 3,
                                targetReps = "12-15",
                                repsCompleted = 0,
                                weight = UserExerciseWeight(value = 15, unit = "lbs")
                            )
                        )
                    ),
                    UserProgramDay(
                        dayIndex = 2,
                        dayName = "Wednesday",
                        focus = "Upper Pull (Gym)",
                        exercises = listOf(
                            UserExerciseEntry(
                                name = "Pull-Ups",
                                sets = 4,
                                targetReps = "To Failure",
                                repsCompleted = 0,
                                weight = UserExerciseWeight(value = 0, unit = "lbs")
                            ),
                            UserExerciseEntry(
                                name = "Barbell Rows",
                                sets = 4,
                                targetReps = "8-12",
                                repsCompleted = 0,
                                weight = UserExerciseWeight(value = 115, unit = "lbs")
                            ),
                            UserExerciseEntry(
                                name = "Face Pulls",
                                sets = 3,
                                targetReps = "12-15",
                                repsCompleted = 0,
                                weight = UserExerciseWeight(value = 40, unit = "lbs")
                            )
                        )
                    ),
                    UserProgramDay(
                        dayIndex = 3,
                        dayName = "Friday",
                        focus = "Lower Body (Gym)",
                        exercises = listOf(
                            UserExerciseEntry(
                                name = "Barbell Back Squat",
                                sets = 4,
                                targetReps = "6-8",
                                repsCompleted = 0,
                                weight = UserExerciseWeight(value = 185, unit = "lbs")
                            ),
                            UserExerciseEntry(
                                name = "Romanian Deadlift",
                                sets = 4,
                                targetReps = "8-10",
                                repsCompleted = 0,
                                weight = UserExerciseWeight(value = 135, unit = "lbs")
                            ),
                            UserExerciseEntry(
                                name = "Walking Lunges",
                                sets = 3,
                                targetReps = "12 steps/leg",
                                repsCompleted = 0,
                                weight = UserExerciseWeight(value = 40, unit = "lbs")
                            ),
                            UserExerciseEntry(
                                name = "Calf Raises",
                                sets = 3,
                                targetReps = "15-20",
                                repsCompleted = 0,
                                weight = UserExerciseWeight(value = 90, unit = "lbs")
                            )
                        )
                    )
                ),
                createdAtUtc = "2025-09-21T16:00:00Z",
                updatedAtUtc = "2025-09-21T16:00:00Z"
            ),
            lastUpdatedUtc = "2025-09-21T16:00:00Z"
        )
    }
}
