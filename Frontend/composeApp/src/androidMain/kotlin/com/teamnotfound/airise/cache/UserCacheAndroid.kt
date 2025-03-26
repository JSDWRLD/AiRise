package com.teamnotfound.airise.cache

import com.teamnotfound.airise.room.DatabaseProvider
import com.teamnotfound.airise.room.UserEntity
import android.content.Context

class UserCacheAndroid(private val context: Context) : UserCache {
    private val db = DatabaseProvider.getDatabase(context)

    override suspend fun cacheUserData(userData: Any) {
        if (userData is UserEntity) {
            db.userDao().insertUser(userData)
        }
    }

    override suspend fun getCachedUserData(email: String): Any? {
        return db.userDao().getUserByEmail(email)
    }
}
