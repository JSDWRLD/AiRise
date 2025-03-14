package com.teamnotfound.airise

import com.teamnotfound.airise.LoginScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import airise.composeapp.generated.resources.Res
import airise.composeapp.generated.resources.compose_multiplatform
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.TextField
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.teamnotfound.airise.network.DemoClient
import com.teamnotfound.airise.util.NetworkError
import com.teamnotfound.airise.util.onError
import com.teamnotfound.airise.util.onSuccess
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


enum class AppScreen {
    LOGIN,
    //SIGNUP
}

// This is basically your main function.
@Composable
fun App(client: DemoClient) {
    val navController = rememberNavController()

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            NavHost(
                navController = navController,
                startDestination = AppScreen.LOGIN.name
            ) {
                //login screen
                composable(route = AppScreen.LOGIN.name) {
                    LoginScreen(
                        onLoginClick = { /* login */ },
                        onForgotPasswordClick = { /* forgot password */ },
                        onSignUpClick = { /*navController.navigate(AppScreen.SIGNUP.name) */},
                        onGoogleSignInClick = { /* google Sign-In */ }
                    )
                }
                /*
                // sign up screens
                composable(route = AppScreen.SIGNUP.name) {
                    SignUpScreen(
                        onSignUpClick = { /* Sign-Up */ },
                        onLoginClick = { navController.popBackStack() },
                        onForgotPasswordClick = { /* Forgot Password */ },
                        onGoogleSignUpClick = { /* Google Sign-Up */ },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                "recover_account" -> RecoverAccountScreen(
                    onSendEmailClick = { currentScreen = "recovery_sent" },
                    onBackClick = { currentScreen = "login" }
                )

                "recovery_sent" -> RecoverySentScreen(
                    onBackToLoginClick = { currentScreen = "login" },
                    onBackClick = { currentScreen = "recover_account" }
                )
                */
            }
        }

/*
@Preview
fun App(client: DemoClient) {
    var showContent by remember { mutableStateOf(false) }
    MaterialTheme {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    greeting.forEach { greeting ->
                        Text(greeting)
                        Divider()
                    }
                }
            }
        }


 */



        /*
        var censoredText by remember {
            mutableStateOf<String?>(null)
        }
        var uncensoredText by remember {
            mutableStateOf("")
        }
        var isLoading by remember {
            mutableStateOf(false)
        }
        var errorMessage by remember {
            mutableStateOf<NetworkError?>(null)
        }
        val scope = rememberCoroutineScope()
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            TextField(
                value = uncensoredText,
                onValueChange = { uncensoredText = it },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                placeholder = {
                    Text("Uncensored text")
                }
            )
            Button(onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null

                    client.censorWords(uncensoredText)
                        .onSuccess {
                            censoredText = it
                        }
                        .onError {
                            errorMessage = it
                        }
                    isLoading = false
                }
            }) {
                if(isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(15.dp),
                        strokeWidth = 1.dp,
                        color = Color.White
                    )
                } else {
                    Text("Censor!")
                }
            }
            censoredText?.let {
                Text(it)
            }
            errorMessage?.let {
                Text(
                    text = it.name,
                    color = Color.Red
                )
            }
        }

         */

    }
}