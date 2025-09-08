package com.teamnotfound.airise.friends.data

data class FriendProfile(
    val firebaseUid: String,
    val displayName: String,
    val profilePicUrl: String?,
    val streak: Int
)