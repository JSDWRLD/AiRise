package com.teamnotfound.airise.community.friends

//allows for swapping from static data provided to actual data info
interface FriendsRepository {
    suspend fun getRecentActivities(): List<FriendActivity>
}