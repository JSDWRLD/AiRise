package com.teamnotfound.airise.data.network

import com.teamnotfound.airise.data.auth.TokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(
    engine: HttpClientEngine,
    tokenProvider: TokenProvider
): HttpClient = HttpClient(engine) {
    install(Logging) { level = LogLevel.ALL }
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }

    install(Auth) {
        bearer {
            // Only attach a token proactively if we're logged in.
            // This prevents TokenProvider from being called on public endpoints
            // (or before login), which would otherwise throw.
            sendWithoutRequest { request ->
                tokenProvider.currentUid() != null
            }

            loadTokens {
                // Only called when sendWithoutRequest returned true, or on 401 retry
                val access = tokenProvider.cachedOrFresh()
                BearerTokens(accessToken = access, refreshToken = "")
            }

            refreshTokens {
                tokenProvider.invalidate()
                val fresh = tokenProvider.forceFresh()
                BearerTokens(accessToken = fresh, refreshToken = "")
            }
        }
    }

    expectSuccess = false
}