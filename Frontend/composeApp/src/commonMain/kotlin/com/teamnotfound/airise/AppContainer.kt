package com.teamnotfound.airise

import com.khealth.KHealth
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.cache.UserCache
import com.teamnotfound.airise.data.cache.SummaryCache


class AppContainer (
    val userClient: UserClient,
    val kHealth: KHealth,
    val userCache: UserCache,
    val summaryCache: SummaryCache
)