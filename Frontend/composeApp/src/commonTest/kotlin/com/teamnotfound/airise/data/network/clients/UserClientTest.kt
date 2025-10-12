package com.teamnotfound.airise.data.network.clients

import com.teamnotfound.airise.auth.onboarding.OnboardingViewModel
import com.teamnotfound.airise.data.auth.AuthService
import com.teamnotfound.airise.data.cache.SummaryCache
import com.teamnotfound.airise.data.serializable.HealthData
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.data.serializable.UserDataUiState
import com.teamnotfound.airise.data.serializable.UserExerciseEntry
import com.teamnotfound.airise.data.serializable.UserExerciseWeight
import com.teamnotfound.airise.home.HomeViewModel
import com.teamnotfound.airise.home.accountSettings.AccountSettingsUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserClientTest {

    @Test
    fun `placeholder test`() {
        assertTrue(true)
    }
}