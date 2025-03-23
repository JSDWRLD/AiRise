package com.teamnotfound.airise.health

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.khealth.KHPermission
import com.khealth.KHReadRequest
import com.khealth.KHealth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days


actual class HealthDataProvider {

    private val kHealth = KHealth()

    actual suspend fun requestPermissions(): Boolean {
        val permissions = setOf(
            KHPermission.StepCount(read = true),
            KHPermission.HeartRate(read = true)
        )

        val result = kHealth.requestPermissions(*permissions.toTypedArray())

        return result.any { it is KHPermission.HeartRate && it.read == true }
    }

    actual suspend fun getHealthData(): HealthData = withContext(Dispatchers.Default) {
        val startTime = Clock.System.now().minus(1.days)
        val endTime = Clock.System.now()

        val steps = kHealth.readRecords(KHReadRequest.StepCount(startTime, endTime))
        val heartRate = kHealth.readRecords(KHReadRequest.HeartRate(startTime, endTime))

        val totalSteps = steps.toString().toInt()
        val totalHeartRate = heartRate.toString().toInt()

        object : HealthData {
            override val steps = totalSteps
            override val heartRate = totalHeartRate
        }
    }
}

@Composable
actual fun rememberHealthDataProvider(): HealthDataProvider {
    return remember { HealthDataProvider() }
}