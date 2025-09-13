package com.teamnotfound.airise.community.challenges

sealed class ChallengesEvent {
    data object CompletedToday : ChallengesEvent()
    data class Error(val message: String) : ChallengesEvent() // optional, if you want
}