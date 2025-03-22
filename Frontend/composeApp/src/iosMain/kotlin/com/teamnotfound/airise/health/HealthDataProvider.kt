package com.teamnotfound.airise.health

import com.khealth.KHealth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSDate
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnit

actual class HealthDataProvider {

    private val kHealth = KHealth()

    actual suspend fun getHealthData(): HealthData = withContext(Dispatchers.Default) {
        val now = NSDate()
        val startOfDay = NSCalendar.currentCalendar.dateBySettingHour(
            0, 0, 0, 0, now, NSCalendarUnit.NSCalendarUnitDay
        ) ?: now

        val steps = kHealth.readSteps(startOfDay, now)
        val heartRate = kHealth.readHeartRate(startOfDay, now)

        return@withContext object : HealthData {
            override val steps = steps
            override val heartRate = heartRate
        }
    }
}

actual suspend fun requestPermissions(): Boolean {
    return withContext(Dispatchers.Main) {
        kHealth.requestPermissions()
    }
}