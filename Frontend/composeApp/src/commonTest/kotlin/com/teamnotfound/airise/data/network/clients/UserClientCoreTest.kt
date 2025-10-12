package com.teamnotfound.airise.data.network.clients

import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.data.serializable.UserSettingsData
import com.teamnotfound.airise.util.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.put
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
 * Unit tests for UserClient core functions using MockEngine.
 * Tests getUserData, insertUserData (upsert), getUserSettings, and upsertUserSettings.
 * These tests use mocked HTTP responses.
 */

class UserClientCoreTest {

    private val baseUrl = "https://airise-b6aqbuerc0ewc2c5.westus-01.azurewebsites.net/api"
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    // Helper functions that mimic UserClient methods but accept uid and token directly

    private suspend fun getUserData(
        httpClient: HttpClient,
        uid: String,
        token: String
    ): Result<UserData, NetworkError> {
        val response = try {
            httpClient.get("$baseUrl/UserData/$uid") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            200 -> Result.Success(response.body<UserData>())
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            409 -> Result.Error(NetworkError.CONFLICT)
            500 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    private suspend fun insertUserData(
        httpClient: HttpClient,
        uid: String,
        token: String,
        userData: UserData
    ): Result<UserData, NetworkError> {
        val response = try {
            httpClient.put("$baseUrl/UserData/$uid") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(userData)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            200 -> Result.Success(response.body<UserData>())
            201 -> Result.Success(response.body<UserData>())
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            409 -> Result.Error(NetworkError.CONFLICT)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    private suspend fun getUserSettings(
        httpClient: HttpClient,
        uid: String,
        token: String
    ): Result<UserSettingsData, NetworkError> {
        val response = try {
            httpClient.get("$baseUrl/UserSettings/$uid") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            200 -> Result.Success(response.body<UserSettingsData>())
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            409 -> Result.Error(NetworkError.CONFLICT)
            500 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    private suspend fun upsertUserSettings(
        httpClient: HttpClient,
        uid: String,
        token: String,
        userSettings: UserSettingsData
    ): Result<Boolean, NetworkError> {
        val response = try {
            httpClient.put("$baseUrl/UserSettings/$uid") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(userSettings)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> Result.Success(true)
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    // ========== getUserData Tests ==========

    @Test
    fun `getUserData - should GET from correct URL with auth header`() = runTest {
        val mockEngine = MockEngine { request ->
            // Verify routing: /UserData/{uid}
            assertEquals("/api/UserData/123", request.url.encodedPath)
            assertEquals(HttpMethod.Get, request.method)
            
            // Verify auth header
            assertEquals("Bearer test-jwt-token", request.headers[HttpHeaders.Authorization])
            assertEquals("application/json", request.headers[HttpHeaders.ContentType])
            
            // Return mock response for serialization test
            respond(
                content = ByteReadChannel("""
                    {
                        "firstName": "John",
                        "lastName": "Doe",
                        "middleName": "",
                        "fullName": "John Doe",
                        "workoutGoal": "Build Muscle",
                        "fitnessLevel": "Intermediate",
                        "workoutLength": 60,
                        "workoutEquipment": "Full Gym",
                        "workoutDays": ["Monday", "Wednesday", "Friday"],
                        "workoutTime": "Morning",
                        "dietaryGoal": "Maintain",
                        "workoutRestrictions": "None",
                        "heightMetric": true,
                        "heightValue": 180,
                        "weightMetric": true,
                        "weightValue": 75,
                        "dobDay": 15,
                        "dobMonth": 6,
                        "dobYear": 1990,
                        "activityLevel": "Moderate"
                    }
                """.trimIndent()),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        
        val result = getUserData(httpClient, "123", "test-jwt-token")
        
        assertTrue(result is Result.Success)
        val userData = (result as Result.Success).data
        assertEquals("John", userData.firstName)
        assertEquals("Doe", userData.lastName)
        assertEquals("John Doe", userData.fullName)
    }

    @Test
    fun `getUserData - should return SERVER_ERROR on 500`() = runTest {
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
        
        val result = getUserData(httpClient, "123", "test-jwt-token")
        
        assertTrue(result is Result.Error)
        assertEquals(NetworkError.SERVER_ERROR, (result as Result.Error).error)
    }

    @Test
    fun `getUserData - should return BAD_REQUEST on 400`() = runTest {
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
        
        val result = getUserData(httpClient, "123", "test-jwt-token")
        
        assertTrue(result is Result.Error)
        assertEquals(NetworkError.BAD_REQUEST, (result as Result.Error).error)
    }

    // ========== insertUserData Tests ==========

    @Test
    fun `insertUserData - should PUT to correct URL with auth header and JSON body`() = runTest {
        val mockEngine = MockEngine { request ->
            // Verify routing: /UserData/{uid}
            assertEquals("/api/UserData/123", request.url.encodedPath)
            assertEquals(HttpMethod.Put, request.method)
            
            // Verify auth header (Content-Type is set by ContentNegotiation plugin)
            assertEquals("Bearer test-jwt-token", request.headers[HttpHeaders.Authorization])
            
            // Return 200 OK with updated data
            respond(
                content = ByteReadChannel("""
                    {
                        "firstName": "Jane",
                        "lastName": "Smith",
                        "middleName": "",
                        "fullName": "Jane Smith",
                        "workoutGoal": "Lose Weight",
                        "fitnessLevel": "Beginner",
                        "workoutLength": 45,
                        "workoutEquipment": "Home",
                        "workoutDays": ["Tuesday", "Thursday"],
                        "workoutTime": "Evening",
                        "dietaryGoal": "Cut",
                        "workoutRestrictions": "Knee injury",
                        "heightMetric": true,
                        "heightValue": 165,
                        "weightMetric": true,
                        "weightValue": 60,
                        "dobDay": 10,
                        "dobMonth": 3,
                        "dobYear": 1995,
                        "activityLevel": "Light"
                    }
                """.trimIndent()),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        
        val userData = UserData(
            firstName = "Jane",
            lastName = "Smith",
            middleName = "",
            fullName = "Jane Smith",
            workoutGoal = "Lose Weight",
            fitnessLevel = "Beginner",
            workoutLength = 45,
            workoutEquipment = "Home",
            workoutDays = listOf("Tuesday", "Thursday"),
            workoutTime = "Evening",
            dietaryGoal = "Cut",
            workoutRestrictions = "Knee injury",
            heightMetric = true,
            heightValue = 165,
            weightMetric = true,
            weightValue = 60,
            dobDay = 10,
            dobMonth = 3,
            dobYear = 1995,
            activityLevel = "Light"
        )
        
        val result = insertUserData(httpClient, "123", "test-jwt-token", userData)
        
        assertTrue(result is Result.Success)
        val updatedData = (result as Result.Success).data
        assertEquals("Jane", updatedData.firstName)
        assertEquals("Smith", updatedData.lastName)
    }

    @Test
    fun `insertUserData - should accept 201 Created status`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel("""
                    {
                        "firstName": "New",
                        "lastName": "User",
                        "middleName": "",
                        "fullName": "New User",
                        "workoutGoal": "General Fitness",
                        "fitnessLevel": "Beginner",
                        "workoutLength": 30,
                        "workoutEquipment": "None",
                        "workoutDays": ["Monday"],
                        "workoutTime": "Morning",
                        "dietaryGoal": "Maintain",
                        "workoutRestrictions": "None",
                        "heightMetric": false,
                        "heightValue": 170,
                        "weightMetric": false,
                        "weightValue": 70,
                        "dobDay": 1,
                        "dobMonth": 1,
                        "dobYear": 2000,
                        "activityLevel": "Sedentary"
                    }
                """.trimIndent()),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        
        val userData = UserData(
            firstName = "New",
            lastName = "User",
            middleName = "",
            fullName = "New User",
            workoutGoal = "General Fitness",
            fitnessLevel = "Beginner",
            workoutLength = 30,
            workoutEquipment = "None",
            workoutDays = listOf("Monday"),
            workoutTime = "Morning",
            dietaryGoal = "Maintain",
            workoutRestrictions = "None",
            heightMetric = false,
            heightValue = 170,
            weightMetric = false,
            weightValue = 70,
            dobDay = 1,
            dobMonth = 1,
            dobYear = 2000,
            activityLevel = "Sedentary"
        )
        
        val result = insertUserData(httpClient, "123", "test-jwt-token", userData)
        
        assertTrue(result is Result.Success)
        val createdData = (result as Result.Success).data
        assertEquals("New", createdData.firstName)
        assertEquals("User", createdData.lastName)
    }

    @Test
    fun `insertUserData - should return CONFLICT on 409`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel("Conflict"),
                status = HttpStatusCode.Conflict,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        
        val userData = UserData(
            firstName = "Test",
            lastName = "User",
            middleName = "",
            fullName = "Test User",
            workoutGoal = "General Fitness",
            fitnessLevel = "Beginner",
            workoutLength = 30,
            workoutEquipment = "None",
            workoutDays = listOf("Monday"),
            workoutTime = "Morning",
            dietaryGoal = "Maintain",
            workoutRestrictions = "None",
            heightMetric = false,
            heightValue = 170,
            weightMetric = false,
            weightValue = 70,
            dobDay = 1,
            dobMonth = 1,
            dobYear = 2000,
            activityLevel = "Sedentary"
        )
        
        val result = insertUserData(httpClient, "123", "test-jwt-token", userData)
        
        assertTrue(result is Result.Error)
        assertEquals(NetworkError.CONFLICT, (result as Result.Error).error)
    }

    // ========== getUserSettings Tests ==========

    @Test
    fun `getUserSettings - should GET from correct URL and deserialize response`() = runTest {
        val mockEngine = MockEngine { request ->
            // Verify routing: /UserSettings/{uid}
            assertEquals("/api/UserSettings/123", request.url.encodedPath)
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("Bearer test-jwt-token", request.headers[HttpHeaders.Authorization])
            
            // Return mock response
            respond(
                content = ByteReadChannel("""
                    {
                        "_id": "settings-123",
                        "firebaseUid": "123",
                        "profile_picture_url": "https://example.com/photo.jpg",
                        "ai_personality": "friendly",
                        "challenge_notifs_enabled": true,
                        "friend_req_notifs_enabled": false,
                        "streak_notifs_enabled": true,
                        "meal_notifs_enabled": true
                    }
                """.trimIndent()),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        
        val result = getUserSettings(httpClient, "123", "test-jwt-token")
        
        assertTrue(result is Result.Success)
        val settings = (result as Result.Success).data
        assertEquals("123", settings.firebaseUid)
        assertEquals("friendly", settings.aiPersonality)
        assertTrue(settings.challengeNotifsEnabled)
        assertTrue(settings.mealNotifsEnabled)
    }

    @Test
    fun `getUserSettings - should return BAD_REQUEST on 400`() = runTest {
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
        
        val result = getUserSettings(httpClient, "123", "test-jwt-token")
        
        assertTrue(result is Result.Error)
        assertEquals(NetworkError.BAD_REQUEST, (result as Result.Error).error)
    }

    @Test
    fun `getUserSettings - should return SERVER_ERROR on 500`() = runTest {
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
        
        val result = getUserSettings(httpClient, "123", "test-jwt-token")
        
        assertTrue(result is Result.Error)
        assertEquals(NetworkError.SERVER_ERROR, (result as Result.Error).error)
    }

    // ========== upsertUserSettings Tests ==========

    @Test
    fun `upsertUserSettings - should PUT to correct URL with auth header and JSON body`() = runTest {
        val mockEngine = MockEngine { request ->
            // Verify routing: /UserSettings/{uid}
            assertEquals("/api/UserSettings/123", request.url.encodedPath)
            assertEquals(HttpMethod.Put, request.method)
            // Auth header verification (Content-Type is set by ContentNegotiation plugin)
            assertEquals("Bearer test-jwt-token", request.headers[HttpHeaders.Authorization])
            
            // Return 200 OK
            respond(
                content = ByteReadChannel("""{"message": "Settings updated successfully"}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        
        val settings = UserSettingsData(
            firebaseUid = "123",
            profilePictureUrl = "https://example.com/new-photo.jpg",
            aiPersonality = "professional",
            challengeNotifsEnabled = false,
            friendReqNotifsEnabled = true,
            streakNotifsEnabled = false,
            mealNotifsEnabled = true
        )
        
        val result = upsertUserSettings(httpClient, "123", "test-jwt-token", settings)
        
        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data)
    }

    @Test
    fun `upsertUserSettings - should accept any 2xx status code as success`() = runTest {
        val mockEngine = MockEngine { request ->
            // Return 201 Created
            respond(
                content = ByteReadChannel("""{"message": "Settings created"}"""),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        
        val settings = UserSettingsData(
            firebaseUid = "123",
            aiPersonality = "helpful"
        )
        
        val result = upsertUserSettings(httpClient, "123", "test-jwt-token", settings)
        
        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data)
    }

    @Test
    fun `upsertUserSettings - should return UNAUTHORIZED on 401`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel("Unauthorized"),
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        
        val settings = UserSettingsData(
            firebaseUid = "123"
        )
        
        val result = upsertUserSettings(httpClient, "123", "test-jwt-token", settings)
        
        assertTrue(result is Result.Error)
        assertEquals(NetworkError.UNAUTHORIZED, (result as Result.Error).error)
    }

    @Test
    fun `upsertUserSettings - should return BAD_REQUEST on 400`() = runTest {
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
        
        val settings = UserSettingsData(
            firebaseUid = "123"
        )
        
        val result = upsertUserSettings(httpClient, "123", "test-jwt-token", settings)
        
        assertTrue(result is Result.Error)
        assertEquals(NetworkError.BAD_REQUEST, (result as Result.Error).error)
    }
}