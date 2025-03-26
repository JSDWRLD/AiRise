package com.teamnotfound.airise.home

import com.teamnotfound.airise.data.auth.User

data class HomeUiState(
    val greeting: String = "",
    val overview: String = "",
    val isOverviewLoading: Boolean = false,
    val selectedTimeFrame: String = "Daily",
    val formattedDateRange: String = "",
    val calories: Int = 0,
    val steps: Int = 0,
    val heartRate: Int = 0,
    val isFitnessSummaryLoading: Boolean = false,
    val errorMessage: String? = null
)
