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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teamnotfound.airise.auth.onboarding.OnboardingScaffold
import com.teamnotfound.airise.data.serializable.UserDataUiState
import com.teamnotfound.airise.util.*

// Name input screen
@Composable
fun NameInputScreen(navController: NavController, newUser: UserDataUiState) {
    val nextScreen = OnboardingScreens.WorkoutGoal.route
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    // Validation states
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var middleNameError by remember { mutableStateOf<String?>(null) }

    // Validation functions
    fun validateFirstName(input: String): String? {
        return when {
            input.isBlank() -> "First name is required"
            input.trim().length < 2 -> "First name must be at least 2 characters"
            input.trim().length > 50 -> "First name cannot exceed 50 characters"
            !input.matches("^[A-Za-zÀ-ÿ\\s'-.]+\$".toRegex()) -> "First name can only contain letters, spaces, hyphens, apostrophes, and periods"
            input.trim().matches(".*\\d.*".toRegex()) -> "First name cannot contain numbers"
            else -> null
        }
    }

    fun validateLastName(input: String): String? {
        return when {
            input.isBlank() -> "Last name is required"
            input.trim().length < 2 -> "Last name must be at least 2 characters"
            input.trim().length > 50 -> "Last name cannot exceed 50 characters"
            !input.matches("^[A-Za-zÀ-ÿ\\s'-.]+\$".toRegex()) -> "Last name can only contain letters, spaces, hyphens, apostrophes, and periods"
            input.trim().matches(".*\\d.*".toRegex()) -> "Last name cannot contain numbers"
            else -> null
        }
    }

    fun validateMiddleName(input: String): String? {
        return when {
            input.isNotBlank() && input.trim().length < 2 -> "Middle name must be at least 2 characters if provided"
            input.trim().length > 50 -> "Middle name cannot exceed 50 characters"
            input.isNotBlank() && !input.matches("^[A-Za-zÀ-ÿ\\s'-.]+\$".toRegex()) -> "Middle name can only contain letters, spaces, hyphens, apostrophes, and periods"
            input.isNotBlank() && input.trim().matches(".*\\d.*".toRegex()) -> "Middle name cannot contain numbers"
            else -> null
        }
    }

    fun isFormValid(): Boolean {
        return firstNameError == null &&
                lastNameError == null &&
                middleNameError == null &&
                firstName.isNotBlank() &&
                lastName.isNotBlank()
    }

    // Input filtering functions
    fun filterNameInput(input: String): String {
        // Remove unwanted characters but allow letters, spaces, hyphens, apostrophes, and periods
        return input.replace("[^A-Za-zÀ-ÿ\\s'-.]".toRegex(), "")
    }

    fun onFirstNameChange(newValue: String) {
        val filtered = filterNameInput(newValue)
        firstName = filtered
        firstNameError = validateFirstName(filtered)
    }

    fun onMiddleNameChange(newValue: String) {
        val filtered = filterNameInput(newValue)
        middleName = filtered
        middleNameError = validateMiddleName(filtered)
    }

    fun onLastNameChange(newValue: String) {
        val filtered = filterNameInput(newValue)
        lastName = filtered
        lastNameError = validateLastName(filtered)
    }

    OnboardingScaffold(
        stepTitle = "Fitness Goal (1/13)",
        onBackClick = { navController.popBackStack() },
        onSkipClick = null // no skip on this screen
    ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Please enter your name",
                style = TextStyle(fontSize = 30.sp, color = White, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // First Name Field
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = ::onFirstNameChange,
                    placeholder = {
                        Text("First Name", color = Silver)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firstName"),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = White,
                        focusedBorderColor = if (firstNameError != null) Color.Red else Orange,
                        unfocusedBorderColor = if (firstNameError != null) Color.Red else Silver,
                        textColor = BgBlack,
                        cursorColor = Orange,
                        focusedLabelColor = Orange
                    ),
                    isError = firstNameError != null,
                    singleLine = true,
                )
                firstNameError?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Middle Name Field
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = middleName,
                    onValueChange = ::onMiddleNameChange,
                    placeholder = {
                        Text("Middle Name (Optional)", color = Silver)
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = White,
                        focusedBorderColor = if (middleNameError != null) Color.Red else Orange,
                        unfocusedBorderColor = if (middleNameError != null) Color.Red else Silver,
                        textColor = BgBlack,
                        cursorColor = Orange,
                        focusedLabelColor = Orange
                    ),
                    isError = middleNameError != null,
                    singleLine = true
                )
                middleNameError?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Last Name Field
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = lastName,
                    onValueChange = ::onLastNameChange,
                    placeholder = {
                        Text("Last Name", color = Silver)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lastName"),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = White,
                        focusedBorderColor = if (lastNameError != null) Color.Red else Orange,
                        unfocusedBorderColor = if (lastNameError != null) Color.Red else Silver,
                        textColor = BgBlack,
                        cursorColor = Orange,
                        focusedLabelColor = Orange
                    ),
                    isError = lastNameError != null,
                    singleLine = true
                )
                lastNameError?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    // Final validation before proceeding
                    firstNameError = validateFirstName(firstName)
                    lastNameError = validateLastName(lastName)
                    middleNameError = validateMiddleName(middleName)

                    if (isFormValid()) {
                        newUser.firstName.value = firstName.trim()
                        newUser.middleName.value = middleName.trim()
                        newUser.lastName.value = lastName.trim()
                        navController.navigate(nextScreen)
                    }
                },
                enabled = isFormValid(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (isFormValid()) Orange else DeepBlue,
                    disabledBackgroundColor = DeepBlue
                ),
                border = BorderStroke(1.dp, if (isFormValid()) Orange else Silver),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
                    .testTag("continueButton")
            ) {
                Text(
                    "Continue",
                    color = if (isFormValid()) BgBlack else Silver,
                    fontWeight = if (isFormValid()) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Help text
            Text(
                text = "• Names should only contain letters, spaces, hyphens (-), apostrophes ('), and periods (.)\n" +
                        "• First and last names are required\n" +
                        "• Middle name is optional",
                color = Silver,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
    }
}