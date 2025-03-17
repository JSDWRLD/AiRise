package com.teamnotfound.airise.onboardingQuestions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController

@Composable
fun WorkoutTimeScreen(navController: NavController){
    val selectedOptions = remember { mutableStateOf(setOf<String>()) }

    MultiSelectQuestionScreen(
        questionText = "What are your preferred workout times?",
        options = listOf("Morning", "Afternoon", "Evening"),
        selectedOptions = selectedOptions,
        nextScreen = OnboardingScreens.DietaryGoal,
        navController = navController
    )
}