package com.teamnotfound.airise.auth.onboarding.onboardingQuestions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.navigation.NavHostController
import com.teamnotfound.airise.AppScreen
import com.teamnotfound.airise.auth.onboarding.OnboardingViewModel
import com.teamnotfound.airise.auth.onboarding.ThankYouScreen
import com.teamnotfound.airise.data.serializable.UserDataUiState
import com.teamnotfound.airise.data.cache.SummaryCache
import com.teamnotfound.airise.util.*
import com.teamnotfound.airise.util.BgBlack

//Creates entry point for onboarding screens
@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel, appNavController: NavController) {

    val navController = rememberNavController()
    //Create a new user onboarding object
    val newUser = remember {UserDataUiState()}
    NavigateQuestions(
        navController = navController, viewModel = viewModel, appNavController = appNavController, newUser = newUser
    )
}

//Defines different navigation routes for onboarding screens
@Composable
fun NavigateQuestions(
    navController: NavHostController,
    viewModel: OnboardingViewModel,
    appNavController: NavController,
    newUser: UserDataUiState
) {
    NavHost(navController = navController, startDestination = OnboardingScreens.NameInput.route) {

        // 1
        composable(OnboardingScreens.NameInput.route) {
            NameInputScreen(navController, newUser)
        }

        // 2
        composable(OnboardingScreens.WorkoutGoal.route) {
            WorkoutGoalScreen(navController, newUser)
        }

        // 3
        composable(OnboardingScreens.FitnessLevel.route) {
            FitnessLevelScreen(navController, newUser)
        }

        // 4 (NOT skippable – handled inside the screen by passing onSkipClick = null)
        composable(OnboardingScreens.WorkoutLength.route) {
            WorkoutLengthScreen(navController, newUser)
        }

        // 5
        composable(OnboardingScreens.EquipmentAccess.route) {
            EquipmentAccessScreen(navController, newUser)
        }

        // 6
        composable(OnboardingScreens.WorkoutDays.route) {
            WorkoutDaysScreen(navController, newUser)
        }

        // 7 (skippable – handled inside the screen by providing onSkipClick)
        composable(OnboardingScreens.WorkoutTime.route) {
            WorkoutTimeScreen(navController, newUser)
        }

        // 8
        composable(OnboardingScreens.DietaryGoal.route) {
            DietaryGoalScreen(navController, newUser)
        }

        // 9
        composable(OnboardingScreens.WorkoutRestrictions.route) {
            WorkoutRestrictionsScreen(navController, newUser)
        }

        // 10
        composable(OnboardingScreens.HeightSelection.route) {
            // This screen uses your OnboardingScaffold internally with:
            // stepTitle = "Fitness Goal (10/13)", onSkipClick -> WeightSelection
            HeightSelectionScreen(
                navController = navController,
                nextScreen = OnboardingScreens.WeightSelection.route,
                newUser = newUser
            )
        }

        // 11
        composable(OnboardingScreens.WeightSelection.route) {
            // Uses your OnboardingScaffold with stepTitle = "Fitness Goal (11/13)"
            // Decide if skip is allowed by setting onSkipClick inside that screen
            WeightSelectionScreen(
                navController = navController,
                nextScreen = OnboardingScreens.AgeSelection.route,
                newUser = newUser
            )
        }

        // 12
        composable(OnboardingScreens.AgeSelection.route) {
            // Uses your OnboardingScaffold with stepTitle = "Fitness Goal (12/13)", skip -> ActivityLevel
            AgeSelectionScreen(
                navController = navController,
                nextScreen = OnboardingScreens.ActivityLevel.route,
                newUser = newUser
            )
        }

        // 13
        composable(OnboardingScreens.ActivityLevel.route) {
            ActivityLevelScreen(navController, newUser)
        }

        // Done
        composable(OnboardingScreens.ThankYou.route) {
            ThankYouScreen(appNavController, viewModel, newUser = newUser)
        }
    }
}
