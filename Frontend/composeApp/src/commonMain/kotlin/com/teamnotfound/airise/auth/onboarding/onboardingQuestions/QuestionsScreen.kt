package com.teamnotfound.airise.auth.onboarding.onboardingQuestions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.teamnotfound.airise.util.*

// Single selection question screen
@Composable
fun QuestionScreen(
    questionText: String,
    options: List<String>,
    optionSubtext: Map<String, String> = emptyMap(),
    nextScreen: OnboardingScreens,
    navController: NavController,
    questionCount: Int,
    onSelection: (String) -> Unit,
    canSkip: Boolean = true
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }

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
                        "Fitness Goal ($questionCount/13)",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    if (questionCount != 1) {
                        Row(
                            modifier = Modifier.align(Alignment.CenterStart),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Orange
                                )
                            }
                        }
                    }

                    if (canSkip) {
                        TextButton(
                            onClick = { navController.navigate(nextScreen.route) },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Text(
                                "Skip",
                                color = Orange,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = questionText,
                style = TextStyle(
                    fontSize = 30.sp,
                    color = White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            options.forEachIndexed { index, option ->
                val subtext = optionSubtext[option] ?: ""

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
                            .padding(10.dp),
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

                    //formats sub descriptions if there are any
                    if (subtext.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = subtext, color = Silver, fontSize = 12.sp)
                    }
                }

                //divider inbetween each option for styling
                if (index != options.lastIndex) {
                    Divider(color = DeepBlue.copy(alpha = 0.5f), thickness = 0.8.dp)
                }
            }

            Button(
                onClick = {
                    onSelection(selectedOption!!)
                    navController.navigate(nextScreen.route)
                },
                enabled = selectedOption != null,
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
            ) {
                Text("Continue", color = White)
            }
        }
    }
}

// Multiple selection question screen
@Composable
fun MultiSelectQuestionScreen(
    questionText: String,
    options: List<String>,
    optionSubtext: Map<String, String> = emptyMap(),
    selectedOptions: MutableState<Set<String>>,
    nextScreen: OnboardingScreens,
    navController: NavController,
    questionCount: Int,
    onSelection: (Set<String>) -> Unit,
    canSkip: Boolean = true
) {
    // User must select 3 to 6 days for question 6
    val onQuestion6 = if (questionCount == 6) {
        selectedOptions.value.size in 3..6
    } else {
        selectedOptions.value.isNotEmpty()
    }

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
                        "Fitness Goal ($questionCount/13)",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)

                    )
                    if (questionCount != 1) {
                        Row(
                            modifier = Modifier.align(Alignment.CenterStart),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Orange
                                )
                            }
                        }
                    }

                    if (canSkip) {
                        TextButton(
                            onClick = { navController.navigate(nextScreen.route) },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Text(
                                "Skip",
                                color = Orange,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = questionText,
                style = TextStyle(
                    fontSize = 30.sp,
                    color = White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            options.forEachIndexed { index, option ->
                val subtext = optionSubtext[option] ?: ""
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
                            checkedColor = Orange,
                            uncheckedColor = White
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
                        modifier = Modifier.padding(start = 30.dp)
                    )
                }

                if (index != options.lastIndex) {
                    Divider(color = DeepBlue.copy(alpha = 0.5f), thickness = 0.8.dp)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            //continue button styling
            Button(
                onClick = {
                    onSelection(selectedOptions.value)
                    navController.navigate(nextScreen.route)
                },
                enabled = onQuestion6,
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
            ) {
                Text("Continue", color = White)
            }
        }
    }
}