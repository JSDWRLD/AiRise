package com.teamnotfound.airise.community.friends.screens

import com.teamnotfound.airise.community.friends.data.FriendActivity
import com.teamnotfound.airise.data.DTOs.UserProfile

data class FriendsUiState(
    val isLoading: Boolean = false,
    val friends: List<FriendActivity> = emptyList(),
    val error: String? = null
)
