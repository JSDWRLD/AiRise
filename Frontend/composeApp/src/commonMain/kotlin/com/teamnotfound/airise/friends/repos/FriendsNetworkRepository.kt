package com.teamnotfound.airise.friends.repos

import com.teamnotfound.airise.friends.data.FriendProfile

interface FriendsNetworkRepository {
    suspend fun getFriends(me: String, idToken: String): List<FriendProfile>
    suspend fun addFriend(me: String, friendUid: String, idToken: String)
    suspend fun removeFriend(me: String, friendUid: String, idToken: String)
}