package com.teamnotfound.airise.data.serializable

import kotlinx.serialization.Serializable

@Serializable
data class DailyProgressData(
    val caloriesProgress: Float = 0f,
    val sleepProgress: Float = 0f,
    val hydrationProgress: Float = 0f,
    val totalProgress: Float = 0f
)
