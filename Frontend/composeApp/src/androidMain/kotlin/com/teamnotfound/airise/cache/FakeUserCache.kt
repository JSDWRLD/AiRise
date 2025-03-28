package com.teamnotfound.airise.cache

import com.teamnotfound.airise.data.auth.User

//This is our dummy user client for the preview instance in MainActivity.kt
class FakeUserCache : UserCache {
    override suspend fun cacheUserData(userData: User) {
    }

    override suspend fun getCachedUserData(email: String): Any? {
        return null
    }
}