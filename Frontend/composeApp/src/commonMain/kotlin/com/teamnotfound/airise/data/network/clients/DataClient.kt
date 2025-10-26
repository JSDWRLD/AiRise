package com.teamnotfound.airise.data.network.clients

import com.teamnotfound.airise.data.DTOs.LeaderboardEntryDTO
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.serializable.Challenge
import com.teamnotfound.airise.data.serializable.FoodDiaryMonth
import com.teamnotfound.airise.data.serializable.FoodEntry
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.util.NetworkError
import dev.gitlive.firebase.auth.FirebaseUser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
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

    // Upserts new Challenge (only available to ADMINS)
    suspend fun upsertChallenge(firebaseUser: FirebaseUser, challenge: Challenge) : Result<Boolean, NetworkError>{
        val token = firebaseUser.getIdToken(false).toString()
        val response = try {
            httpClient.post("$baseUrl/Challenge/upsert"){
                contentType(ContentType.Application.Json)
                setBody(challenge)
                bearerAuth(token)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }
        println(response)
        return when (response.status.value) {
            200 -> Result.Success(true)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            403 -> Result.Error(NetworkError.FORBIDDEN)
            409 -> Result.Error(NetworkError.CONFLICT)
            500-> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    // Delete a challenge (only for ADMINS)
    suspend fun deleteChallenge(firebaseUser: FirebaseUser, id: String) : Result<Boolean, NetworkError>{
        val token = firebaseUser.getIdToken(false).toString()
        val response = try {
            httpClient.delete("$baseUrl/Challenge/delete?id=$id") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        }
        return when (response.status.value) {
            201 -> Result.Success(true)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            403 -> Result.Error(NetworkError.FORBIDDEN)
            500-> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    // Gets a list on entries for the global leaderboard top 10
    // Already in sorted order, highest streak at top
    suspend fun getLeaderboardTop10(firebaseUser: FirebaseUser): com.teamnotfound.airise.data.network.Result<List<LeaderboardEntryDTO>, NetworkError> {
        val token = firebaseUser.getIdToken(true).toString()
        val response = try {
            httpClient.get("$baseUrl/User/leaderboard/global/top10") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
            }
        } catch (e: UnresolvedAddressException) {
            return com.teamnotfound.airise.data.network.Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return com.teamnotfound.airise.data.network.Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            200 -> {
                val entries = response.body<List<LeaderboardEntryDTO>>()
                com.teamnotfound.airise.data.network.Result.Success(entries)
            }

            400 -> com.teamnotfound.airise.data.network.Result.Error(NetworkError.BAD_REQUEST)
            409 -> com.teamnotfound.airise.data.network.Result.Error(NetworkError.CONFLICT)
            500-> com.teamnotfound.airise.data.network.Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    // Gets a list on entries for the global leaderboard top 100
    // Already in sorted order, highest streak at top
    suspend fun getLeaderboardTop100(firebaseUser: FirebaseUser): com.teamnotfound.airise.data.network.Result<List<LeaderboardEntryDTO>, NetworkError> {
        val token = firebaseUser.getIdToken(true).toString()
        val response = try {
            httpClient.get("$baseUrl/User/leaderboard/global/top100") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
            }
        } catch (e: UnresolvedAddressException) {
            return com.teamnotfound.airise.data.network.Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return com.teamnotfound.airise.data.network.Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            200 -> {
                val entries = response.body<List<LeaderboardEntryDTO>>()
                com.teamnotfound.airise.data.network.Result.Success(entries)
            }

            400 -> com.teamnotfound.airise.data.network.Result.Error(NetworkError.BAD_REQUEST)
            409 -> com.teamnotfound.airise.data.network.Result.Error(NetworkError.CONFLICT)
            500-> com.teamnotfound.airise.data.network.Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    // Gets a list on entries for the friend leaderboard
    // Already in sorted order, highest streak at top
    suspend fun getLeaderboardFriends(firebaseUser: FirebaseUser): com.teamnotfound.airise.data.network.Result<List<LeaderboardEntryDTO>, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(true).toString()
        val response = try {
            httpClient.get("$baseUrl/User/leaderboard/friends/$firebaseUid") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
            }
        } catch (e: UnresolvedAddressException) {
            return com.teamnotfound.airise.data.network.Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return com.teamnotfound.airise.data.network.Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            200 -> {
                val entries = response.body<List<LeaderboardEntryDTO>>()
                com.teamnotfound.airise.data.network.Result.Success(entries)
            }

            400 -> com.teamnotfound.airise.data.network.Result.Error(NetworkError.BAD_REQUEST)
            409 -> com.teamnotfound.airise.data.network.Result.Error(NetworkError.CONFLICT)
            500-> com.teamnotfound.airise.data.network.Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    /**
     * Fetches the food diary for a specific month and year for the authenticated user.
     *
     * @param firebaseUser The authenticated Firebase user. Ensure the user is logged in.
     * @param year The year of the diary to fetch (e.g., 2025).
     * @param month The month of the diary to fetch (1 for January, 12 for December).
     * @return A `Result` containing the `FoodDiaryMonth` if successful, or a `NetworkError` if an error occurs.
     *
     * Usage:
     * - Use this method to retrieve the entire food diary for a specific month.
     * - Pass the same `year` and `month` values as used in the backend.
     */
    suspend fun getFoodDiaryMonth(firebaseUser: FirebaseUser, year: Int, month: Int): Result<FoodDiaryMonth, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(true).toString()
        val response = try {
            httpClient.get("$baseUrl/diary/$firebaseUid/$year/$month") {
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
                val diaryMonth = response.body<FoodDiaryMonth>()
                Result.Success(diaryMonth)
            }

            400 -> Result.Error(NetworkError.BAD_REQUEST)
            409 -> Result.Error(NetworkError.CONFLICT)
            500 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    /**
     * Adds a new food entry to a specific day and meal in the food diary.
     *
     * @param firebaseUser The authenticated Firebase user. Ensure the user is logged in.
     * @param year The year of the diary to update (e.g., 2025).
     * @param month The month of the diary to update (1 for January, 12 for December).
     * @param day The day of the month to add the food entry (1-31).
     * @param meal The meal category to add the food entry to ("breakfast", "lunch", or "dinner").
     * @param foodEntry The `FoodEntry` object containing the details of the food (e.g., name, calories, etc.).
     * @return A `Result` indicating success or a `NetworkError` if an error occurs.
     *
     * Usage:
     * - Use this method to add a new food entry to a specific day and meal.
     * - Ensure the `FoodEntry` object is properly populated before passing it.
     * - Pass the same `year`, `month`, and `day` values as used in the backend.
     */
    suspend fun addFoodEntry(firebaseUser: FirebaseUser, year: Int, month: Int, day: Int, meal: String, foodEntry: FoodEntry): Result<Unit, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(true).toString()
        val response = try {
            httpClient.post("$baseUrl/diary/$firebaseUid/$year/$month/$day/meal/$meal") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(foodEntry)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            204 -> Result.Success(Unit)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            409 -> Result.Error(NetworkError.CONFLICT)
            500 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    /**
     * Edits an existing food entry in the food diary.
     *
     * @param firebaseUser The authenticated Firebase user. Ensure the user is logged in.
     * @param entryId The unique ID of the food entry to edit. This ID is returned by the backend when the entry is created.
     * @param updatedEntry The updated `FoodEntry` object containing the new details (e.g., name, calories, etc.).
     * @return A `Result` indicating success or a `NetworkError` if an error occurs.
     *
     * Usage:
     * - Use this method to update an existing food entry.
     * - Ensure the `entryId` matches the ID of the entry you want to edit.
     * - The `updatedEntry` object should contain the updated details, but the `id` field will remain unchanged.
     */
    suspend fun editFoodEntry(firebaseUser: FirebaseUser, entryId: String, updatedEntry: FoodEntry): Result<Unit, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(true).toString()
        val response = try {
            httpClient.patch("$baseUrl/diary/$firebaseUid/items/$entryId") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(updatedEntry)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            204 -> Result.Success(Unit)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            409 -> Result.Error(NetworkError.CONFLICT)
            500 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    /**
     * Deletes an existing food entry from the food diary.
     *
     * @param firebaseUser The authenticated Firebase user. Ensure the user is logged in.
     * @param entryId The unique ID of the food entry to delete. This ID is returned by the backend when the entry is created.
     * @return A `Result` indicating success or a `NetworkError` if an error occurs.
     *
     * Usage:
     * - Use this method to delete a food entry by its unique `entryId`.
     * - Ensure the `entryId` matches the ID of the entry you want to delete.
     */
    suspend fun deleteFoodEntry(firebaseUser: FirebaseUser, entryId: String): Result<Unit, NetworkError> {
        val firebaseUid = firebaseUser.uid
        val token = firebaseUser.getIdToken(true).toString()
        val response = try {
            httpClient.delete("$baseUrl/diary/$firebaseUid/items/$entryId") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            204 -> Result.Success(Unit)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            409 -> Result.Error(NetworkError.CONFLICT)
            500 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }
}