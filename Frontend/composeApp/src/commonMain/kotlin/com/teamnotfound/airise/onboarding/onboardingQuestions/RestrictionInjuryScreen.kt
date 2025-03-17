package com.teamnotfound.airise.onboarding.onboardingQuestions

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun RestrictionInjuryScreen(navController: NavController){
    TextInputQuestionScreen(
        questionText = "Do you have any restrictions or injuries?",
        options = listOf("Yes", "No"),
        nextScreen = OnboardingScreens.ActivityLevel,
        navController = navController
    )
}