package com.teamnotfound.airise.auth.onboarding.onboardingQuestions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import androidx.navigation.NavHostController
import com.teamnotfound.airise.auth.onboarding.ThankYouScreen
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.data.cache.SummaryCache

//Creates entry point for onboarding screens
@Composable
fun OnboardingScreen(summaryCache: SummaryCache) {
    val navController = rememberNavController()
    //Create a new user onboarding object
    val newUser = remember {UserData()}
    NavigateQuestions(navController = navController, newUser, summaryCache = summaryCache)
}

//Defines different navigation routes for onboarding screens
@Composable
fun NavigateQuestions(navController: NavHostController, newUser: UserData, summaryCache: SummaryCache){
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
        composable(OnboardingScreens.HeightSelection.route) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TopAppBar(
                        backgroundColor = Color(0xFF091819),
                        contentColor = Color.White,
                        title = { Text("Fitness Goal (10/13)") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color(0xFFCE5100)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate(OnboardingScreens.WeightSelection.route) }) {
                                Text("Skip", color = Color(0xFFCE5100))
                            }
                        }
                    )
                    HeightSelectionScreen(
                        navController,
                        OnboardingScreens.WeightSelection.route,
                        newUser
                    )
                }
            }
        }
        composable(OnboardingScreens.WeightSelection.route) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TopAppBar(
                        backgroundColor = Color(0xFF091819),
                        contentColor = Color.White,
                        title = { Text("Fitness Goal (10/13)") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color(0xFFCE5100)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate(OnboardingScreens.AgeSelection.route) }) {
                                Text("Skip", color = Color(0xFFCE5100))
                            }
                        }
                    )
                    WeightSelectionScreen(
                        navController,
                        OnboardingScreens.AgeSelection.route,
                        newUser
                    )
                }
            }
        }
        composable(OnboardingScreens.AgeSelection.route) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TopAppBar(
                        backgroundColor = Color(0xFF091819),
                        contentColor = Color.White,
                        title = { Text("Fitness Goal (10/13)") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color(0xFFCE5100)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate(OnboardingScreens.ActivityLevel.route) }) {
                                Text("Skip", color = Color(0xFFCE5100))
                            }
                        }
                    )
                    AgeSelectionScreen(
                        navController,
                        OnboardingScreens.ActivityLevel.route,
                        newUser
                    )
                }
            }
        }
        composable(OnboardingScreens.ActivityLevel.route) { ActivityLevelScreen(navController, newUser) }
        composable(OnboardingScreens.ThankYou.route) { ThankYouScreen(navController, newUser, summaryCache = summaryCache) }
    }
}