package com.teamnotfound.airise.onboardingQuestions

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import androidx.navigation.NavHostController

//Creates entry point for onboarding screens
@Composable
fun OnboardingScreen() {
    val navController = rememberNavController()
    NavigateQuestions(navController = navController)
}

//Defines different navigation routes for onboarding screens
@Composable
fun NavigateQuestions(navController: NavHostController){
    NavHost(navController = navController, startDestination = OnboardingScreens.WorkoutGoal.route) {
        composable(OnboardingScreens.WorkoutGoal.route) { WorkoutGoalScreen(navController) }
        composable(OnboardingScreens.FitnessLevel.route) { FitnessLevelScreen(navController) }
        composable(OnboardingScreens.WorkoutDuration.route) { WorkoutDurationScreen(navController) }
        composable(OnboardingScreens.EquipmentAccess.route) { EquipmentAccessScreen(navController) }
        composable(OnboardingScreens.WorkoutDays.route) { WorkoutDaysScreen(navController) }
        composable(OnboardingScreens.WorkoutTime.route) { WorkoutTimeScreen(navController) }
        composable(OnboardingScreens.DietaryGoal.route) { DietaryGoalScreen(navController) }
        composable(OnboardingScreens.RestrictionInjuries.route) { RestrictionInjuryScreen(navController) }
        composable(OnboardingScreens.ActivityLevel.route) { ActivityLevelScreen(navController) }
        composable(OnboardingScreens.ThankYou.route) { ThankYouScreen(navController) }
    }
}