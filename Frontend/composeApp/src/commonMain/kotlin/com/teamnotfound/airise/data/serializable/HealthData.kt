package com.teamnotfound.airise.data.serializable

import kotlinx.serialization.Serializable

@Serializable
data class HealthData(
    val id: String = "",
    val userDataHealthId: String = "",
    val sleepHours: Double = 0.0,
    val steps: Int = 0,
    val caloriesBurned: Int = 0,
    val avgHeartRate: Int = 0,
    val workout: Int = 0,
    val hydration: Float = 0f,
)
