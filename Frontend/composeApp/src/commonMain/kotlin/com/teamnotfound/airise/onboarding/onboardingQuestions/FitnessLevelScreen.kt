package com.teamnotfound.airise.onboarding.onboardingQuestions

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun FitnessLevelScreen(navController: NavController){
    QuestionScreen(
        questionText = "What is your current fitness level?",
        options = listOf("Novice", "Intermediate", "Advanced"),
        nextScreen = OnboardingScreens.WorkoutDuration,
        navController = navController
    )
}