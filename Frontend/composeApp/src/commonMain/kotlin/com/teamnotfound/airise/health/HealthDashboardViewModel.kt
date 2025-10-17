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

    private var hasRequestedPermissions = false

    fun requestAndLoadData() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Only request permissions once
                if (!hasRequestedPermissions) {
                    val granted = provider.requestPermissions()
                    hasRequestedPermissions = true

                    if (!granted) {
                        _error.value = "Permissions not granted"
                        _isLoading.value = false
                        return@launch
                    }
                }

                val data = provider.getHealthData()
                _healthData.value = data

            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load health data without requesting permissions.
     * Use this on screen start so we do not automatically open the OS permission dialog.
     * Callers that want to request permissions should use requestAndLoadData().
     */
    fun loadData() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val data = provider.getHealthData()
                _healthData.value = data
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun writeHealthData(): Boolean {
        val ok = provider.writeHealthData()
        if (ok) {
            // notify subscribers that platform health changed
            kotlinx.coroutines.GlobalScope.launch {
                HealthEvents.updates.emit(Unit)
            }
        }
        return ok
    }
}