package com.teamnotfound.airise.auth.onboarding.onboardingQuestions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.teamnotfound.airise.data.serializable.UserDataUiState

//Defines different onboarding questions and options
@Composable
fun WorkoutGoalScreen(navController: NavController, newUser: UserDataUiState){
    //description of each of the options
    val optionDescriptions = mapOf(
        "Maintenance" to "Keep your current fitness level and stay active.",
        "Muscle Gain" to "Build strength and increase muscle mass.",
        "Weight Loss" to "Burn calories and shed excess fat."
    )

    QuestionScreen(
        questionText = "What is your workout goal?",
        options = optionDescriptions.keys.toList(),
        optionSubtext = optionDescriptions,
        nextScreen = OnboardingScreens.FitnessLevel,
        navController = navController,
        questionCount = 2,
        onSelection = { selection -> newUser.workoutGoal.value = selection }
    )
}

@Composable
fun FitnessLevelScreen(navController: NavController, newUser: UserDataUiState){
    //description of each of the options
    val optionDescriptions = mapOf(
        "Novice" to "New to working out or returning after a long break.",
        "Intermediate" to "Regularly active with some experience in fitness.",
        "Advanced" to "Consistently training with intensity and experience."
    )

    QuestionScreen(
        questionText = "What is your current fitness level?",
        options = optionDescriptions.keys.toList(),
        optionSubtext = optionDescriptions,
        nextScreen = OnboardingScreens.WorkoutLength,
        navController = navController,
        questionCount = 3,
        onSelection = { selection -> newUser.fitnessLevel.value = selection }
    )
}

@Composable
fun WorkoutLengthScreen(navController: NavController, newUser: UserDataUiState){
    QuestionScreen(
        questionText = "How long would you like to workout?",
        options = listOf("15 minutes", "30 minutes", "45 minutes", "60 minutes+"),
        nextScreen = OnboardingScreens.EquipmentAccess,
        navController = navController,
        questionCount = 4,
        onSelection = { selection ->
            val minutes = when {
                selection.contains("15") -> 15
                selection.contains("30") -> 30
                selection.contains("45") -> 45
                selection.contains("60") -> 60
                else -> 0
            }
            newUser.workoutLength.value = minutes
        }
    )
}

@Composable
fun EquipmentAccessScreen(navController: NavController, newUser: UserDataUiState){
    val selectedOptions = remember { mutableStateOf(setOf<String>()) }

    // Display labels -> canonical keys
    val labelToKey = mapOf(
        "Gym" to "gym",
        "Home" to "home",
        "Bodyweight" to "bodyweight"
    )

    MultiSelectQuestionScreen(
        questionText = "What equipment do you have access to?",
        options = listOf("Gym", "Home", "Body Weight"),
        optionSubtext = mapOf(
            "Gym" to "Full range of machines and free weights.",
            "Home" to "Basic equipment like dumbbells or resistance bands",
            "Body Weight" to "Exercises that rely on your own body only."
        ),
        selectedOptions = selectedOptions,
        nextScreen = OnboardingScreens.WorkoutDays,
        navController = navController,
        questionCount = 5,
        onSelection = { labels ->
            // Save canonical keys (preferred: array). If you must save CSV, join with ","
            val keys = labels.mapNotNull { labelToKey[it] }.toSet()
            newUser.equipmentAccess.value = keys.joinToString(",")
        }
    )
}

@Composable
fun WorkoutDaysScreen(navController: NavController, newUser: UserDataUiState){
    val selectedOptions = remember { mutableStateOf(setOf<String>()) }

    MultiSelectQuestionScreen(
        questionText = "What days do you prefer to workout on?",
        options = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"),
        selectedOptions = selectedOptions,
        nextScreen = OnboardingScreens.WorkoutTime,
        navController = navController,
        questionCount = 6,
        onSelection = { selection -> newUser.workoutDays.value = selection.toList() }
    )
}

@Composable
fun WorkoutTimeScreen(navController: NavController, newUser: UserDataUiState){
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
fun DietaryGoalScreen(navController: NavController, newUser: UserDataUiState){
    //description of each of the options
    val optionDescriptions = mapOf(
        "Lose Weight" to "Reduce calorie intake to burn fat and achieve a learner body.",
        "Maintain" to "Balance calorie intake to sustain your current weight.",
        "Gain Weight" to "Increase calorie intake to build muscles or add mass."
    )

    QuestionScreen(
        questionText = "What is your dietary goal?",
        options = optionDescriptions.keys.toList(),
        optionSubtext = optionDescriptions,
        nextScreen = OnboardingScreens.WorkoutRestrictions,
        navController = navController,
        questionCount = 8,
        onSelection = { selection -> newUser.dietaryGoal.value = selection }
    )
}

@Composable
fun ActivityLevelScreen(navController: NavController, newUser: UserDataUiState){
    //description of each of the options
    val optionDescriptions = mapOf(
        "Sedentary" to "Minimal physical activity, mostly sitting or inactive.",
        "Lightly Active" to "Occasional movement, light exercise or walking.",
        "Active" to "Regular exercise and moderate daily movements.",
        "Very Active" to "Intense workouts and high daily activity levels."
    )

    QuestionScreen(
        questionText = "What is your preferred active level?",
        options = optionDescriptions.keys.toList(),
        optionSubtext = optionDescriptions,
        nextScreen = OnboardingScreens.ThankYou,
        navController = navController,
        questionCount = 13,
        onSelection = { selection -> newUser.activityLevel.value = selection }
    )
}