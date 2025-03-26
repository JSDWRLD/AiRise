package com.teamnotfound.airise

import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.cache.UserCache


class AppContainer (
    val userClient: UserClient,
    val userCache: UserCache
)