package com.teamnotfound.airise.navigationBar

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.teamnotfound.airise.navigationBar.Screen
import com.teamnotfound.airise.util.NotificationSettingsScreen
import com.teamnotfound.airise.home.AccountSettingScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Account.route
    ) {
        composable(route = Screen.Notifications.route) {
            NotificationSettingsScreen(navController = navController)
        }

        composable(route = Screen.Account.route) {
            AccountSettingScreen(
                navController = navController,
                user = TODO()
            )  // Corrected name here
        }
    }
}