package com.teamnotfound.airise.data.network.clients

import com.teamnotfound.airise.data.DTOs.CreateUserDTO
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.serializable.User
import com.teamnotfound.airise.data.serializable.UserOnboardingData
import com.teamnotfound.airise.util.NetworkError
import dev.gitlive.firebase.auth.FirebaseUser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException

class UserClient(
    private val httpClient: HttpClient
) {
    private val baseUrl = "https://airise-b6aqbuerc0ewc2c5.westus-01.azurewebsites.net/api"

    /**
     * API call to register a new user.
     * We simply send the firebaseUid in the createUserRequest.
     */
    suspend fun insertUser(firebaseUser: FirebaseUser): Result<User, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(true).toString()

        val response = try {
            httpClient.post("$baseUrl/User") {
                contentType(ContentType.Application.Json)
                setBody(CreateUserDTO(firebaseUid))
                // bearerAuth(token)
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

    /**
     * API call to get user onboarding data.
     * Queries the user record using email.
     */
    suspend fun getUserOnboarding(email: String): Result<UserOnboardingData, NetworkError> {
        val response = try {
            //This is just a place holder for now.
            httpClient.get("http://localhost:5249/user/onboarding") {
                parameter("email", email)
            }
        } catch(e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch(e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when(response.status.value) {
            in 200..299 -> {
                val onboardingData = response.body<UserOnboardingData>()
                Result.Success(onboardingData)
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    /**
     * API call to insert or update user onboarding data.
     * Sends a PUT request with the onboarding data in JSON format.
     */
    suspend fun insertUserOnboarding(userOnboardingData: UserOnboardingData): Result<UserOnboardingData, NetworkError> {
        val response = try {
            //This is just a place holder for now.
            httpClient.put("http://localhost:5249/user/onboarding") {
                contentType(ContentType.Application.Json)
                setBody(userOnboardingData)
            }
        } catch(e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch(e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when(response.status.value) {
            in 200..299 -> {
                val updatedData = response.body<UserOnboardingData>()
                Result.Success(updatedData)
            }
            409 -> Result.Error(NetworkError.CONFLICT)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    /**
     * API call for user login.
     * Sends the user authentication data (email and password) to our /user/login endpoint.
     * If successful, should return user onboarding data.
     */
    /*
        suspend fun login(userCredentials: UserLogin): Result<UserModel, NetworkError> {
        val response = try {
            //This is just a place holder for now.
            httpClient.post("$baseUrl/Auth/login") {
                contentType(ContentType.Application.Json)
                setBody(userCredentials)
            }
        } catch(e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch(e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when(response.status.value) {
            in 200..299 -> {
                val onboardingData = response.body<UserModel>()
                Result.Success(onboardingData)
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }
    */
}