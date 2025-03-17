package com.teamnotfound.airise.onboardingQuestions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

//Question template for single selection questions
@Composable
fun QuestionScreen(
    questionText: String,
    options: List<String>,
    nextScreen: OnboardingScreens,
    navController: NavController
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        TopAppBar(
            title = { Text("") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowLeft, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = {navController.navigate(nextScreen.route)}) {
                    Text("Skip")
                }
            }
        )

        Text(text = questionText, style = TextStyle(fontSize = 50.sp))

        options.forEach { option ->
            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedOption = option }
                .padding(10.dp)
            ) {
                RadioButton(selected = selectedOption == option, onClick = { selectedOption = option })
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = option, modifier = Modifier.align(Alignment.CenterVertically))
            }
        }

        Button(
            onClick = { navController.navigate(nextScreen.route) },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedOption != null
        ) {
            Text("Continue")
        }
    }
}

//Question template for multiple selection questions
@Composable
fun MultiSelectQuestionScreen(
    questionText: String,
    options: List<String>,
    selectedOptions: MutableState<Set<String>>,
    nextScreen: OnboardingScreens,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        TopAppBar(
            title = { Text("") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowLeft, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = {navController.navigate(nextScreen.route)}) {
                    Text("Skip")
                }
            }
        )

        Text(text = questionText, style = TextStyle(fontSize = 50.sp))

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
                    .padding(10.dp)
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
                    }
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = option, modifier = Modifier.align(Alignment.CenterVertically))
            }
        }

        Button(
            onClick = { navController.navigate(nextScreen.route) },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedOptions.value.isNotEmpty()
        ) {
            Text("Continue")
        }
    }
}

//question template for text input questions (RestrictionInjury for now)
@Composable
fun TextInputQuestionScreen(
    questionText: String,
    options: List<String>,
    nextScreen: OnboardingScreens,
    navController: NavController
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var textInput by remember { mutableStateOf("") }  // Keep track of the text input
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        TopAppBar(
            title = { Text("") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowLeft, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { navController.navigate(nextScreen.route) }) {
                    Text("Skip")
                }
            }
        )

        Text(text = questionText, style = TextStyle(fontSize = 50.sp))

        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedOption = option }
                    .padding(10.dp)
            ) {
                RadioButton(selected = selectedOption == option, onClick = { selectedOption = option })
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = option, modifier = Modifier.align(Alignment.CenterVertically))
            }
        }

        if (selectedOption == "Yes") {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Enter here - Specify any unique limitations or concerns") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp)
            )
        }

        Button(
            onClick = { navController.navigate(nextScreen.route) },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedOption != null || textInput.isNotBlank()
        ) {
            Text("Continue")
        }
    }
}