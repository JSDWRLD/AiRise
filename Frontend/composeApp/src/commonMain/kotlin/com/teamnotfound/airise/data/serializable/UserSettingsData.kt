package com.teamnotfound.airise.data.serializable

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class UserSettingsData(
    @SerialName("_id")               val id: String = "",
    @SerialName("firebaseUid")       val firebaseUid: String,
    @SerialName("profile_picture_url") val profilePictureUrl: String = "",
    @SerialName("ai_personality")    val aiPersonality: String = "",
    @SerialName("challenge_notifs_enabled")    val challengeNotifsEnabled: Boolean = false,
    @SerialName("friend_req_notifs_enabled")   val friendReqNotifsEnabled: Boolean = false,
    @SerialName("streak_notifs_enabled")       val streakNotifsEnabled: Boolean = false,
    @SerialName("meal_notifs_enabled")         val mealNotifsEnabled: Boolean = false
)