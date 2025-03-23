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
    navController: NavController,
    questionCount: Int,
    onSelection: (String) -> Unit
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF091819))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                backgroundColor = Color(0xFF091819),
                contentColor = Color.White,
                title = {Text("Fitness Goal ($questionCount/13)")},
                navigationIcon = {
                    if (questionCount != 1) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFFCE5100)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(nextScreen.route) }) {
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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onSelection(selectedOption!!)
                    navController.navigate(nextScreen.route) },
                enabled = selectedOption != null,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF21565C),
                    disabledBackgroundColor = Color(0xFF21565C)),
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
    navController: NavController,
    questionCount: Int,
    onSelection: (Set<String>) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF091819))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                backgroundColor = Color(0xFF091819),
                contentColor = Color.White,
                title = {Text("Fitness Goal ($questionCount/13)")},
                navigationIcon = {
                    if (questionCount != 1) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFFCE5100)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(nextScreen.route) }) {
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
                onClick = {
                    onSelection(selectedOptions.value)
                    navController.navigate(nextScreen.route) },
                enabled = selectedOptions.value.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF21565C),
                    disabledBackgroundColor = Color(0xFF21565C)),
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
