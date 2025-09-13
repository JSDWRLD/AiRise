package com.teamnotfound.airise.community.friends.screens

import com.teamnotfound.airise.community.friends.data.FriendActivity
import com.teamnotfound.airise.community.friends.models.FriendsViewModel.SearchState

//keeps loading status, list of activities, and error messages if needed
data class FriendsListUiState(
    val isLoading: Boolean = false,
    val items: List<FriendActivity> = emptyList(),
    val error: String? = null,
    val search: SearchState = SearchState()  // <— NEW FIELD
)
