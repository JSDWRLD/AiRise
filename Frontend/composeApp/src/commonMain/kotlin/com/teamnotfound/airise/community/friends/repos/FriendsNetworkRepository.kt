package com.teamnotfound.airise.community.friends.repos

import com.teamnotfound.airise.data.DTOs.UserProfile

interface FriendsNetworkRepository {
    suspend fun getFriends(me: String): List<UserProfile>
    suspend fun addFriend(me: String, friendUid: String)
    suspend fun removeFriend(me: String, friendUid: String)
}