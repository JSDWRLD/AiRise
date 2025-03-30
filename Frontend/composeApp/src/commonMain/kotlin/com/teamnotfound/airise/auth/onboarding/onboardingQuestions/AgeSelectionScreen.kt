package com.teamnotfound.airise.auth.onboarding.onboardingQuestions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
 * Page to select user date of birth
 */
@Composable
fun AgeSelectionScreen(navController: NavController, nextRoute: String, newUser: UserData) {
    // list ranges
    val monthRange = (1..12).toList()
    val yearRange = (1900..2025).toList().reversed()
    val dayRange = remember(newUser.dobMonth.value, newUser.dobYear.value) {
        newUser.dobMonth.value.let { month ->
            newUser.dobYear.value.let { year ->
                getDayRange(month, year).toList()
            }
        }
    }
    // body
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF091819)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(40.dp))
            // title
            Text(
                text = "What Is Your Date of Birth?",
                style = TextStyle(
                    fontSize = 30.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            // spacing
            Spacer(modifier = Modifier.height(16.dp))
            // date of birth scrolls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // year scroll
                ScrollableColumnSelection(
                    label = "Year",
                    items = yearRange,
                    selectedItem = newUser.dobYear.value,
                    onItemSelected = { newUser.dobYear.value = it }
                )
                // month scroll
                ScrollableColumnSelection(
                    label = "Month",
                    items = monthRange,
                    selectedItem = newUser.dobMonth.value,
                    onItemSelected = { newUser.dobMonth.value = it }
                )
                // day scroll
                ScrollableColumnSelection(
                    label = "Day",
                    items = dayRange,
                    selectedItem = newUser.dobDay.value,
                    onItemSelected = { newUser.dobDay.value = it }
                )
            }
        }
        // continue button
        Button(
            onClick = { navController.navigate(nextRoute) },
            enabled = newUser.dobYear.value in yearRange &&
                    newUser.dobMonth.value in monthRange &&
                    newUser.dobDay.value in dayRange,
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

// get day range based on month
fun getDayRange(month: Int, year: Int): IntRange {
    return when (month) {
        4, 6, 9, 11 -> 1..30  // months with 30 days
        2 -> if (isLeapYear(year)) 1..29 else 1..28  // february logic
        else -> 1..31  // months with 31 days
    }
}

// get leap year
fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0))
}
