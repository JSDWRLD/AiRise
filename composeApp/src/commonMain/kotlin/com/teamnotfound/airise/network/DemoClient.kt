package com.teamnotfound.airise.network

import com.teamnotfound.airise.util.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import com.teamnotfound.airise.util.*
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType

class DemoClient(
    private val httpClient: HttpClient
) {
    suspend fun censorWords(uncensored: String): Result<String, NetworkError> {
        val response = try {
            httpClient.get(
                urlString = "https://www.purgomalum.com/service/json"
            ) {
                parameter("text", uncensored)
            }

            /* Post example
            httpClient.post(
                urlString = "www.website.com"
            ) {
                parameter("text", uncensored)
                contentType(ContentType.Application.Json
            }
             */
        } catch(e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch(e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when(response.status.value) {
            in 200..299 -> {
                // Specify the type of body we want to serialize to here.
                val censoredText = response.body<CensoredText>()
                Result.Success(censoredText.result)
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }
}