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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.teamnotfound.airise.auth.onboarding.OnboardingScaffold
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

    OnboardingScaffold(
        stepTitle = "Fitness Goal ($questionCount/13)",
        onBackClick = { navController.popBackStack() },
        onSkipClick = if (canSkip) ({ navController.navigate(nextScreen.route) }) else null
    ) {
        Text(
            text = questionText,
            style = TextStyle(fontSize = 30.sp, color = White, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        options.forEachIndexed { index, option ->
            val subtext = optionSubtext[option].orEmpty()

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
                    Spacer(Modifier.width(10.dp))
                    Text(option, color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                if (subtext.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(subtext, color = Silver, fontSize = 12.sp)
                }
            }

            if (index != options.lastIndex) {
                Divider(color = DeepBlue.copy(alpha = 0.5f), thickness = 0.8.dp)
            }
        }

        Spacer(Modifier.height(16.dp))

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
                .testTag("continueButton")
        ) {
            Text("Continue", color = White)
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
    // For Q6 user must select 3..6 days, otherwise at least 1
    val enabled = if (questionCount == 6) {
        selectedOptions.value.size in 3..6
    } else {
        selectedOptions.value.isNotEmpty()
    }

    OnboardingScaffold(
        stepTitle = "Fitness Goal ($questionCount/13)",
        onBackClick = { navController.popBackStack() },
        onSkipClick = if (canSkip) ({ navController.navigate(nextScreen.route) }) else null
    ) {
        Text(
            text = questionText,
            style = TextStyle(fontSize = 30.sp, color = White, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        options.forEachIndexed { index, option ->
            val subtext = optionSubtext[option].orEmpty()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val s = selectedOptions.value.toMutableSet()
                        if (!s.add(option)) s.remove(option)
                        selectedOptions.value = s
                    }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedOptions.value.contains(option),
                    onCheckedChange = {
                        val s = selectedOptions.value.toMutableSet()
                        if (!s.add(option)) s.remove(option)
                        selectedOptions.value = s
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Orange,
                        uncheckedColor = White
                    )
                )
                Spacer(Modifier.width(10.dp))
                Text(option, color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            if (subtext.isNotEmpty()) {
                Spacer(Modifier.height(1.dp))
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

            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = {
                onSelection(selectedOptions.value)
                navController.navigate(nextScreen.route)
            },
            enabled = enabled,
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
                .testTag("continueButton")
        ) {
            Text("Continue", color = White)
        }
    }
}
