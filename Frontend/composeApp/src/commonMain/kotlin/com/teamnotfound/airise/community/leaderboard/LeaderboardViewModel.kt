package com.teamnotfound.airise.community.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.DTOs.LeaderboardEntryDTO
import com.teamnotfound.airise.data.network.clients.DataClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.teamnotfound.airise.data.network.Result
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth


class LeaderboardViewModel(
    private val dataClient: DataClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState(isLoading = true))
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun onTabSelected(tab: LeaderboardTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val firebaseUser = Firebase.auth.currentUser
            if (firebaseUser == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No authenticated user.",
                    globalUsers = emptyList(),
                    friendsUsers = emptyList()
                )
                return@launch
            }

            val global = dataClient.getLeaderboardTop10(firebaseUser)
            val friends = dataClient.getLeaderboardFriends(firebaseUser)

            when (global) {
                is Result.Success -> {
                    val globalUsers = global.data.mapIndexed { idx, dto -> dto.toUI(idx + 1) }
                    when (friends) {
                        is Result.Success -> {
                            val friendsUsers = friends.data.mapIndexed { idx, dto -> dto.toUI(idx + 1) }
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = null,
                                globalUsers = globalUsers,
                                friendsUsers = friendsUsers
                            )
                        }
                        is Result.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = friends.error.toString(),
                                globalUsers = globalUsers
                            )
                        }
                    }
                }
                is Result.Error -> {
                    when (friends) {
                        is Result.Success -> {
                            val friendsUsers = friends.data.mapIndexed { idx, dto -> dto.toUI(idx + 1) }
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = global.error.toString(),
                                friendsUsers = friendsUsers
                            )
                        }
                        is Result.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "${global.error} • ${friends.error}"
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun LeaderboardEntryDTO.toUI(rank: Int) = LeaderboardUser(
    name = name,
    imageUrl = imageUrl,
    streak = streak,
    rank = rank
)
