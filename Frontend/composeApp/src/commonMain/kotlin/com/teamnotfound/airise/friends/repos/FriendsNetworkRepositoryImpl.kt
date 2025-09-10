package com.teamnotfound.airise.friends.repos

import com.teamnotfound.airise.data.DTOs.toDomain
import com.teamnotfound.airise.friends.data.FriendProfile
import com.teamnotfound.airise.friends.data.FriendsClient

/**
 * Delegates to the Ktor client and maps DTOs to domain.
 */
class FriendsNetworkRepositoryImpl(
    private val client: FriendsClient
) : FriendsNetworkRepository {

    override suspend fun getFriends(me: String, idToken: String): List<FriendProfile> =
        client.getFriends(idToken, me).users.map { it.toDomain() }

    override suspend fun addFriend(me: String, friendUid: String, idToken: String) {
        client.addFriend(idToken, me, friendUid)
    }

    override suspend fun removeFriend(me: String, friendUid: String, idToken: String) {
        client.removeFriend(idToken, me, friendUid)
    }
}