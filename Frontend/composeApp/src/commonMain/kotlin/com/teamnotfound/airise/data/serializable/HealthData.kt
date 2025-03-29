package com.teamnotfound.airise.data.serializable

import kotlinx.serialization.Serializable

@Serializable
data class HealthData(
    val caloriesBurned: Int? = 0,
    val steps: Int? = 0,
    val avgHeartRate: Int? = 0
)
