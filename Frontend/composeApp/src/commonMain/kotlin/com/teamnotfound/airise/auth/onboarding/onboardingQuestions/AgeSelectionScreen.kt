package com.teamnotfound.airise.auth.onboarding.onboardingQuestions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teamnotfound.airise.auth.onboarding.OnboardingScaffold
import com.teamnotfound.airise.data.serializable.UserDataUiState
import com.teamnotfound.airise.util.*
import kotlinx.datetime.*

@Composable
fun AgeSelectionScreen(navController: NavController, nextScreen: String, newUser: UserDataUiState) {
    val monthRange = (1..12).toList()
    val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    val yearRange = (currentYear - 150..currentYear).toList().reversed()
    val dayRange = remember(newUser.dobMonth.value, newUser.dobYear.value) {
        getDayRange(newUser.dobMonth.value, newUser.dobYear.value).toList()
    }

    OnboardingScaffold(
        stepTitle = "Fitness Goal (12/13)",
        onBackClick = { navController.popBackStack() },
        onSkipClick = { navController.navigate(nextScreen) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgBlack)
        ) {
            Text(
                text = "Select Your Date of Birth",
                style = TextStyle(fontSize = 30.sp, color = White, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = newUser.dobYear.value.toString(),
                        color = White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Year", color = Silver, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    ScrollableColumnSelection(null, yearRange, newUser.dobYear.value) {
                        newUser.dobYear.value = it
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = newUser.dobMonth.value.toString(),
                        color = White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Month", color = Silver, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    ScrollableColumnSelection(null, monthRange, newUser.dobMonth.value) {
                        newUser.dobMonth.value = it
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = newUser.dobDay.value.toString(),
                        color = White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Day", color = Silver, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    ScrollableColumnSelection(null, dayRange, newUser.dobDay.value) {
                        newUser.dobDay.value = it
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.navigate(nextScreen) },
                enabled = newUser.dobYear.value in yearRange &&
                        newUser.dobMonth.value in monthRange &&
                        newUser.dobDay.value in dayRange,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (
                        newUser.dobYear.value in yearRange &&
                        newUser.dobMonth.value in monthRange &&
                        newUser.dobDay.value in dayRange) Orange else Silver,
                    disabledBackgroundColor = DeepBlue
                ),
                border = BorderStroke(1.dp, Orange),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
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

// helpers unchanged
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
