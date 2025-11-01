package com.teamnotfound.airise.auth.onboarding.onboardingQuestions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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

@Composable
fun WorkoutRestrictionsScreen(navController: NavController, newUser: UserDataUiState) {
    val questionText = "Do you have any restrictions or injuries?"
    val options = listOf("Yes", "No")
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var textInput by remember { mutableStateOf("") }
    val nextScreen = OnboardingScreens.HeightSelection.route

    OnboardingScaffold(
        stepTitle = "Fitness Goal (9/13)",
        onBackClick = { navController.popBackStack() },
        onSkipClick = { navController.navigate(nextScreen) }
    ) {
        // Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgBlack)
        ) {
            // Title
            Text(
                text = questionText,
                style = TextStyle(fontSize = 30.sp, color = White, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Radio options
            val optionSubtext = mapOf(
                "Yes" to "You have an injury or condition that affects your workouts.",
                "No" to "No injuries or restrictions that limit your exercise."
            )

            options.forEachIndexed { index, option ->
                val subtext = optionSubtext[option].orEmpty()

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

                    if (subtext.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = subtext,
                            color = Silver,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 40.dp)
                        )
                    }
                }

                if (index != options.lastIndex) {
                    Divider(color = Silver.copy(alpha = 0.5f), thickness = 0.8.dp)
                }
            }

            // Conditional text input
            if (selectedOption == "Yes") {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            "Enter here - Specify any unique limitation and/or concerns",
                            color = Silver
                        )
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

            // Continue button
            Button(
                onClick = {
                    newUser.workoutRestrictions.value =
                        if (selectedOption == "Yes") textInput else selectedOption!!
                    navController.navigate(nextScreen)
                },
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
