package com.teamnotfound.airise.auth.onboarding.onboardingQuestions

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teamnotfound.airise.data.serializable.UserDataUiState
import com.teamnotfound.airise.util.*

// Name input screen
@Composable
fun NameInputScreen(navController: NavController, newUser: UserDataUiState){
    val nextScreen = OnboardingScreens.WorkoutGoal.route
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                backgroundColor = BgBlack,
                contentColor = White,
                elevation = 0.dp
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        "Fitness Goal (1/13)",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .testTag("backButton") //test
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Orange
                        )
                    }


                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Please enter your name",
                style = TextStyle(fontSize = 30.sp, color = White, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                placeholder = {
                    Text("First Name", color = Silver)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("firstName"), //test
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = White,
                    focusedBorderColor = Silver,
                    unfocusedBorderColor = Silver,
                    textColor = Silver,
                    cursorColor = Silver,
                    focusedLabelColor = Silver
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = middleName,
                onValueChange = { middleName = it },
                placeholder = {
                    Text("Middle Name (Optional)", color = Silver)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = White,
                    focusedBorderColor = Silver,
                    unfocusedBorderColor = Silver,
                    textColor = Silver,
                    cursorColor = Silver,
                    focusedLabelColor = Silver
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                placeholder = {
                    Text("Last Name", color = Silver)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("lastName"), //test
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = White,
                    focusedBorderColor = Silver,
                    unfocusedBorderColor = Silver,
                    textColor = Silver,
                    cursorColor = Silver,
                    focusedLabelColor = Silver
                )
            )

            Spacer(modifier = Modifier.height(30.dp))
            
            Button(
                onClick = {
                    newUser.firstName.value = firstName
                    newUser.middleName.value = middleName
                    newUser.lastName.value = lastName
                    navController.navigate(nextScreen)
                },
                enabled = firstName.isNotBlank() && lastName.isNotBlank(),
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
                    .testTag("continueButton") //test
            ) {
                Text("Continue", color = White)
            }
        }
    }
}