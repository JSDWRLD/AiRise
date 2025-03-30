package com.teamnotfound.airise.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "summaries")
data class SummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,

    val firstName: String,
    val lastName: String,
    val middleName: String,

    val workoutGoal: String,
    val currentFitnessLevel: String,
    val workoutLength: Int,

    val equipmentAccess: String,


    val daysSelected: String,

    val workoutTimes: String,

    val dietaryGoal: String,

    val hasInjuries: Boolean,
    val injuryDescription: String?,       // if true- text

    // Measurements
    val height: Double,
    val isHeightMetric: Boolean,
    val weight: Double,
    val isWeightMetric: Boolean,

    val dateOfBirth: Long,

    val preferredActivityLevel: String,

    // Timestamp
    val timestamp: Long
)
