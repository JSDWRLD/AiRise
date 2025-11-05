package com.teamnotfound.airise.home.accountSettings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.khealth.KHealth
import com.teamnotfound.airise.AppScreen
import com.teamnotfound.airise.data.serializable.UserDataUiState
import com.teamnotfound.airise.health.HealthDashboardScreen

@Composable
fun AccountSettings(
    navController: NavHostController,
    accountSettingViewModel: AccountSettingsViewModel,
    kHealth: KHealth,
    startScreen: String
) {
    val uiState by accountSettingViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        accountSettingViewModel.loadUserData()
    }

    val user = remember(uiState.userData) {
        UserDataUiState().apply {
            // Initialize with data from ViewModel if available
            uiState.userData?.let { userData ->
                firstName.value = userData.firstName
                middleName.value = userData.middleName
                lastName.value = userData.lastName
                fullName.value = userData.fullName
                workoutGoal.value = userData.workoutGoal
                fitnessLevel.value = userData.fitnessLevel
                workoutLength.value = userData.workoutLength
                equipmentAccess.value = userData.workoutEquipment
                workoutDays.value = userData.workoutDays
                workoutTime.value = userData.workoutTime
                dietaryGoal.value = userData.dietaryGoal
                workoutRestrictions.value = userData.workoutRestrictions
                heightMetric.value = userData.heightMetric
                heightValue.value = userData.heightValue
                weightMetric.value = userData.weightMetric
                weightValue.value = userData.weightValue
                dobDay.value = userData.dobDay
                dobMonth.value = userData.dobMonth
                dobYear.value = userData.dobYear
                activityLevel.value = userData.activityLevel
            }
        }
    }

    when (startScreen) {
        AccountSettingScreens.AccountSettings.route -> AccountSettingScreen(user, navController, accountSettingViewModel)
        AccountSettingScreens.DOBSelect.route -> SettingAgeSelectionScreen(navController, accountSettingViewModel, user)
        AccountSettingScreens.WeightSelect.route -> SettingWeightSelectionScreen(navController, accountSettingViewModel, user)
        AccountSettingScreens.HeightSelect.route -> SettingHeightSelectionScreen(navController, accountSettingViewModel, user)
        // AccountSettingScreens.AiPersonality.route -> AiPersonalityScreen(user, navController)
        AccountSettingScreens.HealthDashboard.route -> HealthDashboardScreen(kHealth, onBackClick = { navController.popBackStack() })
        AccountSettingScreens.NameEdit.route -> NameEditScreen(navController, user, accountSettingViewModel)
    }
}
