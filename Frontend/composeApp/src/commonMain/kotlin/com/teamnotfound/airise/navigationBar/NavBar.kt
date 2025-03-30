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
import androidx.navigation.compose.*
import androidx.navigation.NavHostController

//Defines navigation routes for bottom navigation bar
@Composable
fun NavBar(navController: NavHostController){

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(navController = navController, startDestination = NavBarItems.Overview.route) {
                composable(NavBarItems.Workout.route) { Text(text = "Workout Screen") }
                composable(NavBarItems.Meal.route) { Text(text = "Meal Screen") }
                composable(NavBarItems.Overview.route) { Text(text = "Overview Screen") }
                composable(NavBarItems.Community.route) { Text(text = "Community Screen") }
                composable(NavBarItems.Progress.route) { Text(text = "Progress Screen") }
            }
        }
    }
}

//Creates the bottom navigation bar and ui elements
@Composable
fun BottomNavigationBar(navController: NavHostController){
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
        modifier = Modifier
            .height(90.dp)
            .drawBehind {
                drawLine(
                    color = Color(0xFFFFA500),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 4f
                )
            }
    ){
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
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

                selected = currentRoute == screen.route,
                selectedContentColor = Color.White,
                unselectedContentColor = Color.Gray,
                onClick = {
                    navController.navigate(screen.route){
                        // popUpTo(navController.graph.startDestinationId){ saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
