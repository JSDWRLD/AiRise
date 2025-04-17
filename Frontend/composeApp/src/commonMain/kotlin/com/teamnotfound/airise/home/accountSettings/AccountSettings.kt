package com.teamnotfound.airise.home.accountSettings

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.teamnotfound.airise.auth.onboarding.onboardingQuestions.AgeSelectionScreen
import com.teamnotfound.airise.auth.onboarding.onboardingQuestions.HeightSelectionScreen
import com.teamnotfound.airise.auth.onboarding.onboardingQuestions.WeightSelectionScreen
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.health.HealthDashboardScreen
import com.khealth.KHealth

@Composable
fun AccountSettings(
    navController: NavHostController,
    accountSettingViewModel: AccountSettingsViewModel,
    kHealth: KHealth
) {
    val localNavController = rememberNavController()  // Use a local NavController for account settings
    val user = UserData()

    NavHost(
        navController = localNavController,
        startDestination = AccountSettingScreens.AccountSettings.route
    ) {
        composable(AccountSettingScreens.AccountSettings.route) {
            // Pass parent navController
            AccountSettingScreen(user, navController, localNavController, accountSettingViewModel)
        }
        composable(AccountSettingScreens.DOBSelect.route) {
            SettingAgeSelectionScreen(localNavController, AccountSettingScreens.AccountSettings.route, user)
        }
        composable(AccountSettingScreens.WeightSelect.route) {
            SettingWeightSelectionScreen(localNavController, AccountSettingScreens.AccountSettings.route, user)
        }
        composable(AccountSettingScreens.HeightSelect.route) {
            SettingHeightSelectionScreen(localNavController, AccountSettingScreens.AccountSettings.route, user)
        }
        composable(AccountSettingScreens.AiPersonality.route) {
            AiPersonalityScreen(user, localNavController)
        }
        composable(AccountSettingScreens.Notifications.route) {
            NotificationSettingsScreen(localNavController)
        }
        composable(AccountSettingScreens.HealthDashboard.route) {
            HealthDashboardScreen(kHealth, onBackClick = { localNavController.popBackStack() })
        }
    }
}
