package com.teamnotfound.airise.friends

//temp repos that returns hardcoded data for activity feed
class ExFriendRepository : FriendsRepository {

    //returns static list of friend activity for testing UI
    override suspend fun getRecentActivities(): List<FriendActivity> = listOf(
        FriendActivity("1","Wick","10k Steps","In progress"),
        FriendActivity("2","Nick","Water","Completed"),
        FriendActivity("3","Rick","Run (3 mi)","In progress")
    )
}