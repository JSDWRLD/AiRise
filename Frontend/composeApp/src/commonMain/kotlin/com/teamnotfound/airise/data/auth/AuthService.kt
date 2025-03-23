package com.teamnotfound.airise.data.auth

import com.teamnotfound.airise.data.network.clients.UserClient
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class AuthService(
    private val auth: FirebaseAuth,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val userClient: UserClient
) : IAuthService {

    override val currentUserId: String
        get() = auth.currentUser?.uid.toString()

    override val isAuthenticated: Boolean
        get() = auth.currentUser != null

    override val currentUser: Flow<User> =
        auth.authStateChanged.map { it?.let { User(it.uid) } ?: User() }

    override suspend fun authenticate(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password)
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "Authentication failed")
        }
    }

    override suspend fun createUser(email: String, password: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password)

            val firebaseUser = result.user
            if (firebaseUser != null) {
                userClient.insertUser(firebaseUser)
            }

            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "User creation failed")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email)
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "Failed to send password reset email")
        }
    }

    override suspend fun updateEmail(newEmail: String): AuthResult {
        return try {
            val user = auth.currentUser
            user?.verifyBeforeUpdateEmail(newEmail)
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "Failed to update email")
        }
    }

    override suspend fun updatePassword(newPassword: String): AuthResult {
        return try {
            val user = auth.currentUser
            user?.updatePassword(newPassword)
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "Failed to update password")
        }
    }

    override suspend fun signOut(): AuthResult {
        return try {
            auth.signOut()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "Failed to update password")
        }
    }

    suspend fun getIdToken(): String? {
        val currentUser: FirebaseUser? = auth.currentUser
        return try {
            currentUser?.getIdToken(false) // no force refresh
        } catch (e: Exception) {
            // Handle exceptions (e.g., log, return null)
            e.printStackTrace()
            null
        }
    }
}