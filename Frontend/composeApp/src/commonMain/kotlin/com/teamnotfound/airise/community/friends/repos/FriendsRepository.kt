
package com.teamnotfound.airise.community.friends.repos

import com.teamnotfound.airise.community.friends.data.FriendActivity

//allows for swapping from static data provided to actual data info
interface FriendsRepository {
    suspend fun getRecentActivities(): List<FriendActivity>
}