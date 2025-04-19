package com.teamnotfound.airise.data.serializable

import kotlinx.serialization.Serializable

@Serializable
data class HealthData(
    val id: String = "",
    val userDataHealthId: String = "",
    val sleep: Float = 0f,
    val steps: Int = 0,
    val caloriesBurned: Int = 0,
    val avgHeartRate: Int = 0,
    val workout: Int = 0,
    val hydration: Float = 0f
)
