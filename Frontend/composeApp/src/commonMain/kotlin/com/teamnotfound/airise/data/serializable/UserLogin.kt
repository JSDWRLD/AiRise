package com.teamnotfound.airise.data.serializable

import kotlinx.serialization.Serializable

@Serializable
data class UserLogin(
    val email: String,
    val password: String
)