package com.teamnotfound.airise.community.friends

//keeps loading status, list of activities, and error messages if needed
data class FriendsListUiState(
    val isLoading: Boolean = false,
    val items: List<FriendActivity> = emptyList(),
    val error: String? = null
)
