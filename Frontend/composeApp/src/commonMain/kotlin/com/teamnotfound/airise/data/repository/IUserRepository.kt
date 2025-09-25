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
import com.teamnotfound.airise.data.serializable.User
import com.teamnotfound.airise.data.serializable.UserChallenge

/**
 * Contract for accessing and updating user data.
 * Implemented by the real UserRepository and by fakes in tests.
 */
interface IUserRepository {

    /**
     * Fetch the current user's UserData
     */
    suspend fun fetchUserData(): Result<UserData, NetworkError>

    /**
     * Search for user with by their names
     */
    suspend fun searchUsers(query: String): Result<UsersEnvelope, NetworkError>

    /**
     * Get the user's Challenge
     */
    suspend fun getUserChallengeOrNull(): UserChallenge?

    /**
     * Get the user's workout program data.
     */
    suspend fun getUserProgram(): Result<UserProgramDoc, NetworkError>

    /**
     * Update the user's workout program
     */
    suspend fun updateUserProgram(userProgram: UserProgram): Result<Boolean, NetworkError>

}
