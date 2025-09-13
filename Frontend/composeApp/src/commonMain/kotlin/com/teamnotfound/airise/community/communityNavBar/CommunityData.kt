package com.teamnotfound.airise.community.communityNavBar

import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.data.serializable.UserDataUiState

enum class CommunityPage {
    Leaderboard,
    Friends,
    Challenges
}

data class CommunityNavBarUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val userData: UserData? = null,
    val userProfilePicture: String? = null,
    val rank: Int = 0,
    val page: CommunityPage = CommunityPage.Friends,
    val streak: Int = 0,
    val isUserDataLoaded: Boolean = false
)