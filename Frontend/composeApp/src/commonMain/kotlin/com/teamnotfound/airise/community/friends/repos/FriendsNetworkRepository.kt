package com.teamnotfound.airise.community.friends.repos

import com.teamnotfound.airise.data.DTOs.UserProfile

interface FriendsNetworkRepository {
    suspend fun getFriends(me: String, idToken: String): List<UserProfile>
    suspend fun addFriend(me: String, friendUid: String, idToken: String)
    suspend fun removeFriend(me: String, friendUid: String, idToken: String)
}