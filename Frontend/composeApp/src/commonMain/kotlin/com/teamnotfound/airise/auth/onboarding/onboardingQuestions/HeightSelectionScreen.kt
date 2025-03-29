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
 * Page to select user height
 */
@Composable
fun HeightSelectionScreen(navController: NavController, nextRoute: String, newUser: UserData) {
    // height ranges
    val heightRange = if (newUser.heightMetric.value) {
        (140..210 step 5)
    } else {
        (50..80 step 1)
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
                text = "What Is Your Height?",
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
                // inches
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(1f)
                        .clickable {
                            newUser.heightMetric.value = false
                            newUser.heightValue.value = 0
                        }
                        .background(if (!newUser.heightMetric.value) Color.White else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "IN",
                        color = if (!newUser.heightMetric.value) Color.Black else Color.White,
                        fontSize = 18.sp
                    )
                }
                // centimeters
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(1f)
                        .clickable {
                            newUser.heightMetric.value = true
                            newUser.heightValue.value = 0
                        }
                        .background(if (newUser.heightMetric.value) Color.White else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CM",
                        color = if (newUser.heightMetric.value) Color.Black else Color.White,
                        fontSize = 18.sp
                    )
                }
            }
            // height scroll
            ScrollableColumnSelection(
                label = null,
                items = heightRange.toList(),
                selectedItem = newUser.heightValue.value,
                onItemSelected = { newUser.heightValue.value = it }
            )
        }
        // continue button
        Button(
            onClick = { navController.navigate(nextRoute) },
            enabled = newUser.heightValue.value != 0,
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
