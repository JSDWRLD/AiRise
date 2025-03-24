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

actual class HealthDataProvider(private val activity: ComponentActivity) {

    private val kHealth = KHealth(activity)

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
    val activity = LocalContext.current as ComponentActivity
    return remember { HealthDataProvider(activity) }
}