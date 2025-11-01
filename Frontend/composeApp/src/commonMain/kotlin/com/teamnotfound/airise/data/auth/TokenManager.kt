package com.teamnotfound.airise.data.auth

import kotlinx.serialization.json.longOrNull
import dev.gitlive.firebase.auth.FirebaseAuth
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.datetime.Clock
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

interface TokenProvider {
    suspend fun cachedOrFresh(): String
    suspend fun forceFresh(): String
    fun currentUid(): String?
    fun invalidate()
}

class FirebaseTokenManager(
    private val auth: dev.gitlive.firebase.auth.FirebaseAuth
) : TokenProvider {
    private data class Cache(val token: String, val exp: Long)
    private var cache: Cache? = null
    private val mutex = kotlinx.coroutines.sync.Mutex()
    private val earlyRefreshSeconds = 120L

    override fun currentUid(): String? = auth.currentUser?.uid
    override fun invalidate() { cache = null }

    override suspend fun cachedOrFresh(): String = mutex.withLock {
        val now = kotlinx.datetime.Clock.System.now().epochSeconds
        cache?.let { if (now < it.exp - earlyRefreshSeconds) return it.token }
        return refreshInternal(force = false)
    }

    override suspend fun forceFresh(): String = mutex.withLock {
        refreshInternal(force = true)
    }

    private suspend fun refreshInternal(force: Boolean): String {
        val jwt = auth.currentUser?.getIdToken(force) ?: error("No authenticated user")
        val exp = decodeExp(jwt) ?: (kotlin.math.max(0, kotlinx.datetime.Clock.System.now().epochSeconds) + 55 * 60)
        cache = Cache(jwt, exp)
        return jwt
    }

    // --- minimal JWT exp decoder (no network) ---
    @OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
    private fun decodeExp(jwt: String): Long? {
        val parts = jwt.split(".")
        if (parts.size < 2) return null
        val payload = parts[1]
        val padded = when (payload.length % 4) { 2 -> payload + "=="; 3 -> payload + "="; else -> payload }
        val json = kotlin.io.encoding.Base64.UrlSafe.decode(padded).decodeToString()
        val obj = kotlinx.serialization.json.Json.parseToJsonElement(json).jsonObject
        return obj["exp"]?.jsonPrimitive?.longOrNull
    }
}

