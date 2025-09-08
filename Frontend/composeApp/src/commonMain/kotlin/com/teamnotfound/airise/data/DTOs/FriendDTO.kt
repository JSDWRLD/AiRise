package com.teamnotfound.airise.data.DTOs

import com.teamnotfound.airise.friends.data.FriendProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FriendsEnvelope(
    val users: List<FriendProfileDto> = emptyList()
)

@Serializable
data class FriendProfileDto(
    val firebaseUid: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val fullName: String? = null,
    @SerialName("profile_picture_url")
    val profilePictureUrl: String? = null,
    val streak: Int = 0
)

/** Convert a DTO into our UI/domain model. */
fun FriendProfileDto.toDomain(): FriendProfile {
    val name = when {
        !fullName.isNullOrBlank() -> fullName
        !firstName.isNullOrBlank() || !lastName.isNullOrBlank() ->
            listOfNotNull(firstName?.takeIf { it.isNotBlank() }, lastName?.takeIf { it.isNotBlank() })
                .joinToString(" ")
                .ifBlank { "(unknown)" }
        else -> "(unknown)"
    }
    return FriendProfile(
        firebaseUid = firebaseUid,
        displayName = name,
        profilePicUrl = profilePictureUrl,
        streak = streak
    )
}
