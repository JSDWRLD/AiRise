package com.teamnotfound.airise.data.serializable

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class HealthData(
    val sleep: Double? = null, //hours
    val steps: Int? = null,
    val caloriesBurned: Int? = null,
    val caloriesEaten: Int? = null,
    val caloriesTarget: Int? = null,
    val hydration: Double? = null,
    val hydrationTarget: Double? = null, //ounces
    var localDate: LocalDate? = null
)
