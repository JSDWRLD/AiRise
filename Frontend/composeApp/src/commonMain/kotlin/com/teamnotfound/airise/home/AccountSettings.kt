package com.teamnotfound.airise.home

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.teamnotfound.airise.data.serializable.UserOnboarding
import com.teamnotfound.airise.onboarding.onboardingQuestions.AgeSelectionScreen
import com.teamnotfound.airise.onboarding.onboardingQuestions.HeightSelectionScreen
import com.teamnotfound.airise.onboarding.onboardingQuestions.WeightSelectionScreen

@Composable
fun AccountSettings() {
    val navController = rememberNavController()
    val user = UserOnboarding()
    AccountSetting(navController = navController, user = user)
}

@Composable
fun AccountSetting(navController: NavHostController, user: UserOnboarding) {
    NavHost(navController = navController, startDestination = AccountSettingScreens.AccountSettings.route) {
        composable(AccountSettingScreens.AccountSettings.route) { AccountSettingScreen(user, navController) }
        composable(AccountSettingScreens.DOBSelect.route) { AgeSelectionScreen(user, navController) }
        composable(AccountSettingScreens.WeightSelect.route) { WeightSelectionScreen(user, navController) }
        composable(AccountSettingScreens.HeightSelect.route) { HeightSelectionScreen(user, navController) }
        composable(AccountSettingScreens.AiPersonality.route) { AiPersonalityScreen(user, navController) }
    }
}
