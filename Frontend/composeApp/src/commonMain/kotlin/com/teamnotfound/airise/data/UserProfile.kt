package com.teamnotfound.airise.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/*
 * Class to hold UserProfile data
 */
class UserProfile {
    var email: MutableState<String> = mutableStateOf("")
    var password: MutableState<String> = mutableStateOf("")
    var workoutGoal: MutableState<String> = mutableStateOf("")
    var fitnessLevel: MutableState<String> = mutableStateOf("")
    var workoutLength: MutableState<Int> = mutableStateOf(0)
    var workoutEquipment: MutableState<String> = mutableStateOf("")
    var workoutDays: MutableState<List<Boolean>> = mutableStateOf(
        listOf(false, false, false, false, false, false, false)
    )
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
}
