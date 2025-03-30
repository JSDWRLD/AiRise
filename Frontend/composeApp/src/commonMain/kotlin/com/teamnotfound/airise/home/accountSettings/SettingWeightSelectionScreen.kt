package com.teamnotfound.airise.home.accountSettings

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
import com.teamnotfound.airise.auth.onboarding.onboardingQuestions.ScrollableColumnSelection
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Silver

@Composable
fun SettingWeightSelectionScreen(navController: NavController, nextRoute: String, newUser: UserData) {
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
            .padding(vertical = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                backgroundColor = BgBlack,
                contentColor = Color.White,
                elevation = 0.dp,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Box(
                    Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFFFFA500)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "What Is Your Weight?",
                style = TextStyle(
                    fontSize = 30.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Gray, RoundedCornerShape(16.dp)),
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
                        .background(if (!newUser.weightMetric.value) Color.White else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LB",
                        color = if (!newUser.weightMetric.value) Color.Black else Color.White,
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
                onClick = { navController.navigate(nextRoute) },
                enabled = newUser.weightValue.value != 0,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = DeepBlue,
                    disabledBackgroundColor = Silver
                ),
                border = BorderStroke(1.dp, Color(0xFFCE5100)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text("Continue", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}