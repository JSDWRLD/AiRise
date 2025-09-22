import com.teamnotfound.airise.workout.WorkoutUiState
import com.teamnotfound.airise.workout.WorkoutViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkoutViewModelTest {
    @Test
    fun test() {
        assertTrue(true)
    }

    @Test
    fun `changeSet updates reps and weight`() = runTest {
        val viewModel = WorkoutViewModel()

        val exerciseId = "ex_456"
        val setIndex = 1

        viewModel.changeSet(exerciseId, setIndex, reps = 12, weight = 55.0)

        val updatedSet = (viewModel.uiState.value as WorkoutUiState.Success)
            .workoutPlan.exercises
            .first { it.exerciseTemplateId == exerciseId }
            .setLogs[setIndex]

        assertEquals(12, updatedSet.repsCompleted)
        assertEquals(55.0, updatedSet.weightUsed)
    }

    @Test
    fun `changeExerciseNotes updates notes`() = runTest {
        val viewModel = WorkoutViewModel()

        val exerciseId = "ex_456"
        val newNotes = "New notes for this exercise."

        viewModel.changeExerciseNotes(exerciseId, newNotes)

        val updatedNotes = (viewModel.uiState.value as WorkoutUiState.Success)
            .workoutPlan.exercises
            .first { it.exerciseTemplateId == exerciseId }
            .notes

        assertEquals(newNotes, updatedNotes)
    }

}
