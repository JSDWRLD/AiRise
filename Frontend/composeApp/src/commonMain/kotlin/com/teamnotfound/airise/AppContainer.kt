package com.teamnotfound.airise

import com.khealth.KHealth
import com.teamnotfound.airise.data.network.clients.UserClient

class AppContainer (
    val userClient: UserClient,
    val kHealth: KHealth
)