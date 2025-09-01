package com.teamnotfound.airise.data.network.clients

import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.serializable.Challenge
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.util.NetworkError
import dev.gitlive.firebase.auth.FirebaseUser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException

class DataClient(
    private val httpClient: HttpClient
) {
    private val baseUrl = "https://airise-b6aqbuerc0ewc2c5.westus-01.azurewebsites.net/api"

    // Gets Challenges list, has 3 strings name, description, url
    suspend fun getChallenges(): com.teamnotfound.airise.data.network.Result<List<Challenge>, NetworkError> {
        val response = try {
            httpClient.get("$baseUrl/Challenge") {
                contentType(ContentType.Application.Json)
            }
        } catch (e: UnresolvedAddressException) {
            return com.teamnotfound.airise.data.network.Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return com.teamnotfound.airise.data.network.Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            200 -> {
                val challenges = response.body<List<Challenge>>()
                com.teamnotfound.airise.data.network.Result.Success(challenges)
            }

            400 -> com.teamnotfound.airise.data.network.Result.Error(NetworkError.BAD_REQUEST)
            409 -> com.teamnotfound.airise.data.network.Result.Error(NetworkError.CONFLICT)
            500-> com.teamnotfound.airise.data.network.Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }
}