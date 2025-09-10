package com.teamnotfound.airise

import com.khealth.KHealth
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.cache.UserCache
import com.teamnotfound.airise.data.cache.SummaryCache
import com.teamnotfound.airise.data.network.clients.DataClient
import io.ktor.client.HttpClient

class AppContainer (
    val httpClient: HttpClient,
    val userClient: UserClient,
    val dataClient: DataClient,
    val kHealth: KHealth,
    val userCache: UserCache,
    val summaryCache: SummaryCache
)