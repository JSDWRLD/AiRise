package com.teamnotfound.airise.community.communityNavBar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.DTOs.LeaderboardEntryDTO
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.network.clients.DataClient
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.repository.UserRepository
import com.teamnotfound.airise.data.serializable.User
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.util.NetworkError
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommunityNavBarViewModel(
    private val userRepository: UserRepository,
    private val userClient: UserClient,
    private val dataClient: DataClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityNavBarUiState())
    val uiState: StateFlow<CommunityNavBarUiState> = _uiState

    init {
        // Mirror HomeViewModel: kick off the initial loads
        loadUserData()
        loadUserProfilePic()
        // Ranking needs user's identity, so it's kicked off from loadUserData() success
    }



    private fun bestEffortUserName(userData: UserData?): String? {
        val composed = userData?.let {
            listOf(it.firstName, it.lastName).filter { s -> s.isNotBlank() }.joinToString(" ")
        }?.ifBlank { null }

        return composed ?: Firebase.auth.currentUser?.displayName
    }

    private fun getRankAmongFriends() {
        // keep current loading state for the bar; ranking fetch shouldn't block the whole UI
        viewModelScope.launch {
            val firebaseUser = Firebase.auth.currentUser
            when (val result = firebaseUser?.let { dataClient.getLeaderboardFriends(it) }) {
                is Result.Success<List<LeaderboardEntryDTO>> -> {
                    val meName = bestEffortUserName(_uiState.value.userData)?.trim()
                    val list = result.data

                    // Try to match by (case-insensitive) display name
                    val idx = if (!meName.isNullOrBlank()) {
                        list.indexOfFirst { it.name.equals(meName, ignoreCase = true) }
                    } else -1

                    if (idx >= 0) {
                        val myEntry = list[idx]
                        _uiState.value = _uiState.value.copy(
                            rank = idx + 1,            // 1-based rank
                            streak = myEntry.streak,   // adopt streak from server list
                            errorMessage = null
                        )
                    } else {
                        // Not found — leave rank at 0 (unknown) but don’t error the UI
                        _uiState.value = _uiState.value.copy(
                            // rank stays whatever it was (default 0)
                            // streak stays as-is
                            errorMessage = _uiState.value.errorMessage // no-op
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = _uiState.value.errorMessage ?: result.error.toString()
                    )
                }
                null -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = _uiState.value.errorMessage ?: "No authenticated user."
                    )
                }
            }
        }
    }

    private fun loadUserData() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = userRepository.fetchUserData()) {
                is Result.Success<UserData> -> {
                    // If your UserData includes streak/rank fields, set them here.
                    // Otherwise leave them as-is (0) or fetch from another API.
                    _uiState.value = _uiState.value.copy(
                        userData = result.data,
                        isUserDataLoaded = true,
                        isLoading = false,
                        errorMessage = null,

                    )

                    // Now that we know who the user is, compute rank
                    getRankAmongFriends()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUserDataLoaded = true,
                        isLoading = false,
                        errorMessage = result.error.toString()
                    )
                }
            }
        }
    }

    private fun loadUserProfilePic() {
        viewModelScope.launch {
            val firebaseUser = Firebase.auth.currentUser
            when (val result = firebaseUser?.let { userClient.getUserSettings(it) }) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        userProfilePicture = result.data.profilePictureUrl
                    )
                }
                is Result.Error -> {
                    // Keep UI resilient; log or surface a soft error if you prefer
                    _uiState.value = _uiState.value.copy(
                        errorMessage = _uiState.value.errorMessage ?: result.error.toString()
                    )
                }
                null -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = _uiState.value.errorMessage ?: "No authenticated user."
                    )
                }
            }
        }
    }

    // Public updaters (local state changes; wire these to persistence if needed)
    fun updateStreak(newStreak: Int) {
        _uiState.value = _uiState.value.copy(streak = newStreak)
    }

    fun updateRank(newRank: Int) {
        _uiState.value = _uiState.value.copy(rank = newRank)
    }

    fun updateProfilePictureUrl(newUrl: String?) {
        _uiState.value = _uiState.value.copy(userProfilePicture = newUrl)
    }

    fun updatePage(newPage: CommunityPage) {
        _uiState.value = _uiState.value.copy(page = newPage)
    }

    // Optional: manual refresh hook the UI can call (pull-to-refresh, etc.)
    fun refresh() {
        loadUserData()
        loadUserProfilePic()
        getRankAmongFriends()
    }
}
