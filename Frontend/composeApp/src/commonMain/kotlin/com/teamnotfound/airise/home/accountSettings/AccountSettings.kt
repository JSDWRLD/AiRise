package com.teamnotfound.airise.home.accountSettings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.khealth.KHealth
import com.teamnotfound.airise.data.serializable.UserDataUiState
import com.teamnotfound.airise.health.HealthDashboardScreen

@Composable
fun AccountSettings(
    navController: NavHostController,
    accountSettingViewModel: AccountSettingsViewModel,
    kHealth: KHealth
) {
    val localNavController = rememberNavController()  // Use a local NavController for account settings
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

    NavHost(
        navController = localNavController,
        startDestination = AccountSettingScreens.AccountSettings.route
    ) {
        composable(AccountSettingScreens.AccountSettings.route) {
            // Pass parent navController
            AccountSettingScreen(user, navController, localNavController, accountSettingViewModel)
        }
        composable(AccountSettingScreens.DOBSelect.route) {
            SettingAgeSelectionScreen(localNavController, AccountSettingScreens.AccountSettings.route, user)
        }
        composable(AccountSettingScreens.WeightSelect.route) {
            SettingWeightSelectionScreen(localNavController, AccountSettingScreens.AccountSettings.route, user)
        }
        composable(AccountSettingScreens.HeightSelect.route) {
            SettingHeightSelectionScreen(localNavController, AccountSettingScreens.AccountSettings.route, user)
        }
        composable(AccountSettingScreens.AiPersonality.route) {
            AiPersonalityScreen(user, localNavController)
        }
        composable(AccountSettingScreens.Notifications.route) {
            NotificationSettingsScreen(localNavController)
        }
        composable(AccountSettingScreens.HealthDashboard.route) {
            HealthDashboardScreen(kHealth, onBackClick = { localNavController.popBackStack() })
        }
        composable(AccountSettingScreens.NameEdit.route){
            NameEditScreen(localNavController, user, accountSettingViewModel)
        }
    }
}
