package com.teamnotfound.airise.auth.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.compose.material.Text
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.teamnotfound.airise.AppScreen
import com.teamnotfound.airise.data.serializable.UserDataUiState
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.White
import kotlinx.coroutines.delay

@Composable
fun ThankYouScreen(appNavController: NavController, viewModel: OnboardingViewModel, newUser: UserDataUiState){
    var showUserDetails by remember { mutableStateOf(false) }

    LaunchedEffect(Unit){
        viewModel.saveUserData(newUser)
        delay(2000)
        showUserDetails = true
        delay(2000)
        appNavController.navigate(AppScreen.HOMESCREEN.name)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(BgBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        if (!showUserDetails) {
            Text(
                text = "Thank you! Please wait for your information to be processed.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp),
                style = TextStyle(fontSize = 50.sp),
                color = White
            )
        } else {
            // Display the summary of onboarding values
            Text(
                text = "Onboarding Summary",
                style = TextStyle(fontSize = 25.sp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Workout Goal: ${newUser.workoutGoal.value}", fontSize = 20.sp)
            Text(text = "Fitness Level: ${newUser.fitnessLevel.value}", fontSize = 20.sp)
            Text(text = "Workout Length: ${newUser.workoutLength.value}", fontSize = 20.sp)
            Text(text = "Equipment Access: ${newUser.equipmentAccess.value}", fontSize = 20.sp)
            Text(text = "Workout Days: ${newUser.workoutDays.toString()}", fontSize = 20.sp)
            Text(text = "Workout Time: ${newUser.workoutTime.value}", fontSize = 20.sp)
            Text(text = "Dietary Goal: ${newUser.dietaryGoal.value}", fontSize = 20.sp)
            Text(text = "Workout Restrictions: ${newUser.workoutRestrictions.value}", fontSize = 20.sp)
            Text(text = "Height Metric: ${newUser.heightMetric.value}", fontSize = 20.sp)
            Text(text = "Height Value: ${newUser.heightValue.value}", fontSize = 20.sp)
            Text(text = "Weight Metric: ${newUser.weightMetric.value}", fontSize = 20.sp)
            Text(text = "Weight Value: ${newUser.weightValue.value}", fontSize = 20.sp)
            Text(text = "Activity Level: ${newUser.activityLevel.value}", fontSize = 20.sp)
        }
    }
}