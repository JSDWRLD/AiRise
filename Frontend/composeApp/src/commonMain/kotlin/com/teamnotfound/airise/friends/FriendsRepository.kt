package com.teamnotfound.airise.friends

//allows for swapping from static data provided to actual data info
interface FriendsRepository {
    suspend fun getRecentActivities(): List<FriendActivity>
}