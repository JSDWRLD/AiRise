package com.teamnotfound.airise.navigationBar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

//Defines the different elements within bottom navigation bar
sealed class NavBarItems(val route: String, val title: String, val icon: ImageVector){
    data object Workout : NavBarItems("workout", "Workout", Icons.Filled.FitnessCenter)
    data object Meal : NavBarItems("meal", "Meal", Icons.Filled.Restaurant)
    data object Overview : NavBarItems("overview", "Overview", Icons.Filled.LightMode)
    data object Community : NavBarItems("community", "Community", Icons.Filled.QuestionAnswer)
    data object Progress : NavBarItems("progress", "Progress", Icons.Filled.Insights)
}