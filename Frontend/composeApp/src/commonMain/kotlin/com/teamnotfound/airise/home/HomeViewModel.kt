package com.teamnotfound.airise.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.repository.IUserRepository
import com.teamnotfound.airise.data.serializable.DailyProgressData
import com.teamnotfound.airise.data.serializable.HealthData
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.generativeAi.GeminiApi
import com.teamnotfound.airise.health.HealthDataProvider
import com.teamnotfound.airise.health.HealthEvents
import com.teamnotfound.airise.util.NetworkError
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.min
import kotlin.random.Random

class HomeViewModel(private val userRepository: IUserRepository,
                    private val userClient: UserClient,
                    private val provider: HealthDataProvider
) :  ViewModel(){
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState
    private val geminiApi = GeminiApi()
    private val currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    init {
        generateGreeting()
        getUsername()
        getHealthDataAndLoadWithData()
        subscribeToHealthEvents()
        // Try to read health data on init without requesting permissions
        tryReadHealthDataSilently()
    }

    fun onEvent(uiEvent: HomeUiEvent) {
        when (uiEvent) {
            is HomeUiEvent.GenerateOverview -> {
                generateOverview()
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

    private fun getHealthDataAndLoadWithData(){
        viewModelScope.launch {
            when (val result = userRepository.getHealthData()) {
                is Result.Error<NetworkError> -> _uiState.value = _uiState.value.copy(username = result.error.toString(), isUserDataLoaded = true)
                is Result.Success<HealthData> -> {
                    _uiState.value = _uiState.value.copy(healthData =  result.data)
                }
            }
            generateOverview()
            loadDailyProgress()
            loadFitnessSummary()
            getUserProfilePic()
        }

    }

    private fun generateOverview() {
        viewModelScope.launch {
            try {
                val result = geminiApi.generateTodaysOverview(
                    healthData = uiState.value.healthData,
                    dailyProgress = uiState.value.dailyProgressData
                )
                _uiState.value = _uiState.value.copy(
                    overview = result.text.toString(),
                    isOverviewLoaded = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                val fallbackOverview = generateFallbackOverview(uiState.value.healthData)
                _uiState.value = _uiState.value.copy(
                    overview = fallbackOverview,
                    isOverviewLoaded = true,
                    errorMessage = "Couldn't generate AI summary. Showing basic data."
                )
            }
        }
    }

    private fun generateFallbackOverview(data: HealthData): String {
        val stepsComment = when {
            data.steps > 10000 -> "Fantastic job on your steps today! You're crushing it."
            data.steps > 5000 -> "Great work staying active! You're well on your way to your step goal."
            data.steps > 0 -> "A good start to the day. Let's keep that momentum going!"
            else -> "Ready to get moving? Every step counts towards your goal!"
        }

        val caloriesComment = when {
            data.caloriesBurned > 500 -> "You've been burning a lot of energy. Excellent effort!"
            data.caloriesBurned > 200 -> "You're making solid progress on your calorie burn. Keep it up!"
            data.caloriesBurned > 0 -> "You've started the day strong. Let's see what else you can do!"
            else -> "Your body is fueled and ready for a great workout today."
        }

        return "Here's your current progress report for the day:\n" +
                "Activity: $stepsComment\n" +
                "Energy: $caloriesComment"
    }

    private fun loadDailyProgress(){
        /* Needs to use respective goal to determine percentage,
         * instead of hard coded value */
        val healthData = uiState.value.healthData
        val sleepPercentage = min(healthData.sleep.toFloat() / 8f, 1f) * 100
        val caloriesPercentage = min(healthData.caloriesEaten / healthData.caloriesTarget.toFloat(), 1f ) * 100
        val hydrationPercentage = min(healthData.hydration.toFloat() / healthData.hydrationTarget.toFloat(), 1f) * 100
        val totalPercentage = (sleepPercentage + caloriesPercentage + hydrationPercentage) / 3f
        val progressData = DailyProgressData(
            sleepProgress = sleepPercentage,
            caloriesProgress = caloriesPercentage,
            hydrationProgress = hydrationPercentage,
            totalProgress = totalPercentage
        )
        _uiState.value = _uiState.value.copy(
            dailyProgressData = progressData,
            isDailyProgressLoaded = true
        )
    }

    private fun loadFitnessSummary() {
        // date Formatting based on date
        val currentDate = currentDateTime.date
        val formattedDate = "${currentDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentDate.dayOfMonth}, ${currentDate.year}"

        _uiState.value = _uiState.value.copy(
            formattedDateRange = formattedDate,
            healthData = uiState.value.healthData,
            isFitnessSummaryLoaded = true
        )
    }

    /**
     * Try to read health data without requesting permissions.
     * This silently checks if we can access health data and updates the state accordingly.
     * If read succeeds, canReadHealthData is set to true and data is synced.
     * If read fails OR returns all zeros (likely revoked permissions), canReadHealthData is set to false.
     */
    private fun tryReadHealthDataSilently() {
        viewModelScope.launch {
            try {
                // Try to read health data without requesting permissions
                val platformHealth = provider.getHealthData()

                // Check if we got meaningful data (not all zeros)
                // If all zeros, permissions were likely revoked or never granted
                val hasMeaningfulData = platformHealth.steps > 0 ||
                        platformHealth.caloriesBurned > 0 ||
                        platformHealth.sleep > 0.0

                if (hasMeaningfulData) {
                    // We successfully read meaningful data - permissions are granted
                    _uiState.value = _uiState.value.copy(canReadHealthData = true)
                    syncHealthData(platformHealth)
                } else {
                    // All zeros - likely no permissions or no activity
                    // For safety, assume no permissions and show sync button
                    // This also handles the case where permissions were just revoked
                    _uiState.value = _uiState.value.copy(
                        canReadHealthData = false,
                        errorMessage = null,
                        // Reset health data to zeros when we detect no permissions
                        healthData = _uiState.value.healthData.copy(
                            steps = 0,
                            caloriesBurned = 0,
                            sleep = 0.0
                        )
                    )
                }
            } catch (e: Exception) {
                // Failed to read health data - definitely no permissions
                _uiState.value = _uiState.value.copy(
                    canReadHealthData = false,
                    errorMessage = null,
                    // Reset health data to zeros
                    healthData = _uiState.value.healthData.copy(
                        steps = 0,
                        caloriesBurned = 0,
                        sleep = 0.0
                    )
                )
            }
        }
    }

    /**
     * Request health sync permissions from the user.
     * This should only be called when user explicitly taps the "Enable Health Sync" button.
     * After permissions are granted, automatically syncs health data.
     */
    fun requestHealthSyncPermissions(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Request permissions from the platform
                val granted = provider.requestPermissions()

                if (granted) {
                    // Permissions granted - try to read health data
                    try {
                        val platformHealth = provider.getHealthData()

                        // Successfully read data after permissions granted
                        _uiState.value = _uiState.value.copy(
                            canReadHealthData = true,
                            errorMessage = null
                        )

                        // Sync the health data
                        syncHealthData(platformHealth)
                        onComplete(true)
                    } catch (e: Exception) {
                        // Permissions granted but still can't read data
                        _uiState.value = _uiState.value.copy(
                            canReadHealthData = false,
                            errorMessage = "Unable to read health data: ${e.message}"
                        )
                        onComplete(false)
                    }
                } else {
                    // Permissions not granted
                    _uiState.value = _uiState.value.copy(
                        canReadHealthData = false,
                        errorMessage = "Health permissions not granted"
                    )
                    onComplete(false)
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    canReadHealthData = false,
                    errorMessage = t.message ?: "Failed to request health permissions"
                )
                onComplete(false)
            }
        }
    }

    /**
     * Sync platform health data to our app's health data model.
     * This updates steps, calories burned, and sleep while preserving user-entered hydration.
     * Also updates the backend and refreshes the UI.
     */
    private suspend fun syncHealthData(platformHealth: com.teamnotfound.airise.health.IHealthData) {
        try {
            // Map platform health into serializable HealthData model
            // NOTE: Hydration is NOT fetched from KHealth - user-entered hydration remains intact
            val mapped = HealthData(
                caloriesEaten = uiState.value.healthData.caloriesEaten,
                caloriesTarget = uiState.value.healthData.caloriesTarget,
                hydrationTarget = uiState.value.healthData.hydrationTarget,
                hydration = uiState.value.healthData.hydration, // Keep existing user-entered hydration
                caloriesBurned = platformHealth.caloriesBurned,
                steps = platformHealth.steps,
                sleep = platformHealth.sleep
            )

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
                when (val res = userClient.updateHealthData(user, mapped)) {
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(errorMessage = res.error.toString())
                    }
                    else -> Unit
                }
            }
        } catch (t: Throwable) {
            _uiState.value = _uiState.value.copy(errorMessage = t.message ?: "Health sync failed")
        }
    }

    /**
     * Manually refresh health data from the platform.
     * This is called when the user wants to manually sync or when health data changes in the background.
     * Only syncs if we already have permission to read health data.
     */
    fun refreshHealthData() {
        viewModelScope.launch {
            // Only try to sync if we can already read health data
            if (!uiState.value.canReadHealthData) {
                return@launch
            }

            try {
                val platformHealth = provider.getHealthData()
                syncHealthData(platformHealth)
            } catch (e: Exception) {
                // Lost ability to read health data
                _uiState.value = _uiState.value.copy(
                    canReadHealthData = false,
                    errorMessage = "Unable to sync health data: ${e.message}"
                )
            }
        }
    }

    /**
     * Refresh health sync status when returning to HomeScreen.
     * This checks if we can now read health data (e.g., after granting permissions in Health Dashboard).
     * Call this when HomeScreen resumes or becomes visible.
     */
    fun refreshHealthSyncStatus() {
        viewModelScope.launch {
            try {
                // Try to read health data - if successful, permissions are now granted
                val platformHealth = provider.getHealthData()

                // Check if we got meaningful data (not all zeros)
                val hasMeaningfulData = platformHealth.steps > 0 ||
                        platformHealth.caloriesBurned > 0 ||
                        platformHealth.sleep > 0.0

                val previousState = _uiState.value.canReadHealthData

                if (hasMeaningfulData) {
                    // Successfully read meaningful data - we have permissions
                    _uiState.value = _uiState.value.copy(canReadHealthData = true)

                    // If we just gained ability to read data, sync it
                    if (!previousState) {
                        syncHealthData(platformHealth)
                    } else {
                        // Just refresh the data
                        syncHealthData(platformHealth)
                    }
                } else {
                    // All zeros - likely no permissions
                    _uiState.value = _uiState.value.copy(
                        canReadHealthData = false,
                        // Reset health data to zeros
                        healthData = _uiState.value.healthData.copy(
                            steps = 0,
                            caloriesBurned = 0,
                            sleep = 0.0
                        )
                    )
                }
            } catch (e: Exception) {
                // Still can't read health data
                _uiState.value = _uiState.value.copy(
                    canReadHealthData = false,
                    // Reset health data to zeros
                    healthData = _uiState.value.healthData.copy(
                        steps = 0,
                        caloriesBurned = 0,
                        sleep = 0.0
                    )
                )
            }
        }
    }

    fun writeSampleHealth() {
        viewModelScope.launch {
            // Only write sample health if we can read health data (implies permissions)
            if (!uiState.value.canReadHealthData) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Health sync permissions required to write sample data"
                )
                return@launch
            }

            // Ask KHealth to insert sample records (platform-specific actual code handles this)
            val ok = provider.writeHealthData()
            if (!ok) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to write sample health data")
                return@launch
            }
            // Refresh UI + server with the newest readings
            refreshHealthData()
        }
    }

    private fun subscribeToHealthEvents() {
        viewModelScope.launch {
            HealthEvents.updates.collect {
                // Re-sync when platform health data changes elsewhere
                // Only sync if we can already read health data
                if (uiState.value.canReadHealthData) {
                    refreshHealthData()
                }
            }
        }
    }

    fun updateHydration(newHydration: Double) {
        viewModelScope.launch {
            try {
                val updatedHealthData = _uiState.value.healthData.copy(
                    hydration = newHydration
                )

                _uiState.value = _uiState.value.copy(
                    healthData = updatedHealthData
                )

                // Update backend
                Firebase.auth.currentUser?.let { user ->
                    when (val result = userClient.updateHealthData(user, updatedHealthData)) {
                        is Result.Success -> {
                            _uiState.value = _uiState.value.copy(errorMessage = null)
                        }
                        is Result.Error -> {
                            _uiState.value = _uiState.value.copy(errorMessage = "Failed to update hydration: ${result.error}")
                        }
                    }
                }

                // Update daily progress with new hydration data
                loadDailyProgress()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error updating hydration: ${e.message}")
            }
        }
    }

    // Used for Unit testing
    data class ProviderOverrides(
        val requestPermissions: (suspend () -> Boolean)? = null,
        val getMappedHealthData: (suspend () -> HealthData)? = null,
        val writeHealthData: (suspend () -> Boolean)? = null
    )
}