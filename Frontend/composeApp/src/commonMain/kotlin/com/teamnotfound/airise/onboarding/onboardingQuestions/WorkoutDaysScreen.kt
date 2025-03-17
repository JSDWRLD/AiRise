package com.teamnotfound.airise.onboarding.onboardingQuestions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController

@Composable
fun WorkoutDaysScreen(navController: NavController){
    val selectedOptions = remember { mutableStateOf(setOf<String>()) }

    MultiSelectQuestionScreen(
        questionText = "What days do you prefer to workout on?",
        options = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"),
        selectedOptions = selectedOptions,
        nextScreen = OnboardingScreens.WorkoutTime,
        navController = navController
    )
}