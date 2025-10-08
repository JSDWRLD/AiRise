package com.teamnotfound.airise.health

import com.khealth.KHealth

interface IHealthData {
    val caloriesBurned: Int
    val steps: Int
    val hydration: Double
    val sleep: Double
}

expect class HealthDataProvider(kHealth: KHealth) {
    suspend fun requestPermissions(): Boolean
    suspend fun getHealthData(): IHealthData
    suspend fun writeHealthData() : Boolean
    // Add more functions as needed
}