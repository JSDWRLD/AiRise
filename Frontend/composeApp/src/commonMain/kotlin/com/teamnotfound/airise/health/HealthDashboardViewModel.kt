package com.teamnotfound.airise.health

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class HealthDashboardViewModel(
    private val provider: HealthDataProvider
) : ViewModel() {

    private val _healthData = MutableStateFlow<IHealthData?>(null)
    val healthData: StateFlow<IHealthData?> = _healthData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Request permissions from the platform and then load health data.
     * This should be called when the user explicitly wants to grant permissions.
     */
    fun requestAndLoadData() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // 1) Ask once
                val granted = provider.requestPermissions()

                // 2) If still missing, stop and guide to Settings
                if (!granted) {
                    _healthData.value = null
                    _error.value = "Permissions not granted"
                    // Let the UI show a button that calls onOpenSettings()
                    return@launch
                }

                // 3) With permissions, load data (zeros are legitimate)
                _healthData.value = provider.getHealthData()

            } catch (t: Throwable) {
                _error.value = t.message
                _healthData.value = null                       // <-- key: clear it
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                _healthData.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load health data without requesting permissions.
     * Use this on screen start so we do not automatically open the OS permission dialog.
     * If reading fails (no permissions), the UI will show the permission prompt.
     */
    fun loadData() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val data = provider.getHealthData()
                _healthData.value = data
            } catch (t: Throwable) {
                _error.value = t.message
                _healthData.value = null                       // <-- key: clear it
            } catch (e: Exception) {
                // Failed to read data - likely no permissions
                // Don't set error, just leave healthData as null
                // The UI will detect this and show the permission prompt
                _error.value = e.message
                _healthData.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Write sample health data to the platform.
     * Returns true if successful, false otherwise.
     */
    suspend fun writeHealthData(): Boolean {
        val ok = provider.writeHealthData()
        if (ok) {
            // Notify subscribers that platform health changed
            kotlinx.coroutines.GlobalScope.launch {
                HealthEvents.updates.emit(Unit)
            }
        }
        return ok
    }
}