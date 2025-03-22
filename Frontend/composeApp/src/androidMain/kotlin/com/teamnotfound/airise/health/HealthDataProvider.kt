package com.teamnotfound.airise.health

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.khealth.KHealth
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import java.util.Date
import androidx.activity.ComponentActivity
import com.khealth.KHEither
import com.khealth.KHPermission
import com.khealth.KHReadRequest
import com.khealth.KHUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

actual class HealthDataProvider(private val activity: ComponentActivity) {

    private val kHealth = KHealth(activity)

    private val permissions = arrayOf(
        KHPermission.StepCount(read = true),
        KHPermission.HeartRate(read = true),
        KHPermission.ActiveCaloriesBurned(read = true),
        KHPermission.BasalMetabolicRate(read = true),
        KHPermission.BloodGlucose(read = true),
        KHPermission.BloodPressure(readSystolic = true,
            writeSystolic = true,
            readDiastolic = true,
            writeDiastolic = true),
        KHPermission.Distance(read = true),
        KHPermission.Weight(read = true),
        KHPermission.Height(read = true),
        KHPermission.SleepSession(read = true)
    )

    actual suspend fun sampleRequestAllPerms(kHealth: KHealth) {
        coroutineScope.launch {
            try {
                val response = kHealth.requestPermissions(*permissions)
                println("Request Permissions Response: $response")
            } catch (t: Throwable) {
                println("Request Permissions Error: $t")
            }
        }
    }

    actual suspend fun requestPermissions(): Boolean {
        return withContext(Dispatchers.Main) {
            kHealth.requestPermissions(*permissions)
        }
    }

    actual suspend fun getHealthData(): HealthData = withContext(Dispatchers.Default) {
        val startTime = Clock.System.now().minus(1.days)
        val endTime = Clock.System.now()

        val stepsRecords = kHealth.readRecords(KHReadRequest.StepCount(startTime, endTime))
        val heartRateRecords = kHealth.readRecords(KHReadRequest.HeartRate(startTime, endTime))

        val steps = stepsRecords.sumOf { it.count.toInt() }
        val heartRate = heartRateRecords.map { it.rate }.average().toIntOrNull() ?: 0

        return@withContext object : HealthData {
            override val steps = steps
            override val heartRate = heartRate
        }
    }
}

@Composable
actual fun rememberHealthDataProvider(): HealthDataProvider {
    val activity = LocalContext.current as ComponentActivity
    return remember { HealthDataProvider(activity) }
}