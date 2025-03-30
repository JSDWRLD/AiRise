package com.teamnotfound.airise.health

import com.khealth.KHealth
import com.khealth.KHHeartRateSample
import com.khealth.KHPermission
import com.khealth.KHReadRequest
import com.khealth.KHRecord
import com.khealth.KHUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

actual class HealthDataProvider actual constructor(private val kHealth: KHealth) {

    // Request perm logic
    actual suspend fun requestPermissions(): Boolean {

        val permissionResponse: Set<KHPermission> = kHealth.requestPermissions(
            KHPermission.ActiveCaloriesBurned(read = true, write = true),
            KHPermission.HeartRate(read = true, write = true),
            KHPermission.StepCount(read = true, write = true)
            // Add as many requests as you want
        )

        // Return true if at least one permission was granted
        return permissionResponse.isNotEmpty()
    }

    actual suspend fun getHealthData(): HealthData = withContext(Dispatchers.Default) {
        // Init start and end time when fun is called
        val startTime = Clock.System.now().minus(1.days)
        val endTime = Clock.System.now()

        // Reading records
        val activeCaloriesRecord = kHealth.readRecords(KHReadRequest.ActiveCaloriesBurned(KHUnit.Energy.Calorie, startTime, endTime))
        val stepRecord = kHealth.readRecords(KHReadRequest.StepCount(startTime, endTime))
        val heartRateRecord = kHealth.readRecords(KHReadRequest.HeartRate(startTime, endTime))

        
        // Calculating Steps
        val steps = stepRecord.sumOf {
            (it as? KHRecord.StepCount)?.count?.toInt() ?: 0
        }

        // Flatten heart rate samples and calculate the average BPM
        val heartRateSamples = heartRateRecord
            .filterIsInstance<KHRecord.HeartRate>()
            .flatMap { it.samples }
            .map { it.beatsPerMinute }

        val heartRate = if (heartRateSamples.isNotEmpty())
            heartRateSamples.average().toInt()
        else 0

        // Calculating active calories
        val activeCalories = activeCaloriesRecord.sumOf {
            (it as? KHRecord.ActiveCaloriesBurned)?.value?.toInt() ?: 0
        }

        // Passing to Health Data object for UI
        object : HealthData {
            override val activeCalories = activeCalories
            override val steps = steps
            override val heartRate = heartRate
        }
    }

    // Writing health data using Health Connect
    actual suspend fun writeHealthData(): Boolean = withContext(Dispatchers.IO) {

        val sampleActiveCalories = KHRecord.ActiveCaloriesBurned(
            unit = KHUnit.Energy.Calorie,
            value = 3.0,
            startTime = Clock.System.now().minus(10.minutes),
            endTime = Clock.System.now(),
        )

        val sampleSteps = KHRecord.StepCount(
            count = 24,
            startTime = Clock.System.now().minus(2.minutes),
            endTime = Clock.System.now(),
        )

        val sampleHeartRate = KHRecord.HeartRate(
            samples = listOf(
                KHHeartRateSample(beatsPerMinute = 135, time = Clock.System.now())
            ),
        )
        // Add more sample data here

        // Make sure to include samples here!
        val result = kHealth.writeRecords(sampleActiveCalories, sampleHeartRate, sampleSteps)
        return@withContext result is com.khealth.KHWriteResponse.Success
    }
}