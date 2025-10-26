package com.teamnotfound.airise.customize

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.serializable.HealthData
import com.teamnotfound.airise.util.*
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@Composable
fun TDEEWidget(
    modifier: Modifier = Modifier,
    userClient: UserClient? = null,
    firebaseUser: FirebaseUser? = null
) {
    var uiState by remember { mutableStateOf(TDEEUiState()) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "TDEE Calculator",
            color = White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Calculate your Total Daily Energy Expenditure and set your calorie goal.",
            color = Silver,
            fontSize = 13.sp
        )

        if (!uiState.isCalculated) {
            // Input Mode
            InputMode(
                state = uiState,
                onStateChange = { uiState = it },
                onCalculate = {
                    // Convert imperial units to metric for calculation
                    val heightCm = feetInchesToCm(
                        uiState.heightFeet.toIntOrNull() ?: 0,
                        uiState.heightInches.toIntOrNull() ?: 0
                    )
                    val weightKg = lbsToKg(uiState.weightLbs.toDoubleOrNull() ?: 0.0)
                    
                    val tdee = calculateTDEE(
                        gender = uiState.gender,
                        goalType = uiState.goalType,
                        heightCm = heightCm,
                        weightKg = weightKg,
                        age = uiState.age.toIntOrNull() ?: 0,
                        activityLevel = uiState.activityLevel
                    )
                    uiState = uiState.copy(
                        isCalculated = true,
                        calculatedCalories = tdee
                    )
                }
            )
        } else {
            // Result Mode
            ResultMode(
                calories = uiState.calculatedCalories,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                successMessage = uiState.successMessage,
                onSet = {
                    if (userClient != null && firebaseUser != null) {
                        uiState = uiState.copy(isLoading = true, errorMessage = null, successMessage = null)
                        scope.launch {
                            // First, fetch existing health data to preserve other fields
                            val existingDataResult = userClient.getHealthData(firebaseUser)
                            
                            val result = when (existingDataResult) {
                                is Result.Success -> {
                                    // Update only the caloriesTarget field, preserving all other data
                                    val updatedHealthData = existingDataResult.data.copy(
                                        caloriesTarget = uiState.calculatedCalories
                                    )
                                    userClient.updateHealthData(firebaseUser, updatedHealthData)
                                }
                                is Result.Error -> {
                                    // If no existing data, create new with just caloriesTarget
                                    userClient.updateHealthData(
                                        firebaseUser,
                                        HealthData(caloriesTarget = uiState.calculatedCalories)
                                    )
                                }
                            }
                            
                            uiState = when (result) {
                                is Result.Success -> uiState.copy(
                                    isLoading = false,
                                    successMessage = "Calorie goal set successfully!",
                                    errorMessage = null
                                )
                                is Result.Error -> uiState.copy(
                                    isLoading = false,
                                    errorMessage = "Failed to set goal. Please try again.",
                                    successMessage = null
                                )
                            }
                        }
                    } else {
                        uiState = uiState.copy(
                            errorMessage = "Unable to save. Please ensure you're logged in."
                        )
                    }
                },
                onGoBack = {
                    uiState = TDEEUiState()
                }
            )
        }
    }
}

@Composable
private fun InputMode(
    state: TDEEUiState,
    onStateChange: (TDEEUiState) -> Unit,
    onCalculate: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Gender Selection
        Text(
            text = "Gender",
            color = White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioOption(
                text = "Male",
                selected = state.gender == "Male",
                onClick = { onStateChange(state.copy(gender = "Male")) },
                modifier = Modifier.weight(1f)
            )
            RadioOption(
                text = "Female",
                selected = state.gender == "Female",
                onClick = { onStateChange(state.copy(gender = "Female")) },
                modifier = Modifier.weight(1f)
            )
        }

        // Goal Type Selection
        Text(
            text = "Goal Type",
            color = White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioOption(
                text = "Bulk",
                selected = state.goalType == "Bulk",
                onClick = { onStateChange(state.copy(goalType = "Bulk")) },
                modifier = Modifier.weight(1f)
            )
            RadioOption(
                text = "Cut",
                selected = state.goalType == "Cut",
                onClick = { onStateChange(state.copy(goalType = "Cut")) },
                modifier = Modifier.weight(1f)
            )
            RadioOption(
                text = "Maintain",
                selected = state.goalType == "Maintain",
                onClick = { onStateChange(state.copy(goalType = "Maintain")) },
                modifier = Modifier.weight(1f)
            )
        }

        // Age Input
        Text(
            text = "Age (years)",
            color = White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = state.age,
            onValueChange = { onStateChange(state.copy(age = it.filter { c -> c.isDigit() })) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = White,
                backgroundColor = BgBlack,
                cursorColor = Cyan,
                focusedBorderColor = Cyan,
                unfocusedBorderColor = DeepBlue
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Height Input (Feet and Inches)
        Text(
            text = "Height",
            color = White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Feet Input
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Feet",
                    color = Silver,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = state.heightFeet,
                    onValueChange = {
                        val filtered = it.filter { c -> c.isDigit() }
                        if (filtered.isEmpty() || filtered.toIntOrNull()?.let { it in 0..8 } == true) {
                            onStateChange(state.copy(heightFeet = filtered))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = White,
                        backgroundColor = BgBlack,
                        cursorColor = Cyan,
                        focusedBorderColor = Cyan,
                        unfocusedBorderColor = DeepBlue
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text("0", color = Silver.copy(alpha = 0.5f))
                    }
                )
            }
            
            // Inches Input
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Inches",
                    color = Silver,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = state.heightInches,
                    onValueChange = {
                        val filtered = it.filter { c -> c.isDigit() }
                        if (filtered.isEmpty() || filtered.toIntOrNull()?.let { it in 0..11 } == true) {
                            onStateChange(state.copy(heightInches = filtered))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = White,
                        backgroundColor = BgBlack,
                        cursorColor = Cyan,
                        focusedBorderColor = Cyan,
                        unfocusedBorderColor = DeepBlue
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text("0", color = Silver.copy(alpha = 0.5f))
                    }
                )
            }
        }

        // Weight Input (Pounds)
        Text(
            text = "Weight (lbs)",
            color = White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = state.weightLbs,
            onValueChange = { onStateChange(state.copy(weightLbs = it.filter { c -> c.isDigit() || c == '.' })) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = White,
                backgroundColor = BgBlack,
                cursorColor = Cyan,
                focusedBorderColor = Cyan,
                unfocusedBorderColor = DeepBlue
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = {
                Text("0", color = Silver.copy(alpha = 0.5f))
            }
        )

        // Activity Level Dropdown
        Text(
            text = "Activity Level",
            color = White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        ActivityLevelDropdown(
            selectedLevel = state.activityLevel,
            onLevelSelected = { onStateChange(state.copy(activityLevel = it)) }
        )

        // Calculate Button
        val isValid = state.gender.isNotBlank() &&
                state.goalType.isNotBlank() &&
                state.age.isNotBlank() &&
                state.heightFeet.isNotBlank() &&
                state.heightInches.isNotBlank() &&
                state.weightLbs.isNotBlank() &&
                state.activityLevel.isNotBlank()

        Button(
            onClick = onCalculate,
            enabled = isValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (isValid) DeepBlue else DeepBlue.copy(alpha = 0.5f),
                contentColor = White,
                disabledBackgroundColor = DeepBlue.copy(alpha = 0.3f),
                disabledContentColor = Silver
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isValid) Cyan else DeepBlue)
        ) {
            Text(
                text = "Calculate",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ResultMode(
    calories: Int,
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onSet: () -> Unit,
    onGoBack: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Result Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DeepBlue.copy(alpha = 0.3f))
                .border(BorderStroke(1.dp, Cyan), RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Target Calories",
                    color = Silver,
                    fontSize = 14.sp
                )
                Text(
                    text = "$calories",
                    color = NeonGreen,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "calories/day",
                    color = Silver,
                    fontSize = 13.sp
                )
            }
        }

        // Success/Error Messages
        AnimatedVisibility(visible = successMessage != null) {
            Text(
                text = successMessage ?: "",
                color = NeonGreen,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonGreen.copy(alpha = 0.1f))
                    .padding(12.dp)
            )
        }

        AnimatedVisibility(visible = errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color(0xFFFF6B6B),
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFF6B6B).copy(alpha = 0.1f))
                    .padding(12.dp)
            )
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Go Back Button
            Button(
                onClick = onGoBack,
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = BgBlack,
                    contentColor = White,
                    disabledBackgroundColor = BgBlack.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DeepBlue)
            ) {
                Text(
                    text = "Go Back",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Set Button
            Button(
                onClick = onSet,
                enabled = !isLoading && successMessage == null,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = DeepBlue,
                    contentColor = White,
                    disabledBackgroundColor = DeepBlue.copy(alpha = 0.5f),
                    disabledContentColor = Silver
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Cyan)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Orange,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (successMessage != null) "Set ✓" else "Set",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RadioOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) DeepBlue.copy(alpha = 0.4f) else BgBlack,
        border = BorderStroke(1.dp, if (selected) Cyan else DeepBlue),
        modifier = modifier
            .height(44.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(visible = selected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                )
            }
            AnimatedVisibility(visible = selected) {
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (selected) White else Silver,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ActivityLevelDropdown(
    selectedLevel: String,
    onLevelSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val activityLevels = listOf(
        "Sedentary",
        "Lightly Active",
        "Moderately Active",
        "Very Active",
        "Extremely Active"
    )

    Box {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = BgBlack,
            border = BorderStroke(1.dp, if (expanded) Cyan else DeepBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedLevel.ifBlank { "Select activity level" },
                    color = if (selectedLevel.isBlank()) Silver else White,
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = if (expanded) Cyan else Silver
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(BgBlack)
        ) {
            activityLevels.forEach { level ->
                DropdownMenuItem(
                    onClick = {
                        onLevelSelected(level)
                        expanded = false
                    },
                    modifier = Modifier.background(
                        if (level == selectedLevel) DeepBlue.copy(alpha = 0.3f) else BgBlack
                    )
                ) {
                    Text(
                        text = level,
                        color = if (level == selectedLevel) Cyan else White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// Unit Conversion Functions
private fun feetInchesToCm(feet: Int, inches: Int): Double {
    val totalInches = (feet * 12) + inches
    return totalInches * 2.54
}

private fun lbsToKg(lbs: Double): Double {
    return lbs * 0.453592
}

// TDEE Calculation Logic
private fun calculateTDEE(
    gender: String,
    goalType: String,
    heightCm: Double,
    weightKg: Double,
    age: Int,
    activityLevel: String
): Int {
    // Calculate BMR using Mifflin-St Jeor Equation
    val bmr = if (gender == "Male") {
        (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5
    } else {
        (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161
    }

    // Activity multipliers
    val activityMultiplier = when (activityLevel) {
        "Sedentary" -> 1.2
        "Lightly Active" -> 1.375
        "Moderately Active" -> 1.55
        "Very Active" -> 1.725
        "Extremely Active" -> 1.9
        else -> 1.2
    }

    // Calculate TDEE
    val tdee = bmr * activityMultiplier

    // Adjust for goal
    val targetCalories = when (goalType) {
        "Bulk" -> tdee + 300  // Surplus for muscle gain
        "Cut" -> tdee - 500   // Deficit for fat loss
        "Maintain" -> tdee    // Maintenance
        else -> tdee
    }

    return targetCalories.toInt()
}

// UI State
private data class TDEEUiState(
    val gender: String = "",
    val goalType: String = "",
    val age: String = "",
    val heightFeet: String = "",
    val heightInches: String = "",
    val weightLbs: String = "",
    val activityLevel: String = "",
    val isCalculated: Boolean = false,
    val calculatedCalories: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)