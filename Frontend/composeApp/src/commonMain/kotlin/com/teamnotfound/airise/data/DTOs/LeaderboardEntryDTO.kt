package com.teamnotfound.airise.data.DTOs

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntryDTO (
    val name: String,
    val imageUrl: String,
    val streak: Int
)