package com.teamnotfound.airise.data.repository

import com.teamnotfound.airise.data.cache.UserCache
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.data.serializable.UserProgram
import com.teamnotfound.airise.data.serializable.UserProgramDoc
import com.teamnotfound.airise.util.NetworkError
import dev.gitlive.firebase.auth.FirebaseAuth
import com.teamnotfound.airise.data.DTOs.UsersEnvelope
import com.teamnotfound.airise.data.serializable.HealthData
import com.teamnotfound.airise.data.serializable.UserChallenge

class UserRepository(
    private val auth: FirebaseAuth,
    private val userClient: UserClient,
) : IUserRepository{
    override suspend fun fetchUserData(): Result<UserData, NetworkError>{
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

    override suspend fun searchUsers(query: String): Result<UsersEnvelope, NetworkError> {
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
    override suspend fun getUserChallengeOrNull(): UserChallenge? {
        return try {
            val firebaseUser = auth.currentUser ?: return null
            when (val res = userClient.getUserChallenges(firebaseUser)) {
                is Result.Success -> res.data
                is Result.Error   -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getUserProgram(): Result<UserProgramDoc, NetworkError> {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                userClient.getUserProgram(firebaseUser)
            } else {
                Result.Error(NetworkError.UNAUTHORIZED)
            }
        } catch (e: Exception) {
            Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun updateUserProgram(userProgram: UserProgram): Result<Boolean, NetworkError> {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                userClient.updateUserProgram(firebaseUser, userProgram)
            } else {
                Result.Error(NetworkError.UNAUTHORIZED)
            }
        } catch (e: Exception) {
            Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun getHealthData(): Result<HealthData, NetworkError> {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                userClient.getHealthData(firebaseUser)
            } else {
                Result.Error(NetworkError.UNAUTHORIZED)
            }
        } catch (e: Exception) {
            Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun updateHealthData(healthData: HealthData): Result<Boolean, NetworkError> {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                userClient.updateHealthData(firebaseUser, healthData)
            } else {
                Result.Error(NetworkError.UNAUTHORIZED)
            }
        } catch (e: Exception) {
            Result.Error(NetworkError.UNKNOWN)
        }
    }
}