package com.teamnotfound.airise.onboarding.onboardingQuestions

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teamnotfound.airise.data.serializable.UserData

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
            .background(Color(0xFF091819))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                backgroundColor = Color(0xFF091819),
                contentColor = Color.White,
                title = { Text("Fitness Goal (9/13)") },
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = questionText,
                style = TextStyle(fontSize = 30.sp, color = Color.White),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedOption = option }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedOption == option,
                        onClick = { selectedOption = option },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFFFFA500),
                            unselectedColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = option, color = Color.White)
                }
            }

            if (selectedOption == "Yes") {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("Enter here - Specify any unique limitations or concerns") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color.Gray,
                        unfocusedBorderColor = Color.Gray,
                        textColor = Color.Gray,
                        cursorColor = Color.Gray,
                        focusedLabelColor = Color.Gray
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