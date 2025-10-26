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
        checkHealthSyncPermissions() // Check if permissions are already granted
    }

    fun onEvent(uiEvent: HomeUiEvent) {
        when (uiEvent) {
            is HomeUiEvent.GenerateOverview -> {
                generateOverview()
            }
        }
    }

    private fun getUserProfilePic() {
        viewModelScope.launch {
            val user = Firebase.auth.currentUser
            if (user == null) {
                // not signed in â€" show default avatar or a message
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
        _uiState.value = _uiState.value.copy(
            formattedDateRange = formattedDate,
            isFitnessSummaryLoaded = true
        )
    }

    /**
     * Check if health sync permissions are already granted without requesting them.
     * This is called on init to set the initial flag state.
     */
    private fun checkHealthSyncPermissions() {
        viewModelScope.launch {
            try {
                // Try to read health data - if successful, permissions are granted
                val canReadHealth = try {
                    provider.getHealthData()
                    true
                } catch (e: Exception) {
                    false
                }

                _uiState.value = _uiState.value.copy(hasHealthSyncPermissions = canReadHealth)
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    hasHealthSyncPermissions = false,
                    errorMessage = "Unable to check health permissions"
                )
            }
        }
    }

    /**
     * Refresh health sync status and sync data if permissions are granted.
     * Call this when returning to HomeScreen to update UI after permissions may have changed.
     */
    fun refreshHealthSyncStatus() {
        viewModelScope.launch {
            try {
                // Try to read health data - if successful, permissions are granted
                val canReadHealth = try {
                    provider.getHealthData()
                    true
                } catch (e: Exception) {
                    false
                }

                val previousState = _uiState.value.hasHealthSyncPermissions
                _uiState.value = _uiState.value.copy(hasHealthSyncPermissions = canReadHealth)

                // If permissions were just granted, sync the data
                if (canReadHealth && !previousState) {
                    syncHealthOnEnter()
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Unable to refresh health sync status"
                )
            }
        }
    }

    /**
     * Sync health data only if permissions are already granted.
     * Does NOT request permissions - that should only happen via requestHealthSyncPermissions()
     */
    fun syncHealthOnEnter() {
        viewModelScope.launch {
            try {
                // Only sync if we already have permissions
                if (!uiState.value.hasHealthSyncPermissions) {
                    return@launch
                }

                val platformHealth = provider.getHealthData()

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
    }

    /**
     * Request health sync permissions. This should only be called when user explicitly
     * taps the "Enable Health Sync" CTA.
     */
    fun requestHealthSyncPermissions(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val granted = provider.requestPermissions()

                _uiState.value = _uiState.value.copy(
                    hasHealthSyncPermissions = granted,
                    errorMessage = if (!granted) "Health permissions not granted" else null
                )

                // If permissions granted, immediately sync data
                if (granted) {
                    syncHealthOnEnter()
                }

                onComplete(granted)
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    hasHealthSyncPermissions = false,
                    errorMessage = t.message ?: "Failed to request health permissions"
                )
                onComplete(false)
            }
        }
    }

    fun writeSampleHealth() {
        viewModelScope.launch {
            // Only write sample health if we have permissions
            if (!uiState.value.hasHealthSyncPermissions) {
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
            syncHealthOnEnter()
        }
    }

    private fun subscribeToHealthEvents() {
        viewModelScope.launch {
            HealthEvents.updates.collect {
                // Re-sync when platform health data changes elsewhere
                // Only sync if permissions are already granted
                if (uiState.value.hasHealthSyncPermissions) {
                    syncHealthOnEnter()
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