package com.teamnotfound.airise.onboarding.onboardingQuestions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.teamnotfound.airise.data.serializable.UserData

//Defines different onboarding questions and options
@Composable
fun WorkoutGoalScreen(navController: NavController, newUser: UserData){
    QuestionScreen(
        questionText = "What is your workout goal?",
        options = listOf("Maintenance", "Muscle Gain", "Weight Loss"),
        nextScreen = OnboardingScreens.FitnessLevel,
        navController = navController,
        questionCount = 2,
        onSelection = { selection -> newUser.workoutGoal.value = selection }
    )
}

@Composable
fun FitnessLevelScreen(navController: NavController, newUser: UserData){
    QuestionScreen(
        questionText = "What is your current fitness level?",
        options = listOf("Novice", "Intermediate", "Advanced"),
        nextScreen = OnboardingScreens.WorkoutLength,
        navController = navController,
        questionCount = 3,
        onSelection = { selection -> newUser.fitnessLevel.value = selection }
    )
}

@Composable
fun WorkoutLengthScreen(navController: NavController, newUser: UserData){
    QuestionScreen(
        questionText = "How long would you like to workout?",
        options = listOf("15 minutes", "30 minutes", "45 minutes", "1 hour+"),
        nextScreen = OnboardingScreens.EquipmentAccess,
        navController = navController,
        questionCount = 4,
        onSelection = { selection -> newUser.workoutLength.value = selection }
    )
}

@Composable
fun EquipmentAccessScreen(navController: NavController, newUser: UserData){
    val selectedOptions = remember { mutableStateOf(setOf<String>()) }

    MultiSelectQuestionScreen(
        questionText = "What equipment do you have access to?",
        options = listOf("Gym", "Home", "Body Weight", "Other Equipment"),
        selectedOptions = selectedOptions,
        nextScreen = OnboardingScreens.WorkoutDays,
        navController = navController,
        questionCount = 5,
        onSelection = { selection -> newUser.equipmentAccess.value = selection.joinToString(", ") }
    )
}

@Composable
fun WorkoutDaysScreen(navController: NavController, newUser: UserData){
    val selectedOptions = remember { mutableStateOf(setOf<String>()) }

    MultiSelectQuestionScreen(
        questionText = "What days do you prefer to workout on?",
        options = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"),
        selectedOptions = selectedOptions,
        nextScreen = OnboardingScreens.WorkoutTime,
        navController = navController,
        questionCount = 6,
        onSelection = { selection -> newUser.workoutDays.value = selection.joinToString(", ") }
    )
}

@Composable
fun WorkoutTimeScreen(navController: NavController, newUser: UserData){
    val selectedOptions = remember { mutableStateOf(setOf<String>()) }

    MultiSelectQuestionScreen(
        questionText = "What are your preferred workout times?",
        options = listOf("Morning", "Afternoon", "Evening"),
        selectedOptions = selectedOptions,
        nextScreen = OnboardingScreens.DietaryGoal,
        navController = navController,
        questionCount = 7,
        onSelection = { selection -> newUser.workoutTime.value = selection.joinToString(", ") }
    )
}

@Composable
fun DietaryGoalScreen(navController: NavController, newUser: UserData){
    QuestionScreen(
        questionText = "What is your dietary goal?",
        options = listOf("Lose weight", "Maintain", "Gain weight"),
        nextScreen = OnboardingScreens.WorkoutRestrictions,
        navController = navController,
        questionCount = 8,
        onSelection = { selection -> newUser.dietaryGoal.value = selection }
    )
}

@Composable
fun ActivityLevelScreen(navController: NavController, newUser: UserData){
    QuestionScreen(
        questionText = "What is your preferred active level?",
        options = listOf("Sendentary", "Lightly Active", "Active", "Very Active"),
        nextScreen = OnboardingScreens.ThankYou,
        navController = navController,
        questionCount = 13,
        onSelection = { selection -> newUser.activityLevel.value = selection }
    )
}