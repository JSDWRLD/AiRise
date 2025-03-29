package com.teamnotfound.airise.cache

import com.teamnotfound.airise.room.DatabaseProvider
import com.teamnotfound.airise.room.UserEntity
import android.content.Context
import com.teamnotfound.airise.data.auth.User
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import android.util.Log
import com.teamnotfound.airise.data.cache.UserCache

class UserCacheAndroid(private val context: Context) : UserCache {
    private val db = DatabaseProvider.getDatabase(context)

    override suspend fun cacheUserData(user: User) {
        withContext(Dispatchers.IO) {
            try {
                val entity = UserEntity(
                    mongoId = user.id,
                    email = user.email ?: "",
                    username = user.id,
                    password = ""
                )

                val insertedId = db.userDao().insertUser(entity)
                Log.d("UserCacheAndroid", "Inserted user data with ID: $insertedId")
            } catch (e: Exception) {
                Log.e("UserCacheAndroid", "Error inserting user data", e)
                throw e
            }
        }
    }

    override suspend fun getCachedUserData(email: String): Any? {
        val entity = db.userDao().getUserByEmail(email)
        return entity?.let { User(id = it.mongoId ?: "", email = it.email) }
    }
}
