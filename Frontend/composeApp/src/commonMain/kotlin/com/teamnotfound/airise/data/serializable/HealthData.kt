package com.teamnotfound.airise.data.serializable

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class HealthData(
    val sleep: Double = 0.0, //hours
    val steps: Int = 0,
    val caloriesBurned: Int = 0,
    val caloriesEaten: Int = 0,
    val caloriesTarget: Int = 2000,
    val hydration: Double = 0.0,
    val hydrationTarget: Double = 104.0, //ounces
    val localDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
)
