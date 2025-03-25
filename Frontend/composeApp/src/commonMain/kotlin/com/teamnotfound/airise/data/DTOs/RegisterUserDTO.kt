package com.teamnotfound.airise.data.DTOs

import kotlinx.serialization.Serializable

data class RegisterUserDTO (
    val email: String,
    val password: String
)