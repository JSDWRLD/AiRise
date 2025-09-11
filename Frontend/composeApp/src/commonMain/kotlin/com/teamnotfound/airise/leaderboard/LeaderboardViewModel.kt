package com.teamnotfound.airise.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        loadLeaderboardData()
    }

    fun onTabSelected(tab: LeaderboardTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    private fun loadLeaderboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val globalUsers = LeaderboardDataLoader.loadLeaderboardData()
                val friendsUsers = LeaderboardDataLoader.loadFriendsData()
                
                _uiState.value = _uiState.value.copy(
                    globalUsers = globalUsers,
                    friendsUsers = friendsUsers,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load leaderboard data: ${e.message}"
                )
            }
        }
    }

    fun refresh() {
        loadLeaderboardData()
    }
}