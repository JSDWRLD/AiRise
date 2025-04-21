package com.teamnotfound.airise.home.accountSettings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.teamnotfound.airise.data.serializable.UserDataUiState
import com.teamnotfound.airise.util.*

@Composable
fun SettingAgeSelectionScreen(navController: NavController, nextRoute: String, newUser: UserDataUiState) {
    val monthRange = (1..12).toList()
    val yearRange = (1900..2025).toList().reversed()
    val dayRange = remember(newUser.dobMonth.value, newUser.dobYear.value) {
        getDayRange(newUser.dobMonth.value, newUser.dobYear.value).toList()
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
                    Modifier
                        .fillMaxWidth()
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

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Select Your Date of Birth",
                style = TextStyle(
                    fontSize = 30.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                //year section
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // selected value
                    Text(
                        text = newUser.dobYear.value.toString(),
                        color = White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // label
                    Text(
                        text = "Year",
                        color = Silver,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    ScrollableColumnSelection(
                        null,
                        yearRange,
                        newUser.dobYear.value
                    ) { newUser.dobYear.value = it }
                }

                //month section
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // selected value
                    Text(
                        text = newUser.dobMonth.value.toString(),
                        color = White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // label
                    Text(
                        text = "Month",
                        color = Silver,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    ScrollableColumnSelection(
                        null,
                        monthRange,
                        newUser.dobMonth.value
                    ) { newUser.dobMonth.value = it }
                }

                //day section
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // selected value
                    Text(
                        text = newUser.dobDay.value.toString(),
                        color = White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // label
                    Text(
                        text = "Day",
                        color = Silver,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    ScrollableColumnSelection(
                        null,
                        dayRange,
                        newUser.dobDay.value
                    ) { newUser.dobDay.value = it }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.navigate(nextRoute) },
                enabled = newUser.dobYear.value in yearRange &&
                        newUser.dobMonth.value in monthRange &&
                        newUser.dobDay.value in dayRange,
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
                Text("Continue", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun getDayRange(month: Int, year: Int): IntRange {
    return when (month) {
        4, 6, 9, 11 -> 1..30
        2 -> if (isLeapYear(year)) 1..29 else 1..28
        else -> 1..31
    }
}

fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0))
}