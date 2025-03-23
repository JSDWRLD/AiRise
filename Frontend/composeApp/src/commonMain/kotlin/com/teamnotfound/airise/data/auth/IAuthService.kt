package com.teamnotfound.airise.data.auth

import kotlinx.coroutines.flow.Flow

interface IAuthService {

    val currentUserId: String
    val isAuthenticated: Boolean
    val currentUser: Flow<User>

    suspend fun authenticate(email: String, password: String): AuthResult
    suspend fun createUser(email: String, password: String): AuthResult
    suspend fun sendPasswordResetEmail(email: String): AuthResult
    suspend fun updateEmail(newEmail: String): AuthResult
    suspend fun updatePassword(newPassword: String): AuthResult
    suspend fun signOut(): AuthResult
}