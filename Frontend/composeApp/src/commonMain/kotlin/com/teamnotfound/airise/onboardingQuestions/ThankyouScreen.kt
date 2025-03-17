package com.teamnotfound.airise.onboardingQuestions

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.compose.material.Text
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ThankYouScreen(navController: NavController){
    var isNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit){
        delay(1000)
        isNavigated = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            text = "Thank you! Please wait for your information to be processed.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp),
            style = TextStyle(fontSize = 50.sp)
        )
        // Waiting for icon or any other elements
    }
}