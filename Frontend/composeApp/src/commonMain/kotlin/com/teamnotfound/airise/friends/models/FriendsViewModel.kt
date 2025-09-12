package com.teamnotfound.airise.friends.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.DTOs.UserProfile
import com.teamnotfound.airise.data.DTOs.UsersEnvelope
import com.teamnotfound.airise.data.auth.IAuthService
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.repository.UserRepository
import com.teamnotfound.airise.friends.repos.FriendsNetworkRepository
import kotlinx.coroutines.IO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the FRIENDS LIST (the actual list of user profiles you follow / are friends with).
 * - Fetches the list from the backend using the current Firebase UID and ID token
 * - Adds/removes friends with optimistic updates
 * - Reports user-friendly errors
 * - Exposes a user search state (search bar / modal can observe it)
 */
class FriendsViewModel(
    private val auth: IAuthService,
    private val friendRepo: FriendsNetworkRepository,
    private val userRepo: UserRepository,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    // --- Search state slice ---
    data class SearchState(
        val isSearching: Boolean = false,
        val results: UsersEnvelope? = null,
        val error: String? = null
    )

    // --- Screen UI state, now includes `search` ---
    data class UiState(
        val isLoading: Boolean = false,
        val friends: List<UserProfile> = emptyList(),
        val error: String? = null,
        val search: SearchState = SearchState()  // <— NEW FIELD
    )

    private val _ui = MutableStateFlow(UiState(isLoading = false))
    val ui: StateFlow<UiState> = _ui

    /** Loads the current user's friends from the backend. */
    fun load() {
        val me = auth.currentUserId
        if (me.isBlank()) {
            _ui.value = _ui.value.copy(error = "Not authenticated.")
            return
        }

        _ui.value = _ui.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val token = withContext(io) { auth.getIdToken() }
                    ?: throw IllegalStateException("Missing ID token. Please sign in again.")

                val list = withContext(io) { friendRepo.getFriends(me, token) }
                _ui.value = _ui.value.copy(isLoading = false, friends = list, error = null)
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(
                    isLoading = false,
                    friends = emptyList(),
                    error = t.message ?: "Failed to load friends."
                )
            }
        }
    }

    fun refresh() = load()

    /** Adds a friend by their firebaseUid with an optimistic UI update. */
    fun addFriend(friendUid: String) {
        val me = auth.currentUserId
        if (me.isBlank()) {
            _ui.value = _ui.value.copy(error = "Not authenticated.")
            return
        }

        val before = _ui.value.friends
        if (before.any { it.firebaseUid == friendUid }) return // already present

        // Optimistic placeholder
        val optimistic = before + UserProfile(
            firebaseUid = friendUid,
            displayName = "(adding…)",
            profilePicUrl = null,
            streak = 0
        )
        _ui.value = _ui.value.copy(friends = optimistic, error = null)

        viewModelScope.launch {
            try {
                val token = withContext(io) { auth.getIdToken() }
                    ?: throw IllegalStateException("Missing ID token. Please sign in again.")
                withContext(io) { friendRepo.addFriend(me, friendUid, token) }
                load() // refresh to replace optimistic with real data
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(
                    friends = before,
                    error = t.message ?: "Failed to add friend."
                )
            }
        }
    }

    /** Removes a friend with optimistic UI rollback on failure. */
    fun removeFriend(friendUid: String) {
        val me = auth.currentUserId
        if (me.isBlank()) {
            _ui.value = _ui.value.copy(error = "Not authenticated.")
            return
        }

        val before = _ui.value.friends
        val optimistic = before.filterNot { it.firebaseUid == friendUid }
        _ui.value = _ui.value.copy(friends = optimistic, error = null)

        viewModelScope.launch {
            try {
                val token = withContext(io) { auth.getIdToken() }
                    ?: throw IllegalStateException("Missing ID token. Please sign in again.")
                withContext(io) { friendRepo.removeFriend(me, friendUid, token) }
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(
                    friends = before,
                    error = t.message ?: "Failed to remove friend."
                )
            }
        }
    }

    /** Searches users via the repository and updates the search slice. */
    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _ui.value = _ui.value.copy(
                search = SearchState(isSearching = false, results = null, error = null)
            )
            return
        }

        _ui.value = _ui.value.copy(
            search = _ui.value.search.copy(isSearching = true, error = null)
        )

        viewModelScope.launch {
            val res = withContext(io) { userRepo.searchUsers(query) }
            when (res) {
                is Result.Success -> _ui.value = _ui.value.copy(
                    search = SearchState(isSearching = false, results = res.data, error = null)
                )
                is Result.Error -> _ui.value = _ui.value.copy(
                    search = SearchState(isSearching = false, results = null, error = res.error.name)
                )
            }
        }
    }
}
