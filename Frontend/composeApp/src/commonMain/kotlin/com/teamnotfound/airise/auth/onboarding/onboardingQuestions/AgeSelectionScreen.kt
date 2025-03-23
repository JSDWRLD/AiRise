package com.teamnotfound.airise.auth.onboarding.onboardingQuestions

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teamnotfound.airise.data.serializable.UserData

/*
 * Page to select user date of birth
 */
@Composable
fun AgeSelectionScreen(navController: NavController, newUser: UserData) {
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
    //
    val showDialog = remember { mutableStateOf(false) }
    val nextScreen = OnboardingScreens.ActivityLevel.route

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
                title = {Text("Fitness Goal (12/13)")},
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
                text = "What Is Your Date of Birth?",
                fontSize = 22.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp)
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
            onClick = {
                showDialog.value = true
                navController.navigate(nextScreen)},
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.8f)
                .padding(16.dp),
            enabled = newUser.dobYear.value in yearRange &&
                    newUser.dobMonth.value in monthRange &&
                    newUser.dobDay.value in dayRange,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF21565C))
        ) {
            Text("Continue", fontSize = 18.sp, color = Color.White)
        }
    }

    // Temp display to show selected Date of Birth
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Your Date of Birth Selection") },
            text = {
                Text("Date of Birth: ${newUser.dobYear.value}/${newUser.dobMonth.value}/${newUser.dobDay.value}")
            },
            confirmButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text("OK")
                }
            }
        )
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
