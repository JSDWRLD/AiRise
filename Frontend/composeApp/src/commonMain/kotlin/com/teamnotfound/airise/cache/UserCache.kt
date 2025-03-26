package com.teamnotfound.airise.cache

//This will be used as our interface for AppViewModel to avoid issues with iOS
interface UserCache {
    suspend fun cacheUserData(userData: Any)
    suspend fun getCachedUserData(email: String): Any?
}
