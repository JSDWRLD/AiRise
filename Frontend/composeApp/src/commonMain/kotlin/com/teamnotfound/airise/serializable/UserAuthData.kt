package com.teamnotfound.airise.serializable

import kotlinx.serialization.Serializable

@Serializable

// We will use this to properly serialize the data for network transfer
// That way we can simply send plain data without UI features.
data class UserAuthData(
    val id: String? = null, // For MongoDB
    val email: String,
    val username: String,
    val password: String
)