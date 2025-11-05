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
fun SettingHeightSelectionScreen(navController: NavController, accountSettingViewModel: AccountSettingsViewModel, newUser: UserDataUiState) {
    var selectedFeet by remember { mutableStateOf(5) }
    var selectedInches by remember { mutableStateOf(6) }
    val cmRange = (140..210 step 1).toList()
    val feetRange = (0..11).toList()
    val inchRange = (0..11).toList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        //back button
        Column(modifier = Modifier.fillMaxSize()) {
            SettingsTopBar(
                title = "Height",
                subtitle = "Select your height",
                onBackClick = { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            //title
            Text(
                text = "What Is Your Height?",
                style = TextStyle(
                    fontSize = 30.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Metric/Imperial
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Silver, RoundedCornerShape(16.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .fillMaxHeight()
                        .weight(1f)
                        .clickable {
                            newUser.heightMetric.value = false
                            newUser.heightValue.value = (selectedFeet * 12) + selectedInches
                        }
                        .background(if (!newUser.heightMetric.value) Color.White else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Imperial",
                        color = if (!newUser.heightMetric.value) Color.Black else Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxHeight()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            newUser.heightMetric.value = true
                            newUser.heightValue.value = 0
                        }
                        .background(if (newUser.heightMetric.value) White else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Metric",
                        color = if (newUser.heightMetric.value) Color.Black else White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            //show selected value
            if (newUser.heightValue.value != 0) {
                val displayText = if (newUser.heightMetric.value) {
                    "${newUser.heightValue.value} CM"
                } else {
                    val ft = newUser.heightValue.value / 12
                    val `in` = newUser.heightValue.value % 12
                    "${ft} FT ${`in`} IN"
                }

                Text(
                    text = displayText,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            //scrollables
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                //cm
                if (newUser.heightMetric.value) {
                    ScrollableColumnSelection(null, cmRange, newUser.heightValue.value) {
                        newUser.heightValue.value = it
                    }
                } else {
                    //ft and in
                    ScrollableColumnSelection("FT", feetRange, selectedFeet) {
                        selectedFeet = it
                        newUser.heightValue.value = (selectedFeet * 12) + selectedInches
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    ScrollableColumnSelection("IN", inchRange, selectedInches) {
                        selectedInches = it
                        newUser.heightValue.value = (selectedFeet * 12) + selectedInches
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    accountSettingViewModel.saveUserData(newUser)
                    navController.popBackStack()
                },
                enabled = newUser.heightValue.value != 0,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = DeepBlue,
                    disabledBackgroundColor = DeepBlue
                ),
                border = BorderStroke(1.dp, Orange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text("Continue", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}