package com.teamnotfound.airise.health

import com.khealth.KHealth

interface HealthData {
    val activeCalories : Int
    val steps: Int
    val heartRate: Int
    // Add more fields as needed
}

expect class HealthDataProvider(kHealth: KHealth) {
    suspend fun requestPermissions(): Boolean
    suspend fun getHealthData(): HealthData
    suspend fun writeHealthData() : Boolean
    // Add more functions as needed
}