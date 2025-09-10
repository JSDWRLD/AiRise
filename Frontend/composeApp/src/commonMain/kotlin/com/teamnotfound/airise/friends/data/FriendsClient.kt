package com.teamnotfound.airise.friends.data

import com.teamnotfound.airise.data.DTOs.FriendsEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.HttpResponse

/**
 * Ktor client for Friends endpoints.
 */
class FriendsClient(
    private val http: HttpClient,
    baseUrl: String
) {
    private val apiBase: String = normalizeBase(baseUrl)

    private fun normalizeBase(b: String): String {
        val trimmed = b.trimEnd('/')
        return if (trimmed.endsWith("/api")) trimmed else "$trimmed/api"
    }
    private fun api(path: String) = "$apiBase/${path.trimStart('/')}"

    suspend fun getFriends(idToken: String, meUid: String): FriendsEnvelope {
        val res = http.get {
            url(api("UserFriends/$meUid"))
            contentType(ContentType.Application.Json)
            bearerAuth(idToken)
        }
        return res.parseOrThrow()
    }

    suspend fun addFriend(idToken: String, meUid: String, friendUid: String) {
        val res = http.post {
            // If your backend uses a different route (e.g. /AddFriend/{friendUid}), adjust here.
            url(api("UserFriends/$meUid/$friendUid"))
            contentType(ContentType.Application.Json)
            bearerAuth(idToken)
        }
        res.ensureSuccess()
    }

    suspend fun removeFriend(idToken: String, meUid: String, friendUid: String) {
        val res = http.delete {
            url(api("UserFriends/$meUid/$friendUid"))
            contentType(ContentType.Application.Json)
            bearerAuth(idToken)
        }
        res.ensureSuccess()
    }
    private suspend inline fun <reified T> HttpResponse.parseOrThrow(): T {
        if (!status.isSuccess()) {
            val err = runCatching { bodyAsText() }.getOrNull()
            error("Friends API ${status.value} ${status.description} — ${err ?: "(no body)"}")
        }
        return body()
    }

    private suspend fun HttpResponse.ensureSuccess() {
        if (!status.isSuccess()) {
            val err = runCatching { bodyAsText() }.getOrNull()
            error("Friends API ${status.value} ${status.description} — ${err ?: "(no body)"}")
        }
    }
}
