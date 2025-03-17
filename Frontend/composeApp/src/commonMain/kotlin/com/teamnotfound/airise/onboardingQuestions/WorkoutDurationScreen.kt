package com.teamnotfound.airise.onboardingQuestions

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun WorkoutDurationScreen(navController: NavController){
    QuestionScreen(
        questionText = "How long would you like to workout?",
        options = listOf("15 minutes", "30 minutes", "45 minutes", "1 hour+"),
        nextScreen = OnboardingScreens.EquipmentAccess,
        navController = navController
    )
}