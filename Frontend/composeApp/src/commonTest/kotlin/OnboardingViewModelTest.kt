package com.teamnotfound.airise.onboarding

import com.teamnotfound.airise.auth.onboarding.OnboardingViewModel
import com.teamnotfound.airise.data.auth.AuthService
import com.teamnotfound.airise.data.cache.SummaryCache
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.serializable.UserDataUiState
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OnboardingViewModelTest {

    @Test
    fun test_UserDataUiState_converts_to_UserData_correctly() {
        // Arrange
        val uiState = UserDataUiState().apply {
            firstName.value = "John"
            lastName.value = "Doe"
            workoutGoal.value = "Lose Weight"
            fitnessLevel.value = "Beginner"
            workoutDays.value = listOf("Monday", "Wednesday", "Friday")
            heightMetric.value = true
            heightValue.value = 180
            weightMetric.value = true
            weightValue.value = 75
            dobDay.value = 15
            dobMonth.value = 6
            dobYear.value = 1990
            activityLevel.value = "Active"
        }

        // Act
        val userData = uiState.toData()

        // Assert
        assertEquals("John", userData.firstName)
        assertEquals("Doe", userData.lastName)
        assertEquals("Lose Weight", userData.workoutGoal)
        assertEquals("Beginner", userData.fitnessLevel)
        assertEquals(listOf("Monday", "Wednesday", "Friday"), userData.workoutDays)
        assertTrue(userData.heightMetric)
        assertEquals(180, userData.heightValue)
        assertTrue(userData.weightMetric)
        assertEquals(75, userData.weightValue)
        assertEquals(15, userData.dobDay)
        assertEquals(6, userData.dobMonth)
        assertEquals(1990, userData.dobYear)
        assertEquals("Active", userData.activityLevel)
    }
}
