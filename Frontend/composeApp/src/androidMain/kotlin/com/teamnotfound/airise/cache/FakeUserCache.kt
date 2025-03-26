package com.teamnotfound.airise.cache


//This is our dummy user client for the preview instance in MainActivity.kt
class FakeUserCache : UserCache {
    override suspend fun cacheUserData(userData: Any) {
    }

    override suspend fun getCachedUserData(email: String): Any? {
        return null
    }
}