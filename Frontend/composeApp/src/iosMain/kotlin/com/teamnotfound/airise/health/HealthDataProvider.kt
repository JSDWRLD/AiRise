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

class HealthAccessException(message: String) : Exception(message)

actual class HealthDataProvider actual constructor(private val kHealth: KHealth) {

    private val REQUIRED_PERMS = arrayOf(
        KHPermission.ActiveCaloriesBurned(read = true, write = true),
        KHPermission.StepCount(read = true, write = true),
        KHPermission.SleepSession(read = true, write = true)
    )

    private fun KHPermission.readFlag(): Boolean = when (this) {
        is KHPermission.ActiveCaloriesBurned -> this.read
        is KHPermission.StepCount -> this.read
        is KHPermission.SleepSession -> this.read
        else -> false
    }

    private fun KHPermission.writeFlag(): Boolean = when (this) {
        is KHPermission.ActiveCaloriesBurned -> this.write
        is KHPermission.StepCount -> this.write
        is KHPermission.SleepSession -> this.write
        else -> false
    }

    private fun isWriteGranted(perm: KHPermission): Boolean = when (perm) {
        is KHPermission.ActiveCaloriesBurned -> perm.write
        is KHPermission.StepCount -> perm.write
        is KHPermission.SleepSession -> perm.write
        else -> false
    }

    private fun areAllRequiredWriteGranted(granted: Set<KHPermission>): Boolean {
        val step = granted.any { it is KHPermission.StepCount && isWriteGranted(it) }
        val cal = granted.any { it is KHPermission.ActiveCaloriesBurned && isWriteGranted(it) }
        val sleep = granted.any { it is KHPermission.SleepSession && isWriteGranted(it) }
        return step && cal && sleep
    }

    private val SLEEPING_STAGES = setOf(
        KHSleepStage.Sleeping, KHSleepStage.REM, KHSleepStage.Deep, KHSleepStage.Light
    )

    private fun KHRecord.SleepSession.totalSleepHours(): Double {
        val totalMillis = samples
            .asSequence()
            .filter { it.stage in SLEEPING_STAGES }
            .sumOf { s ->
                val start = minOf(s.startTime, s.endTime)
                val end = maxOf(s.startTime, s.endTime)
                (end - start).inWholeMilliseconds
            }
        return totalMillis.toDuration(DurationUnit.MILLISECONDS).toDouble(DurationUnit.HOURS)
    }

    private fun List<KHRecord>.mostRecentSleepSessionOrNull(): KHRecord.SleepSession? =
        this.filterIsInstance<KHRecord.SleepSession>()
            .maxByOrNull { sess -> sess.samples.maxOfOrNull { it.endTime } ?: Instant.DISTANT_PAST }

    actual suspend fun requestPermissions(): Boolean = withContext(Dispatchers.Main) {
        val available = kHealth.isHealthStoreAvailable
        if (!available) return@withContext false
        val granted = kHealth.requestPermissions(*REQUIRED_PERMS)
        areAllRequiredWriteGranted(granted)
    }

    private suspend fun hasAllAccess(): Boolean = withContext(Dispatchers.Main) {
        val available = kHealth.isHealthStoreAvailable
        if (!available) return@withContext false
        val granted = kHealth.checkPermissions(*REQUIRED_PERMS)
        areAllRequiredWriteGranted(granted)
    }

    actual suspend fun getHealthData(): IHealthData = withContext(Dispatchers.Default) {
        val ok = hasAllAccess()
        if (!ok) {
            throw HealthAccessException("Health permissions not granted or Health app unavailable")
        }

        val now = Clock.System.now()
        val startTime = now - 1.days
        val endTime = now
        val sleepStart = now - 36.hours

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
            override val hydration = 0.0
        }
    }

    actual suspend fun writeHealthData(): Boolean = withContext(Dispatchers.Default) {
        val ok = withContext(Dispatchers.Main) { kHealth.isHealthStoreAvailable }
        if (!ok) return@withContext false

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
}