package com.teamnotfound.airise.onboardingQuestions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController

@Composable
fun EquipmentAccessScreen(navController: NavController){
    val selectedOptions = remember { mutableStateOf(setOf<String>()) }

    MultiSelectQuestionScreen(
        questionText = "What equipment do you have access to?",
        options = listOf("Gym", "Home", "Body Weight", "Other Equipment"),
        selectedOptions = selectedOptions,
        nextScreen = OnboardingScreens.WorkoutDays,
        navController = navController
    )
}