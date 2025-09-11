package com.teamnotfound.airise.leaderboard

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LeaderboardUser(
    val name: String,
    val streak: Int,
    val rank: Int,
    val isFriend: Boolean = false
)

data class LeaderboardUiState(
    val globalUsers: List<LeaderboardUser> = emptyList(),
    val friendsUsers: List<LeaderboardUser> = emptyList(),
    val selectedTab: LeaderboardTab = LeaderboardTab.GLOBAL,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class LeaderboardTab {
    GLOBAL,
    FRIENDS
}

object LeaderboardDataLoader {
    suspend fun loadLeaderboardData(): List<LeaderboardUser> {
        return withContext(Dispatchers.Default) {
            try {
                // Simulating loading from the text file
                // In a real implementation, this would read from the actual file
                val dummyData = listOf(
                    LeaderboardUser("Alex Johnson", 15, 1),
                    LeaderboardUser("Sarah Chen", 12, 2, true),
                    LeaderboardUser("Michael Rodriguez", 11, 3),
                    LeaderboardUser("Emma Thompson", 10, 4, true),
                    LeaderboardUser("David Kim", 9, 5),
                    LeaderboardUser("Jessica Wilson", 8, 6, true),
                    LeaderboardUser("Ryan O'Connor", 7, 7),
                    LeaderboardUser("Sophia Martinez", 6, 8),
                    LeaderboardUser("James Brown", 5, 9, true),
                    LeaderboardUser("Isabella Garcia", 4, 10),
                    LeaderboardUser("Noah Davis", 3, 11),
                    LeaderboardUser("Olivia Taylor", 2, 12),
                    LeaderboardUser("Lucas Anderson", 1, 13),
                    LeaderboardUser("Ava Miller", 0, 14),
                    LeaderboardUser("Ethan Moore", 0, 15)
                )
                dummyData
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    suspend fun loadFriendsData(): List<LeaderboardUser> {
        return withContext(Dispatchers.Default) {
            loadLeaderboardData().filter { it.isFriend }
                .mapIndexed { index, user ->
                    user.copy(rank = index + 1)
                }
        }
    }
}