package com.teamnotfound.airise.auth.onboarding.onboardingQuestions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teamnotfound.airise.data.serializable.UserData

/*
 * Page to select user weight
 */
@Composable
fun WeightSelectionScreen(navController: NavController, newUser: UserData) {
    // weight ranges
    val weightRange = if (newUser.weightMetric.value) {
        (45..150 step 5)
    } else {
        (100..330 step 5)
    }
    //
    val showDialog = remember { mutableStateOf(false) }
    val nextScreen = OnboardingScreens.AgeSelection.route

    // body
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(
                backgroundColor = Color(0xFF091819),
                contentColor = Color.White,
                title = {Text("Fitness Goal (11/13)")},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFCE5100)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(nextScreen) }) {
                        Text("Skip", color = Color(0xFFCE5100))
                    }
                }
            )
            // title
            Text(
                text = "What Is Your Weight?",
                fontSize = 22.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp)
            )
            // metric select
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Gray, RoundedCornerShape(16.dp)),
                horizontalArrangement = Arrangement.Center
            ) {
                // pounds
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(1f)
                        .clickable {
                            newUser.weightMetric.value = false
                            newUser.weightValue.value = 0
                        }
                        .background(if (!newUser.weightMetric.value) Color.White else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LB",
                        color = if (!newUser.weightMetric.value) Color.Black else Color.White,
                        fontSize = 18.sp
                    )
                }
                // kilos
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(1f)
                        .clickable {
                            newUser.weightMetric.value = true
                            newUser.weightValue.value = 0
                        }
                        .background(if (newUser.weightMetric.value) Color.White else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "KG",
                        color = if (newUser.weightMetric.value) Color.Black else Color.White,
                        fontSize = 18.sp
                    )
                }
            }
            // weight scroll
            ScrollableColumnSelection(
                label = null,
                items = weightRange.toList(),
                selectedItem = newUser.weightValue.value,
                onItemSelected = { newUser.weightValue.value = it },
            )
        }
        // continue button
        Button(
            onClick = {
                showDialog.value = true
                navController.navigate(nextScreen)},
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.8f)
                .padding(16.dp),
            enabled = newUser.weightValue.value != 0,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF21565C))
        ) {
            Text("Continue", fontSize = 18.sp, color = Color.White)
        }
    }

    // temp display to show values actually saved to newUser
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Your Weight Selection") },
            text = {
                Text("Weight: ${newUser.weightValue.value} ${if (newUser.weightMetric.value) "KG" else "LB"}")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                        navController.popBackStack()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}
