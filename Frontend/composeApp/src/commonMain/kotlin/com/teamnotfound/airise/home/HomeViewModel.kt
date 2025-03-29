package com.teamnotfound.airise.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.serializable.HealthData
import com.teamnotfound.airise.generativeAi.GeminiApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

class HomeViewModel(private val email: String) :  ViewModel(){
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState
    private val geminiApi = GeminiApi()
    private val todaysHealthData = HealthData(450, 7550, 115, 80, 75, 60) //use api when ready to get real data
    private val currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    init {
        generateGreeting()
        getUsername()
        generateOverview()
        loadFitnessSummary()
    }

    fun onEvent(uiEvent: HomeUiEvent) {
        when (uiEvent) {
            is HomeUiEvent.GenerateOverview -> {
                generateOverview()
            }
            is HomeUiEvent.SelectedTimeFrameChanged -> {

                _uiState.value = _uiState.value.copy(isFitnessSummaryLoading = true, selectedTimeFrame = uiEvent.selectedTimeFrame, errorMessage = null)
                loadFitnessSummary()
            }
        }
    }
    private fun getUsername(){
        _uiState.value = _uiState.value.copy(username = email.substringBefore("@"))
    }
    private fun generateGreeting(){
        val generalGreetings = arrayOf(
            "Hello",
            "Hi",
            "Hey",
            "Welcome",
            "Greetings",
            "Howdy",
        )
        val randomIndex = Random.nextInt(generalGreetings.size)
        _uiState.value = _uiState.value.copy(greeting = generalGreetings[randomIndex])
    }
    private fun generateOverview() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOverviewLoading = true, errorMessage = null)
            try {
                val result = geminiApi.generateTodaysOverview(healthData = todaysHealthData)
                _uiState.value = _uiState.value.copy(
                    overview = result.text.toString(),
                    isOverviewLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    overview = "Error generating Today's Overview",
                    isOverviewLoading = false,
                    errorMessage = e.toString()
                )
            }
        }
    }
    private fun loadFitnessSummary() {
        // date Formatting based on time selected
        val currentDate = currentDateTime.date
        val formattedDate = when (_uiState.value.selectedTimeFrame) {
            "Daily" -> "${currentDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentDate.dayOfMonth}, ${currentDate.year}"

            "Weekly" -> {
                val weekStart = currentDate.minus(DatePeriod(days = 6))
                "${weekStart.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${weekStart.dayOfMonth} - ${currentDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentDate.dayOfMonth}, ${currentDate.year}"
            }

            "Monthly" -> {
                val monthStart = LocalDate(currentDate.year, currentDate.month, 1)
                "${monthStart.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${monthStart.dayOfMonth} - ${currentDate.dayOfMonth}, ${currentDate.year}"
            }

            "Yearly" -> {
                val yearStart = LocalDate(currentDate.year, Month.JANUARY, 1)
                "${yearStart.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${yearStart.dayOfMonth} - ${currentDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentDate.dayOfMonth}, ${currentDate.year}"
            }

            else -> "${currentDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentDate.dayOfMonth}, ${currentDate.year}"
        }
        //Use real data for given time frame once available
        val updatedHealthData = HealthData(450, 7550, 115)
        _uiState.value = _uiState.value.copy(
            formattedDateRange = formattedDate,
            healthData = updatedHealthData,
            isFitnessSummaryLoading = false
        )
    }
}