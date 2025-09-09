package com.teamnotfound.airise.communityNavBar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommunityNavBarViewModel : ViewModel() {
    private val _userProfile = MutableStateFlow(UserProfile(
        name = "Loading...",
        streak = 0,
        rank = 0,
        page = CommunityPage.ActivityFeed,
        profilePictureUrl = null
    ))
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    init {
        viewModelScope.launch {
            _userProfile.value = UserProfile(
                name = "John Doe", streak = 7, rank = 12,
                page = CommunityPage.ActivityFeed,
                profilePictureUrl = null
            )
        }
    }

    fun updateStreak(newStreak: Int) {
        _userProfile.value = _userProfile.value.copy(streak = newStreak)
    }

    fun updateRank(newRank: Int) {
        _userProfile.value = _userProfile.value.copy(rank = newRank)
    }

    fun updateProfilePictureUrl(newUrl: String?) {
        _userProfile.value = _userProfile.value.copy(profilePictureUrl = newUrl)
    }

    fun updatePage(newPage: CommunityPage) {
        _userProfile.value = _userProfile.value.copy(page = newPage)
    }
}