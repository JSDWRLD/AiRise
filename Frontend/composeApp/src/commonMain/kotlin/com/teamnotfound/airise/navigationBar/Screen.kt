package com.teamnotfound.airise.navigationBar

sealed class Screen(val route: String) {
    object Home : Screen("home")

    object Account : Screen("account")
    object Notifications : Screen("notifications")
    // Add any other screens you need
}