package com.teamnotfound.airise.home

import androidx.compose.runtime.mutableStateOf
import com.teamnotfound.airise.data.serializable.DailyProgressData
import com.teamnotfound.airise.data.serializable.HealthData
import com.teamnotfound.airise.data.serializable.UserData

data class HomeUiState(
    val greeting: String = "",
    val username: String = "User",
    val loadingUserData: Boolean = false,
    val overview: String = "",
    val isOverviewLoading: Boolean = false,
    val dailyProgressData: DailyProgressData = DailyProgressData(),
    val selectedTimeFrame: String = "Daily",
    val formattedDateRange: String = "",
    val healthData: HealthData = HealthData(),
    val isDailyProgressLoading: Boolean = false,
    val isFitnessSummaryLoading: Boolean = false,
    val errorMessage: String? = null
)
