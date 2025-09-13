package com.teamnotfound.airise.data.network.clients

import com.teamnotfound.airise.data.DTOs.CreateUserDTO
import com.teamnotfound.airise.data.DTOs.UsersEnvelope
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.serializable.User
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.data.serializable.HealthData
import com.teamnotfound.airise.data.serializable.SetActiveReq
import com.teamnotfound.airise.data.serializable.UidOnlyReq
import com.teamnotfound.airise.data.serializable.UserChallenge
import com.teamnotfound.airise.util.NetworkError
import dev.gitlive.firebase.auth.FirebaseUser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import com.teamnotfound.airise.data.serializable.UserSettingsData

class   UserClient(
    private val httpClient: HttpClient
) {
    private val baseUrl = "https://airise-b6aqbuerc0ewc2c5.westus-01.azurewebsites.net/api"

    /**
     * API call to register a new user.
     * We simply send the firebaseUid in the createUserRequest.
     */
    suspend fun insertUser(firebaseUser: FirebaseUser, email: String): Result<User, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(true).toString()
        val response = try {
            httpClient.post("$baseUrl/User") {
                contentType(ContentType.Application.Json)
                setBody(CreateUserDTO(firebaseUid, email))
                bearerAuth(token)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            201 -> {
                val registeredUser = response.body<User>()
                Result.Success(registeredUser)
            }

            400 -> Result.Error(NetworkError.BAD_REQUEST)
            409 -> Result.Error(NetworkError.CONFLICT)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    suspend fun getUserData(firebaseUser: FirebaseUser): Result<UserData, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(true).toString()
        val response = try {
            httpClient.get("$baseUrl/UserData/$firebaseUid") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            200 -> {
                val registeredUser = response.body<UserData>()
                Result.Success(registeredUser)
            }

            400 -> Result.Error(NetworkError.BAD_REQUEST)
            409 -> Result.Error(NetworkError.CONFLICT)
            500-> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    suspend fun insertUserData(
        firebaseUser: FirebaseUser,
        userData: UserData
    ): Result<UserData, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(true).toString()

        val response = try {
            httpClient.put("$baseUrl/UserData/$firebaseUid") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(userData)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            200 -> {
                val userDataRes = response.body<UserData>()
                Result.Success(userDataRes)
            }
            201 -> {
                val userDataRes = response.body<UserData>()
                Result.Success(userDataRes)
            }

            400 -> Result.Error(NetworkError.BAD_REQUEST)
            409 -> Result.Error(NetworkError.CONFLICT)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    /**
     * API call to insert user health data
     * Sends a POST request with the health data
     */
    suspend fun insertHealthData(
        firebaseUser: FirebaseUser,
        healthData: HealthData
    ): Result<Boolean, NetworkError> {
        val token = firebaseUser.getIdToken(true).toString()

        val response = try {
            httpClient.post("$baseUrl/User/UserHealthData") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(healthData)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> Result.Success(true)
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    suspend fun getUserSettings(firebaseUser: FirebaseUser): Result<UserSettingsData, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(true).toString()
        val response = try {
            httpClient.get("$baseUrl/UserSettings/$firebaseUid") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            200 -> {
                val userSettings = response.body<UserSettingsData>()
                Result.Success(userSettings)
            }

            400 -> Result.Error(NetworkError.BAD_REQUEST)
            409 -> Result.Error(NetworkError.CONFLICT)
            500-> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    suspend fun upsertUserSettings(userSettings: UserSettingsData, firebaseUser: FirebaseUser): Result<Boolean, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(true).toString()

        val response = try {
            httpClient.put("$baseUrl/UserSettings/$firebaseUid") { // Modified URL path
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(userSettings)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> Result.Success(true)
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    suspend fun resetStreak(
        firebaseUser: FirebaseUser
    ): Result<Boolean, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(true).toString()

        val response = try {
            httpClient.post("$baseUrl/User/$firebaseUid/streak/reset") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> Result.Success(true)
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    // Function to update the streak of a user, the streak param is inside user object already
    suspend fun incrementStreak(
        firebaseUser: FirebaseUser
    ): Result<Boolean, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(true).toString()

        val response = try {
            httpClient.post("$baseUrl/User/$firebaseUid/streak") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> Result.Success(true)
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }
    suspend fun searchUsers(firebaseUser: FirebaseUser, query: String): Result<UsersEnvelope, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(false).toString()

        val response = try {
            httpClient.get("$baseUrl/UserData/search-user/$firebaseUid?query=$query") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }
        return when (response.status.value) {
            200 -> {
                val users = response.body<UsersEnvelope>()
                Result.Success(users)
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    // GET /api/UserChallenges/{uid}
    suspend fun getUserChallenges(
        firebaseUser: FirebaseUser
    ): Result<UserChallenge, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(true).toString()

        val response = try {
            httpClient.get("$baseUrl/UserChallenges/$firebaseUid") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            200 -> Result.Success(response.body<UserChallenge>())
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            500 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    // GET /api/UserChallenges/completed-today/{uid}
    suspend fun hasCompletedToday(
        firebaseUser: FirebaseUser
    ): Result<Boolean, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(true).toString()

        val response = try {
            httpClient.get("$baseUrl/UserChallenges/completed-today/$firebaseUid") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            200 -> Result.Success(response.body<Boolean>())
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    // POST /api/UserChallenges/set-active  { firebaseUid, challengeId }
    suspend fun setActiveChallenge(
        firebaseUser: FirebaseUser,
        challengeId: String
    ): Result<Boolean, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(true).toString()

        val response = try {
            httpClient.post("$baseUrl/UserChallenges/set-active") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(SetActiveReq(firebaseUid = firebaseUid, challengeId = challengeId))
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> Result.Success(true)
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    // POST /api/UserChallenges/complete-today  { firebaseUid }
    // Returns the updated UserChallenge (so you get the new lastCompletionEpochDay)
    suspend fun markCompleteToday(
        firebaseUser: FirebaseUser
    ): Result<UserChallenge, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(true).toString()

        val response = try {
            httpClient.post("$baseUrl/UserChallenges/complete-today") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(UidOnlyReq(firebaseUid = firebaseUid))
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            200 -> Result.Success(response.body<UserChallenge>())
            201 -> Result.Success(response.body<UserChallenge>())
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }
}