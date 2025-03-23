package com.teamnotfound.airise.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(engine: HttpClientEngine): HttpClient {
    return HttpClient(engine) {
        install(Logging) {
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            // Define Json parsing library
            json(
                json = Json {
                    // If API has json fields that we dont care about we ignore, no crash
                    ignoreUnknownKeys = true
                }
            )
        }

        /*  Ktor will intercept and make sure ur authenticated before sending requests.
            install(Auth) {

            }
         */
    }
}