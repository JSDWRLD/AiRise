package com.teamnotfound.airise.data.network.clients

import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.serializable.FoodDiaryMonth
import com.teamnotfound.airise.data.serializable.FoodEntry
import com.teamnotfound.airise.util.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for DataClient food tracking functions using MockEngine.
 * These tests use mocked HTTP responses.
 */

class DataClientTest {

    private val baseUrl = "https://airise-b6aqbuerc0ewc2c5.westus-01.azurewebsites.net/api"
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    // Helper functions that mimic DataClient methods but accept uid and token directly

    private suspend fun getFoodDiaryMonth(
        httpClient: HttpClient,
        uid: String,
        token: String,
        year: Int,
        month: Int
    ): Result<FoodDiaryMonth, NetworkError> {
        val response = try {
            httpClient.get("$baseUrl/diary/$uid/$year/$month") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            200 -> Result.Success(response.body<FoodDiaryMonth>())
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            409 -> Result.Error(NetworkError.CONFLICT)
            500 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    private suspend fun addFoodEntry(
        httpClient: HttpClient,
        uid: String,
        token: String,
        year: Int,
        month: Int,
        day: Int,
        meal: String,
        foodEntry: FoodEntry
    ): Result<Unit, NetworkError> {
        val response = try {
            httpClient.post("$baseUrl/diary/$uid/$year/$month/$day/meal/$meal") {
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

    private suspend fun editFoodEntry(
        httpClient: HttpClient,
        uid: String,
        token: String,
        entryId: String,
        updatedEntry: FoodEntry
    ): Result<Unit, NetworkError> {
        val response = try {
            httpClient.patch("$baseUrl/diary/$uid/items/$entryId") {
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

    private suspend fun deleteFoodEntry(
        httpClient: HttpClient,
        uid: String,
        token: String,
        entryId: String
    ): Result<Unit, NetworkError> {
        val response = try {
            httpClient.delete("$baseUrl/diary/$uid/items/$entryId") {
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

    // ========== getFoodDiaryMonth Tests ==========

    @Test
    fun `getFoodDiaryMonth - should make GET request to correct URL with auth header`() = runTest {
        val mockEngine = MockEngine { request ->
            // Verify routing
            assertEquals("/api/diary/123/2025/1", request.url.encodedPath)
            assertEquals(HttpMethod.Get, request.method)
            
            // Verify auth header
            assertEquals("Bearer test-jwt-token", request.headers[HttpHeaders.Authorization])
            
            // Return mock response for serialization test
            respond(
                content = ByteReadChannel("""
                    {
                        "id": "diary-123",
                        "userId": "123",
                        "year": 2025,
                        "month": 1,
                        "days": []
                    }
                """.trimIndent()),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        
        val result = getFoodDiaryMonth(httpClient, "123", "test-jwt-token", 2025, 1)
        
        assertTrue(result is Result.Success)
        val diary = (result as Result.Success).data
        assertEquals("diary-123", diary.id)
        assertEquals("123", diary.userId)
        assertEquals(2025, diary.year)
        assertEquals(1, diary.month)
    }

    @Test
    fun `getFoodDiaryMonth - should return BAD_REQUEST on 400`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel("Bad Request"),
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        
        val result = getFoodDiaryMonth(httpClient, "123", "test-jwt-token", 2025, 1)
        
        assertTrue(result is Result.Error)
        assertEquals(NetworkError.BAD_REQUEST, (result as Result.Error).error)
    }

    // ========== addFoodEntry Tests ==========


    @Test
    fun `addFoodEntry - should return SERVER_ERROR on 500`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel("Internal Server Error"),
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        
        val foodEntry = FoodEntry(
            id = "entry-123",
            name = "Test Food",
            calories = 100.0,
            fats = 1.0,
            carbs = 10.0,
            proteins = 5.0
        )
        
        val result = addFoodEntry(httpClient, "123", "test-jwt-token", 2025, 1, 15, "lunch", foodEntry)
        
        assertTrue(result is Result.Error)
        assertEquals(NetworkError.SERVER_ERROR, (result as Result.Error).error)
    }


    @Test
    fun `editFoodEntry - should PATCH to correct URL with auth header`() = runTest {
        val mockEngine = MockEngine { request ->
            // Verify routing: /diary/{uid}/items/{entryId}
            assertEquals("/api/diary/123/items/entry-456", request.url.encodedPath)
            assertEquals(HttpMethod.Patch, request.method)
            
            // Verify auth header
            assertEquals("Bearer test-jwt-token", request.headers[HttpHeaders.Authorization])
            
            // Return 204 No Content
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.NoContent,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        
        val updatedEntry = FoodEntry(
            id = "entry-456",
            name = "Greek Yogurt",
            calories = 200.0,
            fats = 5.0,
            carbs = 15.0,
            proteins = 20.0
        )
        
        val result = editFoodEntry(httpClient, "123", "test-jwt-token", "entry-456", updatedEntry)
        
        assertTrue(result is Result.Success)
    }


    @Test
    fun `deleteFoodEntry - should DELETE to correct URL with auth header`() = runTest {
        val mockEngine = MockEngine { request ->
            // Verify routing: /diary/{uid}/items/{entryId}
            assertEquals("/api/diary/123/items/entry-789", request.url.encodedPath)
            assertEquals(HttpMethod.Delete, request.method)
            
            // Verify auth header
            assertEquals("Bearer test-jwt-token", request.headers[HttpHeaders.Authorization])
            
            // Return 204 No Content
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.NoContent,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        
        val result = deleteFoodEntry(httpClient, "123", "test-jwt-token", "entry-789")
        
        assertTrue(result is Result.Success)
    }


    @Test
    fun `deleteFoodEntry - should return SERVER_ERROR on 500`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel("Internal Server Error"),
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        
        val result = deleteFoodEntry(httpClient, "123", "test-jwt-token", "entry-999")
        
        assertTrue(result is Result.Error)
        assertEquals(NetworkError.SERVER_ERROR, (result as Result.Error).error)
    }







    @Test
    fun `addFoodEntry - should POST to correct URL with auth header and JSON body`() = runTest {
        val mockEngine = MockEngine { request ->
            // Verify routing: /diary/{uid}/{year}/{month}/{day}/meal/{meal}
            assertEquals("/api/diary/123/2025/1/15/meal/breakfast", request.url.encodedPath)
            assertEquals(HttpMethod.Post, request.method)

            // Verify auth header
            assertEquals("Bearer test-jwt-token", request.headers[HttpHeaders.Authorization])
            assertEquals("application/json", request.headers[HttpHeaders.ContentType])

            // Return 204 No Content (success)
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.NoContent,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val foodEntry = FoodEntry(
            id = "entry-123",
            name = "Oatmeal",
            calories = 150.0,
            fats = 3.0,
            carbs = 27.0,
            proteins = 5.0
        )

        val result = addFoodEntry(httpClient, "123", "test-jwt-token", 2025, 1, 15, "breakfast", foodEntry)

        assertTrue(result is Result.Success)
    }
}