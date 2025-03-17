package com.teamnotfound.airise.navigationBar

import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.navigation.NavHostController

//Creates entry point for bottom navigation bar
@Composable
fun NavBar(){
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ){
        Navigate(navController)
    }
}


//Defines the navigation routes to different screens
@Composable
fun Navigate(navController: NavHostController){
    NavHost(navController = navController, startDestination = NavBarItems.Overview.route){
        composable(NavBarItems.Workout.route) { Text(text = "Workout Screen") }
        composable(NavBarItems.Meal.route) { Text(text = "Meal Screen") }
        composable(NavBarItems.Overview.route) { Text(text = "Overview Screen") }
        composable(NavBarItems.Community.route) { Text(text = "Community Screen") }
        composable(NavBarItems.Progress.route) { Text(text = "Progress Screen") }
    }
}

//Creates the bottom navigation bar and ui elements
@Composable
fun BottomNavigationBar(navController: NavController){
    val items = listOf(
        NavBarItems.Workout,
        NavBarItems.Meal,
        NavBarItems.Overview,
        NavBarItems.Community,
        NavBarItems.Progress
    )
    BottomNavigation(
        backgroundColor = Color.Black,
        contentColor = Color.White,
        elevation = 5.dp,
        modifier = Modifier.height(50.dp)
    ){
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                selectedContentColor = Color.White,
                unselectedContentColor = Color.Gray,
                onClick = {
                    navController.navigate(screen.route){
                        popUpTo(navController.graph.startDestinationId){ saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
