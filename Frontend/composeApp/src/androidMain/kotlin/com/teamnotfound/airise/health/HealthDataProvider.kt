package com.teamnotfound.airise.health

import com.khealth.KHealth
import com.khealth.KHHeartRateSample
import com.khealth.KHPermission
import com.khealth.KHReadRequest
import com.khealth.KHRecord
import com.khealth.KHUnit
import com.khealth.KHSleepStage
import com.khealth.KHSleepStageSample
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.Instant
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.Duration.Companion.hours

actual class HealthDataProvider actual constructor(private val kHealth: KHealth) {

    private val SLEEPING_STAGES = setOf(
        KHSleepStage.Sleeping,
        KHSleepStage.REM,
        KHSleepStage.Deep,
        KHSleepStage.Light
    )

    // Sum only “sleep” stages and return hours (Double)
    private fun KHRecord.SleepSession.totalSleepHours(): Double {
        val totalMillis = samples
            .asSequence()
            .filter { it.stage in SLEEPING_STAGES }
            .sumOf { s ->
                val start = minOf(s.startTime, s.endTime)
                val end = maxOf(s.startTime, s.endTime)
                (end - start).inWholeMilliseconds
            }

        return totalMillis
            .toDuration(DurationUnit.MILLISECONDS)
            .toDouble(DurationUnit.HOURS)
    }

    // Pick the session that ends most recently (covers across-midnight nights)
    private fun List<KHRecord>.mostRecentSleepSessionOrNull(): KHRecord.SleepSession? =
        this.filterIsInstance<KHRecord.SleepSession>()
            .maxByOrNull { sess ->
                sess.samples.maxOfOrNull { it.endTime } ?: Instant.DISTANT_PAST
            }

    // Request perm logic
    actual suspend fun requestPermissions(): Boolean {

        val permissionResponse: Set<KHPermission> = kHealth.requestPermissions(
            KHPermission.ActiveCaloriesBurned(read = true, write = true),
            KHPermission.HeartRate(read = true, write = true),
            KHPermission.StepCount(read = true, write = true),
            KHPermission.SleepSession(read = true, write = true)
            // Add as many requests as you want
        )

        // Return true if at least one permission was granted
        return permissionResponse.isNotEmpty()
    }

    actual suspend fun getHealthData(): IHealthData = withContext(Dispatchers.Default) {
        // Init start and end time when fun is called
        val startTime = Clock.System.now().minus(1.days)
        val endTime = Clock.System.now()
        val sleepStart = Clock.System.now() - 36.hours

        // Reading records
        val activeCaloriesRecord = kHealth.readRecords(KHReadRequest.ActiveCaloriesBurned(KHUnit.Energy.Calorie, startTime, endTime))
        val stepRecord = kHealth.readRecords(KHReadRequest.StepCount(startTime, endTime))
        val hydrationRecord = kHealth.readRecords(KHReadRequest.Hydration(unit = KHUnit.Volume.FluidOunceUS, startTime, endTime))
        val sleepRecord = kHealth.readRecords(KHReadRequest.SleepSession(sleepStart, endTime))



        // Calculating Steps
        val steps = stepRecord.sumOf {
            (it as? KHRecord.StepCount)?.count?.toInt() ?: 0
        }

        // Flatten heart rate samples and calculate the average BPM
        val hydration = hydrationRecord.sumOf {
            (it as? KHRecord.Hydration)?.value ?: 0.0
        }

        // Calculating active calories
        val activeCalories = activeCaloriesRecord.sumOf {
            (it as? KHRecord.ActiveCaloriesBurned)?.value?.toInt() ?: 0
        }

        // Last night’s sleep hours
        val mostRecentSession = sleepRecord.mostRecentSleepSessionOrNull()
        val sleepHours = mostRecentSession?.totalSleepHours() ?: 0.0

        // Passing to Health Data object for UI
        object : IHealthData {
            override val caloriesBurned = activeCalories
            override val steps = steps
            override val sleep = sleepHours
            override val hydration = hydration
        }
    }

    // Writing health data using Health Connect
    actual suspend fun writeHealthData(): Boolean = withContext(Dispatchers.Default) {

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

        // Create a small sleep sample (30 minutes) for testing increments
        val sleepStartTime = Clock.System.now().minus(2.hours)
        val sleepEndTime = Clock.System.now().minus(1.hours + 30.minutes)

        val sampleSleep = KHRecord.SleepSession(
            samples = listOf(
                // Just 30 minutes of sleep for small increment testing
                KHSleepStageSample(
                    stage = KHSleepStage.Light,
                    startTime = sleepStartTime,
                    endTime = sleepEndTime,
                )
            ),
        )
        // Add more sample data here

        val result = kHealth.writeRecords(sampleSteps, sampleHeartRate, sampleActiveCalories, sampleSleep)
        return@withContext result is com.khealth.KHWriteResponse.Success
    }
}