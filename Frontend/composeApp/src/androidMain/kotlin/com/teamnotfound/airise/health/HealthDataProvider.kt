package com.teamnotfound.airise.health

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.khealth.KHealth
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import com.khealth.KHPermission
import com.khealth.KHReadRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

actual class HealthDataProvider actual constructor(private val kHealth: KHealth) {

    // Request perm logic
    actual suspend fun requestPermissions(): Boolean {

        val permissionResponse: Set<KHPermission> = kHealth.requestPermissions(
            KHPermission.ActiveCaloriesBurned(read = true, write = false),
            KHPermission.HeartRate(read = true, write = false),
            KHPermission.StepCount(read = true, write = false)
            // Add as many requests as you want
        )

        // Return true if at least one permission was granted
        return permissionResponse.isNotEmpty()
    }

    actual suspend fun getHealthData(): HealthData = withContext(Dispatchers.Default) {
        // Init Start and End time
        val startTime = Clock.System.now().minus(1.days)
        val endTime = Clock.System.now()

        // Reading Records
        val steps = kHealth.readRecords(KHReadRequest.StepCount(startTime, endTime))
        val heartRate = kHealth.readRecords(KHReadRequest.HeartRate(startTime, endTime))

        // changing KHRecord to int
        val totalSteps = steps.toString().toInt()
        val totalHeartRate = heartRate.toString().toInt()

        // Passing to Health Data object for UI
        object : HealthData {
            override val steps = totalSteps
            override val heartRate = totalHeartRate
        }
    }
}

/*
@Composable
actual fun rememberHealthDataProvider(): HealthDataProvider {
    val context = LocalContext.current as ComponentActivity
    val container = (context.application as AiRiseApp).container // or however you're managing DI
    return remember { HealthDataProvider(container.kHealth) }
}
*/