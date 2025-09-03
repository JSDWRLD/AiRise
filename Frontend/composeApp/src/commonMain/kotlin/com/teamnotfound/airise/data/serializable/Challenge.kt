package com.teamnotfound.airise.data.serializable

import kotlinx.serialization.Serializable

@Serializable
data class Challenge (
    val id: String? = null, // MongoDB ObjectId as String
    val name: String,
    val description: String,
    val url: String
)