package com.teamnotfound.airise.friends.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.friends.screens.FriendsListUiState
import com.teamnotfound.airise.friends.repos.FriendsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the FRIENDS ACTIVITY FEED.
 * - Uses FriendsRepository (e.g., ExFriendRepository) to fetch recent activities.
 * - Exposes a simple UiState via StateFlow for Compose.
 */
class FriendsListViewModel(
    private val repo: FriendsRepository
) : ViewModel() {

    // Keep using the shared data class defined in FriendsListUiState
    private val _uiState = MutableStateFlow(FriendsListUiState())
    val uiState: StateFlow<FriendsListUiState> = _uiState

    /** Optional alias so callers can use either load() or refresh(). */
    fun load() = refresh()

    /** Reload the activity feed items (currently mock data via ExFriendRepository). */
    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val items = repo.getRecentActivities()
                _uiState.value = FriendsListUiState(
                    isLoading = false,
                    items = items,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = FriendsListUiState(
                    isLoading = false,
                    items = emptyList(),
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}
