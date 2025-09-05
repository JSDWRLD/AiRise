package com.teamnotfound.airise.communityNavBar

data class UserProfile(
    val name: String,
    val streak: Int,
    val rank: Int,
    val profilePictureUrl: String? = null
)