package com.teamnotfound.airise.friends.screens

import com.teamnotfound.airise.data.DTOs.UserProfile

data class FriendsUiState(
    val isLoading: Boolean = false,
    val friends: List<UserProfile> = emptyList(),
    val error: String? = null
)
