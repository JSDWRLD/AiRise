package com.teamnotfound.airise.data.cache

import com.teamnotfound.airise.data.serializable.UserOnboardingData

interface SummaryCache {
    suspend fun cacheSummary(summary: UserOnboardingData)
    suspend fun getUserSummaries(userId: String): List<UserOnboardingData>
}
