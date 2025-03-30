package com.teamnotfound.airise.home

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.teamnotfound.airise.auth.onboarding.onboardingQuestions.AgeSelectionScreen
import com.teamnotfound.airise.auth.onboarding.onboardingQuestions.HeightSelectionScreen
import com.teamnotfound.airise.auth.onboarding.onboardingQuestions.WeightSelectionScreen
import com.teamnotfound.airise.data.serializable.UserData

@Composable
fun AccountSettings(navController: NavHostController) {
    val localNavController = rememberNavController()  // Use a local NavController for account settings
    val user = UserData()

    NavHost(
        navController = localNavController,
        startDestination = AccountSettingScreens.AccountSettings.route
    ) {
        composable(AccountSettingScreens.AccountSettings.route) {
            // Pass parent navController
            AccountSettingScreen(user, navController, localNavController)
        }
        composable(AccountSettingScreens.DOBSelect.route) {
            AgeSelectionScreen(localNavController, AccountSettingScreens.AccountSettings.route, user)
        }
        composable(AccountSettingScreens.WeightSelect.route) {
            WeightSelectionScreen(localNavController, AccountSettingScreens.AccountSettings.route, user)
        }
        composable(AccountSettingScreens.HeightSelect.route) {
            HeightSelectionScreen(localNavController, AccountSettingScreens.AccountSettings.route, user)
        }
        composable(AccountSettingScreens.AiPersonality.route) {
            AiPersonalityScreen(user, localNavController)
        }
        composable(AccountSettingScreens.Notifications.route) {
            NotificationSettingsScreen(localNavController)
        }
    }
}
