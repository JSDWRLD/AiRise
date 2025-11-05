package com.teamnotfound.airise.data.auth

import kotlinx.coroutines.flow.Flow
import dev.gitlive.firebase.auth.FirebaseUser

interface IAuthService {

    val currentUserId: String
    val isAuthenticated: Boolean
    val currentUser: Flow<User>
    // Expose firebaseUser for callers that need the platform user (nullable)
    val firebaseUser: FirebaseUser?

    suspend fun authenticate(email: String, password: String): AuthResult
    suspend fun createUser(email: String, password: String): AuthResult
    suspend fun sendPasswordResetEmail(email: String): AuthResult
    suspend fun updateEmail(newEmail: String): AuthResult
    suspend fun updatePassword(newPassword: String): AuthResult
    suspend fun signOut(): AuthResult
    suspend fun authenticateWithGoogle(idToken: String): AuthResult
    suspend fun getIdToken(): String?
}