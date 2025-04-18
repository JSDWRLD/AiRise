package com.teamnotfound.airise.cache

import com.teamnotfound.airise.data.cache.SummaryCache
import com.teamnotfound.airise.data.serializable.UserData

//This is our dummy summary for the preview instance in MainActivity.kt
class FakeSummaryCache : SummaryCache {
    override suspend fun cacheSummary(summary: UserData) {
    }

    override suspend fun getUserSummaries(userId: String): List<UserData> {
        return emptyList()
    }
}