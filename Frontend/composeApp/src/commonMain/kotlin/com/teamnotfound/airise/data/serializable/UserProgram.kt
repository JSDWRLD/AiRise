package com.teamnotfound.airise.data.serializable

import kotlinx.serialization.Serializable

/*
// This is a example of what is returned when getting UserProgramDoc
{
  "id": "66f4a37e57c2d93d5b8f9a21",
  "firebaseUid": "abc123",
  "program": {
    "templateName": "3-Day Full Body Strength (Gym)",
    "days": 3,
    "type": "Gym",
    "schedule": [
      {
        "dayIndex": 1,
        "dayName": "Monday",
        "focus": "Upper Push (Gym)",
        "exercises": [
          {
            "name": "Barbell Bench Press",
            "sets": 4,
            "targetReps": "6-10",
            "repsCompleted": 0,
            "weight": { "value": 0, "unit": "lbs" }
          }
        ]
      }
    ],
    "createdAtUtc": "2025-09-21T16:00:00Z",
    "updatedAtUtc": "2025-09-21T16:00:00Z"
  },
  "lastUpdatedUtc": "2025-09-21T16:00:00Z"
}
 */

@Serializable
data class UserExerciseWeight(
    val value: Int = 0,
    val unit: String = "lbs"
)

@Serializable
data class UserExerciseEntry(
    val name: String = "",
    val sets: Int = 0,
    val targetReps: String = "",
    val repsCompleted: Int = 0,
    val weight: UserExerciseWeight = UserExerciseWeight()
)

@Serializable
data class UserProgramDay(
    val dayIndex: Int = 0,
    val dayName: String = "",
    val focus: String = "",
    val exercises: List<UserExerciseEntry> = emptyList()
)

@Serializable
data class UserProgram(
    val templateName: String = "",
    val days: Int = 0,
    val type: ProgramType = ProgramType.Bodyweight, // mirror your enum
    val schedule: List<UserProgramDay> = emptyList(),
    val createdAtUtc: String = "",
    val updatedAtUtc: String = ""
)

@Serializable
data class UserProgramDoc(
    val id: String? = null,
    val firebaseUid: String = "",
    val program: UserProgram = UserProgram(),
    val lastUpdatedUtc: String = ""
)

@Serializable
enum class ProgramType {
    Bodyweight,
    HomeDumbbell,
    Gym
}