package com.teamnotfound.airise.onboarding.onboardingQuestions

import androidx.navigation.NavController
import androidx.compose.runtime.Composable

@Composable
fun WorkoutGoalScreen(navController: NavController){
    QuestionScreen(
        questionText = "What is your workout goal?",
        options = listOf("Maintenance", "Muscle Gain", "Weight Loss"),
        nextScreen = OnboardingScreens.FitnessLevel,
        navController = navController
    )
}
