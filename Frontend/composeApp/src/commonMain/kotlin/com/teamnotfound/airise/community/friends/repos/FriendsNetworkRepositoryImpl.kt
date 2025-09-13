package com.teamnotfound.airise.community.friends.repos

import com.teamnotfound.airise.data.DTOs.toDomain
import com.teamnotfound.airise.data.DTOs.UserProfile
import com.teamnotfound.airise.community.friends.data.FriendsClient

/**
 * Delegates to the Ktor client and maps DTOs to domain.
 */
class FriendsNetworkRepositoryImpl(
    private val client: FriendsClient
) : FriendsNetworkRepository {
    override suspend fun getFriends(me: String, idToken: String): List<UserProfile> =
        client.getFriends(idToken, me).users.map { it.toDomain() }

    override suspend fun addFriend(me: String, friendUid: String, idToken: String) {
        client.addFriend(idToken, me, friendUid)
    }

    override suspend fun removeFriend(me: String, friendUid: String, idToken: String) {
        client.removeFriend(idToken, me, friendUid)
    }
}