package com.teamnotfound.airise.communityNavBar

data class UserProfile(
    val name: String,
    val streak: Int,
    val rank: Int,
    val page: CommunityPage,
    val profilePictureUrl: String? = null
)

enum class CommunityPage {
    Leaderboard,
    ActivityFeed,
    Challenges
}