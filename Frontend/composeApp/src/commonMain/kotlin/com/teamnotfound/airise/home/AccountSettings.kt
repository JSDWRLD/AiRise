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
fun AccountSettings() {
    val navController = rememberNavController()
    val user = UserData()
    AccountSetting(navController = navController, user = user)
}

@Composable
fun AccountSetting(navController: NavHostController, user: UserData) {
    NavHost(navController = navController, startDestination = AccountSettingScreens.AccountSettings.route) {
        composable(AccountSettingScreens.AccountSettings.route) { AccountSettingScreen(user, navController) }
        composable(AccountSettingScreens.DOBSelect.route) { AgeSelectionScreen(navController, user) }
        composable(AccountSettingScreens.WeightSelect.route) { WeightSelectionScreen(navController, user) }
        composable(AccountSettingScreens.HeightSelect.route) { HeightSelectionScreen(navController, user) }
        composable(AccountSettingScreens.AiPersonality.route) { AiPersonalityScreen(user, navController) }
    }
}
