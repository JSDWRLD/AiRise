package com.teamnotfound.airise.community.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

//loads friends activities from repos and updates UI
class FriendsListViewModel (
    private val repo: FriendsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsListUiState())
    val uiState: StateFlow<FriendsListUiState> = _uiState

    //reloads activity feed items (needs to be updated with actually data)
    //fake data for now
    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val items = repo.getRecentActivities()
                _uiState.value = FriendsListUiState(isLoading = false, items = items)
            } catch (e: Exception) {
                _uiState.value = FriendsListUiState(isLoading = false, error = e.message ?: "Unknown error")
            }
        }
    }
}