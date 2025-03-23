package com.teamnotfound.airise.onboarding.onboardingQuestions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teamnotfound.airise.data.serializable.UserOnboarding

// Name input screen
@Composable
fun NameInputScreen(navController: NavController, newUser: UserOnboarding){
    val nextScreen = OnboardingScreens.WorkoutGoal.route
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF091819))
    ) {
        Column(modifier = Modifier.fillMaxSize()){
            TopAppBar(
                backgroundColor = Color(0xFF091819),
                contentColor = Color.White,
                title = { Text("Fitness Goal (1/13)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }){
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFCE5100)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(nextScreen) }){
                        Text("Skip", color = Color(0xFFCE5100))
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Please enter your name",
                style = TextStyle(fontSize = 30.sp, color = Color.White),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    textColor = Color.Gray,
                    cursorColor = Color.Gray,
                    focusedLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = middleName,
                onValueChange = { middleName = it },
                label = { Text("Middle Name (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    textColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    textColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    newUser.firstName.value = firstName
                    newUser.middleName.value = middleName
                    newUser.lastName.value = lastName
                    navController.navigate(nextScreen)
                },
                enabled = firstName.isNotBlank() && lastName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF21565C),
                    disabledBackgroundColor = Color(0xFF21565C)
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Continue", color = Color.White)
            }
        }
    }
}