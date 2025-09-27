package com.teamnotfound.airise.data.serializable

import kotlinx.serialization.Serializable

@Serializable
data class UserChallenge(
    val id: String = "",
    val firebaseUid: String = "",
    val activeChallengeId: String? = "",
    val lastCompletionEpochDay: Long? = 0
)

// --- UserChallenges DTOs for requests ---
@Serializable
data class SetActiveReq(
    val firebaseUid: String,
    val challengeId: String
)

@Serializable
data class UidOnlyReq(
    val firebaseUid: String
)
