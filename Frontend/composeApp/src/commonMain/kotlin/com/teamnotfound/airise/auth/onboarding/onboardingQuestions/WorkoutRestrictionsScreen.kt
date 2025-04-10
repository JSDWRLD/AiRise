package com.teamnotfound.airise.auth.onboarding.onboardingQuestions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

// Text input question screen for any workout restrictions
@Composable
fun WorkoutRestrictionsScreen( navController: NavController, newUser: UserData) {
    val questionText = "Do you have any restrictions or injuries?"
    val options = listOf("Yes", "No")
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var textInput by remember { mutableStateOf("") }
    val nextScreen = OnboardingScreens.HeightSelection.route

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                backgroundColor = BgBlack,
                contentColor = White,
                elevation = 0.dp,
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    // Center title
                    Text(
                        "Fitness Goal (9/13)",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Orange
                        )
                    }

                    TextButton(
                        onClick = { navController.navigate(nextScreen) },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text(
                            "Skip",
                            color = Orange,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            //question formatting
            Text(
                text = questionText,
                style = TextStyle(fontSize = 30.sp, color = White, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val optionSubtext = mapOf(
                "Yes" to "You have an injury or condition that affects your workouts.",
                "No" to "No injuries or restrictions that limit your exercise."
            )

            options.forEachIndexed { index, option ->
                val subtext = optionSubtext[option] ?: ""

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedOption = option }
                        .padding(start = 24.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = option }
                            .padding(start = 24.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == option,
                            onClick = { selectedOption = option },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Orange,
                                unselectedColor = White
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = option,
                            color = White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                //formatting for subtext
                if (subtext.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = subtext,
                        color = Silver,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 40.dp)
                    )
                }
                //divider
                if (index != options.lastIndex) {
                    androidx.compose.material.Divider(
                        color = Silver.copy(alpha = 0.5f),
                        thickness = 0.8.dp
                    )
                }
            }

            if (selectedOption == "Yes") {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text("Enter here - Specify any unique limitation and/or concerns", color = Silver)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = White,
                        focusedBorderColor = Silver,
                        unfocusedBorderColor = Silver,
                        textColor = Silver,
                        cursorColor = Silver,
                        focusedLabelColor = Silver
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    newUser.workoutRestrictions.value = if (selectedOption == "Yes") textInput else selectedOption!!
                    navController.navigate(nextScreen) },
                enabled = (selectedOption != null && selectedOption != "Yes") || textInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = DeepBlue,
                    disabledBackgroundColor = DeepBlue
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Orange),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text("Continue", color = White)
            }
        }
    }
}