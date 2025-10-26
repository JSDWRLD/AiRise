package com.teamnotfound.airise.community.challenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamnotfound.airise.data.network.Result
import com.teamnotfound.airise.data.network.clients.DataClient
import com.teamnotfound.airise.data.network.clients.UserClient
import com.teamnotfound.airise.util.NetworkError
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class ChallengesViewModelImpl(
    private val dataClient: DataClient,
    private val userClient: UserClient
) : ViewModel(), ChallengesViewModel {

    private val _uiState = MutableStateFlow(ChallengesUiState(isLoading = true))
    override val uiState: StateFlow<ChallengesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ChallengesEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    private val _selected = MutableStateFlow<ChallengeUI?>(null)
    val selected: StateFlow<ChallengeUI?> = _selected.asStateFlow()

    private var lastAutoResetCheckDay: Long? = null

    private fun todayEpochDayUtc(): Long {
        val today: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        // Days since 1970-01-01 UTC using LocalDate.daysUntil(...)
        return LocalDate(1970, 1, 1).daysUntil(today).toLong()
    }

    init { refresh() }

    override fun refresh() = refresh(false)

    fun refresh(force: Boolean = false) {
        // Serve snapshot if available and not forcing
        val (snapItems, snapProgress) = ChallengesCache.snapshot()
        if (!force && snapItems != null && snapProgress != null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = null,
                    items = snapItems,
                    progress = snapProgress
                )
            }
            return
        }

        // Slow path: do exactly what you already do
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadChallengesAndProgress()
    }

    private fun currentUserOrError(): FirebaseUser? {
        val u = Firebase.auth.currentUser
        if (u == null) {
            _uiState.update { it.copy(error = "Not signed in.") }
        }
        return u
    }

    private fun loadChallengesAndProgress() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            // 1) challenges
            val items = when (val r = dataClient.getChallenges()) {
                is Result.Success -> r.data.map { it.toUI() }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = r.error.humanMessage()) }
                    return@launch
                }
            }

            val user = currentUserOrError() ?: return@launch

            // 2) user challenge progress
            val baseProgress = when (val pr = userClient.getUserChallenges(user)) {
                is Result.Success -> {
                    val dto = pr.data
                    UserChallengeProgressUI(
                        activeChallengeId = dto.activeChallengeId?.ifBlank { null },
                        lastCompletionEpochDay = dto.lastCompletionEpochDay.takeIf { it != 0L }
                    )
                }
                is Result.Error -> UserChallengeProgressUI()
            }

            // 3) also ask server if completed today (single source of truth)
            val completedToday = when (val ct = userClient.hasCompletedToday(user)) {
                is Result.Success -> ct.data
                is Result.Error   -> false // fail-open
            }

            // 4) gap reset (>= 2 missed days since last completion)
            val last = baseProgress.lastCompletionEpochDay
            val delta = last?.let { todayEpochDayUtc() - it }
            if (delta != null && delta > 1L) {
                when (val rr = userClient.resetStreak(user)) {
                    is Result.Success -> Unit
                    is Result.Error   -> _uiState.update { it.copy(error = rr.error.humanMessage()) }
                }
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    items = items,
                    progress = baseProgress.copy(completedToday = completedToday)
                )
            }
            ChallengesCache.put(
                newItems = items,
                newProgress = baseProgress.copy(completedToday = completedToday)
            )
        }
    }
    override fun onChallengeClick(id: String) {
        // No navigation; optional: highlight/select
        select(id)
    }

    fun select(id: String) {
        _selected.value = _uiState.value.items.firstOrNull { it.id == id }
    }

    fun clearSelection() { _selected.value = null }

    fun enrollInChallenge(challengeId: String) {
        viewModelScope.launch {
            val user = currentUserOrError() ?: return@launch

            // NEW: don’t allow switching/starting if today is already completed
            if (_uiState.value.progress.completedToday) {
                _uiState.update { it.copy(error = "You’ve already completed a challenge today. Come back tomorrow.") }
                return@launch
            }

            when (val r = userClient.setActiveChallenge(user, challengeId)) {
                is Result.Success -> _uiState.update { s ->
                    s.copy(progress = s.progress.copy(activeChallengeId = challengeId))
                }
                is Result.Error -> _uiState.update { it.copy(error = r.error.humanMessage()) }
            }
        }
    }

    fun completeToday() {
        viewModelScope.launch {
            val user = currentUserOrError() ?: return@launch

            val activeId = _uiState.value.progress.activeChallengeId
            if (activeId == null) {
                _uiState.update { it.copy(error = "Start a challenge first.") }
                return@launch
            }

            // Quick server check; if already done, show a warning (don’t silently return)
            when (val ct = userClient.hasCompletedToday(user)) {
                is Result.Success -> if (ct.data) {
                    _uiState.update { it.copy(error = "You’ve already completed a challenge today.") }
                    return@launch
                }
                is Result.Error -> Unit // allow trying; server will enforce if needed
            }

            // Gap check
            val last = _uiState.value.progress.lastCompletionEpochDay
            val delta = last?.let { todayEpochDayUtc() - it }
            if (delta != null && delta > 1L) {
                when (val r = userClient.resetStreak(user)) {
                    is Result.Success -> Unit
                    is Result.Error -> {
                        _uiState.update { it.copy(error = r.error.humanMessage()) }
                        return@launch
                    }
                }
            }

            // POST complete-today -> server returns updated progress
            val updatedProgress = when (val mc = userClient.markCompleteToday(user)) {
                is Result.Success -> {
                    val dto = mc.data
                    UserChallengeProgressUI(
                        // keep current active if server returns blank
                        activeChallengeId = dto.activeChallengeId?.ifBlank { _uiState.value.progress.activeChallengeId },
                        // prefer server-provided epoch day; as a fallback, set to today
                        lastCompletionEpochDay = dto.lastCompletionEpochDay
                            .takeIf { it != 0L }
                            ?: todayEpochDayUtc(),
                        completedToday = true
                    )
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = mc.error.humanMessage()) }
                    return@launch
                }
            }

            // 4) Increment streak only after successful completion
            when (val inc = userClient.incrementStreak(user)) {
                is Result.Success -> {
                    _events.tryEmit(ChallengesEvent.CompletedToday)
                }
                is Result.Error -> {
                    // Surface the error but don't throw away the completion result
                    _uiState.update { it.copy(error = inc.error.humanMessage()) }
                }
            }

            // 5) Reflect in UI (single place to set progress so the screen re-renders once)
            _uiState.update { s -> s.copy(progress = updatedProgress) }
        }
    }

    fun updateSelectedDescription(newDesc: String) {
        val current = _selected.value ?: return
        val updated = current.copy(description = newDesc)
        _selected.value = updated
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.map { if (it.id == current.id) updated else it }
        )
    }

    fun checkAndAutoResetOnResume() {
        viewModelScope.launch {
            val user = currentUserOrError() ?: return@launch
            val today = todayEpochDayUtc()

            // Avoid hammering the reset endpoint repeatedly within the same day
            if (lastAutoResetCheckDay == today) return@launch

            // Get fresh progress (cheap payload) to compute delta
            val progress = when (val pr = userClient.getUserChallenges(user)) {
                is Result.Success -> pr.data
                is Result.Error -> {
                    // soft-fail; don't block UI, just stop here
                    return@launch
                }
            }

            val last = progress.lastCompletionEpochDay.takeIf { it != 0L }
            val delta = last?.let { today - it }

            if (delta != null && delta > 1L) {
                when (val rr = userClient.resetStreak(user)) {
                    is Result.Success -> {
                        // Optionally: also refresh "completed today" and reflect in UI
                        val completedToday = when (val ct = userClient.hasCompletedToday(user)) {
                            is Result.Success -> ct.data
                            is Result.Error -> false
                        }
                        _uiState.update { s ->
                            s.copy(
                                progress = s.progress.copy(
                                    // keep active as-is (server doesn’t change it on reset)
                                    lastCompletionEpochDay = last,
                                    completedToday = completedToday
                                )
                            )
                        }

                        _events.tryEmit(ChallengesEvent.CompletedToday)
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = rr.error.humanMessage()) }
                    }
                }
                // Even on error, mark we attempted today to avoid loops; remove this line
                // if you want to retry again later today on failures.
                lastAutoResetCheckDay = today
            } else {
                // No reset needed; still sync completedToday for UI accuracy
                val completedToday = when (val ct = userClient.hasCompletedToday(user)) {
                    is Result.Success -> ct.data
                    is Result.Error -> _uiState.value.progress.completedToday
                }
                _uiState.update { s ->
                    s.copy(progress = s.progress.copy(completedToday = completedToday))
                }
                lastAutoResetCheckDay = today
            }
        }
    }
}

// --- Mappers & helpers

internal fun com.teamnotfound.airise.data.serializable.Challenge.toUI(): ChallengeUI =
    ChallengeUI(
        id = id ?: name,
        name = mutableStateOf( name),
        description = mutableStateOf(description),
        imageUrl = mutableStateOf(url)
    )

// --- Mappers & helpers

private fun NetworkError.humanMessage(): String = when (this) {
    NetworkError.NO_INTERNET   -> "No internet connection."
    NetworkError.SERIALIZATION -> "Invalid response format."
    NetworkError.BAD_REQUEST   -> "Bad request."
    NetworkError.CONFLICT      -> "Conflict."
    NetworkError.SERVER_ERROR  -> "Server error."
    NetworkError.UNKNOWN       -> "Unknown error."
    else                       -> "Something went wrong."
}
