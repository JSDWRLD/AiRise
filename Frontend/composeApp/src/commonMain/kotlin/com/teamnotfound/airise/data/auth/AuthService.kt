package com.teamnotfound.airise.data.auth

import com.teamnotfound.airise.data.network.clients.UserClient
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider


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
        auth.authStateChanged.map { it?.let { User(it.uid, it.email) } ?: User()  }



   override suspend fun authenticateWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = dev.gitlive.firebase.auth.GoogleAuthProvider.credential(idToken,null)
            val result = auth.signInWithCredential(credential)
            val firebaseUser = result.user

            if (firebaseUser != null) {
                // Check if this is a new user
                val isNewUser = result.additionalUserInfo?.isNewUser ?: false

                if (isNewUser) {
                    // Create a new user record in DB
                    userClient.insertUser(firebaseUser, firebaseUser.email?: "")
                }

                val user = User(id = firebaseUser.uid, email = firebaseUser.email)
                AuthResult.Success(user)
            } else {
                AuthResult.Failure("Google authentication failed: user is null")
            }
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "Google authentication failed")
        }
    }


    override suspend fun authenticate(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password)
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = User(id = firebaseUser.uid, email = firebaseUser.email)
                AuthResult.Success(user)
            } else {
                AuthResult.Failure("Authentication failed: user is null")
            }
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "Authentication failed")
        }
    }

    override suspend fun createUser(email: String, password: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password)

            val firebaseUser = result.user
            if (firebaseUser != null) {
                userClient.insertUser(firebaseUser, email)
                val user = User(id = firebaseUser.uid, email = email)
                AuthResult.Success(user)
            } else {
                AuthResult.Failure("User creation failed: user is null")
            }

        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "User creation failed")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email)
            val firebaseUser = auth.currentUser
            val user = firebaseUser?.let { User(it.uid, it.email) } ?: User()
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "Failed to send password reset email")
        }
    }

    override suspend fun updateEmail(newEmail: String): AuthResult {
        return try {
            val user = auth.currentUser
            user?.verifyBeforeUpdateEmail(newEmail)
            if (user != null) {
                val userAdd = User(user.uid, newEmail)
                AuthResult.Success(userAdd)
            } else {
                AuthResult.Failure("Failed to update email: no current user")
            }
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "Failed to update email")
        }
    }

    override suspend fun updatePassword(newPassword: String): AuthResult {
        return try {
            val user = auth.currentUser
            user?.updatePassword(newPassword)
            if (user != null) {
                val userAdd = User(user.uid, user.email)
                AuthResult.Success(userAdd)
            } else {
                AuthResult.Failure("Failed to update password: no current user")
            }
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "Failed to update password")
        }
    }

    override suspend fun signOut(): AuthResult {
        return try {
            auth.signOut()
            AuthResult.Success(User())
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