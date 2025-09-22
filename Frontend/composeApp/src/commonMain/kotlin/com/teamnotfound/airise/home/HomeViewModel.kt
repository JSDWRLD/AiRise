package com.teamnotfound.airise.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.repository.UserRepository
import com.teamnotfound.airise.data.serializable.DailyProgressData
import com.teamnotfound.airise.data.serializable.HealthData
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.generativeAi.GeminiApi
import com.teamnotfound.airise.health.HealthDataProvider
import com.teamnotfound.airise.util.NetworkError
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
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
/*FOR PRESENTATION
    email: nd131814@gmail.com
    password: @Aa123456
 */

class HomeViewModel(private val userRepository: UserRepository,
                    private val userClient: UserClient,
                    private val provider: HealthDataProvider
) :  ViewModel(){
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState
    private val geminiApi = GeminiApi()
    private val currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    private lateinit var todaysHealthData: HealthData
    private var hasRequestedHealthPerms = false
    private lateinit var updatedHealthData: HealthData

    init {
        generateGreeting()
        getUsername()
        getTodaysHealthData()
        generateOverview()
        loadDailyProgress()
        loadFitnessSummary()
        getUserProfilePic()
    }

    fun onEvent(uiEvent: HomeUiEvent) {
        when (uiEvent) {
            is HomeUiEvent.GenerateOverview -> {
                generateOverview()
            }
            is HomeUiEvent.SelectedTimeFrameChanged -> {

                _uiState.value = _uiState.value.copy(isFitnessSummaryLoaded = false, selectedTimeFrame = uiEvent.selectedTimeFrame, errorMessage = null)
                loadFitnessSummary()
            }
        }
    }

    fun getUserProfilePic() {
        viewModelScope.launch {
            val user = Firebase.auth.currentUser
            if (user == null) {
                // not signed in – show default avatar or a message
                _uiState.value = _uiState.value.copy(
                    userProfilePicture = null,
                    errorMessage = "Not signed in"
                )
                return@launch
            }

            when (val result = userClient.getUserSettings(user)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        userProfilePicture = result.data.profilePictureUrl,
                        errorMessage = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        userProfilePicture = null,
                        errorMessage = result.error.toString()
                    )
                }
            }
        }
    }

    private fun getUsername() {
        viewModelScope.launch {
            when (val result = userRepository.fetchUserData()) {
                is Result.Error<NetworkError> -> _uiState.value = _uiState.value.copy(username = result.error.toString(), isUserDataLoaded = true)
                is Result.Success<UserData> -> {
                    val name = result.data.firstName
                    _uiState.value = _uiState.value.copy(
                        username = name,
                        userData =  result.data,
                        isUserDataLoaded = true)
                }
            }
        }
    }
    private fun generateGreeting(){
        val generalGreetings = arrayOf(
            "Hello",
            "Hi",
            "Hey",
            "Welcome",
            "Greetings",
        )
        val randomIndex = Random.nextInt(generalGreetings.size)
        _uiState.value = _uiState.value.copy(greeting = generalGreetings[randomIndex])
    }
    private fun getTodaysHealthData(){
        //Gets overwritten by KHealth once permissions are given
        todaysHealthData = HealthData(
            caloriesBurned = 0,
            steps = 0,
            avgHeartRate = 0,
            sleep = 6.5f,
            workout = 3,
            hydration = 2850f
        )
    }
    private fun generateOverview() {
        viewModelScope.launch {
            try {
                val result = geminiApi.generateTodaysOverview(healthData = updatedHealthData)
                _uiState.value = _uiState.value.copy(
                    overview = result.text.toString(),
                    isOverviewLoaded = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    overview = "Error generating Today's Overview",
                    isOverviewLoaded = true,
                    errorMessage = e.toString()
                )
            }
        }
    }
    private fun loadDailyProgress(){
        /* Needs to use respective goal to determine percentage,
         * instead of hard coded value */
        val sleepPercentage = (todaysHealthData.sleep / 8f) * 100
        val workoutPercentage = (todaysHealthData.workout / 5f) * 100
        val hydrationPercentage = (todaysHealthData.hydration / 4000f) * 100
        val totalPercentage = (sleepPercentage + workoutPercentage + hydrationPercentage) / 3f
        val progressData = DailyProgressData(
            sleepProgress = sleepPercentage,
            workoutProgress = workoutPercentage,
            hydrationProgress = hydrationPercentage,
            totalProgress = totalPercentage
        )
        _uiState.value = _uiState.value.copy(
            dailyProgressData = progressData,
            isDailyProgressLoaded = true
        )
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
        updatedHealthData = todaysHealthData

        _uiState.value = _uiState.value.copy(
            formattedDateRange = formattedDate,
            healthData = updatedHealthData,
            isFitnessSummaryLoaded = true
        )
        _uiState.value = _uiState.value.copy(
            formattedDateRange = formattedDate,
            isFitnessSummaryLoaded = true
        )
    }

    // Sync health data on screen view
    fun syncHealthOnEnter() {
        viewModelScope.launch {
            try {
                if (!hasRequestedHealthPerms) {
                    val granted = provider.requestPermissions()
                    hasRequestedHealthPerms = true
                    if (!granted) {
                        _uiState.value = _uiState.value.copy(errorMessage = "Health permissions not granted")
                        return@launch
                    }
                }

                val platformHealth = provider.getHealthData()
                // Map platform health into serializable HealthData model
                val mapped = com.teamnotfound.airise.data.serializable.HealthData(
                    caloriesBurned = platformHealth.activeCalories,
                    steps = platformHealth.steps,
                    avgHeartRate = platformHealth.heartRate,
                    sleep =  todaysHealthData.sleep,   // TODO: wire actual sleep when added
                    workout = todaysHealthData.workout, // TODO
                    hydration = todaysHealthData.hydration // TODO
                )

                updatedHealthData = mapped

                // Update UI
                _uiState.value = _uiState.value.copy(
                    healthData = mapped,
                    isFitnessSummaryLoaded = true,
                    errorMessage = null
                )
                // Recompute progress + overview with fresh health metrics
                loadDailyProgress()
                generateOverview()

                // Push to backend (if signed in)
                Firebase.auth.currentUser?.let { user ->
                    when (val res = userClient.insertHealthData(user, mapped)) {
                        is com.teamnotfound.airise.data.network.Result.Error -> {
                            _uiState.value = _uiState.value.copy(errorMessage = res.error.toString())
                        }
                        else -> Unit
                    }
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(errorMessage = t.message ?: "Health sync failed")
            }
        }
    }

    fun writeSampleHealth() {
        viewModelScope.launch {
            // Ask KHealth to insert sample records (platform-specific actual code handles this)
            val ok = provider.writeHealthData()
            if (!ok) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to write sample health data")
                return@launch
            }
            // Refresh UI + server with the newest readings
            syncHealthOnEnter()
        }
    }
}