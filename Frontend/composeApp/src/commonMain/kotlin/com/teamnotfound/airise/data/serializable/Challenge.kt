package com.teamnotfound.airise.data.serializable

import androidx.compose.runtime.mutableStateOf
import com.teamnotfound.airise.community.challenges.ChallengeUI
import kotlinx.serialization.Serializable

@Serializable
data class Challenge (
    val id: String? = null, // MongoDB ObjectId as String
    val name: String,
    val description: String,
    val url: String
) {
    fun toUI(): ChallengeUI = ChallengeUI(
        id = id ?: "",
        name = mutableStateOf(name),
        description = mutableStateOf(description),
        imageUrl = mutableStateOf(url)
    )
}