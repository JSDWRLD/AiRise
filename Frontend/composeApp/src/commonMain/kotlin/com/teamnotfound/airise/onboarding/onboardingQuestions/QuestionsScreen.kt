package com.teamnotfound.airise.onboarding.onboardingQuestions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController

// Single selection question screen
@Composable
fun QuestionScreen(
    questionText: String,
    options: List<String>,
    nextScreen: OnboardingScreens,
    navController: NavController
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF062022))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                backgroundColor = Color(0xFF062022),
                contentColor = Color.White,
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFFA500)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(nextScreen.route) }) {
                        Text("Skip", color = Color.White)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = questionText,
                style = TextStyle(fontSize = 24.sp, color = Color.White),
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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate(nextScreen.route) },
                enabled = selectedOption != null,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1B424B)),
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

// Multiple selection question screen
@Composable
fun MultiSelectQuestionScreen(
    questionText: String,
    options: List<String>,
    selectedOptions: MutableState<Set<String>>,
    nextScreen: OnboardingScreens,
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF062022))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                backgroundColor = Color(0xFF062022),
                contentColor = Color.White,
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFFA500)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(nextScreen.route) }) {
                        Text("Skip", color = Color.White)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = questionText,
                style = TextStyle(fontSize = 24.sp, color = Color.White),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newSelection = selectedOptions.value.toMutableSet()
                            if (newSelection.contains(option)) {
                                newSelection.remove(option)
                            } else {
                                newSelection.add(option)
                            }
                            selectedOptions.value = newSelection
                        }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedOptions.value.contains(option),
                        onCheckedChange = {
                            val newSelection = selectedOptions.value.toMutableSet()
                            if (newSelection.contains(option)) {
                                newSelection.remove(option)
                            } else {
                                newSelection.add(option)
                            }
                            selectedOptions.value = newSelection
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFFFA500),
                            uncheckedColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = option, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate(nextScreen.route) },
                enabled = selectedOptions.value.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1B424B)),
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

// Text input question screen (e.g., for specifying additional details)
@Composable
fun TextInputQuestionScreen(
    questionText: String,
    options: List<String>,
    nextScreen: OnboardingScreens,
    navController: NavController
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var textInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF062022))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                backgroundColor = Color(0xFF062022),
                contentColor = Color.White,
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFFA500)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(nextScreen.route) }) {
                        Text("Skip", color = Color.White)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = questionText,
                style = TextStyle(fontSize = 24.sp, color = Color.White),
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
                onClick = { navController.navigate(nextScreen.route) },
                enabled = selectedOption != null || textInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1B424B)),
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
