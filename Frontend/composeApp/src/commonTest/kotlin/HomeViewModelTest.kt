package com.teamnotfound.airise.meal

import com.teamnotfound.airise.data.network.clients.DataClient
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.serializable.FoodDiaryMonth
import com.teamnotfound.airise.data.serializable.FoodEntry
import com.teamnotfound.airise.data.serializable.DiaryDay
import com.teamnotfound.airise.data.serializable.Meals
import dev.gitlive.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FoodDiaryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val mockDataClient = mockk<DataClient>()
    private val mockFirebaseUser = mockk<FirebaseUser>()
    private lateinit var viewModel: FoodDiaryViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { mockFirebaseUser.uid } returns "test-user-123"
        coEvery { mockFirebaseUser.getIdToken(any()) } returns "test-token"

        viewModel = FoodDiaryViewModel(mockDataClient, mockFirebaseUser)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have current date`() = testScope.runTest {
        val currentDate = LocalDate(2024, 1, 15)
        // You might need to mock the Clock for consistent testing

        val uiState = viewModel.uiState.value
        assertNotNull(uiState.currentDate)
    }

    @Test
    fun `loadMonth should update state with month data`() = testScope.runTest {
        val testMonthData = createTestMonthData(2024, 1)

        coEvery {
            mockDataClient.getFoodDiaryMonth(mockFirebaseUser, 2024, 1)
        } returns Result.Success(testMonthData)

        viewModel.setCurrentDate(LocalDate(2024, 1, 15))
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(false, uiState.isLoading)
        assertNotNull(uiState.monthData)
        assertEquals(2024, uiState.monthData!!.year)
        assertEquals(1, uiState.monthData!!.month)
    }

    @Test
    fun `loadMonth should handle errors`() = testScope.runTest {
        coEvery {
            mockDataClient.getFoodDiaryMonth(mockFirebaseUser, 2024, 1)
        } returns Result.Error(com.teamnotfound.airise.util.NetworkError.NO_INTERNET)

        viewModel.setCurrentDate(LocalDate(2024, 1, 15))
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(false, uiState.isLoading)
        assertNotNull(uiState.errorMessage)
        assertTrue(uiState.errorMessage!!.contains("Failed to load month data"))
    }

    @Test
    fun `addFoodEntry should call data client and reload data`() = testScope.runTest {
        val testMonthData = createTestMonthData(2024, 1)
        val testFoodEntry = FoodEntry(
            id = "test-entry",
            name = "Test Food",
            calories = 250.0,
            fats = 10.0,
            carbs = 30.0,
            proteins = 15.0
        )

        coEvery {
            mockDataClient.addFoodEntry(mockFirebaseUser, 2024, 1, 15, "breakfast", testFoodEntry)
        } returns Result.Success(Unit)

        coEvery {
            mockDataClient.getFoodDiaryMonth(mockFirebaseUser, 2024, 1)
        } returns Result.Success(testMonthData)

        viewModel.setCurrentDate(LocalDate(2024, 1, 15))
        viewModel.addFoodEntry("breakfast", testFoodEntry)
        advanceUntilIdle()

        // Verify that month data was reloaded after adding entry
        assertNotNull(viewModel.uiState.value.monthData)
    }

    @Test
    fun `deleteFoodEntry should call data client and reload data`() = testScope.runTest {
        val testMonthData = createTestMonthData(2024, 1)

        coEvery {
            mockDataClient.deleteFoodEntry(mockFirebaseUser, "test-entry-id")
        } returns Result.Success(Unit)

        coEvery {
            mockDataClient.getFoodDiaryMonth(mockFirebaseUser, 2024, 1)
        } returns Result.Success(testMonthData)

        viewModel.setCurrentDate(LocalDate(2024, 1, 15))
        viewModel.deleteFoodEntry("test-entry-id")
        advanceUntilIdle()

        // Verify that month data was reloaded after deletion
        assertNotNull(viewModel.uiState.value.monthData)
    }

    @Test
    fun `getDailyCalories should return correct total`() = testScope.runTest {
        val testMonthData = createTestMonthData(2024, 1)

        coEvery {
            mockDataClient.getFoodDiaryMonth(mockFirebaseUser, 2024, 1)
        } returns Result.Success(testMonthData)

        viewModel.setCurrentDate(LocalDate(2024, 1, 15))
        advanceUntilIdle()

        val calories = viewModel.getDailyCalories(LocalDate(2024, 1, 15))
        assertEquals(750.0, calories) // 250 + 300 + 200 from test data
    }

    @Test
    fun `date navigation should update current date`() = testScope.runTest {
        val initialDate = LocalDate(2024, 1, 15)
        viewModel.setCurrentDate(initialDate)

        viewModel.nextDay()
        assertEquals(initialDate.plus(1, DateTimeUnit.DAY), viewModel.uiState.value.currentDate)

        viewModel.previousDay()
        assertEquals(initialDate, viewModel.uiState.value.currentDate)
    }

    private fun createTestMonthData(year: Int, month: Int): FoodDiaryMonth {
        val testDay = DiaryDay(
            day = 15,
            meals = Meals(
                breakfast = listOf(
                    FoodEntry("1", "Breakfast Item", 250.0, 5.0, 30.0, 10.0)
                ),
                lunch = listOf(
                    FoodEntry("2", "Lunch Item", 300.0, 10.0, 40.0, 15.0)
                ),
                dinner = listOf(
                    FoodEntry("3", "Dinner Item", 200.0, 8.0, 25.0, 12.0)
                )
            ),
            totalCalories = 750.0
        )

        val days = List(31) { index ->
            if (index == 14) testDay else null // day 15 is at index 14
        }

        return FoodDiaryMonth(
            id = "test-month",
            userId = "test-user",
            year = year,
            month = month,
            days = days
        )
    }
}