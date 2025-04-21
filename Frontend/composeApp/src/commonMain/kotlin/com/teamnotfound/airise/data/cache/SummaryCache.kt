package com.teamnotfound.airise.data.cache

import com.teamnotfound.airise.data.serializable.UserData

interface SummaryCache {
    suspend fun cacheSummary(summary: UserData)
    suspend fun getUserSummaries(userId: String): List<UserData>
}
