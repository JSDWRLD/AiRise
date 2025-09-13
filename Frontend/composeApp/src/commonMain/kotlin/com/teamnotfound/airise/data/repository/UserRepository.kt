package com.teamnotfound.airise.data.repository

import com.teamnotfound.airise.data.cache.UserCache
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.util.NetworkError
import dev.gitlive.firebase.auth.FirebaseAuth
import com.teamnotfound.airise.data.DTOs.UsersEnvelope

class UserRepository(
    private val auth: FirebaseAuth,
    private val userClient: UserClient,
    private val userCache: UserCache,
) {
    suspend fun fetchUserData(): Result<UserData, NetworkError>{
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null){
                val result = userClient.getUserData(firebaseUser)
                return result
            } else {
                Result.Error(NetworkError.UNAUTHORIZED)
            }
        } catch (e: Exception){
            Result.Error(NetworkError.UNKNOWN)
        }
    }

    suspend fun searchUsers(query: String): Result<UsersEnvelope, NetworkError> {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                userClient.searchUsers(firebaseUser, query)
            } else {
                Result.Error(NetworkError.UNAUTHORIZED)
            }
        } catch (e: Exception) {
            Result.Error(NetworkError.UNKNOWN)
        }
    }
}