package com.teamnotfound.airise.home

import com.teamnotfound.airise.data.serializable.DailyProgressData
import com.teamnotfound.airise.data.serializable.HealthData
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.data.serializable.UserDataUiState

data class HomeUiState(
    val greeting: String = "",
    val username: String = "User",
    val userData: UserData = UserDataUiState().toData(),
    val isUserDataLoaded: Boolean = false,
    val overview: String = "",
    val isOverviewLoaded: Boolean = false,
    val dailyProgressData: DailyProgressData = DailyProgressData(),
    val formattedDateRange: String = "",
    val healthData: HealthData = HealthData(),
    val isDailyProgressLoaded: Boolean = false,
    val isFitnessSummaryLoaded: Boolean = false,
    val errorMessage: String? = null,
    val userProfilePicture: String? = "",
)
