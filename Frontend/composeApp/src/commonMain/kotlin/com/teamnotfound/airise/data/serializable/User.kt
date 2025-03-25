package com.teamnotfound.airise.data.serializable

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null, // MongoDB ObjectId as String
    val firebaseUid: String,
    val streak: Int = 0,
    val userData: String? = null, // References to other documents
    val userSettings: String? = null,
    val goals: String? = null,
    val workouts: String? = null,
    val mealPlan: String? = null,
    val progress: String? = null,
    val challenges: String? = null,
    val healthData: String? = null,
    val chatHistory: String? = null
)