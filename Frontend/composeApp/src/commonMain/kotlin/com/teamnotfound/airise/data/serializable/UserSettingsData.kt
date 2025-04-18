package com.teamnotfound.airise.data.serializable

import kotlinx.serialization.Serializable

@Serializable
data class UserSettingsData(
    val _id: String? = null,
    val firebaseUid: String,
    val profile_picture_url: String = "",
    val ai_personality: String = "",
    val challenge_notifs_enabled: Boolean = false,
    val friend_req_notifs_enabled: Boolean = false,
    val streak_notifs_enabled: Boolean = false,
    val meal_notifs_enabled: Boolean = false
)