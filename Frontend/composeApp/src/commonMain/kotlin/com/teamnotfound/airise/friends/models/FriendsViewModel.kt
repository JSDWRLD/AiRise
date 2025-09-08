package com.teamnotfound.airise.friends.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.auth.IAuthService
import com.teamnotfound.airise.friends.data.FriendProfile
import com.teamnotfound.airise.friends.repos.FriendsNetworkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.plus

/**
 * ViewModel for the FRIENDS LIST (the actual list of user profiles you follow / are friends with).
 * - Fetches the list from the backend using the current Firebase UID and ID token
 * - Adds/removes friends with optimistic updates
 * - Reports user-friendly errors
 */
class FriendsViewModel(
    private val auth: IAuthService,
    private val repo: FriendsNetworkRepository,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val friends: List<FriendProfile> = emptyList(),
        val error: String? = null
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
                // We need the Firebase ID token for the Authorization: Bearer <token> header
                val token = withContext(io) { auth.getIdToken() }
                    ?: throw IllegalStateException("Missing ID token. Please sign in again.")

                val list = withContext(io) { repo.getFriends(me, token) }
                _ui.value = UiState(isLoading = false, friends = list, error = null)
            } catch (t: Throwable) {
                _ui.value = UiState(isLoading = false, friends = emptyList(), error = t.message ?: "Failed to load friends.")
            }
        }
    }

    fun refresh() = load()

    /**
     * Adds a friend by their firebaseUid.
     * We optimistically append a placeholder row to keep the UI snappy, then re-sync.
     */
    fun addFriend(friendUid: String) {
        val me = auth.currentUserId
        if (me.isBlank()) {
            _ui.value = _ui.value.copy(error = "Not authenticated.")
            return
        }

        val before = _ui.value.friends
        if (before.any { it.firebaseUid == friendUid }) return // already present

        // Optimistic placeholder (name/pic will be corrected on refresh)
        val optimistic = before + FriendProfile(
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

                withContext(io) { repo.addFriend(me, friendUid, token) }

                // Re-fetch to replace the optimistic row with real data (name/photo/streak)
                load()
            } catch (t: Throwable) {
                // Roll back if the network call fails
                _ui.value = _ui.value.copy(friends = before, error = t.message ?: "Failed to add friend.")
            }
        }
    }

    /**
     * Removes a friend by their firebaseUid.
     * We optimistically remove it from UI, if it fails, roll back.
     */
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

                withContext(io) { repo.removeFriend(me, friendUid, token) }
                // No immediate re-fetch needed unless you want to validate server state
            } catch (t: Throwable) {
                // Roll back the UI on failure
                _ui.value = _ui.value.copy(friends = before, error = t.message ?: "Failed to remove friend.")
            }
        }
    }
}
