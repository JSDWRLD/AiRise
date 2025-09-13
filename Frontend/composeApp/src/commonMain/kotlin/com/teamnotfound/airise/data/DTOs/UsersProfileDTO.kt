package com.teamnotfound.airise.data.DTOs

import com.teamnotfound.airise.data.DTOs.UserProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsersEnvelope(
    val users: List<UserProfileDto> = emptyList()
)

@Serializable
data class UserProfileDto(
    val firebaseUid: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val fullName: String? = null,
    @SerialName("profile_picture_url")
    val profilePictureUrl: String? = null,
    val streak: Int = 0
)

data class UserProfile(
    val firebaseUid: String,
    val displayName: String,
    val profilePicUrl: String?,
    val streak: Int
)

/** Convert a DTO into our UI/domain model. */
fun UserProfileDto.toDomain(): UserProfile {
    val name = when {
        !fullName.isNullOrBlank() -> fullName
        !firstName.isNullOrBlank() || !lastName.isNullOrBlank() ->
            listOfNotNull(firstName?.takeIf { it.isNotBlank() }, lastName?.takeIf { it.isNotBlank() })
                .joinToString(" ")
                .ifBlank { "(unknown)" }
        else -> "(unknown)"
    }
    return UserProfile(
        firebaseUid = firebaseUid,
        displayName = name,
        profilePicUrl = profilePictureUrl,
        streak = streak
    )
}
