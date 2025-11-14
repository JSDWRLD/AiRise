package com.teamnotfound.airise.health

import com.khealth.KHealth
import com.khealth.KHPermission
import com.khealth.KHReadRequest
import com.khealth.KHRecord
import com.khealth.KHUnit
import com.khealth.KHSleepStage
import com.khealth.KHSleepStageSample
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.toDuration

// Optional: lets your ViewModel distinguish "no access" from other errors
class HealthAccessException(message: String) : Exception(message)

actual class HealthDataProvider actual constructor(private val kHealth: KHealth) {

    // ---- Permissions (require ALL) ----
    private val REQUIRED_PERMS = arrayOf(
        KHPermission.ActiveCaloriesBurned(read = true, write = true),
        KHPermission.StepCount(read = true, write = true),
        KHPermission.SleepSession(read = true, write = true)
        // NOTE: Hydration intentionally excluded (managed manually)
    )
    private val REQUIRED_READ_PERMS = arrayOf(
        KHPermission.ActiveCaloriesBurned(read = true, write = false),
        KHPermission.StepCount(read = true, write = false),
        KHPermission.SleepSession(read = true, write = false)
    )

    // ---- Sleep helpers ----
    private val SLEEPING_STAGES = setOf(
        KHSleepStage.Sleeping,
        KHSleepStage.REM,
        KHSleepStage.Deep,
        KHSleepStage.Light
    )

    // Sum only "sleep" stages and return hours (Double)
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

    // ---- Permission APIs (actuals) ----
    actual suspend fun requestPermissions(): Boolean {
        val granted: Set<KHPermission> = kHealth.requestPermissions(*REQUIRED_PERMS)
        // success ONLY if every required permission was granted
        return granted.containsAll(REQUIRED_PERMS.toSet())
    }

    // ---- Read API (actual) ----
    actual suspend fun getHealthData(): IHealthData = withContext(Dispatchers.Default) {
        if (!kHealth.isHealthStoreAvailable) {
            throw HealthAccessException("Health store unavailable on this device")
        }
        if (!hasAllReadPermissions()) {
            throw HealthAccessException("Health permissions not granted")
        }

        val now = Clock.System.now()
        val startTime = now.minus(1.days)
        val endTime = now
        val sleepStart = now - 36.hours

        // NOTE: Hydration is NOT fetched (kept manual)
        val activeCaloriesRecord = kHealth.readRecords(
            KHReadRequest.ActiveCaloriesBurned(KHUnit.Energy.Calorie, startTime, endTime)
        )
        val stepRecord = kHealth.readRecords(KHReadRequest.StepCount(startTime, endTime))
        val sleepRecord = kHealth.readRecords(KHReadRequest.SleepSession(sleepStart, endTime))

        val steps = stepRecord.sumOf { (it as? KHRecord.StepCount)?.count?.toInt() ?: 0 }
        val activeCalories = activeCaloriesRecord.sumOf { (it as? KHRecord.ActiveCaloriesBurned)?.value?.toInt() ?: 0 }
        val mostRecentSession = sleepRecord.mostRecentSleepSessionOrNull()
        val sleepHours = mostRecentSession?.totalSleepHours() ?: 0.0

        object : IHealthData {
            override val caloriesBurned = activeCalories
            override val steps = steps
            override val sleep = sleepHours
            override val hydration = 0.0 // manual
        }
    }

    // ---- Write API (actual) ----
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

        // Create a small sleep sample (30 minutes) for testing increments
        val sleepStartTime = Clock.System.now().minus(2.hours)
        val sleepEndTime = Clock.System.now().minus(1.hours + 30.minutes)

        val sampleSleep = KHRecord.SleepSession(
            samples = listOf(
                KHSleepStageSample(
                    stage = KHSleepStage.Light,
                    startTime = sleepStartTime,
                    endTime = sleepEndTime,
                )
            ),
        )

        val result = kHealth.writeRecords(sampleSteps, sampleActiveCalories, sampleSleep)
        result is com.khealth.KHWriteResponse.Success
    }

    // ---- Helpers ----
    private suspend fun hasAllReadPermissions(): Boolean {
        val granted = kHealth.checkPermissions(*REQUIRED_READ_PERMS)
        return granted.containsAll(REQUIRED_READ_PERMS.toSet())
    }
}