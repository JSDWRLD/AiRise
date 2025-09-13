package com.teamnotfound.airise.navigationBar

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.NavHostController
import com.teamnotfound.airise.util.*
import com.teamnotfound.airise.AppScreen


//Defines navigation routes for bottom navigation bar
@Composable
fun NavBar(navController: NavHostController,
           appNavController: NavHostController? = null
){
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                appNavController = appNavController,
                onCommunityClick = {
                    // Only navigate if we have the app-level controller
                    appNavController?.navigate(AppScreen.CHALLENGES.name) {
                        launchSingleTop = true
                    }
                },
                onOverviewClick = {
                    appNavController?.navigate(AppScreen.HOMESCREEN.name) {
                        launchSingleTop = true
                        appNavController.graph.startDestinationRoute?.let { startRoute ->
                            popUpTo(startRoute) { saveState = true }
                        }
                        restoreState = true
                    }
                }

            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(navController = navController, startDestination = NavBarItems.Overview.route) {
                composable(NavBarItems.Workout.route) { Text(text = "Workout Screen") }
                composable(NavBarItems.Meal.route) { Text(text = "Meal Screen") }
                composable(NavBarItems.Overview.route) { Text(text = "Overview Screen") }
                composable(NavBarItems.Community.route) { Text("Community") }
                composable(NavBarItems.Progress.route) { Text(text = "Progress Screen") }
            }
        }
    }
}

//Creates the bottom navigation bar and ui elements
@Composable
fun BottomNavigationBar(navController: NavHostController,
                        appNavController: NavHostController? = null,
                        onOverviewClick: () -> Unit = {},
                        onCommunityClick: () -> Unit = {}){
    val items = listOf(
        NavBarItems.Workout,
        NavBarItems.Meal,
        NavBarItems.Overview,
        NavBarItems.Community,
        NavBarItems.Progress
    )
    BottomNavigation(
        backgroundColor = Transparent,
        contentColor = Color.White,
        elevation = 5.dp,
        modifier = Modifier
            .height(90.dp)
            .drawBehind {
                drawLine(
                    color = Orange,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 4f
                )
            }
    ){


        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        val appRoute = appNavController?.currentBackStackEntryAsState()?.value?.destination?.route

        items.forEach { screen ->
            BottomNavigationItem(
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(top = 20.dp)
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            modifier = Modifier.size(30.dp) //icon sizing
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = screen.title,
                            fontSize = 11.sp, //label sizing
                            maxLines = 1
                        )
                    }
                },

                selected = when (screen) {
                    NavBarItems.Overview  -> appRoute == AppScreen.HOMESCREEN.name
                    NavBarItems.Community -> appRoute == AppScreen.CHALLENGES.name
                    else                   -> currentRoute == screen.route
                },                selectedContentColor = Color.White,
                unselectedContentColor = Color.Gray,
                onClick = {
                    when (screen) {
                        NavBarItems.Community -> onCommunityClick()
                        NavBarItems.Overview  -> onOverviewClick()
                        else -> navController.navigate(screen.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
