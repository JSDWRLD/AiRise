package com.teamnotfound.airise.data.DTOs

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserDTO (
    val firebaseUid: String
)