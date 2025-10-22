package com.teamnotfound.airise.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.repository.IUserRepository
import com.teamnotfound.airise.util.NetworkError
import com.teamnotfound.airise.data.serializable.UserData
import com.teamnotfound.airise.data.serializable.UserProgramDoc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import notifications.WorkoutReminderUseCase
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.teamnotfound.airise.data.serializable.UserChallenge


class WorkoutViewModel(
    private val userRepository: IUserRepository,
    private val reminder: WorkoutReminderUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Loading)
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private val _userChallenge = MutableStateFlow<UserChallenge?>(null)
    val userChallenge: StateFlow<UserChallenge?> = _userChallenge.asStateFlow()

    private val _activeDayIndex = MutableStateFlow<Int?>(null)
    val activeDayIndex: StateFlow<Int?> = _activeDayIndex.asStateFlow()

    private var hasScheduledDaily = false

    private var cachedProgramDoc: UserProgramDoc? = null
    private var cachedChallenge: UserChallenge? = null


    init {
        refresh()
    }

    private fun currentEpochDay(): Long {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return now.date.toEpochDays().toLong()
    }


    fun debugNotifyIn(seconds: Long = 60) {
        val triggerAt = Clock.System.now().toEpochMilliseconds() + seconds * 1000
        reminder.cancelActive()
        reminder.scheduleAt(
            title = "Test in ${seconds}s",
            body  = "Should show even if app is closed",
            triggerAtEpochMillis = triggerAt
        )
    }

    fun manualRefresh() {
        cachedProgramDoc = null
        cachedChallenge = null
        refresh(force = true)
    }

     fun refresh(force: Boolean = false) {
         if (!force && cachedProgramDoc != null) {
             _uiState.value = WorkoutUiState.Success(cachedProgramDoc!!)
             _userChallenge.value = cachedChallenge
             ensureDailyReminderFromState()
             return
         }

         viewModelScope.launch {
            _uiState.value = WorkoutUiState.Loading
            try {
                when (val result = userRepository.getUserProgram()) {
                    is Result.Error<NetworkError> -> _uiState.value = WorkoutUiState.Error(result.error)
                    is Result.Success<UserProgramDoc> -> {
                        cachedProgramDoc = result.data //Caching
                        _uiState.value = WorkoutUiState.Success(result.data)
                    }
                }

                val uc = try { userRepository.getUserChallengeOrNull() } catch (_: Throwable) { null }
                cachedChallenge = uc //Caching
                _userChallenge.value = uc

                if (!hasScheduledDaily) {
                    hasScheduledDaily = true
                    reminder.cancelActive()

                    val (hour, minute) = resolveUserWorkoutTimeOrFallback()
                    reminder.scheduleDailyAt(
                        hour = hour,
                        minute = minute,
                        title = "AiRise",
                        body  = "Time to workout!"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = WorkoutUiState.Error(NetworkError.UNKNOWN)
            }
        }
    }

    fun changeSet(dayIndex: Int, exerciseName: String, reps: Int?, weight: Double?) {
        val state = _uiState.value as? WorkoutUiState.Success ?: return
        val programDoc = state.programDoc

        val updatedSchedule = programDoc.program.schedule.map { day ->
            if (day.dayIndex == dayIndex) {
                day.copy(
                    exercises = day.exercises.map { ex ->
                        if (ex.name == exerciseName) {
                            val newReps = reps ?: ex.repsCompleted
                            val newWeight = weight ?: ex.weight.value
                            ex.copy(
                                repsCompleted = newReps,
                                weight = ex.weight.copy(value = newWeight.toInt())
                            )
                        } else ex
                    }
                )
            } else day
        }

        val updatedDoc = programDoc.copy(program = programDoc.program.copy(schedule = updatedSchedule))
        _uiState.value = WorkoutUiState.Success(updatedDoc)
        cachedProgramDoc = updatedDoc // Keep our cache up to date with updates
    }

    fun logAll() {
        val state = _uiState.value as? WorkoutUiState.Success ?: return
        println("Logging data: ${state.programDoc}")

        reminder.cancelActive()

        val today = currentEpochDay()
        _userChallenge.value = _userChallenge.value?.copy(lastCompletionEpochDay = today)
        val programDoc = state.programDoc

        cachedProgramDoc = programDoc

        viewModelScope.launch {
            try {
                when (val result = userRepository.updateUserProgram(programDoc.program)) {
                    is com.teamnotfound.airise.data.network.Result.Success -> {
                        println("Program saved successfully")
                        // Optionally show success message to user
                    }
                    is com.teamnotfound.airise.data.network.Result.Error -> {
                        println("Failed to save program: ${result.error}")
                        // Handle error - maybe show error message to user
                    }
                }
            } catch (e: Exception) {
                println("Error saving program: ${e.message}")
            }
        }
    }


    private fun parseTimeToHourMinute(raw: String?): Pair<Int, Int>? {
        if (raw.isNullOrBlank()) return null
        val s = raw.trim().lowercase()

        // 24h: HH:mm
        Regex("""^(\d{1,2}):(\d{2})$""").matchEntire(s)?.let {
            val h = it.groupValues[1].toInt()
            val m = it.groupValues[2].toInt()
            if (h in 0..23 && m in 0..59) return h to m
        }

        // 12h: h:mm am/pm
        Regex("""^(\d{1,2}):(\d{2})\s*([ap]m)$""").matchEntire(s)?.let {
            var h = it.groupValues[1].toInt()
            val m = it.groupValues[2].toInt()
            val ampm = it.groupValues[3]
            if (h in 1..12 && m in 0..59) {
                if (ampm == "pm" && h != 12) h += 12
                if (ampm == "am" && h == 12) h = 0
                return h to m
            }
        }
        return null
    }

    private suspend fun resolveUserWorkoutTimeOrFallback(): Pair<Int, Int> {
        var hour = 19
        var minute = 0
        when (val result = userRepository.fetchUserData()) {
            is Result.Success<UserData> -> {
                parseTimeToHourMinute(result.data.workoutTime)?.let { (h, m) ->
                    hour = h; minute = m
                }
            }
            is Result.Error -> {  }
        }
        return hour to minute
    }


    fun ensureDailyReminderFromState() {
        if (hasScheduledDaily) return
        val s = _uiState.value as? WorkoutUiState.Success ?: return
        val first = s.programDoc.program.schedule.firstOrNull() ?: return
        hasScheduledDaily = true

        viewModelScope.launch {
            reminder.cancelActive()
            val (hour, minute) = resolveUserWorkoutTimeOrFallback()
            reminder.scheduleDailyAt(
                hour = hour, minute = minute,
                title = "Workout: ${first.dayName}",
                body  = first.focus
            )
        }
    }

    fun setActiveDay(dayIndex: Int, dayTitle: String, dayFocus: String) {
        _activeDayIndex.value = dayIndex
        reminder.cancelActive()

        viewModelScope.launch {
            // default to 19:00 if we can’t load up
            var hour = 19
            var minute = 0

            when (val result = userRepository.fetchUserData()) {
                is Result.Success<UserData> -> {
                    val timeStr = result.data.workoutTime
                    parseTimeToHourMinute(timeStr)?.let { (h, m) ->
                        hour = h; minute = m
                    }
                }
                is Result.Error<NetworkError> -> {
                }
            }

            // schedule a DAILY reminder at the user’s local time
            reminder.scheduleDailyAt(
                hour = hour,
                minute = minute,
                title = "Workout: ${dayTitle.trim()}",
                body  = dayFocus.trim()
            )
        }
    }



    fun onWorkoutLogged() {
        reminder.cancelActive()
    }
}
