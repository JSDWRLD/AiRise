package com.teamnotfound.airise.auth.onboarding.onboardingQuestions

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import androidx.navigation.NavHostController
import com.teamnotfound.airise.auth.onboarding.ThankYouScreen
import com.teamnotfound.airise.data.serializable.UserData

//Creates entry point for onboarding screens
@Composable
fun OnboardingScreen() {
    val navController = rememberNavController()
    //Create a new user onboarding object
    val newUser = remember {UserData()}
    NavigateQuestions(navController = navController, newUser)
}

//Defines different navigation routes for onboarding screens
@Composable
fun NavigateQuestions(navController: NavHostController, newUser: UserData){
    NavHost(navController = navController, startDestination = OnboardingScreens.NameInput.route) {
        composable(OnboardingScreens.NameInput.route) { NameInputScreen(navController, newUser) }
        composable(OnboardingScreens.WorkoutGoal.route) { WorkoutGoalScreen(navController, newUser) }
        composable(OnboardingScreens.FitnessLevel.route) { FitnessLevelScreen(navController, newUser) }
        composable(OnboardingScreens.WorkoutLength.route) { WorkoutLengthScreen(navController, newUser) }
        composable(OnboardingScreens.EquipmentAccess.route) { EquipmentAccessScreen(navController, newUser) }
        composable(OnboardingScreens.WorkoutDays.route) { WorkoutDaysScreen(navController, newUser) }
        composable(OnboardingScreens.WorkoutTime.route) { WorkoutTimeScreen(navController, newUser) }
        composable(OnboardingScreens.DietaryGoal.route) { DietaryGoalScreen(navController, newUser ) }
        composable(OnboardingScreens.WorkoutRestrictions.route) { WorkoutRestrictionsScreen(navController, newUser) }
        composable(OnboardingScreens.HeightSelection.route) { HeightSelectionScreen(navController, newUser) }
        composable(OnboardingScreens.WeightSelection.route) { WeightSelectionScreen(navController, newUser) }
        composable(OnboardingScreens.AgeSelection.route) { AgeSelectionScreen(navController, newUser) }
        composable(OnboardingScreens.ActivityLevel.route) { ActivityLevelScreen(navController, newUser) }
        composable(OnboardingScreens.ThankYou.route) { ThankYouScreen(navController, newUser) }
    }
}