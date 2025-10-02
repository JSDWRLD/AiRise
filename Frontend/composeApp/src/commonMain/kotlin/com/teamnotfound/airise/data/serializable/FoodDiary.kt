package com.teamnotfound.airise.data.serializable

import kotlinx.serialization.Serializable

@Serializable
data class FoodDiaryMonth(
    val id: String? = null, // Nullable since the backend assigns it
    val userId: String,
    val year: Int,
    val month: Int,
    val days: List<DiaryDay?> = List(31) { null } // Always 31 slots, null if unused
)

@Serializable
data class DiaryDay(
    val day: Int,
    val totalCalories: Double = 0.0,
    val meals: Meals = Meals()
)

@Serializable
data class Meals(
    val breakfast: List<FoodEntry> = emptyList(),
    val lunch: List<FoodEntry> = emptyList(),
    val dinner: List<FoodEntry> = emptyList()
)

@Serializable
data class FoodEntry(
    val id: String, // Unique ID assigned by the backend
    val name: String,
    val calories: Double,
    val fats: Double,
    val carbs: Double,
    val proteins: Double
)