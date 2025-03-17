package com.teamnotfound.airise.onboarding.onboardingQuestions

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun DietaryGoalScreen(navController: NavController){
    QuestionScreen(
        questionText = "What is your dietary goal?",
        options = listOf("Lose weight", "Maintain", "Gain weight"),
        nextScreen = OnboardingScreens.RestrictionInjuries,
        navController = navController
    )
}