package com.teamnotfound.airise.health

import androidx.compose.runtime.Composable

interface HealthData {
    val steps: Int
    val heartRate: Int
    // Add more fields as needed
}

expect class HealthDataProvider {
    suspend fun requestPermissions(): Boolean
    suspend fun getHealthData(): HealthData
}

@Composable
expect fun rememberHealthDataProvider(): HealthDataProvider