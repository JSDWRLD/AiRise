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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teamnotfound.airise.auth.onboarding.onboardingQuestions.ScrollableColumnSelection
import com.teamnotfound.airise.data.serializable.UserDataUiState
import com.teamnotfound.airise.util.*

@Composable
fun SettingWeightSelectionScreen(navController: NavController, accountSettingViewModel: AccountSettingsViewModel, newUser: UserDataUiState) {
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

            //back button
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
                                tint = Orange
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(20.dp))

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
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Silver, RoundedCornerShape(16.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                newUser.weightMetric.value = false
                                newUser.weightValue.value = 0
                            }
                            .background(if (!newUser.weightMetric.value) White else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LB",
                            color = if (!newUser.weightMetric.value) Color.Black else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                newUser.weightMetric.value = true
                                newUser.weightValue.value = 0
                            }
                            .background(if (newUser.weightMetric.value) White else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "KG",
                            color = if (newUser.weightMetric.value) Color.Black else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                //display selected value
                if (newUser.weightValue.value != 0) {
                    Text(
                        text = "${newUser.weightValue.value} ${if (newUser.weightMetric.value) "KG" else "LB"}",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

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
                    onClick = {
                        accountSettingViewModel.saveUserData(newUser)
                        navController.popBackStack()
                    },
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
                    Text(
                        "Continue",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}