package com.teamnotfound.airise.cache

import com.teamnotfound.airise.data.cache.SummaryCache
import com.teamnotfound.airise.data.serializable.UserOnboardingData

//This is our dummy summary for the preview instance in MainActivity.kt
class FakeSummaryCache : SummaryCache {
    override suspend fun cacheSummary(summary: UserOnboardingData) {
    }

    override suspend fun getUserSummaries(userId: String): List<UserOnboardingData> {
        return emptyList()
    }
}