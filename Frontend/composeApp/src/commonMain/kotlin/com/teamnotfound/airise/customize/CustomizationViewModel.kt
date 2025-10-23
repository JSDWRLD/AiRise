package com.teamnotfound.airise.customize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.auth.AuthService
import com.teamnotfound.airise.data.cache.SummaryCache
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.data.serializable.UserDataUiState
import com.teamnotfound.airise.util.NetworkError
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CustomizationViewModel(
    private val authService: AuthService,
    private val summaryCache: SummaryCache,
    private val userClient: UserClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomizationUiState())
    val uiState: StateFlow<CustomizationUiState> = _uiState

    private var base: UserDataUiState = UserDataUiState()

    fun seedFrom(user: UserDataUiState) {
        base = user
        _uiState.value = _uiState.value.copy(
            isLoaded = true,
            error = null,
            initialDays = user.workoutDays.value,
            initialLength = user.workoutLength.value,
            initialEquipmentKey = user.equipmentAccess.value.ifBlank { "bodyweight" }
        )
    }


    fun loadFromServer() {
        if (_uiState.value.isLoaded) return
        viewModelScope.launch {
            try {
                val user = authService.firebaseUser ?: run {
                    _uiState.value = _uiState.value.copy(error = "Unauthorized access.")
                    return@launch
                }
                when (val res = userClient.getUserData(user)) {
                    is Result.Success -> {
                        val u = res.data
                        val full = UserDataUiState().apply {
                            firstName.value            = u.firstName
                            middleName.value           = u.middleName
                            lastName.value             = u.lastName
                            fullName.value             = u.fullName

                            workoutGoal.value          = u.workoutGoal
                            fitnessLevel.value         = u.fitnessLevel
                            workoutLength.value        = u.workoutLength
                            dietaryGoal.value          = u.dietaryGoal
                            workoutRestrictions.value  = u.workoutRestrictions
                            activityLevel.value        = u.activityLevel

                            workoutDays.value          = u.workoutDays
                            workoutTime.value          = u.workoutTime
                            equipmentAccess.value      = u.workoutEquipment

                            heightMetric.value         = u.heightMetric
                            weightMetric.value         = u.weightMetric
                            heightValue.value          = u.heightValue
                            weightValue.value          = u.weightValue

                            dobDay.value               = u.dobDay
                            dobMonth.value             = u.dobMonth
                            dobYear.value              = u.dobYear
                        }
                        seedFrom(full)
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(error = mapError(res.error))
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private suspend fun ensureLoadedOrError(): Boolean {
        if (_uiState.value.isLoaded) return true
        val user = authService.firebaseUser ?: run {
            _uiState.value = _uiState.value.copy(error = "Unauthorized access.")
            return false
        }
        return when (val res = userClient.getUserData(user)) {
            is Result.Success -> {
                val u = res.data
                val full = UserDataUiState().apply {
                    firstName.value            = u.firstName
                    middleName.value           = u.middleName
                    lastName.value             = u.lastName
                    fullName.value             = u.fullName
                    workoutGoal.value          = u.workoutGoal
                    fitnessLevel.value         = u.fitnessLevel
                    workoutLength.value        = u.workoutLength
                    dietaryGoal.value          = u.dietaryGoal
                    workoutRestrictions.value  = u.workoutRestrictions
                    activityLevel.value        = u.activityLevel
                    workoutDays.value          = u.workoutDays
                    workoutTime.value          = u.workoutTime
                    equipmentAccess.value      = u.workoutEquipment
                    heightMetric.value         = u.heightMetric
                    weightMetric.value         = u.weightMetric
                    heightValue.value          = u.heightValue
                    weightValue.value          = u.weightValue
                    dobDay.value               = u.dobDay
                    dobMonth.value             = u.dobMonth
                    dobYear.value              = u.dobYear
                }
                seedFrom(full)
                true
            }
            is Result.Error -> {
                _uiState.value = _uiState.value.copy(error = mapError(res.error))
                false
            }
        }
    }

    fun save(update: OnboardingDataUpdate) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, isSaved = false, error = null)

            val user = authService.firebaseUser ?: run {
                _uiState.value = _uiState.value.copy(isSaving = false, error = "Unauthorized access.")
                return@launch
            }

            // must be fully loaded
            if (!_uiState.value.isLoaded) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = "Please wait, loading your profile…")
                return@launch
            }

            // merge only what was needed and not blank required fields
            update.workoutDays?.let { days ->
                if (days.isNotEmpty()) base.workoutDays.value = days
            }
            update.workoutLength?.let {
                if (it > 0) base.workoutLength.value = it
            }
            update.workoutEquipment?.let { eq ->
                if (eq.isNotBlank()) base.equipmentAccess.value = eq
            }

            try {
                val payload = base.toData() // userdata unchanged fields kept
                when (val res = userClient.insertUserData(user, payload)) {
                    is Result.Success -> {
                        summaryCache.cacheSummary(payload)
                        _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true, error = null)
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false, isSaved = false, error = mapError(res.error)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, isSaved = false, error = e.message)
            }
        }
    }

    fun getUserClient(): UserClient = userClient
    
    fun getFirebaseUser(): FirebaseUser? = authService.firebaseUser

    private fun mapError(error: NetworkError): String = when (error) {
        NetworkError.NO_INTERNET       -> "No internet connection."
        NetworkError.SERIALIZATION     -> "Data error. Please try again."
        NetworkError.UNAUTHORIZED      -> "Unauthorized access."
        NetworkError.CONFLICT          -> "User already exists."
        NetworkError.UNKNOWN           -> "Unknown error occurred."
        NetworkError.REQUEST_TIMEOUT   -> "Request timed out."
        NetworkError.TOO_MANY_REQUESTS -> "Too many requests. Please try again later."
        NetworkError.PAYLOAD_TOO_LARGE -> "Data too large to process."
        NetworkError.SERVER_ERROR      -> "Server error occurred."
        NetworkError.BAD_REQUEST       -> "Invalid request. Please check your data."
    }
}

data class CustomizationUiState(
    val isLoaded: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,

    val initialDays: List<String> = emptyList(),
    val initialLength: Int = 30,
    val initialEquipmentKey: String = "bodyweight",
)
