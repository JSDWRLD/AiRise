package com.teamnotfound.airise.cache

import android.content.Context
import com.teamnotfound.airise.room.DatabaseProvider
import com.teamnotfound.airise.room.SummaryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import com.teamnotfound.airise.data.serializable.UserOnboardingData
import java.util.Calendar

class SummaryCacheAndroid(private val context: Context) : SummaryCache {
    private val db = DatabaseProvider.getDatabase(context)

    override suspend fun cacheSummary(summary: UserOnboardingData) {
        withContext(Dispatchers.IO) {
            try {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, summary.dobDay)
                    set(Calendar.MONTH, summary.dobMonth - 1)
                    set(Calendar.YEAR, summary.dobYear)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val dobTimestamp = calendar.timeInMillis

                val workoutLengthInt = summary.workoutLength.toIntOrNull() ?: 0

                val entity = SummaryEntity(
                    id = 0,  // auto-generated
                    userId = summary.firstName,
                    firstName = summary.firstName,
                    lastName = summary.lastName,
                    middleName = summary.middleName,
                    workoutGoal = summary.workoutGoal,
                    currentFitnessLevel = summary.fitnessLevel,
                    workoutLength = workoutLengthInt,
                    equipmentAccess = summary.workoutEquipment,
                    daysSelected = summary.workoutDays,
                    workoutTimes = summary.workoutTime,
                    dietaryGoal = summary.dietaryGoal,
                    hasInjuries = false,
                    injuryDescription = null,
                    height = summary.heightValue.toDouble(),
                    isHeightMetric = summary.heightMetric,
                    weight = summary.weightValue.toDouble(),
                    isWeightMetric = summary.weightMetric,
                    dateOfBirth = dobTimestamp,
                    preferredActivityLevel = summary.activityLevel,
                    timestamp = System.currentTimeMillis()
                )
                val insertedId = db.summaryDao().insertSummary(entity)
                Log.d("SummaryCacheAndroid", "Inserted summary with ID: $insertedId")
            } catch (e: Exception) {
                Log.e("SummaryCacheAndroid", "Error inserting summary", e)
                throw e
            }
        }
    }

    override suspend fun getUserSummaries(userId: String): List<UserOnboardingData> {
        return withContext(Dispatchers.IO) {
            db.summaryDao().getSummariesForUser(userId).map { entity ->
                UserOnboardingData(
                    firstName = entity.firstName,
                    lastName = entity.lastName,
                    middleName = entity.middleName,
                    workoutGoal = entity.workoutGoal,
                    fitnessLevel = entity.currentFitnessLevel,
                    workoutLength = entity.workoutLength.toString(),
                    workoutEquipment = entity.equipmentAccess,
                    workoutDays = entity.daysSelected,
                    workoutTime = entity.workoutTimes,
                    dietaryGoal = entity.dietaryGoal,
                    heightMetric = entity.isHeightMetric,
                    heightValue = entity.height.toInt(),
                    weightMetric = entity.isWeightMetric,
                    weightValue = entity.weight.toInt(),
                    dobDay = 0,
                    dobMonth = 0,
                    dobYear = 0,
                    activityLevel = entity.preferredActivityLevel
                )
            }
        }
    }
}