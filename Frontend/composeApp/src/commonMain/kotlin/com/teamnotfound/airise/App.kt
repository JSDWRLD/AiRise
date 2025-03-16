package com.teamnotfound.airise

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.teamnotfound.airise.network.DemoClient

// main
@Composable
@Preview
fun App(client: DemoClient) {
    MaterialTheme {
        OnBoardNavHost()
    }
}

@Composable
fun OnBoardNavHost() {
    val navController = rememberNavController()
    val newUser = remember {UserProfile()}

    NavHost(navController = navController, startDestination = "onboard") {
        composable("onboard") { OnBoardScreen(navController) }
        composable("height_selection") { HeightSelectionScreen(newUser) }
        composable("weight_selection") { WeightSelectionScreen(newUser) }
        composable("age_selection") { AgeSelectionScreen(newUser) }
    }
}

@Composable
fun OnBoardScreen(navController: NavController) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // height selection button
        Button(onClick = { navController.navigate("height_selection") }) {
            Text("Go to Height Selection")
        }
        // weight selection button
        Button(onClick = { navController.navigate("weight_selection") }) {
            Text("Go to Weight Selection")
        }
        // age selection button
        Button(onClick = { navController.navigate("age_selection") }) {
            Text("Go to Age Selection")
        }
    }
}
