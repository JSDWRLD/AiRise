package com.teamnotfound.airise.onboarding

import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.data.serializable.UserDataUiState
import com.teamnotfound.airise.util.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
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

class OnboardingViewModelTest {

    private val baseUrl = "https://airise-b6aqbuerc0ewc2c5.westus-01.azurewebsites.net/api"
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @Test
    fun test_UserDataUiState_converts_to_UserData_correctly() {
        // Arrange
        val uiState = UserDataUiState().apply {
            firstName.value = "John"
            lastName.value = "Doe"
            workoutGoal.value = "Lose Weight"
            fitnessLevel.value = "Beginner"
            workoutDays.value = listOf("Monday", "Wednesday", "Friday")
            heightMetric.value = true
            heightValue.value = 180
            weightMetric.value = true
            weightValue.value = 75
            dobDay.value = 15
            dobMonth.value = 6
            dobYear.value = 1990
            activityLevel.value = "Active"
        }

        // Act
        val userData = uiState.toData()

        // Assert
        assertEquals("John", userData.firstName)
        assertEquals("Doe", userData.lastName)
        assertEquals("Lose Weight", userData.workoutGoal)
        assertEquals("Beginner", userData.fitnessLevel)
        assertEquals(listOf("Monday", "Wednesday", "Friday"), userData.workoutDays)
        assertTrue(userData.heightMetric)
        assertEquals(180, userData.heightValue)
        assertTrue(userData.weightMetric)
        assertEquals(75, userData.weightValue)
        assertEquals(15, userData.dobDay)
        assertEquals(6, userData.dobMonth)
        assertEquals(1990, userData.dobYear)
        assertEquals("Active", userData.activityLevel)
    }

    @Test
    fun `insertUserData should PUT to correct URL with auth header and JSON body`() = runTest {
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
                        "activityLevel": "Light",
                        "isAdmin": false
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
            activityLevel = "Light",
            isAdmin = false
        )

        val result = insertUserData(httpClient, "123", "test-jwt-token", userData)

        assertTrue(result is Result.Success)
        val updatedData = (result as Result.Success).data
        assertEquals("Jane", updatedData.firstName)
        assertEquals("Smith", updatedData.lastName)
    }

    @Test
    fun `insertUserData should accept 201 Created status`() = runTest {
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
                        "activityLevel": "Sedentary",
                        "isAdmin": false
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
            activityLevel = "Sedentary",
            isAdmin = false
        )

        val result = insertUserData(httpClient, "123", "test-jwt-token", userData)

        assertTrue(result is Result.Success)
        val createdData = (result as Result.Success).data
        assertEquals("New", createdData.firstName)
        assertEquals("User", createdData.lastName)
    }

    @Test
    fun `insertUserData should return CONFLICT on 409`() = runTest {
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
            activityLevel = "Sedentary",
            isAdmin = false
        )

        val result = insertUserData(httpClient, "123", "test-jwt-token", userData)

        assertTrue(result is Result.Error)
        assertEquals(NetworkError.CONFLICT, (result as Result.Error).error)
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
}
