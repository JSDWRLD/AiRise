package com.teamnotfound.airise.auth.onboarding.onboardingQuestions

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.util.*

@Composable
fun WeightSelectionScreen(navController: NavController,  nextScreen: String, newUser: UserData) {
    val weightRange = remember(newUser.weightMetric.value) {
        if (newUser.weightMetric.value) {
            (45..150 step 5).toList()
        } else {
            (100..330 step 5).toList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "What Is Your Weight?",
                style = TextStyle(
                    fontSize = 30.sp,
                    color = White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Silver, RoundedCornerShape(16.dp)),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(1f)
                        .clickable {
                            newUser.weightMetric.value = false
                            newUser.weightValue.value = 0
                        }
                        .background(if (!newUser.weightMetric.value) White else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LB",
                        color = if (!newUser.weightMetric.value) Color.Black else White,
                        fontSize = 18.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(1f)
                        .clickable {
                            newUser.weightMetric.value = true
                            newUser.weightValue.value = 0
                        }
                        .background(if (newUser.weightMetric.value) White else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "KG",
                        color = if (newUser.weightMetric.value) Color.Black else White,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ScrollableColumnSelection(null, weightRange, newUser.weightValue.value) {
                    newUser.weightValue.value = it
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.navigate(nextScreen) },
                enabled = newUser.weightValue.value != 0,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = DeepBlue,
                    disabledBackgroundColor = DeepBlue
                ),
                border = BorderStroke(1.dp, Orange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text("Continue", color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}