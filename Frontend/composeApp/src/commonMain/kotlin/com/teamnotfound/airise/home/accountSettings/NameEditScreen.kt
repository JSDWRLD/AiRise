package com.teamnotfound.airise.home.accountSettings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.teamnotfound.airise.data.serializable.UserDataUiState
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.Silver

@Composable
fun NameEditScreen(
    localNavController: NavHostController,
    user: UserDataUiState,
    accountSettingViewModel: AccountSettingsViewModel
) {
    var newFirstName by remember { mutableStateOf(user.firstName.value) }
    var newMiddleName by remember { mutableStateOf(user.middleName.value) }
    var newLastName by remember { mutableStateOf(user.lastName.value) }

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
                newFirstName.isNotBlank() &&
                newLastName.isNotBlank()
    }

    // Input filtering functions
    fun filterNameInput(input: String): String {
        // Remove unwanted characters but allow letters, spaces, hyphens, apostrophes, and periods
        return input.replace("[^A-Za-zÀ-ÿ\\s'-.]".toRegex(), "")
    }

    fun onFirstNameChange(newValue: String) {
        val filtered = filterNameInput(newValue)
        newFirstName = filtered
        firstNameError = validateFirstName(filtered)
    }

    fun onMiddleNameChange(newValue: String) {
        val filtered = filterNameInput(newValue)
        newMiddleName = filtered
        middleNameError = validateMiddleName(filtered)
    }

    fun onLastNameChange(newValue: String) {
        val filtered = filterNameInput(newValue)
        newLastName = filtered
        lastNameError = validateLastName(filtered)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SettingsTopBar(
                title = "Edit Name",
                subtitle = "Update your profile name",
                onBackClick = { localNavController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Current Name",
                    fontSize = 16.sp,
                    color = Silver,
                    modifier = Modifier.align(Alignment.Start)
                )

                Text(
                    text = "${user.firstName.value} ${user.middleName.value} ${user.lastName.value}",
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 32.dp)
                )

                Text(
                    text = "Edit Your Name",
                    fontSize = 16.sp,
                    color = Silver,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // First Name (Mandatory)
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newFirstName,
                        onValueChange = ::onFirstNameChange,
                        label = {
                            Text("First Name *", color = Silver)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            cursorColor = Orange,
                            focusedBorderColor = if (firstNameError != null) Color.Red else Orange,
                            unfocusedBorderColor = if (firstNameError != null) Color.Red else DeepBlue,
                            focusedLabelColor = if (firstNameError != null) Color.Red else Orange,
                            unfocusedLabelColor = Silver
                        ),
                        isError = firstNameError != null,
                        singleLine = true
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

                // Middle Name (Optional)
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newMiddleName,
                        onValueChange = ::onMiddleNameChange,
                        label = {
                            Text("Middle Name", color = Silver)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            cursorColor = Orange,
                            focusedBorderColor = if (middleNameError != null) Color.Red else Orange,
                            unfocusedBorderColor = if (middleNameError != null) Color.Red else DeepBlue,
                            focusedLabelColor = if (middleNameError != null) Color.Red else Orange,
                            unfocusedLabelColor = Silver
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

                // Last Name (Mandatory)
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newLastName,
                        onValueChange = ::onLastNameChange,
                        label = {
                            Text("Last Name *", color = Silver)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            cursorColor = Orange,
                            focusedBorderColor = if (lastNameError != null) Color.Red else Orange,
                            unfocusedBorderColor = if (lastNameError != null) Color.Red else DeepBlue,
                            focusedLabelColor = if (lastNameError != null) Color.Red else Orange,
                            unfocusedLabelColor = Silver
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

                // Help text
                Text(
                    text = "• Names should only contain letters, spaces, hyphens (-), apostrophes ('), and periods (.)\n" +
                            "• First and last names are required (2-50 characters)\n" +
                            "• Middle name is optional (2-50 characters if provided)",
                    color = Silver,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Continue Button
                Button(
                    onClick = {
                        // Final validation before saving
                        firstNameError = validateFirstName(newFirstName)
                        lastNameError = validateLastName(newLastName)
                        middleNameError = validateMiddleName(newMiddleName)

                        if (isFormValid()) {
                            user.firstName.value = newFirstName.trim()
                            user.middleName.value = newMiddleName.trim()
                            user.lastName.value = newLastName.trim()
                            user.fullName.value = "$newFirstName $newMiddleName $newLastName".trim()

                            accountSettingViewModel.saveUserData(user)

                            localNavController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (isFormValid()) Orange else DeepBlue
                    ),
                    enabled = isFormValid()
                ) {
                    Text(
                        text = "Confirm Changes",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}