package com.teamnotfound.airise.data.serializable

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.serialization.Serializable

@Serializable
class UserData {
    var firstName: MutableState<String> = mutableStateOf("")
    var lastName: MutableState<String> = mutableStateOf("")
    var middleName: MutableState<String> = mutableStateOf("")
    var workoutGoal: MutableState<String> = mutableStateOf("")
    var fitnessLevel: MutableState<String> = mutableStateOf("")
    var workoutLength: MutableState<String> = mutableStateOf("")
    var equipmentAccess: MutableState<String> = mutableStateOf("")
    var workoutDays: MutableState<String> = mutableStateOf("")
    var workoutTime: MutableState<String> = mutableStateOf("")
    var dietaryGoal: MutableState<String> = mutableStateOf("")
    var workoutRestrictions: MutableState<String> = mutableStateOf("")
    var heightMetric: MutableState<Boolean> = mutableStateOf(false)
    var heightValue: MutableState<Int> = mutableStateOf(0)
    var weightMetric: MutableState<Boolean> = mutableStateOf(false)
    var weightValue: MutableState<Int> = mutableStateOf(0)
    var dobDay: MutableState<Int> = mutableStateOf(0)
    var dobMonth: MutableState<Int> = mutableStateOf(0)
    var dobYear: MutableState<Int> = mutableStateOf(0)
    var activityLevel: MutableState<String> = mutableStateOf("")

    // Convert to a serializable data class for MongoDB storage
    fun toData(): UserOnboardingData = UserOnboardingData(
        firstName = firstName.value, 
        lastName = lastName.value, 
        middleName = middleName.value,
        workoutGoal = workoutGoal.value,
        fitnessLevel = fitnessLevel.value,
        workoutLength = workoutLength.value,
        workoutEquipment = equipmentAccess.value,
        workoutDays = workoutDays.value,
        workoutTime = workoutTime.value,
        dietaryGoal = dietaryGoal.value,
        workoutRestrictions = workoutRestrictions.value,
        heightMetric = heightMetric.value,
        heightValue = heightValue.value,
        weightMetric = weightMetric.value,
        weightValue = weightValue.value,
        dobDay = dobDay.value,
        dobMonth = dobMonth.value,
        dobYear = dobYear.value,
        activityLevel = activityLevel.value
    )
}

@Serializable
data class UserOnboardingData(
    val firstName: String, 
    val lastName: String, 
    val middleName: String,
    val workoutGoal: String,
    val fitnessLevel: String,
    val workoutLength: String,
    val workoutEquipment: String,
    val workoutDays: String,
    val workoutTime: String,
    val dietaryGoal: String,
    val workoutRestrictions: String,
    val heightMetric: Boolean,
    val heightValue: Int,
    val weightMetric: Boolean,
    val weightValue: Int,
    val dobDay: Int,
    val dobMonth: Int,
    val dobYear: Int,
    val activityLevel: String
)
