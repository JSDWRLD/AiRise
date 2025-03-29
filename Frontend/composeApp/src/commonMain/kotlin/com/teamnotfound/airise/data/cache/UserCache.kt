package com.teamnotfound.airise.data.cache

import com.teamnotfound.airise.data.auth.User

//This will be used as our interface for AppViewModel to avoid issues with iOS
interface UserCache {
    suspend fun cacheUserData(userData: User)
    suspend fun getCachedUserData(email: String): Any?
}
