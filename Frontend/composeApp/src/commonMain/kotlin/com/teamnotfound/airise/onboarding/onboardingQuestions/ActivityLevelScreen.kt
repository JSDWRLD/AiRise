package com.teamnotfound.airise.onboarding.onboardingQuestions

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun ActivityLevelScreen(navController: NavController){
    QuestionScreen(
        questionText = "What is your preferred active level?",
        options = listOf("Sendentary", "Lightly Active", "Active", "Very Active"),
        nextScreen = OnboardingScreens.ThankYou,
        navController = navController
    )
}