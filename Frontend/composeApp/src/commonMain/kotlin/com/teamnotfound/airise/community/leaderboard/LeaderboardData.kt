package com.teamnotfound.airise.community.leaderboard

data class LeaderboardUser(
    val name: String,
    val imageUrl: String,
    val streak: Int,
    val rank: Int
)

data class LeaderboardUiState(
    val globalUsers: List<LeaderboardUser> = emptyList(),
    val friendsUsers: List<LeaderboardUser> = emptyList(),
    val selectedTab: LeaderboardTab = LeaderboardTab.GLOBAL,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class LeaderboardTab {
    GLOBAL,
    FRIENDS
}
