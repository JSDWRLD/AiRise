package com.teamnotfound.airise.friends.data

//for one activity in feed
//used by FriendListScreen to display info such as challenge and status
data class FriendActivity(
    val id: String,
    val friendName: String,
    val challengeTitle: String,
    val status: String
)