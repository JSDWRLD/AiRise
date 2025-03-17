package com.teamnotfound.airise.network

import com.teamnotfound.airise.serializable.UserAuthData
import com.teamnotfound.airise.serializable.UserOnboardingData
import com.teamnotfound.airise.util.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import com.teamnotfound.airise.util.*
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.contentType

class UserClient(
    private val httpClient: HttpClient
) {
    /**
     * API call for user login.
     * Sends the user authentication data (email and password) to our /user/login endpoint.
     * If successful, should return user onboarding data.
     */
    suspend fun login(userAuthData: UserAuthData): Result<UserOnboardingData, NetworkError> {
        val response = try {
            //This is just a place holder for now.
            httpClient.post("http://localhost:5249/user/login") {
                contentType(ContentType.Application.Json)
                setBody(userAuthData)
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
}