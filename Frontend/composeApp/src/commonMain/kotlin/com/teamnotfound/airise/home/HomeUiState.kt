package com.teamnotfound.airise.home

import com.teamnotfound.airise.data.auth.User
import com.teamnotfound.airise.data.serializable.HealthData

data class HomeUiState(
    val greeting: String = "",
    val username: String = "",
    val overview: String = "",
    val isOverviewLoading: Boolean = false,
    val selectedTimeFrame: String = "Daily",
    val formattedDateRange: String = "",
    val healthData: HealthData = HealthData(),
    val isFitnessSummaryLoading: Boolean = false,
    val errorMessage: String? = null
)
