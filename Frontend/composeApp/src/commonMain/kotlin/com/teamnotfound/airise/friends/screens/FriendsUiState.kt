package com.teamnotfound.airise.friends.screens

import com.teamnotfound.airise.friends.data.FriendProfile

data class FriendsUiState(
    val isLoading: Boolean = false,
    val friends: List<FriendProfile> = emptyList(),
    val error: String? = null
)
