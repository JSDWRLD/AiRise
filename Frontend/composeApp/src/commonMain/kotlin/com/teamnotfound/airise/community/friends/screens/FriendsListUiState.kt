package com.teamnotfound.airise.community.friends.screens

import com.teamnotfound.airise.community.friends.models.FriendsViewModel.SearchState
import com.teamnotfound.airise.data.DTOs.UserProfile

//keeps loading status, list of activities, and error messages if needed
data class FriendsListUiState(
    val isLoading: Boolean = false,
    val friends: List<UserProfile> = emptyList(),
    val error: String? = null,
    val search: SearchState = SearchState()
)
