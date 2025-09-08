package com.teamnotfound.airise.friends.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.appendPathSegments
import com.teamnotfound.airise.data.DTOs.FriendsEnvelope

/**
 * Very small, explicit client for the /api/UserFriends endpoints.
 */
class FriendsClient(
    private val http: HttpClient,
    private val baseUrl: String
) {
    suspend fun getFriends(idToken: String, firebaseUid: String): FriendsEnvelope =
        http.get("$baseUrl/api/UserFriends") {
            url.appendPathSegments(firebaseUid)
            header("Authorization", "Bearer $idToken")
        }.body()

    suspend fun addFriend(idToken: String, firebaseUid: String, friendUid: String) {
        http.post("$baseUrl/api/UserFriends") {
            url.appendPathSegments(firebaseUid)
            url.parameters.append("friendFirebaseUid", friendUid)
            header("Authorization", "Bearer $idToken")
        }
    }

    suspend fun removeFriend(idToken: String, firebaseUid: String, friendUid: String) {
        http.delete("$baseUrl/api/UserFriends") {
            url.appendPathSegments(firebaseUid)
            url.parameters.append("friendFirebaseUid", friendUid)
            header("Authorization", "Bearer $idToken")
        }
    }
}
