package com.teamnotfound.airise.data.serializable

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class HealthData(
    val sleep: Double = 0.0, //hours
    val steps: Int = 0,
    val caloriesBurned: Int = 0,
    val caloriesEaten: Int = 0,
    val caloriesTarget: Int = 0,
    val hydration: Double = 0.0,
    val hydrationTarget: Double = 0.0, //ounces
    var localDate: LocalDate? = null
)
