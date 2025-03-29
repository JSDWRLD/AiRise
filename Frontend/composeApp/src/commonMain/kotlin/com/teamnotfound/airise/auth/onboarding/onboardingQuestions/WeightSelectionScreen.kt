package com.teamnotfound.airise.auth.onboarding.onboardingQuestions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teamnotfound.airise.data.serializable.UserData

/*
 * Page to select user weight
 */
@Composable
fun WeightSelectionScreen(navController: NavController, nextRoute: String, newUser: UserData) {
    // weight ranges
    val weightRange = if (newUser.weightMetric.value) {
        (45..150 step 5)
    } else {
        (100..330 step 5)
    }
    // body
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF091819))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            // title
            Text(
                text = "What Is Your Weight?",
                style = TextStyle(
                    fontSize = 30.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
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
            onClick = { navController.navigate(nextRoute) },
            enabled = newUser.weightValue.value != 0,
            border = BorderStroke(1.dp, Color(0xFFCE5100)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1B424B))
        ) {
            Text("Continue", color = Color.White)
        }
    }
}
