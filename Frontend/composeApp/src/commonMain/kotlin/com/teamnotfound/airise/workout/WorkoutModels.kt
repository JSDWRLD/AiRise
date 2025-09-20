package com.teamnotfound.airise.workout

data class Exercise(
    val exerciseTemplateId: String,
    val name: String,
    val setsPlanned: Int,
    val repsPlanned: String?,
    val weightValuePlanned: Double?,
    val setLogs: List<SetLog> = emptyList()
)

data class SetLog(
    val weightUsed: Double?,
    val repsCompleted: Int?,
    val notes: String?
)


data class WorkoutRow(
    val id: String,
    val name: String,
    val plannedReps: String?,
    val plannedWeightLbs: Double?,
    val sets: List<WorkoutSet>,
    val exerciseNotes: String = ""
)

data class WorkoutSet(
    val index: Int,
    val repsCompleted: Int,
    val weightUsedLbs: Double?,
    val notes: String
)

fun List<Exercise>.toWorkoutRows(): List<WorkoutRow> = map { ex ->
    val sets = (0 until ex.setsPlanned).map { idx ->
        val log = ex.setLogs.getOrNull(idx)
        WorkoutSet(
            index = idx,
            repsCompleted = log?.repsCompleted ?: 0,
            weightUsedLbs = log?.weightUsed,
            notes = log?.notes.orEmpty()
        )
    }
    WorkoutRow(
        id = ex.exerciseTemplateId,
        name = ex.name,
        plannedReps = ex.repsPlanned,
        plannedWeightLbs = ex.weightValuePlanned,
        sets = sets,
        exerciseNotes = ""
    )
}
